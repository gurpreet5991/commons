package org.mosip.auth.core.dto.otpgen;

import java.util.Date;
import java.util.List;

import org.mosip.auth.core.dto.indauth.AuthError;

import lombok.Data;

/**
 * This class is used to provide response for OTP generation.
 * 
 * @author Dinesh Karuppiah
 *
 */

@Data
public class OtpResponseDTO {

	private static final long serialVersionUID = 8039452517810739329L;

	private String status;
	private List<AuthError> errorCode;
	private String txnID;
	private Date responseTime;

}
