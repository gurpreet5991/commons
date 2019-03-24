package io.mosip.registration.controller;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.registration.audit.AuditFactory;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.LoggerConstants;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.device.FaceCaptureController;
import io.mosip.registration.controller.device.FingerPrintCaptureController;
import io.mosip.registration.controller.device.IrisCaptureController;
import io.mosip.registration.controller.reg.BiometricExceptionController;
import io.mosip.registration.controller.reg.DemographicDetailController;
import io.mosip.registration.controller.reg.PacketHandlerController;
import io.mosip.registration.controller.reg.RegistrationPreviewController;
import io.mosip.registration.device.fp.FingerprintFacade;
import io.mosip.registration.dto.AuthenticationValidatorDTO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.biometric.BiometricDTO;
import io.mosip.registration.dto.biometric.BiometricInfoDTO;
import io.mosip.registration.dto.biometric.FaceDetailsDTO;
import io.mosip.registration.entity.UserDetail;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.scheduler.SchedulerUtil;
import io.mosip.registration.service.BaseService;
import io.mosip.registration.service.LoginService;
import io.mosip.registration.service.UserOnboardService;
import io.mosip.registration.service.config.GlobalParamService;
import io.mosip.registration.service.impl.CenterMachineReMapService;
import io.mosip.registration.service.sync.SyncStatusValidatorService;
import io.mosip.registration.service.template.TemplateService;
import io.mosip.registration.util.acktemplate.TemplateGenerator;
import javafx.animation.PauseTransition;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Base class for all controllers
 * 
 * @author Sravya Surampalli
 * @since 1.0.0
 *
 */

@Component
public class BaseController extends BaseService{

	@Autowired
	private SyncStatusValidatorService syncStatusValidatorService;
	@Autowired
	protected AuditFactory auditFactory;
	@Autowired
	private GlobalParamService globalParamService;

	@Autowired
	protected FXComponents fXComponents;

	@Autowired
	private LoginService loginService;

	@Autowired
	private DemographicDetailController demographicDetailController;
	@Autowired
	public RegistrationPreviewController registrationPreviewController;
	@Autowired
	private FingerPrintCaptureController fingerPrintCaptureController;
	@Autowired
	private BiometricExceptionController biometricExceptionController;
	@Autowired
	private IrisCaptureController irisCaptureController;
	@Autowired
	private FaceCaptureController faceCaptureController;

	@Autowired
	private TemplateService templateService;

	@Autowired
	private TemplateManagerBuilder templateManagerBuilder;

	@Autowired
	private TemplateGenerator templateGenerator;

	@Autowired
	private UserOnboardService userOnboardService;
	
	@Autowired
	private CenterMachineReMapService centerMachineReMapService;

	@Autowired
	private PacketHandlerController packetHandlerController;
	
	protected ApplicationContext applicationContext = ApplicationContext.getInstance();

	protected Scene scene;

	private List<String> pageDetails = new ArrayList<>();

	/**
	 * Instance of {@link MosipLogger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(BaseController.class);

	/**
	 * Adding events to the stage
	 * 
	 * @return
	 */
	protected Stage getStage() {
		EventHandler<Event> event = new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				SchedulerUtil.setCurrentTimeToStartTime();
			}
		};
		fXComponents.getStage().addEventHandler(EventType.ROOT, event);
		return fXComponents.getStage();
	}

	protected void loadScreen(String screen) throws IOException {
		Parent createRoot = BaseController.load(getClass().getResource(screen),
				applicationContext.getApplicationLanguageBundle());
		getScene(createRoot);
	}

	protected Scene getScene(Parent borderPane) {
		scene = fXComponents.getScene();
		if (scene == null) {
			scene = new Scene(borderPane);
			fXComponents.setScene(scene);
		}
		scene.setRoot(borderPane);
		fXComponents.getStage().setScene(scene);
		scene.getStylesheets().add(
				ClassLoader.getSystemClassLoader().getResource(RegistrationConstants.CSS_FILE_PATH).toExternalForm());
		return scene;
	}

	/**
	 * Loading FXML files along with beans
	 * 
	 * @return
	 */
	public static <T> T load(URL url) throws IOException {
		clearDeviceOnboardingContext();
		FXMLLoader loader = new FXMLLoader(url, ApplicationContext.applicationLanguageBundle());
		loader.setControllerFactory(Initialization.getApplicationContext()::getBean);
		return loader.load();
	}

	/**
	 * Loading FXML files along with beans
	 * 
	 * @return
	 */
	public static <T> T load(URL url, ResourceBundle resource) throws IOException {
		FXMLLoader loader = new FXMLLoader(url, resource);
		loader.setControllerFactory(Initialization.getApplicationContext()::getBean);
		return loader.load();
	}

	/**
	 * 
	 * /* Alert creation with specified title, header, and context
	 * 
	 * @param title     alert title
	 * @param alertType type of alert
	 * @param header    alert header
	 * @param context   alert context
	 */
	protected void generateAlert(String title, String context) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.setContentText(context);
		alert.setTitle(RegistrationUIConstants.getMessageLanguageSpecific(title));
		alert.setGraphic(null);
		alert.setResizable(true);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.showAndWait();
	}

	/**
	 * 
	 * /* Alert creation with specified title, header, and context
	 * 
	 * @param alertType type of alert
	 * @param header    alert header
	 * @param context   alert context
	 */
	protected void generateAlertLanguageSpecific(String title, String context) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.setContentText(RegistrationUIConstants.getMessageLanguageSpecific(context));
		alert.setTitle(RegistrationUIConstants.getMessageLanguageSpecific(title));
		alert.setGraphic(null);
		alert.setResizable(true);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.showAndWait();
	}

	/**
	 * 
	 * /* Alert creation with specified context
	 * 
	 * @param alertType type of alert
	 * @param context   alert context
	 */
	protected void generateAlert(Pane parentPane, String id, String context, String isConsolidated,
			StringBuilder validationMessage) {
		if (id.matches("dd|mm|yyyy|ddLocalLanguage|mmLocalLanguage|yyyyLocalLanguage")) {
			id = RegistrationConstants.DOB;
			parentPane = (Pane) parentPane.getParent().getParent();
		}
		if(id.contains("ontype")) {
			id=id.replaceAll("_ontype", "");
		}
		if(id.equals("mobileNo")) {
			System.out.println("Hello");
		}	
		if (RegistrationConstants.DISABLE.equalsIgnoreCase(isConsolidated)) {
			Label label = ((Label) (parentPane
					.lookup(RegistrationConstants.HASH + id + RegistrationConstants.MESSAGE)));
			if (!label.isVisible()) {
				label.setText(context);
				label.setVisible(true);
			}
		} else {
			validationMessage.append("* ").append(context).append(System.getProperty("line.separator"));
		}
	}

	protected ResponseDTO validateSyncStatus() {

		return syncStatusValidatorService.validateSyncStatus();
	}

	/**
	 * Validating Id for Screen Authorization
	 * 
	 * @param screenId the screenId
	 * @return boolean
	 */
	protected boolean validateScreenAuthorization(String screenId) {

		return SessionContext.userContext().getAuthorizationDTO().getAuthorizationScreenId().contains(screenId);
	}

	/**
	 * Regex validation with specified field and pattern
	 * 
	 * @param field        concerned field
	 * @param regexPattern pattern need to checked
	 */
	protected boolean validateRegex(Control field, String regexPattern) {
		if (field instanceof TextField) {
			if (!((TextField) field).getText().matches(regexPattern))
				return true;
		} else {
			if (field instanceof PasswordField) {
				if (!((PasswordField) field).getText().matches(regexPattern))
					return true;
			}
		}
		return false;
	}

	/**
	 * {@code autoCloseStage} is to close the stage automatically by itself for a
	 * configured amount of time
	 * 
	 * @param stage
	 */
	protected void autoCloseStage(Stage stage) {
		PauseTransition delay = new PauseTransition(Duration.seconds(5));
		delay.setOnFinished(event -> stage.close());
		delay.play();
	}

	/**
	 * {@code globalParams} is to retrieve required global config parameters for
	 * login from config table
	 */
	protected void getGlobalParams() {
		applicationContext.setApplicationMap(globalParamService.getGlobalParams());
	}

	/**
	 * Get the details form Global Param Map is the values existed or not
	 * 
	 * @return Response DTO
	 */
	protected ResponseDTO getSyncConfigData() {
		return globalParamService.synchConfigData(false);
	}

	/**
	 * 
	 * Opens the home page screen
	 * 
	 * @throws RegBaseCheckedException
	 * 
	 */
	public void goToHomePage() {
		try {
			BaseController.load(getClass().getResource(RegistrationConstants.HOME_PAGE));
			clearOnboardData();
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - REDIRECTHOME - BASE_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_HOME_PAGE);
		} catch (RuntimeException runtimException) {
			LOGGER.error("REGISTRATION - REDIRECTHOME - BASE_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
					runtimException.getMessage() + ExceptionUtils.getStackTrace(runtimException));
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_HOME_PAGE);
		}
	}

	/**
	 * 
	 * Opens the home page screen
	 * 
	 * @throws ioException
	 * 
	 */
	public void loadLoginScreen() {
		try {
			Parent root = load(getClass().getResource(RegistrationConstants.INITIAL_PAGE));
			getStage().setScene(getScene(root));
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - REDIRECLOGIN - BASE_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
		}
	}

	/**
	 * This method is used clear all the new registration related mapm values and
	 * navigates to the home page
	 * 
	 * 
	 */
	public void goToHomePageFromRegistration() {
		LOGGER.info(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Going to home page");

		clearRegistrationData();
		clearOnboardData();
		goToHomePage();
	}

	@SuppressWarnings("unchecked")
	protected void clearRegistrationData() {

		SessionContext.map().remove(RegistrationConstants.REGISTRATION_ISEDIT);
		SessionContext.map().remove(RegistrationConstants.REGISTRATION_PANE1_DATA);
		SessionContext.map().remove(RegistrationConstants.REGISTRATION_PANE2_DATA);
		SessionContext.map().remove(RegistrationConstants.REGISTRATION_AGE_DATA);
		SessionContext.map().remove(RegistrationConstants.REGISTRATION_DATA);
		SessionContext.map().remove(RegistrationConstants.IS_Child);
		SessionContext.map().remove("dd");
		SessionContext.map().remove("mm");
		SessionContext.map().remove("yyyy");
		SessionContext.map().remove("toggleAgeOrDob");
		SessionContext.map().remove("demographicDetail");
		SessionContext.map().remove("documentScan");
		SessionContext.map().remove("fingerPrintCapture");
		SessionContext.map().remove("biometricException");
		SessionContext.map().remove("faceCapture");
		SessionContext.map().remove("irisCapture");
		SessionContext.map().remove("registrationPreview");
		SessionContext.map().remove("operatorAuthenticationPane");
		SessionContext.map().remove(RegistrationConstants.OLD_BIOMETRIC_EXCEPTION);
		SessionContext.map().remove(RegistrationConstants.NEW_BIOMETRIC_EXCEPTION);

		clearAllValues();
		
		SessionContext.userMap().remove(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION);
		SessionContext.map().remove(RegistrationConstants.DUPLICATE_FINGER);

		((Map<String, Map<String, Boolean>>) ApplicationContext.map().get(RegistrationConstants.REGISTRATION_MAP))
				.get(RegistrationConstants.BIOMETRIC_EXCEPTION).put(RegistrationConstants.VISIBILITY,
						(boolean) ApplicationContext.map().get("biometricExceptionFlow"));
	}

	protected void clearOnboardData() {
		SessionContext.map().put(RegistrationConstants.ONBOARD_USER_UPDATE, false);
		SessionContext.map().put(RegistrationConstants.ONBOARD_USER, false);
		SessionContext.map().remove(RegistrationConstants.USER_ONBOARD_DATA);
		SessionContext.map().remove(RegistrationConstants.OLD_BIOMETRIC_EXCEPTION);
		SessionContext.map().remove(RegistrationConstants.NEW_BIOMETRIC_EXCEPTION);
	}

	public static FXMLLoader loadChild(URL url) {
		FXMLLoader loader = new FXMLLoader(url, ApplicationContext.applicationLanguageBundle());
		loader.setControllerFactory(Initialization.getApplicationContext()::getBean);
		return loader;
	}

	/**
	 * Gets the finger print status.
	 *
	 * @param PrimaryStage the primary stage
	 * @return the finger print status
	 */
	public void updateAuthenticationStatus() {

	}

	/**
	 * Scans documents
	 *
	 * @param popupStage the stage
	 */
	public void scan(Stage popupStage) {

	}

	/**
	 * This method is for saving the Applicant Image and Exception Image which are
	 * captured using webcam
	 * 
	 * @param capturedImage BufferedImage that is captured using webcam
	 * @param imageType     Type of image that is to be saved
	 */
	public void saveApplicantPhoto(BufferedImage capturedImage, String imageType) {
		// will be implemented in the derived class.
	}

	/**
	 * This method used to clear the images that are captured using webcam
	 * 
	 * @param imageType Type of image that is to be cleared
	 */
	public void clearPhoto(String imageType) {
		// will be implemented in the derived class.
	}

	private static void clearDeviceOnboardingContext() {
		if (SessionContext.isSessionContextAvailable()) {
			SessionContext.map().remove(RegistrationConstants.ONBOARD_DEVICES_MAP);
			SessionContext.map().remove(RegistrationConstants.ONBOARD_DEVICES_MAP_UPDATED);
		}
	}

	/**
	 * it will wait for the mentioned time to get the capture image from Bio Device.
	 * 
	 * @param count
	 * @param waitTimeInSec
	 * @param fingerprintFacade
	 */
	protected void waitToCaptureBioImage(int count, int waitTimeInSec, FingerprintFacade fingerprintFacade) {
		int counter = 0;
		while (counter < 5) {
			if (!RegistrationConstants.EMPTY.equals(fingerprintFacade.getMinutia())
					|| !RegistrationConstants.EMPTY.equals(fingerprintFacade.getErrorMessage())) {
				break;
			} else {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException interruptedException) {
					LOGGER.error("FINGERPRINT_AUTHENTICATION_CONTROLLER - ERROR_SCANNING_FINGER", APPLICATION_NAME,
							APPLICATION_ID,
							interruptedException.getMessage() + ExceptionUtils.getStackTrace(interruptedException));
				}
			}
			counter++;
		}
	}

	protected Image convertBytesToImage(byte[] imageBytes) {
		Image image = null;
		if (imageBytes != null) {
			image = new Image(new ByteArrayInputStream(imageBytes));
		}
		return image;
	}

	protected Timer onlineAvailabilityCheck() {
		Timer timer = new Timer();
		fXComponents.setTimer(timer);
		return timer;
	}

	protected void stopTimer() {
		if (fXComponents.getTimer() != null) {
			fXComponents.getTimer().cancel();
			fXComponents.getTimer().purge();
			fXComponents.setTimer(null);
		}
	}

	public Timer getTimer() {
		return fXComponents.getTimer() == null ? onlineAvailabilityCheck() : fXComponents.getTimer();
	}

	/**
	 * to validate the password in case of password based authentication
	 */
	protected String validatePwd(String username, String password) {

		LOGGER.info("REGISTRATION - OPERATOR_AUTHENTICATION", APPLICATION_NAME, APPLICATION_ID, "Validating Password");

		if (password.isEmpty()) {
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.PWORD_FIELD_EMPTY);
			return RegistrationUIConstants.PWORD_FIELD_EMPTY;
		} else {
			String hashPassword = null;

			// password hashing
			if (!(password.isEmpty())) {
				byte[] bytePassword = password.getBytes();
				hashPassword = HMACUtils.digestAsPlainText(HMACUtils.generateHash(bytePassword));
			}

			AuthenticationValidatorDTO authenticationValidatorDTO = new AuthenticationValidatorDTO();
			authenticationValidatorDTO.setUserId(username);
			authenticationValidatorDTO.setPassword(hashPassword);

			if (validatePassword(authenticationValidatorDTO).equals(RegistrationConstants.PWD_MATCH)) {
				return RegistrationConstants.SUCCESS;
			}
			return RegistrationConstants.FAILURE;
		}
	}

	/**
	 * to validate the password and send appropriate message to display
	 * 
	 * @param authenticationValidatorDTO - DTO which contains the username and
	 *                                   password entered by the user
	 * @return appropriate message after validation
	 */
	private String validatePassword(AuthenticationValidatorDTO authenticationValidatorDTO) {
		LOGGER.info("REGISTRATION - OPERATOR_AUTHENTICATION", APPLICATION_NAME, APPLICATION_ID,
				"Validating credentials using database");

		UserDetail userDetail = loginService.getUserDetail(authenticationValidatorDTO.getUserId());
		// TO DO-- Yet to implement SSHA512
		if ("E2E488ECAF91897D71BEAC2589433898414FEEB140837284C690DFC26707B262"
				.equals(authenticationValidatorDTO.getPassword())) {
			return RegistrationConstants.PWD_MATCH;
		} else {
			return RegistrationConstants.PWD_MISMATCH;
		}
	}

	protected void clearAllValues() {
		if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			((BiometricDTO) SessionContext.map().get(RegistrationConstants.USER_ONBOARD_DATA))
					.setOperatorBiometricDTO(createBiometricInfoDTO());
			biometricExceptionController.clearSession();
			fingerPrintCaptureController.clearFingerPrintDTO();
			irisCaptureController.clearIrisData();
			faceCaptureController.clearPhoto(RegistrationConstants.APPLICANT_IMAGE);
		} else {
			if (SessionContext.map().get(RegistrationConstants.REGISTRATION_DATA) != null) {
				((RegistrationDTO) SessionContext.map().get(RegistrationConstants.REGISTRATION_DATA)).getBiometricDTO()
						.setApplicantBiometricDTO(createBiometricInfoDTO());
				biometricExceptionController.clearSession();
				fingerPrintCaptureController.clearFingerPrintDTO();
				irisCaptureController.clearIrisData();
				faceCaptureController.clearPhoto(RegistrationConstants.APPLICANT_IMAGE);
				faceCaptureController.clearPhoto(RegistrationConstants.EXCEPTION_IMAGE);
			}
			
		}
	}

	protected BiometricInfoDTO createBiometricInfoDTO() {
		BiometricInfoDTO biometricInfoDTO = new BiometricInfoDTO();
		biometricInfoDTO.setBiometricExceptionDTO(new ArrayList<>());
		biometricInfoDTO.setFingerprintDetailsDTO(new ArrayList<>());
		biometricInfoDTO.setIrisDetailsDTO(new ArrayList<>());
		FaceDetailsDTO obj = new FaceDetailsDTO();
		biometricInfoDTO.setFaceDetailsDTO(obj);
		return biometricInfoDTO;
	}

	protected Writer getNotificationTemplate(String templateCode) {
		RegistrationDTO registrationDTO = getRegistrationDTOFromSession();
		Writer writeNotificationTemplate = new StringWriter();
		try {
			// get the data for notification template
			String platformLanguageCode = ApplicationContext.applicationLanguage();
			String notificationTemplate = templateService.getHtmlTemplate(templateCode,
					platformLanguageCode);
			if (notificationTemplate != null && !notificationTemplate.isEmpty()) {
				// generate the notification template
				writeNotificationTemplate = templateGenerator.generateNotificationTemplate(notificationTemplate,
						registrationDTO, templateManagerBuilder);
			}

		} catch (RegBaseUncheckedException regBaseUncheckedException) {
			LOGGER.error("REGISTRATION - UI - GENERATE_NOTIFICATION", APPLICATION_NAME, APPLICATION_ID,
					regBaseUncheckedException.getMessage() + ExceptionUtils.getStackTrace(regBaseUncheckedException));
		}
		return writeNotificationTemplate;
	}
	
	protected RegistrationDTO getRegistrationDTOFromSession() {
		return (RegistrationDTO) SessionContext.map().get(RegistrationConstants.REGISTRATION_DATA);
	}

	/**
	 * to return to the next page based on the current page and action for User
	 * Onboarding
	 * 
	 * @param currentPage - Id of current Anchorpane
	 * @param action      - action to be performed previous/next
	 * @param currentPage - Id of current Anchorpane
	 * @param action      - action to be performed previous/next
	 * 
	 * @return id of next Anchorpane
	 */

	@SuppressWarnings("unchecked")
	protected String getOnboardPageDetails(String currentPage, String action) {

		LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID,
				"Updating OnBoard based on visibility and returning next page details");

		return getReturnPage((List<String>) ApplicationContext.map().get(RegistrationConstants.ONBOARD_LIST),
				currentPage, action);
	}

	/**
	 * to return to the next page based on the current page and action for New
	 * Registration
	 * 
	 * @param currentPage - Id of current Anchorpane
	 * @param action      - action to be performed previous/next
	 * @param currentPage - Id of current Anchorpane
	 * @param action      - action to be performed previous/next
	 * 
	 * @return id of next Anchorpane
	 */
	@SuppressWarnings("unchecked")
	protected String getPageDetails(String currentPage, String action) {

		LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID,
				"Updating RegistrationMap based on visibility");

		for (Map.Entry<String, Map<String, Boolean>> entry : ((Map<String, Map<String, Boolean>>) ApplicationContext
				.map().get(RegistrationConstants.REGISTRATION_MAP)).entrySet()) {
			if (entry.getValue().get(RegistrationConstants.VISIBILITY)) {
				pageDetails.add(entry.getKey());
			}
		}

		LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID, "Returning Next page details");

		return getReturnPage(pageDetails, currentPage, action);

	}

	/**
	 * to return to the next page based on the current page and action
	 * 
	 * @param pageList    - List of Anchorpane Ids
	 * @param currentPage - Id of current Anchorpane
	 * @param action      - action to be performed previous/next
	 * @param pageList    - List of Anchorpane Ids
	 * @param currentPage - Id of current Anchorpane
	 * @param action      - action to be performed previous/next
	 * 
	 * @return id of next Anchorpane
	 */
	private String getReturnPage(List<String> pageList, String currentPage, String action) {

		LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID,
				"Fetching the next page based on action");

		String returnPage = "";

		if (action.equalsIgnoreCase(RegistrationConstants.NEXT)) {

			LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID,
					"Fetching the next page based from list of ids for Next action");

			returnPage = pageList.get((pageList.indexOf(currentPage)) + 1);
		} else if (action.equalsIgnoreCase(RegistrationConstants.PREVIOUS)) {

			LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID,
					"Fetching the next page based from list of ids for Previous action");

			returnPage = pageList.get((pageList.indexOf(currentPage)) - 1);
		}

		if (returnPage.equalsIgnoreCase(RegistrationConstants.REGISTRATION_PREVIEW)) {

			LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID,
					"Invoking Save Detail before redirecting to Preview");

			demographicDetailController.saveDetail();
			registrationPreviewController.setUpPreviewContent();

			LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID,
					"Details saved and content of preview is set");
		} else if (returnPage.equalsIgnoreCase(RegistrationConstants.ONBOARD_USER_SUCCESS)) {

			LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID, "Validating User Onboard data");

			ResponseDTO response = userOnboardService
					.validate((BiometricDTO) SessionContext.map().get(RegistrationConstants.USER_ONBOARD_DATA));
			if (response != null && response.getErrorResponseDTOs() != null
					&& response.getErrorResponseDTOs().get(0) != null) {

				LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID,
						"Displaying Alert if validation is not success");

				generateAlertLanguageSpecific(RegistrationConstants.ERROR, response.getErrorResponseDTOs().get(0).getMessage());
			} else if (response != null && response.getSuccessResponseDTO() != null) {

				LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID,
						"User Onboard is success and clearing Onboard data");
			
				popupStatge(RegistrationUIConstants.USER_ONBOARD_SUCCESS,RegistrationConstants.ONBOARD_IMG_PATH, RegistrationConstants.ONBOARD_STYLE_CLASS);
				clearOnboardData();
				goToHomePage();

				LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID,
						"Redirecting to Home page after success onboarding");
			}
			returnPage = "";
		}

		LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID,
				"Returning the corresponding next page based on given action");

		pageDetails.clear();
		return returnPage;
	}

	/**
	 * to navigate to the next page based on the current page
	 * 
	 * @param pageId     - Parent Anchorpane where other panes are included
	 * @param notTosShow - Id of Anchorpane which has to be hidden
	 * @param show       - Id of Anchorpane which has to be shown
	 * @param pageId     - Parent Anchorpane where other panes are included
	 * @param notTosShow - Id of Anchorpane which has to be hidden
	 * @param show       - Id of Anchorpane which has to be shown
	 * 
	 */
	protected void getCurrentPage(Pane pageId, String notTosShow, String show) {

		LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID, "Navigating to next page");

		if (notTosShow != null) {
			((Pane) pageId.lookup("#" + notTosShow)).setVisible(false);
		}
		if (show != null) {
			((Pane) pageId.lookup("#" + show)).setVisible(true);
		}

		LOGGER.info(LoggerConstants.LOG_REG_BASE, APPLICATION_NAME, APPLICATION_ID, "Navigated to next page");
	}

	/**
	 * to calculate the time for re-capture since last capture time
	 * 
	 * @param imageType the type of image that is selected to capture
	 * @param imageType the type of image that is selected to capture
	 */
	public void calculateRecaptureTime(String imageType) {
		// will be implemented in the derived class.
	}

	/**
	 * Checks if the machine is remapped to another center and starts the subsequent
	 * processing accordingly
	 */
	public boolean isMachineRemapProcessStarted() {

		Boolean isRemapped = centerMachineReMapService.isMachineRemapped();
		if (isRemapped) {

			String message = RegistrationUIConstants.REMAP_NO_ACCESS_MESSAGE;

			if (isPacketsPendingForEOD()) {
				message += "\n" + RegistrationUIConstants.REMAP_EOD_PROCESS_MESSAGE;
			}
			message += "\n" + RegistrationUIConstants.REMAP_CLICK_OK;
			generateAlert(RegistrationConstants.ALERT_INFORMATION, message);

			packetHandlerController.reMapProgressIndicator.progressProperty().bind(service.progressProperty());

			if (!service.isRunning())
				service.start();

			service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent t) {
					service.reset();
					packetHandlerController.reMapProgressIndicator.setVisible(false);
					if (!centerMachineReMapService.isPacketsPendingForProcessing()) {
						generateAlert(RegistrationConstants.ALERT_INFORMATION,
								RegistrationUIConstants.REMAP_PROCESS_SUCCESS);
					} else {
						generateAlert(RegistrationConstants.ALERT_INFORMATION,
								RegistrationUIConstants.REMAP_PROCESS_STILL_PENDING);
					}

				}
			});

		}
		return isRemapped;
	}
	
	Service<String> service = new Service<String>() {
		@Override
		protected Task<String> createTask() {
			return new Task<String>() {

				@Override
				protected String call() {

					packetHandlerController.reMapProgressIndicator.setVisible(true);
					for (int i = 1; i <= 4; i++) {
						/* starts the remap process */
						centerMachineReMapService.handleReMapProcess(i);
						this.updateProgress(i, 4);
					}

					return null;
				}
			};
		}
	};

	protected boolean isPacketsPendingForEOD() {
			
		return centerMachineReMapService.isPacketsPendingForEOD();
	}

	public void popupStatge(String messgae, String imageUrl, String styleClass) {
		Stage primaryStage = new Stage();
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.setX(540);
		primaryStage.setY(85);
		AnchorPane anchorPane = new AnchorPane();
		anchorPane.setPrefWidth(250);
		anchorPane.setPrefHeight(40);
		Label label = new Label();
		label.setText(messgae);
		label.setLayoutX(60);
		label.setLayoutY(9);
		label.getStyleClass().clear();
		label.getStyleClass().addAll(styleClass, "label");
		Image img = new Image(imageUrl);
		ImageView imageView = new ImageView();
		imageView.setImage(img);
		imageView.setLayoutX(25);
		imageView.setLayoutY(8);
		imageView.setFitHeight(25);
		imageView.setFitWidth(25);
		primaryStage.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (primaryStage.isShowing()) {
					primaryStage.close();
				}
			}
		});
		anchorPane.getChildren().add(imageView);
		anchorPane.getChildren().add(label);
		Scene scene = new Scene(anchorPane);
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		scene.getStylesheets().add(classLoader.getResource(RegistrationConstants.CSS_FILE_PATH).toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.initModality(Modality.WINDOW_MODAL);
		primaryStage.initOwner(fXComponents.getStage());
		primaryStage.show();
	}

	/**
	 * Create alert with given title, header and context
	 * 
	 * @param alertType type of alert
	 * @param title     alert's title
	 * @param header    alert's header
	 * @param context   alert's context
	 * @return alert
	 */
	protected Alert createAlert(AlertType alertType, String title, String header, String context) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(context);
		alert.setGraphic(null);
		alert.setResizable(true);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

		return alert;
	}
	
	protected void updateUINMethodFlow() {
		if ((Boolean) SessionContext.userContext().getUserMap().get(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION)
				|| getRegistrationDTOFromSession().getSelectionListDTO().isBiometricException()
						&& (Boolean) SessionContext.userContext().getUserMap()
								.get(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION)) {
			SessionContext.map().put("biometricException", true);
		} else if ((getRegistrationDTOFromSession().getSelectionListDTO().isBiometricFingerprint()
				&& !getRegistrationDTOFromSession().getSelectionListDTO().isBiometricException())
				|| (getRegistrationDTOFromSession().getSelectionListDTO().isBiometricFingerprint()
						&& getRegistrationDTOFromSession().getSelectionListDTO().isBiometricException()
						&& !(Boolean) SessionContext.userContext().getUserMap()
								.get(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION))) {
			SessionContext.map().put("fingerPrintCapture", true);
		} else if ((getRegistrationDTOFromSession().getSelectionListDTO().isBiometricIris()
				&& !getRegistrationDTOFromSession().getSelectionListDTO().isBiometricException())
				|| (getRegistrationDTOFromSession().getSelectionListDTO().isBiometricIris()
						&& getRegistrationDTOFromSession().getSelectionListDTO().isBiometricException()
						&& !(Boolean) SessionContext.userContext().getUserMap()
								.get(RegistrationConstants.TOGGLE_BIO_METRIC_EXCEPTION))) {
			SessionContext.map().put("irisCapture", true);
		} else if(!RegistrationConstants.DISABLE.equalsIgnoreCase(String.valueOf(
				ApplicationContext.map().get(RegistrationConstants.FACE_DISABLE_FLAG)))){
			SessionContext.map().put("faceCapture", true);
		}else {
			SessionContext.map().put("registrationPreview", true);
			registrationPreviewController.setUpPreviewContent();
		}
	}

}
