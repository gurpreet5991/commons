package io.mosip.registration.controller.device;

import static io.mosip.registration.constants.LoggerConstants.LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
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
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.reg.RegistrationController;
import io.mosip.registration.controller.reg.UserOnboardParentController;
import io.mosip.registration.device.fp.FingerprintFacade;
import io.mosip.registration.dto.biometric.BiometricDTO;
import io.mosip.registration.dto.biometric.BiometricExceptionDTO;
import io.mosip.registration.dto.biometric.FingerprintDetailsDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.device.impl.FingerPrintCaptureServiceImpl;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * {@code FingerPrintCaptureController} is to capture and display the captured
 * fingerprints.
 * 
 * @author Mahesh Kumar
 * @since 1.0
 */
@Controller
public class FingerPrintCaptureController extends BaseController implements Initializable {

	/**
	 * Instance of {@link Logger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(FingerPrintCaptureController.class);

	/** The finger print capture service impl. */
	@Autowired
	private FingerPrintCaptureServiceImpl fingerPrintCaptureServiceImpl;

	/** The registration controller. */
	@Autowired
	private RegistrationController registrationController;

	@Autowired
	private ScanPopUpViewController scanPopUpViewController;

	/** The finger print capture pane. */
	@FXML
	private AnchorPane fingerPrintCapturePane;

	/** The left hand palm pane. */
	@FXML
	private AnchorPane leftHandPalmPane;

	/** The right hand palm pane. */
	@FXML
	private AnchorPane rightHandPalmPane;

	/** The thumb pane. */
	@FXML
	private AnchorPane thumbPane;

	/** The left hand palm imageview. */
	@FXML
	private ImageView leftHandPalmImageview;

	/** The right hand palm imageview. */
	@FXML
	private ImageView rightHandPalmImageview;

	/** The thumb imageview. */
	@FXML
	private ImageView thumbImageview;

	/** The left slap quality score. */
	@FXML
	private Label leftSlapQualityScore;

	/** The right slap quality score. */
	@FXML
	private Label rightSlapQualityScore;

	/** The thumbs quality score. */
	@FXML
	private Label thumbsQualityScore;

	/** The left slap threshold score label. */
	@FXML
	private Label leftSlapThresholdScoreLbl;

	/** The right slap threshold score label. */
	@FXML
	private Label rightSlapThresholdScoreLbl;

	/** The thumbs threshold score label. */
	@FXML
	private Label thumbsThresholdScoreLbl;

	/** The duplicate check label. */
	@FXML
	private Label duplicateCheckLbl;

	/** The selected pane. */
	private AnchorPane selectedPane = null;

	/** The selected pane. */
	@Autowired
	private FingerprintFacade fingerPrintFacade = null;

	@Autowired
	private IrisCaptureController irisCaptureController;

	@Autowired
	private UserOnboardParentController userOnboardParentController;

	/** The scan btn. */
	@FXML
	private Button scanBtn;

	private int leftSlapCount;
	private int rightSlapCount;
	private int thumbCount;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javafx.fxml.Initializable#initialize(java.net.URL,
	 * java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Loading of FingerprintCapture screen started");
		try {

			scanBtn.setDisable(true);

			EventHandler<Event> mouseClick = event -> {
				if (event.getSource() instanceof AnchorPane) {
					AnchorPane sourcePane = (AnchorPane) event.getSource();
					sourcePane.requestFocus();
					selectedPane = sourcePane;
					scanBtn.setDisable(true);
					duplicateCheckLbl.setText(RegistrationConstants.EMPTY);

					exceptionFingersCount();

					// Get the Fingerprint from RegistrationDTO based on selected Fingerprint Pane
					FingerprintDetailsDTO fpDetailsDTO = getFingerprintBySelectedPane().findFirst().orElse(null);

					if ((leftHandPalmPane.getId().equals(selectedPane.getId()) && leftSlapCount < 4)
							&& (fpDetailsDTO == null
									|| (fpDetailsDTO.getFingerType().equals(RegistrationConstants.LEFTPALM)
											&& fpDetailsDTO.getQualityScore() < Double
													.parseDouble(getValueFromApplicationContext(
															RegistrationConstants.LEFTSLAP_FINGERPRINT_THRESHOLD))))
							|| (rightHandPalmPane.getId().equals(selectedPane.getId()) && rightSlapCount < 4)
									&& (fpDetailsDTO == null || (fpDetailsDTO.getFingerType()
											.equals(RegistrationConstants.RIGHTPALM)
											&& fpDetailsDTO.getQualityScore() < Double
													.parseDouble(getValueFromApplicationContext(
															RegistrationConstants.RIGHTSLAP_FINGERPRINT_THRESHOLD))))
							|| (thumbPane.getId().equals(selectedPane.getId()) && thumbCount < 2)
									&& (fpDetailsDTO == null || (fpDetailsDTO.getFingerType()
											.equals(RegistrationConstants.THUMBS)
											&& fpDetailsDTO.getQualityScore() < Double
													.parseDouble(getValueFromApplicationContext(
															RegistrationConstants.THUMBS_FINGERPRINT_THRESHOLD))))) {
						scanBtn.setDisable(false);
					}
				}
			};

			// Add event handler object to mouse click event
			leftHandPalmPane.setOnMouseClicked(mouseClick);
			rightHandPalmPane.setOnMouseClicked(mouseClick);
			thumbPane.setOnMouseClicked(mouseClick);

			leftSlapThresholdScoreLbl.setText(getQualityScore(Double.parseDouble(
					getValueFromApplicationContext(RegistrationConstants.LEFTSLAP_FINGERPRINT_THRESHOLD))));

			rightSlapThresholdScoreLbl.setText(getQualityScore(Double.parseDouble(
					getValueFromApplicationContext(RegistrationConstants.RIGHTSLAP_FINGERPRINT_THRESHOLD))));

			thumbsThresholdScoreLbl.setText(getQualityScore(Double
					.parseDouble(getValueFromApplicationContext(RegistrationConstants.THUMBS_FINGERPRINT_THRESHOLD))));

			loadingImageFromSessionContext();

			LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Loading of FingerprintCapture screen ended");
		} catch (

		RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format(
							"%s -> Exception while initializing Fingerprint Capture page for user registration  %s",
							RegistrationConstants.USER_REG_FINGERPRINT_PAGE_LOAD_EXP,
							runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException)));

			throw new RegBaseUncheckedException(RegistrationConstants.USER_REG_FINGERPRINT_PAGE_LOAD_EXP,
					String.format("Exception while initializing Fingerprint Capture page for user registration  %s",
							runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException)));
		}
	}

	@SuppressWarnings("unchecked")
	public void clearImage() {

		exceptionFingersCount();
		if (leftSlapCount == 4) {
			leftHandPalmImageview.setImage(
					new Image(getClass().getResource(RegistrationConstants.LEFTPALM_IMG_PATH).toExternalForm()));
			leftSlapQualityScore.setText(RegistrationConstants.EMPTY);

			removeFingerPrint(RegistrationConstants.LEFTPALM);

		}
		if (rightSlapCount == 4) {
			rightHandPalmImageview.setImage(
					new Image(getClass().getResource(RegistrationConstants.RIGHTPALM_IMG_PATH).toExternalForm()));
			rightSlapQualityScore.setText(RegistrationConstants.EMPTY);

			removeFingerPrint(RegistrationConstants.RIGHTPALM);

		}
		if (thumbCount == 2) {
			thumbImageview
					.setImage(new Image(getClass().getResource(RegistrationConstants.THUMB_IMG_PATH).toExternalForm()));
			thumbsQualityScore.setText(RegistrationConstants.EMPTY);

			removeFingerPrint(RegistrationConstants.THUMBS);

		}
		List<BiometricExceptionDTO> tempExceptionList = (List<BiometricExceptionDTO>) SessionContext.map()
				.get(RegistrationConstants.NEW_BIOMETRIC_EXCEPTION);
		if ((tempExceptionList == null || tempExceptionList.isEmpty())
				&& !(boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			leftHandPalmImageview.setImage(
					new Image(getClass().getResource(RegistrationConstants.LEFTPALM_IMG_PATH).toExternalForm()));
			leftSlapQualityScore.setText(RegistrationConstants.EMPTY);
			rightHandPalmImageview.setImage(
					new Image(getClass().getResource(RegistrationConstants.RIGHTPALM_IMG_PATH).toExternalForm()));
			rightSlapQualityScore.setText(RegistrationConstants.EMPTY);
			thumbImageview
					.setImage(new Image(getClass().getResource(RegistrationConstants.THUMB_IMG_PATH).toExternalForm()));
			thumbsQualityScore.setText(RegistrationConstants.EMPTY);
		}
		List<BiometricExceptionDTO> bioExceptionList = (List<BiometricExceptionDTO>) SessionContext.map()
				.get(RegistrationConstants.OLD_BIOMETRIC_EXCEPTION);
		if (bioExceptionList == null || bioExceptionList.isEmpty()) {
			bioExceptionList = tempExceptionList;
		} else {
			List<String> bioList1 = null;
			List<String> bioList = bioExceptionList.stream().map(bio -> bio.getMissingBiometric())
					.collect(Collectors.toList());
			if (null != tempExceptionList) {
				bioList1 = tempExceptionList.stream().map(bio -> bio.getMissingBiometric())
						.collect(Collectors.toList());
			}

			List<String> changedException = (List<String>) CollectionUtils.disjunction(bioList, bioList1);

			changedException.forEach(biometricException -> {
				if (biometricException.contains(RegistrationConstants.LEFT.toLowerCase())
						&& !biometricException.contains(RegistrationConstants.THUMB)
						&& !biometricException.contains(RegistrationConstants.EYE)) {
					removeFingerPrint(RegistrationConstants.LEFTPALM);
				} else if (biometricException.contains(RegistrationConstants.RIGHT.toLowerCase())
						&& !biometricException.contains(RegistrationConstants.THUMB)
						&& !biometricException.contains(RegistrationConstants.EYE)) {
					removeFingerPrint(RegistrationConstants.RIGHTPALM);
				} else if (biometricException.contains(RegistrationConstants.THUMB)) {
					removeFingerPrint(RegistrationConstants.THUMBS);
				}
			});

		}
		SessionContext.map().put(RegistrationConstants.OLD_BIOMETRIC_EXCEPTION, tempExceptionList);
	}

	private void removeFingerPrint(String handSlap) {
		Iterator<FingerprintDetailsDTO> iterator;

		if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			iterator = getBiometricDTOFromSession().getOperatorBiometricDTO().getFingerprintDetailsDTO().iterator();
		} else {
			iterator = getRegistrationDTOFromSession() != null
					? getRegistrationDTOFromSession().getBiometricDTO().getApplicantBiometricDTO()
							.getFingerprintDetailsDTO().iterator()
					: null;
		}

		while (iterator != null && iterator.hasNext()) {
			FingerprintDetailsDTO value = iterator.next();
			if (value.getFingerType().contains(handSlap)) {
				iterator.remove();
				break;
			}
		}
	}

	public void clearFingerPrintDTO() {
		leftHandPalmImageview
				.setImage(new Image(getClass().getResource(RegistrationConstants.LEFTPALM_IMG_PATH).toExternalForm()));
		leftSlapQualityScore.setText(RegistrationConstants.EMPTY);
		removeFingerPrint(RegistrationConstants.LEFTPALM);

		rightHandPalmImageview
				.setImage(new Image(getClass().getResource(RegistrationConstants.RIGHTPALM_IMG_PATH).toExternalForm()));
		rightSlapQualityScore.setText(RegistrationConstants.EMPTY);
		removeFingerPrint(RegistrationConstants.RIGHTPALM);

		thumbImageview
				.setImage(new Image(getClass().getResource(RegistrationConstants.THUMB_IMG_PATH).toExternalForm()));
		thumbsQualityScore.setText(RegistrationConstants.EMPTY);
		removeFingerPrint(RegistrationConstants.THUMBS);
	}

	private void exceptionFingersCount() {
		leftSlapCount = 0;
		rightSlapCount = 0;
		thumbCount = 0;

		List<BiometricExceptionDTO> biometricExceptionDTOs;
		if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			biometricExceptionDTOs = getBiometricDTOFromSession().getOperatorBiometricDTO().getBiometricExceptionDTO();
		} else {
			biometricExceptionDTOs = getRegistrationDTOFromSession().getBiometricDTO().getApplicantBiometricDTO()
					.getBiometricExceptionDTO();
		}
		for (BiometricExceptionDTO biometricExceptionDTO : biometricExceptionDTOs) {

			if (biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.LEFT.toLowerCase())
					&& !biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.THUMB)
					&& !biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.EYE)) {
				leftSlapCount++;
			}
			if (biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.RIGHT.toLowerCase())
					&& !biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.THUMB)
					&& !biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.EYE)) {
				rightSlapCount++;
			}
			if (biometricExceptionDTO.getMissingBiometric().contains(RegistrationConstants.THUMB)) {
				thumbCount++;
			}
		}
	}

	private void loadingImageFromSessionContext() {
		if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			if (null != getBiometricDTOFromSession()) {
				loadImage(getBiometricDTOFromSession().getOperatorBiometricDTO().getFingerprintDetailsDTO());
			}
		} else {
			if (null != getRegistrationDTOFromSession()) {
				loadImage(getRegistrationDTOFromSession().getBiometricDTO().getApplicantBiometricDTO()
						.getFingerprintDetailsDTO());
			}
		}
	}

	private void loadImage(List<FingerprintDetailsDTO> fingerprintDetailsDTO) {
		fingerprintDetailsDTO.forEach(item -> {
			if (item.getFingerType().equals(RegistrationConstants.LEFTPALM)) {
				leftHandPalmImageview.setImage(new Image(new ByteArrayInputStream(item.getFingerPrint())));
				leftSlapQualityScore.setText(getQualityScore(item.getQualityScore()));
			} else if (item.getFingerType().equals(RegistrationConstants.RIGHTPALM)) {
				rightHandPalmImageview.setImage(new Image(new ByteArrayInputStream(item.getFingerPrint())));
				rightSlapQualityScore.setText(getQualityScore(item.getQualityScore()));
			} else if (item.getFingerType().equals(RegistrationConstants.THUMBS)) {
				thumbImageview.setImage(new Image(new ByteArrayInputStream(item.getFingerPrint())));
				thumbPane.getStyleClass().add("fingerPrintPanesSelected");
				thumbsQualityScore.setText(getQualityScore(item.getQualityScore()));
			}
		});
	}

	private String getQualityScore(Double qulaityScore) {
		return String.valueOf(Math.round(qulaityScore)).concat(RegistrationConstants.PERCENTAGE);
	}

	public void scan() {
		try {
			LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Opening pop-up screen to capture fingerprint for user registration");
			FingerprintDetailsDTO fpDetailsDTO = getFingerprintBySelectedPane().findFirst().orElse(null);

			if ((fpDetailsDTO == null || fpDetailsDTO.getNumRetry() < Integer
					.parseInt(getValueFromApplicationContext(RegistrationConstants.FINGERPRINT_RETRIES_COUNT)))
					|| ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER))) {

				auditFactory.audit(AuditEvent.REG_BIO_THUMBS_SCAN, Components.REG_BIOMETRICS, SessionContext.userId(),
						AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

				scanPopUpViewController.init(this, RegistrationUIConstants.FINGERPRINT);
			} else {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.FINGERPRINT_MAX_RETRIES_ALERT);
			}

			LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Scanning of fingersplaced ended");

		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID, String.format(
					"%s -> Exception while Opening pop-up screen to capture fingerprint for user registration  %s",
					RegistrationConstants.USER_REG_FINGERPRINT_CAPTURE_POPUP_LOAD_EXP,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_FINGERPRINT_SCAN_POPUP);
		}
	}

	@Override
	public void scan(Stage popupStage) {

		if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			operatorBiometricScan(popupStage);
		} else {
			applicantBiometricScan(popupStage);
		}

	}

	private void operatorBiometricScan(Stage popupStage) {
		try {

			FingerprintDetailsDTO detailsDTO = null;

			List<FingerprintDetailsDTO> fingerprintDetailsDTOs = getBiometricDTOFromSession().getOperatorBiometricDTO()
					.getFingerprintDetailsDTO();

			if (fingerprintDetailsDTOs == null || fingerprintDetailsDTOs.isEmpty()) {
				fingerprintDetailsDTOs = new ArrayList<>(3);
				getBiometricDTOFromSession().getOperatorBiometricDTO().setFingerprintDetailsDTO(fingerprintDetailsDTOs);
			}

			if (selectedPane.getId() == leftHandPalmPane.getId()) {

				scanFingers(detailsDTO, fingerprintDetailsDTOs, RegistrationConstants.LEFTPALM,

						RegistrationConstants.LEFTHAND_SEGMNTD_FILE_PATHS_USERONBOARD, leftHandPalmImageview,

						leftSlapQualityScore, popupStage, leftHandPalmPane);

			} else if (selectedPane.getId() == rightHandPalmPane.getId()) {

				scanFingers(detailsDTO, fingerprintDetailsDTOs, RegistrationConstants.RIGHTPALM,

						RegistrationConstants.RIGHTHAND_SEGMNTD_FILE_PATHS_USERONBOARD, rightHandPalmImageview,

						rightSlapQualityScore, popupStage, rightHandPalmPane);

			} else if (selectedPane.getId() == thumbPane.getId()) {

				scanFingers(detailsDTO, fingerprintDetailsDTOs, RegistrationConstants.THUMBS,

						RegistrationConstants.THUMBS_SEGMNTD_FILE_PATHS_USERONBOARD, thumbImageview, thumbsQualityScore,
						popupStage, thumbPane);

			}

		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format(
							"Exception while getting the scanned Finger details for user registration: %s caused by %s",
							runtimeException.getMessage(),
							runtimeException.getCause() + ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.FINGERPRINT_SCANNING_ERROR);
		} catch (RegBaseCheckedException regBaseCheckedException) {
			LOGGER.error(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID, String.format(
					"Exception while getting the scanned Finger details for user registration: %s caused by %s",
					regBaseCheckedException.getMessage(),
					regBaseCheckedException.getCause() + ExceptionUtils.getStackTrace(regBaseCheckedException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.FINGERPRINT_SCANNING_ERROR);
		}
		LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID, "Scan Finger has ended");
	}

	private void applicantBiometricScan(Stage popupStage) {
		try {

			FingerprintDetailsDTO detailsDTO = null;

			List<FingerprintDetailsDTO> fingerprintDetailsDTOs = getRegistrationDTOFromSession().getBiometricDTO()
					.getApplicantBiometricDTO().getFingerprintDetailsDTO();

			if (fingerprintDetailsDTOs == null || fingerprintDetailsDTOs.isEmpty()) {
				fingerprintDetailsDTOs = new ArrayList<>(3);
				getRegistrationDTOFromSession().getBiometricDTO().getApplicantBiometricDTO()
						.setFingerprintDetailsDTO(fingerprintDetailsDTOs);
			}

			if (selectedPane.getId() == leftHandPalmPane.getId()) {

				scanFingers(detailsDTO, fingerprintDetailsDTOs, RegistrationConstants.LEFTPALM,

						RegistrationConstants.LEFTHAND_SEGMNTD_FILE_PATHS, leftHandPalmImageview,

						leftSlapQualityScore, popupStage,leftHandPalmPane);

			} else if (selectedPane.getId() == rightHandPalmPane.getId()) {

				if (SessionContext.map().containsKey(RegistrationConstants.DUPLICATE_FINGER)) {

					scanFingers(detailsDTO, fingerprintDetailsDTOs, RegistrationConstants.RIGHTPALM,

							RegistrationConstants.RIGHTHAND_SEGMNTD_FILE_PATHS, rightHandPalmImageview,

							rightSlapQualityScore, popupStage, rightHandPalmPane);

				} else {
					scanFingers(detailsDTO, fingerprintDetailsDTOs, RegistrationConstants.RIGHTPALM,

							RegistrationConstants.RIGHTHAND_SEGMNTD_DUPLICATE_FILE_PATHS, rightHandPalmImageview,

							rightSlapQualityScore, popupStage, rightHandPalmPane);
				}

			} else if (selectedPane.getId() == thumbPane.getId()) {

				scanFingers(detailsDTO, fingerprintDetailsDTOs, RegistrationConstants.THUMBS,

						RegistrationConstants.THUMBS_SEGMNTD_FILE_PATHS, thumbImageview, thumbsQualityScore,
						popupStage, thumbPane);

			}

		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format(
							"Exception while getting the scanned Finger details for user registration: %s caused by %s",
							runtimeException.getMessage(),
							runtimeException.getCause() + ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.FINGERPRINT_SCANNING_ERROR);
		} catch (RegBaseCheckedException regBaseCheckedException) {
			LOGGER.error(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID, String.format(
					"Exception while getting the scanned Finger details for user registration: %s caused by %s",
					regBaseCheckedException.getMessage(),
					regBaseCheckedException.getCause() + ExceptionUtils.getStackTrace(regBaseCheckedException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.FINGERPRINT_SCANNING_ERROR);
		}
		LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID, "Scan Finger has ended");

	}

	private void scanFingers(FingerprintDetailsDTO detailsDTO, List<FingerprintDetailsDTO> fingerprintDetailsDTOs,
			String fingerType, String[] segmentedFingersPath, ImageView fingerImageView, Label scoreLabel,
			Stage popupStage, AnchorPane parentPane) throws RegBaseCheckedException {

		ImageView imageView = fingerImageView;
		Label qualityScoreLabel = scoreLabel;
		if (fingerprintDetailsDTOs != null) {

			for (FingerprintDetailsDTO fingerprintDetailsDTO : fingerprintDetailsDTOs) {
				if (fingerprintDetailsDTO.getFingerType().equals(fingerType)) {
					detailsDTO = fingerprintDetailsDTO;

					for (String segmentedFingerPath : segmentedFingersPath) {
						String[] path = segmentedFingerPath.split("/");
						for (FingerprintDetailsDTO segmentedfpDetailsDTO : fingerprintDetailsDTO
								.getSegmentedFingerprints()) {
							if (segmentedfpDetailsDTO.getFingerType().equals(path[3])) {
								fingerprintDetailsDTO.getSegmentedFingerprints().remove(segmentedfpDetailsDTO);
								break;
							}
						}
					}
					if (!(boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
						detailsDTO.setNumRetry(fingerprintDetailsDTO.getNumRetry() + 1);
					}
					break;
				}
			}
			if (detailsDTO == null) {
				detailsDTO = new FingerprintDetailsDTO();
				fingerprintDetailsDTOs.add(detailsDTO);
			}
		}
		fingerPrintFacade.getFingerPrintImageAsDTO(detailsDTO, fingerType);

		fingerPrintFacade.segmentFingerPrintImage(detailsDTO, segmentedFingersPath);

		scanPopUpViewController.getScanImage().setImage(convertBytesToImage(detailsDTO.getFingerPrint()));

		generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.FP_CAPTURE_SUCCESS);

		popupStage.close();
		parentPane.getStyleClass().add("fingerPrintPanesSelected");
		imageView.setImage(convertBytesToImage(detailsDTO.getFingerPrint()));
		qualityScoreLabel.setText(getQualityScore(detailsDTO.getQualityScore()));
		scanBtn.setDisable(true);
	}

	/**
	 * {@code saveBiometricDetails} is to check the deduplication of captured finger
	 * prints
	 */
	public void goToNextPage() {
		try {
			auditFactory.audit(AuditEvent.REG_BIO_FINGERPRINT_NEXT, Components.REG_BIOMETRICS, SessionContext.userId(),
					AuditReferenceIdTypes.USER_ID.getReferenceTypeId());
			
			LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Navigating to Iris capture page for user registration started");

			exceptionFingersCount();
			if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
				if (validateFingerPrints()) {
					irisCaptureController.clearIrisBasedOnExceptions();
					userOnboardParentController.showCurrentPage(RegistrationConstants.FINGERPRINT_CAPTURE,
							getOnboardPageDetails(RegistrationConstants.FINGERPRINT_CAPTURE,
									RegistrationConstants.NEXT));
				}
			} else {
				if (getRegistrationDTOFromSession().getSelectionListDTO() != null) {
					if (validateFingerPrints()) {
						SessionContext.map().remove(RegistrationConstants.DUPLICATE_FINGER);

						long irisCount = getRegistrationDTOFromSession().getBiometricDTO().getApplicantBiometricDTO()
								.getBiometricExceptionDTO().stream()
								.filter(bio -> bio.getBiometricType().equalsIgnoreCase(RegistrationConstants.IRIS))
								.count();

						if (getRegistrationDTOFromSession().getSelectionListDTO().isBiometricIris() || irisCount > 0) {
							irisCaptureController.clearIrisBasedOnExceptions();
							SessionContext.map().put("fingerPrintCapture", false);
							SessionContext.map().put("irisCapture", true);
						} else {
							SessionContext.map().put("fingerPrintCapture", false);
							SessionContext.map().put("faceCapture", true);
						}
						registrationController.showUINUpdateCurrentPage();
					}
				} else {
					if (validateFingerPrints()) {
						SessionContext.map().remove(RegistrationConstants.DUPLICATE_FINGER);
						irisCaptureController.clearIrisBasedOnExceptions();

						registrationController.showCurrentPage(RegistrationConstants.FINGERPRINT_CAPTURE,
								getPageDetails(RegistrationConstants.FINGERPRINT_CAPTURE, RegistrationConstants.NEXT));
					}
				}
			}
			LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Navigating to Iris capture page for user registration ended");
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format("%s -> Exception while navigating to Iris capture page for user registration  %s",
							RegistrationConstants.USER_REG_FINGERPRINT_CAPTURE_NEXT_SECTION_LOAD_EXP,
							runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.FINGERPRINT_NAVIGATE_NEXT_SECTION_ERROR);
		}

	}

	/**
	 * {@code saveBiometricDetails} is to check the deduplication of captured finger
	 * prints
	 */
	public void goToPreviousPage() {
		try {
			auditFactory.audit(AuditEvent.REG_BIO_FINGERPRINT_BACK, Components.REG_BIOMETRICS, SessionContext.userId(),
					AuditReferenceIdTypes.USER_ID.getReferenceTypeId());
			
			LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Navigating to Demographic capture page for user registration started");

			exceptionFingersCount();
			if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
				if (validateFingerPrints()) {
					userOnboardParentController.showCurrentPage(RegistrationConstants.FINGERPRINT_CAPTURE,
							getOnboardPageDetails(RegistrationConstants.FINGERPRINT_CAPTURE,
									RegistrationConstants.PREVIOUS));
				}
			} else {
				if (validateFingerPrints()) {
					SessionContext.getInstance().getMapObject().remove(RegistrationConstants.DUPLICATE_FINGER);
					if (getRegistrationDTOFromSession().getSelectionListDTO() != null) {
						if ((boolean) SessionContext.getInstance().getUserContext().getUserMap()
								.get(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION)) {
							SessionContext.getInstance().getMapObject().put("fingerPrintCapture", false);
							SessionContext.getInstance().getMapObject().put("biometricException", true);
							registrationController.showUINUpdateCurrentPage();
						} else {
							SessionContext.getInstance().getMapObject().put("fingerPrintCapture", false);
							SessionContext.getInstance().getMapObject().put("documentScan", true);
							registrationController.showUINUpdateCurrentPage();
						}
					} else {
						registrationController.showCurrentPage(RegistrationConstants.FINGERPRINT_CAPTURE,
								getPageDetails(RegistrationConstants.FINGERPRINT_CAPTURE,
										RegistrationConstants.PREVIOUS));
					}
				}
			}
			LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Navigating to Demographic capture page for user registration ended");
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format(
							"%s -> Exception while navigating to Demographic capture page for user registration  %s",
							RegistrationConstants.USER_REG_FINGERPRINT_CAPTURE_PREV_SECTION_LOAD_EXP,
							runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR,
					RegistrationUIConstants.FINGERPRINT_NAVIGATE_PREVIOUS_SECTION_ERROR);
		}
	}

	/**
	 * Validating finger prints.
	 *
	 * @return true, if successful
	 */
	private boolean validateFingerPrints() {
		try {
			LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Validating Fingerprints captured started");

			List<FingerprintDetailsDTO> segmentedFingerprintDetailsDTOs = new ArrayList<>();
			boolean isValid = false;
			boolean isleftHandSlapCaptured = false;
			boolean isrightHandSlapCaptured = false;
			boolean isthumbsCaptured = false;

			List<FingerprintDetailsDTO> fingerprintDetailsDTOs;

			if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
				fingerprintDetailsDTOs = getBiometricDTOFromSession().getOperatorBiometricDTO()
						.getFingerprintDetailsDTO();
			} else {
				fingerprintDetailsDTOs = getRegistrationDTOFromSession().getBiometricDTO().getApplicantBiometricDTO()
						.getFingerprintDetailsDTO();
			}

			for (FingerprintDetailsDTO fingerprintDetailsDTO : fingerprintDetailsDTOs) {
				for (FingerprintDetailsDTO segmentedFingerprintDetailsDTO : fingerprintDetailsDTO
						.getSegmentedFingerprints()) {
					segmentedFingerprintDetailsDTOs.add(segmentedFingerprintDetailsDTO);
				}
			}

			for (FingerprintDetailsDTO fingerprintDetailsDTO : fingerprintDetailsDTOs) {
				if (validateQualityScore(fingerprintDetailsDTO)) {
					if (fingerprintDetailsDTO.getFingerType().equalsIgnoreCase(RegistrationConstants.LEFTPALM)
							|| leftSlapCount >= 4) {
						isleftHandSlapCaptured = true;
					}
					if (fingerprintDetailsDTO.getFingerType().equalsIgnoreCase(RegistrationConstants.RIGHTPALM)
							|| rightSlapCount >= 4) {
						isrightHandSlapCaptured = true;
					}
					if (fingerprintDetailsDTO.getFingerType().equalsIgnoreCase(RegistrationConstants.THUMBS)
							|| thumbCount >= 2) {
						isthumbsCaptured = true;
					}
				} else {
					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.IRIS_QUALITY_SCORE_ERROR);
					return isValid;
				}
			}

			if (fingerprintDetailsDTOs.isEmpty() && leftSlapCount >= 4 && rightSlapCount >= 4 && thumbCount >= 2) {
				isleftHandSlapCaptured = true;
				isrightHandSlapCaptured = true;
				isthumbsCaptured = true;
			}

			if (isleftHandSlapCaptured && isrightHandSlapCaptured && isthumbsCaptured) {
				if (!(boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
					if (!fingerPrintCaptureServiceImpl.validateFingerprint(segmentedFingerprintDetailsDTOs)) {
						isValid = true;
					} else {
						FingerprintDetailsDTO duplicateFinger = (FingerprintDetailsDTO) SessionContext.map()
								.get(RegistrationConstants.DUPLICATE_FINGER);

						Iterator<FingerprintDetailsDTO> iterator = fingerprintDetailsDTOs.iterator();

						while (iterator.hasNext()) {
							FingerprintDetailsDTO value = iterator.next();
							for (FingerprintDetailsDTO duplicate : value.getSegmentedFingerprints()) {
								if (duplicate.getFingerType().equals(duplicateFinger.getFingerType())) {
									iterator.remove();
									break;
								}
							}
						}
						String finger;
						if (duplicateFinger.getFingerType().contains(RegistrationConstants.LEFT.toLowerCase())) {
							finger = duplicateFinger.getFingerType().replace(RegistrationConstants.LEFT.toLowerCase(),
									RegistrationConstants.LEFT_HAND);
						} else {
							finger = duplicateFinger.getFingerType().replace(RegistrationConstants.RIGHT.toLowerCase(),
									RegistrationConstants.RIGHT_HAND);
						}
						duplicateCheckLbl.setText(finger + " " + RegistrationUIConstants.FINGERPRINT_DUPLICATION_ALERT);
					}
				} else {
					isValid = true;
				}
			} else {
				generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.FINGERPRINT_SCAN_ALERT);
			}
			LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Validating Fingerprints captured ended");
			return isValid;
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));

			throw new RegBaseUncheckedException(RegistrationConstants.USER_REG_FINGERPRINT_VALIDATION_EXP,
					String.format("Exception while validating the captured fingerprints of individual: %s caused by %s",
							runtimeException.getMessage(), runtimeException.getCause()));
		}
	}

	/**
	 * Validating quality score of captured fingerprints.
	 *
	 * @param fingerprintDetailsDTO the fingerprint details DTO
	 * @return true, if successful
	 */
	private boolean validateQualityScore(FingerprintDetailsDTO fingerprintDetailsDTO) {
		try {
			LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Validating quality score of captured fingerprints started");
			if (fingerprintDetailsDTO.getFingerType().equals(RegistrationConstants.LEFTPALM)) {
				return validate(fingerprintDetailsDTO, RegistrationConstants.LEFTSLAP_FINGERPRINT_THRESHOLD);
			} else if (fingerprintDetailsDTO.getFingerType().equals(RegistrationConstants.RIGHTPALM)) {
				return validate(fingerprintDetailsDTO, RegistrationConstants.RIGHTSLAP_FINGERPRINT_THRESHOLD);
			} else if (fingerprintDetailsDTO.getFingerType().equals(RegistrationConstants.THUMBS)) {
				return validate(fingerprintDetailsDTO, RegistrationConstants.THUMBS_FINGERPRINT_THRESHOLD);
			}
			LOGGER.info(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Validating quality score of captured fingerprints ended");
			return false;
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_FINGERPRINT_CAPTURE_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));

			throw new RegBaseUncheckedException(RegistrationConstants.USER_REG_FINGERPRINT_SCORE_VALIDATION_EXP,
					String.format(
							"Exception while validating the quality score of captured Fingerprints: %s caused by %s",
							runtimeException.getMessage(), runtimeException.getCause()));
		}
	}

	/**
	 * Validates QualityScore.
	 *
	 * @param fingerprintDetailsDTO the fingerprint details DTO
	 * @param handThreshold         the hand threshold
	 * @return boolean
	 */
	private Boolean validate(FingerprintDetailsDTO fingerprintDetailsDTO, String handThreshold) {
		return fingerprintDetailsDTO.getQualityScore() >= Double
				.parseDouble(getValueFromApplicationContext(handThreshold))
				|| (fingerprintDetailsDTO.getQualityScore() < Double
						.parseDouble(getValueFromApplicationContext(handThreshold))
						&& fingerprintDetailsDTO.getNumRetry() == Integer.parseInt(
								getValueFromApplicationContext(RegistrationConstants.FINGERPRINT_RETRIES_COUNT)))
				|| fingerprintDetailsDTO.isForceCaptured();
	}

	/**
	 * Gets the fingerprint by selected pane.
	 *
	 * @return the fingerprint by selected pane
	 */
	private Stream<FingerprintDetailsDTO> getFingerprintBySelectedPane() {
		if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			return getSelectedPane(getBiometricDTOFromSession().getOperatorBiometricDTO().getFingerprintDetailsDTO());
		} else {
			return getSelectedPane(getRegistrationDTOFromSession().getBiometricDTO().getApplicantBiometricDTO()
					.getFingerprintDetailsDTO());
		}
	}

	/**
	 * Gets the selected pane.
	 *
	 * @param fingerPrintDetails the finger print details
	 * @return the selected pane
	 */
	private Stream<FingerprintDetailsDTO> getSelectedPane(List<FingerprintDetailsDTO> fingerPrintDetails) {
		return fingerPrintDetails.stream().filter(fingerprint -> {
			String fingerType;
			if (StringUtils.containsIgnoreCase(selectedPane.getId(), leftHandPalmPane.getId())) {
				fingerType = RegistrationConstants.LEFTPALM;
			} else {
				if (StringUtils.containsIgnoreCase(selectedPane.getId(), rightHandPalmPane.getId())) {
					fingerType = RegistrationConstants.RIGHTPALM;
				} else {
					fingerType = RegistrationConstants.THUMBS;
				}
			}
			return fingerprint.getFingerType().contains(fingerType);
		});
	}

	/**
	 * Gets the biometric DTO from session.
	 *
	 * @return the biometric DTO from session
	 */
	private BiometricDTO getBiometricDTOFromSession() {
		return (BiometricDTO) SessionContext.map().get(RegistrationConstants.USER_ONBOARD_DATA);
	}

	/**
	 * Gets the value from application context.
	 *
	 * @param key the key
	 * @return the value from application context
	 */
	private String getValueFromApplicationContext(String key) {
		return (String) applicationContext.getApplicationMap().get(key);
	}
}
