package io.mosip.idrepository.vid.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * The Class Swagger Config - configuration class for swagger.
 *
 * @author Manoj SP
 * @author Prem Kumar
 */
@Configuration(value = "idrepo_vid_swagger_config")
@EnableSwagger2
public class SwaggerConfig {

	@Value("${application.env.local:false}")
	private Boolean localEnv;

	@Value("${swagger.base-url:#{null}}")
	private String swaggerUrl;

	@Value("${server.port:8080}")
	private int serverPort;

	String proto = "http";
	String host = "localhost";
	int port = -1;
	String hostWithPort = "localhost:8080";

	/**
	 * Gets the api info.
	 *
	 * @return the api info
	 */
	ApiInfo getApiInfo() {
		return new ApiInfoBuilder().title("Id Repo Vid Service").description("Id Repo Vid Service").build();
	}

	@Bean
	public Docket api() throws MalformedURLException {
		boolean targetSwagger = false;
		if (!localEnv && swaggerUrl != null && !swaggerUrl.isEmpty()) {
			try {
				proto = new URL(swaggerUrl).getProtocol();
				host = new URL(swaggerUrl).getHost();
				port = new URL(swaggerUrl).getPort();
				if (port == -1) {
					hostWithPort = host;
				} else {
					hostWithPort = host + ":" + port;
				}
				targetSwagger = true;
			} catch (MalformedURLException e) {
				throw e;
			}
		}
		Docket docket = new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("io.mosip.idrepository.vid.controller"))
				.paths(PathSelectors.regex("(?!/(error|actuator).*).*")).build().apiInfo(getApiInfo());

		if (targetSwagger) {
			docket.protocols(protocols()).host(hostWithPort);
		}

		return docket;
	}

	private Set<String> protocols() {
		Set<String> protocols = new HashSet<>();
		protocols.add(proto);
		return protocols;
	}
}
