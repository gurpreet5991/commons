package io.mosip.registration.service.operator;

import java.util.Map;

import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.biometric.BiometricDTO;

/**
 * Service class for User Onboarding
 * 
 * @author Sreekar Chukka
 *
 * @since 1.0.0
 */
public interface UserOnboardService {

	/**
	 * Validate.
	 *
	 * @param biometricDTO the biometric DTO
	 * @return the response DTO
	 */
	ResponseDTO validate(BiometricDTO biometricDTO);
	
	/**
	 * Gets the station ID.
	 *
	 * @return the station ID
	 */
	Map<String,String> getMachineCenterId();
		
	
	
	
}
