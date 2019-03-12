package io.mosip.preregistration.notification.error;

/**
 * @author Sanober Noor
 *@since 1.0.0
 */
public enum ErrorMessages {

	MOBILE_NUMBER_OR_EMAIL_ADDRESS_NOT_FILLED("MOBILE_NUMBER_OR_EMAIL_ADDRESS_NOT_FILLED"),
	/**
	 * ErrorMessage for PRG_TRL_APP_002
	 */
	INCORRECT_MANDATORY_FIELDS("INCORRECT_MANDATORY_FIELDS"),
	/**
	 * ErrorMessage for PRG_ACK_003
	 */
	JSON_HTTP_REQUEST_EXCEPTION("JSON_HTTP_REQUEST_EXCEPTION"),
	
	/**
	 * ErrorMessage for PRG_ACK_004
	 */
	JSON_PARSING_FAILED("JSON_PARSING_FAILED"),
	/**
	 * @param code
	 * ErrorMessage for PRG_ACK_005
	 */
	
	INPUT_OUTPUT_EXCEPTION("INPUT_OUTPUT_EXCEPTION"),
	
	/**
	 * ErrorMessage for PRG_ACK_006
	 */
	QRCODE_FAILED_TO_GENERATE("QRCODE_FAILED_TO_GENERATE"),
	
	/**
	 * ErrorMessage for PRG_ACK_007
	 */
	CONFIG_FILE_NOT_FOUND_EXCEPTION("CONFIG_FILE_NOT_FOUND_EXCEPTION");
	
	private ErrorMessages(String code) {
		this.code = code;
	}

	private final String code;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
}
