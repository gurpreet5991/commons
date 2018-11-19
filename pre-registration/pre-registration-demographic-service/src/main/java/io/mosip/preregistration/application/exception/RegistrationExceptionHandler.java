package io.mosip.preregistration.application.exception;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;

import io.mosip.preregistration.application.code.StatusCodes;
import io.mosip.preregistration.application.dto.ExceptionJSONInfo;
import io.mosip.preregistration.application.dto.ResponseDto;
import io.mosip.preregistration.application.errorcodes.ErrorCodes;
import io.mosip.preregistration.core.exceptions.TablenotAccessibleException;

/**
 * Exception Handler
 * 
 * @author M1037717
 *
 */
@RestControllerAdvice
public class RegistrationExceptionHandler {

	@ExceptionHandler(TablenotAccessibleException.class)
	public ResponseEntity<ResponseDto> databaseerror(final TablenotAccessibleException e, WebRequest request) {
		ArrayList<ExceptionJSONInfo> err = new ArrayList<>();
		ExceptionJSONInfo errorDetails = new ExceptionJSONInfo(ErrorCodes.PRG_PAM‌_007.toString(),
				StatusCodes.TABLE_NOT_ACCESSABLE.toString());
		err.add(errorDetails);
		ResponseDto errorRes = new ResponseDto<>();
		errorRes.setErr(err);
		errorRes.setStatus("False");
		errorRes.setResTime(new Timestamp(System.currentTimeMillis()));
		return new ResponseEntity<>(errorRes, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(JsonValidationException.class)
	public ResponseEntity<ResponseDto> jsonValidationException(final JsonValidationException e, WebRequest request) {
		ArrayList<ExceptionJSONInfo> err = new ArrayList<>();
		ExceptionJSONInfo errorDetails = new ExceptionJSONInfo(ErrorCodes.PRG_PAM_010.toString(),
				StatusCodes.JSON_VALIDATION_FAIL.toString());
		err.add(errorDetails);
		ResponseDto errorRes = new ResponseDto<>();
		errorRes.setErr(err);
		errorRes.setStatus("False");
		errorRes.setResTime(new Timestamp(System.currentTimeMillis()));
		return new ResponseEntity<>(errorRes, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
