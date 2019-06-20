package io.mosip.registration.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.registration.dto.AuthenticationValidatorDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.util.common.OTPManager;

/**
 * 
 * This class is for validating OTP Authentication
 * 
 * @author SaravanaKumar G
 *
 */
@Service("oTPValidatorImpl")
public class OTPValidatorImpl extends AuthenticationBaseValidator {

	@Autowired
	private OTPManager otpManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.validator.AuthenticationValidatorImplementation#
	 * validate(io.mosip.registration.dto.AuthenticationValidatorDTO)
	 */
	@Override
	public boolean validate(AuthenticationValidatorDTO authenticationValidatorDTO) {
		 ResponseDTO responseDTO = otpManager.validateOTP(authenticationValidatorDTO.getUserId(), authenticationValidatorDTO.getOtp());

		return responseDTO.getSuccessResponseDTO()!=null;
	}

}
