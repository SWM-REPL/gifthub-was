package org.swmaestro.repl.gifthub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.swmaestro.repl.gifthub.util.HttpJsonHeaders;
import org.swmaestro.repl.gifthub.util.Message;

import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<Message> handleBusinessException(final BusinessException e) {
		log.error("handleEntityNotFoundException", e);
		Sentry.captureException(e);

		return new ResponseEntity<>(
				Message.builder()
						.status(e.getStatus())
						.message(e.getMessage())
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.BAD_REQUEST);
	}
}
