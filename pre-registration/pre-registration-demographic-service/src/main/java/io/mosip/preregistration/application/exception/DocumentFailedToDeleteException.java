/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.application.errorcodes.ErrorCodes;

/**
 * This class defines the DocumentFailedToDeleteException
 * 
 * @author Tapaswini Bahera
 * @since 1.0.0
 * 
 */
public class DocumentFailedToDeleteException extends BaseUncheckedException {

	private static final long serialVersionUID = 1L;

	public DocumentFailedToDeleteException() {
		super();
	}

	public DocumentFailedToDeleteException(String errorMessage) {
		super(ErrorCodes.PRG_PAM_DOC_015.toString(), errorMessage);
	}

	public DocumentFailedToDeleteException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage, null);
	}

	public DocumentFailedToDeleteException(String errorMessage, Throwable rootCause) {
		super(ErrorCodes.PRG_PAM_DOC_015.toString(), errorMessage, rootCause);
	}

	public DocumentFailedToDeleteException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}
