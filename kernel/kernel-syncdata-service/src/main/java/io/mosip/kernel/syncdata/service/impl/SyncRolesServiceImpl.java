package io.mosip.kernel.syncdata.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.syncdata.constant.RolesErrorCode;
import io.mosip.kernel.syncdata.dto.response.RolesResponseDto;
import io.mosip.kernel.syncdata.exception.SyncDataServiceException;
import io.mosip.kernel.syncdata.service.SyncRolesService;

/**
 * 
 * @author Srinivasan
 * This class handles fetching of everey roles that is in the server.
 * The flow is given as follows
 * SYNC -> AUTH SERVICE -> AUTH SERVER
 * 
 */
@Service
public class SyncRolesServiceImpl implements SyncRolesService {

	/**
	 * restemplate instance
	 */
	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Base end point read from property file
	 */
	@Value("${mosip.kernel.syncdata.auth-manager-base-uri}")
	private String authBaseUrl;
	
	/**
	 * all roles end-point read from properties file
	 */
	@Value("${mosip.kernel.syncdata.auth-manager-roles}")
	private String authServiceName;
	
	/*
	 * (non-Javadoc)
	 * @see io.mosip.kernel.syncdata.service.SyncRolesService#getAllRoles()
	 */
	@Override
	public RolesResponseDto getAllRoles() {
		RolesResponseDto rolesDtos = null;
		try {
			
			StringBuilder uriBuilder=new StringBuilder();
			uriBuilder.append(authBaseUrl).append(authServiceName);
			rolesDtos = restTemplate.getForObject(uriBuilder.toString(),
					RolesResponseDto.class);
		} catch (RestClientException ex) {
			throw new SyncDataServiceException(
					RolesErrorCode.ROLES_FETCH_EXCEPTION.getErrorCode(),
					RolesErrorCode.ROLES_FETCH_EXCEPTION.getErrorMessage());
		}
       
		return rolesDtos;

	}

}
