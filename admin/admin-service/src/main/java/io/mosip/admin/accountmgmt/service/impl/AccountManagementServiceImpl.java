package io.mosip.admin.accountmgmt.service.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.admin.accountmgmt.constant.AccountManagementErrorCode;
import io.mosip.admin.accountmgmt.dto.PasswordDto;
import io.mosip.admin.accountmgmt.dto.StatusResponseDto;
import io.mosip.admin.accountmgmt.dto.UserNameDto;
import io.mosip.admin.accountmgmt.exception.AccountManagementServiceException;
import io.mosip.admin.accountmgmt.exception.AccountServiceException;
import io.mosip.admin.accountmgmt.service.AccountManagementService;
import io.mosip.kernel.auth.adapter.exception.AuthNException;
import io.mosip.kernel.auth.adapter.exception.AuthZException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.signatureutil.exception.ParseResponseException;

/**
 * The Class AccountManagementServiceImpl.
 * 
 * @author Srinivasan
 * @since 1.0.0
 */
@Service
public class AccountManagementServiceImpl implements AccountManagementService {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${mosip.admin.accountmgmt.auth-manager-base-uri}")
	private String authManagerBaseUrl;

	@Value("${mosip.admin.accountmgmt.user-name-url}")
	private String userNameUrl;

	@Value("${mosip.admin.accountmgmt.unblock-url}")
	private String unBlockUrl;

	@Value("${mosip.admin.accountmgmt.change-passoword-url}")
	private String changePassword;

	@Value("${mosip.admin.accountmgmt.reset-password-url}")
	private String resetPassword;

	@Autowired
	private ObjectMapper objectMapper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.admin.accountmgmt.service.AccountManagementService#getUserName(java.
	 * lang.String, java.lang.String)
	 */
	@Override
	public UserNameDto getUserName(String userId) {
		String response = null;
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(authManagerBaseUrl).append(userNameUrl + "registrationclient/").append(userId);
		response = callAuthManagerService(urlBuilder.toString(), HttpMethod.GET, null);
		return getUserDetailFromResponse(response);

	}

	@Override
	public StatusResponseDto unBlockUserName(String userId) {
		String response = null;
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(authManagerBaseUrl).append(unBlockUrl + "registrationclient/").append(userId);
		response = callAuthManagerService(urlBuilder.toString(), HttpMethod.GET, null);
		return getSuccessResponse(response);

	}

	private StatusResponseDto getSuccessResponse(String responseBody) {
		List<ServiceError> validationErrorsList = null;
		validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);
		StatusResponseDto unBlockResponseDto = null;
		if (!validationErrorsList.isEmpty()) {
			throw new AccountServiceException(validationErrorsList);
		}
		ResponseWrapper<StatusResponseDto> responseObject = null;
		try {

			responseObject = objectMapper.readValue(responseBody,
					new TypeReference<ResponseWrapper<StatusResponseDto>>() {
					});
			unBlockResponseDto = responseObject.getResponse();
		} catch (IOException | NullPointerException exception) {
			throw new ParseResponseException(AccountManagementErrorCode.PARSE_EXCEPTION.getErrorCode(),
					AccountManagementErrorCode.PARSE_EXCEPTION.getErrorMessage() + exception.getMessage(), exception);
		}

		return unBlockResponseDto;
	}

	@Override
	public StatusResponseDto changePassword(PasswordDto passwordDto) {
		passwordDto.setHashAlgo("SSHA-256");
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(authManagerBaseUrl).append(changePassword + "registrationclient/");
		HttpEntity<RequestWrapper<?>> passwordHttpEntity = getHttpRequest(passwordDto);
		String response = callAuthManagerService(urlBuilder.toString(), HttpMethod.POST, passwordHttpEntity);

		return getSuccessResponse(response);
	}

	@Override
	public StatusResponseDto resetPassword(PasswordDto passwordDto) {
		passwordDto.setHashAlgo("SSHA-256");
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(authManagerBaseUrl).append(resetPassword + "registrationclient");
		HttpEntity<RequestWrapper<?>> passwordHttpEntity = getHttpRequest(passwordDto);
		String response = callAuthManagerService(urlBuilder.toString(), HttpMethod.POST, passwordHttpEntity);
		return getSuccessResponse(response);
	}

	@Override
	public UserNameDto getUserNameBasedOnMobileNumber(String mobile) {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(authManagerBaseUrl).append(userNameUrl + "registrationclient").append(mobile);
		String response = callAuthManagerService(urlBuilder.toString(), HttpMethod.GET, null);
		return getUserDetailFromResponse(response);
	}

	private String callAuthManagerService(String url, HttpMethod httpMethod,
			HttpEntity<RequestWrapper<?>> requestEntity) {
		String response = null;
		try {
			ResponseEntity<String> responeEntity = restTemplate.exchange(url, httpMethod, requestEntity, String.class);
			response = responeEntity.getBody();
		} catch (HttpServerErrorException | HttpClientErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

			if (ex.getRawStatusCode() == 401) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthNException(validationErrorsList);
				} else {
					throw new BadCredentialsException("Authentication failed from AuthManager");
				}
			}
			if (ex.getRawStatusCode() == 403) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthZException(validationErrorsList);
				} else {
					throw new AccessDeniedException("Access denied from AuthManager");
				}
			}
			throw new AccountManagementServiceException(
					AccountManagementErrorCode.REST_SERVICE_EXCEPTION.getErrorCode(),
					AccountManagementErrorCode.REST_SERVICE_EXCEPTION.getErrorMessage()+""+ex.getResponseBodyAsString());
		}

		return response;
	}

	private UserNameDto getUserDetailFromResponse(String responseBody) {
		List<ServiceError> validationErrorsList = null;
		validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);
		UserNameDto userNameDto = null;
		if (!validationErrorsList.isEmpty()) {
			throw new AccountServiceException(validationErrorsList);
		}
		ResponseWrapper<UserNameDto> responseObject = null;
		try {

			responseObject = objectMapper.readValue(responseBody, new TypeReference<ResponseWrapper<UserNameDto>>() {
			});
			userNameDto = responseObject.getResponse();
		} catch (IOException | NullPointerException exception) {
			throw new ParseResponseException(AccountManagementErrorCode.PARSE_EXCEPTION.getErrorCode(),
					AccountManagementErrorCode.PARSE_EXCEPTION.getErrorMessage() + exception.getMessage(), exception);
		}

		return userNameDto;
	}

	private HttpEntity<RequestWrapper<?>> getHttpRequest(PasswordDto passwordDto) {
		RequestWrapper<PasswordDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId("ADMIN_REQUEST");
		requestWrapper.setVersion("V1.0");
		requestWrapper.setRequest(passwordDto);
		HttpHeaders syncDataRequestHeaders = new HttpHeaders();
		syncDataRequestHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(requestWrapper, syncDataRequestHeaders);

	}

}
