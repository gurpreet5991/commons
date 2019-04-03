package io.mosip.registration.service.config.impl;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.LoggerConstants;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.dao.GlobalParamDAO;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.entity.GlobalParam;
import io.mosip.registration.entity.id.GlobalParamId;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.BaseService;
import io.mosip.registration.service.config.GlobalParamService;
import io.mosip.registration.util.healthcheck.RegistrationAppHealthCheckUtil;

/**
 * Class for implementing GlobalContextParam service
 * 
 * @author Sravya Surampalli
 * @since 1.0.0
 *
 */
@Service
public class GlobalParamServiceImpl extends BaseService implements GlobalParamService {

	/**
	 * Instance of LOGGER
	 */
	private static final Logger LOGGER = AppConfig.getLogger(GlobalParamServiceImpl.class);

	/**
	 * Class to retrieve Global parameters of application
	 */
	@Autowired
	private GlobalParamDAO globalParamDAO;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.service.GlobalParamService#getGlobalParams
	 */
	public Map<String, Object> getGlobalParams() {

		LOGGER.info(LoggerConstants.GLOBAL_PARAM_SERVICE_LOGGER_TITLE, APPLICATION_NAME, APPLICATION_ID,
				"Fetching list of global params");

		return globalParamDAO.getGlobalParams();
	}

	@Override
	public ResponseDTO synchConfigData(boolean isJob) {
		LOGGER.info(LoggerConstants.GLOBAL_PARAM_SERVICE_LOGGER_TITLE, APPLICATION_NAME, APPLICATION_ID,
				"config data synch is started");

		ResponseDTO responseDTO = new ResponseDTO();

		String triggerPoint = (isJob ? RegistrationConstants.JOB_TRIGGER_POINT_SYSTEM
				: RegistrationConstants.JOB_TRIGGER_POINT_USER);

		saveGlobalParams(responseDTO, triggerPoint);

		if (!isJob) {
			/* If unable to fetch from server and no data in DB create error response */
			if (responseDTO.getSuccessResponseDTO() == null && getGlobalParams().isEmpty()) {
				setErrorResponse(responseDTO, RegistrationConstants.POLICY_SYNC_ERROR_MESSAGE, null);
			} else if (responseDTO.getSuccessResponseDTO() != null){
				setSuccessResponse(responseDTO, RegistrationConstants.POLICY_SYNC_SUCCESS_MESSAGE, responseDTO.getSuccessResponseDTO().getOtherAttributes());
			}
		}

		LOGGER.info(LoggerConstants.GLOBAL_PARAM_SERVICE_LOGGER_TITLE, APPLICATION_NAME, APPLICATION_ID,
				"config data synch is completed");

		return responseDTO;
	}

	@SuppressWarnings("unchecked")
	private void parseToMap(HashMap<String, Object> map, HashMap<String, String> globalParamMap) {
		for (Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();

			if (entry.getValue() instanceof HashMap) {
				parseToMap((HashMap<String, Object>) entry.getValue(), globalParamMap);
			} else {
				globalParamMap.put(key, String.valueOf(entry.getValue()));
			}
		}
	}

	private void saveGlobalParams(ResponseDTO responseDTO, String triggerPoinnt) {
		if (RegistrationAppHealthCheckUtil.isNetworkAvailable()) {
			try {
				boolean isToBeRestarted = false;
				Map<String, String> requestParamMap = new HashMap<>();

				/* REST CALL */
				@SuppressWarnings("unchecked")
				HashMap<String, Object> globalParamJsonMap = (HashMap<String, Object>) serviceDelegateUtil
						.get(RegistrationConstants.GET_GLOBAL_CONFIG, requestParamMap, true, triggerPoinnt);
				if (null != globalParamJsonMap.get(RegistrationConstants.PACKET_STATUS_READER_RESPONSE)) {
					HashMap<String, String> globalParamMap = new HashMap<>();
					parseToMap(globalParamJsonMap, globalParamMap);

					List<GlobalParam> globalParamList = globalParamDAO.getAllEntries();

					for (GlobalParam globalParam : globalParamList) {

						/* Check in map, if exists, update it and remove from map */
						GlobalParamId globalParamId = globalParam.getGlobalParamId();

						if (globalParamMap.get(globalParamId.getCode()) != null) {

						/* update (Local already exists) but val change */
						if (!globalParamMap.get(globalParamId.getCode()).trim().equals(globalParam.getVal())
								|| !(globalParam.getIsActive().booleanValue())) {
							String val = globalParamMap.get(globalParamId.getCode()).trim();
							updateVal(globalParam, val);

								/* Add in application map */
								ApplicationContext.setGlobalConfigValueOf(globalParamId.getCode(), val);

								if (globalParamId.getCode().contains("kernel") || globalParamId.getCode().contains("mosip.primary")) {
									isToBeRestarted = true;
								}
							}
						}
						/* Set is deleted true as removed from server */
						else {
							updateIsDeleted(globalParam);
							ApplicationContext.removeGlobalConfigValueOf(globalParamId.getCode());
						}
						globalParamMap.remove(globalParamId.getCode());
					}

					for (Entry<String, String> key : globalParamMap.entrySet()) {
						createNew(key.getKey(), globalParamMap.get(key.getKey()), globalParamList);

						if (key.getKey().contains("kernel") || key.getKey().contains("mosip.primary")) {
							isToBeRestarted = true;
						}
						/* Add in application map */
						ApplicationContext.setGlobalConfigValueOf(key.getKey(), key.getValue());
					}

					/* Save all Global Params */
					globalParamDAO.saveAll(globalParamList);
					if (isToBeRestarted) {
						Map<String, Object> attributes = new HashMap<>();
						attributes.put("Restart", RegistrationConstants.ENABLE);
						setSuccessResponse(responseDTO, RegistrationConstants.POLICY_SYNC_SUCCESS_MESSAGE, attributes);
					} else {
						setSuccessResponse(responseDTO, RegistrationConstants.POLICY_SYNC_SUCCESS_MESSAGE, null);
					}
				} else {
					setErrorResponse(responseDTO, RegistrationConstants.POLICY_SYNC_ERROR_MESSAGE, null);
				}
			} catch (HttpServerErrorException | HttpClientErrorException | SocketTimeoutException
					| RegBaseCheckedException | ClassCastException | ResourceAccessException exception) {
				setErrorResponse(responseDTO, RegistrationConstants.POLICY_SYNC_ERROR_MESSAGE, null);
				LOGGER.error("REGISTRATION_SYNCH_CONFIG_DATA", APPLICATION_NAME, APPLICATION_ID,
						exception.getMessage() + ExceptionUtils.getStackTrace(exception));
			}
		} else {
			LOGGER.error(LoggerConstants.GLOBAL_PARAM_SERVICE_LOGGER_TITLE, APPLICATION_NAME, APPLICATION_ID,
					" Unable to synch config data as no internet connection and no data in DB");
		}
	}

	private void updateVal(GlobalParam globalParam, String val) {
		globalParam.setVal(val);
		globalParam.setUpdBy(getUserIdFromSession());
		globalParam.setUpdDtimes(Timestamp.valueOf(DateUtils.getUTCCurrentDateTime()));
		globalParam.setIsActive(true);
		globalParam.setIsDeleted(false);
	}

	private void updateIsDeleted(GlobalParam globalParam) {
		globalParam.setIsActive(false);
		globalParam.setIsDeleted(true);
		globalParam.setUpdBy(getUserIdFromSession());
		globalParam.setUpdDtimes(Timestamp.valueOf(DateUtils.getUTCCurrentDateTime()));
	}

	private void createNew(String code, String value, List<GlobalParam> globalParamList) {
		GlobalParam globalParam = new GlobalParam();

		GlobalParamId globalParamId = new GlobalParamId();
		globalParamId.setCode(code);
		globalParamId.setLangCode(RegistrationConstants.ENGLISH_LANG_CODE);

		/* TODO Need to Add Description not key (CODE) */
		globalParam.setName(code);
		globalParam.setTyp("CONFIGURATION");
		globalParam.setIsActive(true);
		globalParam.setCrBy(getUserIdFromSession());
		globalParam.setCrDtime(Timestamp.valueOf(DateUtils.getUTCCurrentDateTime()));
		globalParam.setVal(value);
		globalParam.setGlobalParamId(globalParamId);
		globalParamList.add(globalParam);
	}

	/* (non-Javadoc)
	 * @see io.mosip.registration.service.config.GlobalParamService#updateSoftwareUpdateStatus(boolean)
	 */
	@Override
	public ResponseDTO updateSoftwareUpdateStatus(boolean isUpdateAvailable) {

		LOGGER.info(LoggerConstants.GLOBAL_PARAM_SERVICE_LOGGER_TITLE, APPLICATION_NAME, APPLICATION_ID,
				"Updating the SoftwareUpdate flag started.");

		ResponseDTO responseDTO = new ResponseDTO();

		GlobalParam globalParam = globalParamDAO.updateSoftwareUpdateStatus(isUpdateAvailable);

		if (globalParam.getVal().equalsIgnoreCase(RegistrationConstants.ENABLE)) {

			SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
			successResponseDTO.setMessage(RegistrationConstants.SOFTWARE_UPDATE_SUCCESS_MSG);
			responseDTO.setSuccessResponseDTO(successResponseDTO);

		} else {

			List<ErrorResponseDTO> errorResponseDTOs = new ArrayList<>();
			ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
			errorResponseDTO.setMessage(RegistrationConstants.SOFTWARE_UPDATE_FAILURE_MSG);
			errorResponseDTOs.add(errorResponseDTO);
			responseDTO.setErrorResponseDTOs(errorResponseDTOs);

		}
		LOGGER.info(LoggerConstants.GLOBAL_PARAM_SERVICE_LOGGER_TITLE, APPLICATION_NAME, APPLICATION_ID,
				"Updating the SoftwareUpdate flag ended.");
		return responseDTO;
	}
}
