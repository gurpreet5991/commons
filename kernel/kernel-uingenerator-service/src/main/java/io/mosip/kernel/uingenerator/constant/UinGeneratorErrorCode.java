package io.mosip.kernel.uingenerator.constant;

/**
 * Error Code for Uin generator
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public enum UinGeneratorErrorCode {
	UIN_NOT_FOUND("KER-UIG-001", "UIN could not be found"), UIN_STATUS_NOT_FOUND("KER-UIG-003",
			"Given UIN status not found"), UIN_NOT_ISSUED("KER-UIG-004", "Given UIN is not in ISSUED status");

	/**
	 * The error code
	 */
	private final String errorCode;
	/**
	 * The error message
	 */
	private final String errorMessage;

	/**
	 * Constructor to set error code and message
	 * 
	 * @param errorCode
	 *            the error code
	 * @param errorMessage
	 *            the error message
	 */
	private UinGeneratorErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Function to get error code
	 * 
	 * @return {@link #errorCode}
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Function to get the error message
	 * 
	 * @return {@link #errorMessage}r
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
