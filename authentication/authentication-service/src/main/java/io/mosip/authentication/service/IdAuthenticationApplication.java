package io.mosip.authentication.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.kernel.idvalidator.uin.impl.UinValidatorImpl;
import io.mosip.kernel.idvalidator.vid.impl.VidValidatorImpl;

/**
 * Spring-boot class for ID Authentication Application.
 *
 * @author Dinesh Karuppiah
 */
@SpringBootApplication
@Import(value = { HibernateDaoConfig.class, UinValidatorImpl.class, VidValidatorImpl.class })
public class IdAuthenticationApplication {

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(IdAuthenticationApplication.class, args);
	}
}
