package org.swmaestro.repl.gifthub.exception;

import org.swmaestro.repl.gifthub.util.StatusEnum;

public class GptResponseException extends RuntimeException {
	private StatusEnum status;

	public GptResponseException(String message, StatusEnum status) {
		super(message);
		this.status = status;
	}
}