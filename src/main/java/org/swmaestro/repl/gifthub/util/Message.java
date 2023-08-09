package org.swmaestro.repl.gifthub.util;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Message {
	private int statusCode;
	private String message;
	private Object data;

	public Message() {
		this.statusCode = StatusEnum.BAD_REQUEST.statusCode;
		this.message = null;
		this.data = null;
	}

	@Builder
	public Message(StatusEnum status, String message, Object data) {
		this.statusCode = status.statusCode;
		this.message = message;
		this.data = data;
	}
}
