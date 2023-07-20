package org.swmaestro.repl.gifthub.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ISO8601Converter {
	public static LocalDateTime iso8601ToLocalDateTime(String iso8601) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return LocalDateTime.parse(iso8601, formatter);
	}

	public static String localDateTimeToIso8601(LocalDateTime localDateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		return localDateTime.format(formatter);
	}
}
