package io.mosip.kernel.masterdata.constant;

/**
 * Constant for valid document.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
public enum ValidDocumentErrorCode {

	VALID_DOCUMENT_INSERT_EXCEPTION("KER-MSD-071","Exception during inserting data into db"),
	VALID_DOCUMENT_NOT_FOUND_EXCEPTION("KER-MSD-016","Valid document not found"),
	VALID_DOCUMENT_DELETE_EXCEPTION("KER-MSD-113","Error occurred while deleting a mapping of Document Category and Document Type details"),
	VALID_DOCUMENT_FETCH_EXCEPTION("KER-MSD-XXX", "Error occurred while fetching Document Categories and Document Types");

	/**
	 * The errorCode.
	 */
	private final String errorCode;

	/**
	 * The errorMessage.
	 */
	private final String errorMessage;

	/**
	 * Constructor for ValidDocumentErrorCode.
	 * 
	 * @param errorCode
	 *            the error code.
	 * @param errorMessage
	 *            the error message.
	 */
	private ValidDocumentErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for error code.
	 * 
	 * @return the error code.
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Getter for error message
	 * 
	 * @return the error message.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
