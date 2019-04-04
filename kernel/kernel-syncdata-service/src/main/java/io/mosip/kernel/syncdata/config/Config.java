package io.mosip.kernel.syncdata.config;

import javax.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import io.mosip.kernel.syncdata.filter.ReqResFilter;

/**
 * Config class with beans for modelmapper and request logging
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Configuration
public class Config {

	/**
	 * Produce Request Logging bean
	 * 
	 * @return Request logging bean
	 */
	@Bean
	public CommonsRequestLoggingFilter logFilter() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(10000);
		filter.setIncludeHeaders(false);
		filter.setAfterMessagePrefix("REQUEST DATA : ");
		return filter;
	}

	@Bean
	public FilterRegistrationBean<Filter> registerReqResFilter() {
		FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
		filterRegistrationBean.setFilter(getReqResFilter());
		filterRegistrationBean.setOrder(1);
		return filterRegistrationBean;
	}

	@Bean
	public Filter getReqResFilter() {
		return new ReqResFilter();
	}

	/*
	 * @Bean public RestTemplate restTemplateConfig() throws KeyManagementException,
	 * NoSuchAlgorithmException, KeyStoreException {
	 * 
	 * TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String
	 * authType) -> true;
	 * 
	 * SSLContext sslContext =
	 * org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null,
	 * acceptingTrustStrategy) .build();
	 * 
	 * SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
	 * 
	 * CloseableHttpClient httpClient =
	 * HttpClients.custom().setSSLSocketFactory(csf).build();
	 * 
	 * HttpComponentsClientHttpRequestFactory requestFactory = new
	 * HttpComponentsClientHttpRequestFactory();
	 * 
	 * requestFactory.setHttpClient(httpClient); return new
	 * RestTemplate(requestFactory);
	 * 
	 * }
	 */

}
