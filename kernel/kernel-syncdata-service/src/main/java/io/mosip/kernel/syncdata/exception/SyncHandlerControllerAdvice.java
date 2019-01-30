package io.mosip.kernel.syncdata.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * synch handler controller advice
 * 
 * @author Abhishek Kumar
 * @since 1.0.0
 *
 */
@RestControllerAdvice
public class SyncHandlerControllerAdvice {
	@ExceptionHandler(SyncDataServiceException.class)
	public ResponseEntity<ErrorResponse<Error>> controlDataServiceException(final SyncDataServiceException e) {
		return new ResponseEntity<>(getErrorResponse(e), HttpStatus.OK);
	}

	@ExceptionHandler(DateParsingException.class)
	public ResponseEntity<ErrorResponse<Error>> controlDataServiceException(final DateParsingException e) {
		return new ResponseEntity<>(getErrorResponse(e), HttpStatus.OK);
	}

	private ErrorResponse<Error> getErrorResponse(BaseUncheckedException e) {
		Error error = new Error(e.getErrorCode(), e.getErrorText());
		ErrorResponse<Error> errorResponse = new ErrorResponse<>();
		errorResponse.getErrors().add(error);
		return errorResponse;
	}
}
