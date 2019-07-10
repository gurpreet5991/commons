package io.mosip.registration.jobs.impl;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.jobs.BaseJob;
import io.mosip.registration.service.config.GlobalParamService;

/**
 * This is a job to sync the config data.
 * 
 * <p>
 * This Job will be automatically triggered based on sync_frequency which has in
 * local DB.
 * </p>
 * 
 * <p>
 * If Sync_frequency = "0 0 11 * * ?" this job will be triggered everyday 11:00
 * AM, if it was missed on 11:00 AM, trigger on immediate application launch.
 * </p>
 * 
 * @author Sreekar Chukka
 *
 * @since 1.0.0
 */
@Component("synchConfigDataJob")
public class SynchConfigDataJob extends BaseJob {
	@Autowired
	private GlobalParamService globalParamService;

	/**
	 * LOGGER for logging
	 */
	private static final Logger LOGGER = AppConfig.getLogger(SynchConfigDataJob.class);

	@Override
	public ResponseDTO executeJob(String triggerPoint, String jobId) {
		LOGGER.info(RegistrationConstants.SYNCH_CONFIG_DATA_JOB_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "execute Job started");

		this.responseDTO = globalParamService
				.synchConfigData(RegistrationConstants.JOB_TRIGGER_POINT_SYSTEM.equalsIgnoreCase(triggerPoint));
		syncTransactionUpdate(responseDTO, triggerPoint, jobId);

		LOGGER.info(RegistrationConstants.SYNCH_CONFIG_DATA_JOB_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "execute job ended");

		return responseDTO;
	}

	@Override
	public void executeInternal(JobExecutionContext context) throws JobExecutionException {
		LOGGER.info(RegistrationConstants.SYNCH_CONFIG_DATA_JOB_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "job execute internal started");
		this.responseDTO = new ResponseDTO();

		try {

			this.jobId = loadContext(context);
			globalParamService = applicationContext.getBean(GlobalParamService.class);

			// Run the Parent JOB always first
			this.responseDTO = globalParamService.synchConfigData(true);

			// To run the child jobs after the parent job Success
			if (responseDTO.getSuccessResponseDTO() != null) {
				executeChildJob(jobId, jobMap);
			}

			syncTransactionUpdate(responseDTO, triggerPoint, jobId);

		} catch (RegBaseUncheckedException baseUncheckedException) {
			LOGGER.error(RegistrationConstants.SYNCH_CONFIG_DATA_JOB_TITLE, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, baseUncheckedException.getMessage());
			throw baseUncheckedException;
		}

		LOGGER.info(RegistrationConstants.SYNCH_CONFIG_DATA_JOB_TITLE, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "job execute internal Ended");

	}

}
