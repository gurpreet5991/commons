package io.mosip.registration.processor.message.sender.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.fsadapter.exception.FSAdapterException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleManager;
import io.mosip.registration.processor.core.code.ApiName;
import io.mosip.registration.processor.core.code.EventId;
import io.mosip.registration.processor.core.code.EventName;
import io.mosip.registration.processor.core.code.EventType;
import io.mosip.registration.processor.core.constant.IdType;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.notification.template.generator.dto.ResponseDto;
import io.mosip.registration.processor.core.notification.template.generator.dto.SmsResponseDto;
import io.mosip.registration.processor.core.notification.template.generator.dto.TemplateResponseDto;
import io.mosip.registration.processor.core.spi.message.sender.MessageNotificationService;
import io.mosip.registration.processor.core.spi.restclient.RegistrationProcessorRestClientService;
import io.mosip.registration.processor.message.sender.exception.ConfigurationNotFoundException;
import io.mosip.registration.processor.message.sender.exception.EmailIdNotFoundException;
import io.mosip.registration.processor.message.sender.exception.PhoneNumberNotFoundException;
import io.mosip.registration.processor.message.sender.exception.TemplateGenerationFailedException;
import io.mosip.registration.processor.message.sender.exception.TemplateNotFoundException;
import io.mosip.registration.processor.message.sender.util.StatusNotificationTypeMapUtil;
import io.mosip.registration.processor.message.sender.utility.MessageSenderStatusMessage;
import io.mosip.registration.processor.message.sender.utility.NotificationTemplateCode;
import io.mosip.registration.processor.message.sender.utility.NotificationTemplateType;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import io.mosip.registration.processor.status.code.TransactionTypeCode;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.dto.TransactionDto;
import io.mosip.registration.processor.status.service.RegistrationStatusService;
import io.mosip.registration.processor.status.service.TransactionService;

/**
 * The Class MessageSenderStage.
 * 
 * @author M1048358 Alok
 * @since 1.0.0
 */
@RefreshScope
@Service
public class MessageSenderStage extends MosipVerticleManager {

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(MessageSenderStage.class);

	/** The registration status service. */
	@Autowired
	private RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	/** The transcation status service. */
	@Autowired
	private TransactionService<TransactionDto> transcationStatusService;

	/** The cluster manager url. */
	@Value("${vertx.ignite.configuration}")
	private String clusterManagerUrl;

	/** The core audit request builder. */
	@Autowired
	private AuditLogRequestBuilder auditLogRequestBuilder;

	/** The notification emails. */
	@Value("${registration.processor.notification.emails}")
	private String notificationEmails;
	
	/** The uin generated subject. */
	@Value("${registration.processor.uin.generated.subject}")
	private String uinGeneratedSubject;

	/** The duplicate uin subject. */
	@Value("${registration.processor.duplicate.uin.subject}")
	private String duplicateUinSubject;

	/** The reregister subject. */
	@Value("${registration.processor.reregister.subject}")
	private String reregisterSubject;

	@Value("${mosip.registration.processor.notification.types}")
	private String notificationTypes;

	/** The Constant TEMPLATES. */
	private static final String TEMPLATES = "templates";

	/** The rest client service. */
	@Autowired
	private RegistrationProcessorRestClientService<Object> restClientService;

	/** The service. */
	@Autowired
	private MessageNotificationService<SmsResponseDto, ResponseDto, MultipartFile[]> service;

	/** The Constant SMS_TYPE. */
	private static final String SMS_TYPE = "SMS";

	/** The Constant EMAIL_TYPE. */
	private static final String EMAIL_TYPE = "EMAIL";

	/** The is template available. */
	private boolean isTemplateAvailable = false;

	/** The sms template code. */
	private NotificationTemplateCode smsTemplateCode = null;

	/** The email template code. */
	private NotificationTemplateCode emailTemplateCode = null;

	/** The subject. */
	private String subject = "";

	/** The id type. */
	private IdType idType = null;

	/** The description. */
	private String description = "";

	/**
	 * Deploy verticle.
	 */
	public void deployVerticle() {
		MosipEventBus mosipEventBus = this.getEventBus(this.getClass(), clusterManagerUrl);
		this.consume(mosipEventBus, MessageBusAddress.MESSAGE_SENDER_BUS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.processor.core.spi.eventbus.EventBusManager#process(
	 * java.lang.Object)
	 */
	@Override
	public MessageDTO process(MessageDTO object) {
		object.setMessageBusAddress(MessageBusAddress.MESSAGE_SENDER_BUS);
		boolean isTransactionSuccessful = false;
		String id = object.getRid();
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
				"MessageSenderStage::process()::entry");
		InternalRegistrationStatusDto registrationStatusDto = registrationStatusService.getRegistrationStatus(id);

		try {
			StatusNotificationTypeMapUtil map = new StatusNotificationTypeMapUtil();
			NotificationTemplateType type = map.getTemplateType(registrationStatusDto.getStatusCode());
			if (type != null) {
				setTemplateAndSubject(type);
			}

			Map<String, Object> attributes = new HashMap<>();
			String[] ccEMailList = null;

			if (notificationTypes == null || notificationTypes.isEmpty()) {
                description = "Message sender failed for registrationId " + id + "::"
                        + PlatformErrorMessages.RPR_TEM_CONFIGURATION_NOT_FOUND.getCode();
                throw new ConfigurationNotFoundException(
						PlatformErrorMessages.RPR_TEM_CONFIGURATION_NOT_FOUND.getCode());
			}
			String[] allNotificationTypes = notificationTypes.split("\\|");

			if (notificationEmails != null && notificationEmails.length() > 0) {
				ccEMailList = notificationEmails.split("\\|");
			}

			sendNotification(id, attributes, ccEMailList, allNotificationTypes);
			isTransactionSuccessful = true;
            description = "Notification sent successfully for registrationId " + id;
            regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                    id, "MessageSenderStage::process()::exit");
            regProcLogger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                    id, description);
			registrationStatusDto.setStatusCode(RegistrationStatusCode.NOTIFICATION_SENT_TO_RESIDENT.toString());
			registrationStatusDto.setStatusComment(description);

			TransactionDto transactionDto = new TransactionDto(UUID.randomUUID().toString(),
					registrationStatusDto.getRegistrationId(), null, TransactionTypeCode.UPDATE.toString(),
					"updated registration status record", registrationStatusDto.getStatusCode(),
					registrationStatusDto.getStatusComment());
			transactionDto.setReferenceId(registrationStatusDto.getRegistrationId());
			transactionDto.setReferenceIdType("updated registration record");
			transcationStatusService.addRegistrationTransaction(transactionDto);

		} catch (EmailIdNotFoundException | PhoneNumberNotFoundException | TemplateGenerationFailedException
				| ConfigurationNotFoundException e) {

			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					id, e.getMessage() + ExceptionUtils.getStackTrace(e));
			description = "Email/phone/template/notification type is missing for registrationId " + id + "::"
					+ e.getMessage();
			throw new TemplateGenerationFailedException(PlatformErrorMessages.RPR_TEM_PROCESSING_FAILURE.getCode());
		} catch (TemplateNotFoundException tnf) {

            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
                    id, tnf.getMessage() + ExceptionUtils.getStackTrace(tnf));
            description = "template not found for notification with registrationId " + id + "::" + tnf.getMessage();
        } catch (FSAdapterException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), id,
					PlatformErrorMessages.RPR_TEM_PACKET_STORE_NOT_ACCESSIBLE.getMessage() + e.getMessage());
			description = "The Packet store set by the System is not accessible" + id;

		} catch (Exception ex) {

			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					id, ex.getMessage() + ExceptionUtils.getStackTrace(ex));
			description = "Internal error occured while processing registrationId " + id + "::" + ex.getMessage();
		} finally {
			String eventId = isTransactionSuccessful ? EventId.RPR_402.toString() : EventId.RPR_405.toString();
			String eventName = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventName.UPDATE.toString()
					: EventName.EXCEPTION.toString();
			String eventType = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType, id);

		}

		return object;
	}

	/**
	 * Send notification.
	 *
	 * @param id
	 *            the id
	 * @param attributes
	 *            the attributes
	 * @param ccEMailList
	 *            the cc E mail list
	 * @param allNotificationTypes
	 *            the all notification types
	 * @throws Exception
	 *             the exception
	 */
	private void sendNotification(String id, Map<String, Object> attributes, String[] ccEMailList,
			String[] allNotificationTypes) throws Exception {
		for (String notificationType : allNotificationTypes) {

			if (notificationType.equalsIgnoreCase(SMS_TYPE) && isTemplateAvailable(smsTemplateCode.name())) {

				service.sendSmsNotification(smsTemplateCode.name(), id, idType, attributes);
				regProcLogger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), id,
						MessageSenderStatusMessage.SMS_NOTIFICATION_SUCCESS);

			} else if (notificationType.equalsIgnoreCase(EMAIL_TYPE) && isTemplateAvailable(emailTemplateCode.name())) {

				service.sendEmailNotification(emailTemplateCode.name(), id, idType, attributes, ccEMailList, subject,
						null);
				regProcLogger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), id,
						MessageSenderStatusMessage.EMAIL_NOTIFICATION_SUCCESS);

			} else {
				throw new TemplateNotFoundException(MessageSenderStatusMessage.TEMPLATE_NOT_FOUND);
			}
		}
	}

	/**
	 * Sets the template and subject.
	 *
	 * @param templatetype
	 *            the new template and subject
	 */
	private void setTemplateAndSubject(NotificationTemplateType templatetype) {
		switch (templatetype) {
		case UIN_CREATED:
			smsTemplateCode = NotificationTemplateCode.RPR_UIN_GEN_SMS;
			emailTemplateCode = NotificationTemplateCode.RPR_UIN_GEN_EMAIL;
			idType = IdType.RID;
			subject = uinGeneratedSubject;
			break;
		case UIN_UPDATE:
			smsTemplateCode = NotificationTemplateCode.RPR_UIN_UPD_SMS;
			emailTemplateCode = NotificationTemplateCode.RPR_UIN_UPD_EMAIL;
			idType = IdType.RID;
			subject = uinGeneratedSubject;
			break;
		case DUPLICATE_UIN:
			smsTemplateCode = NotificationTemplateCode.RPR_DUP_UIN_SMS;
			emailTemplateCode = NotificationTemplateCode.RPR_DUP_UIN_EMAIL;
			idType = IdType.RID;
			subject = duplicateUinSubject;
			break;
		case TECHNICAL_ISSUE:
			smsTemplateCode = NotificationTemplateCode.RPR_TEC_ISSUE_SMS;
			emailTemplateCode = NotificationTemplateCode.RPR_TEC_ISSUE_EMAIL;
			idType = IdType.RID;
			subject = reregisterSubject;
			break;
		default:
			break;
		}
	}

	/**
	 * Checks if is template available.
	 *
	 * @param templateCode
	 *            the template code
	 * @return true, if is template available
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 */
	private boolean isTemplateAvailable(String templateCode) throws ApisResourceAccessException {

		List<String> pathSegments = new ArrayList<>();
		pathSegments.add(TEMPLATES);
		TemplateResponseDto template = (TemplateResponseDto) restClientService.getApi(ApiName.MASTER, pathSegments, "",
				"", TemplateResponseDto.class);
		template.getTemplates().forEach(dto -> {
			if (dto.getTemplateTypeCode().equalsIgnoreCase(templateCode)) {
				isTemplateAvailable = true;
			}
		});

		return isTemplateAvailable;
	}

}
