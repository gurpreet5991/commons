package io.mosip.registration.manager.impl;

import java.sql.Timestamp;
import java.util.Random;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dao.SyncJobTransactionDAO;
import io.mosip.registration.dao.SyncJobDAO;
import io.mosip.registration.entity.SyncControl;
import io.mosip.registration.entity.SyncJob;
import io.mosip.registration.entity.SyncTransaction;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.manager.BaseTransactionManager;
import io.mosip.registration.service.impl.JobConfigurationServiceImpl;
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
public class SyncTransactionManagerImpl implements BaseTransactionManager {

	@Autowired
	SyncJobTransactionDAO jobTransactionDAO;

	@Autowired
	SyncJobDAO syncJobDAO;

	/**
	 * LOGGER for logging
	 */
	private static final Logger LOGGER = AppConfig.getLogger(SyncTransactionManagerImpl.class);

	// Need to be removed if the transaction table's primary key ID is Auto -
	// Generatable
	Random random = new Random();

	@Override
	public SyncJob getJob(JobExecutionContext context) {
		LOGGER.debug(RegistrationConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Get Job started");

		SyncJob syncJob = null;

		syncJob = getJob(context.getJobDetail());

		LOGGER.debug(RegistrationConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Get Job Ended");

		return syncJob;
	}

	@Override
	public SyncJob getJob(String jobId) {
		LOGGER.debug(RegistrationConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Get Job started");

		SyncJob syncJob = JobConfigurationServiceImpl.SYNC_JOB_MAP.get(jobId);

		LOGGER.debug(RegistrationConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Get Job Ended");

		return syncJob;
	}

	@Override
	public SyncJob getJob(final Trigger trigger) {
		LOGGER.debug(RegistrationConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Get Job started");

		SyncJob syncJob = null;

		syncJob = getJob((JobDetail) trigger.getJobDataMap().get("jobDetail"));

		LOGGER.debug(RegistrationConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Get Job Ended");

		return syncJob;
	}

	@Override
	public SyncJob getJob(final JobDetail jobDetail) {

		LOGGER.debug(RegistrationConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Get Job started");

		String jobId = jobDetail.getKey().getName();
		SyncJob syncJob = JobConfigurationServiceImpl.SYNC_JOB_MAP.get(jobId);

		LOGGER.debug(RegistrationConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Get Job Ended");

		return syncJob;
	}

	@Override
	public SyncTransaction createSyncTransaction(final String status, final String statusComment,
			final String triggerPoint, final SyncJob syncJob) {
		LOGGER.debug(RegistrationConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Create Sync Transaction started");

		SyncTransaction syncTransaction = new SyncTransaction();

	
		try {

			// TODO to be auto generated and has to be remove from here
			String transactionId = Integer.toString(random.nextInt(10000));
			syncTransaction.setId(transactionId);

			syncTransaction.setSyncJobId(syncJob.getId());

			syncTransaction.setSyncDateTime(new Timestamp(System.currentTimeMillis()));
			syncTransaction.setStatusCode(status);
			syncTransaction.setStatusComment(statusComment);

			// TODO
			syncTransaction.setTriggerPoint(triggerPoint);

			syncTransaction.setSyncFrom(RegistrationSystemPropertiesChecker.getMachineId());

			// TODO
			syncTransaction.setSyncTo("SERVER???");

			syncTransaction.setMachmId(RegistrationSystemPropertiesChecker.getMachineId());
			// syncTransaction.setCntrId(SessionContext.getInstance().getUserContext().getRegistrationCenterDetailDTO()
			// .getRegistrationCenterId());

			// TODO
			/*
			 * syncTransaction.setRefId("REFID"); syncTransaction.setRefType("REFTYPE");
			 * syncTransaction.setSyncParam("SyncParam");
			 */

			// TODO
			syncTransaction.setLangCode("EN");

			syncTransaction.setActive(true);

			syncTransaction.setCrBy(SessionContext.getInstance().getUserContext().getUserId());

			syncTransaction.setCrDtime(new Timestamp(System.currentTimeMillis()));

			// TODO
			// update by and timez info

			// TODO
			// ISDeleted and Timez info

			syncTransaction = jobTransactionDAO.save(syncTransaction);

			createSyncControlTransaction(syncTransaction);
		} catch (NullPointerException nullPointerException) {
			throw new RegBaseUncheckedException(RegistrationConstants.SYNC_TRANSACTION_NULL_POINTER_EXCEPTION,
					nullPointerException.getMessage());

		}

		LOGGER.debug(RegistrationConstants.BATCH_JOBS_SYNC_TRANSC_LOGGER_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Create Sync Transaction Ended");

		return syncTransaction;
	}

	

	@Override
	public SyncControl createSyncControlTransaction(final SyncTransaction syncTransaction) throws NullPointerException{

		SyncControl syncControl = syncJobDAO.findBySyncJobId(syncTransaction.getSyncJobId());

		boolean isNotCreated = syncControl == null;
		if (syncControl == null) {
			syncControl = new SyncControl();
			syncControl.setId(Integer.toString(random.nextInt(10000)));
			syncControl.setSyncJobId(syncTransaction.getSyncJobId());
			syncControl.setIsActive(true);
			syncControl.setMachineId(RegistrationSystemPropertiesChecker.getMachineId());
			/*
			 * // syncControl.setCntrId(SessionContext.getInstance().getUserContext().
			 * getRegistrationCenterDetailDTO() // .getRegistrationCenterId());
			 */
			syncControl.setLangCode("EN");

			syncControl.setCrBy(SessionContext.getInstance().getUserContext().getUserId());
			syncControl.setCrDtime(new Timestamp(System.currentTimeMillis()));

		} else {
			syncControl.setUpdBy(SessionContext.getInstance().getUserContext().getUserId());
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

}