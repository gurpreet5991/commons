package io.mosip.registration.processor.manual.verification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
	
/**
 * The Swagger config class for Manual Adjudication API.
 *
 * @author Pranav Kumar
 * @since 0.0.1
 */
@Configuration
@EnableSwagger2
public class ManualVerificationConfig {
	
	/**
	 * Registration status bean.
	 *
	 * @return Docket
	 */
	@Bean
	public Docket registrationStatusBean() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("Manual Adjudication").select()
				.apis(RequestHandlerSelectors.basePackage("io.mosip.registration.processor.manual.verification"))
				.paths(PathSelectors.ant("/v0.1/registration-processor/manual-verification/*")).build();
	}

}
