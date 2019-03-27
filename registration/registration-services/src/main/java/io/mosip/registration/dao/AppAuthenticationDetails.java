package io.mosip.registration.dao;

/**
 * Interface for RegistrationAppAuthentication
 * 
 * @author Sravya Surampalli
 * @since 1.0.0
 *
 */
public interface AppAuthenticationDetails {
	
	/**
	 * To fetch only selected columns from Embeddable
	 *
	 * @return {@link AppAuthentication}
	 */
	AppAuthentication getAppAuthenticationMethodId();
	
	/**
	 * To fetch only selected columns from table
	 */
	interface AppAuthentication{
		String getProcessName();
		String getAuthMethodCode();
	}

}
