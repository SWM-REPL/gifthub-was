package org.swmaestro.repl.gifthub.util.exception;

import org.swmaestro.repl.gifthub.util.StatusEnum;

public class BusinessException extends RuntimeException {
	private int statusCode;

	public BusinessException(String message, StatusEnum status) {
		super(message);
		this.statusCode = status.statusCode;
	}

	public BusinessException(StatusEnum status) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
