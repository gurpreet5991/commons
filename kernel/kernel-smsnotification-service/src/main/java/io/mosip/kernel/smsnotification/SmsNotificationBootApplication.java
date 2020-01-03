package io.mosip.kernel.smsnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * -------------------------------------------------------------------------
 * SMS-NOTIFICATION-SERVICE APPLICATION
 * -------------------------------------------------------------------------
 * This service serves the functionality of sending SMS.
 * -------------------------------------------------------------------------
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.smsnotification.*", "io.mosip.kernel.auth.*" })
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class SmsNotificationBootApplication {

	/**
	 * Main method to run spring boot application
	 * 
	 * @param args params
	 */
	public static void main(String[] args) {
		SpringApplication.run(SmsNotificationBootApplication.class, args);
	}
}
