package io.mosip.registration.controller.reg;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.AuditReferenceIdTypes;
import io.mosip.registration.constants.Components;
import io.mosip.registration.constants.RegistrationClientStatusCode;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.dto.PacketStatusDTO;
import io.mosip.registration.dto.RegistrationPacketSyncDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SyncRegistrationDTO;
import io.mosip.registration.entity.Registration;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.exception.RegistrationExceptionConstants;
import io.mosip.registration.service.packet.PacketUploadService;
import io.mosip.registration.service.sync.PacketSynchService;
import io.mosip.registration.util.healthcheck.RegistrationAppHealthCheckUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

@Controller
public class PacketUploadController extends BaseController {

	@FXML
	private ProgressIndicator progressIndicator;

	@Autowired
	private PacketUploadService packetUploadService;

	@FXML
	private TableColumn<PacketStatusDTO, String> fileNameColumn;

	@FXML
	private TableColumn<PacketStatusDTO, String> uploadStatusColumn;

	@FXML
	private TableView<PacketStatusDTO> table;

	@Autowired
	private PacketSynchService packetSynchService;

	@Autowired
	private PacketExportController packetExportController;

	private static final Logger LOGGER = AppConfig.getLogger(PacketUploadController.class);

	/**
	 * This method is used to Sync as well as upload the packets.
	 * 
	 */
	public void syncAndUploadPacket() {

		LOGGER.info("REGISTRATION - SYNCH_PACKETS_AND_PUSH_TO_SERVER - PACKET_UPLOAD_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "Sync the packets and push it to the server");
		table.getItems().clear();
		table.refresh();
		service.reset();
		try {
			if (RegistrationAppHealthCheckUtil.isNetworkAvailable()) {
				String packetSyncStatus = packetSynchService.packetSync();

				auditFactory.audit(AuditEvent.UPLOAD_PACKET, Components.UPLOAD_PACKET,
						SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

				progressIndicator.progressProperty().bind(service.progressProperty());
				service.start();
				service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent t) {
						String status = service.getValue();
						if (!status.equals(RegistrationConstants.EMPTY)) {
							String[] displayStatus = status.split("-");
							if (RegistrationConstants.PACKET_SYNC_ERROR.equals(displayStatus[0])) {
								if (!RegistrationConstants.EMPTY.equals(packetSyncStatus)) {
									generateAlert(displayStatus[0], displayStatus[1] + " " + packetSyncStatus);
								} else {
									generateAlert(displayStatus[0], displayStatus[1]);
								}
							} else {
								generateAlert(displayStatus[0], displayStatus[1]);
							}

						}
					}
				});
			} else {
				generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.NETWORK_ERROR);
			}
		} catch (RegBaseCheckedException checkedException) {
			LOGGER.info("REGISTRATION - UPLOAD_ERROR - PACKET_UPLOAD_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
					checkedException.getMessage() + ExceptionUtils.getStackTrace(checkedException));
			generateAlert(RegistrationConstants.ERROR, checkedException.getErrorText());
		}

	}

	

	/**
	 * This anonymous service class will do the packet upload as well as the upload
	 * progress.
	 * 
	 */
	Service<String> service = new Service<String>() {
		@Override
		protected Task<String> createTask() {
			return /**
					 * @author SaravanaKumar
					 *
					 */
			new Task<String>() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see javafx.concurrent.Task#call()
				 */
				@Override
				protected String call() {

					LOGGER.info("REGISTRATION - HANDLE_PACKET_UPLOAD_START - PACKET_UPLOAD_CONTROLLER",
							APPLICATION_NAME, APPLICATION_ID, "Handling all the packet upload activities");
					List<Registration> synchedPackets = packetUploadService.getSynchedPackets();
					List<Registration> packetUploadList = new ArrayList<>();
					String status = "";
					Map<String, String> tableMap = new WeakHashMap<>();
					if (!synchedPackets.isEmpty()) {
						auditFactory.audit(AuditEvent.PACKET_UPLOAD, Components.PACKET_UPLOAD,
								SessionContext.userContext().getUserId(), RegistrationConstants.PACKET_UPLOAD_REF_ID);

						progressIndicator.setVisible(true);
						for (int i = 0; i < synchedPackets.size(); i++) {
							Registration synchedPacket = synchedPackets.get(i);
							synchedPacket.setUploadCount((short) (synchedPacket.getUploadCount() + 1));
							String ackFileName = synchedPacket.getAckFilename();
							int lastIndex = ackFileName.indexOf(RegistrationConstants.ACKNOWLEDGEMENT_FILE);
							String packetPath = ackFileName.substring(0, lastIndex);
							File packet = new File(packetPath + RegistrationConstants.ZIP_FILE_EXTENSION);
							try {
								if (packet.exists()) {
									if ((("RESEND".equals(synchedPacket.getServerStatusCode())
											&& synchedPacket.getServerStatusTimestamp()
													.compareTo(synchedPacket.getUploadTimestamp()) == 1)
											|| RegistrationClientStatusCode.META_INFO_SYN_SERVER.getCode()
													.equals(synchedPacket.getClientStatusCode())
											|| RegistrationClientStatusCode.EXPORT.getCode()
													.equals(synchedPacket.getClientStatusCode())
											|| "E".equals(synchedPacket.getFileUploadStatus())) && packet.exists()) {

										ResponseDTO response = packetUploadService.pushPacket(packet);
										if (response.getSuccessResponseDTO() != null) {

											synchedPacket.setClientStatusCode(
													RegistrationClientStatusCode.UPLOADED_SUCCESSFULLY.getCode());
											synchedPacket.setFileUploadStatus(
													RegistrationClientStatusCode.UPLOAD_SUCCESS_STATUS.getCode());
											packetUploadList.add(synchedPacket);
											tableMap.put(synchedPacket.getId(),
													RegistrationConstants.PACKET_UPLOAD_SUCCESS);

										} else if (response.getErrorResponseDTOs() != null) {
											String errMessage = response.getErrorResponseDTOs().get(0).getMessage();
											if (errMessage.contains(RegistrationConstants.PACKET_DUPLICATE)) {

												tableMap.put(synchedPacket.getId(), "Error(Duplicate Packet)");
												synchedPacket.setClientStatusCode(
														RegistrationClientStatusCode.UPLOADED_SUCCESSFULLY.getCode());
												synchedPacket.setFileUploadStatus(
														RegistrationClientStatusCode.UPLOAD_SUCCESS_STATUS.getCode());
												packetUploadList.add(synchedPacket);

											} else {
												synchedPacket.setFileUploadStatus(
														RegistrationClientStatusCode.UPLOAD_ERROR_STATUS.getCode());
												packetUploadList.add(synchedPacket);
												tableMap.put(synchedPacket.getId(), "Error");
											}
										}

									}
								} else {
									tableMap.put(synchedPacket.getId(), "Error(Packet not available)");
								}

							} catch (URISyntaxException uriSyntaxException) {

								LOGGER.error("REGISTRATION - HANDLE_PACKET_UPLOAD_URI_ERROR - PACKET_UPLOAD_CONTROLLER",
										APPLICATION_NAME, APPLICATION_ID,
										"Error in uri syntax" + ExceptionUtils.getStackTrace(uriSyntaxException));
								status = "Error-Unable to push packets to the server.";
							} catch (RegBaseCheckedException regBaseCheckedException) {
								LOGGER.error("REGISTRATION - HANDLE_PACKET_UPLOAD_ERROR - PACKET_UPLOAD_CONTROLLER",
										APPLICATION_NAME, APPLICATION_ID, "Error while pushing packets to the server"
												+ ExceptionUtils.getStackTrace(regBaseCheckedException));

								synchedPacket.setFileUploadStatus(
										RegistrationClientStatusCode.UPLOAD_ERROR_STATUS.getCode());
								tableMap.put(synchedPacket.getId(), "Error(Service Error)");
								packetUploadList.add(synchedPacket);
								synchedPacket.setUploadCount((short) (synchedPacket.getUploadCount() + 1));

							} catch (RuntimeException runtimeException) {
								LOGGER.error(
										"REGISTRATION - HANDLE_PACKET_UPLOAD_RUNTIME_ERROR - PACKET_UPLOAD_CONTROLLER",
										APPLICATION_NAME, APPLICATION_ID,
										"Run time error while connecting to the server"
												+ ExceptionUtils.getStackTrace(runtimeException));
								if (i == 0) {
									status = "Error-Unable to push packets to the server.";
								} else if (i > 0) {
									status = "Error-Unable to push some packets to the server.";
								}
								for (int count = i; count < synchedPackets.size(); count++) {
									synchedPacket = synchedPackets.get(count);
									synchedPacket.setFileUploadStatus(
											RegistrationClientStatusCode.UPLOAD_ERROR_STATUS.getCode());
									synchedPacket.setUploadCount((short) (synchedPacket.getUploadCount() + 1));
									packetUploadList.add(synchedPacket);
									tableMap.put(synchedPacket.getId(), "Error");
								}
								break;
							}

							this.updateProgress(i, synchedPackets.size());
						}
						packetUploadService.updateStatus(packetUploadList);
						progressIndicator.setVisible(false);
						displayData(populateTableData(tableMap));
					} else {
						status = "Info-No packets to upload.";
					}

					return status;
				}
			};
		}
	};

	/**
	 * Export the packets and show the exported packets in the table
	 */
	public void packetExport() {

		LOGGER.info("REGISTRATION - PACKET_EXPORT_START - PACKET_UPLOAD_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
				"Exporting the Synched the packets");

		List<Registration> exportedPackets = packetExportController.packetExport();
		Map<String, String> exportedPacketMap = new WeakHashMap<>();
		exportedPackets.forEach(regPacket -> {
			exportedPacketMap.put(regPacket.getId(), RegistrationClientStatusCode.EXPORT.getCode());
		});
		displayData(populateTableData(exportedPacketMap));
	}

	/**
	 * To display the Uploaded packet details in UI
	 * 
	 * @param tableData
	 */
	private void displayData(List<PacketStatusDTO> tableData) {
		
		LOGGER.info("REGISTRATION - DISPLAY_DATA - PACKET_UPLOAD_CONTROLLER", APPLICATION_NAME, APPLICATION_ID,
				"To display all the ui data");
		fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
		uploadStatusColumn.setCellValueFactory(new PropertyValueFactory<>("uploadStatus"));

		ObservableList<PacketStatusDTO> list = FXCollections.observableArrayList(tableData);
		table.setItems(list);
	}

	/**
	 * To populate the data for the UI table
	 * 
	 * @param verifiedPackets
	 * @return
	 */
	private List<PacketStatusDTO> populateTableData(Map<String, String> packetStatus) {
		LOGGER.info("REGISTRATION - POPULATE_UI_TABLE_DATA - PACKET_UPLOAD_CONTROLLER", APPLICATION_NAME,
				APPLICATION_ID, "Populating the table data with the Updated details");
		List<PacketStatusDTO> listUploadStatus = new ArrayList<>();
		packetStatus.forEach((id, status) -> {
			PacketStatusDTO packetUploadStatusDTO = new PacketStatusDTO();
			packetUploadStatusDTO.setUploadStatus(status);
			packetUploadStatusDTO.setFileName(id);
			listUploadStatus.add(packetUploadStatusDTO);

		});
		return listUploadStatus;
	}
}