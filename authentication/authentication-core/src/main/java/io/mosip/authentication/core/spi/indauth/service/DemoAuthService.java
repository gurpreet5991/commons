package io.mosip.authentication.core.spi.indauth.service;

import java.util.List;
import java.util.Map;

import io.mosip.authentication.core.dto.indauth.AuthRequestDTO;
import io.mosip.authentication.core.dto.indauth.AuthStatusInfo;
import io.mosip.authentication.core.dto.indauth.IdentityInfoDTO;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;

/**
 * 
 * This interface is used to authenticate Individual based on Demo attributes.
 * 
 * @author Gurpreet Bagga
 */
public interface DemoAuthService extends AuthService {
	
	/**
	 * Gets the demo status.
	 *
	 * @param authRequestDTO the auth request DTO
	 * @param uin the uin
	 * @param idInfo the id info
	 * @return the demo status
	 * @throws IdAuthenticationBusinessException the id authentication business exception
	 *//*
	AuthStatusInfo getDemoStatus(AuthRequestDTO authRequestDTO,String uin, Map<String, List<IdentityInfoDTO>> idInfo) throws IdAuthenticationBusinessException;*/
}