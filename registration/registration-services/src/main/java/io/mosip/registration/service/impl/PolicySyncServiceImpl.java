package io.mosip.registration.service.impl;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.IOException;
import java.security.KeyManagementException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.dao.PolicySyncDAO;
import io.mosip.registration.dto.PublicKeyResponse;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.entity.KeyStore;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.BaseService;
import io.mosip.registration.service.PolicySyncService;
import io.mosip.registration.util.healthcheck.RegistrationAppHealthCheckUtil;
import io.mosip.registration.util.restclient.ServiceDelegateUtil;

/**
 * it does the key policy synch
 * 
 * @author Brahmananda Reddy
 * @since 1.0.0
 *
 */
@Service
public class PolicySyncServiceImpl extends BaseService implements PolicySyncService {
	@Autowired
	private ServiceDelegateUtil serviceDelegateUtil;
	@Autowired
	private PolicySyncDAO policySyncDAO;

	private static final Logger LOGGER = AppConfig.getLogger(PolicySyncServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.service.PolicySyncService#fetchPolicy(centerId)
	 */
	@Override
	synchronized public ResponseDTO fetchPolicy() {
		LOGGER.debug("REGISTRATION_KEY_POLICY_SYNC", APPLICATION_NAME, APPLICATION_ID,
				"synch the public key is started");
		KeyStore keyStore = null;
		ResponseDTO responseDTO = new ResponseDTO();
		if (!RegistrationAppHealthCheckUtil.isNetworkAvailable()) {
			LOGGER.error("REGISTRATION_KEY_POLICY_SYNC", APPLICATION_NAME, APPLICATION_ID, "user is not in online");
			setErrorResponse(responseDTO, RegistrationConstants.POLICY_SYNC_CLIENT_NOT_ONLINE_ERROR_MESSAGE, null);
		} else {
			keyStore = policySyncDAO.findByMaxExpireTime();

			if (keyStore != null) {
				Date validDate = new Date(keyStore.getValidTillDtimes().getTime());
				long difference = ChronoUnit.DAYS.between(new Date().toInstant(), validDate.toInstant());
				if (Integer
						.parseInt((String) ApplicationContext.map().get(RegistrationConstants.KEY_NAME)) < difference) {
					setSuccessResponse(responseDTO, RegistrationConstants.POLICY_SYNC_SUCCESS_MESSAGE, null);
				} else {

					try {
						getPublicKey(responseDTO);
					} catch (KeyManagementException | IOException | java.security.NoSuchAlgorithmException exception) {
						LOGGER.error("REGISTRATION_KEY_POLICY_SYNC", APPLICATION_NAME, APPLICATION_ID,
								exception.getMessage());

						setErrorResponse(responseDTO, RegistrationConstants.POLICY_SYNC_ERROR_MESSAGE, null);

					}
				}
			} else {
				try {
					getPublicKey(responseDTO);
				} catch (KeyManagementException | IOException | java.security.NoSuchAlgorithmException exception) {
					LOGGER.error("REGISTRATION_KEY_POLICY_SYNC", APPLICATION_NAME, APPLICATION_ID,
							exception.getMessage());
					setErrorResponse(responseDTO, RegistrationConstants.POLICY_SYNC_ERROR_MESSAGE, null);

				}

			}
		}
		return responseDTO;
	}

	public synchronized void getPublicKey(ResponseDTO responseDTO)
			throws KeyManagementException, IOException, java.security.NoSuchAlgorithmException {
		LOGGER.debug("REGISTRATION_KEY_POLICY_SYNC", APPLICATION_NAME, APPLICATION_ID,
				getCenterId(getStationId(getMacAddress())));
		KeyStore keyStore = new KeyStore();
		Map<String, String> requestParams = new HashMap<String, String>();
		requestParams.put("timeStamp", Instant.now().toString());
		requestParams.put("referenceId", getCenterId(getStationId(getMacAddress())));
		try {
			PublicKeyResponse<String> publicKeyResponse = (PublicKeyResponse<String>) serviceDelegateUtil
					.get("policysync", requestParams, false, RegistrationConstants.JOB_TRIGGER_POINT_SYSTEM);
			keyStore.setId(UUID.randomUUID().toString());
			keyStore.setPublicKey(((String) publicKeyResponse.getPublicKey()).getBytes());
			keyStore.setValidFromDtimes(Timestamp.valueOf(publicKeyResponse.getIssuedAt()));
			keyStore.setValidTillDtimes(Timestamp.valueOf(publicKeyResponse.getExpiryAt()));
			keyStore.setCreatedBy(getUserIdFromSession());
			keyStore.setCreatedDtimes(Timestamp.valueOf(DateUtils.getUTCCurrentDateTime()));
			policySyncDAO.updatePolicy(keyStore);
			responseDTO = setSuccessResponse(responseDTO, RegistrationConstants.POLICY_SYNC_SUCCESS_MESSAGE, null);
			LOGGER.debug("REGISTRATION_KEY_POLICY_SYNC", APPLICATION_NAME, APPLICATION_ID,
					"synch the public key is completed");

		} catch (HttpClientErrorException | RegBaseCheckedException exception) {
			LOGGER.error("REGISTRATION_KEY_POLICY_SYNC", APPLICATION_NAME, APPLICATION_ID, exception.getMessage());
			setErrorResponse(responseDTO, RegistrationConstants.POLICY_SYNC_ERROR_MESSAGE, null);

		}

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.service.PolicySyncService#checkKeyValidation()
	 */
	@Override
	public ResponseDTO checkKeyValidation() {

		LOGGER.info("REGISTRATION_KEY_POLICY_SYNC", APPLICATION_NAME, APPLICATION_ID, "Key Validation is started");

		ResponseDTO responseDTO = new ResponseDTO();

		try {

			KeyStore keyStore = policySyncDAO.findByMaxExpireTime();

			if (keyStore != null) {
				String val = getGlobalConfigValueOf(RegistrationConstants.KEY_NAME);
				if (val != null) {

					/* Get Calendar instance */
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Timestamp(System.currentTimeMillis()));
					cal.add(Calendar.DATE, +Integer.parseInt(val));

					/* Compare Key Validity Date with currentDate+configuredDays */
					if (keyStore.getValidTillDtimes().after(new Timestamp(cal.getTimeInMillis()))) {
						setSuccessResponse(responseDTO, RegistrationConstants.VALID_KEY, null);
					} else {
						setErrorResponse(responseDTO, RegistrationConstants.INVALID_KEY, null);
					}

				}
			} else {
				fetchPolicy();
			}
		} catch (RuntimeException runtimeException) {

			LOGGER.info("REGISTRATION_KEY_POLICY_SYNC", APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
			setErrorResponse(responseDTO, RegistrationConstants.INVALID_KEY, null);
		}

		LOGGER.info("REGISTRATION_KEY_POLICY_SYNC", APPLICATION_NAME, APPLICATION_ID, "Key Validation is started");

		return responseDTO;

	}

}
