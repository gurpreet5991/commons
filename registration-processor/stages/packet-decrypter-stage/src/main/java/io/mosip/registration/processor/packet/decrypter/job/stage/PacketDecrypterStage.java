package io.mosip.registration.processor.packet.decrypter.job.stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleManager;
import io.mosip.registration.processor.core.code.EventId;
import io.mosip.registration.processor.core.code.EventName;
import io.mosip.registration.processor.core.code.EventType;
import io.mosip.registration.processor.core.spi.filesystem.adapter.FileSystemAdapter;
import io.mosip.registration.processor.packet.archiver.util.PacketArchiver;
import io.mosip.registration.processor.packet.archiver.util.exception.PacketNotFoundException;
import io.mosip.registration.processor.packet.archiver.util.exception.UnableToAccessPathException;
import io.mosip.registration.processor.packet.decrypter.job.Decryptor;
import io.mosip.registration.processor.packet.decryptor.job.exception.PacketDecryptionFailureException;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.exception.TablenotAccessibleException;
import io.mosip.registration.processor.status.service.RegistrationStatusService;

@Component
public class PacketDecrypterStage extends MosipVerticleManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(PacketDecrypterStage.class);

	private static final String USER = "MOSIP_SYSTEM";

	private static final String LOGDISPLAY = "{} - {} - {}";

	// @Value("${landingzone.scanner.stage.time.interval}")
	private long secs = 30;

	MosipEventBus mosipEventBus = null;

	@Autowired
	RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	@Autowired
	private FileSystemAdapter<InputStream, Boolean> adapter;

	@Autowired
	private Decryptor decryptor;

	@Autowired
	AuditLogRequestBuilder auditLogRequestBuilder;

	@Autowired
	private PacketArchiver packetArchiver;

	private static final String DFS_NOT_ACCESSIBLE = "The DFS Path set by the System is not accessible";

	private static final String REGISTRATION_STATUS_TABLE_NOT_ACCESSIBLE = "The Registration Status table "
			+ "is not accessible";

	private String description = "";
	private boolean isTransactionSuccessful = false;
	private String registrationId = "";

	@Override
	public MessageDTO process(MessageDTO object) {
		List<InternalRegistrationStatusDto> dtolist = null;

		try {
			dtolist = registrationStatusService
					.getByStatus(RegistrationStatusCode.PACKET_UPLOADED_TO_FILESYSTEM.toString());
			if (!(dtolist.isEmpty())) {
				dtolist.forEach(dto -> {
					this.registrationId = dto.getRegistrationId();
					try {
						decryptpacket(dto);

					} catch (TablenotAccessibleException e) {

						LOGGER.error(LOGDISPLAY, REGISTRATION_STATUS_TABLE_NOT_ACCESSIBLE, e.getMessage(), e);

						this.isTransactionSuccessful = false;
						this.description = "Registration status table is not accessible for packet "
								+ this.registrationId;
					} catch (PacketDecryptionFailureException e) {

						LOGGER.error(LOGDISPLAY, e.getErrorCode(), e.getErrorText(), e);

						dto.setStatusCode(RegistrationStatusCode.PACKET_DECRYPTION_FAILED.toString());
						dto.setStatusComment("packet is in status packet for decryption failed");
						dto.setUpdatedBy(USER);
						registrationStatusService.updateRegistrationStatus(dto);
						this.isTransactionSuccessful = false;
						this.description = "Packet decryption failed for packet " + this.registrationId;
					} catch (IOException e) {

						LOGGER.error(LOGDISPLAY, DFS_NOT_ACCESSIBLE, e.getMessage(), e);
						this.isTransactionSuccessful = false;
						this.description = "File System is not accessible for packet " + this.registrationId;
					} finally {

						String eventId = "";
						String eventName = "";
						String eventType = "";
						eventId = this.isTransactionSuccessful ? EventId.RPR_402.toString()
								: EventId.RPR_405.toString();
						eventName = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventName.UPDATE.toString()
								: EventName.EXCEPTION.toString();
						eventType = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventType.BUSINESS.toString()
								: EventType.SYSTEM.toString();

						auditLogRequestBuilder.createAuditRequestBuilder(this.description, eventId, eventName,
								eventType, this.registrationId);

					}
				});
			} else if (dtolist.isEmpty()) {

				LOGGER.info("There are currently no files to be decrypted");
			}
		} catch (TablenotAccessibleException e) {

			LOGGER.error(LOGDISPLAY, REGISTRATION_STATUS_TABLE_NOT_ACCESSIBLE, e);
		}

		return null;
	}

	/**
	 * method for decrypting registration packet
	 *
	 * @param dto
	 *            RegistrationStatus of the packet to be decrypted
	 * @throws IOException
	 * @throws PacketDecryptionFailureException
	 */
	private void decryptpacket(InternalRegistrationStatusDto dto) throws IOException, PacketDecryptionFailureException {
		try {
			packetArchiver.archivePacket(dto.getRegistrationId());
		} catch (UnableToAccessPathException e) {
			LOGGER.error(LOGDISPLAY, e.getErrorCode(), e.getMessage(), e.getCause());
		} catch (PacketNotFoundException ex) {
			LOGGER.error(LOGDISPLAY, ex.getErrorCode(), ex.getMessage(), ex.getCause());
		}

		InputStream encryptedPacket = adapter.getPacket(dto.getRegistrationId());
		InputStream decryptedData = decryptor.decrypt(encryptedPacket, dto.getRegistrationId());

		// if (decryptedData != null) {

		encryptedPacket.close();

		adapter.storePacket(dto.getRegistrationId(), decryptedData);

		adapter.unpackPacket(dto.getRegistrationId());

		dto.setStatusCode(RegistrationStatusCode.PACKET_DECRYPTION_SUCCESSFUL.toString());
		dto.setStatusComment("packet is in status packet for decryption successful");
		dto.setUpdatedBy(USER);
		registrationStatusService.updateRegistrationStatus(dto);

		MessageDTO messageDTO = new MessageDTO();

		messageDTO.setRid(dto.getRegistrationId());

		LOGGER.info(LOGDISPLAY, dto.getRegistrationId(),
				" Packet decrypted and extracted encrypted files stored in DFS.");

		MessageDTO message = new MessageDTO();
		message.setRid(dto.getRegistrationId());

		sendMessage(mosipEventBus, message);
		this.description = "packet decryption was successful for packet" + this.registrationId;
		this.isTransactionSuccessful = true;
		// else {
		// encryptedPacket.close();
		//
		// dto.setStatusCode(RegistrationStatusCode.PACKET_DECRYPTION_FAILED.toString());
		// dto.setStatusComment("packet is in status packet for decryption failed");
		// dto.setUpdatedBy(USER);
		// registrationStatusService.updateRegistrationStatus(dto);
		// this.description="packet decryption failed for packet"+this.registrationId;
		// this.isTransactionSuccessful=false;
		// LOGGER.info(LOGDISPLAY, dto.getRegistrationId(), " Packet is null and could
		// not be decrypted ");
		// }

	}

	public void deployVerticle() {
		mosipEventBus = this.getEventBus(this.getClass());
		mosipEventBus.getEventbus().setPeriodic(secs * 1000, msg ->
		// sendMessage(mosipEventBus, new MessageDTO())
		process(new MessageDTO()));
	}

	private void sendMessage(MosipEventBus mosipEventBus, MessageDTO message) {
		this.send(mosipEventBus, MessageBusAddress.BATCH_BUS, message);
	}

}
