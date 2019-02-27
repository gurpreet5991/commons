package io.mosip.registration.dao.impl;

import static io.mosip.registration.constants.LoggerConstants.LOG_SAVE_PKT;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationClientStatusCode;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationTransactionType;
import io.mosip.registration.constants.RegistrationType;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dao.RegistrationDAO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.entity.Registration;
import io.mosip.registration.entity.RegistrationTransaction;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.repositories.RegistrationRepository;

/**
 * The implementation class of {@link RegistrationDAO}.
 *
 * @author Balaji Sridharan
 * @author Mahesh Kumar
 * @author Saravanakumar Gnanaguru
 * @since 1.0.0
 */
@Repository
@Transactional
public class RegistrationDAOImpl implements RegistrationDAO {

	/** The registration repository. */
	@Autowired
	private RegistrationRepository registrationRepository;

	/** Object for Logger. */
	private static final Logger LOGGER = AppConfig.getLogger(RegistrationDAOImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.dao.RegistrationDAO#save(java.lang.String,
	 * io.mosip.registration.dto.RegistrationDTO)
	 */
	@Override
	public void save(String zipFileName, RegistrationDTO registrationDTO) throws RegBaseCheckedException {
		try {
			LOGGER.info(LOG_SAVE_PKT, APPLICATION_NAME, APPLICATION_ID, "Save Registartion had been started");

			Timestamp time = new Timestamp(System.currentTimeMillis());

			Registration registration = new Registration();
			registration.setId(registrationDTO.getRegistrationId());
			registration.setRegType(RegistrationType.NEW.getCode());
			registration.setRefRegId("12345");
			registration.setStatusCode(RegistrationClientStatusCode.CREATED.getCode());
			registration.setLangCode("ENG");
			registration.setStatusTimestamp(time);
			registration.setAckFilename(zipFileName + "_Ack." + RegistrationConstants.ACKNOWLEDGEMENT_FORMAT);
			registration.setClientStatusCode(RegistrationClientStatusCode.CREATED.getCode());
			registration.setUploadCount((short) 1);
			registration.setRegCntrId(SessionContext.userContext().getRegistrationCenterDetailDTO()
					.getRegistrationCenterId());
			registration.setIsActive(true);
			registration.setCrBy(SessionContext.userContext().getUserId());
			registration.setCrDtime(time);
			registration.setRegUsrId(SessionContext.userContext().getUserId());
			registration.setApproverUsrId(SessionContext.userContext().getUserId());

			List<RegistrationTransaction> registrationTransactions = new ArrayList<>();
			RegistrationTransaction registrationTxn = new RegistrationTransaction();
			registrationTxn.setRegId(registration.getId());
			registrationTxn.setTrnTypeCode(RegistrationTransactionType.CREATED.getCode());
			registrationTxn.setLangCode("ENG");
			registrationTxn.setStatusCode(RegistrationClientStatusCode.CREATED.getCode());
			registrationTxn.setCrBy(SessionContext.userContext().getUserId());
			registrationTxn.setCrDtime(time);
			registrationTransactions.add(registrationTxn);
			registration.setRegistrationTransaction(registrationTransactions);

			registrationRepository.create(registration);

			LOGGER.info(LOG_SAVE_PKT, APPLICATION_NAME, APPLICATION_ID, "Save Registration had been ended");
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.CREATE_PACKET_ENTITY,
					runtimeException.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mosip.registration.dao.RegistrationDAO#updateStatus(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Registration updateRegistration(String registrationID, String statusComments, String clientStatusCode) {
		try {
			LOGGER.info("REGISTRATION - UPDATE_STATUS - REGISTRATION_DAO", APPLICATION_NAME, APPLICATION_ID,
					"Packet updation has been started");

			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			Registration registration = registrationRepository.getOne(registrationID);
			registration.setStatusCode(clientStatusCode);
			registration.setStatusTimestamp(timestamp);
			registration.setClientStatusCode(clientStatusCode);
			registration.setClientStatusTimestamp(timestamp);
			registration.setClientStatusComments(statusComments);
			registration.setApproverUsrId(SessionContext.userContext().getUserId());
			registration.setApproverRoleCode(SessionContext.userContext().getRoles().get(0));
			registration.setUpdBy(SessionContext.userContext().getUserId());
			registration.setUpdDtimes(timestamp);

			List<RegistrationTransaction> registrationTransaction = registration.getRegistrationTransaction();

			RegistrationTransaction registrationTxn = new RegistrationTransaction();
			registrationTxn.setRegId(registrationID);
			registrationTxn.setTrnTypeCode(RegistrationTransactionType.UPDATED.getCode());
			registrationTxn.setLangCode("ENG");
			registrationTxn.setStatusCode(clientStatusCode);
			registrationTxn.setStatusComment(statusComments);
			registrationTxn.setCrBy(SessionContext.userContext().getUserId());
			registrationTxn.setCrDtime(timestamp);
			registrationTransaction.add(registrationTxn);

			LOGGER.info("REGISTRATION - UPDATE_STATUS - REGISTRATION_DAO", APPLICATION_NAME, APPLICATION_ID,
					"Packet updation has been ended");

			return registrationRepository.update(registration);
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.PACKET_UPDATE_STATUS,
					runtimeException.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mosip.registration.dao.RegistrationDAO#getEnrollmentByStatus(java.lang.
	 * String)
	 */
	@Override
	public List<Registration> getEnrollmentByStatus(String status) {
		LOGGER.info("REGISTRATION - BY_STATUS - REGISTRATION_DAO", APPLICATION_NAME, APPLICATION_ID,
				"Retriving packets based on status");

		return registrationRepository.findByclientStatusCode(status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.dao.RegistrationDAO#getPacketsToBeSynched(java.util.
	 * List)
	 */
	public List<Registration> getPacketsToBeSynched(List<String> statusCodes) {
		return registrationRepository.findByClientStatusCodeIn(statusCodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.dao.RegistrationDAO#getRegistrationByStatus(java.util.
	 * List)
	 */
	@Override
	public List<Registration> getRegistrationByStatus(List<String> packetStatus) {
		LOGGER.info("REGISTRATION - GET_PACKET_DETAILS_BY_ID - REGISTRATION_DAO", APPLICATION_NAME, APPLICATION_ID,
				"got the packet details by id");

		return registrationRepository.findByStatusCodes(packetStatus.get(0), packetStatus.get(1), packetStatus.get(2), packetStatus.get(3));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mosip.registration.dao.RegistrationDAO#updateRegStatus(java.lang.String)
	 */
	public Registration updateRegStatus(Registration registrationPacket) {
		LOGGER.info("REGISTRATION - UPDATE_THE_PACKET_STATUS - REGISTRATION_DAO", APPLICATION_NAME, APPLICATION_ID,
				"Updating the packet details in the Registation table");

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		Registration reg = registrationRepository.getOne(registrationPacket.getId());
		reg.setClientStatusCode(registrationPacket.getClientStatusCode());
		if (registrationPacket.getFileUploadStatus() != null) {
			reg.setFileUploadStatus(registrationPacket.getFileUploadStatus());
		}
		reg.setIsActive(true);
		reg.setUploadTimestamp(timestamp);
		reg.setClientStatusTimestamp(timestamp);
		reg.setRegistrationTransaction(buildRegistrationTransaction(reg));
		reg.setClientStatusComments(registrationPacket.getClientStatusComments());
		return registrationRepository.update(reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.dao.RegistrationDAO#updatePacketSyncStatus(io.mosip.
	 * registration.entity.Registration)
	 */
	public Registration updatePacketSyncStatus(Registration packet) {
		LOGGER.info("REGISTRATION - UPDATE_THE_PACKET_STATUS - REGISTRATION_DAO", APPLICATION_NAME, APPLICATION_ID,
				"Updating the packet details in the Registation table");

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Registration reg = registrationRepository.getOne(packet.getId());
		reg.setStatusCode(packet.getClientStatusCode());
		reg.setClientStatusCode(packet.getClientStatusCode());
		reg.setIsActive(true);
		reg.setUploadTimestamp(timestamp);
		reg.setRegistrationTransaction(buildRegistrationTransaction(reg));
		return registrationRepository.update(reg);
	}

	/**
	 * Builds the registration transaction.
	 *
	 * @param registrationPacket
	 *            the registration packet
	 * @return the list
	 */
	private List<RegistrationTransaction> buildRegistrationTransaction(Registration registrationPacket) {
		LOGGER.info("REGISTRATION - PACKET_ENCRYPTION - REGISTRATION_TRANSACTION_DAO", APPLICATION_NAME, APPLICATION_ID,
				"Packet encryption had been ended");

		Timestamp time = new Timestamp(System.currentTimeMillis());
		RegistrationTransaction regTransaction = new RegistrationTransaction();
		regTransaction.setId(String.valueOf(UUID.randomUUID().getMostSignificantBits()));
		regTransaction.setRegId(registrationPacket.getId());
		regTransaction.setTrnTypeCode(RegistrationTransactionType.UPDATED.getCode());
		regTransaction.setStatusCode(registrationPacket.getClientStatusCode());
		regTransaction.setLangCode("ENG");
		regTransaction.setCrBy("mosip");
		regTransaction.setCrDtime(time);
		regTransaction.setStatusComment(registrationPacket.getClientStatusComments());
		List<RegistrationTransaction> registrationTransaction = registrationPacket.getRegistrationTransaction();
		registrationTransaction.add(regTransaction);
		LOGGER.info("REGISTRATION - PACKET_ENCRYPTION - REGISTRATION_TRANSACTION_DAO", APPLICATION_NAME, APPLICATION_ID,
				"Packet encryption had been ended");

		return registrationTransaction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.dao.RegistrationDAO#getAllReRegistrationPackets(java.
	 * lang.String[])
	 */
	public List<Registration> getAllReRegistrationPackets(String[] status) {
		return registrationRepository.findByClientStatusCodeAndServerStatusCode(status[0], status[1]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.dao.RegistrationDAO#getRegistrationsToBeDeleted(java.
	 * sql.Timestamp, java.lang.String)
	 */
	@Override
	public List<Registration> getRegistrationsToBeDeleted(Timestamp crDtimes) {

		LOGGER.info("REGISTRATION - BY_STATUS - REGISTRATION_DAO", APPLICATION_NAME, APPLICATION_ID,
				"Retriving Registrations based on crDtime and status");

		return registrationRepository.findByCrDtimeBefore(crDtimes);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.dao.RegistrationDAO#getRegistrationById(java.lang.
	 * String)
	 */
	@Override
	public Registration getRegistrationById(String clientStatusCode, String rId) {
		LOGGER.debug("REGISTRATION - BY_STATUS - REGISTRATION_DAO", APPLICATION_NAME, APPLICATION_ID,
				"Get Registration based on reg Id and client status code started");
		return registrationRepository.findByClientStatusCodeAndId(clientStatusCode, rId);
	}

	@Override
	public List<Registration> get(List<String> regIds) {
		LOGGER.debug("REGISTRATION - BY_STATUS - REGISTRATION_DAO", APPLICATION_NAME, APPLICATION_ID,
				"Get Registrations based on reg Ids started");

		Iterable<String> iterableRegIds = regIds;
		
		LOGGER.debug("REGISTRATION - BY_STATUS - REGISTRATION_DAO", APPLICATION_NAME, APPLICATION_ID,
				"Get Registration based on reg Ids completed");

		return registrationRepository.findAllById(iterableRegIds);
	}
	
	/* (non-Javadoc)
	 * @see io.mosip.registration.dao.RegistrationDAO#get(java.sql.Timestamp, java.lang.String)
	 */
	@Override
	public List<Registration> get(Timestamp crDtimes, String clientStatus) {
		
		LOGGER.debug("REGISTRATION - BY_STATUS - REGISTRATION_DAO", APPLICATION_NAME, APPLICATION_ID,
				"Retriving Registrations based on crDtime and status");

		return registrationRepository.findByCrDtimeBeforeAndClientStatusCode(crDtimes, clientStatus);

	}
}
