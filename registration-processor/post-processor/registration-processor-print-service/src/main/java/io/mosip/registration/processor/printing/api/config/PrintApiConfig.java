package io.mosip.registration.processor.printing.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import org.json.simple.parser.JSONParser;

@Configuration
@EnableSwagger2
public class PrintApiConfig {

	/**
	 * Registration status bean.
	 *
	 * @return the docket
	 */
	@Bean
	public Docket printingapiBean() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("Print PDF").select()
				.apis(RequestHandlerSelectors.basePackage("io.mosip.registration.processor.printing.api.controller"))
				.paths(PathSelectors.ant("/*")).build();
	}
	@Bean
	public JSONParser getJsonParser() {
		return new JSONParser();
	}

}
