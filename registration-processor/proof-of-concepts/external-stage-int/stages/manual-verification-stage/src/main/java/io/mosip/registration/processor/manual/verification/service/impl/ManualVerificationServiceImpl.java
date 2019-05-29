package io.mosip.registration.processor.manual.verification.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.code.EventId;
import io.mosip.registration.processor.core.code.EventName;
import io.mosip.registration.processor.core.code.EventType;
import io.mosip.registration.processor.core.exception.util.PacketStructure;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.packet.dto.PacketMetaInfo;
import io.mosip.registration.processor.core.spi.filesystem.adapter.FileSystemAdapter;
import io.mosip.registration.processor.core.util.JsonUtil;
import io.mosip.registration.processor.filesystem.ceph.adapter.impl.utils.PacketFiles;
import io.mosip.registration.processor.manual.verification.dto.ManualVerificationDTO;
import io.mosip.registration.processor.manual.verification.dto.ManualVerificationStatus;
import io.mosip.registration.processor.manual.verification.dto.UserDto;
import io.mosip.registration.processor.manual.verification.exception.InvalidFileNameException;
import io.mosip.registration.processor.manual.verification.exception.InvalidUpdateException;
import io.mosip.registration.processor.manual.verification.exception.NoRecordAssignedException;
import io.mosip.registration.processor.manual.verification.service.ManualVerificationService;
import io.mosip.registration.processor.manual.verification.stage.ManualVerificationStage;
import io.mosip.registration.processor.manual.verification.util.StatusMessage;
import io.mosip.registration.processor.packet.storage.entity.ManualVerificationEntity;
import io.mosip.registration.processor.packet.storage.repository.BasePacketRepository;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.exception.TablenotAccessibleException;
import io.mosip.registration.processor.status.service.RegistrationStatusService;

/**
 * The Class ManualVerificationServiceImpl.
 */
@Component
public class ManualVerificationServiceImpl implements ManualVerificationService {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(ManualVerificationServiceImpl.class);
	/** The Constant USER. */
	private static final String USER = "MOSIP_SYSTEM";
	/** The audit log request builder. */

	@Autowired
	private AuditLogRequestBuilder auditLogRequestBuilder;

	/** The registration status service. */
	@Autowired
	private RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	/** The filesystem ceph adapter impl. */
	@Autowired
	private FileSystemAdapter<InputStream, Boolean> filesystemCephAdapterImpl;

	/** The base packet repository. */
	@Autowired
	private BasePacketRepository<ManualVerificationEntity, String> basePacketRepository;

	/** The manual verification stage. */
	@Autowired
	private ManualVerificationStage manualVerificationStage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.processor.manual.adjudication.service.
	 * ManualAdjudicationService#assignStatus(io.mosip.registration.processor.manual
	 * .adjudication.dto.UserDto)
	 */
	@Override
	public ManualVerificationDTO assignApplicant(UserDto dto) {
		ManualVerificationDTO manualVerificationDTO = new ManualVerificationDTO();
		List<ManualVerificationEntity> entities;
		entities = basePacketRepository.getAssignedApplicantDetails(dto.getUserId(),
				ManualVerificationStatus.ASSIGNED.name());
		ManualVerificationEntity manualVerificationEntity;
		if (!entities.isEmpty()) {
			manualVerificationEntity = entities.get(0);
			manualVerificationDTO.setRegId(manualVerificationEntity.getId().getRegId());
			manualVerificationDTO.setMatchedRefId(manualVerificationEntity.getId().getMatchedRefId());
			manualVerificationDTO.setMatchedRefType(manualVerificationEntity.getId().getMatchedRefType());
			manualVerificationDTO.setMvUsrId(manualVerificationEntity.getMvUsrId());
			manualVerificationDTO.setStatusCode(manualVerificationEntity.getStatusCode());
			manualVerificationDTO.setReasonCode(manualVerificationEntity.getReasonCode());
		} else {
			entities = basePacketRepository.getFirstApplicantDetails(ManualVerificationStatus.PENDING.name());
			if (entities.isEmpty()) {
				throw new NoRecordAssignedException(PlatformErrorMessages.RPR_MVS_NO_ASSIGNED_RECORD.getCode(),
						PlatformErrorMessages.RPR_MVS_NO_ASSIGNED_RECORD.getMessage());
			} else {
				manualVerificationEntity = entities.get(0);
				manualVerificationEntity.setStatusCode(ManualVerificationStatus.ASSIGNED.name());
				manualVerificationEntity.setMvUsrId(dto.getUserId());
				ManualVerificationEntity updatedManualVerificationEntity = basePacketRepository
						.update(manualVerificationEntity);
				if (updatedManualVerificationEntity != null) {
					manualVerificationDTO.setRegId(updatedManualVerificationEntity.getId().getRegId());
					manualVerificationDTO.setMatchedRefId(updatedManualVerificationEntity.getId().getMatchedRefId());
					manualVerificationDTO
							.setMatchedRefType(updatedManualVerificationEntity.getId().getMatchedRefType());
					manualVerificationDTO.setMvUsrId(updatedManualVerificationEntity.getMvUsrId());
					manualVerificationDTO.setStatusCode(updatedManualVerificationEntity.getStatusCode());
				}
			}

		}
		return manualVerificationDTO;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.processor.manual.adjudication.service.
	 * ManualAdjudicationService#getApplicantFile(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public byte[] getApplicantFile(String regId, String fileName) {
		byte[] file = null;
		InputStream fileInStream = null;

		if (checkBiometric(fileName)) {
			fileInStream = getApplicantBiometricFile(regId, fileName);
		} else if (checkDemographic(fileName)) {
			fileInStream = getApplicantDemographicFile(regId, fileName);
		} else {
			throw new InvalidFileNameException(PlatformErrorMessages.RPR_MVS_INVALID_FILE_REQUEST.getCode(),
					PlatformErrorMessages.RPR_MVS_INVALID_FILE_REQUEST.getMessage());
		}
		try {
			file = IOUtils.toByteArray(fileInStream);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
		return file;
	}

	/**
	 * Gets the applicant biometric file.
	 *
	 * @param regId
	 *            the reg id
	 * @param fileName
	 *            the file name
	 * @return the applicant biometric file
	 */
	private InputStream getApplicantBiometricFile(String regId, String fileName) {
		return filesystemCephAdapterImpl.getFile(regId, PacketStructure.BIOMETRIC + fileName);
	}

	/**
	 * Gets the applicant demographic file.
	 *
	 * @param regId
	 *            the reg id
	 * @param fileName
	 *            the file name
	 * @return the applicant demographic file
	 */
	private InputStream getApplicantDemographicFile(String regId, String fileName) {
		return filesystemCephAdapterImpl.getFile(regId, PacketStructure.APPLICANTDEMOGRAPHIC + fileName);
	}

	/**
	 * Check biometric.
	 *
	 * @param fileName
	 *            the file name
	 * @return true, if successful
	 */
	private boolean checkBiometric(String fileName) {

		return fileName.equals(PacketFiles.APPLICANTPHOTO.name()) || fileName.equals(PacketFiles.PROOFOFADDRESS.name())
				|| fileName.equals(PacketFiles.PROOFOFIDENTITY.name())
				|| fileName.equals(PacketFiles.EXCEPTIONPHOTO.name()) || fileName.equals(PacketFiles.ID.name());
	}

	/**
	 * Check demographic.
	 *
	 * @param fileName
	 *            the file name
	 * @return true, if successful
	 */
	private boolean checkDemographic(String fileName) {

		return fileName.equals(PacketFiles.RIGHTPALM.name()) || fileName.equals(PacketFiles.LEFTPALM.name())
				|| fileName.equals(PacketFiles.BOTHTHUMBS.name()) || fileName.equals(PacketFiles.LEFTEYE.name())
				|| fileName.equals(PacketFiles.RIGHTEYE.name());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.processor.manual.adjudication.service.
	 * ManualAdjudicationService#updatePacketStatus(io.mosip.registration.processor.
	 * manual.adjudication.dto.ManualVerificationDTO)
	 */
	@Override
	public ManualVerificationDTO updatePacketStatus(ManualVerificationDTO manualVerificationDTO) {
		String registrationId = manualVerificationDTO.getRegId();
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setInternalError(false);
		messageDTO.setIsValid(false);
		messageDTO.setRid(manualVerificationDTO.getRegId());

		String description = "";
		boolean isTransactionSuccessful = false;
		ManualVerificationEntity manualVerificationEntity;
		if (!manualVerificationDTO.getStatusCode().equalsIgnoreCase(ManualVerificationStatus.REJECTED.name())
				&& !manualVerificationDTO.getStatusCode().equalsIgnoreCase(ManualVerificationStatus.APPROVED.name())) {
			throw new InvalidUpdateException(PlatformErrorMessages.RPR_MVS_INVALID_STATUS_UPDATE.getCode(),
					PlatformErrorMessages.RPR_MVS_INVALID_STATUS_UPDATE.getMessage());
		}
		List<ManualVerificationEntity> entities = basePacketRepository.getSingleAssignedRecord(
				manualVerificationDTO.getRegId(), manualVerificationDTO.getMatchedRefId(),
				manualVerificationDTO.getMvUsrId(), ManualVerificationStatus.ASSIGNED.name());
		if (entities.isEmpty()) {
			throw new NoRecordAssignedException(PlatformErrorMessages.RPR_MVS_NO_ASSIGNED_RECORD.getCode(),
					PlatformErrorMessages.RPR_MVS_NO_ASSIGNED_RECORD.getMessage());
		} else {
			manualVerificationEntity = entities.get(0);
			manualVerificationEntity.setStatusCode(manualVerificationDTO.getStatusCode());
			manualVerificationEntity.setReasonCode(manualVerificationDTO.getReasonCode());
		}
		try {
			InternalRegistrationStatusDto registrationStatusDto = registrationStatusService
					.getRegistrationStatus(registrationId);
			if (manualVerificationDTO.getStatusCode().equalsIgnoreCase(ManualVerificationStatus.APPROVED.name())) {
				messageDTO.setIsValid(true);
				manualVerificationStage.sendMessage(messageDTO);
				registrationStatusDto.setStatusComment(StatusMessage.MANUAL_VERFICATION_PACKET_APPROVED);
				registrationStatusDto.setStatusCode(RegistrationStatusCode.MANUAL_ADJUDICATION_SUCCESS.toString());
				isTransactionSuccessful = true;
				description = "Manual verification approved for registration id : " + registrationId;
			} else {
				registrationStatusDto.setStatusCode(RegistrationStatusCode.MANUAL_ADJUDICATION_FAILED.toString());
				registrationStatusDto.setStatusComment(StatusMessage.MANUAL_VERFICATION_PACKET_REJECTED);
				description = "Manual verification rejected for registration id : " + registrationId;
			}
			ManualVerificationEntity maVerificationEntity = basePacketRepository.update(manualVerificationEntity);
			manualVerificationDTO.setStatusCode(maVerificationEntity.getStatusCode());
			registrationStatusDto.setUpdatedBy(USER);
			registrationStatusService.updateRegistrationStatus(registrationStatusDto);
		} catch (TablenotAccessibleException e) {
			logger.error(e.getMessage());
		}

		finally {
			String eventId = "";
			String eventName = "";
			String eventType = "";
			eventId = isTransactionSuccessful ? EventId.RPR_402.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventName.UPDATE.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					registrationId);
		}
		return manualVerificationDTO;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.processor.manual.verification.service.
	 * ManualVerificationService#getApplicantPacketInfo(java.lang.String)
	 */
	@Override
	public PacketMetaInfo getApplicantPacketInfo(String regId) {
		PacketMetaInfo packetMetaInfo = new PacketMetaInfo();
		InputStream fileInStream = filesystemCephAdapterImpl.getFile(regId, PacketStructure.PACKETMETAINFO);
		try {
			packetMetaInfo = (PacketMetaInfo) JsonUtil.inputStreamtoJavaObject(fileInStream, PacketMetaInfo.class);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getLocalizedMessage());
		}
		if (packetMetaInfo != null) {
			packetMetaInfo.getIdentity().setMetaData(null);
			packetMetaInfo.getIdentity().setHashSequence(null);
			packetMetaInfo.getIdentity().setCheckSum(null);
			packetMetaInfo.getIdentity().setOsiData(null);
		}

		return packetMetaInfo;
	}

}
