package io.mosip.registration.processor.core.auth.dto;

import lombok.Data;

/**
 * Generic class to find authentication type. For e.g if go for otp validation,
 * then <b>otp</b> attribute set as <b>true</b>
 * 
 * @author Rakesh Roshan
 * @author Dinesh Karuppiah.T
 */
@Data
public class AuthTypeDTO {

	/** For personal identity Authentication */
	private boolean personalIdentity;

	/** For address Authentication */
	private boolean address;

	/** For Full address Authentication */
	private boolean fullAddress;

	/** For biometric Authentication */
	private boolean bio;

	/** For otp Authentication */
	private boolean otp;

	/** For pin Authentication */
	private boolean pin;

}
