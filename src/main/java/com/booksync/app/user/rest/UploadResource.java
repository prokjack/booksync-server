package com.booksync.app.user.rest;

import com.booksync.app.config.props.S3ClientConfigurationProperties;
import com.booksync.app.user.exceptions.UploadFailedException;
import com.booksync.app.user.model.ApiResponse;
import com.booksync.app.user.model.S3Upload;
import com.booksync.app.user.model.UploadResult;
import com.booksync.app.user.repository.S3UploadsRepository;
import com.booksync.app.user.util.S3MetadataHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.translate.UnicodeEscaper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/inbox")
@RequiredArgsConstructor
@Slf4j
public class UploadResource {
    private final S3AsyncClient s3client;
    private final S3ClientConfigurationProperties s3config;
    private final S3UploadsRepository s3UploadsRepository;


    /**
     * Standard file upload.
     */
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<UploadResult>>> uploadHandler(@RequestHeader HttpHeaders headers, @RequestBody Flux<ByteBuffer> body) {

        long length = headers.getContentLength();
        if (length < 0) {
            throw new UploadFailedException(HttpStatus.BAD_REQUEST.value(), Optional.of("required header missing: Content-Length"));
        }

        String fileKey = UUID.randomUUID().toString();
        Map<String, String> metadata = new HashMap<>();
        MediaType mediaType = headers.getContentType();

        if (mediaType == null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        log.info("[I95] uploadHandler: mediaType{}, length={}", mediaType, length);
        CompletableFuture<PutObjectResponse> future = s3client
                .putObject(PutObjectRequest.builder()
                                .bucket(s3config.getBucket())
                                .contentLength(length)
                                .key(fileKey)
                                .contentType(mediaType.toString())
                                .metadata(metadata)
                                .build(),
                        AsyncRequestBody.fromPublisher(body));

        return Mono.fromFuture(future)
                .map((response) -> {
                    checkResult(response);
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(new ApiResponse<>(ApiResponse.Status.SUCCESS, new UploadResult(HttpStatus.CREATED, new String[]{fileKey})));
                });
    }

    /**
     * Multipart file upload
     *
     * @param parts
     * @param headers
     * @return
     */
    @RequestMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = {RequestMethod.POST, RequestMethod.PUT})
    public Mono<ResponseEntity<UploadResult>> multipartUploadHandler(@RequestHeader HttpHeaders headers, @RequestBody Flux<Part> parts, Principal user) {

        // TODO: check if storage have file with same name, save metadata of file


        return parts
                .ofType(FilePart.class) // We'll ignore other data for now
                .flatMap((part) -> saveFile(headers, s3config.getBucket(), part)
                        .flatMap(key -> s3UploadsRepository.save(new S3Upload(key, part.filename(), user.getName()))
                                .map(up -> key)))
                .collect(Collectors.toList())
                .map((keys) -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new UploadResult(HttpStatus.CREATED, keys)));
    }


    /**
     * Save file using a multipart upload. This method does not require any temporary
     * storage at the REST service
     *
     * @param headers
     * @param bucket  Bucket name
     * @param part    Uploaded file
     * @return
     */
    protected Mono<String> saveFile(HttpHeaders headers, String bucket, FilePart part) {

        // Generate a filekey for this upload
        String filekey = UUID.randomUUID().toString();

        log.info("[I137] saveFile: filekey={}, filename={}", filekey, part.filename());

        // Gather metadata
        Map<String, String> metadata = new HashMap<String, String>();
        String filename = part.filename();
        if (filename == null) {
            filename = filekey;
        }

        metadata.put("filename", S3MetadataHelper.getAsciiStringBytes(filename));

        MediaType mt = part.headers().getContentType();
        if (mt == null) {
            mt = MediaType.APPLICATION_OCTET_STREAM;
        }

        // Create multipart upload request
        CompletableFuture<CreateMultipartUploadResponse> uploadRequest = s3client
                .createMultipartUpload(CreateMultipartUploadRequest.builder()
                        .contentType(mt.toString())
                        .key(filekey)
                        .metadata(metadata)
                        .bucket(bucket)
                        .build());

        // This variable will hold the upload state that we must keep
        // around until all uploads complete
        final UploadState uploadState = new UploadState(bucket, filekey);

        return Mono
                .fromFuture(uploadRequest)
                .flatMapMany((response) -> {
                    checkResult(response);
                    uploadState.uploadId = response.uploadId();
                    log.info("[I183] uploadId={}", response.uploadId());
                    return part.content();
                })
                .bufferUntil((buffer) -> {
                    uploadState.buffered += buffer.readableByteCount();
                    if (uploadState.buffered >= s3config.getMultipartMinPartSize()) {
                        log.info("[I173] bufferUntil: returning true, bufferedBytes={}, partCounter={}, uploadId={}", uploadState.buffered, uploadState.partCounter, uploadState.uploadId);
                        uploadState.buffered = 0;
                        return true;
                    } else {
                        return false;
                    }
                })
                .map(UploadResource::concatBuffers)
                .flatMap((buffer) -> uploadPart(uploadState, buffer))
                .onBackpressureBuffer()
                .reduce(uploadState, (state, completedPart) -> {
                    log.info("[I188] completed: partNumber={}, etag={}", completedPart.partNumber(), completedPart.eTag());
                    state.completedParts.put(completedPart.partNumber(), completedPart);
                    return state;
                })
                .flatMap((state) -> completeUpload(state))
                .map((response) -> {
                    checkResult(response);
                    return uploadState.filekey;
                });
    }

    private static ByteBuffer concatBuffers(List<DataBuffer> buffers) {
        log.info("[I198] creating BytBuffer from {} chunks", buffers.size());

        int partSize = 0;
        for (DataBuffer b : buffers) {
            partSize += b.readableByteCount();
        }

        ByteBuffer partData = ByteBuffer.allocate(partSize);
        buffers.forEach((buffer) -> {
            partData.put(buffer.asByteBuffer());
        });

        // Reset read pointer to first byte
        partData.rewind();

        log.info("[I208] partData: size={}", partData.capacity());
        return partData;

    }

    /**
     * Upload a single file part to the requested bucket
     *
     * @param uploadState
     * @param buffer
     * @return
     */
    private Mono<CompletedPart> uploadPart(UploadState uploadState, ByteBuffer buffer) {
        final int partNumber = ++uploadState.partCounter;
        log.info("[I218] uploadPart: partNumber={}, contentLength={}", partNumber, buffer.capacity());

        CompletableFuture<UploadPartResponse> request = s3client.uploadPart(UploadPartRequest.builder()
                        .bucket(uploadState.bucket)
                        .key(uploadState.filekey)
                        .partNumber(partNumber)
                        .uploadId(uploadState.uploadId)
                        .contentLength((long) buffer.capacity())
                        .build(),
                AsyncRequestBody.fromPublisher(Mono.just(buffer)));

        return Mono
                .fromFuture(request)
                .map((uploadPartResult) -> {
                    checkResult(uploadPartResult);
                    log.info("[I230] uploadPart complete: part={}, etag={}", partNumber, uploadPartResult.eTag());
                    return CompletedPart.builder()
                            .eTag(uploadPartResult.eTag())
                            .partNumber(partNumber)
                            .build();
                });
    }

    private Mono<CompleteMultipartUploadResponse> completeUpload(UploadState state) {
        log.info("[I202] completeUpload: bucket={}, filekey={}, completedParts.size={}", state.bucket, state.filekey, state.completedParts.size());

        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
                .parts(state.completedParts.values())
                .build();

        return Mono.fromFuture(s3client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(state.bucket)
                .uploadId(state.uploadId)
                .multipartUpload(multipartUpload)
                .key(state.filekey)
                .build()));
    }


    /**
     * check result from an API call.
     *
     * @param result Result from an API call
     */
    private static void checkResult(SdkResponse result) {
        if (result.sdkHttpResponse() == null || !result.sdkHttpResponse().isSuccessful()) {
            throw new UploadFailedException(result);
        }
    }


    /**
     * Holds upload state during a multipart upload
     */
    static class UploadState {
        final String bucket;
        final String filekey;

        String uploadId;
        int partCounter;
        Map<Integer, CompletedPart> completedParts = new HashMap<>();
        int buffered = 0;

        UploadState(String bucket, String filekey) {
            this.bucket = bucket;
            this.filekey = filekey;
        }
    }
}