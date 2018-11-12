package io.mosip.registration.service.impl;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import io.mosip.kernel.core.spi.logger.MosipLogger;
import io.mosip.registration.audit.AuditFactory;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.AppModule;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dao.RegistrationAppLoginDAO;
import io.mosip.registration.dao.RegistrationCenterDAO;
import io.mosip.registration.dao.RegistrationScreenAuthorizationDAO;
import io.mosip.registration.dao.RegistrationUserDetailDAO;
import io.mosip.registration.dao.RegistrationUserPasswordDAO;
import io.mosip.registration.dto.AuthorizationDTO;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.OtpGeneratorRequestDto;
import io.mosip.registration.dto.OtpGeneratorResponseDto;
import io.mosip.registration.dto.OtpValidatorResponseDto;
import io.mosip.registration.dto.RegistrationCenterDetailDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.entity.RegistrationUserDetail;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.LoginService;
import io.mosip.registration.util.restclient.ServiceDelegateUtil;

/**
 * Class for implementing login service
 * 
 * @author Sravya Surampalli
 * @since 1.0.0
 *
 */
@Service
public class LoginServiceImpl implements LoginService {

	/**
	 * Instance of LOGGER
	 */
	private static final MosipLogger LOGGER = AppConfig.getLogger(LoginServiceImpl.class);

	/**
	 * Instance of {@code AuditFactory}
	 */
	@Autowired
	private AuditFactory auditFactory;

	/**
	 * serviceDelegateUtil which processes the HTTPRequestDTO requests
	 */
	@Autowired
	private ServiceDelegateUtil serviceDelegateUtil;

	/**
	 * Class to retrieve the Login Details from DB
	 */
	@Autowired
	private RegistrationAppLoginDAO registrationAppLoginDAO;

	/**
	 * Class to retrieve the Registration Officer Credentials from DB
	 */
	@Autowired
	private RegistrationUserPasswordDAO registrationUserPasswordDAO;

	/**
	 * Class to retrieve the Registration Officer Details from DB
	 */
	@Autowired
	private RegistrationUserDetailDAO registrationUserDetailDAO;

	/**
	 * Class to retrieve the Registration Center details from DB
	 */
	@Autowired
	private RegistrationCenterDAO registrationCenterDAO;

	/**
	 * Class to retrieve the Registration screen authorization from DB
	 */
	@Autowired
	private RegistrationScreenAuthorizationDAO registrationScreenAuthorizationDAO;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mosip.registration.service.login.LoginService#getModesOfLogin()
	 */
	@Override
	public Map<String, Object> getModesOfLogin() {
		// Retrieve Login information

		LOGGER.debug("REGISTRATION - LOGINMODES - LOGINSERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Fetching list of login modes");

		auditFactory.audit(AuditEvent.LOGIN_MODES_FETCH, AppModule.LOGIN_MODES, "Fetching list of login modes",
				"refId", "refIdType");

		return registrationAppLoginDAO.getModesOfLogin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mosip.registration.service.login.LoginService#validateUserPassword(
	 * java.lang.String,java.lang.String)
	 */
	@Override
	public boolean validateUserPassword(String userId, String hashPassword) {
		// Validating Registration Officer Credentials

		LOGGER.debug("REGISTRATION - VALIDATECREDENTIALS - LOGINSERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Validating User credentials");

		auditFactory.audit(AuditEvent.VALIDATE_USER_CRED, AppModule.VALIDATE_USER,
				"Validating User credentials", "refId", "refIdType");

		return registrationUserPasswordDAO.getPassword(userId, hashPassword);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mosip.registration.service.login.LoginService#getUserDetail(java.lang.
	 * String)
	 */
	@Override
	public RegistrationUserDetail getUserDetail(String userId) {
		// Retrieving Registration Officer details

		LOGGER.debug("REGISTRATION - USERDETAIL - LOGINSERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Fetching User details");

		auditFactory.audit(AuditEvent.FETCH_USR_DET, AppModule.USER_DETAIL, "Fetching User details", "refId",
				"refIdType");

		return registrationUserDetailDAO.getUserDetail(userId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mosip.registration.service.login.LoginService#
	 * getRegistrationCenterDetails(java.lang.String)
	 */
	@Override
	public RegistrationCenterDetailDTO getRegistrationCenterDetails(String centerId) {
		// Retrieving Registration Center details

		LOGGER.debug("REGISTRATION - CENTERDETAILS - LOGINSERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Fetching Center details");

		auditFactory.audit(AuditEvent.FETCH_CNTR_DET, AppModule.CENTER_DETAIL, "Fetching Center details",
				"refId", "refIdType");

		return registrationCenterDAO.getRegistrationCenterDetails(centerId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mosip.registration.service.login.LoginService#
	 * getScreenAuthorizationDetails(java.lang.String)
	 */
	@Override
	public AuthorizationDTO getScreenAuthorizationDetails(String roleCode) {
		// Fetching screen authorization details

		LOGGER.debug("REGISTRATION - SCREENAUTHORIZATION - LOGINSERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Fetching list of Screens to be Authorized");

		auditFactory.audit(AuditEvent.FETCH_SCR_AUTH, AppModule.SCREEN_AUTH,
				"Fetching list of Screens to be Authorized", "refId", "refIdType");

		return registrationScreenAuthorizationDAO.getScreenAuthorizationDetails(roleCode);
	}

	

	/* (non-Javadoc)
	 * @see io.mosip.registration.service.LoginService#getOTP(java.lang.String)
	 */
	@Override
    public ResponseDTO getOTP(final String key) {
          LOGGER.debug("REGISTRATION - LOGIN - OTP", APPLICATION_NAME, APPLICATION_ID,
                       "Get OTP method called");

          // Create Response to return to UI layer
          ResponseDTO response = new ResponseDTO();
          OtpGeneratorRequestDto otpGeneratorRequestDto = new OtpGeneratorRequestDto();
          OtpGeneratorResponseDto otpGeneratorResponseDto = new OtpGeneratorResponseDto();

          SuccessResponseDTO successResponse = null;

          // prepare otpGeneratorRequestDto with specified key(EO Username) obtained from
          otpGeneratorRequestDto.setKey(key);

          try {

                 // obtain otpGeneratorResponseDto from serviceDelegateUtil
                 otpGeneratorResponseDto = (OtpGeneratorResponseDto) serviceDelegateUtil
                              .post(RegistrationConstants.OTP_GENERATOR_SERVICE_NAME, otpGeneratorRequestDto);
                 if (otpGeneratorResponseDto != null && otpGeneratorResponseDto.getOtp() != null) {

                       // create Success Response
                       successResponse = new SuccessResponseDTO();
                        successResponse.setCode(RegistrationConstants.ALERT_INFORMATION);
                       successResponse
                                     .setMessage(RegistrationConstants.OTP_GENERATION_SUCCESS_MESSAGE + otpGeneratorResponseDto.getOtp());

                       Map<String, Object> otherAttributes = new HashMap<String, Object>();
                     otherAttributes.put(RegistrationConstants.OTP_GENERATOR_RESPONSE_DTO, otpGeneratorResponseDto);

                        successResponse.setOtherAttributes(otherAttributes);
                        response.setSuccessResponseDTO(successResponse);
                       LOGGER.debug("REGISTRATION - LOGIN - OTP", APPLICATION_NAME,
                                     APPLICATION_ID, "Success Response created");

                 } else {
                       // create Error Response
                       response = getErrorResponse(response, RegistrationConstants.OTP_GENERATION_ERROR_MESSAGE);
                       LOGGER.debug("REGISTRATION - LOGIN - OTP", APPLICATION_NAME,
                                     APPLICATION_ID, "Error Response called");

                 }

          } catch (RegBaseCheckedException | HttpClientErrorException | HttpServerErrorException exception) {
                 // create Error Response
                 response = getErrorResponse(response, RegistrationConstants.OTP_GENERATION_ERROR_MESSAGE);
                 LOGGER.debug("REGISTRATION - LOGIN - OTP", APPLICATION_NAME,
                              APPLICATION_ID, "Error Response created");

          }

          LOGGER.debug("REGISTRATION - LOGIN - OTP", APPLICATION_NAME, APPLICATION_ID,
                       "Get OTP method ended");

          return response;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mosip.registration.service.LoginService#validateOTP(java.lang.String,
	 * java.lang.String)
	 */
	@Override
    public ResponseDTO validateOTP(final String key, final String otp) {

          LOGGER.debug("REGISTRATION - LOGIN - OTP", APPLICATION_NAME, APPLICATION_ID,
                       "Validation of OTP called");

          // Create Response to Return to UI layer
          ResponseDTO response = new ResponseDTO();
          SuccessResponseDTO successResponse;
          OtpValidatorResponseDto otpValidatorResponseDto = null;

          // Validator response service api creation
          final String SERVICE_NAME = RegistrationConstants.OTP_VALIDATOR_SERVICE_NAME;

          // prepare request params to pass through URI
          Map<String, String> requestParamMap = new HashMap<String, String>();
          requestParamMap.put(RegistrationConstants.USERNAME_KEY, key);
          requestParamMap.put(RegistrationConstants.OTP_GENERATED, otp);

          try {
                 // Obtain otpValidatorResponseDto from service delegate util
                 otpValidatorResponseDto = (OtpValidatorResponseDto) serviceDelegateUtil.get(SERVICE_NAME, requestParamMap);
                 if (otpValidatorResponseDto != null && otpValidatorResponseDto.getStatus() != null
                              && otpValidatorResponseDto.getStatus().equalsIgnoreCase("true")) {

                       // Create Success Response
                       successResponse = new SuccessResponseDTO();
                        successResponse.setCode(RegistrationConstants.ALERT_INFORMATION);
                 successResponse.setMessage(RegistrationConstants.OTP_VALIDATION_SUCCESS_MESSAGE);
                        response.setSuccessResponseDTO(successResponse);
                       LOGGER.debug("REGISTRATION - LOGIN - OTP", APPLICATION_NAME,
                                     APPLICATION_ID, "Success Response Created");

                 } else {

                       // Create Error response
                       response = getErrorResponse(response, RegistrationConstants.OTP_VALIDATION_ERROR_MESSAGE);
                       LOGGER.debug("REGISTRATION - LOGIN - OTP", APPLICATION_NAME,
                                    APPLICATION_ID, "Error Response Created");

                 }

          } catch (RegBaseCheckedException | HttpClientErrorException | HttpServerErrorException exception) {
                 // Create Error response
                 response = getErrorResponse(response, RegistrationConstants.OTP_VALIDATION_ERROR_MESSAGE);
                 LOGGER.debug("REGISTRATION - LOGIN - OTP", APPLICATION_NAME,
                              APPLICATION_ID, "Error Response Created");

          }

          LOGGER.debug("REGISTRATION - LOGIN - OTP", APPLICATION_NAME, APPLICATION_ID,
                       "Validation of OTP ended");

          return response;

    }


	private ResponseDTO getErrorResponse(ResponseDTO response, final String message) {
		// Create list of Error Response
		LinkedList<ErrorResponseDTO> errorResponses = new LinkedList<ErrorResponseDTO>();

		// Error response
		ErrorResponseDTO errorResponse = new ErrorResponseDTO();

		errorResponse.setCode(RegistrationConstants.ALERT_ERROR);
		errorResponse.setMessage(message);
		Map<String, Object> otherAttributes = new HashMap<String, Object>();
		otherAttributes.put(RegistrationConstants.OTP_VALIDATOR_RESPONSE_DTO, null);

		errorResponses.add(errorResponse);

		// Assing list of error responses to response
		response.setErrorResponseDTOs(errorResponses);
		return response;

	}
}
