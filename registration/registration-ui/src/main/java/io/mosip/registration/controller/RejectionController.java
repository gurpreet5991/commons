package io.mosip.registration.controller;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationClientStatusCode;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dto.RegistrationApprovalDTO;
import io.mosip.registration.entity.GlobalContextParam;
import io.mosip.registration.service.GlobalContextParamService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
*
* {@code RejectionController} is the controller class for rejection of packets
* @author Mahesh Kumar
*/
@Controller
public class RejectionController extends BaseController implements Initializable{
	/**
	 * Stage
	 */
	private Stage rejPrimarystage;
			
	/**
	 * Instance of {@link Logger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(RejectionController.class);

	/**
	 * Object for RegistrationApprovalController
	 */
	@Autowired
	private RegistrationApprovalController registrationApprovalController;
	
	@Autowired
	private GlobalContextParamService globalContextParamService;
	
	/**
	 * Combobox for for rejection reason
	 */
	@FXML
	private ComboBox<String> rejectionComboBox;
	/**
	 * Button for Submit
	 */
	@FXML
	private Button rejectionSubmit;

	/**
	 * HyperLink for Exit
	 */
	@FXML
	private Hyperlink rejectionExit;

	private TableView<RegistrationApprovalDTO> rejtable;

	private List<Map<String, String>> rejectionmapList;

	/** The rej reg data. */
	private RegistrationApprovalDTO rejRegData;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javafx.fxml.Initializable#initialize(java.net.URL,
	 * java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOGGER.debug("REGISTRATION - PAGE_LOADING - REGISTRATION_REJECTION_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "Page loading has been started");
		GlobalContextParam globalContextParam = globalContextParamService.findRejectionOnholdComments("REJECT_COMMENTS");
		rejectionSubmit.disableProperty().set(true);
		rejectionComboBox.getItems().clear();
		rejectionComboBox.setItems(FXCollections.observableArrayList(globalContextParam.getVal().split(",")));
	}

	/**
	 * Method to get the Stage, Registration Data,Table Data and list map from the other controller page.
	 *
	 * @param regData 
	 * @param stage 
	 * @param mapList 
	 * @param table 
	 */
	public void initData(RegistrationApprovalDTO regData,Stage stage,List<Map<String, String>> mapList,TableView<RegistrationApprovalDTO> table) {
		rejRegData=regData;
		rejPrimarystage = stage;
		rejectionmapList=mapList;
		rejtable=table;
	}

	/**
	 * {@code updatePacketStatus} is event class for updating packet status to
	 * reject
	 * 
	 * @param event
	 */
	public void packetUpdateStatus(ActionEvent event) {
		LOGGER.debug("REGISTRATION - UPDATE_PACKET_STATUS - REGISTRATION_REJECTION_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "Packet updation as rejection has been started");
		Map<String, String> map = new HashMap<>();
		map.put("registrationID",rejRegData.getId()); 
		map.put("statusCode", RegistrationClientStatusCode.REJECTED.getCode());
		map.put("statusComment", rejectionComboBox.getSelectionModel().getSelectedItem());
		rejectionmapList.add(map);

		generateAlert(RegistrationConstants.STATUS, AlertType.INFORMATION, RegistrationConstants.REJECTED_STATUS_MESSAGE);
		rejectionSubmit.disableProperty().set(true);		
		rejPrimarystage.close();
		rejtable.getItems().remove(rejRegData);
		registrationApprovalController.setInvisible();
		LOGGER.debug("REGISTRATION - UPDATE_PACKET_STATUS - REGISTRATION_REJECTION_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "Packet updation as rejection has been started");
	}

	/**
	 * {@code rejectionWindowExit} is event class to exit from reason for rejection
	 * pop up window.
	 * 
	 * @param event
	 */
	public void rejectionWindowExit(ActionEvent event) {
		LOGGER.debug("REGISTRATION - PAGE_LOADING - REGISTRATION_REJECTION_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "Rejection Popup window is closed");
		rejPrimarystage.close();
	}

	/**
	 * Rejection combobox action.
	 * 
	 * @param event
	 */
	public void rejectionComboboxAction(ActionEvent event) {
		rejectionSubmit.disableProperty().set(false);
	}
}
