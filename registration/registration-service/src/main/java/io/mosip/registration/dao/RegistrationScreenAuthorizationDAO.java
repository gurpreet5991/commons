package io.mosip.registration.dao;

import io.mosip.registration.dto.AuthorizationDTO;

/**
 * DAO class for RegistrationScreenAuthorization
 * 
 * @author Sravya Surampalli
 * @since 1.0.0
 *
 */
public interface RegistrationScreenAuthorizationDAO {

	/**
	 * This method is used to get the screen authorization
	 * 
	 * @return AuthorizationDTO of authorization details
	 */
	AuthorizationDTO getScreenAuthorizationDetails(String roleCode);
}
