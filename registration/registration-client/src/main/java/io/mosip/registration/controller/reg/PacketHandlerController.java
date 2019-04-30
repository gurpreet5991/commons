package io.mosip.registration.controller.reg;

import static io.mosip.kernel.core.util.DateUtils.formatDate;
import static io.mosip.registration.constants.LoggerConstants.PACKET_HANDLER;
import static io.mosip.registration.constants.RegistrationConstants.ACKNOWLEDGEMENT_TEMPLATE_PART_1;
import static io.mosip.registration.constants.RegistrationConstants.ACKNOWLEDGEMENT_TEMPLATE_PART_2;
import static io.mosip.registration.constants.RegistrationConstants.ACKNOWLEDGEMENT_TEMPLATE_PART_3;
import static io.mosip.registration.constants.RegistrationConstants.ACKNOWLEDGEMENT_TEMPLATE_PART_4;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.kernel.core.util.FileUtils;
import io.mosip.registration.builder.Builder;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.AuditReferenceIdTypes;
import io.mosip.registration.constants.Components;
import io.mosip.registration.constants.RegistrationClientStatusCode;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.PacketStatusDTO;
import io.mosip.registration.dto.RegistrationApprovalDTO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.dto.demographic.AddressDTO;
import io.mosip.registration.dto.demographic.LocationDTO;
import io.mosip.registration.dto.demographic.MoroccoIdentity;
import io.mosip.registration.entity.PreRegistrationList;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.PolicySyncService;
import io.mosip.registration.service.packet.PacketHandlerService;
import io.mosip.registration.service.packet.PacketUploadService;
import io.mosip.registration.service.packet.ReRegistrationService;
import io.mosip.registration.service.packet.RegistrationApprovalService;
import io.mosip.registration.service.sync.PacketSynchService;
import io.mosip.registration.service.sync.PreRegistrationDataSyncService;
import io.mosip.registration.service.template.NotificationService;
import io.mosip.registration.service.template.TemplateService;
import io.mosip.registration.util.acktemplate.TemplateGenerator;
import io.mosip.registration.util.healthcheck.RegistrationAppHealthCheckUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Class for Registration Packet operations
 * 
 * @author Sravya Surampalli
 * @since 1.0.0
 *
 */
@Controller
public class PacketHandlerController extends BaseController implements Initializable {

	private static final Logger LOGGER = AppConfig.getLogger(PacketHandlerController.class);

	@FXML
	private Button uinUpdateBtn;

	@FXML
	private ImageView uinUpdateImage;

	@FXML
	private Button newRegistrationBtn;

	@FXML
	private GridPane uploadRoot;

	@FXML
	private Label pendingApprovalCountLbl;

	@FXML
	private Label reRegistrationCountLbl;

	@FXML
	private GridPane eodProcessGridPane;

	@FXML
	private GridPane lostUINPane;

	@FXML
	private VBox vHolder;

	@FXML
	public GridPane uinUpdateGridPane;

	@Autowired
	private AckReceiptController ackReceiptController;

	@Autowired
	private HomeController homeController;

	@Autowired
	private TemplateService templateService;

	@Autowired
	private TemplateManagerBuilder templateManagerBuilder;

	@Autowired
	private TemplateGenerator templateGenerator;

	@Autowired
	PreRegistrationDataSyncService preRegistrationDataSyncService;

	@Autowired
	private UserOnboardController userOnboardController;

	@Autowired
	private PacketHandlerService packetHandlerService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private RegistrationApprovalService registrationApprovalService;

	@Autowired
	private ReRegistrationService reRegistrationService;

	@Autowired
	private UserOnboardParentController userOnboardParentController;

	@Autowired
	private PacketSynchService packetSynchService;

	@Autowired
	private PacketUploadService packetUploadService;
	@Autowired
	private PolicySyncService policySyncService;

	@Autowired
	private RegistrationController registrationController;

	@Autowired
	private DemographicDetailController demographicDetailController;

	@FXML
	ProgressIndicator progressIndicator;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		if (!SessionContext.userContext().getRoles().contains(RegistrationConstants.SUPERVISOR)
				&& !SessionContext.userContext().getRoles().contains(RegistrationConstants.ADMIN_ROLE)) {
			eodProcessGridPane.setVisible(false);
		}
		pendingApprovalCountLbl.setText(RegistrationUIConstants.NO_PENDING_APPLICATIONS);
		reRegistrationCountLbl.setText(RegistrationUIConstants.NO_RE_REGISTER_APPLICATIONS);

		List<RegistrationApprovalDTO> pendingApprovalRegistrations = registrationApprovalService
				.getEnrollmentByStatus(RegistrationClientStatusCode.CREATED.getCode());
		List<PacketStatusDTO> reRegisterRegistrations = reRegistrationService.getAllReRegistrationPackets();
		List<String> configuredFieldsfromDB = Arrays.asList(
				getValueFromApplicationContext(RegistrationConstants.UIN_UPDATE_CONFIG_FIELDS_FROM_DB).split(","));

		if (!pendingApprovalRegistrations.isEmpty()) {
			pendingApprovalCountLbl
					.setText(pendingApprovalRegistrations.size() + " " + RegistrationUIConstants.APPLICATIONS);
		}
		if (!reRegisterRegistrations.isEmpty()) {
			reRegistrationCountLbl.setText(reRegisterRegistrations.size() + " " + RegistrationUIConstants.APPLICATIONS);
		}
		if (!(getValueFromApplicationContext(RegistrationConstants.UIN_UPDATE_CONFIG_FLAG))
				.equalsIgnoreCase(RegistrationConstants.ENABLE)
				|| configuredFieldsfromDB.get(RegistrationConstants.PARAM_ZERO).isEmpty()) {
			vHolder.getChildren().forEach(btnNode -> {
				if (btnNode instanceof GridPane && btnNode.getId() != null
						&& btnNode.getId().equals(uinUpdateGridPane.getId())) {
					btnNode.setVisible(false);
					btnNode.setManaged(false);
				}
			});

		}

		if (!(getValueFromApplicationContext(RegistrationConstants.LOST_UIN_CONFIG_FLAG))
				.equalsIgnoreCase(RegistrationConstants.ENABLE)) {
			lostUINPane.setVisible(false);
			// vHolder.setManaged(false);
		}

	}

	/**
	 * Validating screen authorization and Creating Packet and displaying
	 * acknowledgement form
	 */
	public void createPacket() {
		if (isMachineRemapProcessStarted()) {

			LOGGER.info("REGISTRATION - CREATE_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, RegistrationConstants.MACHINE_CENTER_REMAP_MSG);
			return;
		}
		if (isKeyValid()) {
			LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Creating of Registration Starting.");
			try {
				auditFactory.audit(AuditEvent.NAV_NEW_REG, Components.NAVIGATION,
						SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

				Parent createRoot = BaseController.load(
						getClass().getResource(RegistrationConstants.CREATE_PACKET_PAGE),
						applicationContext.getApplicationLanguageBundle());
				LOGGER.info("REGISTRATION - CREATE_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
						APPLICATION_ID, "Validating Create Packet screen for specific role");

				if (!validateScreenAuthorization(createRoot.getId())) {
					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.AUTHORIZATION_ERROR);
				} else {
					StringBuilder errorMessage = new StringBuilder();
					ResponseDTO responseDTO;
					responseDTO = validateSyncStatus();
					List<ErrorResponseDTO> errorResponseDTOs = responseDTO.getErrorResponseDTOs();
					if (errorResponseDTOs != null && !errorResponseDTOs.isEmpty()) {
						for (ErrorResponseDTO errorResponseDTO : errorResponseDTOs) {
							errorMessage.append(errorResponseDTO.getMessage() + "\n\n");
						}
						generateAlertLanguageSpecific(RegistrationConstants.ERROR, errorMessage.toString().trim());
					} else {
						getScene(createRoot).setRoot(createRoot);
					}
				}

			} catch (IOException ioException) {
				LOGGER.error("REGISTRATION - UI- Officer Packet Create ", APPLICATION_NAME, APPLICATION_ID,
						ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));

				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_REG_PAGE);
			}
		} else {
			generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.INVALID_KEY);
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Creating of Registration ended.");
	}

	/**
	 * Validating screen authorization and Creating Packet in case of Lost UIN
	 */
	public void lostUIN() {
		if (isMachineRemapProcessStarted()) {

			LOGGER.info("REGISTRATION - lost UIN - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, RegistrationConstants.MACHINE_CENTER_REMAP_MSG);
			return;
		}
		String fingerPrintDisableFlag = getValueFromApplicationContext(RegistrationConstants.FINGERPRINT_DISABLE_FLAG);
		String irisDisableFlag = getValueFromApplicationContext(RegistrationConstants.IRIS_DISABLE_FLAG);
		String faceDisableFlag = getValueFromApplicationContext(RegistrationConstants.FACE_DISABLE_FLAG);

		if (RegistrationConstants.DISABLE.equalsIgnoreCase(fingerPrintDisableFlag)
				&& RegistrationConstants.DISABLE.equalsIgnoreCase(irisDisableFlag)
				&& RegistrationConstants.DISABLE.equalsIgnoreCase(faceDisableFlag)) {
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.LOST_UIN_REQUEST_ERROR);
		} else {
			if (isMachineRemapProcessStarted()) {
				LOGGER.info("REGISTRATION - CREATE_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
						APPLICATION_ID, RegistrationConstants.MACHINE_CENTER_REMAP_MSG);
				return;
			}
			if (isKeyValid()) {
				LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID,
						"Creating of Registration for lost UIN Starting.");
				try {
					auditFactory.audit(AuditEvent.NAV_NEW_REG, Components.NAVIGATION,
							SessionContext.userContext().getUserId(),
							AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

					/* Mark Registration Category as Lost UIN */
					registrationController.initializeLostUIN();

					Parent createRoot = BaseController.load(
							getClass().getResource(RegistrationConstants.CREATE_PACKET_PAGE),
							applicationContext.getApplicationLanguageBundle());
					LOGGER.info("REGISTRATION - CREATE_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER",
							APPLICATION_NAME, APPLICATION_ID, "Validating Create Packet screen for specific role");

					if (!validateScreenAuthorization(createRoot.getId())) {
						generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.AUTHORIZATION_ERROR);
					} else {
						StringBuilder errorMessage = new StringBuilder();
						ResponseDTO responseDTO;
						responseDTO = validateSyncStatus();
						List<ErrorResponseDTO> errorResponseDTOs = responseDTO.getErrorResponseDTOs();
						if (errorResponseDTOs != null && !errorResponseDTOs.isEmpty()) {
							for (ErrorResponseDTO errorResponseDTO : errorResponseDTOs) {
								errorMessage.append(errorResponseDTO.getMessage() + "\n\n");
							}
							generateAlertLanguageSpecific(RegistrationConstants.ERROR, errorMessage.toString().trim());
						} else {
							getScene(createRoot).setRoot(createRoot);
							demographicDetailController.lostUIN();
						}
					}
				} catch (IOException ioException) {
					LOGGER.error("REGISTRATION - UI- Officer Packet Create for Lost UIN", APPLICATION_NAME,
							APPLICATION_ID, ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));

					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_REG_PAGE);
				}
			} else {
				generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.INVALID_KEY);
			}
		}

		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Creating of Registration for lost UIN ended.");
	}

	public void showReciept() {
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Showing receipt Started.");
		try {
			RegistrationDTO registrationDTO = getRegistrationDTOFromSession();

			StringBuilder templateContent = new StringBuilder();
			String platformLanguageCode = ApplicationContext.applicationLanguage();
			templateContent
					.append(templateService.getHtmlTemplate(ACKNOWLEDGEMENT_TEMPLATE_PART_1, platformLanguageCode));
			templateContent
					.append(templateService.getHtmlTemplate(ACKNOWLEDGEMENT_TEMPLATE_PART_2, platformLanguageCode));
			templateContent
					.append(templateService.getHtmlTemplate(ACKNOWLEDGEMENT_TEMPLATE_PART_3, platformLanguageCode));
			templateContent
					.append(templateService.getHtmlTemplate(ACKNOWLEDGEMENT_TEMPLATE_PART_4, platformLanguageCode));
			String ackTemplateText = templateContent.toString();

			if (ackTemplateText != null && !ackTemplateText.isEmpty()) {
				ResponseDTO templateResponse = templateGenerator.generateTemplate(ackTemplateText, registrationDTO,
						templateManagerBuilder, RegistrationConstants.ACKNOWLEDGEMENT_TEMPLATE);
				if (templateResponse != null && templateResponse.getSuccessResponseDTO() != null) {
					Writer stringWriter = (Writer) templateResponse.getSuccessResponseDTO().getOtherAttributes()
							.get(RegistrationConstants.TEMPLATE_NAME);
					ackReceiptController.setStringWriter(stringWriter);
					ResponseDTO packetCreationResponse = savePacket(stringWriter, registrationDTO);
					if (packetCreationResponse.getSuccessResponseDTO() != null) {
						Parent createRoot = BaseController.load(
								getClass().getResource(RegistrationConstants.ACK_RECEIPT_PATH),
								applicationContext.getApplicationLanguageBundle());
						getScene(createRoot).setRoot(createRoot);
					} else {
						clearRegistrationData();
						createPacket();
					}
				} else if (templateResponse != null && templateResponse.getErrorResponseDTOs() != null) {
					generateAlert(RegistrationConstants.ERROR,
							RegistrationUIConstants.UNABLE_LOAD_ACKNOWLEDGEMENT_PAGE);
					clearRegistrationData();
					createPacket();
				}
			} else {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_ACKNOWLEDGEMENT_PAGE);
				clearRegistrationData();
				createPacket();
			}
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - UI- Officer Packet Create ", APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Showing receipt ended.");
	}

	/**
	 * Validating screen authorization and Approve, Reject and Hold packets
	 */
	public void approvePacket() {
		if (isMachineRemapProcessStarted()) {

			LOGGER.info("REGISTRATION - UPLOAD_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, RegistrationConstants.MACHINE_CENTER_REMAP_MSG);
			/*
			 * check if there is no pending packets and blocks the user to proceed further
			 */
			if (!isPacketsPendingForEOD())
				return;
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Pending Approval screen started.");
		try {
			auditFactory.audit(AuditEvent.NAV_APPROVE_REG, Components.NAVIGATION,
					SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			GridPane root = BaseController.load(getClass().getResource(RegistrationConstants.PENDING_APPROVAL_PAGE));

			LOGGER.info("REGISTRATION - APPROVE_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, "Validating Approve Packet screen for specific role");

			if (!validateScreenAuthorization(root.getId())) {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.AUTHORIZATION_ERROR);
			} else {
				getScene(root);
			}
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - OFFICER_PACKET_MANAGER - APPROVE PACKET", APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_APPROVAL_PAGE);
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Pending Approval screen ended.");
	}

	/**
	 * Validating screen authorization and Uploading packets to FTP server
	 */
	public void uploadPacket() {

		if (isMachineRemapProcessStarted()) {

			LOGGER.info("REGISTRATION - UPLOAD_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, RegistrationConstants.MACHINE_CENTER_REMAP_MSG);
			return;
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Packet Upload screen started.");
		try {
			auditFactory.audit(AuditEvent.NAV_UPLOAD_PACKETS, Components.NAVIGATION,
					SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			uploadRoot = BaseController.load(getClass().getResource(RegistrationConstants.FTP_UPLOAD_PAGE));

			LOGGER.info("REGISTRATION - UPLOAD_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, "Validating Upload Packet screen for specific role");

			if (!validateScreenAuthorization(uploadRoot.getId())) {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.AUTHORIZATION_ERROR);
			} else {
				getScene(uploadRoot);
			}
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - UI- Officer Packet upload", APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Packet Upload screen ended.");
	}

	public void updateUIN() {
		if (isMachineRemapProcessStarted()) {

			LOGGER.info("REGISTRATION - update UIN - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, RegistrationConstants.MACHINE_CENTER_REMAP_MSG);
			return;
		}
		if (isKeyValid()) {

			LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Update UIN screen started.");
			try {
				auditFactory.audit(AuditEvent.NAV_UIN_UPDATE, Components.NAVIGATION,
						SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

				if (RegistrationConstants.DISABLE.equalsIgnoreCase(
						getValueFromApplicationContext(RegistrationConstants.FINGERPRINT_DISABLE_FLAG))
						&& RegistrationConstants.DISABLE.equalsIgnoreCase(
								getValueFromApplicationContext(RegistrationConstants.IRIS_DISABLE_FLAG))) {

					generateAlert(RegistrationConstants.ERROR,
							RegistrationUIConstants.UPDATE_UIN_NO_BIOMETRIC_CONFIG_ALERT);
				} else {
					Parent root = BaseController.load(getClass().getResource(RegistrationConstants.UIN_UPDATE));

					LOGGER.info("REGISTRATION - update UIN - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
							APPLICATION_ID, "updating UIN");

					if (!validateScreenAuthorization(root.getId())) {
						generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.AUTHORIZATION_ERROR);
					} else {

						StringBuilder errorMessage = new StringBuilder();
						ResponseDTO responseDTO;
						responseDTO = validateSyncStatus();
						List<ErrorResponseDTO> errorResponseDTOs = responseDTO.getErrorResponseDTOs();
						if (errorResponseDTOs != null && !errorResponseDTOs.isEmpty()) {
							for (ErrorResponseDTO errorResponseDTO : errorResponseDTOs) {
								errorMessage.append(errorResponseDTO.getMessage() + "\n\n");
							}
							generateAlertLanguageSpecific(RegistrationConstants.ERROR, errorMessage.toString().trim());

						} else {
							getScene(root);
						}
					}
				}
			} catch (IOException ioException) {
				LOGGER.error("REGISTRATION - UI- UIN Update", APPLICATION_NAME, APPLICATION_ID,
						ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
			}
		} else {
			generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.INVALID_KEY);
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Update UIN screen ended.");
	}

	/**
	 * Sync data through batch jobs.
	 */
	public void syncData() {
		if (isMachineRemapProcessStarted()) {

			LOGGER.info("REGISTRATION - SYNC_DATA - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, RegistrationConstants.MACHINE_CENTER_REMAP_MSG);
			return;
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Sync Data screen started.");
		AnchorPane syncData;
		try {
			auditFactory.audit(AuditEvent.NAV_SYNC_DATA, Components.NAVIGATION,
					SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			syncData = BaseController.load(getClass().getResource(RegistrationConstants.SYNC_DATA));
			ObservableList<Node> nodes = homeController.getMainBox().getChildren();
			IntStream.range(1, nodes.size()).forEach(index -> {
				nodes.get(index).setVisible(false);
				nodes.get(index).setManaged(false);
			});
			nodes.add(syncData);
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - REDIRECTHOME - REGISTRATION_OFFICER_DETAILS_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Sync Data screen ended.");
	}

	/**
	 * This method is to trigger the Pre registration sync service
	 */
	@FXML
	public void downloadPreRegData() {

		if (isMachineRemapProcessStarted()) {
			LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID,
					RegistrationConstants.MACHINE_CENTER_REMAP_MSG);
			return;
		}
		auditFactory.audit(AuditEvent.NAV_DOWNLOAD_PRE_REG_DATA, Components.NAVIGATION,
				SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Downloading pre-registration data started.");
		ResponseDTO responseDTO = preRegistrationDataSyncService
				.getPreRegistrationIds(RegistrationConstants.JOB_TRIGGER_POINT_USER);

		if (responseDTO.getSuccessResponseDTO() != null) {
			SuccessResponseDTO successResponseDTO = responseDTO.getSuccessResponseDTO();
			generateAlertLanguageSpecific(successResponseDTO.getCode(), successResponseDTO.getMessage());

		} else if (responseDTO.getErrorResponseDTOs() != null) {

			ErrorResponseDTO errorresponse = responseDTO.getErrorResponseDTOs().get(0);
			generateAlertLanguageSpecific(errorresponse.getCode(), errorresponse.getMessage());

		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Downloading pre-registration data ended.");

	}

	/**
	 * change On-Board user Perspective
	 */
	public void onBoardUser() {

		if (isMachineRemapProcessStarted()) {

			LOGGER.info("REGISTRATION - ONBOARD_USER_UPDATE - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, RegistrationConstants.MACHINE_CENTER_REMAP_MSG);
			return;
		}
		auditFactory.audit(AuditEvent.NAV_ON_BOARD_USER, Components.NAVIGATION, APPLICATION_NAME,
				AuditReferenceIdTypes.APPLICATION_ID.getReferenceTypeId());

		SessionContext.map().put(RegistrationConstants.ONBOARD_USER, true);
		SessionContext.map().put(RegistrationConstants.ONBOARD_USER_UPDATE, true);

		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading User Onboard Update page");

		try {
			GridPane headerRoot = BaseController.load(getClass().getResource(RegistrationConstants.USER_ONBOARD));
			getScene(headerRoot);
			userOnboardParentController.userOnboardId.lookup("#onboardUser").setVisible(false);
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - ONBOARD_USER_UPDATE - REGISTRATION_OFFICER_DETAILS_CONTROLLER",
					APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
		}
		userOnboardController.initUserOnboard();
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "User Onboard Update page is loaded");
	}

	/**
	 * To save the acknowledgement receipt along with the registration data and
	 * create packet
	 */
	private ResponseDTO savePacket(Writer stringWriter, RegistrationDTO registrationDTO) {
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "packet creation has been started");
		byte[] ackInBytes = null;
		try {
			ackInBytes = stringWriter.toString().getBytes(RegistrationConstants.TEMPLATE_ENCODING);
		} catch (java.io.IOException ioException) {
			LOGGER.error("REGISTRATION - SAVE_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
		}

		if (RegistrationConstants.ENABLE
				.equalsIgnoreCase(getValueFromApplicationContext(RegistrationConstants.ACK_INSIDE_PACKET))) {
			registrationDTO.getDemographicDTO().getApplicantDocumentDTO().setAcknowledgeReceipt(ackInBytes);
			registrationDTO.getDemographicDTO().getApplicantDocumentDTO().setAcknowledgeReceiptName(
					"RegistrationAcknowledgement." + RegistrationConstants.ACKNOWLEDGEMENT_FORMAT);
		}

		// packet creation
		ResponseDTO response = packetHandlerService.handle(registrationDTO);

		if (response.getSuccessResponseDTO() != null
				&& response.getSuccessResponseDTO().getMessage().equals(RegistrationConstants.SUCCESS)) {

			MoroccoIdentity moroccoIdentity = (MoroccoIdentity) registrationDTO.getDemographicDTO()
					.getDemographicInfoDTO().getIdentity();

			try {

				// Sync and Uploads Packet when EOD Process Configuration is set to OFF
				if (!getValueFromApplicationContext(RegistrationConstants.EOD_PROCESS_CONFIG_FLAG)
						.equalsIgnoreCase(RegistrationConstants.ENABLE)) {
					updatePacketStatus();
					syncAndUploadPacket();
				}

				// Deletes the pre registration Data after creation of registration Packet.
				if (getRegistrationDTOFromSession().getPreRegistrationId() != null
						&& !getRegistrationDTOFromSession().getPreRegistrationId().isEmpty()) {

					ResponseDTO responseDTO = new ResponseDTO();
					List<PreRegistrationList> preRegistrationLists = new ArrayList<>();
					PreRegistrationList preRegistrationList = preRegistrationDataSyncService
							.getPreRegistrationRecordForDeletion(
									getRegistrationDTOFromSession().getPreRegistrationId());
					preRegistrationLists.add(preRegistrationList);
					preRegistrationDataSyncService.deletePreRegRecords(responseDTO, preRegistrationLists);

				}

				// Generate the file path for storing the Encrypted Packet and Acknowledgement
				// Receipt
				String seperator = "/";
				String filePath = getValueFromApplicationContext(RegistrationConstants.PKT_STORE_LOC) + seperator
						+ formatDate(new Date(),
								getValueFromApplicationContext(RegistrationConstants.PKT_STORE_DATE_FORMAT))
										.concat(seperator).concat(registrationDTO.getRegistrationId());

				// Storing the Registration Acknowledge Receipt Image
				FileUtils.copyToFile(new ByteArrayInputStream(ackInBytes),
						new File(filePath.concat("_Ack.").concat(RegistrationConstants.ACKNOWLEDGEMENT_FORMAT)));

				LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID,
						"Registration's Acknowledgement Receipt saved");
			} catch (io.mosip.kernel.core.exception.IOException ioException) {
				LOGGER.error("REGISTRATION - SAVE_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
						APPLICATION_ID, ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
			} catch (RegBaseCheckedException regBaseCheckedException) {
				LOGGER.error("REGISTRATION - SAVE_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
						APPLICATION_ID,
						regBaseCheckedException.getMessage() + ExceptionUtils.getStackTrace(regBaseCheckedException));
			}

			sendNotification(moroccoIdentity.getEmail(), moroccoIdentity.getPhone(),
					registrationDTO.getRegistrationId());

			if (registrationDTO.getSelectionListDTO() == null) {

				AddressDTO addressDTO = Builder.build(AddressDTO.class)
						.with(address -> address.setAddressLine1(moroccoIdentity.getAddressLine1() != null
								? moroccoIdentity.getAddressLine1().get(0).getValue()
								: null))
						.with(address -> address.setAddressLine2(moroccoIdentity.getAddressLine2() != null
								? moroccoIdentity.getAddressLine2().get(0).getValue()
								: null))
						.with(address -> address.setLine3(moroccoIdentity.getAddressLine3() != null
								? moroccoIdentity.getAddressLine3().get(0).getValue()
								: null))
						.with(address -> address
								.setLocationDTO(Builder.build(LocationDTO.class)
										.with(location -> location.setCity(moroccoIdentity.getCity() != null
												? moroccoIdentity.getCity().get(0).getValue()
												: null))
										.with(location -> location.setProvince(moroccoIdentity.getProvince() != null
												? moroccoIdentity.getProvince().get(0).getValue()
												: null))
										.with(location -> location.setRegion(moroccoIdentity.getRegion() != null
												? moroccoIdentity.getRegion().get(0).getValue()
												: null))
										.with(location -> location.setPostalCode(moroccoIdentity.getPostalCode() != null
												? moroccoIdentity.getPostalCode()
												: null))
										.get()))
						.get();

				SessionContext.map().put(RegistrationConstants.ADDRESS_KEY, addressDTO);
			}
		} else {
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.PACKET_CREATION_FAILURE);
		}
		return response;
	}

	/**
	 * Load re registration screen.
	 */
	public void loadReRegistrationScreen() {

		if (isMachineRemapProcessStarted()) {

			LOGGER.info("REGISTRATION - LOAD_REREGISTRATION_SCREEN - REGISTRATION_OFFICER_PACKET_CONTROLLER",
					APPLICATION_NAME, APPLICATION_ID, RegistrationConstants.MACHINE_CENTER_REMAP_MSG);
			return;
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading re-registration screen sarted.");
		try {
			auditFactory.audit(AuditEvent.NAV_RE_REGISTRATION, Components.NAVIGATION,
					SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			Parent root = BaseController.load(getClass().getResource(RegistrationConstants.REREGISTRATION_PAGE));

			LOGGER.info("REGISTRATION - LOAD_REREGISTRATION_SCREEN - REGISTRATION_OFFICER_PACKET_CONTROLLER",
					APPLICATION_NAME, APPLICATION_ID, "Loading reregistration screen");

			getScene(root);
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - LOAD_REREGISTRATION_SCREEN - REGISTRATION_OFFICER_PACKET_CONTROLLER",
					APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_APPROVAL_PAGE);
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading re-registration screen ended.");
	}

	/**
	 * Update packet status.
	 */
	private void updatePacketStatus() {
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID,
				"Auto Approval of Packet when EOD process disabled started");

		registrationApprovalService.updateRegistration((getRegistrationDTOFromSession().getRegistrationId()),
				RegistrationConstants.EMPTY, RegistrationClientStatusCode.APPROVED.getCode());

		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID,
				"Auto Approval of Packet when EOD process disabled ended");

	}

	/**
	 * Sync and upload packet.
	 *
	 * @throws RegBaseCheckedException
	 *             the reg base checked exception
	 */
	private void syncAndUploadPacket() throws RegBaseCheckedException {
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Sync and Upload of created Packet started");
		if (RegistrationAppHealthCheckUtil.isNetworkAvailable()) {

			String response = packetSynchService.packetSync(getRegistrationDTOFromSession().getRegistrationId());

			if (response.equals(RegistrationConstants.EMPTY)) {

				packetUploadService.uploadPacket(getRegistrationDTOFromSession().getRegistrationId());
			}

		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Sync and Upload of created Packet ended");
	}

	private boolean isKeyValid() {

		return policySyncService.checkKeyValidation().getSuccessResponseDTO() != null;

	}

	private void sendNotification(String email, String mobile, String regID) {
		try {
			boolean emailSent = false;
			boolean smsSent = false;
			if (RegistrationAppHealthCheckUtil.isNetworkAvailable()) {
				String notificationServiceName = String.valueOf(
						applicationContext.getApplicationMap().get(RegistrationConstants.MODE_OF_COMMUNICATION));
				if (notificationServiceName != null && !notificationServiceName.equals("NONE")) {
					ResponseDTO notificationResponse;
					Writer writeNotificationTemplate;
					if (email != null && (notificationServiceName.toUpperCase())
							.contains(RegistrationConstants.EMAIL_SERVICE.toUpperCase())) {
						writeNotificationTemplate = getNotificationTemplate(RegistrationConstants.EMAIL_TEMPLATE);
						if (!writeNotificationTemplate.toString().isEmpty()) {
							notificationResponse = notificationService.sendEmail(writeNotificationTemplate.toString(),
									email, regID);
							if (notificationResponse.getErrorResponseDTOs() == null
									|| notificationResponse.getSuccessResponseDTO() != null) {
								emailSent = true;
							} else {
								notificationAlert(notificationResponse, RegistrationUIConstants.EMAIL_ERROR_MSG);
							}
						}
					}
					if (mobile != null && (notificationServiceName.toUpperCase())
							.contains(RegistrationConstants.SMS_SERVICE.toUpperCase())) {
						writeNotificationTemplate = getNotificationTemplate(RegistrationConstants.SMS_TEMPLATE);
						if (!writeNotificationTemplate.toString().isEmpty()) {
							notificationResponse = notificationService.sendSMS(writeNotificationTemplate.toString(),
									mobile, regID);
							if (notificationResponse.getErrorResponseDTOs() == null
									|| notificationResponse.getSuccessResponseDTO() != null) {
								smsSent = true;
							} else {
								notificationAlert(notificationResponse, RegistrationUIConstants.SMS_ERROR_MSG);
							}
						}
					}
				}
			}
			if (emailSent) {
				if (smsSent) {
					generateAlert(RegistrationConstants.ALERT_INFORMATION,
							RegistrationUIConstants.NOTIFICATION_SUCCESS);
				} else {
					generateAlert(RegistrationConstants.ALERT_INFORMATION,
							RegistrationUIConstants.EMAIL_NOTIFICATION_SUCCESS);
				}
			} else if (smsSent) {
				generateAlert(RegistrationConstants.ALERT_INFORMATION,
						RegistrationUIConstants.SMS_NOTIFICATION_SUCCESS);
			}
		} catch (RegBaseUncheckedException regBaseUncheckedException) {
			LOGGER.error("REGISTRATION - UI - GENERATE_NOTIFICATION", APPLICATION_NAME, APPLICATION_ID,
					regBaseUncheckedException.getMessage() + ExceptionUtils.getStackTrace(regBaseUncheckedException));
		}
	}

	private void notificationAlert(ResponseDTO notificationResponse, String alertMsg) {

		Optional.ofNullable(notificationResponse).map(ResponseDTO::getErrorResponseDTOs)
				.flatMap(list -> list.stream().findFirst()).map(ErrorResponseDTO::getMessage)
				.ifPresent(message -> generateAlert("ERROR", alertMsg));

	}

	public ProgressIndicator getProgressIndicator() {
		return progressIndicator;
	}
}
