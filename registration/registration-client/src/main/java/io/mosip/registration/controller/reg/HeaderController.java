package io.mosip.registration.controller.reg;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.IOException;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.AuditReferenceIdTypes;
import io.mosip.registration.constants.Components;
import io.mosip.registration.constants.LoggerConstants;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.RestartController;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.jobs.BaseJob;
import io.mosip.registration.scheduler.SchedulerUtil;
import io.mosip.registration.service.MasterSyncService;
import io.mosip.registration.service.config.JobConfigurationService;
import io.mosip.registration.service.packet.RegistrationPacketVirusScanService;
import io.mosip.registration.service.sync.PreRegistrationDataSyncService;
import io.mosip.registration.util.healthcheck.RegistrationAppHealthCheckUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Class for Registration Officer details
 * 
 * @author Sravya Surampalli
 * @author Balaji Sridharan
 * @since 1.0.0
 *
 */
@Controller
public class HeaderController extends BaseController {

	/**
	 * Instance of {@link Logger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(HeaderController.class);

	@FXML
	private Label registrationOfficerName;

	@FXML
	private Label registrationOfficeId;

	@FXML
	private Label registrationOfficeLocation;

	@FXML
	private MenuBar menu;

	@FXML
	private ImageView availableIcon;

	@FXML
	private Label online;

	@FXML
	private Label offline;

	@FXML
	private Menu homeSelectionMenu;

	@Autowired
	PreRegistrationDataSyncService preRegistrationDataSyncService;

	@Autowired
	JobConfigurationService jobConfigurationService;

	@Autowired
	MasterSyncService masterSyncService;

	@Autowired
	PacketHandlerController packetHandlerController;

	@Autowired
	private RegistrationPacketVirusScanService registrationPacketVirusScanService;

	@Autowired
	private RestartController restartController;

	/**
	 * Mapping Registration Officer details
	 */
	public void initialize() {

		LOGGER.info(LoggerConstants.LOG_REG_HEADER, APPLICATION_NAME, APPLICATION_ID,
				"Displaying Registration Officer details");

		registrationOfficerName.setText(SessionContext.userContext().getName());
		registrationOfficeId
				.setText(SessionContext.userContext().getRegistrationCenterDetailDTO().getRegistrationCenterId());
		registrationOfficeLocation
				.setText(SessionContext.userContext().getRegistrationCenterDetailDTO().getRegistrationCenterName());
		menu.setBackground(Background.EMPTY);

		menu.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
		if ((boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)
				&& !(boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER_UPDATE)) {
			homeSelectionMenu.getItems().remove(0, homeSelectionMenu.getItems().size() - 3);
		} else {
			homeSelectionMenu.setDisable(false);
		}

		getTimer().schedule(new TimerTask() {

			@Override
			public void run() {
				Boolean flag = RegistrationAppHealthCheckUtil.isNetworkAvailable();
				online.setVisible(flag);
				offline.setVisible(!flag);
			}
		}, 0, 5000);
	}

	/**
	 * Redirecting to Home page on Logout and destroying Session context
	 */
	public void logout(ActionEvent event) {
		try {
			auditFactory.audit(AuditEvent.LOGOUT_USER, Components.NAVIGATION, SessionContext.userContext().getUserId(),
					AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			LOGGER.info(LoggerConstants.LOG_REG_HEADER, APPLICATION_NAME, APPLICATION_ID, "Clearing Session context");

			SessionContext.destroySession();
			SchedulerUtil.stopScheduler();

			BorderPane loginpage = BaseController.load(getClass().getResource(RegistrationConstants.INITIAL_PAGE));

			getScene(loginpage);

		} catch (IOException ioException) {
			LOGGER.error(LoggerConstants.LOG_REG_HEADER, APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_LOGOUT_PAGE);
		}
	}

	/**
	 * Redirecting to Home page
	 */
	public void redirectHome(ActionEvent event) {
		goToHomePageFromRegistration();
	}

	/**
	 * Sync data through batch jobs.
	 *
	 * @param event
	 *            the event
	 */
	public void syncData(ActionEvent event) {

		AnchorPane syncData;
		try {
			auditFactory.audit(AuditEvent.NAV_SYNC_DATA, Components.NAVIGATION,
					SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());
			ResponseDTO responseDTO = jobConfigurationService.executeAllJobs();

			if (responseDTO.getSuccessResponseDTO() != null) {
				generateAlert(RegistrationUIConstants.SYNC_SUCCESS);
			} else if (responseDTO.getErrorResponseDTOs() != null) {
				generateAlert(RegistrationUIConstants.SYNC_FAILURE,
						responseDTO.getErrorResponseDTOs().get(0).getMessage());
			}

			while (restartController.isToBeRestarted()) {
				/* Clear the completed job map */
				BaseJob.clearCompletedJobMap();

				/* Restart the application */
				restartController.restart();
			}

			if ("Y".equalsIgnoreCase((String) ApplicationContext.getInstance().getApplicationMap()
					.get(RegistrationConstants.UI_SYNC_DATA))) {
				syncData = BaseController.load(getClass().getResource(RegistrationConstants.SYNC_DATA));

				VBox pane = (VBox) menu.getParent().getParent().getParent();
				Object parent = pane.getChildren().get(0);
				pane.getChildren().clear();
				pane.getChildren().add((Node) parent);
				pane.getChildren().add(syncData);
			}

		} catch (IOException ioException) {
			LOGGER.error(LoggerConstants.LOG_REG_HEADER, APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));

		} catch (RuntimeException runtimeException) {
			LOGGER.error(LoggerConstants.LOG_REG_HEADER, APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}

	}

	/**
	 * Redirecting to PacketStatusSync Page
	 */
	public void syncPacketStatus(ActionEvent event) {
		try {
			auditFactory.audit(AuditEvent.SYNC_REGISTRATION_PACKET_STATUS, Components.SYNC_SERVER_TO_CLIENT,
					SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			AnchorPane syncServerClientRoot = BaseController
					.load(getClass().getResource(RegistrationConstants.SYNC_STATUS));

			if (!validateScreenAuthorization(syncServerClientRoot.getId())) {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.AUTHORIZATION_ERROR);
			} else {
				VBox pane = (VBox) (menu.getParent().getParent().getParent());
				for (int index = pane.getChildren().size() - 1; index > 0; index--) {
					pane.getChildren().remove(index);
				}
				pane.getChildren().add(syncServerClientRoot);

			}
		} catch (IOException ioException) {
			LOGGER.error(LoggerConstants.LOG_REG_HEADER, APPLICATION_NAME, APPLICATION_ID, ioException.getMessage());
		}
	}

	/**
	 * Redirects to Device On-Boarding UI Page.
	 * 
	 * @param actionEvent
	 *            is an action event
	 */
	public void onBoardDevice(ActionEvent actionEvent) {
		LOGGER.info(LoggerConstants.DEVICE_ONBOARD_PAGE_NAVIGATION, APPLICATION_NAME, APPLICATION_ID,
				"Navigating to Device Onboarding Page");

		try {
			auditFactory.audit(AuditEvent.NAV_ON_BOARD_DEVICES, Components.NAVIGATION,
					SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			AnchorPane onBoardRoot = BaseController
					.load(getClass().getResource(RegistrationConstants.DEVICE_ONBOARDING_PAGE));

			if (!validateScreenAuthorization(onBoardRoot.getId())) {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.AUTHORIZATION_ERROR);
			} else {
				VBox pane = (VBox) menu.getParent().getParent().getParent();
				Object parent = pane.getChildren().get(0);
				pane.getChildren().clear();
				pane.getChildren().add((Node) parent);
				pane.getChildren().add(onBoardRoot);
			}
		} catch (IOException ioException) {
			LOGGER.error(LoggerConstants.DEVICE_ONBOARD_PAGE_NAVIGATION, APPLICATION_NAME, APPLICATION_ID,
					RegistrationConstants.DEVICE_ONBOARD_PAGE_NAVIGATION_EXCEPTION
							+ "-> Exception while navigating to Device Onboarding page:" + ioException.getMessage());

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.DEVICE_ONBOARD_ERROR_MSG);
		} finally {
			LOGGER.info(LoggerConstants.DEVICE_ONBOARD_PAGE_NAVIGATION, APPLICATION_NAME, APPLICATION_ID,
					"Navigation to Device Onboarding page completed");
		}
	}

	/**
	 * This method is to trigger the Pre registration sync service
	 * 
	 * @param event
	 */
	@FXML
	public void downloadPreRegData(ActionEvent event) {
		auditFactory.audit(AuditEvent.SYNC_PRE_REGISTRATION_PACKET, Components.SYNC_SERVER_TO_CLIENT,
				SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		ResponseDTO responseDTO = preRegistrationDataSyncService
				.getPreRegistrationIds(RegistrationConstants.JOB_TRIGGER_POINT_USER);

		if (responseDTO.getSuccessResponseDTO() != null) {
			SuccessResponseDTO successResponseDTO = responseDTO.getSuccessResponseDTO();
			generateAlert(successResponseDTO.getCode(), successResponseDTO.getMessage());

		} else if (responseDTO.getErrorResponseDTOs() != null) {

			ErrorResponseDTO errorresponse = responseDTO.getErrorResponseDTOs().get(0);
			generateAlert(errorresponse.getCode(), errorresponse.getMessage());

		}
	}

	public void uploadPacketToServer() {
		auditFactory.audit(AuditEvent.SYNC_PRE_REGISTRATION_PACKET, Components.SYNC_SERVER_TO_CLIENT,
				SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		packetHandlerController.uploadPacket();
	}

}