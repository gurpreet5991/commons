package io.mosip.registration.jobs.impl;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.LoggerConstants;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.jobs.BaseJob;
import io.mosip.registration.service.UserSaltDetailsService;

/**
 * This is a job to sync the user salt.
 *
 * @author Sreekar Chukka
 * @since 1.0.0
 */
@Component(value = "userSaltSyncJob")
public class UserSaltSyncJob extends BaseJob {

	/**
	 * The userSaltDetailsService
	 */
	@Autowired
	private UserSaltDetailsService userSaltDetailsService;

	/**
	 * LOGGER for logging
	 */
	private static final Logger LOGGER = AppConfig.getLogger(UserSaltSyncJob.class);

	/**
	 * Execute internal.
	 *
	 * @param context the context
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.
	 * quartz.JobExecutionContext)
	 */
	@Async
	@Override
	public void executeInternal(JobExecutionContext context) {
		LOGGER.info(LoggerConstants.USER_SALT_SYNC_STATUS_JOB_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "job execute internal started");
		this.responseDTO = new ResponseDTO();

		try {
			this.jobId = loadContext(context);
			userSaltDetailsService = applicationContext.getBean(UserSaltDetailsService.class);

			// Run the Parent JOB always first
			this.responseDTO = userSaltDetailsService.getUserSaltDetails(triggerPoint);

			// To run the child jobs after the parent job Success
			if (responseDTO.getSuccessResponseDTO() != null) {
				executeChildJob(jobId, jobMap);
			}

			syncTransactionUpdate(responseDTO, triggerPoint, jobId);

		} catch (RegBaseUncheckedException baseUncheckedException) {
			LOGGER.error(LoggerConstants.USER_SALT_SYNC_STATUS_JOB_TITLE, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID,
					baseUncheckedException.getMessage() + ExceptionUtils.getStackTrace(baseUncheckedException));
			throw baseUncheckedException;
		}

		LOGGER.info(LoggerConstants.USER_SALT_SYNC_STATUS_JOB_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "job execute internal Ended");
	}

	@Override
	public ResponseDTO executeJob(String triggerPoint, String jobId) {

		LOGGER.info(LoggerConstants.USER_SALT_SYNC_STATUS_JOB_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "execute Job started");

		this.responseDTO = userSaltDetailsService.getUserSaltDetails(triggerPoint);
		syncTransactionUpdate(responseDTO, triggerPoint, jobId);

		LOGGER.info(LoggerConstants.USER_SALT_SYNC_STATUS_JOB_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "execute job ended");

		return responseDTO;
	}

}
