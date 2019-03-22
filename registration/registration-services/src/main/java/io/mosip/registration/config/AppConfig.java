package io.mosip.registration.config;

import java.util.Properties;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.quartz.JobListener;
import org.quartz.TriggerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.auditmanager.config.AuditConfig;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.kernel.dataaccess.hibernate.repository.impl.HibernateRepositoryImpl;
import io.mosip.kernel.logger.logback.appender.RollingFileAppender;
import io.mosip.kernel.logger.logback.factory.Logfactory;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;
import io.mosip.registration.dao.SyncJobConfigDAO;
import io.mosip.registration.jobs.JobProcessListener;
import io.mosip.registration.jobs.JobTriggerListener;

/**
 * Spring Configuration class for Registration-Service Module
 * 
 * @author Balaji Sridharan
 * @since 1.0.0
 *
 */
@Configuration
@EnableAspectJAutoProxy
@Import({ DaoConfig.class, AuditConfig.class, PropertiesConfig.class })
@EnableJpaRepositories(basePackages = "io.mosip.registration", repositoryBaseClass = HibernateRepositoryImpl.class)
@ComponentScan({ "io.mosip.registration", "io.mosip.kernel.core", "io.mosip.kernel.keygenerator",
		"io.mosip.kernel.idvalidator", "io.mosip.kernel.ridgenerator", "io.mosip.kernel.qrcode",
		"io.mosip.kernel.crypto", "io.mosip.kernel.jsonvalidator", "io.mosip.kernel.idgenerator",
		"io.mosip.kernel.virusscanner", "io.mosip.kernel.transliteration", "io.mosip.kernel.applicanttype", "io.mosip.kernel.cbeffutil" })
@PropertySource(value= {"classpath:spring.properties", "classpath:spring-${spring.profiles.active}.properties"})
public class AppConfig {

	private static final RollingFileAppender MOSIP_ROLLING_APPENDER = new RollingFileAppender();

	private static final ResourceBundle applicationProperties = ResourceBundle.getBundle("reg_application");

	@Autowired
	@Qualifier("dataSource")
	private DataSource datasource;

	/**
	 * Job processor
	 */
	@Autowired
	private JobProcessListener jobProcessListener;

	/**
	 * Job Trigger
	 */
	@Autowired
	private JobTriggerListener commonTriggerListener;

	@Autowired
	private SyncJobConfigDAO syncJobConfigDAO;

	static {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("log4j");
		MOSIP_ROLLING_APPENDER.setAppend(true);
		MOSIP_ROLLING_APPENDER.setAppenderName(resourceBundle.getString("log4j.appender.Appender"));
		MOSIP_ROLLING_APPENDER.setFileName(resourceBundle.getString("log4j.appender.Appender.file"));
		MOSIP_ROLLING_APPENDER.setFileNamePattern(resourceBundle.getString("log4j.appender.Appender.filePattern"));
		MOSIP_ROLLING_APPENDER.setMaxFileSize(resourceBundle.getString("log4j.appender.Appender.maxFileSize"));
		MOSIP_ROLLING_APPENDER.setTotalCap(resourceBundle.getString("log4j.appender.Appender.totalCap"));
		MOSIP_ROLLING_APPENDER
				.setMaxHistory(Integer.valueOf(resourceBundle.getString("log4j.appender.Appender.maxBackupIndex")));
		MOSIP_ROLLING_APPENDER.setImmediateFlush(true);
		MOSIP_ROLLING_APPENDER.setPrudent(true);
	}

	public static Logger getLogger(Class<?> className) {
		return Logfactory.getDefaultRollingFileLogger(MOSIP_ROLLING_APPENDER, className);
	}

	public static String getApplicationProperty(String property) {
		return applicationProperties.getString(property);
	}

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	public TemplateManagerBuilder getTemplateManagerBuilder() {
		return new TemplateManagerBuilderImpl();
	}

	
}
