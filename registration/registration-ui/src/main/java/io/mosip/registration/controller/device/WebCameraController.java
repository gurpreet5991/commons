package io.mosip.registration.controller.device;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.device.webcam.MosipWebcamProvider;
import io.mosip.registration.device.webcam.PhotoCaptureFacade;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Class for Opening Web Camera
 *
 * @author Himaja Dhanyamraju
 */
@Controller
public class WebCameraController extends BaseController implements Initializable {

	/**
	 * Instance of {@link MosipLogger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(WebCameraController.class);

	@FXML
	public AnchorPane webCameraPane;

	@FXML
	private SwingNode webcamera;

	@FXML
	private Button clear;

	@FXML
	private Button close;

	private BaseController parentController = null;

	private BufferedImage capturedImage = null;

	private MosipWebcamProvider photoProvider = null;
	@Autowired
	private PhotoCaptureFacade photoCaptureFacade;

	private Webcam webcam;
	private String imageType;

	@Value("${WEBCAM_PROVIDER_NAME}")
	private String photoProviderName;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOGGER.debug("REGISTRATION - UI - WEB_CAMERA_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
				"Page loading has been started");
		
		WebcamPanel cameraPanel = new WebcamPanel(webcam);
		JPanel jPanelWindow = new JPanel();
		jPanelWindow.add(cameraPanel);
		jPanelWindow.setVisible(true);
		webcamera.setContent(jPanelWindow);
	}

	public void init(BaseController parentController, String imageType) {
		LOGGER.debug("REGISTRATION - UI - WEB_CAMERA_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
				"Initializing the controller to be used and imagetype to be captured");

		this.parentController = parentController;
		this.imageType = imageType;
	}

	public boolean isWebcamPluggedIn() {
		LOGGER.debug("REGISTRATION - UI - WEB_CAMERA_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
				"Connecting to the webcam");
		
		photoProvider = photoCaptureFacade.getPhotoProviderFactory(photoProviderName);
		if (webcam != null) {
			photoProvider.close(webcam);
		}
		webcam = photoProvider.connect(640, 480);
		if (webcam != null) {
			return true;
		} 
		return false;
	}

	@FXML
	public void captureImage(ActionEvent event) {
		LOGGER.debug("REGISTRATION - UI - WEB_CAMERA_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
				"capturing the image from webcam");
		if (capturedImage != null) {
			capturedImage.flush();
		}
		capturedImage = photoProvider.captureImage(webcam);
		parentController.saveApplicantPhoto(capturedImage, imageType);

		clear.setDisable(false);
	}

	@FXML
	public void clearImage(ActionEvent event) {
		LOGGER.debug("REGISTRATION - UI - WEB_CAMERA_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
				"clearing the image from webcam");

		parentController.clearPhoto(imageType);
		clear.setDisable(true);
	}

	@FXML
	public void closeWindow(ActionEvent event) {
		LOGGER.debug("REGISTRATION - UI - WEB_CAMERA_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
				"closing the webcam window");

		photoProvider.close(webcam);
		Stage stage = (Stage) ((Node) event.getSource()).getParent().getScene().getWindow();
		stage.close();
	}
}
