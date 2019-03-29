
package io.mosip.registration.processor.biodedupe.stage;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.fsadapter.exception.FSAdapterException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.bio.dedupe.exception.ABISAbortException;
import io.mosip.registration.processor.bio.dedupe.exception.ABISInternalError;
import io.mosip.registration.processor.bio.dedupe.exception.UnableToServeRequestABISException;
import io.mosip.registration.processor.bio.dedupe.exception.UnexceptedError;
import io.mosip.registration.processor.biodedupe.stage.utils.StatusMessage;
import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;

import io.mosip.registration.processor.core.code.ApiName;

import io.mosip.registration.processor.core.code.DedupeSourceName;

import io.mosip.registration.processor.core.code.EventId;
import io.mosip.registration.processor.core.code.EventName;
import io.mosip.registration.processor.core.code.EventType;
import io.mosip.registration.processor.core.code.ModuleName;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.constant.ResponseStatusCode;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.exception.util.PlatformSuccessMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.packet.dto.Identity;
import io.mosip.registration.processor.core.spi.biodedupe.BioDedupeService;
import io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager;
import io.mosip.registration.processor.packet.storage.dto.ApplicantInfoDto;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.service.RegistrationStatusService;

/**
 * @author Sowmya
 *
 */
/*
 * @Transactional removed temporarily since the refid is not getting saved
 * immediately in abisref table. TODO : need to fix this.
 */
@Service
public class BioDedupeProcessor {

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(BioDedupeProcessor.class);

	/** The Constant USER. */
	private static final String USER = "MOSIP_SYSTEM";

	private static final String BIO = "BIO";

	/** The registration status service. */
	@Autowired
	private RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	/** The core audit request builder. */
	@Autowired
	private AuditLogRequestBuilder auditLogRequestBuilder;

	/** The bio dedupe service. */
	@Autowired
	private BioDedupeService bioDedupeService;

	/** The packet info manager. */
	@Autowired
	private PacketInfoManager<Identity, ApplicantInfoDto> packetInfoManager;

	String description = "";

	private String code = "";

	public MessageDTO process(MessageDTO object) {
		object.setMessageBusAddress(MessageBusAddress.BIO_DEDUPE_BUS_IN);
		object.setInternalError(Boolean.FALSE);

		boolean isTransactionSuccessful = false;

		String registrationId = object.getRid();

		InternalRegistrationStatusDto registrationStatusDto = registrationStatusService
				.getRegistrationStatus(registrationId);
		try {
			String insertionResult = bioDedupeService.insertBiometrics(registrationId);
			if (insertionResult.equalsIgnoreCase(ResponseStatusCode.SUCCESS.name())) {
				List<String> matchedRegIds = bioDedupeService.performDedupe(registrationId);
				checkBiometricPotentialMatch(matchedRegIds, registrationStatusDto, registrationId, object);
			} else {
				object.setIsValid(Boolean.FALSE);
				code = PlatformErrorMessages.RPR_BIO_BIOMETRIC_INSERTION_TO_ABIS.getCode();
				description = registrationStatusDto.getStatusComment() + registrationId;
				registrationStatusDto
						.setStatusComment(PlatformErrorMessages.RPR_BIO_BIOMETRIC_INSERTION_TO_ABIS.name());
				registrationStatusDto.setStatusCode(RegistrationStatusCode.PACKET_BIO_DEDUPE_FAILED.toString());

			}
			registrationStatusDto.setUpdatedBy(USER);
			registrationStatusService.updateRegistrationStatus(registrationStatusDto);
			isTransactionSuccessful = true;

			regProcLogger.info(LoggerFileConstant.SESSIONID.toString(),
					code + " - " + LoggerFileConstant.REGISTRATIONID.toString(), registrationId, description);
		} catch (ABISInternalError e) {
			code = PlatformErrorMessages.RPR_ABIS_INTERNAL_ERROR.getCode();
			description = PlatformErrorMessages.RPR_ABIS_INTERNAL_ERROR.getMessage();
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
					code + " -- " + LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
					description + "\n" + ExceptionUtils.getStackTrace(e));
			object.setInternalError(Boolean.TRUE);
		} catch (ABISAbortException e) {
			code = PlatformErrorMessages.RPR_ABIS_INTERNAL_ERROR.getCode();
			description = PlatformErrorMessages.RPR_ABIS_INTERNAL_ERROR.getMessage();
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
					code + " -- " + LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
					description + "\n" + ExceptionUtils.getStackTrace(e));
			object.setInternalError(Boolean.TRUE);

		} catch (UnexceptedError e) {
			code = PlatformErrorMessages.PACKET_BIO_DEDUPE_FAILED.getCode();
			description = PlatformErrorMessages.PACKET_BIO_DEDUPE_FAILED.getMessage();
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
					code + " -- " + LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
					description + "\n" + ExceptionUtils.getStackTrace(e));
			object.setInternalError(Boolean.TRUE);

		} catch (UnableToServeRequestABISException e) {
			code = PlatformErrorMessages.PACKET_BIO_DEDUPE_FAILED.getCode();
			description = PlatformErrorMessages.PACKET_BIO_DEDUPE_FAILED.getMessage() + " -- " + registrationId;
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
					code + " -- " + LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
					description + "\n" + ExceptionUtils.getStackTrace(e));
			object.setInternalError(Boolean.TRUE);

		} catch (FSAdapterException e) {
			code = PlatformErrorMessages.PACKET_BDD_PACKET_STORE_NOT_ACCESSIBLE.getCode();
			description = PlatformErrorMessages.PACKET_BDD_PACKET_STORE_NOT_ACCESSIBLE.getMessage();
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
					code + " -- " + LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
					description + "\n" + ExceptionUtils.getStackTrace(e));
			object.setInternalError(Boolean.TRUE);

		} catch (DataAccessException e) {
			code = PlatformErrorMessages.PACKET_BIO_DEDUPE_FAILED.getCode();
			description = PlatformErrorMessages.PACKET_BIO_DEDUPE_FAILED.getMessage();
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
					code + " -- " + LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
					description + "\n" + ExceptionUtils.getStackTrace(e));
			object.setInternalError(Boolean.TRUE);
		} catch (ApisResourceAccessException e) {
			code = PlatformErrorMessages.PACKET_BIO_DEDUPE_FAILED.getCode();
			description = PlatformErrorMessages.PACKET_BIO_DEDUPE_FAILED.getMessage();
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
					code + " -- " + LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
					description + "\n" + ExceptionUtils.getStackTrace(e));
			object.setInternalError(Boolean.TRUE);

		} catch (Exception ex) {
			code = PlatformErrorMessages.PACKET_BIO_DEDUPE_FAILED.getCode();
			description = PlatformErrorMessages.PACKET_BIO_DEDUPE_FAILED.getMessage();
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
					code + " -- " + LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
					description + "\n" + ExceptionUtils.getStackTrace(ex));
			object.setInternalError(Boolean.TRUE);
		} finally {
			String eventId = isTransactionSuccessful ? EventId.RPR_402.toString() : EventId.RPR_405.toString();
			String eventName = isTransactionSuccessful ? EventName.UPDATE.toString() : EventName.EXCEPTION.toString();
			String eventType = isTransactionSuccessful ? EventType.BUSINESS.toString() : EventType.SYSTEM.toString();

			String moduleId = isTransactionSuccessful ? PlatformSuccessMessages.RPR_BIO_DEDUPE_SUCCESS.getCode() : code;
			String moduleName =  ModuleName.BIO_DEDUPE.name();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType, moduleId,moduleName, registrationId);
		}
		return object;
	}

	private void checkBiometricPotentialMatch(List<String> matchedRegIds,
			InternalRegistrationStatusDto registrationStatusDto, String registrationId, MessageDTO object) {
		if (matchedRegIds != null && !matchedRegIds.isEmpty()) {
			object.setIsValid(Boolean.FALSE);
			registrationStatusDto.setStatusComment(StatusMessage.PACKET_BIOMETRIC_POTENTIAL_MATCH);
			registrationStatusDto.setStatusCode(RegistrationStatusCode.PACKET_BIO_POTENTIAL_MATCH.toString());
			code= PlatformSuccessMessages.RPR_BIO_METRIC_POTENTIAL_MATCH.getCode();
			description =PlatformSuccessMessages.RPR_BIO_METRIC_POTENTIAL_MATCH.getMessage();
            packetInfoManager.saveManualAdjudicationData(matchedRegIds, registrationId, DedupeSourceName.BIO);
		} else {
			object.setIsValid(Boolean.TRUE);
			registrationStatusDto.setStatusComment(StatusMessage.PACKET_BIODEDUPE_SUCCESS);
			registrationStatusDto.setStatusCode(RegistrationStatusCode.PACKET_BIO_DEDUPE_SUCCESS.toString());
			code= PlatformSuccessMessages.RPR_BIO_DEDUPE_SUCCESS.getCode();
			description =PlatformSuccessMessages.RPR_BIO_DEDUPE_SUCCESS.getMessage();
		}
	}

}
