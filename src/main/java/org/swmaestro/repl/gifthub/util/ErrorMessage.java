package org.swmaestro.repl.gifthub.util;

import java.sql.Timestamp;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ErrorMessage extends Message {
	private String error;

	@Builder
	public ErrorMessage(int status, String path, String error, Timestamp timestamp) {
		super(status, path, timestamp);
		this.error = error;
	}
}
