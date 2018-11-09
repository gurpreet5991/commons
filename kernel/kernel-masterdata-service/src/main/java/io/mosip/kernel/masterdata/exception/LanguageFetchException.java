package io.mosip.kernel.masterdata.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Custom Exception Class in case of error while fetching language
 * 
 * @author Bal Vikash Sharma
 * @version 1.0.0
 */
public class LanguageFetchException extends BaseUncheckedException {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 8621530697947108810L;

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode
	 *            The error code for this exception
	 * @param errorMessage
	 *            The error message for this exception
	 */
	public LanguageFetchException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
