package org.swmaestro.repl.gifthub.exception;

public class BusinessException extends RuntimeException {
	private ErrorCode errorCode;

	public BusnessException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public BusnessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
