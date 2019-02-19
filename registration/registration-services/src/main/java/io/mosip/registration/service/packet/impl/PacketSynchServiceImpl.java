package io.mosip.registration.service.packet.impl;

import static io.mosip.kernel.core.util.JsonUtils.javaObjectToJsonString;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationClientStatusCode;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dao.RegistrationDAO;
import io.mosip.registration.dto.SyncRegistrationDTO;
import io.mosip.registration.entity.Registration;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.exception.RegistrationExceptionConstants;
import io.mosip.registration.service.sync.PacketSynchService;
import io.mosip.registration.util.restclient.ServiceDelegateUtil;

@Service
public class PacketSynchServiceImpl implements PacketSynchService {

	@Autowired
	private RegistrationDAO syncRegistrationDAO;

	@Autowired
	private ServiceDelegateUtil serviceDelegateUtil;

	@Value("${PACKET_SYNC_URL}")
	private String syncUrlPath;

	private static final Logger LOGGER = AppConfig.getLogger(PacketSynchServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.impl.PacketSynchStatus#fetchPacketsToBeSynched(
	 * )
	 */

	@Override
	public List<Registration> fetchPacketsToBeSynched() {
		LOGGER.info("REGISTRATION - FETCH_PACKETS_TO_BE_SYNCHED - PACKET_SYNC_SERVICE", APPLICATION_NAME,
				APPLICATION_ID, "Fetch the packets that needs to be synched to the server");
		return syncRegistrationDAO.getPacketsToBeSynched(RegistrationConstants.PACKET_STATUS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.impl.PacketSynchStatus#syncPacketsToServer(java
	 * .util.List)
	 */

	@Override
	public Object syncPacketsToServer(List<SyncRegistrationDTO> syncDtoList)
			throws RegBaseCheckedException, URISyntaxException, JsonProcessingException {
		LOGGER.info("REGISTRATION - SYNCH_PACKETS_TO_SERVER - PACKET_SYNC_SERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Sync the packets to the server");

		Object response = null;
		try {

			response = serviceDelegateUtil.post(RegistrationConstants.PACKET_SYNC, javaObjectToJsonString(syncDtoList));
		} catch (HttpClientErrorException e) {
			LOGGER.error("REGISTRATION - SYNCH_PACKETS_TO_SERVER_CLIENT_ERROR - PACKET_SYNC_SERVICE", APPLICATION_NAME,
					APPLICATION_ID, e.getRawStatusCode() + "Error in sync packets to the server");
			throw new RegBaseCheckedException(Integer.toString(e.getRawStatusCode()), e.getStatusText());
		} catch (RuntimeException e) {
			LOGGER.error("REGISTRATION - SYNCH_PACKETS_TO_SERVER_RUNTIME - PACKET_SYNC_SERVICE", APPLICATION_NAME,
					APPLICATION_ID, e.getMessage() + "Error in sync and push packets to the server");
			throw new RegBaseUncheckedException(RegistrationExceptionConstants.REG_PACKET_SYNC_EXCEPTION.getErrorCode(),
					RegistrationExceptionConstants.REG_PACKET_SYNC_EXCEPTION.getErrorMessage());
		} catch (SocketTimeoutException e) {
			LOGGER.error("REGISTRATION - SYNCH_PACKETS_TO_SERVER_SOCKET_ERROR - PACKET_SYNC_SERVICE", APPLICATION_NAME,
					APPLICATION_ID, e.getMessage() + "Error in sync packets to the server");
			throw new RegBaseCheckedException((e.getMessage()), e.getLocalizedMessage());
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.impl.PacketSynchStatus#updateSyncStatus(java.
	 * util.List)
	 */

	@Override
	public Boolean updateSyncStatus(List<Registration> synchedPackets) {
		LOGGER.info("REGISTRATION -UPDATE_SYNC_STATUS - PACKET_SYNC_SERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Updating the status of the synched packets to the database");
		for (Registration syncPacket : synchedPackets) {
			syncRegistrationDAO.updatePacketSyncStatus(syncPacket);
		}
		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.sync.PacketSynchService#getPacketToSync(java.
	 * lang.String)
	 */
	@Override
	public String packetSync(String rId) throws RegBaseCheckedException {
		LOGGER.debug("REGISTRATION -UPDATE_SYNC_STATUS - PACKET_SYNC_SERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Updating the status of the synched packets to the database");

		Registration registration = syncRegistrationDAO
				.getRegistrationById(RegistrationClientStatusCode.APPROVED.getCode(), rId);
		List<Registration> registrations = new ArrayList<>();
		registrations.add(registration);
		return syncPackets(registrations);
	}


	/**
	 * Sync packets.
	 *
	 * @param registrations the registrations
	 * @return the string
	 * @throws RegBaseCheckedException the reg base checked exception
	 */
	private String syncPackets(List<Registration> registrations) throws RegBaseCheckedException {
		String syncErrorStatus = "";
		try {

			List<SyncRegistrationDTO> syncRegistrationDTOs = new ArrayList<>();

			for (Registration registration : registrations) {
				SyncRegistrationDTO syncRegistrationDTO = new SyncRegistrationDTO();
				syncRegistrationDTO.setLangCode("ENG");
				syncRegistrationDTO.setRegistrationId(registration.getId());
				syncRegistrationDTO.setSyncStatus(RegistrationConstants.PACKET_STATUS_PRE_SYNC);
				syncRegistrationDTO.setSyncType(RegistrationConstants.PACKET_STATUS_SYNC_TYPE);
				syncRegistrationDTOs.add(syncRegistrationDTO);

				registration.setClientStatusCode(RegistrationClientStatusCode.META_INFO_SYN_SERVER.getCode());

			}

			Object response = syncPacketsToServer(syncRegistrationDTOs);

			if (response != null) {

				updateSyncStatus(registrations);

			}

		} catch (RegBaseCheckedException | JsonProcessingException | URISyntaxException exception) {
			LOGGER.error("REGISTRATION -UPDATE_SYNC_STATUS - PACKET_SYNC_SERVICE", APPLICATION_NAME, APPLICATION_ID,
					exception.getMessage());

			syncErrorStatus = exception.getMessage();

		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION -UPDATE_SYNC_STATUS - PACKET_SYNC_SERVICE", APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage());

			throw new RegBaseUncheckedException(RegistrationExceptionConstants.REG_PACKET_SYNC_EXCEPTION.getErrorCode(),
					RegistrationExceptionConstants.REG_PACKET_SYNC_EXCEPTION.getErrorMessage(), runtimeException);
		}
		return syncErrorStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.sync.PacketSynchService#syncEODPackets(java.
	 * util.List)
	 */
	@Override
	public String syncEODPackets(List<String> regIds) throws RegBaseCheckedException {
		List<Registration> registrations = syncRegistrationDAO.get(regIds);
		return syncPackets(registrations);
	}
}
