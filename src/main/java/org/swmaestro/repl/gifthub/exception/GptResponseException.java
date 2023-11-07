package org.swmaestro.repl.gifthub.exception;

import org.swmaestro.repl.gifthub.util.StatusEnum;

import io.sentry.Sentry;

public class GptResponseException extends RuntimeException {
	private StatusEnum status;

	public GptResponseException(String message, StatusEnum status) {
		super(message);
		this.status = status;
		captureExceptionWithSentry(this);
	}

	private void captureExceptionWithSentry(Throwable throwable) {
		Sentry.captureException(throwable);

	}
}