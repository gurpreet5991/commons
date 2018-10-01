package org.mosip.kernel.dataaccess.constant;

/**
 * Error code constants for Hibernate implementation of Dao Manager
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public enum HibernateErrorCodes {
	ERR_DATABASE("COK-DAO-DAM-001"), HIBERNATE_EXCEPTION("COK-DAO-DAM-002"), NO_RESULT_EXCEPTION("COK-DAO-DAM-003");

	/**
	 * Field for error code
	 */
	private final String errorCode;

	/**
	 * Function to set errorcode
	 * 
	 * @param errorCode
	 *            The errorcode
	 */
	private HibernateErrorCodes(final String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Function to get errorcode
	 * 
	 * @return The errorcode
	 */
	public String getErrorCode() {
		return errorCode;
	}

}
