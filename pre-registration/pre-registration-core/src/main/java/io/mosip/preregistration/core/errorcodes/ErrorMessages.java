package io.mosip.preregistration.core.errorcodes;

/**
 * This Enum provides the constant variables to define Error Messages.
 * 
 * @author Kishan Rathore
 * @author Ravi C. Balaji
 * @since 1.0.0
 *
 */
public enum ErrorMessages {
	INVALID_REQUEST_ID("Request id is invalid"), // PRG_CORE_REQ_001
	INVALID_REQUEST_VERSION("Request version is invalid"), // PRG_CORE_REQ_002
	INVALID_REQUEST_DATETIME("Request timestamp is invalid"), // PRG_CORE_REQ_003
	INVALID_REQUEST_BODY("Request body is invalid"), // PRG_CORE_REQ_004
	INVALID_STATUS_CODE("status code is invalid"), 
	INVALID_DATE("date is invalid"), 
	APPOINTMENT_CANNOT_BE_BOOKED("appointment cannot be booked"), 
	APPONIMENT_CANNOT_BE_CANCELED("appointment cannot be cancelled"), 
	APPONIMENT_CANNOT_BE_REBOOK("appointment cannot be rebooked"), 
	HASHING_FAILED("hashing failed"), // PRG_CORE_REQ_010
	FAILED_TO_ENCRYPT("encryption failed"), // PRG_CORE_REQ_011
	FAILED_TO_DECRYPT("decryption failes"), // PRG_CORE_REQ_012
	MISSING_REQUEST_PARAMETER("request parameter is missing"),
	INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE("Request date should be current date");//PRG_CORE_REQ_013

	private ErrorMessages(String message) {
		this.message = message;
	}

	private final String message;

	/**
	 * @return message
	 */
	public String getMessage() {
		return message;
	}
}
