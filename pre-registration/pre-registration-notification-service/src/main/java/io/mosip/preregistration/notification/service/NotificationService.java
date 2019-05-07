package io.mosip.preregistration.notification.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.util.NotificationUtil;
import io.mosip.preregistration.core.util.ValidationUtil;
import io.mosip.preregistration.notification.code.RequestCodes;
import io.mosip.preregistration.notification.dto.ResponseDTO;
import io.mosip.preregistration.notification.error.ErrorCodes;
import io.mosip.preregistration.notification.error.ErrorMessages;
import io.mosip.preregistration.notification.exception.MandatoryFieldException;
import io.mosip.preregistration.notification.exception.NotificationSeriveException;
import io.mosip.preregistration.notification.exception.util.NotificationExceptionCatcher;
import io.mosip.preregistration.notification.service.util.NotificationServiceUtil;

/**
 * The service class for notification.
 * 
 * @author Sanober Noor
 * @since 1.0.0
 *
 */
@Service
public class NotificationService {

	/**
	 * The reference to {@link NotificationUtil}.
	 */
	@Autowired
	private NotificationUtil notificationUtil;

	/**
	 * The reference to {@link NotificationServiceUtil}.
	 */
	@Autowired
	private NotificationServiceUtil serviceUtil;

	private Logger log = LoggerConfiguration.logConfig(NotificationService.class);

	Map<String, String> requiredRequestMap = new HashMap<>();
	/**
	 * Autowired reference for {@link #restTemplateBuilder}
	 */
	@Autowired
	RestTemplate restTemplate;

	@Value("${mosip.pre-registration.notification.id}")
	private String Id;

	@Value("${version}")
	private String version;

	/**
	 * 
	 */
	@Value("${demographic.resource.url}")
	private String demographicResourceUrl;
	/**
	 * 
	 */
	@Value("${preregistartion.response}")
	private String demographicResponse;
	
	@Value("${preregistartion.demographicDetails}")
	private String demographicDetails;
	
	@Value("${preregistartion.identity}")
	private String identity;
	
	@Value("${preregistartion.identity.email}")
	private String email;
	
	@Value("${preregistartion.identity.phone}")
	private String phone;

	MainResponseDTO<ResponseDTO> response;


	@PostConstruct
	public void setupBookingService() {
		requiredRequestMap.put("version", version);
	}

	/**
	 * Method to send notification.
	 * 
	 * @param jsonString
	 *            the json string.
	 * @param langCode
	 *            the language code.
	 * @param file
	 *            the file to send.
	 * @return the response dto.
	 */
	public MainResponseDTO<ResponseDTO> sendNotification(String jsonString, String langCode, MultipartFile file) {

		response=new MainResponseDTO<>();

		ResponseDTO notificationResponse = new ResponseDTO();
		log.info("sessionId", "idType", "id", "In notification service of sendNotification ");
		requiredRequestMap.put("id", Id);
		String resp = null;
		try {
			MainRequestDTO<NotificationDTO> notificationReqDTO = serviceUtil.createNotificationDetails(jsonString);
			response.setId(notificationReqDTO.getId());
			response.setVersion(notificationReqDTO.getVersion());
			NotificationDTO notificationDto = notificationReqDTO.getRequest();
			if (ValidationUtil.requestValidator(serviceUtil.prepareRequestMap(notificationReqDTO),
					requiredRequestMap)) {
				if (notificationDto.isAdditionalRecipient()) {
					if (notificationDto.getMobNum() != null && !notificationDto.getMobNum().isEmpty()) {
						notificationUtil.notify(RequestCodes.SMS.getCode(), notificationDto, langCode, file);
					}
					if (notificationDto.getEmailID() != null && !notificationDto.getEmailID().isEmpty()) {
						notificationUtil.notify(RequestCodes.EMAIL.getCode(), notificationDto, langCode, file);
					}
					if ((notificationDto.getEmailID() == null || notificationDto.getEmailID().isEmpty())
							&& (notificationDto.getMobNum() == null || notificationDto.getMobNum().isEmpty())) {
						throw new MandatoryFieldException(ErrorCodes.PRG_ACK_001.getCode(),
								ErrorMessages.MOBILE_NUMBER_OR_EMAIL_ADDRESS_NOT_FILLED.getCode(), response);
					}
					notificationResponse.setMessage(RequestCodes.MESSAGE.getCode());
				} else {
					resp = callGetDemographicDetailsWithPreIdRestService(notificationDto, langCode, file);
					notificationResponse.setMessage(resp);
				}
			}

			response.setResponse(notificationResponse);

		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", "In notification service of sendNotification " + ex.getMessage());
			new NotificationExceptionCatcher().handle(ex, response);
		} finally {
			response.setResponsetime(serviceUtil.getCurrentResponseTime());
		}
		return response;
	}

	private String callGetDemographicDetailsWithPreIdRestService(NotificationDTO notificationDto, String langCode,
			MultipartFile file) throws IOException {
		String url = demographicResourceUrl + "/" + "applications" + "/" + notificationDto.getPreRegistrationId();
		ObjectMapper mapper = new ObjectMapper();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		HttpEntity<MainResponseDTO<DemographicResponseDTO>> httpEntity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

		List<ServiceError> validationErrorList = ExceptionUtils.getServiceErrorList(responseEntity.getBody());
		if (!validationErrorList.isEmpty()) {
			throw new NotificationSeriveException(validationErrorList, response);
		}

		JsonNode responseNode = mapper.readTree(responseEntity.getBody());

		responseNode = responseNode.get(demographicResponse);
		if (responseNode.isArray()) {
			for (final JsonNode objNode : responseNode) {
				responseNode = objNode.get(demographicDetails);
				responseNode = responseNode.get(identity);
			}
		}else {
			responseNode = responseNode.get(demographicDetails);
			responseNode = responseNode.get(identity);
		}

		if (responseNode.get(email) != null) {
			String emailId = responseNode.get(email).asText();
			notificationDto.setEmailID(emailId);
			notificationUtil.notify(RequestCodes.EMAIL.getCode(), notificationDto, langCode, file);
		}
		if (responseNode.get(phone) != null) {
			String phoneNumber = responseNode.get(phone).asText();
			notificationDto.setMobNum(phoneNumber);
			notificationUtil.notify(RequestCodes.SMS.getCode(), notificationDto, langCode, file);

		}
		if (responseNode.get(email) == null && responseNode.get(phone) == null) {
			log.info("sessionId", "idType", "id",
					"In notification service of sendNotification failed to send Email and sms request ");
		}
		return RequestCodes.MESSAGE.getCode();
	}

}
