package io.mosip.registration.controller;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;
import static io.mosip.registration.constants.RegistrationConstants.URL;
import static io.mosip.registration.constants.RegistrationExceptions.REG_UI_LOGIN_INITIALSCREEN_NULLPOINTER_EXCEPTION;
import static io.mosip.registration.constants.RegistrationExceptions.REG_UI_LOGIN_SCREEN_NULLPOINTER_EXCEPTION;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.machinezoo.sourceafis.FingerprintTemplate;

import MFS100.FingerData;
import MFS100.MFS100;
import MFS100.MFS100Event;
import io.mosip.kernel.core.spi.logger.MosipLogger;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationExceptions;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.LoginUserDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.entity.RegistrationUserDetail;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.scheduler.SchedulerUtil;
import io.mosip.registration.service.LoginService;
import io.mosip.registration.util.biometric.FingerprintProvider;
import io.mosip.registration.util.healthcheck.RegistrationSystemPropertiesChecker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Class for loading Login screen with Username and password
 * 
 * @author Sravya Surampalli
 * @since 1.0.0
 *
 */
@Controller
public class LoginController extends BaseController implements Initializable, MFS100Event {

	@FXML
	private TextField userId;

	@FXML
	private TextField password;

	@FXML
	private Button submit;

	@FXML
	private Button getOTP;

	@FXML
	private Button resend;

	@FXML
	private Label otpValidity;

	@FXML
	private Label fingerprint;

	@FXML
	private ImageView fingerImage;

	@Autowired
	private LoginService loginService;

	@Autowired
	private SchedulerUtil schedulerUtil;

	private FingerprintProvider fingerprintProvider = new FingerprintProvider();

	@Value("${FINGER_PRINT_SCORE}")
	private long fingerPrintScore;

	@Value("${TIME_OUT_INTERVAL}")
	private long timeoutInterval;

	@Value("${IDEAL_TIME}")
	private long idealTime;

	@Value("${REFRESHED_LOGIN_TIME}")
	private long refreshedLoginTime;

	@Value("${otp_validity_in_mins}")
	private long otpValidityImMins;

	@Value("${QUALITY_SCORE}")
	private int qualityScore;

	@Value("${CAPTURE_TIME_OUT}")
	private int captureTimeOut;

	private static Scene scene;

	/**
	 * Instance of {@link MosipLogger}
	 */
	private static final MosipLogger LOGGER = AppConfig.getLogger(LoginController.class);

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		otpValidity.setText("Valid for " + otpValidityImMins + " minutes");
	}

	public static Scene getScene() {
		return scene;
	}

	private MFS100 fpDevice = new MFS100(this, "");

	/**
	 * To get the Sequence of which Login screen to be displayed
	 * 
	 * @return String loginMode
	 * @throws RegBaseCheckedException
	 */
	public String loadInitialScreen(Stage primaryStage) throws RegBaseCheckedException {

		LOGGER.debug("REGISTRATION - LOGIN_MODE - LOGIN_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
				"Retrieve Login mode");

		BaseController.stage = primaryStage;
		String loginMode = null;

		try {
			Map<String, Object> userLoginMode = loginService.getModesOfLogin();

			if (userLoginMode.containsKey((RegistrationConstants.LOGIN_INITIAL_VAL))) {
				loginMode = String.valueOf(userLoginMode.get(RegistrationConstants.LOGIN_INITIAL_VAL));
			}

			// To maintain the login mode sequence
			SessionContext.getInstance().setMapObject(userLoginMode);
			SessionContext.getInstance().getMapObject().put(RegistrationConstants.LOGIN_INITIAL_SCREEN, loginMode);

			LOGGER.debug("REGISTRATION - LOGIN_MODE - LOGIN_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
					"Retrieved corresponding Login mode");

			BorderPane loginRoot = BaseController.load(getClass().getResource(RegistrationConstants.INITIAL_PAGE));

			if (loginMode == null) {
				AnchorPane loginType = BaseController.load(getClass().getResource(RegistrationConstants.ERROR_PAGE));
				loginRoot.setCenter(loginType);
			} else {
				loadLoginScreen(loginMode);
			}

			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			scene = new Scene(loginRoot, 950, 630);
			scene.getStylesheets().add(loader.getResource(RegistrationConstants.CSS_FILE_PATH).toExternalForm());

			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch (IOException | RuntimeException exception) {
			
			LOGGER.error("REGISTRATION - LOGIN_MODE - LOGIN_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, REG_UI_LOGIN_INITIALSCREEN_NULLPOINTER_EXCEPTION.getErrorMessage());
			
			generateAlert(RegistrationConstants.ALERT_ERROR, AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
					REG_UI_LOGIN_INITIALSCREEN_NULLPOINTER_EXCEPTION.getErrorMessage());
		}

		return loginMode;
	}

	/**
	 * 
	 * Validating User credentials on Submit
	 * 
	 * @return String loginMode
	 * @throws RegBaseCheckedException
	 */
	public void validateCredentials(ActionEvent event) {

		LOGGER.debug("REGISTRATION - LOGIN_MODE_PWORD - LOGIN_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
				"Validating Credentials entered through UI");

		if (userId.getText().isEmpty() && password.getText().isEmpty()) {
			generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE, AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
					RegistrationConstants.MISSING_MANDATOTY_FIELDS, RegistrationConstants.CREDENTIALS_FIELD_EMPTY);
		} else if (userId.getText().isEmpty()) {
			generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE, AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
					RegistrationConstants.MISSING_MANDATOTY_FIELDS, RegistrationConstants.USERNAME_FIELD_EMPTY);
		} else if (password.getText().isEmpty()) {
			generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE, AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
					RegistrationConstants.MISSING_MANDATOTY_FIELDS, RegistrationConstants.PWORD_FIELD_EMPTY);
		} else {

			String hashPassword = null;

			// password hashing
			if (!(password.getText().isEmpty())) {
				byte[] bytePassword = password.getText().getBytes();
				hashPassword = HMACUtils.digestAsPlainText(HMACUtils.generateHash(bytePassword));
			}
			LoginUserDTO userDTO = new LoginUserDTO();
			userDTO.setUserId(userId.getText());
			userDTO.setPassword(hashPassword);
			// Server connection check
			boolean serverStatus = getConnectionCheck(userDTO);
			boolean offlineStatus = false;

			if (!serverStatus) {
				LOGGER.debug("REGISTRATION - USER_PASSWORD - LOGIN_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
						"Retrieving User Password from database");

				offlineStatus = loginService.validateUserPassword(userDTO.getUserId(), hashPassword);
				if (!offlineStatus) {
					generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE,
							AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
							RegistrationConstants.LOGIN_INFO_MESSAGE, RegistrationConstants.INCORRECT_PWORD);
				}
			}
			if (serverStatus || offlineStatus) {
				if (validateUserStatus(userId.getText())) {

					LOGGER.debug("REGISTRATION - LOGIN_PWORD - LOGIN_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
							"Validating user status");

					generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE,
							AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
							RegistrationConstants.LOGIN_INFO_MESSAGE, RegistrationConstants.BLOCKED_USER_ERROR);
				} else {
					try {

						LOGGER.debug("REGISTRATION - LOGIN_PWORD - LOGIN_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
								"Loading next login screen");

						SessionContext sessionContext = SessionContext.getInstance();
						loadLoginAfterLogout(sessionContext, RegistrationConstants.LOGIN_METHOD_PWORD);
						loadNextScreen(sessionContext);

					} catch (IOException | RuntimeException | RegBaseCheckedException exception) {
						
						LOGGER.error("REGISTRATION - LOGIN_PWORD - LOGIN_CONTROLLER", APPLICATION_NAME,
								APPLICATION_ID, REG_UI_LOGIN_SCREEN_NULLPOINTER_EXCEPTION.getErrorMessage());
						
						generateAlert(RegistrationConstants.ALERT_ERROR,
								AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
								RegistrationConstants.LOGIN_FAILURE,
								REG_UI_LOGIN_SCREEN_NULLPOINTER_EXCEPTION.getErrorMessage());
					}
				}
			}
		}

	}

	/**
	 * Generate OTP based on EO username
	 * 
	 * @param event
	 * @throws RegBaseCheckedException
	 */
	@FXML
	public void generateOtp(ActionEvent event) {

		if (!userId.getText().isEmpty()) {
			// Response obtained from server
			ResponseDTO responseDTO = null;

			// Service Layer interaction
			responseDTO = loginService.getOTP(userId.getText());

			if (responseDTO.getSuccessResponseDTO() != null) {
				// Enable submit button
				changeToOTPSubmitMode();

				// Generate alert to show OTP
				SuccessResponseDTO successResponseDTO = responseDTO.getSuccessResponseDTO();
				generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE, AlertType.valueOf(successResponseDTO.getCode()),
						RegistrationConstants.OTP_INFO_MESSAGE, successResponseDTO.getMessage());

			} else if (responseDTO.getErrorResponseDTOs() != null) {
				// Generate Alert to show INVALID USERNAME
				ErrorResponseDTO errorResponseDTO = responseDTO.getErrorResponseDTOs().get(0);
				generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE, AlertType.valueOf(errorResponseDTO.getCode()),
						RegistrationConstants.OTP_INFO_MESSAGE, errorResponseDTO.getMessage());

			}

		} else {
			// Generate Alert to show username field was empty
			generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE, AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
					RegistrationConstants.OTP_INFO_MESSAGE, RegistrationConstants.USERNAME_FIELD_EMPTY);

		}

	}

	/**
	 * Validate User through username and otp
	 * 
	 * @param event
	 */
	@FXML
	public void validateUser(ActionEvent event) {
		if (!password.getText().isEmpty() && password.getText().length() != 3) {

			ResponseDTO responseDTO = null;

			responseDTO = loginService.validateOTP(userId.getText(), password.getText());

			if (responseDTO != null) {
				if (responseDTO.getSuccessResponseDTO() != null) {
					// // Validating User status
					if (validateUserStatus(userId.getText())) {
						generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE,
								AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
								RegistrationConstants.LOGIN_INFO_MESSAGE, RegistrationConstants.BLOCKED_USER_ERROR);
					} else {
						try {
							SessionContext sessionContext = SessionContext.getInstance();
							loadLoginAfterLogout(sessionContext, RegistrationConstants.LOGIN_METHOD_PWORD);
							loadNextScreen(sessionContext);
						} catch (IOException | RuntimeException | RegBaseCheckedException exception) {
							
							LOGGER.error("REGISTRATION - LOGIN_OTP - LOGIN_CONTROLLER", APPLICATION_NAME,
									APPLICATION_ID, REG_UI_LOGIN_SCREEN_NULLPOINTER_EXCEPTION.getErrorMessage());
							
							generateAlert(RegistrationConstants.ALERT_ERROR,
									AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
									RegistrationConstants.LOGIN_FAILURE,
									RegistrationExceptions.REG_UI_LOGIN_SCREEN_NULLPOINTER_EXCEPTION.getErrorMessage());
						}

					}

				} else {
					// Generate invalid otp alert
					ErrorResponseDTO errorResponseDTO = responseDTO.getErrorResponseDTOs().get(0);
					generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE,
							AlertType.valueOf(errorResponseDTO.getCode()), RegistrationConstants.LOGIN_FAILURE,
							errorResponseDTO.getMessage());
				}
			}

		} else if (userId.getText().length() == 3) {
			generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE, AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
					RegistrationConstants.OTP_INFO_MESSAGE, RegistrationConstants.USERNAME_FIELD_ERROR);
		} else {
			generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE, AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
					RegistrationConstants.OTP_INFO_MESSAGE, RegistrationConstants.OTP_FIELD_EMPTY);
		}
	}

	public void validateFingerPrint(ActionEvent event) {
		
		LOGGER.debug("REGISTRATION - LOGIN_BIO - LOGIN_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "Validating Credentials for Biometric login");
		
		if (userId.getText().isEmpty()) {
			generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE, AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
					RegistrationConstants.MISSING_MANDATOTY_FIELDS, RegistrationConstants.USERNAME_FIELD_EMPTY);
		} else {
			
			LOGGER.debug("REGISTRATION - LOGIN_BIO - LOGIN_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, "Initializing FingerPrint device");
			
			if (fpDevice.Init() != 0) {
				generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE,
						AlertType.valueOf(RegistrationConstants.ALERT_ERROR), RegistrationConstants.DEVICE_INFO_MESSAGE,
						RegistrationConstants.DEVICE_FP_NOT_FOUND);
			} else {
				
				LOGGER.debug("REGISTRATION - LOGIN_BIO - LOGIN_CONTROLLER", APPLICATION_NAME,
						APPLICATION_ID, "Start capturing FingerPrint");
				
				if (fpDevice.StartCapture(qualityScore, captureTimeOut, false) != 0) {
					generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE,
							AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
							RegistrationConstants.DEVICE_INFO_MESSAGE, fpDevice.GetLastError());
				}
			}
		}
	}

	/**
	 * Checking server status
	 * 
	 * @param LoginUserDTO
	 *            the UserDTO object
	 * @return boolean
	 */
	private boolean getConnectionCheck(LoginUserDTO userObj) {

		HttpEntity<LoginUserDTO> loginEntity = new HttpEntity<>(userObj);
		ResponseEntity<String> tokenId = null;
		boolean serverStatus = false;

		try {
			tokenId = new RestTemplate().exchange(URL, HttpMethod.POST, loginEntity, String.class);
			if (tokenId.getStatusCode().is2xxSuccessful()) {
				serverStatus = true;
			}
		} catch (RestClientException resourceAccessException) {
			
			LOGGER.error("REGISTRATION - SERVER_CONNECTION_CHECK", APPLICATION_NAME, APPLICATION_ID,
					resourceAccessException.getMessage());
		}
		return serverStatus;
	}

	/**
	 * Mode of login with set of fields enabling and disabling
	 */
	private void changeToOTPSubmitMode() {
		submit.setDisable(false);
		getOTP.setVisible(false);
		resend.setVisible(true);
	}

	/**
	 * Enable OTP login specific attributes
	 */
	private void enableOTP() {
		password.clear();
		password.setPromptText("Enter OTP");
		otpValidity.setVisible(true);
		getOTP.setVisible(true);
		fingerprint.setVisible(false);
		fingerImage.setVisible(false);
		getOTP.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				generateOtp(event);
			}
		});
		submit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				validateUser(event);
			}
		});
	}

	/**
	 * Enable PWD login specific attributes
	 */
	private void enablePWD() {
		password.clear();
		password.setPromptText("RO Password");
		otpValidity.setVisible(false);
		getOTP.setVisible(false);
		fingerprint.setVisible(false);
		fingerImage.setVisible(false);
		submit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				validateCredentials(event);
			}
		});
	}

	/**
	 * Enable BIO login specific attributes
	 */
	private void enableFingerPrint() {
		password.setVisible(false);
		otpValidity.setVisible(false);
		getOTP.setVisible(false);
		fingerprint.setVisible(true);
		fingerImage.setVisible(true);
		submit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				validateFingerPrint(event);
			}
		});
	}

	/**
	 * Load login screen depending on Loginmode
	 * 
	 * @param loginMode
	 *            login screen to be loaded
	 */
	public void loadLoginScreen(String loginMode) {
		switch (loginMode) {
		case RegistrationConstants.LOGIN_METHOD_OTP:
			enableOTP();
			break;
		case RegistrationConstants.LOGIN_METHOD_PWORD:
			enablePWD();
			break;
		case RegistrationConstants.LOGIN_METHOD_BIO:
			enableFingerPrint();
			break;
		default:
			enablePWD();
		}
	}

	/**
	 * Validating user status
	 * 
	 * @param userId
	 *            the userId
	 * @return boolean
	 */
	private boolean validateUserStatus(String userId) {
		RegistrationUserDetail userDetail = loginService.getUserDetail(userId);
		
		LOGGER.debug("REGISTRATION - USER_STATUS - LOGIN_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "Validating User Status");
		
		return userDetail.getUserStatus() != null
				&& userDetail.getUserStatus().equalsIgnoreCase(RegistrationConstants.BLOCKED);
	}

	/**
	 * Capturing Finger print
	 * 
	 * @param status
	 *            the status
	 * @param errorCode
	 *            the errorCode
	 * @param errorMsg
	 *            the errorMsg
	 * @param fingerData
	 *            the MFS100.FingerData
	 */
	@Override
	public void OnCaptureCompleted(boolean status, int errorCode, String errorMsg, FingerData fingerData) {
		
		LOGGER.debug("REGISTRATION - LOGIN_BIO - LOGIN_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "After capturing Fingerprint");
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				RegistrationUserDetail detail = loginService.getUserDetail(userId.getText());
				if (fingerData != null) {
					FingerprintTemplate fingerprintTemplate = new FingerprintTemplate().convert(fingerData.ISOTemplate());
					String minutia = fingerprintTemplate.serialize();
					
					LOGGER.debug("REGISTRATION - LOGIN_BIO - LOGIN_CONTROLLER", APPLICATION_NAME,
							APPLICATION_ID, "Validating Fingerprint with minutia");
					
					if (validateBiometric(minutia, detail)) {
						if (detail.getUserStatus() != null
								&& detail.getUserStatus().equalsIgnoreCase(RegistrationConstants.BLOCKED)) {
							generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE,
									AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
									RegistrationConstants.LOGIN_INFO_MESSAGE, RegistrationConstants.BLOCKED_USER_ERROR);
						} else {
							try {
								SessionContext sessionContext = SessionContext.getInstance();
								loadLoginAfterLogout(sessionContext, RegistrationConstants.LOGIN_METHOD_PWORD);
								loadNextScreen(sessionContext);
							} catch (IOException | RuntimeException | RegBaseCheckedException exception) {
								
								LOGGER.error("REGISTRATION - LOGIN_BIO - LOGIN_CONTROLLER", APPLICATION_NAME,
										APPLICATION_ID, REG_UI_LOGIN_SCREEN_NULLPOINTER_EXCEPTION.getErrorMessage());
								
								generateAlert(RegistrationConstants.ALERT_ERROR,
										AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
										RegistrationConstants.LOGIN_FAILURE,
										REG_UI_LOGIN_SCREEN_NULLPOINTER_EXCEPTION.getErrorMessage());
							}
						}
					} else {
						generateAlert(RegistrationConstants.LOGIN_ALERT_TITLE,
								AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
								RegistrationConstants.LOGIN_INFO_MESSAGE, RegistrationConstants.FINGER_PRINT_MATCH);
					}
				}
			}
		});
	}

	/**
	 * Preview finger print
	 * 
	 * @param previewImage
	 *            the MFS100.FingerData
	 */
	@Override
	public void OnPreview(FingerData previewImage) {

	}

	/**
	 * Validating User role and Machine mapping during login
	 * 
	 * @param userId
	 *            entered userId
	 * @throws RegBaseCheckedException
	 */
	private boolean setInitialLoginInfo(String userId) throws RegBaseCheckedException {
		RegistrationUserDetail userDetail = loginService.getUserDetail(userId);
		String authInfo = null;
		List<String> roleList = new ArrayList<>();

		userDetail.getUserRole().forEach(roleCode -> {
			if (roleCode.getIsActive()) {
				roleList.add(String.valueOf(roleCode.getRegistrationUserRoleID().getRoleCode()));
			}
		});

		LOGGER.debug("REGISTRATION - ROLES_MACHINE_MAPPING - LOGIN_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "Validating roles and machine and center mapping");
		
		// Checking roles
		if (roleList.isEmpty()) {
			authInfo = RegistrationConstants.ROLES_EMPTY;
		} else if (roleList.contains(RegistrationConstants.ADMIN_ROLE)) {
			authInfo = RegistrationConstants.SUCCESS_MSG;
		} else {
			// checking for machine mapping
			if (!getCenterMachineStatus(userDetail)) {
				authInfo = RegistrationConstants.MACHINE_MAPPING;
			} else {
				authInfo = RegistrationConstants.SUCCESS_MSG;
			}
		}
		return setSessionContext(authInfo, userDetail, roleList);
	}

	/**
	 * Fetching and Validating machine and center id
	 * 
	 * @param userDetail
	 *            the userDetail
	 * @return boolean
	 * @throws RegBaseCheckedException
	 */
	private boolean getCenterMachineStatus(RegistrationUserDetail userDetail) {
		List<String> machineList = new ArrayList<>();
		List<String> centerList = new ArrayList<>();
		
		LOGGER.debug("REGISTRATION - MACHINE_MAPPING - LOGIN_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "Validating User machine and center mapping");
		
		userDetail.getUserMachineMapping().forEach(machineMapping -> {
			if (machineMapping.getIsActive()) {
				machineList.add(machineMapping.getUserMachineMappingId().getMachineID());
				centerList.add(machineMapping.getUserMachineMappingId().getCentreID());
			}
		});
		return machineList.contains(RegistrationSystemPropertiesChecker.getMachineId())
				&& centerList.contains(userDetail.getCntrId());
	}

	/**
	 * Setting values for Session context and User context and Initial info for
	 * Login
	 * 
	 * @param userId
	 *            entered userId
	 * @param userDetail
	 *            userdetails
	 * @param roleList
	 *            list of user roles
	 * @throws RegBaseCheckedException
	 */
	private boolean setSessionContext(String authInfo, RegistrationUserDetail userDetail, List<String> roleList) {
		boolean result = false;
		
		LOGGER.debug("REGISTRATION - ROLES_MACHINE_MAPPING - LOGIN_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "Validating roles and machine and center mapping");
		
		if (authInfo != null && authInfo.equals(RegistrationConstants.ROLES_EMPTY)) {
			generateAlert(RegistrationConstants.AUTHORIZATION_ALERT_TITLE,
					AlertType.valueOf(RegistrationConstants.ALERT_ERROR), RegistrationConstants.LOGIN_FAILURE,
					RegistrationConstants.ROLES_EMPTY_ERROR);
		} else if (authInfo != null && authInfo.equals(RegistrationConstants.MACHINE_MAPPING)) {
			generateAlert(RegistrationConstants.AUTHORIZATION_ALERT_TITLE,
					AlertType.valueOf(RegistrationConstants.ALERT_ERROR), RegistrationConstants.LOGIN_FAILURE,
					RegistrationConstants.MACHINE_MAPPING_ERROR);
		} else if (authInfo != null && authInfo.equalsIgnoreCase(RegistrationConstants.SUCCESS_MSG)) {
			SessionContext sessionContext = SessionContext.getInstance();

			LOGGER.debug("REGISTRATION - ROLES_MACHINE_MAPPING - LOGIN_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, "Setting values for session context and user context");
			
			sessionContext.setLoginTime(new Date());
			sessionContext.setRefreshedLoginTime(refreshedLoginTime);
			sessionContext.setIdealTime(idealTime);
			sessionContext.setTimeoutInterval(timeoutInterval);

			SessionContext.UserContext userContext = sessionContext.getUserContext();
			userContext.setUserId(userId.getText());
			userContext.setName(userDetail.getName());
			userContext.setRoles(roleList);
			userContext
					.setRegistrationCenterDetailDTO(loginService.getRegistrationCenterDetails(userDetail.getCntrId()));

			String userRole = !userContext.getRoles().isEmpty() ? userContext.getRoles().get(0) : null;
			userContext.setAuthorizationDTO(loginService.getScreenAuthorizationDetails(userRole));
			result = true;
		}
		return result;
	}

	/**
	 * Loading login screen after logout with multiple screens in case of
	 * multifactor authentication
	 * 
	 * @param sessionContext
	 *            the sessionContext
	 * @param loginModeToLoad
	 *            the loginMode to load
	 */
	private void loadLoginAfterLogout(SessionContext sessionContext, String loginModeToLoad) {

		LOGGER.debug("REGISTRATION - LOGIN_MODE - LOGIN_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
				"Resetting login sequence to the Session context after log out");

		if (sessionContext.getMapObject() == null) {
			Map<String, Object> userLoginMode = loginService.getModesOfLogin();
			sessionContext.setMapObject(userLoginMode);
			sessionContext.getMapObject().put(RegistrationConstants.LOGIN_INITIAL_SCREEN, loginModeToLoad);
		}
	}

	/**
	 * Loading next login screen in case of multifactor authentication
	 * 
	 * @param sessionContext
	 *            the sessionContext
	 */
	private void loadNextScreen(SessionContext sessionContext) throws IOException, RegBaseCheckedException {

		int counter = (int) sessionContext.getMapObject().get(RegistrationConstants.LOGIN_SEQUENCE);
		counter++;
		if (sessionContext.getMapObject().containsKey(String.valueOf(counter))) {

			LOGGER.debug("REGISTRATION - LOGIN_MODE - LOGIN_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
					"Loading next login screen in case of multifactor authentication");

			String mode = sessionContext.getMapObject().get(String.valueOf(counter)).toString();
			sessionContext.getMapObject().remove(String.valueOf(counter));
			loadLoginScreen(mode);
		} else {
			if (setInitialLoginInfo(userId.getText())) {

				LOGGER.debug("REGISTRATION - LOGIN_MODE - LOGIN_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
						"Loading Home screen");

				schedulerUtil.startSchedulerUtil();
				BaseController.load(getClass().getResource(RegistrationConstants.HOME_PAGE));
			}
		}
	}

	/**
	 * Validating User Biometrics using Minutia
	 * 
	 * @param minutia
	 *            minutia of fingerprint
	 * @param registrationUserDetail
	 *            user dtails
	 * @return boolean
	 */
	private boolean validateBiometric(String minutia, RegistrationUserDetail registrationUserDetail) {

		LOGGER.debug("REGISTRATION - LOGIN - BIOMETRICS", APPLICATION_NAME, APPLICATION_ID,
				"Validation of fingerprint through Minutia");

		return registrationUserDetail.getUserBiometric().stream()
				.anyMatch(bio -> fingerprintProvider.scoreCalculator(minutia, bio.getBioMinutia()) > fingerPrintScore);
	}

}
