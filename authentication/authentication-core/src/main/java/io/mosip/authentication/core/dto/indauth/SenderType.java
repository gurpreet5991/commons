package io.mosip.authentication.core.dto.indauth;

/**
 * 
 * @author Dinesh Karuppiah.T
 */
public enum SenderType {

	AUTH("auth"), OTP("otp");

	/**
	 * Variable to hold name
	 */
	private String name;

	SenderType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
