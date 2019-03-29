/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception.util;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.exception.ParseException;
import io.mosip.kernel.core.jsonvalidator.exception.FileIOException;
import io.mosip.kernel.core.jsonvalidator.exception.HttpRequestException;
import io.mosip.kernel.core.jsonvalidator.exception.JsonIOException;
import io.mosip.kernel.core.jsonvalidator.exception.JsonSchemaIOException;
import io.mosip.kernel.core.jsonvalidator.exception.JsonValidationProcessingException;
import io.mosip.kernel.core.jsonvalidator.exception.UnidentifiedJsonException;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.preregistration.application.errorcodes.ErrorCodes;
import io.mosip.preregistration.application.errorcodes.ErrorMessages;
import io.mosip.preregistration.application.exception.BookingDeletionFailedException;
import io.mosip.preregistration.application.exception.DocumentFailedToDeleteException;
import io.mosip.preregistration.application.exception.InvalidDateFormatException;
import io.mosip.preregistration.application.exception.MissingRequestParameterException;
import io.mosip.preregistration.application.exception.OperationNotAllowedException;
import io.mosip.preregistration.application.exception.RecordFailedToDeleteException;
import io.mosip.preregistration.application.exception.RecordFailedToUpdateException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.exception.RecordNotFoundForPreIdsException;
import io.mosip.preregistration.application.exception.system.DateParseException;
import io.mosip.preregistration.application.exception.system.JsonParseException;
import io.mosip.preregistration.application.exception.system.JsonValidationException;
import io.mosip.preregistration.application.exception.system.SystemFileIOException;
import io.mosip.preregistration.application.exception.system.SystemIllegalArgumentException;
import io.mosip.preregistration.application.exception.system.SystemUnsupportedEncodingException;
import io.mosip.preregistration.core.exception.DecryptionFailedException;
import io.mosip.preregistration.core.exception.EncryptionFailedException;
import io.mosip.preregistration.core.exception.HashingException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;

/**
 * This class is used to catch the exceptions that occur while creating the
 * pre-registration
 * 
 * @author Ravi C Balaji
 * 
 * @since 1.0.0
 *
 */
public class DemographicExceptionCatcher {
	/**
	 * Method to handle the respective exceptions
	 * 
	 * @param ex
	 *            pass the exception
	 */
	public void handle(Exception ex) {
		if (ex instanceof HttpRequestException) {
			throw new JsonValidationException(((HttpRequestException) ex).getErrorCode(),
					((HttpRequestException) ex).getErrorText());
		} else if (ex instanceof DataAccessLayerException) {
			throw new TableNotAccessibleException(((DataAccessLayerException) ex).getErrorCode(),
					((DataAccessLayerException) ex).getErrorText());
		} else if (ex instanceof JsonValidationProcessingException) {
			throw new JsonValidationException(((JsonValidationProcessingException) ex).getErrorCode(),
					((JsonValidationProcessingException) ex).getErrorText());
		} else if (ex instanceof JsonIOException) {
			throw new JsonValidationException(((JsonIOException) ex).getErrorCode(),
					((JsonIOException) ex).getErrorText());
		} else if (ex instanceof JsonSchemaIOException) {
			throw new JsonValidationException(((JsonSchemaIOException) ex).getErrorCode(),
					((JsonSchemaIOException) ex).getErrorText());
		} else if (ex instanceof FileIOException) {
			throw new SystemFileIOException(((FileIOException) ex).getErrorCode(),
					((FileIOException) ex).getErrorText());
		} else if (ex instanceof ParseException) {
			throw new JsonParseException(((ParseException) ex).getErrorCode(), ((ParseException) ex).getErrorText());
		} else if (ex instanceof RecordNotFoundException) {
			throw new RecordNotFoundException(((RecordNotFoundException) ex).getErrorCode(),
					((RecordNotFoundException) ex).getErrorText());
		} else if (ex instanceof RecordNotFoundForPreIdsException) {
			throw new RecordNotFoundForPreIdsException(((RecordNotFoundForPreIdsException) ex).getErrorCode(),
					((RecordNotFoundForPreIdsException) ex).getErrorText());
		} else if (ex instanceof InvalidRequestParameterException) {
			throw new InvalidRequestParameterException(((InvalidRequestParameterException) ex).getErrorCode(),
					((InvalidRequestParameterException) ex).getErrorText());
		} else if (ex instanceof MissingRequestParameterException) {
			throw new MissingRequestParameterException(((MissingRequestParameterException) ex).getErrorCode(),
					((MissingRequestParameterException) ex).getErrorText());
		} else if (ex instanceof DocumentFailedToDeleteException) {
			throw new DocumentFailedToDeleteException(((DocumentFailedToDeleteException) ex).getErrorCode(),
					((DocumentFailedToDeleteException) ex).getErrorText());
		} else if (ex instanceof IllegalArgumentException) {
			throw new SystemIllegalArgumentException(ErrorCodes.PRG_PAM_APP_007.name(),
					ErrorMessages.UNSUPPORTED_DATE_FORMAT.name());
		} else if (ex instanceof SystemUnsupportedEncodingException) {
			throw new SystemUnsupportedEncodingException(((SystemUnsupportedEncodingException) ex).getErrorCode(),
					((SystemUnsupportedEncodingException) ex).getErrorText());
		} else if (ex instanceof DateParseException) {
			throw new DateParseException(((DateParseException) ex).getErrorCode(),
					((DateParseException) ex).getErrorText());
		} else if (ex instanceof UnidentifiedJsonException) {
			throw new JsonValidationException(((UnidentifiedJsonException) ex).getErrorCode(),
					((UnidentifiedJsonException) ex).getErrorText());
		} else if (ex instanceof RecordFailedToUpdateException) {
			throw new RecordFailedToUpdateException(((RecordFailedToUpdateException) ex).getErrorCode(),
					((RecordFailedToUpdateException) ex).getErrorText());
		} else if (ex instanceof RecordFailedToDeleteException) {
			throw new RecordFailedToDeleteException(((RecordFailedToDeleteException) ex).getErrorCode(),
					((RecordFailedToDeleteException) ex).getErrorText());
		} else if (ex instanceof InvalidDateFormatException) {
			throw new InvalidDateFormatException(((InvalidDateFormatException) ex).getErrorCode(),
					((InvalidDateFormatException) ex).getErrorText());
		} else if (ex instanceof BookingDeletionFailedException) {
			throw new BookingDeletionFailedException(((BookingDeletionFailedException) ex).getErrorCode(),
					((BookingDeletionFailedException) ex).getErrorText());
		} else if (ex instanceof HashingException) {
			throw new HashingException(((HashingException) ex).getErrorCode(), ((HashingException) ex).getErrorText());
		} else if (ex instanceof OperationNotAllowedException) {
			throw new OperationNotAllowedException(((OperationNotAllowedException) ex).getErrorCode(),
					((OperationNotAllowedException) ex).getErrorText());
		} else if (ex instanceof EncryptionFailedException) {
			throw new EncryptionFailedException(((EncryptionFailedException) ex).getErrorCode(),
					((EncryptionFailedException) ex).getErrorText());
		} else if (ex instanceof DecryptionFailedException) {
			throw new DecryptionFailedException(((DecryptionFailedException) ex).getErrorCode(),
					((DecryptionFailedException) ex).getErrorText());
		} else if (ex instanceof JsonMappingException) {
			throw new JsonValidationException(((JsonMappingException) ex).getErrorCode(),
					((JsonMappingException) ex).getErrorText());
		} else if (ex instanceof IOException) {
			throw new JsonValidationException(((IOException) ex).getErrorCode(), ((IOException) ex).getErrorText());
		}
	}

}
