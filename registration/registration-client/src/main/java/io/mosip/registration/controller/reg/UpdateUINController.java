package io.mosip.registration.controller.reg;

import static io.mosip.registration.constants.LoggerConstants.LOG_REG_UIN_UPDATE;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.FXUtils;
import io.mosip.registration.dto.SelectionListDTO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * UpdateUINController Class.
 * 
 * @author Mahesh Kumar
 *
 */
@Controller
public class UpdateUINController extends BaseController implements Initializable {

	private static final List<String> UIN_UPDATE_CONFIGURED_DEMOGRAPHIC_FIELDS_LIST = Arrays.asList(
			RegistrationConstants.UIN_UPDATE_NAME, RegistrationConstants.UIN_UPDATE_AGE,
			RegistrationConstants.UIN_UPDATE_GENDER, RegistrationConstants.UIN_UPDATE_ADDRESS,
			RegistrationConstants.UIN_UPDATE_CONTACT_DETAILS, RegistrationConstants.UIN_UPDATE_PARENT_DETAILS,
			RegistrationConstants.UIN_UPDATE_FOREIGNER);

	private static final List<String> UIN_UPDATE_CONFIGURED_BIO_FIELDS_LIST = Arrays.asList(
			RegistrationConstants.UIN_UPDATE_BIO_EXCEPTION, RegistrationConstants.UIN_UPDATE_BIO_FP,
			RegistrationConstants.UIN_UPDATE_BIO_IRIS, RegistrationConstants.UIN_UPDATE_CNIE_NUMBER);

	private static final Logger LOGGER = AppConfig.getLogger(UpdateUINController.class);

	@Autowired
	private RegistrationController registrationController;

	@FXML
	private TextField uinId;
	@FXML
	private CheckBox name;
	@FXML
	private CheckBox age;
	@FXML
	private CheckBox gender;
	@FXML
	private CheckBox address;
	@FXML
	private CheckBox contactDetails;
	@FXML
	private CheckBox biometricException;
	@FXML
	private CheckBox biometricIris;
	@FXML
	private CheckBox biometricFingerprint;
	@FXML
	private CheckBox cnieNumber;
	@FXML
	private CheckBox parentOrGuardianDetails;
	@FXML
	private CheckBox foreigner;
	@FXML
	private Label toggleLabel1;
	@FXML
	private Label toggleLabel2;
	@FXML
	private HBox biometricBox;
	@FXML
	private HBox demographicHBox;
	@FXML
	private AnchorPane uinUpdateRoot;
	private SimpleBooleanProperty switchedOn;
	private boolean isChild;

	@Autowired
	private UinValidator<String> uinValidatorImpl;

	@Autowired
	Validations validation;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javafx.fxml.Initializable#initialize(java.net.URL,
	 * java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		try {
			switchedOn = new SimpleBooleanProperty(false);
			switchedOn.set(false);
			isChild = switchedOn.get();
			if (!isChild) {
				parentOrGuardianDetails.setDisable(true);
			}
			toggleFunction();
			FXUtils fxUtils = FXUtils.getInstance();
			listenerOnFields(fxUtils);
			SessionContext.map().put(RegistrationConstants.IS_CONSOLIDATED, RegistrationConstants.DISABLE);
			fxUtils.validateOnType(uinUpdateRoot, uinId, validation);
			biometricBox.getChildren().forEach(bio -> {
				if (fingerprintDisableFlag.equals(RegistrationConstants.DISABLE)
						&& bio.getId().equals(RegistrationConstants.UIN_UPDATE_BIO_FP)) {
					bio.setVisible(false);
					bio.setManaged(false);
				}
				if (irisDisableFlag.equals(RegistrationConstants.DISABLE)
						&& bio.getId().equals(RegistrationConstants.UIN_UPDATE_BIO_IRIS)) {
					bio.setVisible(false);
					bio.setManaged(false);
				}
				if (fingerprintDisableFlag.equals(RegistrationConstants.DISABLE)
						&& irisDisableFlag.equals(RegistrationConstants.DISABLE)
						&& bio.getId().equals(RegistrationConstants.UIN_UPDATE_BIO_EXCEPTION)) {
					bio.setVisible(false);
					bio.setManaged(false);
				}
			});
			updateUINFieldsConfiguration();

		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_UIN_UPDATE, APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}
	}

	/**
	 * Update UIN fields configuration.
	 */
	private void updateUINFieldsConfiguration() {
		List<String> configuredFieldsfromDB = Arrays.asList(
				String.valueOf(ApplicationContext.map().get(RegistrationConstants.UIN_UPDATE_CONFIG_FIELDS_FROM_DB))
						.split(","));

		for (String configureField : UIN_UPDATE_CONFIGURED_DEMOGRAPHIC_FIELDS_LIST) {
			if (!configuredFieldsfromDB.contains(configureField)) {
				demographicHBox.getChildren().forEach(demographicNode -> {
					if (demographicNode.getId().equals(configureField)) {
						demographicNode.setVisible(false);
						demographicNode.setManaged(false);
					}
				});
			}

		}

		for (String configureField : UIN_UPDATE_CONFIGURED_BIO_FIELDS_LIST) {
			if (!configuredFieldsfromDB.contains(configureField)) {
				biometricBox.getChildren().forEach(demographicNode -> {
					if (demographicNode.getId().equals(configureField)) {
						demographicNode.setVisible(false);
						demographicNode.setManaged(false);
					}
				});
			} else {
				biometricBox.getChildren().forEach(demographicNode -> {
					if (demographicNode.getId().equals(RegistrationConstants.UIN_UPDATE_BIO_FP)
							&& fingerprintDisableFlag.equals(RegistrationConstants.DISABLE)) {
						demographicNode.setVisible(false);
						demographicNode.setManaged(false);
					} else if (demographicNode.getId().equals(RegistrationConstants.UIN_UPDATE_BIO_IRIS)
							&& irisDisableFlag.equals(RegistrationConstants.DISABLE)) {
						demographicNode.setVisible(false);
						demographicNode.setManaged(false);
					} else if (demographicNode.getId().equals(RegistrationConstants.UIN_UPDATE_BIO_EXCEPTION)
							&& fingerprintDisableFlag.equals(RegistrationConstants.DISABLE)
							&& irisDisableFlag.equals(RegistrationConstants.DISABLE)) {
						demographicNode.setVisible(false);
						demographicNode.setManaged(false);
					}
				});
			}
		}
	}

	private void listenerOnFields(FXUtils fxUtils) {
		fxUtils.listenOnSelectedCheckBox(name);
		fxUtils.listenOnSelectedCheckBox(age);
		fxUtils.listenOnSelectedCheckBox(gender);
		fxUtils.listenOnSelectedCheckBox(address);
		fxUtils.listenOnSelectedCheckBox(contactDetails);
		fxUtils.listenOnSelectedCheckBox(biometricException);
		fxUtils.listenOnSelectedCheckBox(biometricIris);
		fxUtils.listenOnSelectedCheckBox(biometricFingerprint);
		fxUtils.listenOnSelectedCheckBox(cnieNumber);
		fxUtils.listenOnSelectedCheckBox(parentOrGuardianDetails);
		fxUtils.listenOnSelectedCheckBox(foreigner);
	}

	/**
	 * Toggle functionality to give individual is adult or child.
	 */
	private void toggleFunction() {
		try {
			LOGGER.info(LOG_REG_UIN_UPDATE, APPLICATION_NAME, APPLICATION_ID,
					"Entering into toggle function for toggle label 1 and toggle level 2");

			switchedOn.addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
					if (newValue) {
						toggleLabel1.setLayoutX(30);
						isChild = newValue;
						biometricException.setDisable(true);
						biometricFingerprint.setDisable(true);
						biometricIris.setDisable(true);
						parentOrGuardianDetails.setDisable(false);
						biometricException.selectedProperty().set(false);
						biometricFingerprint.selectedProperty().set(false);
						biometricIris.selectedProperty().set(false);
					} else {
						toggleLabel1.setLayoutX(0);
						isChild = newValue;
						parentOrGuardianDetails.setDisable(true);
						biometricException.setDisable(false);
						biometricFingerprint.setDisable(false);
						biometricIris.setDisable(false);
						parentOrGuardianDetails.selectedProperty().set(false);
					}
				}
			});

			toggleLabel1.setOnMouseClicked(event -> switchedOn.set(!switchedOn.get()));
			toggleLabel2.setOnMouseClicked(event -> switchedOn.set(!switchedOn.get()));
			LOGGER.info(LOG_REG_UIN_UPDATE, APPLICATION_NAME, APPLICATION_ID,
					"Exiting the toggle function for toggle label 1 and toggle level 2");
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_UIN_UPDATE, APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}
	}

	/**
	 * Submitting for UIN update after selecting the required fields.
	 *
	 * @param event the event
	 */
	@FXML
	public void submitUINUpdate(ActionEvent event) {
		LOGGER.info(LOG_REG_UIN_UPDATE, APPLICATION_NAME, APPLICATION_ID, "Updating UIN details");
		try {

			if (StringUtils.isEmpty(uinId.getText())) {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UPDATE_UIN_ENTER_UIN_ALERT);
			} else {

				if (uinValidatorImpl.validateId(uinId.getText())) {

					SelectionListDTO selectionListDTO = new SelectionListDTO();

					selectionListDTO.setName(name.isSelected());
					selectionListDTO.setAge(age.isSelected());
					selectionListDTO.setGender(gender.isSelected());
					selectionListDTO.setAddress(address.isSelected());
					selectionListDTO.setContactDetails(contactDetails.isSelected());
					selectionListDTO.setBiometricException(biometricException.isSelected());
					selectionListDTO.setBiometricIris(biometricIris.isSelected());
					selectionListDTO.setBiometricFingerprint(biometricFingerprint.isSelected());
					selectionListDTO.setCnieNumber(cnieNumber.isSelected());
					selectionListDTO.setParentOrGuardianDetails(parentOrGuardianDetails.isSelected());
					selectionListDTO.setForeigner(foreigner.isSelected());

					selectionListDTO.setChild(isChild);
					selectionListDTO.setUinId(uinId.getText());

					if (name.isSelected() || age.isSelected() || gender.isSelected() || address.isSelected()
							|| contactDetails.isSelected() || biometricException.isSelected()
							|| biometricIris.isSelected() || biometricFingerprint.isSelected()
							|| cnieNumber.isSelected() || parentOrGuardianDetails.isSelected()
							|| foreigner.isSelected()) {
						registrationController.init(selectionListDTO);

						Parent createRoot = BaseController.load(
								getClass().getResource(RegistrationConstants.CREATE_PACKET_PAGE),
								applicationContext.getApplicationLanguageBundle());

						getScene(createRoot).setRoot(createRoot);
					} else {
						generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UPDATE_UIN_SELECTION_ALERT);
					}
				}
			}
		} catch (InvalidIDException invalidIdException) {
			LOGGER.error(LOG_REG_UIN_UPDATE, APPLICATION_NAME, APPLICATION_ID,
					invalidIdException.getMessage() + ExceptionUtils.getStackTrace(invalidIdException));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UPDATE_UIN_VALIDATION_ALERT);
		} catch (IOException ioException) {
			LOGGER.error(LOG_REG_UIN_UPDATE, APPLICATION_NAME, APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_REG_PAGE);
		}
	}
}
