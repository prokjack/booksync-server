package com.booksync.app.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	@Override
	public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		if (parser.getCurrentToken() == JsonToken.VALUE_STRING) {
			String rawDate = parser.getText();
			return FORMATTER.parse(rawDate, LocalDate::from);
		}
		throw context.wrongTokenException(parser, JsonToken.START_ARRAY, "Expected string.");
	}
}