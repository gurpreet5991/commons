/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjob.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * This class is a job scheduler of batch in which jobs are getting executed
 * based on cron expressions.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@RefreshScope
@Component
@EnableScheduling
public class PreregistrationBatchJobScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PreregistrationBatchJobScheduler.class);

	private static final String LOGDISPLAY = "{} - {} - {}";

	private static final String JOB_STATUS = "Job's status";

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job bookingJob;

	@Autowired
	private Job consumedStatusJob;

	@Autowired
	private Job expiredStatusJob;

	@Scheduled(cron = "${preregistration.job.schedule.cron.consumedStatusJob}")
	public void upadteStatusScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("updateStatusTime", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(consumedStatusJob, jobParam);

			LOGGER.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId(), jobExecution.getStatus());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			LOGGER.error(LOGDISPLAY, "UpdateTableJob failed to read Processed_pre_registration_list", e);
		}
	}

	@Scheduled(cron = "${preregistration.job.schedule.cron.slotavailability}")
	public void bookingJobScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("bookingJobTime", System.currentTimeMillis())
				.toJobParameters();
		try {

			JobExecution jobExecution = jobLauncher.run(bookingJob, jobParam);

			LOGGER.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId(), jobExecution.getStatus());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			LOGGER.error(LOGDISPLAY, "Booking Job failed to read data from master data service", e);
		}

	}

	@Scheduled(cron = "${preregistration.job.schedule.cron.expiredStatusJob}")
	public void expiredStatusScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("expiredStatusJobTime", System.currentTimeMillis())
				.toJobParameters();
		try {

			JobExecution jobExecution = jobLauncher.run(expiredStatusJob, jobParam);

			LOGGER.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId(), jobExecution.getStatus());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			LOGGER.error(LOGDISPLAY, "Expired Status Job failed to read data from service", e);
		}

	}

}
