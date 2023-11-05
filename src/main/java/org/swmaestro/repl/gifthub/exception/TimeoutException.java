package org.swmaestro.repl.gifthub.exception;

import org.swmaestro.repl.gifthub.util.StatusEnum;

public class TimeoutException extends RuntimeException {
	private StatusEnum status;

	public TimeoutException(String message, StatusEnum status) {
		super(message);
		this.status = status;
	}
}
