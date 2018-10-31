/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.crypto.bouncycastle.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.crypto.bouncycastle.constant.MosipSecurityExceptionCodeConstant;

/**
 * {@link Exception} to be thrown when data is null
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class MosipNullDataException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 5282175344975485527L;

	/**
	 * Constructor for this class
	 * 
	 * @param exceptionCodeConstants
	 *            exception code constant
	 */
	public MosipNullDataException(MosipSecurityExceptionCodeConstant exceptionCodeConstants) {
		super(exceptionCodeConstants.getErrorCode(), exceptionCodeConstants.getErrorMessage());
	}

}
