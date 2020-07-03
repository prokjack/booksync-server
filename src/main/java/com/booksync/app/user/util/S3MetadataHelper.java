package com.booksync.app.user.util;

import com.nimbusds.jose.util.StandardCharset;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class S3MetadataHelper {
    public static String getAsciiStringBytes(String utfString) {
        byte[] bytes = utfString.getBytes(StandardCharsets.UTF_8);
        return Arrays.toString(bytes);
    }

    public static String getUtfStringBytes(String asciiString) {
        String[] byteValues = asciiString.substring(1, asciiString.length() - 1).split(",");
        byte[] bytes = new byte[byteValues.length];

        for (int i=0, len=bytes.length; i<len; i++) {
            bytes[i] = Byte.parseByte(byteValues[i].trim());
        }

        return new String(bytes);
    }
}
