package io.mosip.registration.controller.reg;

import static io.mosip.kernel.core.util.DateUtils.formatDate;
import static io.mosip.registration.constants.LoggerConstants.PACKET_HANDLER;
import static io.mosip.registration.constants.RegistrationConstants.ACKNOWLEDGEMENT_TEMPLATE;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.PolicySyncService;
import io.mosip.registration.service.packet.PacketHandlerService;
import io.mosip.registration.service.packet.PacketUploadService;
import io.mosip.registration.service.packet.ReRegistrationService;
import io.mosip.registration.service.packet.RegistrationApprovalService;
import io.mosip.registration.service.sync.PacketSynchService;
import io.mosip.registration.service.sync.PreRegistrationDataSyncService;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
	private AnchorPane acknowRoot;

	@FXML
	private Button uinUpdateBtn;

	@FXML
	private ImageView uinUpdateImage;

	@FXML
	private Button newRegistrationBtn;

	@FXML
	private AnchorPane uploadRoot;

	@FXML
	private AnchorPane optionRoot;

	@FXML
	private Label pendingApprovalCountLbl;

	@FXML
	private Label reRegistrationCountLbl;

	@FXML
	private AnchorPane eodProcessAnchorPane;

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

	@Value("${mosip.registration.save_ack_inside_packet}")
	private String saveAck;

	@Value("${PACKET_STORE_LOCATION}")
	private String packetStoreLocation;

	@Autowired
	private Environment environment;

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

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		if (!SessionContext.userContext().getRoles().contains(RegistrationConstants.SUPERVISOR)
				&& !SessionContext.userContext().getRoles().contains(RegistrationConstants.ADMIN_ROLE)) {
			eodProcessAnchorPane.setVisible(false);
		}
		pendingApprovalCountLbl.setText(RegistrationUIConstants.NO_PENDING_APPLICATIONS);
		reRegistrationCountLbl.setText(RegistrationUIConstants.NO_RE_REGISTER_APPLICATIONS);

		List<RegistrationApprovalDTO> pendingApprovalRegistrations = registrationApprovalService
				.getEnrollmentByStatus(RegistrationClientStatusCode.CREATED.getCode());
		List<PacketStatusDTO> reRegisterRegistrations = reRegistrationService.getAllReRegistrationPackets();

		if (!pendingApprovalRegistrations.isEmpty()) {
			pendingApprovalCountLbl
					.setText(pendingApprovalRegistrations.size() + " " + RegistrationUIConstants.APPLICATIONS);
		}
		if (!reRegisterRegistrations.isEmpty()) {
			reRegistrationCountLbl.setText(reRegisterRegistrations.size() + " " + RegistrationUIConstants.APPLICATIONS);
		}
		if (!(String.valueOf(ApplicationContext.map().get(RegistrationConstants.UIN_UPDATE_CONFIG_FLAG))).equalsIgnoreCase(RegistrationConstants.ENABLE)) {
			uinUpdateBtn.setVisible(false);
			uinUpdateImage.setVisible(false);
		}

	}

	/**
	 * Validating screen authorization and Creating Packet and displaying
	 * acknowledgement form
	 */
	public void createPacket() {
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
						generateAlert(RegistrationConstants.ERROR, errorMessage.toString().trim());

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
			generateAlert(RegistrationUIConstants.INVALID_KEY);
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Creating of Registration ended.");
	}

	public void showReciept(String capturePhotoUsingDevice) {
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Showing receipt Started.");
		try {
			RegistrationDTO registrationDTO = (RegistrationDTO) SessionContext.map()
					.get(RegistrationConstants.REGISTRATION_DATA);
			ackReceiptController.setRegistrationData(registrationDTO);
			String ackTemplateText = templateService.getHtmlTemplate(ACKNOWLEDGEMENT_TEMPLATE);
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

		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Pending Approval screen started.");
		try {
			auditFactory.audit(AuditEvent.NAV_APPROVE_REG, Components.NAVIGATION,
					SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			Parent root = BaseController.load(getClass().getResource(RegistrationConstants.PENDING_APPROVAL_PAGE));

			LOGGER.info("REGISTRATION - APPROVE_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER", APPLICATION_NAME,
					APPLICATION_ID, "Validating Approve Packet screen for specific role");

			if (!validateScreenAuthorization(root.getId())) {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.AUTHORIZATION_ERROR);
			} else {
				ObservableList<Node> nodes = homeController.getMainBox().getChildren();
				IntStream.range(1, nodes.size()).forEach(index -> {
					nodes.get(index).setVisible(false);
					nodes.get(index).setManaged(false);
				});
				nodes.add(root);
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

				ObservableList<Node> nodes = homeController.getMainBox().getChildren();
				IntStream.range(1, nodes.size()).forEach(index -> {
					nodes.get(index).setVisible(false);
					nodes.get(index).setManaged(false);
				});
				nodes.add(uploadRoot);
			}
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - UI- Officer Packet upload", APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Packet Upload screen ended.");
	}

	public void updateUIN() {
		if (isKeyValid()) {

			LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Update UIN screen started.");
			try {
				auditFactory.audit(AuditEvent.NAV_UIN_UPDATE, Components.NAVIGATION,
						SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

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
						generateAlert(RegistrationConstants.ERROR, errorMessage.toString().trim());

					} else {
						ObservableList<Node> nodes = homeController.getMainBox().getChildren();
						IntStream.range(1, nodes.size()).forEach(index -> {
							nodes.get(index).setVisible(false);
							nodes.get(index).setManaged(false);
						});
						nodes.add(root);
					}
				}
			} catch (IOException ioException) {
				LOGGER.error("REGISTRATION - UI- UIN Update", APPLICATION_NAME, APPLICATION_ID,
						ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
			}
		} else {
			generateAlert(RegistrationUIConstants.INVALID_KEY);
		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading Update UIN screen ended.");
	}

	/**
	 * Sync data through batch jobs.
	 *
	 * @param event
	 *            the event
	 */
	public void syncData() {

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
	 * 
	 * @param event
	 */
	@FXML
	public void downloadPreRegData() {

		auditFactory.audit(AuditEvent.NAV_DOWNLOAD_PRE_REG_DATA, Components.NAVIGATION,
				SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Downloading pre-registration data started.");
		ResponseDTO responseDTO = preRegistrationDataSyncService
				.getPreRegistrationIds(RegistrationConstants.JOB_TRIGGER_POINT_USER);

		if (responseDTO.getSuccessResponseDTO() != null) {
			SuccessResponseDTO successResponseDTO = responseDTO.getSuccessResponseDTO();
			generateAlert(successResponseDTO.getCode(), successResponseDTO.getMessage());

		} else if (responseDTO.getErrorResponseDTOs() != null) {

			ErrorResponseDTO errorresponse = responseDTO.getErrorResponseDTOs().get(0);
			generateAlert(errorresponse.getCode(), errorresponse.getMessage());

		}
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Downloading pre-registration data ended.");

	}

	/**
	 * change On-Board user Perspective
	 * 
	 * @param event
	 *            is an action event
	 * @throws IOException
	 */
	public void onBoardUser() {

		auditFactory.audit(AuditEvent.NAV_ON_BOARD_USER, Components.NAVIGATION, APPLICATION_NAME,
				AuditReferenceIdTypes.APPLICATION_ID.getReferenceTypeId());

		SessionContext.map().put(RegistrationConstants.ONBOARD_USER, true);
		SessionContext.map().put(RegistrationConstants.ONBOARD_USER_UPDATE, true);

		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading User Onboard Update page");

		VBox mainBox = new VBox();
		try {
			HBox headerRoot = BaseController.load(getClass().getResource(RegistrationConstants.HEADER_PAGE));
			mainBox.getChildren().add(headerRoot);
			AnchorPane onboardRoot = BaseController.load(getClass().getResource(RegistrationConstants.USER_ONBOARD),
					applicationContext.getApplicationLanguageBundle());
			mainBox.getChildren().add(onboardRoot);
			getScene(mainBox).setRoot(mainBox);
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

		if (saveAck.equalsIgnoreCase(RegistrationConstants.ENABLE)) {
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

			String mobile = moroccoIdentity.getPhone();
			String email = moroccoIdentity.getEmail();
			sendEmailNotification(email);
			sendSMSNotification(mobile);

			try {

				if (!String.valueOf(ApplicationContext.map().get(RegistrationConstants.EOD_PROCESS_CONFIG_FLAG))
						.equalsIgnoreCase(RegistrationConstants.ENABLE)) {
					updatePacketStatus();
					syncAndUploadPacket();
				}

				// Generate the file path for storing the Encrypted Packet and Acknowledgement
				// Receipt
				String seperator = "/";
				String filePath = packetStoreLocation + seperator
						+ formatDate(new Date(),
								environment.getProperty(RegistrationConstants.PACKET_STORE_DATE_FORMAT))
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

			if (registrationDTO.getSelectionListDTO() == null) {

				AddressDTO addressDTO = Builder.build(AddressDTO.class)
						.with(address -> address.setAddressLine1(moroccoIdentity.getAddressLine1().get(0).getValue()))
						.with(address -> address.setAddressLine2(moroccoIdentity.getAddressLine2().get(0).getValue()))
						.with(address -> address.setLine3(moroccoIdentity.getAddressLine3().get(0).getValue()))
						.with(address -> address.setLocationDTO(Builder.build(LocationDTO.class)
								.with(location -> location.setCity(moroccoIdentity.getCity().get(0).getValue()))
								.with(location -> location.setProvince(moroccoIdentity.getProvince().get(0).getValue()))
								.with(location -> location.setRegion(moroccoIdentity.getRegion().get(0).getValue()))
								.with(location -> location.setPostalCode(moroccoIdentity.getPostalCode())).get()))
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
		LOGGER.info(PACKET_HANDLER, APPLICATION_NAME, APPLICATION_ID, "Loading re-registration screen sarted.");
		try {
			auditFactory.audit(AuditEvent.NAV_RE_REGISTRATION, Components.NAVIGATION,
					SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

			Parent root = BaseController.load(getClass().getResource(RegistrationConstants.REREGISTRATION_PAGE));

			LOGGER.info("REGISTRATION - LOAD_REREGISTRATION_SCREEN - REGISTRATION_OFFICER_PACKET_CONTROLLER",
					APPLICATION_NAME, APPLICATION_ID, "Loading reregistration screen");

			if (!validateScreenAuthorization(root.getId())) {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.AUTHORIZATION_ERROR);
			} else {
				ObservableList<Node> nodes = homeController.getMainBox().getChildren();
				IntStream.range(1, nodes.size()).forEach(index -> {
					nodes.get(index).setVisible(false);
					nodes.get(index).setManaged(false);
				});
				nodes.add(root);
			}
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
}
