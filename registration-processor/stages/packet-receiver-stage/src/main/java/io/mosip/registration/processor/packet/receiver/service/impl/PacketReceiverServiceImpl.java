package io.mosip.registration.processor.packet.receiver.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.code.ApiName;
import io.mosip.registration.processor.core.code.EventId;
import io.mosip.registration.processor.core.code.EventName;
import io.mosip.registration.processor.core.code.EventType;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.spi.filesystem.manager.FileManager;
import io.mosip.registration.processor.packet.manager.dto.DirectoryPathDto;
import io.mosip.registration.processor.packet.receiver.exception.DuplicateUploadRequestException;
import io.mosip.registration.processor.packet.receiver.exception.FileSizeExceedException;
import io.mosip.registration.processor.packet.receiver.exception.PacketNotSyncException;
import io.mosip.registration.processor.packet.receiver.exception.PacketNotValidException;
import io.mosip.registration.processor.packet.receiver.service.PacketReceiverService;
import io.mosip.registration.processor.packet.receiver.util.StatusMessage;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.code.RegistrationExternalStatusCode;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusSubRequestDto;
import io.mosip.registration.processor.status.dto.SyncRegistrationDto;
import io.mosip.registration.processor.status.dto.SyncResponseDto;
import io.mosip.registration.processor.status.entity.SyncRegistrationEntity;
import io.mosip.registration.processor.status.service.RegistrationStatusService;
import io.mosip.registration.processor.status.service.SyncRegistrationService;
import io.mosip.registration.processor.status.utilities.RegistrationStatusMapUtil;

/**
 * The Class PacketReceiverServiceImpl.
 *
 */
@RefreshScope
@Component
public class PacketReceiverServiceImpl implements PacketReceiverService<File, MessageDTO> {

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(PacketReceiverServiceImpl.class);

	/** The Constant USER. */
	private static final String USER = "MOSIP_SYSTEM";

	/** The Constant LOG_FORMATTER. */
	public static final String LOG_FORMATTER = "{} - {}";

	private static final String RESEND = "RESEND";

	/** The file manager. */
	@Autowired
	private FileManager<DirectoryPathDto, InputStream> fileManager;

	/** The sync registration service. */
	@Autowired
	private SyncRegistrationService<SyncResponseDto, SyncRegistrationDto> syncRegistrationService;

	/** The registration status service. */
	@Autowired
	private RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	/** The core audit request builder. */
	@Autowired
	private AuditLogRequestBuilder auditLogRequestBuilder;

	/** The packet receiver stage. */

	@Value("${registration.processor.packet.ext}")
	private String extention;

	@Value("${registration.processor.max.file.size}")
	private String fileSize;

	@Autowired
	private RegistrationStatusMapUtil registrationStatusMapUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.id.issuance.packet.handler.service.PacketUploadService#storePacket(
	 * java.lang.Object)
	 */
	@Override
	public MessageDTO storePacket(File file) {

		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setInternalError(false);

		messageDTO.setIsValid(false);
		boolean storageFlag = false;

		if (file.getName() != null && file.exists()) {
			String fileOriginalName = file.getName();
			String registrationId = fileOriginalName.split("\\.")[0];
			regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationId, "PacketReceiverServiceImpl::storePacket()::entry");
			messageDTO.setRid(registrationId);
			String description = "";
			boolean isTransactionSuccessful = false;
			SyncRegistrationEntity regEntity = syncRegistrationService.findByRegistrationId(registrationId);
			if (regEntity == null) {
				description = "PacketNotSync exception in packet receiver for registartionId " + registrationId + "::"
						+ PlatformErrorMessages.RPR_PKR_PACKET_NOT_YET_SYNC.getMessage();
				regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
						PlatformErrorMessages.RPR_PKR_PACKET_NOT_YET_SYNC.getMessage());
				throw new PacketNotSyncException(PlatformErrorMessages.RPR_PKR_PACKET_NOT_YET_SYNC.getMessage());
			}

			if (file.length() > getMaxFileSize()) {
				regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
						PlatformErrorMessages.RPR_PKR_INVALID_PACKET_SIZE.getMessage());
				description = "FileSizeExceedException in packetReceiver for registartionId " + registrationId + "::"
						+ PlatformErrorMessages.RPR_PKR_INVALID_PACKET_SIZE.getMessage();
				throw new FileSizeExceedException(PlatformErrorMessages.RPR_PKR_INVALID_PACKET_SIZE.getMessage());
			}
			if (!(fileOriginalName.endsWith(getExtention()))) {
				regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
						PlatformErrorMessages.RPR_PKR_INVALID_PACKET_FORMAT.getMessage());
				description = "PacketNotValidException in packet receiver for registartionId " + registrationId + "::"
						+ PlatformErrorMessages.RPR_PKR_INVALID_PACKET_FORMAT.getMessage();
				throw new PacketNotValidException(PlatformErrorMessages.RPR_PKR_INVALID_PACKET_FORMAT.getMessage());
			} 
			else if (isDuplicatePacket(registrationId) && !isExternalStatusResend(registrationId)) {
				throw new DuplicateUploadRequestException(
						PlatformErrorMessages.RPR_PKR_DUPLICATE_PACKET_RECIEVED.getMessage());
			} 


			else   {
				try {
					fileManager.put(registrationId, new FileInputStream(file.getAbsolutePath()),
							DirectoryPathDto.VIRUS_SCAN_ENC);
					InternalRegistrationStatusDto dto;

					dto= registrationStatusService
							.getRegistrationStatus(registrationId);
					if(dto == null)
						dto = new InternalRegistrationStatusDto();
					else {
						int retryCount = dto.getRetryCount() != null? dto.getRetryCount() + 1: 1;
						dto.setRetryCount(retryCount);

					}
					dto.setRegistrationId(registrationId);
					dto.setRegistrationType(regEntity.getRegistrationType());
					dto.setReferenceRegistrationId(null);
					dto.setStatusCode(RegistrationStatusCode.PACKET_UPLOADED_TO_VIRUS_SCAN.toString());
					dto.setLangCode("eng");
					dto.setStatusComment(StatusMessage.PACKET_UPLOADED_VIRUS_SCAN);
					dto.setIsActive(true);
					dto.setCreatedBy(USER);
					dto.setIsDeleted(false);
					registrationStatusService.addRegistrationStatus(dto);
					storageFlag = true;
					isTransactionSuccessful = true;
					description = "Packet sucessfully synced for registrationId " + registrationId;
					regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(),
							LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
							"PacketReceiverServiceImpl::storePacket()::exit");
					regProcLogger.info(LoggerFileConstant.SESSIONID.toString(),
							LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
							"Registration Packet is successfully sync in Sync table.");
				} catch (DataAccessException | IOException e) {
					description = "DataAccessException | IOException in packet receiver for registrationId"
							+ registrationId + "::" + e.getMessage();
					regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
							LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
							"Error while updating status : " + e.getMessage());
				} finally {
					String eventId = "";
					String eventName = "";
					String eventType = "";
					eventId = isTransactionSuccessful ? EventId.RPR_407.toString() : EventId.RPR_405.toString();
					eventName = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventName.ADD.toString()
							: EventName.EXCEPTION.toString();
					eventType = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventType.BUSINESS.toString()
							: EventType.SYSTEM.toString();

					auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
							registrationId, ApiName.DMZAUDIT);
				}
			} 
		}
		if (storageFlag) {
			messageDTO.setIsValid(true);
		}
		return messageDTO;
	}

	/**
	 * check if file exists or not
	 *
	 * @param file
	 * @param fileOriginalName
	 * @return
	 */
	boolean fileExists(MultipartFile file, String fileOriginalName) {
		return file.getOriginalFilename() != null && !file.isEmpty() && fileOriginalName != null;
	}

	/**
	 * Gets the max file size.
	 *
	 * @return the max file size
	 */
	public long getMaxFileSize() {
		int maxFileSize = Integer.parseInt(fileSize);
		return maxFileSize * 1024L * 1024;
	}

	public String getExtention() {
		return extention;
	}

	/**
	 * Checks if registration id is already present in registration status table.
	 *
	 * @param registrationId
	 *            the registration id
	 * @return the boolean
	 */
	private Boolean isDuplicatePacket(String registrationId) {
		return registrationStatusService.getRegistrationStatus(registrationId) != null;
	}

	public Boolean isExternalStatusResend(String registrationId) {
		List<RegistrationStatusSubRequestDto> regIds=new ArrayList<RegistrationStatusSubRequestDto>();
		RegistrationStatusSubRequestDto registrationStatusSubRequestDto = new RegistrationStatusSubRequestDto();
		registrationStatusSubRequestDto.setRegistrationId(registrationId);
		regIds.add(registrationStatusSubRequestDto);
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				registrationId, "PacketReceiverServiceImpl::isExternalStatusResend()::entry");

		List<RegistrationStatusDto> registrationInternalStatusDto = registrationStatusService.getByIds(regIds);
		RegistrationExternalStatusCode registrationExternalStatusCode = registrationStatusMapUtil.getExternalStatus(registrationInternalStatusDto.get(0).getStatusCode(), registrationInternalStatusDto.get(0).getRetryCount());
		String mappedValue = registrationExternalStatusCode.toString();
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				registrationId, "PacketReceiverServiceImpl::isExternalStatusResend()::exit");
		return (mappedValue.equals(RESEND));
	}

}