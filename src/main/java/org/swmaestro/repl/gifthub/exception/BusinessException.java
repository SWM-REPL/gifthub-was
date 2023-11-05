package org.swmaestro.repl.gifthub.exception;

import org.swmaestro.repl.gifthub.util.StatusEnum;

public class BusinessException extends RuntimeException {
	private StatusEnum status;

	public BusinessException(String message, StatusEnum status) {
		super(message);
		this.status = status;
	}

	public BusinessException(StatusEnum status) {
		super(status.code);
		this.status = status;
	}

	public StatusEnum getStatus() {
		return status;
	}
}
