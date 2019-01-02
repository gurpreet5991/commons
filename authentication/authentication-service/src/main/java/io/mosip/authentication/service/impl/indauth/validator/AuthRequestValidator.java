package io.mosip.authentication.service.impl.indauth.validator;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.dto.indauth.AuthRequestDTO;
import io.mosip.authentication.core.dto.indauth.AuthTypeDTO;
import io.mosip.authentication.core.exception.IDDataValidationException;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.service.helper.DateHelper;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * 
 * This class validates the parameters for Authorization Request. The class
 * {@code AuthRequestValidator} validates AuthRequestDTO
 * 
 * @author Manoj SP
 * @author Rakesh Roshan
 * 
 */
@Component
public class AuthRequestValidator extends BaseAuthRequestValidator {

	/** The Constant AUTH_REQUEST. */
	private static final String AUTH_REQUEST = "authRequest";

	/** The Constant AUTH_TYPE. */
	private static final String AUTH_TYPE = "authType";

	/** The Constant MISSING_INPUT_PARAMETER. */
	private static final String MISSING_INPUT_PARAMETER = "MISSING_INPUT_PARAMETER - ";

	/** The Constant INVALID_INPUT_PARAMETER. */
	private static final String INVALID_INPUT_PARAMETER = "INVALID_INPUT_PARAMETER - ";

	/** The Constant VALIDATE. */
	private static final String VALIDATE = "VALIDATE";

	/** The Constant ID_AUTH_VALIDATOR. */
	private static final String AUTH_REQUEST_VALIDATOR = "AUTH_REQUEST_VALIDATOR";

	/** The Constant SESSION_ID. */
	private static final String SESSION_ID = "SESSION_ID";

	/** The Constant REQ_TIME. */
	private static final String REQ_TIME = "reqTime";

	/** The Constant REQ_HMAC. */
	private static final String REQ_HMAC = "reqHmac";

	/** The Constant VALIDATE_REQUEST_TIMED_OUT. */
	private static final String VALIDATE_REQUEST_TIMED_OUT = "validateRequestTimedOut";

	/** The Constant REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS. */
	private static final String REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS = "authrequest.received-time-allowed.in-hours";

	/** The Constant INVALID_AUTH_REQUEST. */
	private static final String INVALID_AUTH_REQUEST = "INVALID_AUTH_REQUEST-No auth type found";

	/** The mosip logger. */
	private static Logger mosipLogger = IdaLogger.getLogger(AuthRequestValidator.class);

	/** The date helper. */
	@Autowired
	private DateHelper dateHelper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.authentication.service.impl.indauth.validator.
	 * BaseAuthRequestValidator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return AuthRequestDTO.class.equals(clazz);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.authentication.service.impl.indauth.validator.
	 * BaseAuthRequestValidator#validate(java.lang.Object,
	 * org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {

		AuthRequestDTO authRequestDto = (AuthRequestDTO) target;

		if (authRequestDto != null) {
			validateReqTime(authRequestDto.getReqTime(), errors);

			if (!errors.hasErrors()) {
				validateRequestTimedOut(authRequestDto.getReqTime(), errors);
			}

			if (!errors.hasErrors()) {
				super.validate(target, errors);

				validateIdvId(authRequestDto.getIdvId(), authRequestDto.getIdvIdType(), errors);

				validateMuaCode(authRequestDto.getMuaCode(), errors);

				validateTxnId(authRequestDto.getTxnID(), errors);

				validateReqHmac(authRequestDto.getReqHmac(), errors);

				validateBioDetails(authRequestDto, errors);

				if (!errors.hasErrors()) {
					checkAuthRequest(authRequestDto, errors);
				}
			}
		} else {
			mosipLogger.error(SESSION_ID, AUTH_REQUEST_VALIDATOR, VALIDATE, INVALID_INPUT_PARAMETER + AUTH_REQUEST);
			errors.rejectValue(AUTH_REQUEST, IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorCode(),
					IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorMessage());
		}
	}

	/**
	 * Validate req hmac.
	 *
	 * @param reqHmac the req hmac
	 * @param errors  the errors
	 */
	private void validateReqHmac(String reqHmac, Errors errors) {
		if (Objects.isNull(reqHmac)) {
			mosipLogger.error(SESSION_ID, AUTH_REQUEST_VALIDATOR, VALIDATE, MISSING_INPUT_PARAMETER + REQ_HMAC);
			errors.rejectValue(REQ_HMAC, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					new Object[] { REQ_HMAC },
					IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage());
		}
	}

	/**
	 * Validate request timed out.
	 *
	 * @param reqTime the req time
	 * @param errors  the errors
	 */
	private void validateRequestTimedOut(String reqTime, Errors errors) {
		try {
			Instant reqTimeInstance = dateHelper.convertStringToDate(reqTime).toInstant();
			Instant now = Instant.now();
			mosipLogger.debug(SESSION_ID, AUTH_REQUEST_VALIDATOR, VALIDATE_REQUEST_TIMED_OUT,
					"reqTimeInstance" + reqTimeInstance.toString() + " -- current time : " + now.toString());
			Integer reqDateMaxTimeInt = env
					.getProperty(REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS, Integer.class);
			Long reqDateMaxTimeLong = env.getProperty(REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS, Long.class);
			if (null !=reqDateMaxTimeInt && null != reqDateMaxTimeLong && 
					Duration.between(reqTimeInstance, now).toHours() > reqDateMaxTimeInt) {
				mosipLogger.debug(SESSION_ID, AUTH_REQUEST_VALIDATOR, VALIDATE_REQUEST_TIMED_OUT,
						"Time difference in min : " + Duration.between(reqTimeInstance, now).toMinutes());
				mosipLogger.error(SESSION_ID, AUTH_REQUEST_VALIDATOR, VALIDATE_REQUEST_TIMED_OUT,
						"INVALID_AUTH_REQUEST_TIMESTAMP -- " + String.format(
								IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST_TIMESTAMP.getErrorMessage(),
								Duration.between(reqTimeInstance, now).toMinutes()
										- reqDateMaxTimeLong));
				errors.rejectValue(REQ_TIME,
						IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST_TIMESTAMP.getErrorCode(),
						new Object[] { env.getProperty(REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS, Integer.class) },
						IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST_TIMESTAMP.getErrorMessage());
			}
		} catch (DateTimeParseException | IDDataValidationException e) {
			mosipLogger.error(SESSION_ID, AUTH_REQUEST_VALIDATOR, VALIDATE_REQUEST_TIMED_OUT,
					INVALID_INPUT_PARAMETER + REQ_TIME);
			errors.rejectValue(REQ_TIME, IdAuthenticationErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					new Object[] { REQ_TIME },
					IdAuthenticationErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage());
		}

	}

	/**
	 * Check auth request.
	 *
	 * @param authRequest the auth request
	 * @param errors      the errors
	 */
	private void checkAuthRequest(AuthRequestDTO authRequest, Errors errors) {
		AuthTypeDTO authType = authRequest.getAuthType();
		if (!Objects.isNull(authType)) {
			boolean anyAuthType = Stream
					.<Supplier<Boolean>>of(authType::isOtp, authType::isBio, authType::isAddress,
							authType::isFullAddress, authType::isPin, authType::isPersonalIdentity)
					.anyMatch(Supplier<Boolean>::get);

			if (!anyAuthType) {
				mosipLogger.error(SESSION_ID, AUTH_REQUEST_VALIDATOR, VALIDATE, INVALID_AUTH_REQUEST);
				errors.rejectValue(AUTH_TYPE, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
						new Object[] { AUTH_TYPE },
						IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage());

			} else if (authType.isOtp()) {
				checkOTPAuth(authRequest, errors);
			} else if (authType.isPersonalIdentity() || authType.isAddress() || authType.isFullAddress()) {
				checkDemoAuth(authRequest, errors);
			}
		} else {
			mosipLogger.error(SESSION_ID, AUTH_REQUEST_VALIDATOR, VALIDATE, MISSING_INPUT_PARAMETER + AUTH_TYPE);
			errors.rejectValue(AUTH_TYPE, IdAuthenticationErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					new Object[] { AUTH_TYPE },
					IdAuthenticationErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage());
		}
	}

	/**
	 * Adding IdAuthValidator Methods in AuthRequestValidator Class *.
	 *
	 * @param id     the id
	 * @param idType the id type
	 * @param errors the errors
	 */
}
