package io.mosip.registration.processor.packet.upload.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.registration.processor.core.code.ApiName;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.spi.restclient.RegistrationProcessorRestClientService;
import io.mosip.registration.processor.packet.service.constants.RegistrationConstants;
import io.mosip.registration.processor.packet.service.dto.PacketGeneratorResDto;
import io.mosip.registration.processor.packet.service.dto.PacketReceiverResponseDTO;
import io.mosip.registration.processor.packet.service.dto.RegSyncResponseDTO;
import io.mosip.registration.processor.packet.service.dto.RegistrationSyncRequestDTO;
import io.mosip.registration.processor.packet.service.dto.SyncRegistrationDTO;
import io.mosip.registration.processor.packet.service.dto.SyncResponseDto;
import io.mosip.registration.processor.packet.service.exception.RegBaseCheckedException;
import io.mosip.registration.processor.packet.service.util.encryptor.EncryptorUtil;
import io.mosip.registration.processor.packet.upload.service.SyncUploadEncryptionService;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;

/**
 * The Class SyncUploadEncryptionServiceImpl.
 * 
 * @author Rishabh Keshari
 */
@Service
public class SyncUploadEncryptionServiceImpl implements SyncUploadEncryptionService {

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(SyncUploadEncryptionServiceImpl.class);

	/** The rest client service. */
	@Autowired
	private RegistrationProcessorRestClientService<Object> restClientService;

	/** The encryptor util. */
	@Autowired
	EncryptorUtil encryptorUtil;

	/** The gson. */
	Gson gson = new GsonBuilder().create();

	@Autowired
	private Environment env;

	private static final String REG_SYNC_SERVICE_ID = "mosip.registration.processor.registration.sync.id";
	private static final String REG_SYNC_APPLICATION_VERSION = "mosip.registration.processor.application.version";
	private static final String DATETIME_PATTERN = "mosip.registration.processor.datetime.pattern";
	private static final String SYNCSTATUSCOMMENT = "UIN Reactivation and Deactivation By External Resources";
	private static final String UPLOADSTATUSCOMMENT = "RECEIVED";

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.processor.packet.upload.service.
	 * SyncUploadEncryptionService#uploadUinPacket(java.io.File, java.lang.String,
	 * java.lang.String)
	 */
	public PacketGeneratorResDto uploadUinPacket(File decryptedUinZipFile, String registartionId, String creationTime)
			throws RegBaseCheckedException {
		PacketGeneratorResDto packerGeneratorResDto = new PacketGeneratorResDto();

		String syncStatus = "";
		String encryptedFilePath = "";
		InputStream decryptedFileStream = null;
		String uploadStatus = "";
		try {
			decryptedFileStream = new FileInputStream(decryptedUinZipFile);

			encryptedFilePath = encryptorUtil.encryptUinUpdatePacket(decryptedFileStream, registartionId, creationTime);

			RegSyncResponseDTO regSyncResponseDTO = packetSync(registartionId);
			if (regSyncResponseDTO != null) {

				List<SyncResponseDto> synList = regSyncResponseDTO.getResponse();
				if (synList != null) {
					SyncResponseDto syncResponseDto = synList.get(0);
					syncStatus = syncResponseDto.getStatus();
				}

			}
			if ("success".equalsIgnoreCase(syncStatus)) {

				PacketReceiverResponseDTO packetReceiverResponseDTO = null;
				File enryptedUinZipFile = new File(encryptedFilePath);
				LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
				map.add("file", new FileSystemResource(enryptedUinZipFile));
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.MULTIPART_FORM_DATA);
				HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
						map, headers);

				String result = null;
				result = (String) restClientService.postApi(ApiName.PACKETRECEIVER, "", "", requestEntity,
						String.class);
				if (result != null) {
					packetReceiverResponseDTO = gson.fromJson(result, PacketReceiverResponseDTO.class);
					uploadStatus = packetReceiverResponseDTO.getResponse().getStatus();
					packerGeneratorResDto.setRegistrationId(registartionId);
					if (uploadStatus
							.equalsIgnoreCase(RegistrationStatusCode.PACKET_UPLOADED_TO_VIRUS_SCAN.toString())) {
						packerGeneratorResDto.setStatus(UPLOADSTATUSCOMMENT);
					} else {
						packerGeneratorResDto.setStatus(uploadStatus);
					}
					packerGeneratorResDto.setMessage("Packet created and uploaded");
					return packerGeneratorResDto;
				}

			}

		} catch (FileNotFoundException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registartionId,
					PlatformErrorMessages.RPR_PGS_FILE_NOT_PRESENT.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new RegBaseCheckedException(PlatformErrorMessages.RPR_PGS_FILE_NOT_PRESENT, e);

		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registartionId, PlatformErrorMessages.RPR_PGS_INVALID_KEY_ILLEGAL_ARGUMENT.getMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new RegBaseCheckedException(PlatformErrorMessages.RPR_PGS_INVALID_KEY_ILLEGAL_ARGUMENT, e);
		} catch (IOException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registartionId,
					PlatformErrorMessages.RPR_SYS_IO_EXCEPTION.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new RegBaseCheckedException(PlatformErrorMessages.RPR_PGS_API_RESOURCE_NOT_AVAILABLE, e);
		} catch (ApisResourceAccessException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registartionId, PlatformErrorMessages.RPR_PGS_API_RESOURCE_NOT_AVAILABLE.getMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new RegBaseCheckedException(PlatformErrorMessages.RPR_PGS_API_RESOURCE_NOT_AVAILABLE, e);
		} catch (RegBaseCheckedException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registartionId,
					PlatformErrorMessages.RPR_PGS_REG_BASE_EXCEPTION.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new RegBaseCheckedException(PlatformErrorMessages.RPR_PGS_REG_BASE_EXCEPTION, e);
		} finally {

		}

		return packerGeneratorResDto;

	}

	/**
	 * Packet sync.
	 *
	 * @param regId
	 *            the reg id
	 * @return the reg sync response DTO
	 * @throws ApisResourceAccessException
	 */
	private RegSyncResponseDTO packetSync(String regId) throws ApisResourceAccessException {
		RegSyncResponseDTO regSyncResponseDTO = null;

		List<SyncRegistrationDTO> syncDtoList = new ArrayList<>();
		String response = null;
		RegistrationSyncRequestDTO registrationSyncRequestDTO = new RegistrationSyncRequestDTO();
		registrationSyncRequestDTO.setId(env.getProperty(REG_SYNC_SERVICE_ID));
		registrationSyncRequestDTO.setVersion(env.getProperty(REG_SYNC_APPLICATION_VERSION));
		registrationSyncRequestDTO
				.setRequesttime(DateUtils.getUTCCurrentDateTimeString(env.getProperty(DATETIME_PATTERN)));
		SyncRegistrationDTO syncDto = new SyncRegistrationDTO();
		syncDto.setLangCode("ENG");
		syncDto.setStatusComment(SYNCSTATUSCOMMENT);
		syncDto.setRegistrationId(regId);
		syncDto.setSyncStatus(RegistrationConstants.PACKET_STATUS_PRE_SYNC);
		syncDto.setSyncType(RegistrationConstants.PACKET_STATUS_SYNC_TYPE);
		syncDtoList.add(syncDto);
		registrationSyncRequestDTO.setRequest(syncDtoList);
		response = (String) restClientService.postApi(ApiName.SYNCSERVICE, "", "", registrationSyncRequestDTO,
				String.class);
		regSyncResponseDTO = gson.fromJson(response, RegSyncResponseDTO.class);

		return regSyncResponseDTO;
	}

}