package io.mosip.kernel.syncdata.exception;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ErrorResponse;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.syncdata.constant.MasterDataErrorCode;

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
		e.printStackTrace();
		return new ResponseEntity<>(getErrorResponse(e,HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(DateParsingException.class)
	public ResponseEntity<ErrorResponse<Error>> controlDataServiceException(final DateParsingException e) {
		return new ResponseEntity<>(getErrorResponse(e,HttpStatus.OK), HttpStatus.OK);
	}
	
	@ExceptionHandler(DataNotFoundException.class)
	public ResponseEntity<ErrorResponse<Error>> controlDataNotFoundException(final DataNotFoundException e) {
		return new ResponseEntity<>(getErrorResponse(e,HttpStatus.OK),HttpStatus.OK); 
	}

	private ErrorResponse<Error> getErrorResponse(BaseUncheckedException e,HttpStatus httpStatus) {
		Error error = new Error(e.getErrorCode(), e.getErrorText());
		ErrorResponse<Error> errorResponse = new ErrorResponse<>();
		errorResponse.setStatus(httpStatus.value());
		errorResponse.getErrors().add(error);
		return errorResponse;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse<ServiceError>> onHttpMessageNotReadable(
			final HttpMessageNotReadableException e) {
		ErrorResponse<ServiceError> errorResponse = new ErrorResponse<>();
		ServiceError error = new ServiceError(MasterDataErrorCode.REQUEST_DATA_NOT_VALID.getErrorCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		errorResponse.setStatus(HttpStatus.OK.value());
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}
	
	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public ResponseEntity<ErrorResponse<ServiceError>> defaultErrorHandler(HttpServletRequest request, Exception e) {
		e.printStackTrace();
		ErrorResponse<ServiceError> errorResponse = new ErrorResponse<>();
		ServiceError error = new ServiceError(MasterDataErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
