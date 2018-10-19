package io.mosip.registration.processor.status.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import io.mosip.registration.processor.status.dto.ExceptionJSONInfo;
import io.mosip.registration.processor.status.exception.TablenotAccessibleException;

@RestControllerAdvice
public class RegistrationStatusExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(RegistrationStatusExceptionHandler.class);

	@ExceptionHandler(TablenotAccessibleException.class)
	public ResponseEntity<ExceptionJSONInfo> duplicateentry(final TablenotAccessibleException e, WebRequest request) {
		ExceptionJSONInfo errorDetails = new ExceptionJSONInfo(e.getErrorCode(), e.getErrorText());
		log.error(e.getErrorCode(), e.getCause());
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

}
