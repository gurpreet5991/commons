package io.mosip.registration.controller.reg;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bridj.cpp.std.list;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.dto.mastersync.BlacklistedWordsDto;
import io.mosip.registration.entity.BlacklistedWords;
import io.mosip.registration.service.MasterSyncService;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Class for validation of the Registration Field
 * 
 * @author Taleev.Aalam
 * @author Balaji
 * @since 1.0.0
 *
 */
@Component
public class Validations extends BaseController {

	/**
	 * Instance of {@link Logger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(Validations.class);
	private boolean isChild;
	private ResourceBundle validationBundle;
	private ResourceBundle applicationMessageBundle;
	private ResourceBundle localMessageBundle;
	private ResourceBundle applicationLabelBundle;
	private ResourceBundle localLabelBundle;
	private StringBuilder validationMessage;
	private List<String> applicationLanguageblackListedWords;
	private List<String> localLanguageblackListedWords;
	private List<String> noAlert;
	private boolean isLostUIN = false;

	/**
	 * Instantiates a new validations.
	 */
	public Validations() {
		try {
			noAlert = new ArrayList<>();
			noAlert.add("ageField");
			noAlert.add("dd");
			noAlert.add("mm");
			noAlert.add("yyyy");
			noAlert.add("ddLocalLanguage");
			noAlert.add("mmLocalLanguage");
			noAlert.add("yyyyLocalLanguage");
			noAlert.add("mobileNo");
			noAlert.add("postalCode");
			noAlert.add("cniOrPinNumber");
			noAlert.add("uinId");
			validationMessage = new StringBuilder();
		} catch (RuntimeException runtimeException) {
			LOGGER.error(RegistrationConstants.VALIDATION_LOGGER, APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}
	}

	/**
	 * Sets the resource bundles for Validations, Messages in Application and
	 * Secondary Languages and Labels in Application and Secondary Languages
	 */
	public void setResourceBundle() {
		validationBundle = ApplicationContext.applicationLanguageValidationBundle();
		applicationMessageBundle = ApplicationContext.applicationMessagesBundle();
		localMessageBundle = ApplicationContext.localMessagesBundle();
		applicationLabelBundle = ApplicationContext.applicationLanguageBundle();
		localLabelBundle = ApplicationContext.localLanguageBundle();
	}

	/**
	 * Iterate the fields to invoke the validate.
	 *
	 * @param pane
	 *            the {@link Pane} containing the fields
	 * @param notTovalidate
	 *            the {@link List} of UI fields not be validated
	 * @param isValid
	 *            the flag indicating whether validation is success or fail
	 * @param isConsolidated
	 *            the flag to indicate for displaying consolidated message
	 * @return true, if successful
	 */
	public boolean validateTheFields(Pane pane, List<String> notTovalidate, boolean isValid, String isConsolidated) {
		for (Node node : pane.getChildren()) {
			if (node instanceof Pane) {
				if (!validateTheFields((Pane) node, notTovalidate, isValid, isConsolidated)) {
					isValid = false;
				}
			} else if (nodeToValidate(notTovalidate, node)) {
					if (!validateTheNode(pane, node, node.getId(), isConsolidated)) {
						isValid = false;
					}
					if (isConsolidated.equals(RegistrationConstants.ENABLE)) {
						isValid = getValidationMessage().toString().isEmpty();
					}
				}
		}
		return isValid;
	}

	/**
	 * To mark as lost UIN for demographic fields validation.
	 *
	 * @param isLostUIN
	 *            the flag indicating whether work flow is for Lost UIN
	 */
	protected void updateAsLostUIN(boolean isLostUIN) {
		this.isLostUIN = isLostUIN;
	}

	/**
	 * To decide whether this node should be validated or not.
	 *
	 * @param notTovalidate
	 *            the {@link list} of fields not be validated
	 * @param node
	 *            the {@link Node} to be checked
	 * @return true, if successful
	 */
	private boolean nodeToValidate(List<String> notTovalidate, Node node) {
		return !(node.getId() == null || notTovalidate.contains(node.getId()) || node instanceof ImageView
				|| node instanceof Button || node instanceof Label || node instanceof Hyperlink);
	}

	/**
	 * Validate the UI fields. Fetch the {@link BlacklistedWords} for application
	 * specific and secondary specific languages.
	 *
	 * @param pane
	 *            the {@link Pane} containing the UI Fields to be validated
	 * @param notTovalidate
	 *            the {@link List} of fields not be validated
	 * @param isValid
	 *            the flag to indicating the status of validation
	 * @param masterSync
	 *            the instance of {@link MasterSyncService} for fetching
	 *            {@link BlacklistedWords}
	 * @return true, if successful
	 */
	public boolean validate(Pane pane, List<String> notTovalidate, boolean isValid, MasterSyncService masterSync) {
		this.applicationLanguageblackListedWords = masterSync
				.getAllBlackListedWords(ApplicationContext.applicationLanguage()).stream().map(BlacklistedWordsDto :: getWord)
				.collect(Collectors.toList());
		this.localLanguageblackListedWords = masterSync.getAllBlackListedWords(ApplicationContext.localLanguage())
				.stream().map(BlacklistedWordsDto :: getWord).collect(Collectors.toList());
		return validateTheFields(pane, notTovalidate, isValid, "N");
	}

	/**
	 * Pass the node to check for the validation, specific validation method will be
	 * called for each field.
	 *
	 * @param parentPane
	 *            the {@link Pane} containing the UI Fields to be validated
	 * @param node
	 *            the {@link Node} to be validated
	 * @param id
	 *            the id of the field to be validated
	 * @param isConsolidated
	 *            the flag to indicate for displaying consolidated message
	 * @return true, if successful
	 */
	public boolean validateTheNode(Pane parentPane, Node node, String id, String isConsolidated) {
		if (node instanceof ComboBox<?>) {
			return validateComboBox(parentPane, (ComboBox<?>) node, id, isConsolidated);
		}
		if (node instanceof VBox) {
			return validateDocument((VBox) node, id, isConsolidated);
		} else {
			return validateTextField(parentPane, (TextField) node, id, isConsolidated);
		}
	}

	/**
	 * Validate for the document upload.
	 *
	 * @param node the node
	 * @param id the id
	 * @param isConsolidated
	 *            the flag to indicate for displaying consolidated message
	 * @return true, if successful
	 */
	private boolean validateDocument(VBox node, String id, String isConsolidated) {
		boolean validated = false;
		try {

			outer: for (Node documentNode : node.getChildren()) {
				if (documentNode instanceof HBox) {
					for (Node innerNode : ((HBox) documentNode).getChildren()) {
						if (innerNode instanceof VBox) {
							validated = false;
							if (innerNode.isDisabled()) {
								validated = true;
							} else if (!(((VBox) innerNode).getChildren().isEmpty())) {
								validated = true;
							} else {
								/*
								 * generateAlert(messageBundle.getString( "docMandateMsg").replace("{}", ((VBox)
								 * innerNode).getId()), isConsolidated, validationMessage);
								 */
								node.requestFocus();
								break outer;
							}
						}
					}
				}
			}

		} catch (RuntimeException runtimeException) {
			LOGGER.error(RegistrationConstants.VALIDATION_LOGGER, APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}
		return node.getChildren().isEmpty() ? true : validated;
	}

	/**
	 * Validate for the TextField.
	 *
	 * @param parentPane
	 *            the {@link Pane} containing the fields
	 * @param node
	 *            the {@link Node} to be validated
	 * @param id
	 *            the id of the UI field
	 * @param isConsolidated
	 *            the flag to indicate for displaying consolidated message
	 * @return true, if successful
	 */
	public boolean validateTextField(Pane parentPane, TextField node, String id, String isConsolidated) {
		if (node.getId().contains(RegistrationConstants.LOCAL_LANGUAGE)) {
			return languageSpecificValidation(parentPane, node, id, isConsolidated, localLabelBundle,
					localMessageBundle, localLanguageblackListedWords);
		} else {
			return languageSpecificValidation(parentPane, node, id, isConsolidated, applicationLabelBundle,
					applicationMessageBundle, applicationLanguageblackListedWords);
		}
	}
	/**
	 * Language specific validation of text field
	 *
	 * @param parentPane
	 *            the {@link Pane} containing the fields
	 * @param node
	 *            the {@link Node} to be validated
	 * @param id
	 *            the id of the UI field
	 * @param isConsolidated
	 *            the flag to indicate for displaying consolidated message
	 * @return true, if successful
	 */
	private boolean languageSpecificValidation(Pane parentPane, TextField node, String id, String isConsolidated,
			ResourceBundle labelBundle, ResourceBundle messageBundle, List<String> blackListedWords) {
		boolean isInputValid = false;

		try {
			String label = id.replaceAll(RegistrationConstants.ON_TYPE, RegistrationConstants.EMPTY)
					.replaceAll(RegistrationConstants.LOCAL_LANGUAGE, RegistrationConstants.EMPTY);
			String[] validationProperty = getValidationProperties(id, isLostUIN);
			String regex = validationProperty[0];
			boolean isMandatory = RegistrationConstants.TRUE.equalsIgnoreCase(validationProperty[1]);
			boolean showAlert = (noAlert.contains(node.getId()) && id.contains(RegistrationConstants.ON_TYPE));

			String inputText = node.getText();

			if (node.isDisabled() || (!isMandatory && inputText.isEmpty())) {
				isInputValid = true;
			} else if (!id.contains(RegistrationConstants.ON_TYPE) && isMandatory && inputText.isEmpty()) {
				generateInvalidValueAlert(parentPane, id, isConsolidated, labelBundle.getString(label).concat(" ")
						.concat(messageBundle.getString(RegistrationConstants.REG_LGN_001)), showAlert);
			} else if (inputText.matches(regex)) {
				isInputValid = validateBlackListedWords(parentPane, node, id, isConsolidated, blackListedWords,
						showAlert,
						String.format("%s %s %s", messageBundle.getString(RegistrationConstants.BLACKLISTED_1),
								labelBundle.getString(label),
								messageBundle.getString(RegistrationConstants.BLACKLISTED_2)));
			} else {
				generateInvalidValueAlert(parentPane, id, isConsolidated,
						messageBundle.getString(label + "_" + RegistrationConstants.REG_DDC_004), showAlert);
			}
		} catch (RuntimeException runtimeException) {
			LOGGER.error(RegistrationConstants.VALIDATION_LOGGER, APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}

		return isInputValid;
	}

	private boolean validateBlackListedWords(Pane parentPane, TextField node, String id, String isConsolidated,
			List<String> blackListedWords, boolean showAlert, String errorMessage) {
		boolean isInputValid = false;
		if (blackListedWords != null && !id.contains(RegistrationConstants.ON_TYPE)) {
			String bWords = String.join(", ", Stream.of(node.getText().split("\\s+")).collect(Collectors.toList())
					.stream().filter(word -> blackListedWords.contains(word)).collect(Collectors.toList()));
			if (bWords.length() > 0) {
				generateInvalidValueAlert(parentPane, id, isConsolidated, String.format("%s %s", bWords, errorMessage),
						showAlert);
			} else {
				isInputValid = true;
			}
		} else {
			isInputValid = true;
		}
		return isInputValid;
	}

	private void generateInvalidValueAlert(Pane parentPane, String id, String isConsolidated, String message,
			boolean showAlert) {
		if (!showAlert)
			generateAlert(parentPane, id, message, isConsolidated, validationMessage);
	}

	/**
	 * Validate for the ComboBox type of node
	 */
	private boolean validateComboBox(Pane parentPane, ComboBox<?> node, String id, String isConsolidated) {
		boolean isComboBoxValueValid = false;
		try {
			if (id.matches(RegistrationConstants.POR_DOCUMENTS) && !isChild)
				return true;
			if (node.isDisabled())
				return true;
			if (isLostUIN) {
				return true;
			}
			if (id.equalsIgnoreCase(RegistrationConstants.DOCUMENT_SCAN_PANE)) {
				return true;
			}
			if (node.getValue() == null) {
				generateAlert(parentPane, id,
						applicationLabelBundle.getString(id).concat(" ")
								.concat(applicationMessageBundle.getString(RegistrationConstants.REG_LGN_001)),
						isConsolidated, validationMessage);
			} else {
				isComboBoxValueValid = true;
			}
		} catch (RuntimeException runtimeException) {
			LOGGER.error(RegistrationConstants.VALIDATION_LOGGER, APPLICATION_NAME, APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}
		return isComboBoxValueValid;
	}

	/**
	 * Validates the Parent or Guardian's UIN or RID UI Field
	 * 
	 * @param field
	 *            the {@link TextField} to be validated
	 * @param isChild
	 *            the flag to determine whether the individual or applicant is child
	 * @param uinValidator
	 *            the instance of {@link UinValidator} required to validate the UIN
	 * @param ridValidator
	 *            the instance of {@link RidValidator} required to validate the RID
	 * @return <code>true</code> if UIN or RID is valid, else <code>false</code>
	 */
	public boolean validateUinOrRid(TextField field, boolean isChild, UinValidator<String> uinValidator,
			RidValidator<String> ridValidator) {
		boolean isIdValid = false;

		if (isChild) {
			if (field.getText().length() <= Integer
					.parseInt((String) ApplicationContext.map().get(RegistrationConstants.UIN_LENGTH))) {
				try {
					isIdValid = uinValidator.validateId(field.getText());
				} catch (InvalidIDException invalidUinException) {
					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UIN_INVALID);
					LOGGER.error("UIN VALIDATOIN FAILED", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
							invalidUinException.getMessage() + ExceptionUtils.getStackTrace(invalidUinException));
				}
			} else {
				if (getRegistrationDTOFromSession().getSelectionListDTO() == null) {
					try {
						isIdValid = ridValidator.validateId(field.getText());
					} catch (InvalidIDException invalidRidException) {
						generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.RID_INVALID);
						LOGGER.error("RID VALIDATOIN FAILED", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
								invalidRidException.getMessage() + ExceptionUtils.getStackTrace(invalidRidException));
					}
				}else {
					generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UIN_INVALID);
				}
			}
		} else {
			isIdValid = true;
		}

		return isIdValid;
	}

	/**
	 * Validate for the single string.
	 *
	 * @param value
	 *            the value to be validated
	 * @param id
	 *            the id of the UI field whose value is provided as input
	 * @return <code>true</code>, if successful, else <code>false</code>
	 */
	public boolean validateSingleString(String value, String id) {
		String[] validationProperty = validationBundle.getString(id).split(RegistrationConstants.VALIDATION_SPLITTER);

		return value.matches(validationProperty[0]);
	}

	/**
	 * Check for child.
	 *
	 * @return true, if is child
	 */
	public boolean isChild() {
		return isChild;
	}

	/**
	 * Set for child.
	 *
	 * @param isChild the new child
	 */
	public void setChild(boolean isChild) {
		this.isChild = isChild;
	}

	/**
	 * Gets the validation message.
	 *
	 * @return the validation message
	 */
	public StringBuilder getValidationMessage() {
		return validationMessage;
	}

	/**
	 * Sets the validation message.
	 */
	public void setValidationMessage() {
		validationMessage.delete(0, validationMessage.length());
	}

	/**
	 * This method will get the validations property from the global param
	 *
	 * @param id
	 *            the id of the UI field
	 * @param isLostUIN
	 *            the flag to indicate for lost UIN
	 * @return String[]
	 */
	private String[] getValidationProperties(String id, boolean isLostUIN) {
		String[] validation = new String[2];

		if (validationBundle.containsKey(id)) {
			validation = validationBundle.getString(id).split(RegistrationConstants.VALIDATION_SPLITTER);
		} else {
			switch (id.replaceAll(RegistrationConstants.LOCAL_LANGUAGE, RegistrationConstants.EMPTY)
					.replaceAll(RegistrationConstants.ON_TYPE, RegistrationConstants.EMPTY)) {
			case "emailId":
				validation[0] = String
						.valueOf(ApplicationContext.map().get(RegistrationConstants.EMAIL_VALIDATION_REGEX));
				validation[1] = RegistrationConstants.TRUE;
				break;
			case "cniOrPinNumber":
				validation[0] = String
						.valueOf(ApplicationContext.map().get(RegistrationConstants.CNIE_VALIDATION_REGEX));
				validation[1] = RegistrationConstants.FALSE;
				break;
			case "mobileNo":
				validation[0] = String
						.valueOf(ApplicationContext.map().get(RegistrationConstants.PHONE_VALIDATION_REGEX));
				validation[1] = RegistrationConstants.TRUE;
				break;
			case "postalCode":
				validation[0] = String
						.valueOf(ApplicationContext.map().get(RegistrationConstants.POSTAL_CODE_VALIDATION_REGEX));
				validation[1] = RegistrationConstants.TRUE;
				break;
			default:
				validation[0] = ".*";
				validation[1] = RegistrationConstants.FALSE;
			}
		}

		if (isLostUIN) {
			validation[1] = RegistrationConstants.FALSE;
		}

		return validation;
	}

}
