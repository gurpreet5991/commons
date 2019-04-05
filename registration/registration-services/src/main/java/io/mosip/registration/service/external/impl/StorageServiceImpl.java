package io.mosip.registration.service.external.impl;

import static io.mosip.kernel.core.util.DateUtils.formatDate;
import static io.mosip.registration.constants.LoggerConstants.LOG_PKT_STORAGE;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;
import static io.mosip.registration.constants.RegistrationConstants.ZIP_FILE_EXTENSION;
import static io.mosip.registration.exception.RegistrationExceptionConstants.REG_IO_EXCEPTION;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Date;

import org.springframework.stereotype.Service;

import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.FileUtils;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.entity.Registration;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.external.StorageService;

/**
 * Class to store the encrypted packet of the {@link Registration} in configured
 * location in local disk
 * 
 * @author Balaji Sridharan
 * @since 1.0.0
 *
 */
@Service
public class StorageServiceImpl implements StorageService {

	private static final Logger LOGGER = AppConfig.getLogger(StorageServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.external.StorageService#storeToDisk(java.lang.
	 * String, byte[])
	 */
	@Override
	public String storeToDisk(String registrationId, byte[] packet) throws RegBaseCheckedException {
		try {
			// Generate the file path for storing the Encrypted Packet
			String seperator="/";
			String filePath = String.valueOf(ApplicationContext.map().get(RegistrationConstants.PACKET_STORE_LOCATION)).concat(seperator).concat(formatDate(new Date(), String.valueOf(ApplicationContext.map().get(RegistrationConstants.PACKET_STORE_DATE_FORMAT))))
					.concat(seperator).concat(registrationId);
			// Storing the Encrypted Registration Packet as zip
			FileUtils.copyToFile(new ByteArrayInputStream(CryptoUtil.encodeBase64(packet).getBytes()), new File(filePath.concat(ZIP_FILE_EXTENSION)));

			LOGGER.info(LOG_PKT_STORAGE, APPLICATION_NAME, APPLICATION_ID, "Encrypted packet saved");

			return filePath;
		} catch (IOException ioException) {
			throw new RegBaseCheckedException(REG_IO_EXCEPTION.getErrorCode(), REG_IO_EXCEPTION.getErrorMessage());
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.ENCRYPTED_PACKET_STORAGE,
					runtimeException.toString());
		}
	}
}
