package io.mosip.registration.service.packet.impl;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.audit.AuditManagerService;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.AuditReferenceIdTypes;
import io.mosip.registration.constants.Components;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dao.RegistrationDAO;
import io.mosip.registration.dto.RegistrationApprovalDTO;
import io.mosip.registration.entity.Registration;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.packet.RegistrationApprovalService;

/**
 * {@code RegistrationApprovalServiceImpl} is the registration approval service
 * class
 *
 * @author Mahesh Kumar
 */
@Service
public class RegistrationApprovalServiceImpl implements RegistrationApprovalService {

	/**
	 * Object for Registration DAO
	 */
	@Autowired
	private RegistrationDAO registrationDAO;

	/**
	 * Object for Logger
	 */
	private static final Logger LOGGER = AppConfig.getLogger(RegistrationApprovalServiceImpl.class);

	/**
	 * Instance of {@code AuditFactory}
	 */
	@Autowired
	private AuditManagerService auditFactory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.service.RegistrationApprovalService#
	 * getEnrollmentByStatus(java.lang.String)
	 */
	public List<RegistrationApprovalDTO> getEnrollmentByStatus(String status) {
		LOGGER.info("REGISTRATION - PACKET - RETRIVE", APPLICATION_NAME, APPLICATION_ID,
				"Fetching Packets list by status started");
		auditFactory.audit(AuditEvent.PACKET_RETRIVE, Components.PACKET_RETRIVE,
				SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		List<RegistrationApprovalDTO> list = new ArrayList<>();
		try {
			List<Registration> details = registrationDAO.getEnrollmentByStatus(status);

			LOGGER.info("REGISTRATION - PACKET - RETRIVE", APPLICATION_NAME, APPLICATION_ID,
					"Packet  list has been fetched");
			auditFactory.audit(AuditEvent.PACKET_RETRIVE, Components.PACKET_RETRIVE,
					SessionContext.userContext().getUserId(), AuditReferenceIdTypes.USER_ID.getReferenceTypeId());
			details.forEach(detail -> list.add(new RegistrationApprovalDTO(detail.getId(),
					regDateConversion(detail.getCrDtime()), detail.getAckFilename(), RegistrationConstants.EMPTY)));
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.PACKET_RETRIVE_STATUS,
					runtimeException.toString());
		}
		LOGGER.info("REGISTRATION - PACKET - RETRIVE", APPLICATION_NAME, APPLICATION_ID,
				"Fetching Packets list by status ended");
		return list;
	}

	/**
	 * Registration date conversion.
	 *
	 * @param timestamp the timestamp
	 * @return the string
	 */
	private String regDateConversion(Timestamp timestamp) {

		DateFormat dateFormat = new SimpleDateFormat(RegistrationConstants.EOD_PROCESS_DATE_FORMAT);
		Date date = new Date(timestamp.getTime());
		return dateFormat.format(date);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.RegistrationApprovalService#packetUpdateStatus(
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	public Registration updateRegistration(String registrationID, String statusComments, String clientStatusCode) {

		LOGGER.info("REGISTRATION - PACKET - UPDATE", APPLICATION_NAME, APPLICATION_ID, "Updating status of Packet");
		auditFactory.audit(AuditEvent.PACKET_UPDATE, Components.PACKET_UPDATE, SessionContext.userContext().getUserId(),
				AuditReferenceIdTypes.USER_ID.getReferenceTypeId());

		return registrationDAO.updateRegistration(registrationID, statusComments, clientStatusCode);
	}

}
