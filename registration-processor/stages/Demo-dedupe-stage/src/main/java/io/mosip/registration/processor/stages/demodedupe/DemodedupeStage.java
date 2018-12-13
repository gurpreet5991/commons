package io.mosip.registration.processor.stages.demodedupe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleManager;
import io.mosip.registration.processor.core.code.EventId;
import io.mosip.registration.processor.core.code.EventName;
import io.mosip.registration.processor.core.code.EventType;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.packet.storage.entity.ManualVerificationEntity;
import io.mosip.registration.processor.packet.storage.entity.ManualVerificationPKEntity;
import io.mosip.registration.processor.packet.storage.repository.BasePacketRepository;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.service.RegistrationStatusService;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * The Class DemodedupeStage
 *
 * @author M1048358 Alok Ranjan
 * 
 */

@RefreshScope
@Service
public class DemodedupeStage extends MosipVerticleManager {

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DemodedupeStage.class);

	/** The Constant USER. */
	private static final String USER = "MOSIP_SYSTEM";

	/** The registration status service. */
	@Autowired
	private RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	@Autowired
	private BasePacketRepository<ManualVerificationEntity, String> manualVerficationRepository;

	@Value("${registration.processor.vertx.cluster.address}")
	private String clusterAddress;

	@Value("${registration.processor.vertx.localhost}")
	private String localhost;

	/** The core audit request builder. */
	@Autowired
	private AuditLogRequestBuilder auditLogRequestBuilder;

	@Autowired
	private DemoDedupe demoDedupe;

	private static final String MATCHED_REFERENCE_TYPE = "uin";

	/**
	 * Deploy verticle.
	 */
	public void deployVerticle() {
		MosipEventBus mosipEventBus = this.getEventBus(this.getClass(), clusterAddress, localhost);
		this.consumeAndSend(mosipEventBus, MessageBusAddress.DEMODEDUPE_BUS_IN, MessageBusAddress.DEMODEDUPE_BUS_OUT);
	}

	@Override
	public MessageDTO process(MessageDTO object) {

		object.setMessageBusAddress(MessageBusAddress.DEMODEDUPE_BUS_IN);
		object.setInternalError(Boolean.FALSE);
		String description = "";
		boolean isTransactionSuccessful = false;

		String registrationId = object.getRid();

		try {
			InternalRegistrationStatusDto registrationStatusDto = registrationStatusService
					.getRegistrationStatus(registrationId);

			// Potential Duplicate Ids after performing demo dedupe
			Set<String> duplicateDtos = demoDedupe.performDedupe(registrationId);
			List<String> duplicateUINList = new ArrayList<>(duplicateDtos);

			if (!duplicateDtos.isEmpty()) {

				// authenticating duplicateIds with provided packet biometrics
				boolean isDuplicateAfterAuth = demoDedupe.authenticateDuplicates(registrationId, duplicateUINList);

				if (isDuplicateAfterAuth) {
					
					int retryCount = registrationStatusDto.getRetryCount() != null
							? registrationStatusDto.getRetryCount() + 1
							: 1;
					description = registrationStatusDto.getStatusComment() + registrationId;
					registrationStatusDto.setRetryCount(retryCount);
					
					registrationStatusDto.setStatusComment(StatusMessage.PACKET_DEMO_DEDUPE_FAILED);
					registrationStatusDto.setStatusCode(RegistrationStatusCode.PACKET_DEMO_DEDUPE_FAILED.toString());
					description = "Packet Demo dedupe failed for registration id : " + registrationId;

				} else {
					object.setIsValid(Boolean.FALSE);
					registrationStatusDto.setStatusComment(StatusMessage.PACKET_DEMO_POTENTIAL_MATCH);
					registrationStatusDto.setStatusCode(RegistrationStatusCode.PACKET_DEMO_POTENTIAL_MATCH.toString());
					description = "Potential duplicate packet found for registration id : " + registrationId;
					
					// Saving potential duplicates in reg_manual_verification table
					saveManualAdjudicationData(duplicateUINList, registrationId);
				}

			} else {
				object.setIsValid(Boolean.TRUE);
				registrationStatusDto.setStatusComment(StatusMessage.PACKET_DEMO_DEDUPE_SUCCESSFUL);
				registrationStatusDto.setStatusCode(RegistrationStatusCode.PACKET_DEMO_DEDUPE_SUCCESSFUL.toString());
				description = "Packet Demo dedupe successful for registration id : " + registrationId;
			}

			registrationStatusDto.setUpdatedBy(USER);
			registrationStatusService.updateRegistrationStatus(registrationStatusDto);
			isTransactionSuccessful = true;

		} catch (IOException | ApisResourceAccessException e) {
			log.error(PlatformErrorMessages.PACKET_DEMO_DEDUPE_FAILED.getMessage(), e);
			object.setInternalError(Boolean.TRUE);
			description = "Internal error occured while processing registration  id : " + registrationId;
		} catch (Exception ex) {
			log.error(PlatformErrorMessages.PACKET_DEMO_DEDUPE_FAILED.getMessage(), ex);
			object.setInternalError(Boolean.TRUE);
			description = "Internal error occured while processing registration  id : " + registrationId;
		} finally {

			String eventId = isTransactionSuccessful ? EventId.RPR_402.toString() : EventId.RPR_405.toString();
			String eventName = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventName.UPDATE.toString()
					: EventName.EXCEPTION.toString();
			String eventType = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					registrationId);

		}

		return object;
	}

	private void saveManualAdjudicationData(List<String> duplicateUINList, String registrationId) {
		boolean isTransactionSuccessful = false;
		String description = "";
		try {
			for (String duplicateUin : duplicateUINList) {
				ManualVerificationEntity manualVerificationEntity = new ManualVerificationEntity();
				ManualVerificationPKEntity manualVerificationPKEntity = new ManualVerificationPKEntity();
				manualVerificationPKEntity.setMatchedRefId(duplicateUin);
				manualVerificationPKEntity.setMatchedRefType(MATCHED_REFERENCE_TYPE);
				manualVerificationPKEntity.setRegId(registrationId);

				manualVerificationEntity.setId(manualVerificationPKEntity);
				manualVerificationEntity.setLangCode(null);
				manualVerificationEntity.setMatchedScore(null);
				manualVerificationEntity.setMvUsrId(null);
				manualVerificationEntity.setReasonCode("Potential Match");
				manualVerificationEntity.setStatusCode("PENDING");
				manualVerificationEntity.setStatusComment("Assigned to manual Adjudication");
				manualVerificationEntity.setIsActive(true);
				manualVerificationEntity.setIsDeleted(false);

				manualVerficationRepository.save(manualVerificationEntity);
				isTransactionSuccessful = true;
				description = "Packet Demo dedupe successful for registration id : " + registrationId;

			}

		} finally {

			String eventId = isTransactionSuccessful ? EventId.RPR_402.toString() : EventId.RPR_405.toString();
			String eventName = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventName.UPDATE.toString()
					: EventName.EXCEPTION.toString();
			String eventType = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					registrationId);

		}
	}

}