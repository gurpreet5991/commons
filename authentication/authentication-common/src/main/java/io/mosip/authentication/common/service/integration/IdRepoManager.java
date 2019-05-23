package io.mosip.authentication.common.service.integration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.authentication.common.service.factory.RestRequestFactory;
import io.mosip.authentication.common.service.helper.RestHelper;
import io.mosip.authentication.core.constant.IdAuthCommonConstants;
import io.mosip.authentication.core.constant.IdAuthConfigKeyConstants;
import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.constant.RestServicesConstants;
import io.mosip.authentication.core.dto.RestRequestDTO;
import io.mosip.authentication.core.exception.IDDataValidationException;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.exception.RestServiceException;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.idrepository.core.constant.IdRepoConstants;
import io.mosip.idrepository.core.constant.IdRepoErrorConstants;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * 
 * @author Dinesh Karuppiah.T
 * @author Rakesh Roshan
 */

@Component
public class IdRepoManager {

	
	private static final String VID2 = "vid";

	/** The Constant EXPIRED_VID. */
	private static final String EXPIRED_VID = "Expired VID";

	/** The Constant ERRORMESSAGE_VID. */
	private static final String ERRORMESSAGE_VID = "message";
	
	private static final String ERROR_CODE = "errorCode";

	private static final String ERRORS = "errors";

	private static final String USER_ID_NOTEXIST_ERRORCODE = "KER-ATH-003";

	private static final String USER_ID_NOTEXIST_ERRORMSG = "User Detail doesn't exist";

	private static final String REG_ID = "rid";

	/**
	 * The Constant Id Repo Errors
	 */
	private static final List<String> ID_REPO_ERRORS_INVALID_UIN = Arrays.asList(
			IdRepoErrorConstants.NO_RECORD_FOUND.getErrorCode(),
			IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode());

	/**
	 * The Rest Helper
	 */
	@Autowired
	private RestHelper restHelper;

	/**
	 * The Restrequest Factory
	 */
	@Autowired
	private RestRequestFactory restRequestFactory;

	/**
	 * The Environment
	 */
	@Autowired
	private Environment environment;
	
	
	/** The logger. */
	private static Logger logger = IdaLogger.getLogger(IdRepoManager.class);

	/**
	 * Fetch data from Id Repo based on Individual's UIN / VID value and all UIN
	 * 
	 * @param uin
	 * @param isBio
	 * @return
	 * @throws IdAuthenticationBusinessException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getIdenity(String uin, boolean isBio) throws IdAuthenticationBusinessException {

		RestRequestDTO buildRequest;
		Map<String, Object> response = null;

		try {
			Map<String, String> params = new HashMap<>();
			params.put("uin", uin);
			if (isBio) {
				buildRequest = restRequestFactory.buildRequest(RestServicesConstants.ID_REPO_SERVICE, null, Map.class);
				params.put("type", "bio");
			} else {
				buildRequest = restRequestFactory.buildRequest(RestServicesConstants.ID_REPO_SERVICE_WITHOUT_TYPE, null,
						Map.class);
			}
			buildRequest.setPathVariables(params);
			response = restHelper.requestSync(buildRequest);
			if (environment.getProperty(IdRepoConstants.ACTIVE_STATUS.getValue()).equalsIgnoreCase(
					(String) ((Map<String, Object>) response.get("response")).get(IdAuthCommonConstants.STATUS))) {
				response.put("uin", uin);
			} else {
				throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UIN_DEACTIVATED);
			}

		} catch (RestServiceException e) {
			logger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getSimpleName(), e.getErrorCode(), e.getErrorText());
			Optional<Object> responseBody = e.getResponseBody();
			if (responseBody.isPresent()) {
				Map<String, Object> idrepoMap = (Map<String, Object>) responseBody.get();
				if (idrepoMap.containsKey(ERRORS)) {
					List<Map<String, Object>> idRepoerrorList = (List<Map<String, Object>>) idrepoMap.get(ERRORS);
					if (!idRepoerrorList.isEmpty()
							&& idRepoerrorList.stream().anyMatch(map -> map.containsKey("errCode")
									&& ID_REPO_ERRORS_INVALID_UIN.contains(map.get("errCode")))) {
						throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.INVALID_UIN, e);
					} else {
						throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS,
								e);
					}
				}
			}
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		} catch (IDDataValidationException e) {
			logger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getSimpleName(), e.getErrorCode(), e.getErrorText());
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.DATA_VALIDATION_FAILED, e);
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	public String getRIDByUID(String idvId) throws IdAuthenticationBusinessException {
		RestRequestDTO buildRequest = null;
		String rid = null;
		try {
			Map<String, String> params = new HashMap<>();
			params.put("appId", environment.getProperty(IdAuthConfigKeyConstants.APPLICATION_ID));
			params.put("uid", idvId);
			buildRequest = restRequestFactory.buildRequest(RestServicesConstants.USERID_RID, null, Map.class);

			buildRequest.setPathVariables(params);
			Map<String, Object> ridMap = restHelper.requestSync(buildRequest);
			rid = (String) ((Map<String, Object>) ridMap.get("response")).get("rid");
		} catch (RestServiceException e) {
			logger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getSimpleName(), e.getErrorCode(), e.getErrorText());
			Optional<Object> responseBody = e.getResponseBody();
			if (responseBody.isPresent()) {
				Map<String, Object> idrepoMap = (Map<String, Object>) responseBody.get();
				if (idrepoMap.containsKey(ERRORS)) {
					List<Map<String, Object>> idRepoerrorList = (List<Map<String, Object>>) idrepoMap.get(ERRORS);
					if (!idRepoerrorList.isEmpty()
							&& idRepoerrorList.stream().anyMatch(map -> map.containsKey(ERROR_CODE)
									&& USER_ID_NOTEXIST_ERRORCODE.equalsIgnoreCase((String) map.get(ERROR_CODE)))) {
						throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.INVALID_USERID);
					} else {
						throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS,
								e);
					}
				}
			}
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		}

		catch (IDDataValidationException e) {
			logger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getSimpleName(), e.getErrorCode(), e.getErrorText());
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.DATA_VALIDATION_FAILED, e);
		}
		return rid;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getUINByRID(String regID) throws IdAuthenticationBusinessException {
		RestRequestDTO buildRequest = null;
		Map<String, Object> uinMap = null;
		try {
			Map<String, String> params = new HashMap<>();
			params.put("rid", regID);
			buildRequest = restRequestFactory.buildRequest(RestServicesConstants.RID_UIN, null, Map.class);
			buildRequest.setPathVariables(params);
			uinMap = restHelper.requestSync(buildRequest);
		} catch (RestServiceException e) {
			logger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getSimpleName(), e.getErrorCode(), e.getErrorText());
			Optional<Object> responseBody = e.getResponseBody();
			if (responseBody.isPresent()) {
				Map<String, Object> idrepoMap = (Map<String, Object>) responseBody.get();
				if (idrepoMap.containsKey(ERRORS)) {
					List<Map<String, Object>> idRepoerrorList = (List<Map<String, Object>>) idrepoMap.get(ERRORS);

					if (!idRepoerrorList.isEmpty() && idRepoerrorList.stream()
							.anyMatch(map -> map.containsKey(ERROR_CODE) && IdRepoErrorConstants.INVALID_INPUT_PARAMETER
									.getErrorCode().equalsIgnoreCase((String) map.get(ERROR_CODE)))) {
						throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.INVALID_USERID);
					} else {
						throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS,
								e);
					}
				}
			}
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		} catch (IDDataValidationException e) {
			logger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getSimpleName(), e.getErrorCode(), e.getErrorText());
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.DATA_VALIDATION_FAILED, e);
		}
		return uinMap;
	}
	
	@SuppressWarnings("unchecked")
	public String getUINByVID(String vid) throws IdAuthenticationBusinessException {
		RestRequestDTO buildRequest;
		String uin = null;
		try {
			Map<String, String> params = new HashMap<>();
			params.put(VID2, vid);
			buildRequest = restRequestFactory.buildRequest(RestServicesConstants.VID_SERVICE, null, Map.class);
			buildRequest.setPathVariables(params);
			Map<String, Object> vidMap = restHelper.requestSync(buildRequest);
			List<Map<String, Object>> vidErrorList = (List<Map<String, Object>>) vidMap.get("errors");
			if ((null == vidErrorList || vidErrorList.isEmpty()) && vidMap.get("response") instanceof Map) {
				uin = (String) ((Map<String, Object>) vidMap.get("response")).get("UIN");
			}
		} catch (RestServiceException e) {
			logger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getSimpleName(), e.getErrorCode(), e.getErrorText());
			Optional<Object> responseBody = e.getResponseBody();
			if (responseBody.isPresent()) {
				Map<String, Object> idrepoMap = (Map<String, Object>) responseBody.get();
				if (idrepoMap.containsKey(ERRORS)) {
					List<Map<String, Object>> vidErrorList = (List<Map<String, Object>>) idrepoMap.get(ERRORS);
					if (vidErrorList.stream().anyMatch(
							map -> map.containsKey(ERRORMESSAGE_VID)
									&& ((String) map.get(ERRORMESSAGE_VID)).equalsIgnoreCase(
											String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), VID2)))) {
						throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.INVALID_VID);
					}

					else if (vidErrorList.stream()
							.anyMatch(map -> map.containsKey(ERRORMESSAGE_VID)
									&& ((String) map.get(ERRORMESSAGE_VID)).equalsIgnoreCase(EXPIRED_VID))) {
						throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.EXPIRED_VID);
					}
				}
			}
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS);
		} catch (IDDataValidationException e) {
			logger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getSimpleName(), e.getErrorCode(), e.getErrorText());
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS);
		}
		return uin;
	}

}
