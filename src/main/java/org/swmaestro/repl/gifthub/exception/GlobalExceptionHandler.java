package org.swmaestro.repl.gifthub.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<Message> handleBusinessException(final BusinessException e) {
		log.error("handleEntityNotFoundException", e);
		Sentry.captureException(e);
		Message.builder()

				.status(StatusEnum
				.build();
		final ErrorCode errorCode = e.getErrorCode();
		final ErrorResponse response = ErrorResponse.of(errorCode);
		return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatus()));
	}
}
