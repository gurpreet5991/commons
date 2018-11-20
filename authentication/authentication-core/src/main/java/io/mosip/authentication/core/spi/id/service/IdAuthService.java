package io.mosip.authentication.core.spi.id.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.exception.IdValidationFailedException;

/**
 * 
 * @author Arun Bose
 *  The code {@IdAuthService} validates UIN
 * 
 */
@Service
public interface IdAuthService {

	/**
	 * validates the UIN
	 * @param UIN
	 * @return
	 * @throws IdValidationFailedException
	 */
	 String validateUIN(String uin)  throws IdAuthenticationBusinessException;
	 
	/**
	 * validates the VID
	 * @param VID
	 * @return
	 * @throws IdValidationFailedException
	 */
	String validateVID(String vid)  throws IdAuthenticationBusinessException;
	
	public Optional<String> getUIN(String uinRefId) throws IdAuthenticationBusinessException;
}
