/**
 * 
 */
package io.mosip.kernel.idvalidator.uin.constants;

/**
 *
 * @author Megha Tanga
 * 
 * @since 1.0.0
 */

public enum MosipUinExceptionConstants {
	
	UIN_VAL_INVALID_NULL("KER-IDV-201" , "Entered UIN should not be Empty or Null."),
	UIN_VAL_ILLEGAL_SEQUENCE_REPEATATIVE("KER-IDV-202", "Entered UIN should not contain any sequential and repeated block of number for 2 or more than two digits"), 
	UIN_VAL_ILLEGAL_LENGTH("KER-IDV-203", "Entered UIN length should be 12 digit."), 
	UIN_VAL_INVALID_DIGITS("KER-IDV-204", "Entered UIN should not contain any alphanumeric characters."), 
	UIN_VAL_ILLEGAL_CHECKSUM("KER-IDV-205", "Entered UIN should match checksum."),
	UIN_VAL_INVALID_ZERO_ONE("KER-IDV-206", "Entered UIN should not contain Zero or One as first digit.");
	/**
	 * This variable holds the error code.
	 */
	private String errorCode;

	/**
	 * This variable holds the error message.
	 */
	private String errorMessage;

	/**
	 * Constructor for UINErrorConstants Enum.
	 * 
	 * @param errorCode
	 *            the error code.
	 * @param errorMessage
	 *            the error message.
	 */
	MosipUinExceptionConstants(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for errorCode.
	 * 
	 * @return the error code.
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Getter for errorMessage.
	 * 
	 * @return the error message.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}

