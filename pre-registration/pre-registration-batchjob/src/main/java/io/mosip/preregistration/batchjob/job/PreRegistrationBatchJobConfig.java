package io.mosip.preregistration.batchjob.job;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mosip.preregistration.batchjob.tasklets.BookingTasklet;
import io.mosip.preregistration.batchjob.tasklets.ExpiredStatusTasklet;
import io.mosip.preregistration.batchjob.tasklets.UpdateConsumedStatusTasklet;

/**
 * @author M1043008
 *
 */
@Configuration
@EnableBatchProcessing
public class PreRegistrationBatchJobConfig {
	
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private UpdateConsumedStatusTasklet updateTableTasklet;
	
	/*@Autowired
	private ArchivingConsumedPreIdTasklet archivingTasklet;*/
	
	@Autowired
	private BookingTasklet bookingtasklet;
	
	@Autowired
	private ExpiredStatusTasklet expiredStatusTasklet;
	
	
	@Bean
	public Step updateTableStep() {
		return stepBuilderFactory.get("updateTableStep").tasklet(updateTableTasklet).build();
	}
	
	/*@Bean
	public Step archivingStep() {
		return stepBuilderFactory.get("archivingStep").tasklet(archivingTasklet).build();
	}*/
	
	@Bean
	public Step bookingStep() {
		return stepBuilderFactory.get("bookingStep").tasklet(bookingtasklet).build();
	}
	
	@Bean
	public Step expiredStatusStep() {
		return stepBuilderFactory.get("expiredStatusStep").tasklet(expiredStatusTasklet).build();
	}
	
	@Bean
	public Job bookingJob(){
		return this.jobBuilderFactory.get("bookingJob")
				   .incrementer(new RunIdIncrementer())
				   .start(bookingStep())
				   .build();
	}
	@Bean
	public Job consumedStatusJob() {
		return this.jobBuilderFactory.get("consumedStatusJob")
				   .incrementer(new RunIdIncrementer())
				   .start(updateTableStep())
				   .build();
	}
	
	@Bean
	public Job expiredStatusJob() {
		return this.jobBuilderFactory.get("expiredStatusJob")
				   .incrementer(new RunIdIncrementer())
				   .start(expiredStatusStep())
				   .build();
	}
}
