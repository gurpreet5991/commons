
package io.mosip.registration.controller.auth;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.xml.sax.SAXException;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.AuditReferenceIdTypes;
import io.mosip.registration.constants.Components;
import io.mosip.registration.constants.LoggerConstants;
import io.mosip.registration.constants.LoginMode;
import io.mosip.registration.constants.ProcessNames;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.Initialization;
import io.mosip.registration.controller.RestartController;
import io.mosip.registration.controller.reg.HomeController;
import io.mosip.registration.controller.reg.PacketHandlerController;
import io.mosip.registration.controller.reg.Validations;
import io.mosip.registration.device.face.FaceFacade;
import io.mosip.registration.device.fp.FingerprintFacade;
import io.mosip.registration.device.fp.MosipFingerprintProvider;
import io.mosip.registration.device.iris.IrisFacade;
import io.mosip.registration.dto.AuthenticationValidatorDTO;
import io.mosip.registration.dto.LoginUserDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.dto.biometric.FaceDetailsDTO;
import io.mosip.registration.dto.biometric.FingerprintDetailsDTO;
import io.mosip.registration.dto.biometric.IrisDetailsDTO;
import io.mosip.registration.entity.UserDetail;
import io.mosip.registration.entity.UserMachineMapping;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.scheduler.SchedulerUtil;
import io.mosip.registration.service.AuthenticationService;
import io.mosip.registration.service.LoginService;
import io.mosip.registration.service.MasterSyncService;
import io.mosip.registration.service.UserDetailService;
import io.mosip.registration.service.UserMachineMappingService;
import io.mosip.registration.service.UserOnboardService;
import io.mosip.registration.service.config.GlobalParamService;
import io.mosip.registration.service.config.JobConfigurationService;
import io.mosip.registration.service.impl.PublicKeySyncImpl;
import io.mosip.registration.update.RegistrationUpdate;
import io.mosip.registration.util.common.OTPManager;
import io.mosip.registration.util.common.PageFlow;
import io.mosip.registration.util.healthcheck.RegistrationAppHealthCheckUtil;
import io.mosip.registration.util.healthcheck.RegistrationSystemPropertiesChecker;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Class for loading Login screen with Username and password
 * 
 * @author Sravya Surampalli
 * @since 1.0.0
 *
 */
@Controller
public class LoginController extends BaseController implements Initializable {
	/**
	 * Instance of {@link Logger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(LoginController.class);

	@FXML
	private AnchorPane userIdPane;

	@FXML
	private AnchorPane credentialsPane;

	@FXML
	private AnchorPane otpPane;

	@FXML
	private AnchorPane fingerprintPane;

	@FXML
	private AnchorPane irisPane;

	@FXML
	private AnchorPane facePane;

	@FXML
	private AnchorPane errorPane;

	@FXML
	private TextField userId;

	@FXML
	private TextField password;

	@FXML
	private TextField otp;

	@FXML
	private Button submit;

	@FXML
	private Button otpSubmit;

	@FXML
	private Button getOTP;

	@FXML
	private Button resend;

	@FXML
	private Label otpValidity;

	@Autowired
	private LoginService loginService;

	@Autowired
	private AuthenticationService authService;

	@Autowired
	private OTPManager otpGenerator;

	@Autowired
	private SchedulerUtil schedulerUtil;

	@Autowired
	private UserOnboardService userOnboardService;

	@Autowired
	private FingerprintFacade fingerprintFacade;

	@Autowired
	private IrisFacade irisFacade;

	@Autowired
	private FaceFacade faceFacade;

	@Autowired
	private GlobalParamService globalParamService;

	@Autowired
	private Validations validations;

	@Autowired
	private PageFlow pageFlow;

	@Autowired
	private MasterSyncService masterSyncService;

	@Autowired
	private UserDetailService userDetailService;
	@Autowired
	private RestartController restartController;

	@FXML
	private ProgressIndicator progressIndicator;

	private Service<List<String>> taskService;

	private List<String> loginList = new ArrayList<>();

	@Autowired
	private JobConfigurationService jobConfigurationService;

	private boolean isInitialSetUp;

	@Autowired
	private RegistrationUpdate registrationUpdate;

	private BorderPane loginRoot;

	@Autowired
	private PacketHandlerController packetHandlerController;

	@Autowired
	private HomeController homeController;

	private Boolean isUserNewToMachine;

	@FXML
	private ProgressIndicator passwordProgressIndicator;

	@Autowired
	private UserMachineMappingService machineMappingService;

	@Autowired
	private PublicKeySyncImpl publicKeySyncImpl;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		try {
			if (RegistrationAppHealthCheckUtil.isNetworkAvailable()) {
				globalParamService.updateSoftwareUpdateStatus(registrationUpdate.hasUpdate());
			}

		} catch (IOException | ParserConfigurationException | SAXException exception) {
			LOGGER.error(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
					exception.getMessage() + ExceptionUtils.getStackTrace(exception));
		}

		try {
			isInitialSetUp = RegistrationConstants.ENABLE
					.equalsIgnoreCase(getValueFromApplicationContext(RegistrationConstants.INITIAL_SETUP));

			stopTimer();
			password.textProperty().addListener((obsValue, oldValue, newValue) -> {
				if (newValue.length() > Integer
						.parseInt(getValueFromApplicationContext(RegistrationConstants.PWORD_LENGTH))) {
					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.PWORD_LENGTH);
				}
			});
		} catch (RuntimeException runtimeExceptionexception) {
			LOGGER.error(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
					runtimeExceptionexception.getMessage() + ExceptionUtils.getStackTrace(runtimeExceptionexception));
		}
	}

	/**
	 * To get the Sequence of which Login screen to be displayed
	 * 
	 * @param primaryStage
	 *            primary Stage
	 */
	public void loadInitialScreen(Stage primaryStage) {

		/* Save Global Param Values in Application Context's application map */
		getGlobalParams();
		ApplicationContext.loadResources();

		try {

			LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Retrieve Login mode");

			fXComponents.setStage(primaryStage);

			validations.setResourceBundle();
			loginRoot = BaseController.load(getClass().getResource(RegistrationConstants.INITIAL_PAGE));

			scene = getScene(loginRoot);
			pageFlow.getInitialPageDetails();
			Screen screen = Screen.getPrimary();
			Rectangle2D bounds = screen.getVisualBounds();
			primaryStage.setX(bounds.getMinX());
			primaryStage.setY(bounds.getMinY());
			primaryStage.setWidth(bounds.getWidth());
			primaryStage.setHeight(bounds.getHeight());
			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.show();

			if (!isInitialSetUp) {
				executePreLaunchTask(loginRoot, progressIndicator);
				jobConfigurationService.startScheduler();
			}

		} catch (IOException ioException) {
			LOGGER.error(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_LOGIN_SCREEN);
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_LOGIN_SCREEN);
		}
	}

	/**
	 * Validate user id.
	 *
	 * @param event
	 *            the event
	 */
	public void validateUserId(ActionEvent event) {

		auditFactory.audit(AuditEvent.LOGIN_AUTHENTICATE_USER_ID, Components.LOGIN, userId.getText(),
				AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Validating Credentials entered through UI");

		if (userId.getText().isEmpty()) {
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.USERNAME_FIELD_EMPTY);
		} else {

			if (isInitialSetUp) {
				initialSetUpOrNewUserLaunch();

			} else {
				try {

					UserDetail userDetail = loginService.getUserDetail(userId.getText());

					Map<String, String> centerAndMachineId = userOnboardService.getMachineCenterId();

					String centerId = centerAndMachineId.get(RegistrationConstants.USER_CENTER_ID);

					if (userDetail != null
							&& userDetail.getRegCenterUser().getRegCenterUserId().getRegcntrId().equals(centerId)) {

						isUserNewToMachine = machineMappingService.isUserNewToMachine(userId.getText())
								.getErrorResponseDTOs() != null;
						if (isUserNewToMachine) {
							initialSetUpOrNewUserLaunch();
						} else {

							ApplicationContext.map().put(RegistrationConstants.USER_CENTER_ID, centerId);

							if (userDetail.getStatusCode().equalsIgnoreCase(RegistrationConstants.BLOCKED)) {
								generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.BLOCKED_USER_ERROR);
							} else {

								// Set Dongle Serial Number in ApplicationContext Map
								for (UserMachineMapping userMachineMapping : userDetail.getUserMachineMapping()) {
									ApplicationContext.map().put(RegistrationConstants.DONGLE_SERIAL_NUMBER,
											userMachineMapping.getMachineMaster().getSerialNum());
								}

								Set<String> roleList = new LinkedHashSet<>();

								userDetail.getUserRole().forEach(roleCode -> {
									if (roleCode.getIsActive()) {
										roleList.add(String.valueOf(roleCode.getUserRoleID().getRoleCode()));
									}
								});

								LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
										"Validating roles");
								// Checking roles
								if (roleList.isEmpty() || !(roleList.contains(RegistrationConstants.OFFICER)
										|| roleList.contains(RegistrationConstants.SUPERVISOR)
										|| roleList.contains(RegistrationConstants.ADMIN_ROLE))) {
									generateAlert(RegistrationConstants.ERROR,
											RegistrationUIConstants.ROLES_EMPTY_ERROR);
								} else {

									Map<String, Object> sessionContextMap = SessionContext.getInstance().getMapObject();

									ApplicationContext.map().put(RegistrationConstants.USER_STATION_ID,
											centerAndMachineId.get(RegistrationConstants.USER_STATION_ID));

									boolean status = getCenterMachineStatus(userDetail);
									sessionContextMap.put(RegistrationConstants.ONBOARD_USER, !status);
									sessionContextMap.put(RegistrationConstants.ONBOARD_USER_UPDATE, false);
									loginList = status
											? loginService.getModesOfLogin(ProcessNames.LOGIN.getType(), roleList)
											: loginService.getModesOfLogin(ProcessNames.ONBOARD.getType(), roleList);

									String fingerprintDisableFlag = getValueFromApplicationContext(
											RegistrationConstants.FINGERPRINT_DISABLE_FLAG);
									String irisDisableFlag = getValueFromApplicationContext(
											RegistrationConstants.IRIS_DISABLE_FLAG);
									String faceDisableFlag = getValueFromApplicationContext(
											RegistrationConstants.FACE_DISABLE_FLAG);

									LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
											"Ignoring FingerPrint login if the configuration is off");

									removeLoginParam(fingerprintDisableFlag, RegistrationConstants.FINGERPRINT);
									removeLoginParam(irisDisableFlag, RegistrationConstants.IRIS);
									removeLoginParam(faceDisableFlag, RegistrationConstants.FINGERPRINT);

									String loginMode = !loginList.isEmpty()
											? loginList.get(RegistrationConstants.PARAM_ZERO)
											: null;

									LOGGER.debug(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
											"Retrieved corresponding Login mode");

									if (loginMode == null) {
										userIdPane.setVisible(false);
										errorPane.setVisible(true);
									} else {

										if ((RegistrationConstants.DISABLE.equalsIgnoreCase(fingerprintDisableFlag)
												&& RegistrationConstants.FINGERPRINT.equalsIgnoreCase(loginMode))
												|| (RegistrationConstants.DISABLE.equalsIgnoreCase(irisDisableFlag)
														&& RegistrationConstants.IRIS.equalsIgnoreCase(loginMode))
												|| (RegistrationConstants.DISABLE.equalsIgnoreCase(faceDisableFlag)
														&& RegistrationConstants.FACE.equalsIgnoreCase(loginMode))) {

											generateAlert(RegistrationConstants.ERROR,
													RegistrationUIConstants.BIOMETRIC_DISABLE_SCREEN_1.concat(
															RegistrationUIConstants.BIOMETRIC_DISABLE_SCREEN_2));

										} else {
											userIdPane.setVisible(false);
											loadLoginScreen(loginMode);
										}
									}
								}
							}
						}
					} else {
						generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.USER_MACHINE_VALIDATION_MSG);
					}
				} catch (RegBaseUncheckedException regBaseUncheckedException) {

					LOGGER.error(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
							regBaseUncheckedException.getMessage()
									+ ExceptionUtils.getStackTrace(regBaseUncheckedException));

					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_LOGIN_SCREEN);
				}
			}
		}
	}

	private void initialSetUpOrNewUserLaunch() {
		userIdPane.setVisible(false);
		loadLoginScreen(LoginMode.PASSWORD.toString());
	}

	/**
	 * 
	 * Validating User credentials on Submit
	 * 
	 * @param event
	 *            event for validating credentials
	 */
	public void validateCredentials(ActionEvent event) {

		auditFactory.audit(AuditEvent.LOGIN_WITH_PASSWORD, Components.LOGIN, userId.getText(),
				AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Validating Credentials entered through UI");

		if (isInitialSetUp || isUserNewToMachine) {
			LoginUserDTO loginUserDTO = new LoginUserDTO();
			loginUserDTO.setUserId(userId.getText());
			loginUserDTO.setPassword(password.getText());

			ApplicationContext.map().put(RegistrationConstants.USER_DTO, loginUserDTO);

			try {
				// Get Auth Token
				getAuthToken(loginUserDTO, LoginMode.PASSWORD);
				if (isInitialSetUp) {
					executePreLaunchTask(credentialsPane, passwordProgressIndicator);

				} else {
					validateUserCredentialsInLocal();
				}

				// // Execute Sync
				// executePreLaunchTask(credentialsPane, passwordProgressIndicator);

			} catch (Exception exception) {
				LOGGER.error(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, String
						.format("Exception while getting AuthZ Token --> %s", ExceptionUtils.getStackTrace(exception)));

				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_TO_GET_AUTH_TOKEN);

				loadInitialScreen(Initialization.getPrimaryStage());
			}
		} else {
			validateUserCredentialsInLocal();
		}

	}

	private void validateUserCredentialsInLocal() {
		boolean pwdValidationStatus = false;
		UserDetail userDetail = loginService.getUserDetail(userId.getText());

		if (userDetail != null) {
			// TODO: Since AuthN web-service not accepting Hash Password and SHA is not
			// implemented, getting AuthZ Token by Client ID and Secret Key

			LoginUserDTO loginUserDTO = new LoginUserDTO();
			// loginUserDTO.setUserId(userId.getText());
			// loginUserDTO.setPassword(password.getText());

			ApplicationContext.map().put(RegistrationConstants.USER_DTO, loginUserDTO);
			if (RegistrationAppHealthCheckUtil.isNetworkAvailable()) {
				try {
					serviceDelegateUtil.getAuthToken(LoginMode.CLIENTID);
				} catch (Exception exception) {
					LOGGER.error(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, String.format(
							"Exception while getting AuthZ Token --> %s", ExceptionUtils.getStackTrace(exception)));

				}
			}

			String status = validatePwd(userId.getText().toLowerCase(), password.getText());

			if (RegistrationConstants.SUCCESS.equals(status)) {
				pwdValidationStatus = validateInvalidLogin(userDetail, "");
			} else if (RegistrationConstants.FAILURE.equals(status)) {
				pwdValidationStatus = validateInvalidLogin(userDetail, RegistrationUIConstants.INCORRECT_PWORD);
			}

			if (pwdValidationStatus) {

				LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
						"Loading next login screen");
				credentialsPane.setVisible(false);
				loadNextScreen(userDetail, RegistrationConstants.PWORD);

			}
		} else {
			loadInitialScreen(Initialization.getPrimaryStage());
		}

	}

	/**
	 * Generate OTP based on EO username
	 * 
	 * @param event
	 *            event for generating OTP
	 */
	@FXML
	public void generateOtp(ActionEvent event) {

		if (userId.getText().isEmpty()) {
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.USERNAME_FIELD_EMPTY);
		} else {

			auditFactory.audit(AuditEvent.LOGIN_GET_OTP, Components.LOGIN, userId.getText(),
					AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			// Response obtained from server
			ResponseDTO responseDTO = otpGenerator.getOTP(userId.getText());

			if (responseDTO.getSuccessResponseDTO() != null) {
				// Enable submit button
				changeToOTPSubmitMode();

				// Generate alert to show OTP
				generateAlert(RegistrationConstants.ALERT_INFORMATION,
						RegistrationUIConstants.OTP_GENERATION_SUCCESS_MESSAGE);

			} else if (responseDTO.getErrorResponseDTOs() != null) {
				// Generate Alert to show INVALID USERNAME
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.OTP_GENERATION_ERROR_MESSAGE);

			}
		}
	}

	/**
	 * Validate User through username and otp
	 * 
	 * @param event
	 *            event for validating OTP
	 */
	@FXML
	public void validateOTP(ActionEvent event) {

		auditFactory.audit(AuditEvent.LOGIN_SUBMIT_OTP, Components.LOGIN, userId.getText(),
				AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		if (validations.validateTextField(otpPane, otp, otp.getId(), true)) {

			UserDetail userDetail = loginService.getUserDetail(userId.getText());

			boolean otpLoginStatus = false;

			ResponseDTO responseDTO = otpGenerator.validateOTP(userId.getText(), otp.getText());
			if (responseDTO.getSuccessResponseDTO() != null) {
				otpLoginStatus = validateInvalidLogin(userDetail, "");
			} else {
				otpLoginStatus = validateInvalidLogin(userDetail, RegistrationUIConstants.OTP_VALIDATION_ERROR_MESSAGE);
			}

			if (otpLoginStatus) {
				otpPane.setVisible(false);
				int otpExpirySeconds = Integer
						.parseInt((getValueFromApplicationContext(RegistrationConstants.OTP_EXPIRY_TIME)).trim());
				int minutes = otpExpirySeconds / 60;
				String seconds = String.valueOf(otpExpirySeconds % 60);
				seconds = seconds.length() < 2 ? "0" + seconds : seconds;
				otpValidity.setText(RegistrationUIConstants.OTP_VALIDITY + " " + minutes + ":" + seconds + " "
						+ RegistrationUIConstants.MINUTES);
				loadNextScreen(userDetail, RegistrationConstants.OTP);

			}

		}
	}

	/**
	 * Validate User through username and fingerprint
	 * 
	 * @param event
	 *            event for capturing fingerprint
	 */
	public void captureFingerPrint(ActionEvent event) {

		auditFactory.audit(AuditEvent.LOGIN_WITH_FINGERPRINT, Components.LOGIN, userId.getText(),
				AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Validating Credentials for Biometric login");

		UserDetail detail = loginService.getUserDetail(userId.getText());

		boolean bioLoginStatus = false;

		if (validateFingerPrint()) {
			bioLoginStatus = validateInvalidLogin(detail, "");
		} else {
			bioLoginStatus = validateInvalidLogin(detail, RegistrationUIConstants.FINGER_PRINT_MATCH);
		}

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Validating Fingerprint with minutia");

		if (bioLoginStatus) {
			fingerprintPane.setVisible(false);
			loadNextScreen(detail, RegistrationConstants.FINGERPRINT);
		}

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Fingerprint validation done");
	}

	/**
	 * Validate User through username and Iris
	 * 
	 * @param event
	 *            event for capturing Iris
	 */
	public void captureIris(ActionEvent event) {

		auditFactory.audit(AuditEvent.LOGIN_WITH_IRIS, Components.LOGIN, userId.getText(),
				AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Validating Biometric login with Iris");

		UserDetail detail = loginService.getUserDetail(userId.getText());

		boolean irisLoginStatus = false;

		if (validateIris()) {
			irisLoginStatus = validateInvalidLogin(detail, "");
		} else {
			irisLoginStatus = validateInvalidLogin(detail, RegistrationUIConstants.IRIS_MATCH);
		}

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Validating Iris with stored data");

		if (irisLoginStatus) {
			irisPane.setVisible(false);
			loadNextScreen(detail, RegistrationConstants.IRIS);
		}

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Iris validation done");
	}

	/**
	 * Validate User through username and face
	 * 
	 * @param event
	 *            event to capture face
	 */
	public void captureFace(ActionEvent event) {

		auditFactory.audit(AuditEvent.LOGIN_WITH_FACE, Components.LOGIN, userId.getText(),
				AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Validating Biometric login with Iris");

		UserDetail detail = loginService.getUserDetail(userId.getText());

		boolean faceLoginStatus = false;

		if (validateFace()) {
			faceLoginStatus = validateInvalidLogin(detail, "");
		} else {
			faceLoginStatus = validateInvalidLogin(detail, RegistrationUIConstants.FACE_MATCH);
		}

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Validating Face with stored data");

		if (faceLoginStatus) {
			facePane.setVisible(false);
			loadNextScreen(detail, RegistrationConstants.FACE);
		}

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Face validation done");
	}

	/**
	 * Mode of login with set of fields enabling and disabling
	 */
	private void changeToOTPSubmitMode() {
		submit.setDisable(false);
		otpSubmit.setDisable(false);
		getOTP.setVisible(false);
		resend.setVisible(true);
		otpValidity.setVisible(true);
	}

	/**
	 * Load login screen depending on Loginmode
	 * 
	 * @param loginMode
	 *            login screen to be loaded
	 */
	public void loadLoginScreen(String loginMode) {

		switch (loginMode.toUpperCase()) {
		case RegistrationConstants.OTP:
			otpPane.setVisible(true);
			break;
		case RegistrationConstants.PWORD:
			credentialsPane.setVisible(true);
			break;
		case RegistrationConstants.FINGERPRINT:
			fingerprintPane.setVisible(true);
			break;
		case RegistrationConstants.IRIS:
			irisPane.setVisible(true);
			break;
		case RegistrationConstants.FACE:
			facePane.setVisible(true);
			break;
		default:
			credentialsPane.setVisible(true);
		}

		if (!loginList.isEmpty()) {
			loginList.remove(RegistrationConstants.PARAM_ZERO);
		}
	}

	/**
	 * Validating User role and Machine mapping during login
	 * 
	 * @param userId
	 *            entered userId
	 * @throws RegBaseCheckedException
	 */
	private boolean setInitialLoginInfo(String userId) {
		UserDetail userDetail = loginService.getUserDetail(userId);
		String authInfo = RegistrationConstants.SUCCESS;
		List<String> roleList = new ArrayList<>();

		userDetail.getUserRole().forEach(roleCode -> {
			if (roleCode.getIsActive()) {
				roleList.add(String.valueOf(roleCode.getUserRoleID().getRoleCode()));
			}
		});

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Validating roles and machine and center mapping");
		// Checking roles
		if (roleList.contains(RegistrationConstants.ADMIN_ROLE)) {
			authInfo = RegistrationConstants.SUCCESS;
		} else if (!(roleList.contains(RegistrationConstants.SUPERVISOR)
				|| roleList.contains(RegistrationConstants.OFFICER))) {
			authInfo = RegistrationConstants.ROLES_EMPTY;
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
	private boolean getCenterMachineStatus(UserDetail userDetail) {
		List<String> machineList = new ArrayList<>();
		List<String> centerList = new ArrayList<>();

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Validating User machine and center mapping");

		userDetail.getUserMachineMapping().forEach(machineMapping -> {
			if (machineMapping.getIsActive()) {
				machineList.add(machineMapping.getMachineMaster().getMacAddress());
				centerList.add(machineMapping.getUserMachineMappingId().getCentreID());
			}
		});
		return machineList.contains(RegistrationSystemPropertiesChecker.getMachineId())
				&& centerList.contains(userDetail.getRegCenterUser().getRegCenterUserId().getRegcntrId());
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
	private boolean setSessionContext(String authInfo, UserDetail userDetail, List<String> roleList) {
		boolean result = false;

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Validating roles and machine and center mapping");

		if (authInfo.equals(RegistrationConstants.ROLES_EMPTY)) {
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.ROLES_EMPTY_ERROR);
		} else if (authInfo.equalsIgnoreCase(RegistrationConstants.SUCCESS)) {
			SessionContext sessionContext = SessionContext.getInstance();

			LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
					"Setting values for session context and user context");

			long refreshedLoginTime = Long
					.parseLong(getValueFromApplicationContext(RegistrationConstants.REFRESHED_LOGIN_TIME));
			long idealTime = Long.parseLong(getValueFromApplicationContext(RegistrationConstants.IDEAL_TIME));

			sessionContext.setLoginTime(new Date());
			sessionContext.setRefreshedLoginTime(refreshedLoginTime);
			sessionContext.setIdealTime(idealTime);

			SessionContext.UserContext userContext = sessionContext.getUserContext();
			userContext.setUserId(userId.getText());
			userContext.setName(userDetail.getName());
			userContext.setRoles(roleList);
			userContext.setRegistrationCenterDetailDTO(loginService.getRegistrationCenterDetails(
					userDetail.getRegCenterUser().getRegCenterUserId().getRegcntrId(),
					ApplicationContext.applicationLanguage()));
			userContext.setAuthorizationDTO(loginService.getScreenAuthorizationDetails(roleList));
			userContext.setUserMap(new HashMap<String, Object>());
			result = true;
		}
		return result;
	}

	/**
	 * Loading next login screen in case of multifactor authentication
	 * 
	 * @param userDetail
	 *            the userDetail
	 * @param loginMode
	 *            the loginMode
	 */
	private void loadNextScreen(UserDetail userDetail, String loginMode) {

		if (!loginList.isEmpty()) {

			LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
					"Loading next login screen in case of multifactor authentication");

			loadLoginScreen(loginList.get(RegistrationConstants.PARAM_ZERO));

		} else {

			if (setInitialLoginInfo(userId.getText())) {

				auditFactory.audit(AuditEvent.NAV_HOME, Components.LOGIN, userId.getText(),
						AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

				try {

					LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Loading Home screen");
					schedulerUtil.startSchedulerUtil();
					loginList.clear();

					BaseController.load(getClass().getResource(RegistrationConstants.HOME_PAGE));
					// to add events to the stage
					getStage();

					userDetail.setLastLoginMethod(loginMode);
					userDetail.setLastLoginDtimes(Timestamp.valueOf(DateUtils.getUTCCurrentDateTime()));
					userDetail.setUnsuccessfulLoginCount(RegistrationConstants.PARAM_ZERO);

					loginService.updateLoginParams(userDetail);
				} catch (IOException ioException) {

					LOGGER.error(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
							ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));

					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_LOGIN_SCREEN);
				} catch (RegBaseCheckedException regBaseCheckedException) {

					LOGGER.error(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
							regBaseCheckedException.getMessage()
									+ ExceptionUtils.getStackTrace(regBaseCheckedException));

					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_LOGIN_SCREEN);

				}
			}
		}
	}

	/**
	 * Validating User Biometrics using Minutia
	 * 
	 * @return boolean
	 */
	private boolean validateFingerPrint() {

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Initializing FingerPrint device");

		MosipFingerprintProvider fingerPrintConnector = fingerprintFacade
				.getFingerprintProviderFactory(getValueFromApplicationContext(RegistrationConstants.PROVIDER_NAME));

		if (fingerPrintConnector.captureFingerprint(
				Integer.parseInt(getValueFromApplicationContext(RegistrationConstants.QUALITY_SCORE)),
				Integer.parseInt(getValueFromApplicationContext(RegistrationConstants.CAPTURE_TIME_OUT)),
				"") != RegistrationConstants.PARAM_ZERO) {

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.DEVICE_FP_NOT_FOUND);

			return false;
		} else {
			// Thread to wait until capture the bio image/ minutia from FP. based on the
			// error code or success code the respective action will be taken care.
			waitToCaptureBioImage(5, 2000, fingerprintFacade);

			LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Fingerprint scan done");

			fingerPrintConnector.uninitFingerPrintDevice();

			LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
					"Validation of fingerprint through Minutia");

			boolean fingerPrintStatus = false;

			if (RegistrationConstants.EMPTY.equals(fingerprintFacade.getMinutia())) {

				AuthenticationValidatorDTO authenticationValidatorDTO = new AuthenticationValidatorDTO();
				List<FingerprintDetailsDTO> fingerprintDetailsDTOs = new ArrayList<>();
				FingerprintDetailsDTO fingerprintDetailsDTO = new FingerprintDetailsDTO();
				fingerprintDetailsDTO.setFingerPrint(fingerprintFacade.getIsoTemplate());
				fingerprintDetailsDTOs.add(fingerprintDetailsDTO);

				authenticationValidatorDTO.setFingerPrintDetails(fingerprintDetailsDTOs);
				authenticationValidatorDTO.setUserId(userId.getText());
				authenticationValidatorDTO.setAuthValidationType(RegistrationConstants.VALIDATION_TYPE_FP_SINGLE);
				fingerPrintStatus = authService.authValidator(RegistrationConstants.FINGERPRINT,
						authenticationValidatorDTO);

			} else if (!RegistrationConstants.EMPTY.equals(fingerprintFacade.getErrorMessage())) {
				if (fingerprintFacade.getErrorMessage().equalsIgnoreCase(RegistrationConstants.FP_TIMEOUT)) {
					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.FP_DEVICE_TIMEOUT);
				} else {
					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.FP_DEVICE_ERROR);
				}
			}
			return fingerPrintStatus;
		}

	}

	/**
	 * Validating User Biometrics using Iris
	 * 
	 * @return boolean
	 */
	private boolean validateIris() {

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Scanning Iris");

		AuthenticationValidatorDTO authenticationValidatorDTO = new AuthenticationValidatorDTO();
		List<IrisDetailsDTO> irisDetailsDTOs = new ArrayList<>();
		IrisDetailsDTO irisDetailsDTO = new IrisDetailsDTO();
		irisDetailsDTO.setIris(irisFacade.captureIris());
		irisDetailsDTOs.add(irisDetailsDTO);
		authenticationValidatorDTO.setUserId(userId.getText());
		authenticationValidatorDTO.setIrisDetails(irisDetailsDTOs);

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Iris scan done");

		return authService.authValidator(RegistrationConstants.IRIS, authenticationValidatorDTO);
	}

	/**
	 * Validating User Biometrics using Face
	 * 
	 * @return boolean
	 */
	private boolean validateFace() {

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Scanning Face");
		AuthenticationValidatorDTO authenticationValidatorDTO = new AuthenticationValidatorDTO();
		FaceDetailsDTO faceDetailsDTO = new FaceDetailsDTO();
		faceDetailsDTO.setFace(faceFacade.captureFace());
		authenticationValidatorDTO.setUserId(userId.getText());
		authenticationValidatorDTO.setFaceDetail(faceDetailsDTO);

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Face scan done");

		return authService.authValidator(RegistrationConstants.FACE, authenticationValidatorDTO);
	}

	/**
	 * Validating invalid number of login attempts
	 * 
	 * @param userDetail
	 *            user details
	 * @param userId
	 *            entered userId
	 * @return boolean
	 */
	private boolean validateInvalidLogin(UserDetail userDetail, String errorMessage) {

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "Fetching invalid login params");

		int loginCount = userDetail.getUnsuccessfulLoginCount() != null
				? userDetail.getUnsuccessfulLoginCount().intValue()
				: RegistrationConstants.PARAM_ZERO;

		int invalidLoginCount = Integer
				.parseInt(getValueFromApplicationContext(RegistrationConstants.INVALID_LOGIN_COUNT));

		int invalidLoginTime = Integer
				.parseInt(getValueFromApplicationContext(RegistrationConstants.INVALID_LOGIN_TIME));

		Timestamp loginTime = userDetail.getUserlockTillDtimes();

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID, "validating invalid login params");

		if (validateLoginTime(loginCount, invalidLoginCount, loginTime, invalidLoginTime)) {

			loginCount = RegistrationConstants.PARAM_ZERO;
			userDetail.setUnsuccessfulLoginCount(RegistrationConstants.PARAM_ZERO);

			loginService.updateLoginParams(userDetail);

		}

		String unlockMessage = String.format("%s %s %s %s %s", RegistrationUIConstants.USER_ACCOUNT_LOCK_MESSAGE_NUMBER,
				String.valueOf(invalidLoginCount), RegistrationUIConstants.USER_ACCOUNT_LOCK_MESSAGE,
				String.valueOf(invalidLoginTime), RegistrationUIConstants.USER_ACCOUNT_LOCK_MESSAGE_MINUTES);

		if (loginCount >= invalidLoginCount) {

			LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
					"validating login count and time ");

			if (TimeUnit.MILLISECONDS.toMinutes(loginTime.getTime() - System.currentTimeMillis()) > invalidLoginTime) {

				userDetail.setUnsuccessfulLoginCount(RegistrationConstants.PARAM_ONE);

				loginService.updateLoginParams(userDetail);

			} else {

				generateAlert(RegistrationConstants.ERROR, unlockMessage);
				loadLoginScreen();

			}
			return false;

		} else {
			if (!errorMessage.isEmpty()) {

				LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
						"updating login count and time for invalid login attempts");
				loginCount = loginCount + RegistrationConstants.PARAM_ONE;
				userDetail.setUserlockTillDtimes(Timestamp.valueOf(DateUtils.getUTCCurrentDateTime()));
				userDetail.setUnsuccessfulLoginCount(loginCount);

				loginService.updateLoginParams(userDetail);

				if (loginCount >= invalidLoginCount) {

					generateAlert(RegistrationConstants.ERROR, unlockMessage);
					loadLoginScreen();

				} else {

					generateAlert(RegistrationConstants.ERROR, errorMessage);

				}
				return false;
			}
			return true;
		}
	}

	/**
	 * Validating login time and count
	 * 
	 * @param loginCount
	 *            number of invalid attempts
	 * @param invalidLoginCount
	 *            count from global param
	 * @param loginTime
	 *            login time from table
	 * @param invalidLoginTime
	 *            login time from global param
	 * @return boolean
	 */
	private boolean validateLoginTime(int loginCount, int invalidLoginCount, Timestamp loginTime,
			int invalidLoginTime) {

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Comparing timestamps in case of invalid login attempts");

		return (loginCount >= invalidLoginCount
				&& TimeUnit.MILLISECONDS.toMinutes(Timestamp.valueOf(DateUtils.getUTCCurrentDateTime()).getTime()
						- loginTime.getTime()) > invalidLoginTime);
	}

	private void executePreLaunchTask(Pane pane, ProgressIndicator progressIndicator) {

		progressIndicator.setVisible(true);
		pane.setDisable(true);

		/**
		 * This anonymous service class will do the pre application launch task
		 * progress.
		 * 
		 */
		taskService = new Service<List<String>>() {
			@Override
			protected Task<List<String>> createTask() {
				return /**
						 * @author SaravanaKumar
						 *
						 */
				new Task<List<String>>() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see javafx.concurrent.Task#call()
					 */
					@Override
					protected List<String> call() {

						LOGGER.info("REGISTRATION - HANDLE_PACKET_UPLOAD_START - PACKET_UPLOAD_CONTROLLER",
								APPLICATION_NAME, APPLICATION_ID, "Handling all the packet upload activities");

						List<String> val = new LinkedList<>();
						publicKeySyncImpl.getPublicKey(RegistrationConstants.JOB_TRIGGER_POINT_USER);

						ResponseDTO responseDTO = getSyncConfigData();
						SuccessResponseDTO successResponseDTO = responseDTO.getSuccessResponseDTO();
						if (successResponseDTO != null && successResponseDTO.getOtherAttributes() != null) {
							val.add(RegistrationConstants.RESTART);
						}
						ResponseDTO masterResponseDTO = masterSyncService.getMasterSync(
								RegistrationConstants.OPT_TO_REG_MDS_J00001,
								RegistrationConstants.JOB_TRIGGER_POINT_USER);

						ResponseDTO userResponseDTO = userDetailService
								.save(RegistrationConstants.JOB_TRIGGER_POINT_USER);

						if (((masterResponseDTO.getErrorResponseDTOs() != null
								|| userResponseDTO.getErrorResponseDTOs() != null)
								|| responseDTO.getErrorResponseDTOs() != null)) {
							val.add(RegistrationConstants.FAILURE);
						} else {
							val.add(RegistrationConstants.SUCCESS);
						}
						return val;

					}
				};
			}
		};

		progressIndicator.progressProperty().bind(taskService.progressProperty());
		taskService.start();
		taskService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {

				if (taskService.getValue().contains(RegistrationConstants.FAILURE)) {
					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.SYNC_CONFIG_DATA_FAILURE);
					if (isInitialSetUp) {
						loadInitialScreen(Initialization.getPrimaryStage());
						return;
					}
				} else if (taskService.getValue().contains(RegistrationConstants.RESTART)
						|| taskService.getValue().contains(RegistrationConstants.SUCCESS)) {

					if (isInitialSetUp) {
						// update initial set up flag

						globalParamService.update(RegistrationConstants.INITIAL_SETUP, RegistrationConstants.DISABLE);

					}
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							generateAlert(RegistrationConstants.SUCCESS.toUpperCase(),
									RegistrationUIConstants.RESTART_APPLICATION);
							restartController.restart();
						}
					});
				}
				pane.setDisable(false);
				progressIndicator.setVisible(false);
			}
		});

	}

	/**
	 * This method will remove the loginmethod from list
	 * 
	 * @param disableFlag
	 *            configuration flag
	 * @param loginMethod
	 *            login method
	 */
	private void removeLoginParam(String disableFlag, String loginMethod) {

		loginList.removeIf(login -> loginList.size() > 1 && RegistrationConstants.DISABLE.equalsIgnoreCase(disableFlag)
				&& login.equalsIgnoreCase(loginMethod));

		LOGGER.info(LoggerConstants.LOG_REG_LOGIN, APPLICATION_NAME, APPLICATION_ID,
				"Ignoring login method if the configuration is off");

	}

	private void getAuthToken(LoginUserDTO loginUserDTO, LoginMode loginMode) throws RegBaseCheckedException {

		ApplicationContext.map().put(RegistrationConstants.USER_DTO, loginUserDTO);
		if (RegistrationAppHealthCheckUtil.isNetworkAvailable()) {

			serviceDelegateUtil.getAuthToken(loginMode);

		} else {
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.NO_INTERNET_CONNECTION);
		}
	}

}
