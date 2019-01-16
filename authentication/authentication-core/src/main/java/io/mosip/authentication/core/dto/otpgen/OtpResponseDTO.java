package io.mosip.authentication.core.dto.otpgen;

import java.util.List;

import io.mosip.authentication.core.dto.indauth.AuthError;
import lombok.Data;

/**
 * This class is used to provide response for OTP generation.
 * 
 * @author Dinesh Karuppiah
 *
 */

@Data
public class OtpResponseDTO {

	/**
	 * Variable to hold status
	 */
	private String status;
	/**
	 * List to hold errors
	 */
	private List<AuthError> err;
	/**
	 * List to hold Transaction Id
	 */
	private String txnId;
	/**
	 * List to hold Response Time
	 */
	private String resTime;

	/**
	 * masked mobile(i.e XXXXXXX123) number where send OTP
	 */
	// TODO
	private String maskedMobile;
	/**
	 * masked email id(raXXXXXXXXXan@xyz.com) where send OTP
	 */
	// TODO
	private String maskedEmail;

}
