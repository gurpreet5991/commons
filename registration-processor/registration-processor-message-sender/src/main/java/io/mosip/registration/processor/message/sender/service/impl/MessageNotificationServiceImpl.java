package io.mosip.registration.processor.message.sender.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.code.ApiName;
import io.mosip.registration.processor.core.constant.IdType;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.constant.PacketFiles;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.exception.TemplateProcessingFailureException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.idrepo.dto.IdResponseDTO;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.notification.template.generator.dto.ResponseDto;
import io.mosip.registration.processor.core.notification.template.generator.dto.SmsRequestDto;
import io.mosip.registration.processor.core.notification.template.generator.dto.SmsResponseDto;
import io.mosip.registration.processor.core.packet.dto.demographicinfo.JsonValue;
import io.mosip.registration.processor.core.packet.dto.demographicinfo.identify.RegistrationProcessorIdentity;
import io.mosip.registration.processor.core.spi.message.sender.MessageNotificationService;
import io.mosip.registration.processor.core.spi.restclient.RegistrationProcessorRestClientService;
import io.mosip.registration.processor.core.util.JsonUtil;
import io.mosip.registration.processor.message.sender.exception.EmailIdNotFoundException;
import io.mosip.registration.processor.message.sender.exception.IDRepoResponseNull;
import io.mosip.registration.processor.message.sender.exception.PhoneNumberNotFoundException;
import io.mosip.registration.processor.message.sender.exception.TemplateGenerationFailedException;
import io.mosip.registration.processor.message.sender.exception.TemplateNotFoundException;
import io.mosip.registration.processor.message.sender.template.TemplateGenerator;
import io.mosip.registration.processor.packet.storage.exception.IdentityNotFoundException;
import io.mosip.registration.processor.packet.storage.exception.ParsingException;
import io.mosip.registration.processor.packet.storage.utils.Utilities;
import io.mosip.registration.processor.rest.client.utils.RestApiClient;
import io.mosip.registration.processor.status.code.RegistrationType;

/**
 * ServiceImpl class for sending notification.
 * 
 * @author Alok Ranjan
 * 
 * @since 1.0.0
 *
 */
@Service
public class MessageNotificationServiceImpl
		implements MessageNotificationService<SmsResponseDto, ResponseDto, MultipartFile[]> {

	/** The Constant VALUE. */
	private static final String VALUE = "value";

	/** The Constant UIN. */
	private static final String UIN = "UIN";

	/** The Constant FILE_SEPARATOR. */
	public static final String FILE_SEPARATOR = File.separator;

	/** The Constant ENCODING. */
	public static final String ENCODING = "UTF-8";

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(MessageNotificationServiceImpl.class);

	/** The primary language. */
	@Value("${mosip.primary-language}")
	private String langCode;

	/** The env. */
	@Autowired
	private Environment env;

	/** The adapter. */
	@Autowired
	private FileSystemAdapter adapter;

	/** The template generator. */
	@Autowired
	private TemplateGenerator templateGenerator;

	/** The utility. */
	@Autowired
	private Utilities utility;

	/** The rest client service. */
	@Autowired
	private RegistrationProcessorRestClientService<Object> restClientService;

	/** The resclient. */
	@Autowired
	private RestApiClient resclient;

	/** The email id. */
	private String emailId;

	/** The phone number. */
	private String phoneNumber;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.processor.core.spi.message.sender.
	 * MessageNotificationService#sendSmsNotification(java.lang.String,
	 * java.lang.String, io.mosip.registration.processor.core.constant.IdType,
	 * java.util.Map)
	 */
	@Override
	public SmsResponseDto sendSmsNotification(String templateTypeCode, String id, IdType idType,
			Map<String, Object> attributes, String regType) throws ApisResourceAccessException, IOException {
		SmsResponseDto response = null;
		SmsRequestDto smsDto = new SmsRequestDto();
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), id,
				"MessageNotificationServiceImpl::sendSmsNotification()::entry");
		try {
			setAttributes(id, idType, attributes, regType);

			InputStream in = templateGenerator.getTemplate(templateTypeCode, attributes, langCode);
			String artifact = IOUtils.toString(in, ENCODING);

			if (phoneNumber == null || phoneNumber.isEmpty()) {
				throw new PhoneNumberNotFoundException(PlatformErrorMessages.RPR_SMS_PHONE_NUMBER_NOT_FOUND.getCode());
			}
			smsDto.setNumber(phoneNumber);
			smsDto.setMessage(artifact);

			response = (SmsResponseDto) restClientService.postApi(ApiName.SMSNOTIFIER, "", "", smsDto,
					SmsResponseDto.class);

		} catch (TemplateNotFoundException | TemplateProcessingFailureException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					id, PlatformErrorMessages.RPR_SMS_TEMPLATE_GENERATION_FAILURE.name() + e.getMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new TemplateGenerationFailedException(
					PlatformErrorMessages.RPR_SMS_TEMPLATE_GENERATION_FAILURE.getCode(), e);
		}
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), id,
				"MessageNotificationServiceImpl::sendSmsNotification()::exit");

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.processor.core.spi.message.sender.
	 * MessageNotificationService#sendEmailNotification(java.lang.String,
	 * java.lang.String, io.mosip.registration.processor.core.constant.IdType,
	 * java.util.Map, java.lang.String[], java.lang.String, java.lang.Object)
	 */
	@Override
	public ResponseDto sendEmailNotification(String templateTypeCode, String id, IdType idType,
			Map<String, Object> attributes, String[] mailCc, String subject, MultipartFile[] attachment, String regType)
			throws Exception {
		ResponseDto response = null;
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), id,
				"MessageNotificationServiceImpl::sendEmailNotification()::entry");
		try {
			setAttributes(id, idType, attributes, regType);

			InputStream in = templateGenerator.getTemplate(templateTypeCode, attributes, langCode);
			String artifact = IOUtils.toString(in, ENCODING);

			if (emailId == null || emailId.isEmpty()) {
				throw new EmailIdNotFoundException(PlatformErrorMessages.RPR_EML_EMAILID_NOT_FOUND.getCode());
			}
			String[] mailTo = { emailId };

			response = sendEmail(mailTo, mailCc, subject, artifact, attachment);

		} catch (TemplateNotFoundException | TemplateProcessingFailureException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					id, PlatformErrorMessages.RPR_SMS_TEMPLATE_GENERATION_FAILURE.name() + e.getMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new TemplateGenerationFailedException(
					PlatformErrorMessages.RPR_SMS_TEMPLATE_GENERATION_FAILURE.getCode(), e);
		}
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), id,
				"MessageNotificationServiceImpl::sendEmailNotification()::exit");

		return response;
	}

	/**
	 * Send email.
	 *
	 * @param mailTo
	 *            the mail to
	 * @param mailCc
	 *            the mail cc
	 * @param subject
	 *            the subject
	 * @param artifact
	 *            the artifact
	 * @param attachment
	 *            the attachment
	 * @return the response dto
	 * @throws Exception
	 *             the exception
	 */
	private ResponseDto sendEmail(String[] mailTo, String[] mailCc, String subject, String artifact,
			MultipartFile[] attachment) throws Exception {
		LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();

		String apiHost = env.getProperty(ApiName.EMAILNOTIFIER.name());
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiHost);

		for (String item : mailTo) {
			builder.queryParam("mailTo", item);
		}

		if (mailCc != null) {
			for (String item : mailCc) {
				builder.queryParam("mailCc", item);
			}
		}

		builder.queryParam("mailSubject", subject);
		builder.queryParam("mailContent", artifact);

		params.add("attachments", attachment);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);

		Object response = resclient.postApi(builder.build().toUriString(), requestEntity, ResponseDto.class);

		return (ResponseDto) response;
	}

	/**
	 * Gets the template json.
	 *
	 * @param id
	 *            the id
	 * @param idType
	 *            the id type
	 * @param attributes
	 *            the attributes
	 * @param regType
	 *            the reg type
	 * @return the template json
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private Map<String, Object> setAttributes(String id, IdType idType, Map<String, Object> attributes, String regType)
			throws IOException {
		InputStream demographicInfoStream = null;
		Integer uin = null;
		if (idType.toString().equalsIgnoreCase(UIN)) {
			InputStream idJsonStream = adapter.getFile(id,
					PacketFiles.DEMOGRAPHIC.name() + FILE_SEPARATOR + PacketFiles.ID.name());
			String getJsonStringFromBytes = IOUtils.toString(idJsonStream, ENCODING);
			JSONObject identityJson = JsonUtil.objectMapperReadValue(getJsonStringFromBytes, JSONObject.class);
			JSONObject demographicIdentity = JsonUtil.getJSONObject(identityJson,
					utility.getGetRegProcessorDemographicIdentity());
			uin = JsonUtil.getJSONValue(demographicIdentity, UIN);
			attributes.put("RID", id);
			attributes.put("UIN", uin);
		} else {
			attributes.put("RID", id);
		}
		demographicInfoStream = adapter.getFile(id,
				PacketFiles.DEMOGRAPHIC.name() + FILE_SEPARATOR + PacketFiles.ID.name());
		String demographicInfo = IOUtils.toString(demographicInfoStream, ENCODING);

		if (regType.equalsIgnoreCase(RegistrationType.NEW.name())) {
			setAttributes(demographicInfo, attributes, regType);
		} else if (regType.equalsIgnoreCase(RegistrationType.ACTIVATED.name())
				|| regType.equalsIgnoreCase(RegistrationType.DEACTIVATED.name())) {
			setAttributesFromIdRepo(uin, attributes, regType);
		}

		return attributes;
	}

	/**
	 * Sets the attributes from id repo.
	 *
	 * @param uin
	 *            the uin
	 * @param attributes
	 *            the attributes
	 * @param regType
	 *            the reg type
	 * @return the map
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("rawtypes")
	private Map<String, Object> setAttributesFromIdRepo(Integer uin, Map<String, Object> attributes, String regType)
			throws IOException {
		List<String> pathsegments = new ArrayList<>();
		pathsegments.add(uin.toString());

		IdResponseDTO response = null;
		try {
			response = (IdResponseDTO) restClientService.getApi(ApiName.IDREPOSITORY, pathsegments, "", "",
					IdResponseDTO.class);

			if (response == null || response.getResponse() == null) {
				regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), uin.toString(),
						PlatformErrorMessages.RPR_PRT_IDREPO_RESPONSE_NULL.name());
				throw new IDRepoResponseNull(PlatformErrorMessages.RPR_PRT_IDREPO_RESPONSE_NULL.getCode());
			}

			String jsonString = new JSONObject((Map) response.getResponse().getIdentity()).toString();
			setAttributes(jsonString, attributes, regType);

		} catch (ApisResourceAccessException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					uin.toString(), PlatformErrorMessages.RPR_PRT_IDREPO_RESPONSE_NULL.name() + e.getStackTrace());
			throw new IDRepoResponseNull(PlatformErrorMessages.RPR_PRT_IDREPO_RESPONSE_NULL.getCode());
		}

		return attributes;
	}

	/**
	 * Gets the keysand values.
	 *
	 * @param idJsonString
	 *            the id json string
	 * @param attribute
	 *            the attribute
	 * @param regType
	 *            the reg type
	 * @return the keysand values
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> setAttributes(String idJsonString, Map<String, Object> attribute, String regType)
			throws IOException {
		JSONObject demographicIdentity = null;
		try {
			if (regType.equalsIgnoreCase(RegistrationType.NEW.name())) {
				JSONObject demographicjson = JsonUtil.objectMapperReadValue(idJsonString, JSONObject.class);
				demographicIdentity = JsonUtil.getJSONObject(demographicjson,
						utility.getGetRegProcessorDemographicIdentity());
			} else if (regType.equalsIgnoreCase(RegistrationType.ACTIVATED.name())
					|| regType.equalsIgnoreCase(RegistrationType.DEACTIVATED.name())) {
				demographicIdentity = JsonUtil.objectMapperReadValue(idJsonString, JSONObject.class);
			}

			if (demographicIdentity == null)
				throw new IdentityNotFoundException(PlatformErrorMessages.RPR_PIS_IDENTITY_NOT_FOUND.getMessage());

			String mapperJsonString = Utilities.getJson(utility.getConfigServerFileStorageURL(),
					utility.getGetRegProcessorIdentityJson());
			JSONObject mapperJson = JsonUtil.objectMapperReadValue(mapperJsonString, JSONObject.class);
			JSONObject mapperIdentity = JsonUtil.getJSONObject(mapperJson,
					utility.getGetRegProcessorDemographicIdentity());

			List<String> mapperJsonKeys = new ArrayList<>(mapperIdentity.keySet());
			for (String key : mapperJsonKeys) {
				JSONObject jsonValue = JsonUtil.getJSONObject(mapperIdentity, key);
				Object object = JsonUtil.getJSONValue(demographicIdentity, (String) jsonValue.get(VALUE));
				if (object instanceof ArrayList) {
					JSONArray node = JsonUtil.getJSONArray(demographicIdentity, (String) jsonValue.get(VALUE));
					JsonValue[] jsonValues = JsonUtil.mapJsonNodeToJavaObject(JsonValue.class, node);
					for (int count = 0; count < jsonValues.length; count++) {
						String lang = jsonValues[count].getLanguage();
						attribute.put(key + "_" + lang, jsonValues[count].getValue());
					}
				} else if (object instanceof LinkedHashMap) {
					JSONObject json = JsonUtil.getJSONObject(demographicIdentity, (String) jsonValue.get(VALUE));
					attribute.put(key, json.get(VALUE));
				} else {
					attribute.put(key, object);
				}
			}

			setEmailAndPhone(demographicIdentity);

		} catch (JsonParseException | JsonMappingException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					null, "Error while parsing Json file" + ExceptionUtils.getStackTrace(e));
			throw new ParsingException(PlatformErrorMessages.RPR_SYS_JSON_PARSING_EXCEPTION.getMessage(), e);
		}

		return attribute;
	}

	/**
	 * Sets the email and phone.
	 *
	 * @param demographicIdentity
	 *            the new email and phone
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void setEmailAndPhone(JSONObject demographicIdentity) throws IOException {

		String getIdentityJsonString = Utilities.getJson(utility.getConfigServerFileStorageURL(),
				utility.getGetRegProcessorIdentityJson());
		ObjectMapper mapIdentityJsonStringToObject = new ObjectMapper();
		RegistrationProcessorIdentity regProcessorIdentityJson = mapIdentityJsonStringToObject
				.readValue(getIdentityJsonString, RegistrationProcessorIdentity.class);
		String email = regProcessorIdentityJson.getIdentity().getEmail().getValue();
		String phone = regProcessorIdentityJson.getIdentity().getPhone().getValue();

		emailId = JsonUtil.getJSONValue(demographicIdentity, email);
		phoneNumber = JsonUtil.getJSONValue(demographicIdentity, phone);
	}

}