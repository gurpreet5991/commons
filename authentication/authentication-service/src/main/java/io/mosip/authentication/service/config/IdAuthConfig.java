package io.mosip.authentication.service.config;

import java.util.Map;

import org.hibernate.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;

/**
 * Class for defining configurations for the service.
 * 
 * @author Manoj SP
 *
 */
@Configuration
public class IdAuthConfig extends HibernateDaoConfig {
	
	/** The interceptor. */
	@Autowired
	private Interceptor interceptor;

	/* (non-Javadoc)
	 * @see io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig#jpaProperties()
	 */
	@Override
	public Map<String, Object> jpaProperties() {
		Map<String, Object> jpaProperties = super.jpaProperties();
		jpaProperties.put("hibernate.ejb.interceptor", interceptor);
		return jpaProperties;
	}
	
	
}
