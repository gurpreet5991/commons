package io.mosip.registration.device.iris;

import static io.mosip.registration.constants.LoggerConstants.LOG_REG_IRIS_FACADE;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.biometric.IrisDetailsDTO;
import io.mosip.registration.entity.UserBiometric;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.exception.RegistrationExceptionConstants;
import io.mosip.registration.mdm.service.impl.MosipBioDeviceManager;

/**
 * It takes a decision based on the input provider name and initialize the
 * respective implementation class and perform the required operation.
 * 
 * @author Balaji Sridharan
 * @since 1.0.0
 */
@Component
public class IrisFacade {

	private static final Logger LOGGER = AppConfig.getLogger(IrisFacade.class);


	@Autowired
	MosipBioDeviceManager mosipBioDeviceManager;
	
	/**
	 * Gets the iris stub image as DTO.
	 *
	 * @param irisDetailsDTO the iris details DTO
	 * @param irisType       the iris type
	 * @throws RegBaseCheckedException the reg base checked exception
	 */
	public void getIrisImageAsDTO(IrisDetailsDTO irisDetailsDTO, String irisType) throws RegBaseCheckedException {
		try {
			LOGGER.info(LOG_REG_IRIS_FACADE, APPLICATION_NAME, APPLICATION_ID,
					"Stubbing iris details for user registration");

			Map<String, Object> scannedIrisMap = getIrisScannedImage(irisType);
			double qualityScore = 0;
			if (!(boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
				qualityScore = (double) scannedIrisMap.get(RegistrationConstants.IMAGE_SCORE_KEY);
			}

			if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)
					|| Double.compare(irisDetailsDTO.getQualityScore(), qualityScore) < 0) {
				// Set the values in IrisDetailsDTO object
				irisDetailsDTO.setIris((byte[]) scannedIrisMap.get(RegistrationConstants.IMAGE_BYTE_ARRAY_KEY));
				irisDetailsDTO.setForceCaptured(false);
				irisDetailsDTO.setIrisImageName(irisType.concat(RegistrationConstants.DOT)
						.concat((String) scannedIrisMap.get(RegistrationConstants.IMAGE_FORMAT_KEY)));
				irisDetailsDTO.setIrisType(irisType);
				if (!(boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
					irisDetailsDTO.setQualityScore(qualityScore);
				}
				if (irisDetailsDTO.getNumOfIrisRetry() > 1) {
					irisDetailsDTO.setQualityScore(91.0);
				}
			}

			LOGGER.info(LOG_REG_IRIS_FACADE, APPLICATION_NAME, APPLICATION_ID,
					"Stubbing iris details for user registration completed");
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.USER_REG_IRIS_SCAN_EXP,
					String.format("Exception while stubbing the iris details for user registration: %s caused by %s",
							runtimeException.getMessage(), runtimeException.getCause()));
		}
	}

	private Map<String, Object> getIrisScannedImage(String irisType) throws RegBaseCheckedException {
		try {
			LOGGER.info(LOG_REG_IRIS_FACADE, APPLICATION_NAME, APPLICATION_ID,
					"Scanning of iris details for user registration");

			BufferedImage bufferedImage = ImageIO.read(this.getClass().getResourceAsStream("/images/scanned-iris.png"));

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, RegistrationConstants.IMAGE_FORMAT, byteArrayOutputStream);

			byte[] scannedIrisBytes = byteArrayOutputStream.toByteArray();

			double qualityScore;
			if (irisType.equalsIgnoreCase("LeftEye")) {
				qualityScore = 90.5;
			} else {
				qualityScore = 50.0;
			}

			// Add image format, image and quality score in bytes array to map
			Map<String, Object> scannedIris = new WeakHashMap<>();
			scannedIris.put(RegistrationConstants.IMAGE_FORMAT_KEY, "png");
			scannedIris.put(RegistrationConstants.IMAGE_BYTE_ARRAY_KEY, scannedIrisBytes);
			if (!(boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
				scannedIris.put(RegistrationConstants.IMAGE_SCORE_KEY, qualityScore);
			}

			LOGGER.info(LOG_REG_IRIS_FACADE, APPLICATION_NAME, APPLICATION_ID,
					"Scanning of iris details for user registration completed");

			return scannedIris;
		} catch (IOException ioException) {
			throw new RegBaseCheckedException(RegistrationExceptionConstants.REG_IRIS_SCANNING_ERROR.getErrorCode(),
					RegistrationExceptionConstants.REG_IRIS_SCANNING_ERROR.getErrorMessage());
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.USER_REG_IRIS_STUB_IMAGE_EXP,
					String.format("Exception while scanning iris details for user registration: %s caused by %s",
							runtimeException.getMessage(), runtimeException.getCause()));
		}
	}

	/**
	 * Capture Iris
	 * 
	 * @return byte[] of captured Iris
	 */
	public byte[] captureIris() {

		LOGGER.info(LOG_REG_IRIS_FACADE, APPLICATION_NAME, APPLICATION_ID, "Stub data for Iris");
		
		byte[] capturedByte=null;
		
		try {
			if(RegistrationConstants.ENABLE.equalsIgnoreCase(((String)ApplicationContext.getInstance().map().get(RegistrationConstants.MDM_ENABLED))))
				capturedByte= mosipBioDeviceManager.scan("IRIS_SINGLE").get("IRIS_SINGLE");
			else
				capturedByte=RegistrationConstants.IRIS_STUB.getBytes();
		} catch (RegBaseCheckedException | RuntimeException exception) {
			exception.printStackTrace();
		}
		return capturedByte;
	}

	/**
	 * Validate Iris
	 * 
	 * @param irisDetailsDTO  the {@link IrisDetailsDTO} to be validated
	 * @param userIrisDetails the list of {@link IrisDetailsDTO} available in
	 *                        database
	 * 
	 * @return the validation result. <code>true</code> if match is found, else
	 *         <code>false</code>
	 */
	public boolean validateIris(IrisDetailsDTO irisDetailsDTO, List<UserBiometric> userIrisDetails) {

		LOGGER.info(LOG_REG_IRIS_FACADE, APPLICATION_NAME, APPLICATION_ID,
				"Validating iris details for user registration");

		userIrisDetails.forEach(
				irisEach -> irisDetailsDTO.setIrisType(irisEach.getUserBiometricId().getBioAttributeCode() + ".jpg"));
		return userIrisDetails.stream()
				.anyMatch(iris -> Arrays.equals(irisDetailsDTO.getIris(), iris.getBioIsoImage()));
	}

}
