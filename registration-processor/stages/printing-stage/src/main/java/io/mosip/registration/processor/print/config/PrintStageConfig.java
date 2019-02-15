package io.mosip.registration.processor.print.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePropertySource;

import io.mosip.registration.processor.core.spi.uincardgenerator.UinCardGenerator;
import io.mosip.registration.processor.print.exception.PrintGlobalExceptionHandler;
import io.mosip.registration.processor.print.stage.PrintStage;
import io.mosip.registration.processor.print.util.UinCardGeneratorImpl;

/**
 * @author M1048399
 *
 */
@Configuration
@EnableAspectJAutoProxy
@PropertySource("classpath:bootstrap.properties")
public class PrintStageConfig {

	/**
	 * Loads config server values
	 * 
	 * @param env
	 * @return
	 * @throws IOException
	 */
	@Bean
	public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer(Environment env) throws IOException {

		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		List<String> applicationNames = getAppNames(env);
		Resource[] appResources = new Resource[applicationNames.size()];

		for (int i = 0; i < applicationNames.size(); i++) {
			String loc = env.getProperty("spring.cloud.config.uri") + "/registration-processor/"
					+ env.getProperty("spring.profiles.active") + "/" + env.getProperty("spring.cloud.config.label")
					+ "/" + applicationNames.get(i) + "-" + env.getProperty("spring.profiles.active") + ".properties";
			
			appResources[i] = resolver.getResources(loc)[0];
			((AbstractEnvironment) env).getPropertySources()
            .addLast(new ResourcePropertySource(applicationNames.get(i), loc));
		}
		pspc.setLocations(appResources);
		return pspc;
	}

	/**
	 * Gets list of application name mentioned in bootstrap.properties
	 * 
	 * @param env
	 * @return
	 */
	public List<String> getAppNames(Environment env) {
		String names = env.getProperty("spring.application.name");
		return Stream.of(names.split(",")).collect(Collectors.toList());
	}

	
	
	@Bean 
	public PrintStage getPrintStage() {
		return new PrintStage();
	}
	
	@Bean
	public UinCardGenerator<ByteArrayOutputStream> getUinCardGeneratorImpl() {
		return new UinCardGeneratorImpl();
	}
	
	/**
	 * GlobalExceptionHandler bean
	 * 
	 * @return
	 */
	@Bean
	public PrintGlobalExceptionHandler getPrintGlobalExceptionHandler() {
		return new PrintGlobalExceptionHandler();
	}

}
