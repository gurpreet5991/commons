package io.mosip.kernel.idrepo.controller;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.ServletException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.idrepo.constant.IdRepoErrorConstants;
import io.mosip.kernel.core.idrepo.exception.IdRepoAppException;
import io.mosip.kernel.core.idrepo.exception.IdRepoUnknownException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.idrepo.config.IdRepoLogger;
import io.mosip.kernel.idrepo.dto.ErrorDTO;
import io.mosip.kernel.idrepo.dto.IdResponseDTO;

/**
 * The Class IdRepoExceptionHandler.
 *
 * @author Manoj SP
 */
@RestControllerAdvice
public class IdRepoExceptionHandler extends ResponseEntityExceptionHandler {

	/** The Constant ID_REPO_EXCEPTION_HANDLER. */
	private static final String ID_REPO_EXCEPTION_HANDLER = "IdRepoExceptionHandler";

	/** The Constant ID_REPO. */
	private static final String ID_REPO = "IdRepo";

	/** The Constant SESSION_ID. */
	private static final String SESSION_ID = "sessionId";

	/** The mosip logger. */
	Logger mosipLogger = IdRepoLogger.getLogger(IdRepoExceptionHandler.class);

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;

	/**
	 * Handle all exceptions.
	 *
	 * @param ex
	 *            the ex
	 * @param request
	 *            the request
	 * @return the response entity
	 */
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
		mosipLogger.error(SESSION_ID, ID_REPO, ID_REPO_EXCEPTION_HANDLER,
				"handleAllExceptions - \n" + ExceptionUtils.getStackTrace(ex));
		IdRepoUnknownException e = new IdRepoUnknownException(IdRepoErrorConstants.UNKNOWN_ERROR);
		return new ResponseEntity<>(buildExceptionResponse((BaseCheckedException) e), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.mvc.method.annotation.
	 * ResponseEntityExceptionHandler#handleExceptionInternal(java.lang.Exception,
	 * java.lang.Object, org.springframework.http.HttpHeaders,
	 * org.springframework.http.HttpStatus,
	 * org.springframework.web.context.request.WebRequest)
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object errorMessage,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		mosipLogger.error(SESSION_ID, ID_REPO, ID_REPO_EXCEPTION_HANDLER,
				"handleExceptionInternal - \n" + ExceptionUtils.getStackTrace(ex));
		if (ex instanceof ServletException || ex instanceof BeansException
				|| ex instanceof HttpMessageConversionException) {
			ex = new IdRepoAppException(IdRepoErrorConstants.INVALID_REQUEST.getErrorCode(),
					IdRepoErrorConstants.INVALID_REQUEST.getErrorMessage());

			return new ResponseEntity<>(buildExceptionResponse(ex), HttpStatus.BAD_REQUEST);
		} else if (ex instanceof AsyncRequestTimeoutException) {
			ex = new IdRepoAppException(IdRepoErrorConstants.CONNECTION_TIMED_OUT.getErrorCode(),
					IdRepoErrorConstants.CONNECTION_TIMED_OUT.getErrorMessage());

			return new ResponseEntity<>(buildExceptionResponse(ex), HttpStatus.REQUEST_TIMEOUT);
		} else {
			return handleAllExceptions(ex, request);
		}
	}

	/**
	 * Handle id app exception.
	 *
	 * @param ex
	 *            the ex
	 * @param request
	 *            the request
	 * @return the response entity
	 */
	@ExceptionHandler(IdRepoAppException.class)
	protected ResponseEntity<Object> handleIdAppException(IdRepoAppException ex, WebRequest request) {

		mosipLogger.error(SESSION_ID, ID_REPO, ID_REPO_EXCEPTION_HANDLER,
				"handleIdAppException - \n" + ExceptionUtils.getStackTrace(ex));

		return new ResponseEntity<>(buildExceptionResponse((Exception) ex), HttpStatus.BAD_REQUEST);
	}

	/**
	 * Constructs exception response body for all exceptions.
	 *
	 * @param ex
	 *            the exception occurred
	 * @return Object .
	 */
	private Object buildExceptionResponse(Exception ex) {

		IdResponseDTO response = new IdResponseDTO();

		Throwable e = ex;
		while (e.getCause() != null) {
			if (e.getCause() instanceof IdRepoAppException) {
				if (Objects.nonNull(((IdRepoAppException) e).getId())) {
					response.setId(((IdRepoAppException) e).getId());
				}
				e = e.getCause();
			} else {
				break;
			}
		}

		if (Objects.isNull(response.getId())) {
			response.setId("mosip.id.error");
		}

		if (e instanceof BaseCheckedException) {
			List<String> errorCodes = ((BaseCheckedException) e).getCodes();
			List<String> errorTexts = ((BaseCheckedException) e).getErrorTexts();

			List<ErrorDTO> errors = errorTexts.parallelStream()
					.map(errMsg -> new ErrorDTO(errorCodes.get(errorTexts.indexOf(errMsg)), errMsg)).distinct()
					.collect(Collectors.toList());

			response.setErr(errors);
		}

		response.setTimestamp(DateUtils.getDefaultUTCCurrentDateTimeString());

		mapper.setFilterProvider(new SimpleFilterProvider().addFilter("responseFilter",
				SimpleBeanPropertyFilter.serializeAllExcept("registrationId", "status", "response", "uin")));

		return response;
	}
}
