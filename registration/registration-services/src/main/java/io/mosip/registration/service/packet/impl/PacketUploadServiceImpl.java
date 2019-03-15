package io.mosip.registration.service.packet.impl;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.File;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationClientStatusCode;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dao.RegistrationDAO;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.PacketStatusDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.entity.Registration;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.exception.RegistrationExceptionConstants;
import io.mosip.registration.service.packet.PacketUploadService;
import io.mosip.registration.util.restclient.ServiceDelegateUtil;

/**
 * 
 * This class will update the packet status in the table and also push the
 * packets to the server.
 * 
 * @author SaravanaKumar G
 *
 */
@Service
@Transactional
public class PacketUploadServiceImpl implements PacketUploadService {

	/** The registration DAO. */
	@Autowired
	private RegistrationDAO registrationDAO;

	/** The service delegate util. */
	@Autowired
	private ServiceDelegateUtil serviceDelegateUtil;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = AppConfig.getLogger(PacketUploadServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.service.PacketUploadService#getSynchedPackets()
	 */
	public List<Registration> getSynchedPackets() {
		LOGGER.info("REGISTRATION - GET_SYNCHED_PACKETS - PACKET_UPLOAD_SERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Fetching synched packets from the database");
		return registrationDAO.getRegistrationByStatus(RegistrationConstants.PACKET_UPLOAD_STATUS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.PacketUploadService#pushPacket(java.io.File)
	 */
	@SuppressWarnings("unchecked")
	public ResponseDTO pushPacket(File packet) throws URISyntaxException, RegBaseCheckedException {
		
		LOGGER.info("REGISTRATION - PUSH_PACKET - PACKET_UPLOAD_SERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Push packets to the server");

		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add(RegistrationConstants.PACKET_TYPE, new FileSystemResource(packet));
		ResponseDTO responseDTO = new ResponseDTO();
		List<ErrorResponseDTO> erResponseDTOs = new ArrayList<>();
		try {
			LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) serviceDelegateUtil
					.post(RegistrationConstants.PACKET_UPLOAD, map, RegistrationConstants.JOB_TRIGGER_POINT_USER);
			if (response.get(RegistrationConstants.PACKET_STATUS_READER_RESPONSE) != null
					&& response.get(RegistrationConstants.ERRORS) == null) {
				SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
				successResponseDTO.setCode(RegistrationConstants.SUCCESS);
				responseDTO.setSuccessResponseDTO(successResponseDTO);
			} else if (response.get(RegistrationConstants.ERRORS) != null) {
				ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
				errorResponseDTO.setCode(RegistrationConstants.ERROR);
				errorResponseDTO
						.setMessage(((List<LinkedHashMap<String, String>>) response.get(RegistrationConstants.ERRORS))
								.get(0).get(RegistrationConstants.PACKET_STATUS_READER_RESPONSE));
				erResponseDTOs.add(errorResponseDTO);
				responseDTO.setErrorResponseDTOs(erResponseDTOs);
			}
		} catch (HttpClientErrorException clientException) {
			LOGGER.error("REGISTRATION - PUSH_PACKET_CLIENT_ERROR - PACKET_UPLOAD_SERVICE", APPLICATION_NAME,
					APPLICATION_ID,
					clientException.getRawStatusCode() + "Http error while pushing packets to the server"
							+ ExceptionUtils.getStackTrace(clientException));
			throw new RegBaseCheckedException(Integer.toString(clientException.getRawStatusCode()),
					clientException.getStatusText());
		} catch (HttpServerErrorException serverException) {
			LOGGER.error("REGISTRATION - PUSH_PACKET_SERVER_ERROR - PACKET_UPLOAD_SERVICE", APPLICATION_NAME,
					APPLICATION_ID,
					serverException.getRawStatusCode() + "Http server error while pushing packets to the server"
							+ ExceptionUtils.getStackTrace(serverException));
			throw new RegBaseCheckedException(Integer.toString(serverException.getRawStatusCode()),
					serverException.getResponseBodyAsString());

		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - PUSH_PACKET_CONNECTION_ERROR - PACKET_UPLOAD_SERVICE", APPLICATION_NAME,
					APPLICATION_ID, runtimeException.getMessage() + "Runtime error while pushing packets to the server"
							+ ExceptionUtils.getStackTrace(runtimeException));
			throw new RegBaseUncheckedException(RegistrationExceptionConstants.REG_PACKET_UPLOAD_ERROR.getErrorCode(),
					RegistrationExceptionConstants.REG_PACKET_UPLOAD_ERROR.getErrorMessage());
		} catch (SocketTimeoutException socketTimeoutException) {
			LOGGER.error("REGISTRATION - PUSH_PACKETS_TO_SERVER_SOCKET_ERROR - PACKET_UPLOAD_SERVICE", APPLICATION_NAME,
					APPLICATION_ID, socketTimeoutException.getMessage() + "Error in sync packets to the server");
			throw new RegBaseCheckedException(
					(socketTimeoutException.getMessage() + ExceptionUtils.getStackTrace(socketTimeoutException)),
					socketTimeoutException.getLocalizedMessage());
		}
		return responseDTO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.PacketUploadService#updateStatus(java.util.
	 * List)
	 */
	public Boolean updateStatus(List<PacketStatusDTO> packetsUploadStatus) {
		LOGGER.info("REGISTRATION - UPDATE_STATUS - PACKET_UPLOAD_SERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Update the status of the uploaded packet");
		for (PacketStatusDTO registrationPacket : packetsUploadStatus) {
			registrationDAO.updateRegStatus(registrationPacket);
		}
		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.packet.PacketUploadService#uploadPacket(java.
	 * lang.String)
	 */
	@Override
	public void uploadPacket(String rid) {
		Registration syncedPacket = registrationDAO
				.getRegistrationById(RegistrationClientStatusCode.META_INFO_SYN_SERVER.getCode(), rid);
		List<Registration> packetList = new ArrayList<>();
		packetList.add(syncedPacket);

		uploadSyncedPacket(packetList);
	}

	/**
	 * Upload synced packets.
	 *
	 * @param syncedPackets
	 *            the synced packets
	 */
	private void uploadSyncedPacket(List<Registration> syncedPackets) {

		List<Registration> packetUploadList = new ArrayList<>();

		if (!syncedPackets.isEmpty()) {
			for (Registration syncedPacket : syncedPackets) {
				if (syncedPacket != null) {
					syncedPacket.setUploadCount((short) (syncedPacket.getUploadCount() + 1));
					String ackFileName = syncedPacket.getAckFilename();
					int lastIndex = ackFileName.indexOf(RegistrationConstants.ACKNOWLEDGEMENT_FILE);
					String packetPath = ackFileName.substring(0, lastIndex);
					File packet = new File(packetPath + RegistrationConstants.ZIP_FILE_EXTENSION);
					try {
						if (packet.exists()) {
							ResponseDTO response = pushPacket(packet);

							if (response.getSuccessResponseDTO() != null) {
								syncedPacket.setClientStatusCode(
										RegistrationClientStatusCode.UPLOADED_SUCCESSFULLY.getCode());
								syncedPacket.setFileUploadStatus(
										RegistrationClientStatusCode.UPLOAD_SUCCESS_STATUS.getCode());
								packetUploadList.add(syncedPacket);

							} else if (response.getErrorResponseDTOs() != null) {
								String errMessage = response.getErrorResponseDTOs().get(0).getMessage();
								if (errMessage.contains(RegistrationConstants.PACKET_DUPLICATE)) {

									syncedPacket.setClientStatusCode(
											RegistrationClientStatusCode.UPLOADED_SUCCESSFULLY.getCode());
									syncedPacket.setFileUploadStatus(
											RegistrationClientStatusCode.UPLOAD_SUCCESS_STATUS.getCode());
									packetUploadList.add(syncedPacket);

								}
							} else {
								syncedPacket.setFileUploadStatus(
										RegistrationClientStatusCode.UPLOAD_ERROR_STATUS.getCode());
								packetUploadList.add(syncedPacket);
							}
						}
					} catch (RegBaseCheckedException | URISyntaxException exception) {
						LOGGER.error("REGISTRATION - HANDLE_PACKET_UPLOAD_ERROR - PACKET_UPLOAD_SERVICE",
								APPLICATION_NAME, APPLICATION_ID, "Error while pushing packets to the server"
										+ exception.getMessage() + ExceptionUtils.getStackTrace(exception));
						syncedPacket.setFileUploadStatus(RegistrationClientStatusCode.UPLOAD_ERROR_STATUS.getCode());
						packetUploadList.add(syncedPacket);
					} catch (RuntimeException runtimeException) {
						LOGGER.error("REGISTRATION - HANDLE_PACKET_UPLOAD_RUNTIME_ERROR - PACKET_UPLOAD_SERVICE",
								APPLICATION_NAME, APPLICATION_ID,
								"Run time error while connecting to the server" + runtimeException.getMessage()
										+ ExceptionUtils.getStackTrace(runtimeException));

						syncedPacket.setFileUploadStatus(RegistrationClientStatusCode.UPLOAD_ERROR_STATUS.getCode());
						packetUploadList.add(syncedPacket);
					}
				}
			}
		}
		// updateStatus(packetUploadList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.packet.PacketUploadService#uploadEODPackets(
	 * java.util.List)
	 */
	@Override
	public void uploadEODPackets(List<String> regIds) {
		List<Registration> registrations = registrationDAO.get(regIds);
		uploadSyncedPacket(registrations);
	}

	@Override
	public void uploadAllSyncedPackets() {

		List<Registration> synchedPackets = getSynchedPackets();
		uploadSyncedPacket(synchedPackets);

	}
}
