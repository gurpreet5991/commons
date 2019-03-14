package io.mosip.authentication.service.validator;

import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.dto.indauth.IdType;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ParseException;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.idvalidator.uin.impl.UinValidatorImpl;
import io.mosip.kernel.idvalidator.vid.impl.VidValidatorImpl;

/**
 * The Class IdAuthValidator.
 *
 * @author Manoj SP
 */
@Component
public abstract class IdAuthValidator implements Validator {

	private static final String REQUEST = "request";

	/** The Constant TSP_ID. */
	private static final String PARTNER_ID = "partnerID";

	/** The Constant TSP_ID. */
	private static final String MISP_LICENSE_KEY = "MISP License Key";

	/** The Constant IDV_ID_TYPE. */
	private static final String IDV_ID_TYPE = "individualIdType";

	/** The Constant IDV_ID. */
	private static final String IDV_ID = "individualId";

	/** The Constant MISSING_INPUT_PARAMETER. */
	private static final String MISSING_INPUT_PARAMETER = "MISSING_INPUT_PARAMETER - ";

	/** The Constant VALIDATE. */
	private static final String VALIDATE = "VALIDATE";

	/** The Constant ID_AUTH_VALIDATOR. */
	private static final String ID_AUTH_VALIDATOR = "ID_AUTH_VALIDATOR";

	/** The Constant SESSION_ID. */
	private static final String SESSION_ID = "SESSION_ID";

	/** The Constant DATETIME_PATTERN. */
	protected static final String DATETIME_PATTERN = "datetime.pattern";

	/** The Constant REQ_TIME. */
	private static final String REQ_TIME = "requestTime";

	/** The Constant TXN_ID. */
	private static final String TXN_ID = "transactionID";

	/** The Constant ID. */
	private static final String ID = "id";

	/** The Constant A_Z0_9_10. */
	private static final Pattern A_Z0_9_10 = Pattern.compile("^[A-Z0-9]{10}");

	/** The mosip logger. */
	private static Logger mosipLogger = IdaLogger.getLogger(IdAuthValidator.class);

	/** The Constant REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS. */
	private static final String REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS = "authrequest.received-time-allowed.in-hours";

	/** The uin validator. */
	@Autowired
	private UinValidatorImpl uinValidator;

	/** The vid validator. */
	@Autowired
	private VidValidatorImpl vidValidator;

	@Autowired
	protected Environment env;

	/**
	 * Validate id - check whether id is null or not.
	 *
	 * @param id     the id
	 * @param errors the errors
	 */
	public void validateId(String id, Errors errors) {
		// TODO check id based on the request and add validation for version.
		if (Objects.isNull(id) || id.isEmpty()) {
			mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE, MISSING_INPUT_PARAMETER + " - id");
			errors.rejectValue(ID, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					new Object[] { ID }, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage());
		}
	}

	public void validateIdvId(String id, String idType, Errors errors) {
		validateIdvId(id, idType, errors, REQUEST);
	}

	/**
	 * Validate individual's id - check whether id is null or not and if valid,
	 * validates idType and UIN/VID.
	 *
	 * @param id     the id
	 * @param idType the id type
	 * @param errors the errors
	 */
	public void validateIdvId(String id, String idType, Errors errors, String idFieldName) {
		if (Objects.isNull(id) || id.isEmpty()) {
			mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE, MISSING_INPUT_PARAMETER + IDV_ID);
			errors.rejectValue(idFieldName, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					new Object[] { IDV_ID }, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage());
		} else {
			validateIdtypeUinVid(id, idType, errors, idFieldName);
		}
	}

	/**
	 * Validate txn id - check whether it is of length 10 and alphanumeric.
	 *
	 * @param txnID  the txn ID
	 * @param errors the errors
	 */
	protected void validateTxnId(String txnID, Errors errors) {
		if (Objects.isNull(txnID)) {
			mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE, MISSING_INPUT_PARAMETER + TXN_ID);
			errors.rejectValue(TXN_ID, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					new Object[] { TXN_ID }, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage());
		} else if (!A_Z0_9_10.matcher(txnID).matches()) {
			mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE,
					"INVALID_INPUT_PARAMETER - txnID - value -> " + txnID);
			errors.rejectValue(TXN_ID, IdAuthenticationErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					new Object[] { TXN_ID }, IdAuthenticationErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage());
		}
	}

	/**
	 * Validate req time.
	 *
	 * @param reqTime the req time
	 * @param errors  the errors
	 */
	protected void validateReqTime(String reqTime, Errors errors) {

		if (Objects.isNull(reqTime)) {
			mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE,
					MISSING_INPUT_PARAMETER + REQ_TIME);
			errors.rejectValue(REQ_TIME, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					new Object[] { REQ_TIME },
					IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage());
		} else {
			checkFutureReqTime(reqTime, errors);
		}
	}

	/**
	 * Check future req time.
	 *
	 * @param reqTime the req time
	 * @param errors  the errors
	 */
	private void checkFutureReqTime(String reqTime, Errors errors) {

		Date reqDateAndTime = null;
		try {
			reqDateAndTime = DateUtils.parseToDate(reqTime, env.getProperty(DATETIME_PATTERN));
		} catch (ParseException e) {
			mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE,
					"ParseException : Invalid Date\n" + ExceptionUtils.getStackTrace(e));
			errors.rejectValue(REQ_TIME, IdAuthenticationErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					new Object[] { REQ_TIME },
					IdAuthenticationErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage());
		}

		if (reqDateAndTime != null && DateUtils.after(reqDateAndTime, new Date())) {
			mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE, "Invalid Date");
			errors.rejectValue(REQ_TIME, IdAuthenticationErrorConstants.INVALID_TIMESTAMP.getErrorCode(),
					new Object[] { env.getProperty(REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS, Integer.class) },
					IdAuthenticationErrorConstants.INVALID_TIMESTAMP.getErrorMessage());
		}
	}

	/**
	 * Validate UIN, VID.
	 *
	 * @param id     the id
	 * @param idType the id type
	 * @param errors the errors
	 */
	private void validateIdtypeUinVid(String id, String idType, Errors errors, String idFieldName) {
		if (Objects.isNull(idType)) {
			mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE,
					MISSING_INPUT_PARAMETER + IDV_ID_TYPE);
			errors.rejectValue(REQUEST, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					new Object[] { IDV_ID_TYPE }, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage());
		} else if (idType.equals(IdType.UIN.getType())) {
			try {
				uinValidator.validateId(id);
			} catch (InvalidIDException e) {
				mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE, "InvalidIDException - " + e);
				errors.rejectValue(idFieldName, IdAuthenticationErrorConstants.INVALID_UIN.getErrorCode(),
						IdAuthenticationErrorConstants.INVALID_UIN.getErrorMessage());

			}
		} else if (idType.equals(IdType.VID.getType())) {
			try {
				vidValidator.validateId(id);
			} catch (InvalidIDException e) {
				mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE, "InvalidIDException - " + e);
				errors.rejectValue(idFieldName, IdAuthenticationErrorConstants.INVALID_VID.getErrorCode(),
						IdAuthenticationErrorConstants.INVALID_VID.getErrorMessage());
			}
		} else {
			mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE, "INCORRECT_IDTYPE - " + idType);
			errors.rejectValue(IDV_ID_TYPE, IdAuthenticationErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					new Object[] { IDV_ID_TYPE },
					IdAuthenticationErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage());
		}
	}

	/**
	 * Validation for TspId.
	 *
	 * @param tspID  the tsp ID
	 * @param errors the errors
	 */
	protected void validateTspId(String tspID, Errors errors) {
		if (Objects.isNull(tspID) || tspID.isEmpty()) {
			mosipLogger.error(SESSION_ID, this.getClass().getSimpleName(), VALIDATE,
					MISSING_INPUT_PARAMETER + PARTNER_ID);
			errors.rejectValue(PARTNER_ID, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					new Object[] { PARTNER_ID },
					IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage());
		}

	}

	protected void validateLicenseKey(String misPlicenseKey, Errors errors) {
		if (Objects.isNull(misPlicenseKey) || misPlicenseKey.isEmpty()) {
			mosipLogger.error(SESSION_ID, ID_AUTH_VALIDATOR, VALIDATE, MISSING_INPUT_PARAMETER + MISP_LICENSE_KEY);
			errors.rejectValue(PARTNER_ID, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					new Object[] { MISP_LICENSE_KEY },
					IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage());
		}

	}

}
