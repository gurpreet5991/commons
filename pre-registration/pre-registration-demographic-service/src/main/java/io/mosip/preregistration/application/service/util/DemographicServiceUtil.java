/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.service.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.application.code.RequestCodes;
import io.mosip.preregistration.application.dto.DemographicCreateResponseDTO;
import io.mosip.preregistration.application.dto.DemographicRequestDTO;
import io.mosip.preregistration.application.dto.DemographicUpdateResponseDTO;
import io.mosip.preregistration.application.errorcodes.ErrorCodes;
import io.mosip.preregistration.application.errorcodes.ErrorMessages;
import io.mosip.preregistration.application.exception.OperationNotAllowedException;
import io.mosip.preregistration.application.exception.SchemaValidationException;
import io.mosip.preregistration.application.exception.system.DateParseException;
import io.mosip.preregistration.application.exception.system.JsonParseException;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.entity.DemographicEntity;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.HashUtill;
import io.mosip.preregistration.core.util.ValidationUtil;

/**
 * This class provides the utility methods for DemographicService
 * 
 * @author Ravi C Balaji
 * @author Sanober Noor
 * @since 1.0.0
 */
@Component
public class DemographicServiceUtil {

	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;


	/**
	 * Logger instance
	 */
	private Logger log = LoggerConfiguration.logConfig(DemographicServiceUtil.class);

	@Autowired
	CryptoUtil cryptoUtil;

	/**
	 * This setter method is used to assign the initial demographic entity values to
	 * the createDTO
	 * 
	 * @param demographicEntity
	 *            pass the demographicEntity
	 * @return createDTO with the values
	 */
	public DemographicResponseDTO setterForCreateDTO(DemographicEntity demographicEntity) {
		log.info("sessionId", "idType", "id", "In setterForCreateDTO method of pre-registration service util");
		JSONParser jsonParser = new JSONParser();
		DemographicResponseDTO createDto = new DemographicResponseDTO();
		try {
			createDto.setPreRegistrationId(demographicEntity.getPreRegistrationId());
			createDto.setDemographicDetails((JSONObject) jsonParser.parse(new String(cryptoUtil
					.decrypt(demographicEntity.getApplicantDetailJson(), demographicEntity.getEncryptedDateTime()))));
			createDto.setStatusCode(demographicEntity.getStatusCode());
			createDto.setLangCode(demographicEntity.getLangCode());
			createDto.setCreatedBy(demographicEntity.getCreatedBy());
			createDto.setCreatedDateTime(getLocalDateString(demographicEntity.getCreateDateTime()));
			createDto.setUpdatedBy(demographicEntity.getUpdatedBy());
			createDto.setUpdatedDateTime(getLocalDateString(demographicEntity.getUpdateDateTime()));
		} catch (ParseException ex) {
			log.error("sessionId", "idType", "id",
					"In setterForCreateDTO method of pre-registration service- " + ex.getMessage());
			throw new JsonParseException(ErrorCodes.PRG_PAM_APP_007.getCode(),
					ErrorMessages.JSON_PARSING_FAILED.getMessage(), ex.getCause());
		}
		return createDto;
	}

	/**
	 * This setter method is used to assign the initial demographic entity values to
	 * the createDTO
	 * 
	 * @param demographicEntity
	 *            pass the demographicEntity
	 * @return createDTO with the values
	 */
	public DemographicCreateResponseDTO setterForCreatePreRegistration(DemographicEntity demographicEntity) {
		log.info("sessionId", "idType", "id", "In setterForCreateDTO method of pre-registration service util");
		JSONParser jsonParser = new JSONParser();
		DemographicCreateResponseDTO createDto = new DemographicCreateResponseDTO();
		try {
			createDto.setPreRegistrationId(demographicEntity.getPreRegistrationId());
			createDto.setDemographicDetails((JSONObject) jsonParser.parse(new String(cryptoUtil
					.decrypt(demographicEntity.getApplicantDetailJson(), demographicEntity.getEncryptedDateTime()))));
			createDto.setStatusCode(demographicEntity.getStatusCode());
			createDto.setLangCode(demographicEntity.getLangCode());
			createDto.setCreatedDateTime(getLocalDateString(demographicEntity.getCreateDateTime()));
		} catch (ParseException ex) {
			log.error("sessionId", "idType", "id",
					"In setterForCreateDTO method of pre-registration service- " + ex.getMessage());
			throw new JsonParseException(ErrorCodes.PRG_PAM_APP_007.getCode(),
					ErrorMessages.JSON_PARSING_FAILED.getMessage(), ex.getCause());
		}
		return createDto;
	}

	/**
	 * This setter method is used to assign the initial demographic entity values to
	 * the createDTO
	 * 
	 * @param demographicEntity
	 *            pass the demographicEntity
	 * @return createDTO with the values
	 */
	public DemographicUpdateResponseDTO setterForUpdatePreRegistration(DemographicEntity demographicEntity) {
		log.info("sessionId", "idType", "id", "In setterForCreateDTO method of pre-registration service util");
		JSONParser jsonParser = new JSONParser();
		DemographicUpdateResponseDTO createDto = new DemographicUpdateResponseDTO();
		try {
			createDto.setPreRegistrationId(demographicEntity.getPreRegistrationId());
			createDto.setDemographicDetails((JSONObject) jsonParser.parse(new String(cryptoUtil
					.decrypt(demographicEntity.getApplicantDetailJson(), demographicEntity.getEncryptedDateTime()))));
			createDto.setStatusCode(demographicEntity.getStatusCode());
			createDto.setLangCode(demographicEntity.getLangCode());
			createDto.setUpdatedDateTime(getLocalDateString(demographicEntity.getCreateDateTime()));
		} catch (ParseException ex) {
			log.error("sessionId", "idType", "id",
					"In setterForCreateDTO method of pre-registration service- " + ex.getMessage());
			throw new JsonParseException(ErrorCodes.PRG_PAM_APP_007.getCode(),
					ErrorMessages.JSON_PARSING_FAILED.getMessage(), ex.getCause());
		}
		return createDto;
	}

	/**
	 * This method is used to set the values from the request to the
	 * demographicEntity entity fields.
	 * 
	 * @param demographicRequest
	 *            pass demographicRequest
	 * @param requestId
	 *            pass requestId
	 * @param entityType
	 *            pass entityType
	 * @return demographic entity with values
	 */
	public DemographicEntity prepareDemographicEntityForCreate(DemographicRequestDTO demographicRequest,
			String statuscode, String userId, String preRegistrationId) {
		log.info("sessionId", "idType", "id", "In prepareDemographicEntity method of pre-registration service util");
		DemographicEntity demographicEntity = new DemographicEntity();
		demographicEntity.setPreRegistrationId(preRegistrationId);
		LocalDateTime encryptionDateTime = DateUtils.getUTCCurrentDateTime();
		log.info("sessionId", "idType", "id", "Encryption start time : " + DateUtils.getUTCCurrentDateTimeString());
		byte[] encryptedDemographicDetails = cryptoUtil
				.encrypt(demographicRequest.getDemographicDetails().toJSONString().getBytes(), encryptionDateTime);
		log.info("sessionId", "idType", "id", "Encryption end time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setApplicantDetailJson(encryptedDemographicDetails);
		demographicEntity.setLangCode(demographicRequest.getLangCode());
		demographicEntity.setCrAppuserId(userId);
		demographicEntity.setCreatedBy(userId);
		demographicEntity.setCreateDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		demographicEntity.setStatusCode(statuscode);
		log.info("sessionId", "idType", "id", "Hashing start time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setDemogDetailHash(HashUtill.hashUtill(demographicEntity.getApplicantDetailJson()));
		log.info("sessionId", "idType", "id", "Hashing end time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setUpdatedBy(userId);
		demographicEntity.setUpdateDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		demographicEntity.setEncryptedDateTime(encryptionDateTime);
		return demographicEntity;
	}

	/**
	 * This method is used to set the values from the request to the
	 * demographicEntity entity fields.
	 * 
	 * @param demographicRequest
	 *            pass demographicRequest
	 * @param requestId
	 *            pass requestId
	 * @param entityType
	 *            pass entityType
	 * @return demographic entity with values
	 */
	public DemographicEntity prepareDemographicEntityForUpdate(DemographicEntity demographicEntity,
			DemographicRequestDTO demographicRequest, String statuscode, String userId, String preRegistrationId) {
		log.info("sessionId", "idType", "id", "In prepareDemographicEntity method of pre-registration service util");
		demographicEntity.setPreRegistrationId(preRegistrationId);
		LocalDateTime encryptionDateTime = DateUtils.getUTCCurrentDateTime();
		log.info("sessionId", "idType", "id", "Encryption start time : " + DateUtils.getUTCCurrentDateTimeString());
		byte[] encryptedDemographicDetails = cryptoUtil
				.encrypt(demographicRequest.getDemographicDetails().toJSONString().getBytes(), encryptionDateTime);
		log.info("sessionId", "idType", "id", "Encryption end time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setApplicantDetailJson(encryptedDemographicDetails);
		demographicEntity.setLangCode(demographicRequest.getLangCode());
		demographicEntity.setStatusCode(statuscode);
		log.info("sessionId", "idType", "id", "Hashing start time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setDemogDetailHash(HashUtill.hashUtill(demographicEntity.getApplicantDetailJson()));
		log.info("sessionId", "idType", "id", "Hashing end time : " + DateUtils.getUTCCurrentDateTimeString());
		demographicEntity.setUpdateDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		demographicEntity.setEncryptedDateTime(encryptionDateTime);
		return demographicEntity;
	}

	/**
	 * This method is used to add the initial request values into a map for input
	 * validations.
	 *
	 * @param demographicRequestDTO
	 *            pass demographicRequestDTO
	 * @return a map for request input validation
	 */

	public Map<String, String> prepareRequestMap(MainRequestDTO<?> requestDto) {
		log.info("sessionId", "idType", "id", "In prepareRequestMap method of Login Service Util");
		Map<String, String> requestMap = new HashMap<>();
		requestMap.put("id", requestDto.getId());
		requestMap.put("version", requestDto.getVersion());
		LocalDate date = requestDto.getRequesttime().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
		requestMap.put("requesttime", date.toString());
		requestMap.put("request", requestDto.getRequest().toString());
		return requestMap;
	}
	// public Map<String, String>
	// prepareRequestParamMap(MainRequestDTO<DemographicRequestDTO>
	// demographicRequestDTO) {
	// log.info("sessionId", "idType", "id", "In prepareRequestParamMap method of
	// pre-registration service util");
	// Map<String, String> inputValidation = new HashMap<>();
	// inputValidation.put(RequestCodes.ID.getCode(),
	// demographicRequestDTO.getId());
	// inputValidation.put(RequestCodes.VER.getCode(),
	// demographicRequestDTO.getVersion());
	// inputValidation.put(RequestCodes.REQ_TIME.getCode(),
	// new
	// SimpleDateFormat(utcDateTimePattern).format(demographicRequestDTO.getRequesttime()));
	// inputValidation.put(RequestCodes.REQUEST.getCode(),
	// demographicRequestDTO.getRequest().toString());
	// return inputValidation;
	// }

	/**
	 * This method is used to set the JSON values to RequestCodes constants.
	 * 
	 * @param demographicData
	 *            pass demographicData
	 * @param identityKey
	 *            pass identityKey
	 * @return values from JSON based on key
	 * 
	 * @throws ParseException
	 *             On json Parsing Failed
	 * @throws org.json.simple.parser.ParseException
	 * 
	 */
	public JSONArray getValueFromIdentity(byte[] demographicData, String identityKey)
			throws ParseException, org.json.simple.parser.ParseException {
		log.info("sessionId", "idType", "id", "In getValueFromIdentity method of pre-registration service util ");
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = (JSONObject) jsonParser.parse(new String(demographicData));
		JSONObject identityObj = (JSONObject) jsonObj.get(RequestCodes.IDENTITY.getCode());
		return (JSONArray) identityObj.get(identityKey);
	}

	/**
	 * This method is used to set the JSON values to RequestCodes constants.
	 * 
	 * @param demographicData
	 *            pass demographicData
	 * @param identityKey
	 *            pass postalcode
	 * @return values from JSON
	 * 
	 * @throws ParseException
	 *             On json Parsing Failed
	 * @throws org.json.simple.parser.ParseException
	 * 
	 */

	public String getIdJSONValue(String demographicData, String value) throws ParseException {
		log.info("sessionId", "idType", "id",
				"In getValueFromIdentity method of pe-registration service util to get getIdJSONValue ");

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = (JSONObject) jsonParser.parse(demographicData);
		JSONObject identityObj = (JSONObject) jsonObj.get(RequestCodes.IDENTITY.getCode());
		if (identityObj.get(value) != null)
			return identityObj.get(value).toString();
		return "";

	}

	/**
	 * This method is used as Null checker for different input keys.
	 *
	 * @param key
	 *            pass the key
	 * @return true if key not null and return false if key is null.
	 */
	public boolean isNull(Object key) {
		if (key instanceof String) {
			if (key.equals(""))
				return true;
		} else if (key instanceof List<?>) {
			if (((List<?>) key).isEmpty())
				return true;
		} else {
			if (key == null)
				return true;
		}
		return false;

	}

	/**
	 * This method is used to validate Pending_Appointment and Booked status codes.
	 * 
	 * @param statusCode
	 *            pass statusCode
	 * @return true or false
	 */
	public boolean checkStatusForDeletion(String statusCode) {
		log.info("sessionId", "idType", "id", "In checkStatusForDeletion method of pre-registration service util ");
		if (statusCode.equals(StatusCodes.PENDING_APPOINTMENT.getCode())
				|| statusCode.equals(StatusCodes.BOOKED.getCode())) {
			return true;
		} else {
			throw new OperationNotAllowedException(ErrorCodes.PRG_PAM_APP_003.getCode(),
					ErrorMessages.DELETE_OPERATION_NOT_ALLOWED.getMessage());
		}
	}


	public String getCurrentResponseTime() {
		return DateUtils.formatDate(new Date(System.currentTimeMillis()), utcDateTimePattern);
	}

	public Date getDateFromString(String date) {
		log.info("sessionId", "idType", "id", "In getDateFromString method of pre-registration service util ");
		try {
			return new SimpleDateFormat(utcDateTimePattern).parse(date);
		} catch (java.text.ParseException ex) {
			log.error("sessionId", "idType", "id",
					"In getDateFromString method of pre-registration service- " + ex.getCause());
			throw new DateParseException(ErrorCodes.PRG_PAM_APP_011.getCode(),
					ErrorMessages.UNSUPPORTED_DATE_FORMAT.getMessage(), ex.getCause());
		}
	}

	public String getLocalDateString(LocalDateTime date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(utcDateTimePattern);
		return date.format(dateTimeFormatter);
	}

	
	public boolean isStatusValid(String status) {
		for (StatusCodes choice : StatusCodes.values())
			if (choice.getCode().equals(status))
				return true;
		return false;
	}

	/**
	 * This method will return the MainResponseDTO with id and version
	 * 
	 * @param mainRequestDto
	 * @return MainResponseDTO<?>
	 */
	public  MainResponseDTO<?> getMainResponseDto(MainRequestDTO<?> mainRequestDto ){
		log.info("sessionId", "idType", "id", "In getMainResponseDTO method of Login Common Util");
		MainResponseDTO<?> response=new MainResponseDTO<>();
		response.setId(mainRequestDto.getId());
		response.setVersion(mainRequestDto.getVersion());
		
		return response;
	}
	
}
