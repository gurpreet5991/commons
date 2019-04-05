package io.mosip.kernel.auth.adapter.config;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.auth.adapter.filter.AuthFilter;
import io.mosip.kernel.auth.adapter.filter.ClientInterceptor;
import io.mosip.kernel.auth.adapter.filter.CorsFilter;
import io.mosip.kernel.auth.adapter.handler.AuthHandler;
import io.mosip.kernel.auth.adapter.handler.AuthSuccessHandler;

/**
 * Holds the main configuration for authentication and authorization using
 * spring security.
 *
 * Inclusions: 1. AuthenticationManager bean configuration: a. This is assigned
 * an authProvider that we implemented. This option can include multiple auth
 * providers if necessary based on the requirement. b. RETURNS an instance of
 * the ProviderManager. 2. AuthFilter bean configuration: a. This extends
 * AbstractAuthenticationProcessingFilter. b. Instance of the AuthFilter is
 * created. c. This filter comes in line after the AuthHeadersFilter. d. Binds
 * the AuthenticationManager instance created with the filter. e. Binds the
 * AuthSuccessHandler created with the filter. f. RETURNS an instance of the
 * AuthFilter. 3. RestTemplate bean configuration: a. Binds the
 * ClientInterceptor instance with the RestTemplate instance created. b. RETURNS
 * an instance of the RestTemplate. 4. Secures endpoints using antMatchers and
 * adds filters in a sequence for execution.
 *
 * @author Sabbu Uday Kumar
 * @author Ramadurai Saravana Pandian
 * 
 * @since 1.0.0
 **/

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(2)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthHandler authProvider;

	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(Collections.singletonList(authProvider));
	}

	@Bean
    public AuthFilter authFilter() {
          RequestMatcher requestMatcher = new AntPathRequestMatcher("*");
          AuthFilter filter = new AuthFilter(requestMatcher);
           filter.setAuthenticationManager(authenticationManager());
          filter.setAuthenticationSuccessHandler(new AuthSuccessHandler());
        return filter;
    }
	
	@Bean
	public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();
		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		restTemplate.setInterceptors(Collections.singletonList(new ClientInterceptor()));
		return restTemplate;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers("*").authenticated().and().exceptionHandling()
				.authenticationEntryPoint(new AuthEntryPoint()).and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		http.addFilterBefore(authFilter(), UsernamePasswordAuthenticationFilter.class);
		http.addFilterBefore(new CorsFilter(), AuthFilter.class);
		http.headers().cacheControl();
	}
}

class AuthEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			AuthenticationException e) throws IOException {
		httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED");
	}
}