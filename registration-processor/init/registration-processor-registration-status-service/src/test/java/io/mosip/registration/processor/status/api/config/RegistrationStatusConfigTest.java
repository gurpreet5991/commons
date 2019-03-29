package io.mosip.registration.processor.status.api.config;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.fsadapter.hdfs.util.ConnectionUtils;
import io.mosip.registration.processor.rest.client.config.RestConfigBean;
import io.mosip.registration.processor.status.config.RegistrationStatusBeanConfig;

@Configuration
@ComponentScan(basePackages = { "io.mosip.registration.processor.status.*",
		"io.mosip.registration.processor.rest.client.*" }, excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, value = {
				RegistrationStatusBeanConfig.class, RestConfigBean.class }))
public class RegistrationStatusConfigTest {

	@MockBean
	public FileSystemAdapter filesystemAdapter;

	@MockBean
	public ConnectionUtils connectionUtil;

}
