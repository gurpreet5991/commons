package io.mosip.admin.usermgmt.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class ServiceException extends BaseUncheckedException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3383837827871687253L;

	public ServiceException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

	public ServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}
}
