package org.swmaestro.repl.gifthub.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.swmaestro.repl.gifthub.util.ErrorMessage;
import org.swmaestro.repl.gifthub.util.HttpJsonHeaders;
import org.swmaestro.repl.gifthub.util.Message;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<Message> handleBusinessException(final BusinessException e, HttpServletRequest request) {
		log.error("handleEntityNotFoundException", e);
		Sentry.captureException(e);

		return new ResponseEntity<>(
				ErrorMessage.builder()
						.status(e.getStatus().statusCode)
						.path(request.getRequestURI())
						.error(e.getMessage())
						.build(),
				new HttpJsonHeaders(), e.getStatus().statusCode);
	}
}
