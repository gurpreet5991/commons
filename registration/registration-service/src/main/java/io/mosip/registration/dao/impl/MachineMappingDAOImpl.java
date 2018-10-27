package io.mosip.registration.dao.impl;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;
import static io.mosip.registration.constants.RegistrationConstants.MACHINE_MAPPING_LOGGER_TITLE;
import static io.mosip.registration.constants.RegistrationExceptions.REG_USER_MACHINE_MAP_CENTER_MACHINE_CODE;
import static io.mosip.registration.constants.RegistrationExceptions.REG_USER_MACHINE_MAP_CENTER_USER_MACHINE_CODE;
import static io.mosip.registration.constants.RegistrationExceptions.REG_USER_MACHINE_MAP_MACHINE_MASTER_CODE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.spi.logger.MosipLogger;
import io.mosip.kernel.logger.logback.appender.MosipRollingFileAppender;
import io.mosip.kernel.logger.logback.factory.MosipLogfactory;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dao.MachineMappingDAO;
import io.mosip.registration.entity.CenterMachine;
import io.mosip.registration.entity.MachineMaster;
import io.mosip.registration.entity.RegistrationUserDetail;
import io.mosip.registration.entity.UserMachineMapping;
import io.mosip.registration.entity.UserMachineMappingID;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.repositories.CenterMachineRepository;
import io.mosip.registration.repositories.MachineMasterRepository;
import io.mosip.registration.repositories.RegistrationUserDetailRepository;
import io.mosip.registration.repositories.UserMachineMappingRepository;

/**
 * The implementation class of {@link MachineMappingDAO}.
 * 
 * @author Yaswanth S
 * @since 1.0.0
 *
 */
@Repository
public class MachineMappingDAOImpl implements MachineMappingDAO {

	/**
	 * logger for logging
	 */
	private static MosipLogger LOGGER;

	/**
	 * intializing logger
	 * 
	 * @param mosipRollingFileAppender appender
	 */
	@Autowired
	private void initializeLogger(MosipRollingFileAppender mosipRollingFileAppender) {
		LOGGER = MosipLogfactory.getMosipDefaultRollingFileLogger(mosipRollingFileAppender, this.getClass());
	}

	/**
	 * centerMachineRepository instance creation using autowired annotation
	 */
	@Autowired
	private CenterMachineRepository centerMachineRepository;

	/**
	 * machineMasterRepository instance creation using autowired annotation
	 */
	@Autowired
	private MachineMasterRepository machineMasterRepository;

	/**
	 * machineMappingRepository instance creation using autowired annotation
	 */
	@Autowired
	private UserMachineMappingRepository machineMappingRepository;

	/**
	 * userDetailRepository instance creation using autowired annotation
	 */
	@Autowired
	private RegistrationUserDetailRepository userDetailRepository;

	/*
	 * (non-Javadoc) Getting station id based on mac address
	 * 
	 * @see
	 * io.mosip.registration.dao.MachineMappingDAO#getStationID(java.lang.String)
	 */
	@Override
	public String getStationID(String macAddress) throws RegBaseCheckedException {

		try {
			LOGGER.debug(MACHINE_MAPPING_LOGGER_TITLE, APPLICATION_NAME,
					APPLICATION_ID, "getStationID() macAddress --> " + macAddress);
			MachineMaster machineMaster = machineMasterRepository.findByMacAddress(macAddress);
			return machineMaster.getId();
		} catch (NullPointerException nullPointerException) {
			throw new RegBaseCheckedException(REG_USER_MACHINE_MAP_MACHINE_MASTER_CODE.getErrorCode(),
					REG_USER_MACHINE_MAP_MACHINE_MASTER_CODE.getErrorMessage());
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.MACHINE_MAPPING_STATIONID_RUN_TIME_EXCEPTION,
					runtimeException.getMessage());
		}
	}

	/*
	 * (non-Javadoc) Getting center id based on station id
	 * 
	 * @see
	 * io.mosip.registration.dao.MachineMappingDAO#getCenterID(java.lang.String)
	 */
	@Override
	public String getCenterID(String stationID) throws RegBaseCheckedException {
		try {
			LOGGER.debug(MACHINE_MAPPING_LOGGER_TITLE, APPLICATION_NAME,
					APPLICATION_ID, "getCenterID() stationID --> " + stationID);
			CenterMachine centerMachine = centerMachineRepository.findByCenterMachineIdId(stationID);
			return centerMachine.getCenterMachineId().getCentreId();
		} catch (NullPointerException nullPointerException) {
			throw new RegBaseCheckedException(REG_USER_MACHINE_MAP_CENTER_MACHINE_CODE.getErrorCode(),
					REG_USER_MACHINE_MAP_CENTER_MACHINE_CODE.getErrorMessage());
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.MACHINE_MAPPING_CENTERID_RUN_TIME_EXCEPTION,
					runtimeException.getMessage());
		}
	}

	/*
	 * (non-Javadoc) get list of users belongs to the center id
	 * 
	 * @see io.mosip.registration.dao.MachineMappingDAO#getUsers(java.lang.String)
	 */
	@Override
	public List<RegistrationUserDetail> getUsers(String ceneterID) throws RegBaseCheckedException {

		try {
			LOGGER.debug(MACHINE_MAPPING_LOGGER_TITLE, APPLICATION_NAME,
					APPLICATION_ID, "getUsers() ceneterID -> " + ceneterID);
			return userDetailRepository.findByCntrIdAndIsActiveTrueAndUserStatusNotLikeAndIdNotLike(ceneterID,
					RegistrationConstants.BLACKLISTED, SessionContext.getInstance().getUserContext().getUserId());
		} catch (NullPointerException nullPointerException) {
			throw new RegBaseCheckedException(REG_USER_MACHINE_MAP_CENTER_USER_MACHINE_CODE.getErrorCode(),
					REG_USER_MACHINE_MAP_CENTER_USER_MACHINE_CODE.getErrorMessage());
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.MACHINE_MAPPING_USERLIST_RUN_TIME_EXCEPTION,
					runtimeException.getMessage());
		}

	}

	/*
	 * (non-Javadoc) Save user to UserMachineMapping
	 * 
	 * @see
	 * io.mosip.registration.dao.MachineMappingDAO#save(io.mosip.registration.entity
	 * .UserMachineMapping)
	 */
	@Override
	public String save(UserMachineMapping user) {
		LOGGER.debug("REGISTRATION - USER CLIENT MACHINE MAPPING", APPLICATION_NAME,
				APPLICATION_ID, "DAO save method called");

		try {
			// create new mapping
			machineMappingRepository.save(user);
			LOGGER.debug("REGISTRATION - USER CLIENT MACHINE MAPPING", APPLICATION_NAME,
					APPLICATION_ID, "DAO save method ended");

			return RegistrationConstants.MACHINE_MAPPING_CREATED;
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.MACHINE_MAPPING_RUN_TIME_EXCEPTION,
					runtimeException.getMessage());
		}
	}

	/*
	 * (non-Javadoc) update user to UserMachineMapping
	 * 
	 * @see
	 * io.mosip.registration.dao.MachineMappingDAO#update(io.mosip.registration.
	 * entity.UserMachineMapping)
	 */
	@Override
	public String update(UserMachineMapping user) {
		LOGGER.debug("REGISTRATION - USER CLIENT MACHINE MAPPING", APPLICATION_NAME,
				APPLICATION_ID, "DAO update method called");

		try {
			// update user details in user mapping
			machineMappingRepository.update(user);
			LOGGER.debug("REGISTRATION - USER CLIENT MACHINE MAPPING", APPLICATION_NAME,
					APPLICATION_ID, "DAO update method ended");

			return RegistrationConstants.MACHINE_MAPPING_UPDATED;
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.MACHINE_MAPPING_RUN_TIME_EXCEPTION,
					runtimeException.getMessage());
		}
	}

	/*
	 * (non-Javadoc) find user from UserMachineMappingID
	 * 
	 * @see
	 * io.mosip.registration.dao.MachineMappingDAO#findByID(io.mosip.registration.
	 * entity.UserMachineMapping)
	 */
	@Override
	public UserMachineMapping findByID(UserMachineMappingID userID) {
		LOGGER.debug("REGISTRATION - USER CLIENT MACHINE MAPPING", APPLICATION_NAME,
				APPLICATION_ID, "DAO findByID method called");

		UserMachineMapping machineMapping = null;
		try {
			// find the user
			machineMapping = machineMappingRepository.findById(UserMachineMapping.class, userID);
		} catch (RuntimeException runtimeException) {
			throw new RegBaseUncheckedException(RegistrationConstants.MACHINE_MAPPING_RUN_TIME_EXCEPTION,
					runtimeException.getMessage());
		}
		LOGGER.debug("REGISTRATION - USER CLIENT MACHINE MAPPING", APPLICATION_NAME,
				APPLICATION_ID, "DAO findByID method ended");

		return machineMapping;
	}

}