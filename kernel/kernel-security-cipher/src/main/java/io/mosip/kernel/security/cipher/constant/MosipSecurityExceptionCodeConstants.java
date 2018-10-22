/*
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */

package io.mosip.kernel.security.cipher.constant;

/**
 * {@link Enum} for exception constants
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public enum MosipSecurityExceptionCodeConstants {
	MOSIP_INVALID_KEY_EXCEPTION("COK-FSE-001", "invalid Key (null key,invalid encoding, wronglength, uninitialized, etc)."),
	MOSIP_INVALID_DATA_LENGTH_EXCEPTION("COK-FSE-02", "check input data length"),
	MOSIP_INVALID_DATA_EXCEPTION("COK-FSE-003", "data not valid (currupted,length is not valid etc.)"),
	MOSIP_INVALID_ENCRYPTED_DATA_CORRUPT_EXCEPTION("COK-FSE-004", "encrypted data is corrupted"),
	MOSIP_INVALID_DATA_SIZE_EXCEPTION("COK-FSE-005", "ecrypted data size is not valid"),
	MOSIP_NULL_DATA_EXCEPTION("COK-FSE-06", "data is null or length is 0"),
	MOSIP_NULL_METHOD_EXCEPTION("COK-FSE-07", "mosip security method is null"),
	MOSIP_NO_SUCH_ALGORITHM_EXCEPTION("COK-FSE-08", "no such algorithm");

	/**
	 * Constant {@link Enum} errorCode
	 */
	private final String errorCode;

	/**
	 * Getter for errorMessage
	 * 
	 * @return get errorMessage value
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Constant {@link Enum} errorMessage
	 */
	private final String errorMessage;

	/**
	 * Constructor for this class
	 * 
	 * @param value set {@link Enum} value
	 */
	private MosipSecurityExceptionCodeConstants(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for errorCode
	 * 
	 * @return get errorCode value
	 */
	public String getErrorCode() {
		return errorCode;
	}
}
