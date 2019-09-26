package io.mosip.kernel.keymanagerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Key Manager Application
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.keymanagerservice.*", "io.mosip.kernel.auth.*" })
public class KeymanagerBootApplication {

	/**
	 * Main method to run spring boot application
	 * 
	 * @param args args
	 */
	public static void main(String[] args) {

		SpringApplication.run(KeymanagerBootApplication.class, args);
	}
}
