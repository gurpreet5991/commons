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
import io.mosip.admin.accountmgmt.dto.ResetPasswordDto;
import io.mosip.admin.accountmgmt.dto.StatusResponseDto;
import io.mosip.admin.accountmgmt.dto.UserDetailDto;
import io.mosip.admin.accountmgmt.dto.UserNameDto;
import io.mosip.admin.accountmgmt.dto.ValidationResponseDto;
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

	/** The rest template. */
	@Autowired
	private RestTemplate restTemplate;

	/** The auth manager base url. */
	@Value("${mosip.admin.accountmgmt.auth-manager-base-uri}")
	private String authManagerBaseUrl;

	/** The user name url. */
	@Value("${mosip.admin.accountmgmt.user-name-url}")
	private String userNameUrl;

	/** The un block url. */
	@Value("${mosip.admin.accountmgmt.unblock-url}")
	private String unBlockUrl;

	/** The change password. */
	@Value("${mosip.admin.accountmgmt.change-passoword-url}")
	private String changePassword;

	/** The reset password. */
	@Value("${mosip.admin.accountmgmt.reset-password-url}")
	private String resetPassword;

	@Value("${mosip.admin.app-id}")
	private String appId;
	
	/** The user detail url. */
	@Value("${mosip.admin.accountmgmt.user-detail-url}")
	private String userDetailUrl;
	
	/** The user detail url. */
	@Value("${mosip.admin.accountmgmt.validate-url}")
	private String validateUrl;

	/** The object mapper. */
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
		urlBuilder.append(authManagerBaseUrl).append(userNameUrl + appId + "/").append(userId);
		response = callAuthManagerService(urlBuilder.toString(), HttpMethod.GET, null);
		return getUserDetailFromResponse(response);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.admin.accountmgmt.service.AccountManagementService#unBlockUserName(
	 * java.lang.String)
	 */
	@Override
	public StatusResponseDto unBlockUserName(String userId) {
		String response = null;
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(authManagerBaseUrl).append(unBlockUrl + "registrationclient/").append(userId);
		response = callAuthManagerService(urlBuilder.toString(), HttpMethod.GET, null);
		return getSuccessResponse(response);

	}

	/**
	 * Gets the success response.
	 *
	 * @param responseBody
	 *            the response body
	 * @return the success response
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.admin.accountmgmt.service.AccountManagementService#changePassword(io
	 * .mosip.admin.accountmgmt.dto.PasswordDto)
	 */
	@Override
	public StatusResponseDto changePassword(PasswordDto passwordDto) {
		passwordDto.setHashAlgo("SSHA-256");
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(authManagerBaseUrl).append(changePassword + appId+"/");
		HttpEntity<RequestWrapper<?>> passwordHttpEntity = getChangePasswordHttpRequest(passwordDto);
		String response = callAuthManagerService(urlBuilder.toString(), HttpMethod.POST, passwordHttpEntity);

		return getSuccessResponse(response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.admin.accountmgmt.service.AccountManagementService#resetPassword(io.
	 * mosip.admin.accountmgmt.dto.PasswordDto)
	 */
	@Override
	public StatusResponseDto resetPassword(ResetPasswordDto passwordDto) {
		passwordDto.setHashAlgo("SSHA-256");
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(authManagerBaseUrl).append(resetPassword + appId);
		HttpEntity<RequestWrapper<?>> passwordHttpEntity = getResetPasswordHttpRequest(passwordDto);
		String response = callAuthManagerService(urlBuilder.toString(), HttpMethod.POST, passwordHttpEntity);
		return getSuccessResponse(response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.admin.accountmgmt.service.AccountManagementService#
	 * getUserNameBasedOnMobileNumber(java.lang.String)
	 */
	@Override
	public UserNameDto getUserNameBasedOnMobileNumber(String mobile) {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(authManagerBaseUrl).append(userNameUrl + appId+"/").append(mobile);
		String response = callAuthManagerService(urlBuilder.toString(), HttpMethod.GET, null);
		return getUserDetailFromResponse(response);
	}
	
	/* (non-Javadoc)
	 * @see io.mosip.admin.accountmgmt.service.AccountManagementService#getUserDetailBasedOnMobileNumber(java.lang.String)
	 */
	@Override
	public UserDetailDto getUserDetailBasedOnMobileNumber(String mobile) {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(authManagerBaseUrl).append(userDetailUrl + appId+"/").append(mobile);
		String response = callAuthManagerService(urlBuilder.toString(), HttpMethod.GET, null);
		return getUserFromResponse(response);

	}
	
	/*
	 * (non-Javadoc)
	 * @see io.mosip.admin.accountmgmt.service.AccountManagementService#validateResponseDto(java.lang.String)
	 */
	@Override
	public ValidationResponseDto validateUserName(String userId) {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(authManagerBaseUrl).append(validateUrl + appId+"/").append(userId);
		String response = callAuthManagerService(urlBuilder.toString(), HttpMethod.GET, null);
		return getValidateResponse(response);
	}

	/**
	 * Call auth manager service.
	 *
	 * @param url
	 *            the url
	 * @param httpMethod
	 *            the http method
	 * @param requestEntity
	 *            the request entity
	 * @return the string
	 */
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

			if (validationErrorsList != null) {
				throw new AccountServiceException(validationErrorsList);
			} else {
				throw new AccountManagementServiceException(
						AccountManagementErrorCode.REST_SERVICE_EXCEPTION.getErrorCode(),
						AccountManagementErrorCode.REST_SERVICE_EXCEPTION.getErrorMessage() + ""
								+ ex.getResponseBodyAsString());
			}

		}

		return response;
	}

	/**
	 * Gets the user detail from response.
	 *
	 * @param responseBody
	 *            the response body
	 * @return the user detail from response
	 */
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

	/**
	 * Gets the http request.
	 *
	 * @param passwordDto
	 *            the password dto
	 * @return the http request
	 */
	private HttpEntity<RequestWrapper<?>> getChangePasswordHttpRequest(PasswordDto passwordDto) {
		RequestWrapper<PasswordDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId("ADMIN_REQUEST");
		requestWrapper.setVersion("V1.0");
		requestWrapper.setRequest(passwordDto);
		HttpHeaders syncDataRequestHeaders = new HttpHeaders();
		syncDataRequestHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(requestWrapper, syncDataRequestHeaders);

	}

	/**
	 * Gets the http request.
	 *
	 * @param passwordDto
	 *            the password dto
	 * @return {@link HttpEntity}
	 */
	private HttpEntity<RequestWrapper<?>> getResetPasswordHttpRequest(ResetPasswordDto passwordDto) {
		RequestWrapper<ResetPasswordDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId("ADMIN_REQUEST");
		requestWrapper.setVersion("V1.0");
		requestWrapper.setRequest(passwordDto);
		HttpHeaders syncDataRequestHeaders = new HttpHeaders();
		syncDataRequestHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(requestWrapper, syncDataRequestHeaders);

	}
	
	/**
	 * Gets the user detail from response.
	 *
	 * @param responseBody
	 *            the response body
	 * @return the user detail from response
	 */
	private UserDetailDto getUserFromResponse(String responseBody) {
		List<ServiceError> validationErrorsList = null;
		validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);
		UserDetailDto userDetailDto = null;
		if (!validationErrorsList.isEmpty()) {
			throw new AccountServiceException(validationErrorsList);
		}
		ResponseWrapper<UserDetailDto> responseObject = null;
		try {

			responseObject = objectMapper.readValue(responseBody, new TypeReference<ResponseWrapper<UserDetailDto>>() {
			});
			userDetailDto = responseObject.getResponse();
		} catch (IOException | NullPointerException exception) {
			throw new ParseResponseException(AccountManagementErrorCode.PARSE_EXCEPTION.getErrorCode(),
					AccountManagementErrorCode.PARSE_EXCEPTION.getErrorMessage() + exception.getMessage(), exception);
		}

		return userDetailDto;
	}
	
	
	/**
	 * Gets the user detail from response.
	 *
	 * @param responseBody
	 *            the response body
	 * @return the user detail from response
	 */
	private ValidationResponseDto getValidateResponse(String responseBody) {
		List<ServiceError> validationErrorsList = null;
		validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);
		ValidationResponseDto validationResponseDto = null;
		if (!validationErrorsList.isEmpty()) {
			throw new AccountServiceException(validationErrorsList);
		}
		ResponseWrapper<ValidationResponseDto> responseObject = null;
		try {

			responseObject = objectMapper.readValue(responseBody, new TypeReference<ResponseWrapper<ValidationResponseDto>>() {
			});
			validationResponseDto = responseObject.getResponse();
		} catch (IOException | NullPointerException exception) {
			throw new ParseResponseException(AccountManagementErrorCode.PARSE_EXCEPTION.getErrorCode(),
					AccountManagementErrorCode.PARSE_EXCEPTION.getErrorMessage() + exception.getMessage(), exception);
		}

		return validationResponseDto;
	}

	

}
