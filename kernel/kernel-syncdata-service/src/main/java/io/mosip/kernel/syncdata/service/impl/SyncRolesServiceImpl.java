package io.mosip.kernel.syncdata.service.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.syncdata.constant.RolesErrorCode;
import io.mosip.kernel.syncdata.constant.UserDetailsErrorCode;
import io.mosip.kernel.syncdata.dto.response.RolesResponseDto;
import io.mosip.kernel.syncdata.exception.AuthManagerServiceException;
import io.mosip.kernel.syncdata.exception.ParseResponseException;
import io.mosip.kernel.syncdata.exception.SyncDataServiceException;
import io.mosip.kernel.syncdata.service.SyncRolesService;

/**
 * This class handles fetching of everey roles that is in the server. The flow
 * is given as follows SYNC - AUTH SERVICE - AUTH SERVER
 * 
 * @author Srinivasan
 * @since 1.0.0
 * 
 */
@RefreshScope
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
	
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * all roles end-point read from properties file
	 */
	@Value("${mosip.kernel.syncdata.auth-manager-roles}")
	private String authServiceName;
	
	@Value("${mosip.kernel.syncdata.syncdata-request-id:SYNCDATA.REQUEST}")
	private String syncDataRequestId;

	@Value("${mosip.kernel.syncdata.syncdata-version-id:v1.0}")
	private String syncDataVersionId;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.syncdata.service.SyncRolesService#getAllRoles()
	 */
	@Override
	public RolesResponseDto getAllRoles() {
		RolesResponseDto rolesDtos = null;
		ResponseEntity<String> response=null;
		try {

			StringBuilder uriBuilder = new StringBuilder();
			uriBuilder.append(authBaseUrl).append(authServiceName);
			RequestWrapper<?> requestWrapper = new RequestWrapper<>();
			requestWrapper.setId(syncDataRequestId);
			requestWrapper.setVersion(syncDataVersionId);
			HttpHeaders syncDataRequestHeaders = new HttpHeaders();
			syncDataRequestHeaders.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<RequestWrapper<?>> userRolesRequestEntity = new HttpEntity<>(requestWrapper,
					syncDataRequestHeaders);
			response = restTemplate.exchange(uriBuilder.toString()+"/registrationclient",HttpMethod.GET , userRolesRequestEntity, String.class);//(uriBuilder.toString() + "/registrationclient",
					//String.class);
		} catch (RestClientException ex) {
			throw new SyncDataServiceException(RolesErrorCode.ROLES_FETCH_EXCEPTION.getErrorCode(),
					RolesErrorCode.ROLES_FETCH_EXCEPTION.getErrorMessage());
		}
		String responseBody = response.getBody();
		List<ServiceError> validationErrorsList = null;
		validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);
		if (!validationErrorsList.isEmpty()) {
			throw new AuthManagerServiceException(validationErrorsList);
		}
		ResponseWrapper<?> responseObject = null;
		try {
			responseObject = objectMapper.readValue(response.getBody(), ResponseWrapper.class);
			rolesDtos = objectMapper.readValue(
					objectMapper.writeValueAsString(responseObject.getResponse()), RolesResponseDto.class);
		} catch (IOException | NullPointerException exception) {
			throw new ParseResponseException(UserDetailsErrorCode.USER_DETAILS_PARSE_ERROR.getErrorCode(),
					UserDetailsErrorCode.USER_DETAILS_PARSE_ERROR.getErrorMessage() + exception.getMessage(),
					exception);
		}

		return rolesDtos;

	}

}
