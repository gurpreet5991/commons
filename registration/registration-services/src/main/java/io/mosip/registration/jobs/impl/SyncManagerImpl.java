package io.mosip.registration.jobs.impl;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.LoggerConstants;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dao.SyncJobControlDAO;
import io.mosip.registration.dao.SyncTransactionDAO;
import io.mosip.registration.entity.SyncControl;
import io.mosip.registration.entity.SyncTransaction;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.jobs.SyncManager;
import io.mosip.registration.service.BaseService;
import io.mosip.registration.util.healthcheck.RegistrationSystemPropertiesChecker;

/**
 * This class includes the functionalities of what transaction table needed.,
 * like getting job details and preparation of sync transaction data
 * 
 * @author YASWANTH S
 * @since 1.0.0
 *
 */
@Component
public class SyncManagerImpl extends BaseService implements SyncManager {

	@Autowired
	private SyncTransactionDAO jobTransactionDAO;

	@Autowired
	private SyncJobControlDAO syncJobDAO;


	/**
	 * LOGGER for logging
	 */
	private static final Logger LOGGER = AppConfig.getLogger(SyncManagerImpl.class);

	/* (non-Javadoc)
	 * @see io.mosip.registration.jobs.SyncManager#createSyncControlTransaction(io.mosip.registration.entity.SyncTransaction)
	 */
	@Override
	public synchronized SyncControl createSyncControlTransaction(final SyncTransaction syncTransaction) {

		SyncControl syncControl = syncJobDAO.findBySyncJobId(syncTransaction.getSyncJobId());

		boolean isNotCreated = syncControl == null;
		if (syncControl == null) {
			syncControl = new SyncControl();
			syncControl.setId(UUID.randomUUID().toString());
			syncControl.setSyncJobId(syncTransaction.getSyncJobId());
			syncControl.setIsActive(true);
			syncControl.setMachineId(getMacAddress());

			syncControl.setRegcntrId(syncTransaction.getCntrId());
			syncControl.setLangCode(AppConfig.getApplicationProperty(RegistrationConstants.APPLICATION_LANUAGE));

			syncControl.setCrBy(syncTransaction.getCrBy());
			syncControl.setCrDtime(new Timestamp(System.currentTimeMillis()));

		} else {
			syncControl.setUpdBy(syncTransaction.getCrBy());
			syncControl.setUpdDtimes(new Timestamp(System.currentTimeMillis()));

		}
		syncControl.setSynctrnId(syncTransaction.getId());
		syncControl.setLastSyncDtimes(new Timestamp(System.currentTimeMillis()));

		if (isNotCreated) {
			syncControl = syncJobDAO.save(syncControl);
		} else {
			syncControl = syncJobDAO.update(syncControl);
		}
		return syncControl;

	}

	/* (non-Javadoc)
	 * @see io.mosip.registration.jobs.SyncManager#createSyncTransaction(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized SyncTransaction createSyncTransaction(final String status, final String statusComment,
			final String triggerPoint, final String syncJobId) {
		LOGGER.info(LoggerConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Create Sync Transaction started");

		SyncTransaction syncTransaction = new SyncTransaction();

		try {

			syncTransaction.setId(UUID.randomUUID().toString());

			syncTransaction.setSyncJobId(syncJobId);

			syncTransaction.setSyncDateTime(new Timestamp(System.currentTimeMillis()));
			syncTransaction.setStatusCode(status);
			syncTransaction.setStatusComment(statusComment);

			syncTransaction.setTriggerPoint(triggerPoint);

			syncTransaction.setSyncFrom(RegistrationSystemPropertiesChecker.getMachineId());

			syncTransaction.setSyncTo(RegistrationConstants.JOB_SYNC_TO_SERVER);

			syncTransaction.setMachmId(getStationId(getMacAddress()));

			syncTransaction.setCntrId(getCenterId());

			syncTransaction.setLangCode(AppConfig.getApplicationProperty(RegistrationConstants.APPLICATION_LANUAGE));

			syncTransaction.setCrBy(RegistrationConstants.JOB_TRIGGER_POINT_SYSTEM.equals(triggerPoint) ? triggerPoint
					: getUserIdFromSession());

			syncTransaction.setCrDtime(new Timestamp(System.currentTimeMillis()));

			syncTransaction = jobTransactionDAO.save(syncTransaction);

		} catch (RuntimeException runtimeException) {
			LOGGER.error(LoggerConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, runtimeException.getMessage());

			throw new RegBaseUncheckedException(RegistrationConstants.SYNC_TRANSACTION_RUNTIME_EXCEPTION,
					runtimeException.getMessage());
		}

		LOGGER.info(LoggerConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Create Sync Transaction Ended");

		return syncTransaction;
	}

}
