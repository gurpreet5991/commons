package io.mosip.registration.controller;

import static io.mosip.registration.constants.RegistrationConstants.ACKNOWLEDGEMENT_TEMPLATE;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;
import static io.mosip.registration.constants.RegistrationExceptions.REG_UI_APPROVE_SCREEN_EXCEPTION;
import static io.mosip.registration.constants.RegistrationExceptions.REG_UI_AUTHORIZATION_EXCEPTION;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.TemplateService;
import io.mosip.registration.util.acktemplate.VelocityPDFGenerator;
import io.mosip.registration.util.dataprovider.DataProvider;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Class for Registration Packet operations
 * 
 * @author Sravya Surampalli
 * @since 1.0.0
 *
 */
@Controller
public class RegistrationOfficerPacketController extends BaseController {

	private static final Logger LOGGER = AppConfig.getLogger(RegistrationOfficerPacketController.class);

	@FXML
	private AnchorPane acknowRoot;

	@FXML
	private AnchorPane uploadRoot;

	@Autowired
	private AckReceiptController ackReceiptController;

	@Autowired
	private TemplateService templateService;

	private VelocityPDFGenerator velocityGenerator = new VelocityPDFGenerator();

	/**
	 * Validating screen authorization and Creating Packet and displaying
	 * acknowledgement form
	 */

	public void createPacket(ActionEvent event) {

		try {
			ResourceBundle bundle = ResourceBundle.getBundle("resourceBundle/labels");
			Parent createRoot = BaseController.load(getClass().getResource(RegistrationConstants.CREATE_PACKET_PAGE), bundle);
			
			LOGGER.debug("REGISTRATION - CREATE_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER",
					APPLICATION_NAME, APPLICATION_ID,
					"Validating Create Packet screen for specific role");

			if (!validateScreenAuthorization(createRoot.getId())) {
				generateAlert(RegistrationConstants.AUTHORIZATION_ALERT_TITLE,
						AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
						RegistrationConstants.AUTHORIZATION_INFO_MESSAGE,
						REG_UI_AUTHORIZATION_EXCEPTION.getErrorMessage());
			} else {
				StringBuilder errorMessage = new StringBuilder();
				String errorAlert = null;
				ResponseDTO responseDTO;
				responseDTO = validateSyncStatus();
				List<ErrorResponseDTO> errorResponseDTOs = responseDTO.getErrorResponseDTOs();
				if (errorResponseDTOs != null && !errorResponseDTOs.isEmpty()) {
					for (ErrorResponseDTO errorResponseDTO : errorResponseDTOs) {
						errorMessage
						.append(errorResponseDTO.getMessage() + " - " + errorResponseDTO.getCode() + "\n\n");
				errorAlert = errorResponseDTO.getInfoType();
					}
					generateAlert(RegistrationConstants.ERROR, AlertType.valueOf(errorAlert),
							errorMessage.toString().trim());

				} else {
					LoginController.getScene().setRoot(createRoot);
					ClassLoader loader = Thread.currentThread().getContextClassLoader();
					LoginController.getScene().getStylesheets()
							.add(loader.getResource("application.css").toExternalForm());
				}
			}

		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - UI- Officer Packet Create ", APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage());
		}
	}

	public void showReciept(RegistrationDTO registrationDTO) {

		try {
			registrationDTO = DataProvider.getPacketDTO(registrationDTO);
			ackReceiptController.setRegistrationData(registrationDTO);

			String ackTemplateText = templateService.getHtmlTemplate(ACKNOWLEDGEMENT_TEMPLATE);
			Writer writer = velocityGenerator.generateTemplate(ackTemplateText, registrationDTO);
			ackReceiptController.setStringWriter(writer);

			Stage primaryStage = new Stage();
			Parent ackRoot = BaseController.load(getClass().getResource(RegistrationConstants.ACK_RECEIPT_PATH));
			primaryStage.setResizable(false);
			primaryStage.setTitle(RegistrationConstants.ACKNOWLEDGEMENT_FORM_TITLE);
			Scene scene = new Scene(ackRoot);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (RegBaseCheckedException regBaseCheckedException) {
			LOGGER.error("REGISTRATION - OFFICER_PACKET_MANAGER - CREATE PACKET", APPLICATION_NAME,
					APPLICATION_ID, regBaseCheckedException.getMessage());
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - UI- Officer Packet Create ", APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage());
		}

	}

	/**
	 * Validating screen authorization and Approve, Reject and Hold packets
	 */
	public void approvePacket(ActionEvent event) {
		try {
			Parent root = BaseController.load(getClass().getResource(RegistrationConstants.APPROVAL_PAGE));

			LOGGER.debug("REGISTRATION - APPROVE_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER",
					APPLICATION_NAME, APPLICATION_ID,
					"Validating Approve Packet screen for specific role");

			if (!validateScreenAuthorization(root.getId())) {
				generateAlert(RegistrationConstants.AUTHORIZATION_ALERT_TITLE,
						AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
						RegistrationConstants.AUTHORIZATION_INFO_MESSAGE,
						REG_UI_AUTHORIZATION_EXCEPTION.getErrorMessage());
			} else {
				Button button = (Button) event.getSource();
				AnchorPane anchorPane = (AnchorPane) button.getParent();
				VBox vBox = (VBox) (anchorPane.getParent());
				ObservableList<Node> nodes = vBox.getChildren();
				IntStream.range(1, nodes.size()).forEach(index -> {
					nodes.get(index).setVisible(false);
					nodes.get(index).setManaged(false);
				});
				nodes.add(root);
			}
		} catch (IOException ioException) {
			generateAlert(RegistrationConstants.ALERT_ERROR, AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
					REG_UI_APPROVE_SCREEN_EXCEPTION.getErrorMessage());
		}
	}

	/**
	 * Validating screen authorization and Uploading packets to FTP server
	 */
	public void uploadPacket(ActionEvent event) {
		try {
			uploadRoot = BaseController.load(getClass().getResource(RegistrationConstants.FTP_UPLOAD_PAGE));

			LOGGER.debug("REGISTRATION - UPLOAD_PACKET - REGISTRATION_OFFICER_PACKET_CONTROLLER",
					APPLICATION_NAME, APPLICATION_ID,
					"Validating Upload Packet screen for specific role");

			if (!validateScreenAuthorization(uploadRoot.getId())) {
				generateAlert(RegistrationConstants.AUTHORIZATION_ALERT_TITLE,
						AlertType.valueOf(RegistrationConstants.ALERT_ERROR),
						RegistrationConstants.AUTHORIZATION_INFO_MESSAGE,
						REG_UI_AUTHORIZATION_EXCEPTION.getErrorMessage());
			} else {
				Button button = (Button) event.getSource();
				AnchorPane anchorPane = (AnchorPane) button.getParent();
				VBox vBox = (VBox) (anchorPane.getParent());
				ObservableList<Node> nodes = vBox.getChildren();
				IntStream.range(1, nodes.size()).forEach(index -> {
					nodes.get(index).setVisible(false);
					nodes.get(index).setManaged(false);
				});
				nodes.add(uploadRoot);
			}
		} catch (IOException ioException) {
			LOGGER.error("REGISTRATION - UI- Officer Packet upload", APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage());
		}
	}

}
