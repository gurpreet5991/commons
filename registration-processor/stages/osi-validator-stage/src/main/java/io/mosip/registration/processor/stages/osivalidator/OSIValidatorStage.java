package io.mosip.registration.processor.stages.osivalidator;

import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

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
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.stages.osivalidator.utils.StatusMessage;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.service.RegistrationStatusService;

/**
 * The Class OSIValidatorStage.
 */
@Service
public class OSIValidatorStage extends MosipVerticleManager {

	/** The Constant USER. */
	private static final String USER = "MOSIP_SYSTEM";
	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(OSIValidatorStage.class);

	/** The registration status service. */
	@Autowired
	RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	/** The audit log request builder. */
	@Autowired
	AuditLogRequestBuilder auditLogRequestBuilder;

	/** The osi validator. */
	@Autowired
	OSIValidator osiValidator;

	/** The umc validator. */
	@Autowired
	UMCValidator umcValidator;

	@Value("${vertx.ignite.configuration}")
	private String clusterManagerUrl;

	private static final String OSI_VALIDATOR_FAILED = "OSI validation failed for registrationId ";

	/**
	 * Deploy verticle.
	 */
	public void deployVerticle() {
		MosipEventBus mosipEventBus = this.getEventBus(this.getClass(), clusterManagerUrl);
		this.consumeAndSend(mosipEventBus, MessageBusAddress.OSI_BUS_IN, MessageBusAddress.OSI_BUS_OUT);

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
		object.setMessageBusAddress(MessageBusAddress.OSI_BUS_IN);
		object.setIsValid(Boolean.FALSE);
		object.setInternalError(Boolean.FALSE);
		String description = "";
		boolean isTransactionSuccessful = false;
		boolean isValidUMC = false;
		String registrationId = object.getRid();
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				registrationId, "OSIValidatorStage::process()::entry");
		boolean isValidOSI = false;
		InternalRegistrationStatusDto registrationStatusDto = registrationStatusService
				.getRegistrationStatus(registrationId);

		osiValidator.registrationStatusDto = registrationStatusDto;
		umcValidator.setRegistrationStatusDto(registrationStatusDto);
		try {
			isValidUMC = umcValidator.isValidUMC(registrationId);
			if (isValidUMC) {
				isValidOSI = osiValidator.isValidOSI(registrationId);
			}
			if (isValidUMC && isValidOSI) {
				object.setIsValid(Boolean.TRUE);
				registrationStatusDto.setStatusComment(StatusMessage.OSI_VALIDATION_SUCCESS);
				registrationStatusDto.setStatusCode(RegistrationStatusCode.PACKET_OSI_VALIDATION_SUCCESS.toString());
				isTransactionSuccessful = true;
				description = "OSI validation successful for registration id : " + registrationId;
			} else {
				object.setIsValid(Boolean.FALSE);
				int retryCount = registrationStatusDto.getRetryCount() != null
						? registrationStatusDto.getRetryCount() + 1
						: 1;
				registrationStatusDto.setRetryCount(retryCount);

				registrationStatusDto.setStatusComment(osiValidator.registrationStatusDto.getStatusComment());
				registrationStatusDto.setStatusCode(RegistrationStatusCode.PACKET_OSI_VALIDATION_FAILED.toString());

				description = "OSI validation Failed for registrationId " + registrationId + "::" + "either UMC("
						+ isValidUMC + ")/OSI(" + isValidOSI + ") is not valid";
			}
			registrationStatusDto.setUpdatedBy(USER);
			registrationStatusService.updateRegistrationStatus(registrationStatusDto);
			regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationId, "OSIValidatorStage::process()::exit");
			regProcLogger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationId, description);
		} catch (FSAdapterException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationId, PlatformErrorMessages.OSI_VALIDATION_PACKET_STORE_NOT_ACCESSIBLE.name()
							+ e.getMessage() + ExceptionUtils.getStackTrace(e));
			object.setInternalError(Boolean.TRUE);
			description = "The Packet store set by the System is not accessible for registartion id : "
					+ registrationId;
		} catch (DataAccessException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationId, PlatformErrorMessages.OSI_VALIDATION_FAILED.name() + e.getMessage()
							+ ExceptionUtils.getStackTrace(e));
			object.setInternalError(Boolean.TRUE);
			description = "Data voilation in OSIValidator for registrationId " + registrationId + e.getMessage();

		} catch (IOException | ApisResourceAccessException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationId, PlatformErrorMessages.OSI_VALIDATION_FAILED.name() + e.getMessage()
							+ ExceptionUtils.getStackTrace(e));

			object.setInternalError(Boolean.TRUE);
			if (e.getCause() instanceof HttpClientErrorException) {
				HttpClientErrorException httpClientException = (HttpClientErrorException) e.getCause();
				description = OSI_VALIDATOR_FAILED + registrationId + "::"
						+ httpClientException.getResponseBodyAsString();

			} else if (e.getCause() instanceof HttpServerErrorException) {
				HttpServerErrorException httpServerException = (HttpServerErrorException) e.getCause();
				description = OSI_VALIDATOR_FAILED + registrationId + "::"
						+ httpServerException.getResponseBodyAsString();

			} else {
				description = "Internal error occured while processing registrationId" + registrationId
						+ e.getMessage();

			}

		} catch (Exception ex) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationId, PlatformErrorMessages.OSI_VALIDATION_FAILED.name() + ex.getMessage()
							+ ExceptionUtils.getStackTrace(ex));
			object.setInternalError(Boolean.TRUE);
			description = "Internal error occured while processing registration  id : " + registrationId;
		} finally {

			String eventId = "";
			String eventName = "";
			String eventType = "";
			eventId = isTransactionSuccessful ? EventId.RPR_402.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventName.UPDATE.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					registrationId, ApiName.AUDIT);

		}

		return object;
	}

}
