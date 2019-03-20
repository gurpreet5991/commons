package io.mosip.registration.controller.device;

import static io.mosip.registration.constants.LoggerConstants.LOG_REG_IRIS_CAPTURE_CONTROLLER;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.AuditReferenceIdTypes;
import io.mosip.registration.constants.Components;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.reg.BiometricExceptionController;
import io.mosip.registration.controller.reg.RegistrationController;
import io.mosip.registration.controller.reg.UserOnboardParentController;
import io.mosip.registration.device.iris.IrisFacade;
import io.mosip.registration.dto.biometric.BiometricDTO;
import io.mosip.registration.dto.biometric.BiometricExceptionDTO;
import io.mosip.registration.dto.biometric.IrisDetailsDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * This is the {@link Controller} class for capturing the Iris image
 * 
 * @author Balaji Sridharan
 * @since 1.0.0
 */
@Controller
public class IrisCaptureController extends BaseController {

	private static final Logger LOGGER = AppConfig.getLogger(IrisCaptureController.class);

	@FXML
	private ImageView leftIrisImage;
	@FXML
	private Label rightIrisThreshold;
	@FXML
	private Label leftIrisQualityScore;
	@FXML
	private AnchorPane rightIrisPane;
	@FXML
	private AnchorPane leftIrisPane;
	@FXML
	private Label leftIrisThreshold;
	@FXML
	private Label rightIrisQualityScore;
	@FXML
	private ImageView rightIrisImage;
	@FXML
	private Button scanIris;

	@Autowired
	private RegistrationController registrationController;
	@Autowired
	private ScanPopUpViewController scanPopUpViewController;
	@Autowired
	private IrisFacade irisFacade;
	@Autowired
	private BiometricExceptionController biometricExceptionController;

	@Autowired
	private UserOnboardParentController userOnboardParentController;

	@FXML
	private Label registrationNavlabel;

	private Pane selectedIris;

	/**
	 * This method is invoked when IrisCapture FXML page is loaded. This method
	 * initializes the Iris Capture page.
	 */
	@FXML
	public void initialize() {

		try {
			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Initializing Iris Capture page for user registration");
			if (getRegistrationDTOFromSession() != null
					&& getRegistrationDTOFromSession().getSelectionListDTO() != null) {
				registrationNavlabel.setText(RegistrationConstants.UIN_NAV_LABEL);
			}
			// Set Threshold
			String irisThreshold = getValueFromApplicationMap(RegistrationConstants.IRIS_THRESHOLD);
			leftIrisThreshold.setText(irisThreshold.concat(RegistrationConstants.PERCENTAGE));
			rightIrisThreshold.setText(irisThreshold.concat(RegistrationConstants.PERCENTAGE));

			// Disable Scan button
			scanIris.setDisable(true);

			// Display the Captured Iris
			if (getBiometricDTOFromSession() != null || getRegistrationDTOFromSession() != null) {
				displayCapturedIris();
			}

			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Initializing Iris Capture page for user registration completed");
		} catch (RuntimeException runtimeException) {

			throw new RegBaseUncheckedException(RegistrationConstants.USER_REG_IRIS_CAPTURE_PAGE_LOAD_EXP,
					String.format("Exception while initializing Iris Capture page for user registration  %s",
							ExceptionUtils.getStackTrace(runtimeException)));
		}

	}

	private void displayCapturedIris() {
		for (IrisDetailsDTO capturedIris : getIrises()) {
			if (capturedIris.getIrisType().contains(RegistrationConstants.LEFT)) {
				leftIrisImage.setImage(convertBytesToImage(capturedIris.getIris()));
				leftIrisQualityScore.setText(getQualityScoreAsString(capturedIris.getQualityScore()));
			} else if (capturedIris.getIrisType().contains(RegistrationConstants.RIGHT)) {
				rightIrisImage.setImage(convertBytesToImage(capturedIris.getIris()));
				rightIrisQualityScore.setText(getQualityScoreAsString(capturedIris.getQualityScore()));
			}
		}
	}

	/**
	 * This event handler will be invoked when left iris or right iris {@link Pane}
	 * is clicked.
	 * 
	 * @param mouseEvent
	 *            the triggered {@link MouseEvent} object
	 */
	@FXML
	private void enableScan(MouseEvent mouseEvent) {
		try {
			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Enabling scan button for user registration");

			Pane sourcePane = (Pane) mouseEvent.getSource();
			sourcePane.requestFocus();
			selectedIris = sourcePane;
			scanIris.setDisable(true);
			// Get the Iris from RegistrationDTO based on selected Iris Pane
			IrisDetailsDTO irisDetailsDTO = getIrisBySelectedPane().findFirst().orElse(null);

			boolean isExceptionIris = getIrisExceptions().stream()
					.anyMatch(exceptionIris -> StringUtils.containsIgnoreCase(exceptionIris.getMissingBiometric(),
							(StringUtils.containsIgnoreCase(selectedIris.getId(), RegistrationConstants.LEFT)
									? RegistrationConstants.LEFT
									: RegistrationConstants.RIGHT).concat(RegistrationConstants.EYE)));

			// Enable the scan button, if any of the following satisfies
			// 1. If Iris was not scanned
			// 2. Quality score of the scanned image is less than threshold and number of
			// retries is less than configured
			// 3. If iris is not forced captured
			// 4. If iris is an exception iris
			if (!isExceptionIris
					&& (irisDetailsDTO == null
							|| (Double.compare(irisDetailsDTO.getQualityScore(),
									Double.parseDouble(
											getValueFromApplicationMap(RegistrationConstants.IRIS_THRESHOLD))) < 0
									&& irisDetailsDTO.getNumOfIrisRetry() < Integer.parseInt(
											getValueFromApplicationMap(RegistrationConstants.IRIS_RETRY_COUNT)))
							|| irisDetailsDTO.isForceCaptured())) {
				scanIris.setDisable(false);
			}

			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Enabling scan button for user registration completed");
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format("%s -> Exception while enabling scan button for user registration  %s %s",
							RegistrationConstants.USER_REG_IRIS_CAPTURE_POPUP_LOAD_EXP, runtimeException.getMessage(),
							ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_IRIS_SCAN_POPUP);
		}
	}

	/**
	 * This method displays the Biometric Scan pop-up window. This method will be
	 * invoked when Scan button is clicked.
	 */
	@FXML
	private void scan() {
		try {
			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Opening pop-up screen to capture Iris for user registration");

			IrisDetailsDTO irisDetailsDTO = getIrisBySelectedPane().findFirst().orElse(null);

			if ((irisDetailsDTO == null || (irisDetailsDTO.getNumOfIrisRetry() < Integer
					.parseInt(getValueFromApplicationMap(RegistrationConstants.IRIS_RETRY_COUNT))))
					|| (irisDetailsDTO == null
							&& ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)))) {
				auditFactory.audit(AuditEvent.REG_BIO_IRIS_SCAN, Components.REG_BIOMETRICS, SessionContext.userId(),
						AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

				scanPopUpViewController.init(this, RegistrationUIConstants.IRIS_SCAN);
			} else {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.IRIS_SCAN_RETRIES_EXCEEDED);
			}

			// Disable the scan button
			scanIris.setDisable(true);

			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Opening pop-up screen to capture Iris for user registration completed");
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format(
							"%s -> Exception while Opening pop-up screen to capture Iris for user registration  %s",
							RegistrationConstants.USER_REG_IRIS_CAPTURE_POPUP_LOAD_EXP,
							ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_IRIS_SCAN_POPUP);
		}
	}

	@Override
	public void scan(Stage popupStage) {
		try {
			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Scanning of iris details for user registration");

			Optional<IrisDetailsDTO> captiredIrisDetailsDTO = getIrisBySelectedPane().findFirst();

			IrisDetailsDTO irisDetailsDTO = null;
			if (!captiredIrisDetailsDTO.isPresent()) {
				irisDetailsDTO = new IrisDetailsDTO();
				getIrises().add(irisDetailsDTO);
			} else {
				irisDetailsDTO = captiredIrisDetailsDTO.get();
				if (!(boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
					irisDetailsDTO.setNumOfIrisRetry(irisDetailsDTO.getNumOfIrisRetry() + 1);
				}
			}

			String irisType = StringUtils.containsIgnoreCase(selectedIris.getId(), RegistrationConstants.LEFT)
					? RegistrationConstants.LEFT
					: RegistrationConstants.RIGHT;

			irisFacade.getIrisImageAsDTO(irisDetailsDTO, irisType.concat(RegistrationConstants.EYE));

			// Display the Scanned Iris Image in the Scan pop-up screen
			scanPopUpViewController.getScanImage().setImage(convertBytesToImage(irisDetailsDTO.getIris()));

			generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.IRIS_SUCCESS_MSG);

			if (irisType.equals(RegistrationConstants.LEFT)) {
				leftIrisImage.setImage(convertBytesToImage(irisDetailsDTO.getIris()));
				leftIrisPane.getStyleClass().add("IrisPanesSelected");
				leftIrisQualityScore.setText(getQualityScoreAsString(irisDetailsDTO.getQualityScore()));
			} else {
				rightIrisImage.setImage(convertBytesToImage(irisDetailsDTO.getIris()));
				rightIrisPane.getStyleClass().add("IrisPanesSelected");
				rightIrisQualityScore.setText(getQualityScoreAsString(irisDetailsDTO.getQualityScore()));
			}

			popupStage.close();

			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Scanning of iris details for user registration completed");
		} catch (RegBaseCheckedException regBaseCheckedException) {
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.IRIS_SCANNING_ERROR);
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID, String.format(
					"%s Exception while getting the scanned iris details for user registration: %s caused by %s",
					RegistrationConstants.USER_REG_IRIS_SAVE_EXP, runtimeException.getMessage(),
					ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.IRIS_SCANNING_ERROR);
		} finally {
			selectedIris.requestFocus();
		}
	}

	private boolean validateIrisLocalDedup() {
		// TODO: Implement Local Dedup for Iris
		return true;
	}

	/**
	 * This method will be invoked when Next button is clicked. The next section
	 * will be displayed.
	 */
	@FXML
	private void nextSection() {
		try {
			auditFactory.audit(AuditEvent.REG_BIO_IRIS_NEXT, Components.REG_BIOMETRICS, SessionContext.userId(),
					AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Navigating to Photo capture page for user registration");
			if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
				if (validateIris()) {
					userOnboardParentController.showCurrentPage(RegistrationConstants.IRIS_CAPTURE,
							getOnboardPageDetails(RegistrationConstants.IRIS_CAPTURE, RegistrationConstants.NEXT));
				}
			} else {
				if (validateIris() && validateIrisLocalDedup()) {
					if (getRegistrationDTOFromSession().getSelectionListDTO() != null) {

						SessionContext.getInstance().getMapObject().put("irisCapture", false);

						if (!RegistrationConstants.DISABLE.equalsIgnoreCase(String
								.valueOf(ApplicationContext.map().get(RegistrationConstants.FACE_DISABLE_FLAG)))) {
							SessionContext.getInstance().getMapObject().put("faceCapture", true);
						} else {
							SessionContext.getInstance().getMapObject().put("registrationPreview", true);
							registrationPreviewController.setUpPreviewContent();
						}
						registrationController.showUINUpdateCurrentPage();
					} else {
						registrationController.showCurrentPage(RegistrationConstants.IRIS_CAPTURE,
								getPageDetails(RegistrationConstants.IRIS_CAPTURE, RegistrationConstants.NEXT));
					}
				}
			}

			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Navigating to Photo capture page for user registration completed");
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format("%s -> Exception while navigating to Photo capture page for user registration  %s",
							RegistrationConstants.USER_REG_IRIS_CAPTURE_NEXT_SECTION_LOAD_EXP,
							ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.IRIS_NAVIGATE_NEXT_SECTION_ERROR);
		}
	}

	/**
	 * This method will be invoked when Previous button is clicked. The previous
	 * section will be displayed.
	 */
	@FXML
	private void previousSection() {
		try {
			auditFactory.audit(AuditEvent.REG_BIO_IRIS_BACK, Components.REG_BIOMETRICS, SessionContext.userId(),
					AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Navigating to Fingerprint capture page for user registration");

			if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
				if (validateIris()) {
					userOnboardParentController.showCurrentPage(RegistrationConstants.IRIS_CAPTURE,
							getOnboardPageDetails(RegistrationConstants.IRIS_CAPTURE, RegistrationConstants.PREVIOUS));
				}
			} else {
				if (getRegistrationDTOFromSession().getSelectionListDTO() != null) {
					if (validateIris() && validateIrisLocalDedup()) {

						long fingerPrintCount = getRegistrationDTOFromSession().getBiometricDTO()
								.getApplicantBiometricDTO().getBiometricExceptionDTO().stream()
								.filter(bio -> bio.getBiometricType().equals("fingerprint")).count();

						SessionContext.map().put("irisCapture", false);

						if (getRegistrationDTOFromSession().getSelectionListDTO().isBiometricFingerprint()
								|| fingerPrintCount > 0) {
							SessionContext.map().put("fingerPrintCapture", true);
						} else if (getRegistrationDTOFromSession().getSelectionListDTO().isBiometricException()
								&& fingerPrintCount == 0) {
							biometricExceptionController.setExceptionImage();
							SessionContext.map().put("biometricException", true);
						} else if (!RegistrationConstants.DISABLE.equalsIgnoreCase(
								String.valueOf(ApplicationContext.map().get(RegistrationConstants.DOC_DISABLE_FLAG)))) {
							SessionContext.map().put("documentScan", true);
						} else {
							SessionContext.map().put("demographicDetail", true);
						}
						registrationController.showUINUpdateCurrentPage();
					}
				} else {
					if (validateIris() && validateIrisLocalDedup()) {
						registrationController.showCurrentPage(RegistrationConstants.IRIS_CAPTURE,
								getPageDetails(RegistrationConstants.IRIS_CAPTURE, RegistrationConstants.PREVIOUS));
					}
				}
			}

			LOGGER.debug(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Navigating to Fingerprint capture page for user registration completed");
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format(
							"%s -> Exception while navigating to Fingerprint capture page for user registration  %s",
							RegistrationConstants.USER_REG_IRIS_CAPTURE_PREV_SECTION_LOAD_EXP,
							ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.IRIS_NAVIGATE_PREVIOUS_SECTION_ERROR);
		}
	}

	/**
	 * Validate iris.
	 *
	 * @return true, if successful
	 */
	private boolean validateIris() {
		try {
			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Validating the captured irises of individual");

			boolean isValid = false;
			boolean isLeftEyeCaptured = false;
			boolean isRightEyeCaptured = false;

			for (BiometricExceptionDTO exceptionIris : getIrisExceptions()) {
				if (exceptionIris.getMissingBiometric()
						.equalsIgnoreCase(RegistrationConstants.LEFT.concat(RegistrationConstants.EYE))) {
					isLeftEyeCaptured = true;
				} else if (exceptionIris.getMissingBiometric()
						.equalsIgnoreCase(RegistrationConstants.RIGHT.concat(RegistrationConstants.EYE))) {
					isRightEyeCaptured = true;
				}
			}

			for (IrisDetailsDTO irisDetailsDTO : getIrises()) {
				if (validateIrisCapture(irisDetailsDTO)) {
					if (irisDetailsDTO.getIrisType()
							.equalsIgnoreCase(RegistrationConstants.LEFT.concat(RegistrationConstants.EYE))) {
						isLeftEyeCaptured = true;
					} else if (irisDetailsDTO.getIrisType()
							.equalsIgnoreCase(RegistrationConstants.RIGHT.concat(RegistrationConstants.EYE))) {
						isRightEyeCaptured = true;
					}
				} else {
					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.IRIS_QUALITY_SCORE_ERROR);
					return isValid;
				}
			}

			if (isLeftEyeCaptured && isRightEyeCaptured) {
				isValid = true;
			} else {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.IRIS_VALIDATION_ERROR);
			}

			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Validating the captured irises of individual completed");

			return isValid;
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.USER_REG_IRIS_VALIDATION_EXP,
					String.format("Exception while validating the captured irises of individual: %s caused by %s",
							runtimeException.getMessage(), ExceptionUtils.getStackTrace(runtimeException)));
		}
	}

	private boolean validateIrisCapture(IrisDetailsDTO irisDetailsDTO) {
		try {
			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Validating the quality score of the captured iris");

			// Get Configured Threshold and Number of Retries from properties file
			double irisThreshold = Double.parseDouble(getValueFromApplicationMap(RegistrationConstants.IRIS_THRESHOLD));
			int numOfRetries = Integer.parseInt(getValueFromApplicationMap(RegistrationConstants.IRIS_RETRY_COUNT));

			LOGGER.info(LOG_REG_IRIS_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Validating the quality score of the captured iris completed");

			return irisDetailsDTO.getQualityScore() >= irisThreshold
					|| (Double.compare(irisDetailsDTO.getQualityScore(), irisThreshold) < 0
							&& irisDetailsDTO.getNumOfIrisRetry() == numOfRetries)
					|| irisDetailsDTO.isForceCaptured();
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.USER_REG_IRIS_SCORE_VALIDATION_EXP,
					String.format("Exception while validating the quality score of captured iris: %s caused by %s",
							runtimeException.getMessage(), ExceptionUtils.getStackTrace(runtimeException)));
		}
	}

	private List<IrisDetailsDTO> getIrises() {
		if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			return getBiometricDTOFromSession().getOperatorBiometricDTO().getIrisDetailsDTO();
		} else {
			return getRegistrationDTOFromSession().getBiometricDTO().getApplicantBiometricDTO().getIrisDetailsDTO();
		}
	}

	private List<BiometricExceptionDTO> getIrisExceptions() {
		if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			return getBiometricDTOFromSession().getOperatorBiometricDTO().getBiometricExceptionDTO();
		} else {
			return getRegistrationDTOFromSession().getBiometricDTO().getApplicantBiometricDTO()
					.getBiometricExceptionDTO();
		}
	}

	private Stream<IrisDetailsDTO> getIrisBySelectedPane() {
		return getIrises().stream()
				.filter(iris -> iris.getIrisType()
						.contains(StringUtils.containsIgnoreCase(selectedIris.getId(), RegistrationConstants.LEFT)
								? RegistrationConstants.LEFT
								: RegistrationConstants.RIGHT));
	}

	private BiometricDTO getBiometricDTOFromSession() {
		return (BiometricDTO) SessionContext.map().get(RegistrationConstants.USER_ONBOARD_DATA);
	}

	private String getQualityScoreAsString(double qualityScore) {
		return String.valueOf(Math.round(qualityScore)).concat(RegistrationConstants.PERCENTAGE);
	}

	private String getValueFromApplicationMap(String key) {
		return (String) applicationContext.getApplicationMap().get(key);
	}

	public void clearIrisData() {
		leftIrisImage
				.setImage(new Image(getClass().getResource(RegistrationConstants.LEFT_IRIS_IMG_PATH).toExternalForm()));
		leftIrisQualityScore.setText(RegistrationConstants.EMPTY);

		rightIrisImage.setImage(
				new Image(getClass().getResource(RegistrationConstants.RIGHT_IRIS_IMG_PATH).toExternalForm()));
		rightIrisQualityScore.setText(RegistrationConstants.EMPTY);

		if (getRegistrationDTOFromSession() != null) {
			getRegistrationDTOFromSession().getBiometricDTO().getApplicantBiometricDTO()
					.setIrisDetailsDTO(new ArrayList<>());
		}
	}

	public void clearIrisBasedOnExceptions() {
		if (getIrisExceptions().stream()
				.anyMatch(exceptionIris -> exceptionIris.isMarkedAsException()
						&& StringUtils.containsIgnoreCase(exceptionIris.getMissingBiometric(),
								(RegistrationConstants.LEFT).concat(RegistrationConstants.EYE)))) {
			leftIrisImage.setImage(
					new Image(getClass().getResource(RegistrationConstants.LEFT_IRIS_IMG_PATH).toExternalForm()));
			leftIrisQualityScore.setText(RegistrationConstants.EMPTY);

			getIrises().removeIf(iris -> iris.getIrisType()
					.equalsIgnoreCase((RegistrationConstants.LEFT).concat(RegistrationConstants.EYE)));
		}

		if (getIrisExceptions().stream()
				.anyMatch(exceptionIris -> exceptionIris.isMarkedAsException()
						&& StringUtils.containsIgnoreCase(exceptionIris.getMissingBiometric(),
								(RegistrationConstants.RIGHT).concat(RegistrationConstants.EYE)))) {
			rightIrisImage.setImage(
					new Image(getClass().getResource(RegistrationConstants.RIGHT_IRIS_IMG_PATH).toExternalForm()));
			rightIrisQualityScore.setText(RegistrationConstants.EMPTY);
			getIrises().removeIf(iris -> iris.getIrisType()
					.equalsIgnoreCase((RegistrationConstants.RIGHT).concat(RegistrationConstants.EYE)));
		}
	}
}
