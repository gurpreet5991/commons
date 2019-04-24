package io.mosip.registration.processor.rest.client.utils;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.rest.client.audit.dto.Metadata;
import io.mosip.registration.processor.rest.client.audit.dto.PasswordRequest;
import io.mosip.registration.processor.rest.client.audit.dto.SecretKeyRequest;
import io.mosip.registration.processor.rest.client.audit.dto.TokenRequestDTO;
import io.mosip.registration.processor.rest.client.exception.TokenGenerationFailedException;

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.google.gson.Gson;

import io.mosip.kernel.core.util.DateUtils;

/**
 * The Class RestApiClient.
 * 
 * @author Rishabh Keshari
 */
@Component
public class RestApiClient {

	/** The logger. */
	private final Logger logger = RegProcessorLogger.getLogger(RestApiClient.class);

	/** The builder. */
	@Autowired
	RestTemplateBuilder builder;

	@Autowired
	Environment environment;

	/**
	 * Gets the api.
	 *
	 * @param <T>          the generic type
	 * @param getURI       the get URI
	 * @param responseType the response type
	 * @return the api
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> T getApi(URI uri, Class<?> responseType) throws Exception {
		RestTemplate restTemplate;
		T result = null;
		try {
			restTemplate = getRestTemplate();
//			 result = (T) restTemplate.getForObject(uri, responseType);
			result = (T) restTemplate.exchange(uri, HttpMethod.GET, setRequestHeader(null, null), responseType)
					.getBody();
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw e;
		}
		return result;
	}

	/**
	 * Post api.
	 *
	 * @param <T>           the generic type
	 * @param uri           the uri
	 * @param requestType   the request type
	 * @param responseClass the response class
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public <T> T postApi(String uri, MediaType mediaType, Object requestType, Class<?> responseClass) throws Exception {

		RestTemplate restTemplate;
		T result = null;
		try {
			restTemplate = getRestTemplate();
			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), uri);
			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), requestType.toString());
			result = (T) restTemplate.postForObject(uri, setRequestHeader(requestType, mediaType), responseClass);

		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), e.getMessage() + ExceptionUtils.getStackTrace(e));

			throw e;
		}
		return result;
	}

	/**
	 * Patch api.
	 *
	 * @param <T>           the generic type
	 * @param uri           the uri
	 * @param requestType   the request type
	 * @param responseClass the response class
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public <T> T patchApi(String uri, MediaType mediaType, Object requestType, Class<?> responseClass)
			throws Exception {

		RestTemplate restTemplate;
		T result = null;
		try {
			restTemplate = getRestTemplate();
			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), uri);
			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), requestType.toString());
			result = (T) restTemplate.patchForObject(uri, setRequestHeader(requestType, mediaType), responseClass);
			JSONObject resObj = (JSONObject) result;

		} catch (Exception e) {

			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), e.getMessage() + ExceptionUtils.getStackTrace(e));

			throw e;
		}
		return result;
	}

	public <T> T patchApi(String uri, Object requestType, Class<?> responseClass) throws Exception {
		return patchApi(uri, null, requestType, responseClass);
	}

	/**
	 * Put api.
	 *
	 * @param <T>           the generic type
	 * @param uri           the uri
	 * @param requestType   the request type
	 * @param responseClass the response class
	 * @return the t
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	public <T> T putApi(String uri, Object requestType, Class<?> responseClass) throws Exception {

		RestTemplate restTemplate;
		T result = null;
		ResponseEntity<T> response = null;
		try {
			restTemplate = getRestTemplate();
			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), uri);
			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), requestType.toString());

			response = (ResponseEntity<T>) restTemplate.exchange(uri, HttpMethod.PUT,
					setRequestHeader(requestType.toString(), null), responseClass);
			result = response.getBody();
		} catch (Exception e) {

			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), e.getMessage() + ExceptionUtils.getStackTrace(e));

			throw e;
		}
		return result;
	}

	public RestTemplate getRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), Arrays.asList(environment.getActiveProfiles()).toString());
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch("dev-k8"::equals)) {
			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					Arrays.asList(environment.getActiveProfiles()).toString());
			return new RestTemplate();
		} else {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();

			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

			requestFactory.setHttpClient(httpClient);
			return new RestTemplate(requestFactory);
		}

	}

	/**
	 * this method sets token to header of the request
	 * 
	 * @param requestType
	 * @param mediaType
	 * @return
	 * @throws IOException
	 */
	private HttpEntity<Object> setRequestHeader(Object requestType, MediaType mediaType) throws IOException {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("Cookie", getToken());
		if (mediaType != null) {
			headers.add("Content-Type", mediaType.toString());
		}
		if (requestType != null)
			return new HttpEntity<Object>(requestType, headers);
		else
			return new HttpEntity<Object>(headers);
	}

	/**
	 * This method gets the token for the user details present in config server.
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getToken() throws IOException {
//		TokenRequestDTO<PasswordRequest> tokenRequestDTO = new TokenRequestDTO<PasswordRequest>();
		TokenRequestDTO<SecretKeyRequest> tokenRequestDTO = new TokenRequestDTO<SecretKeyRequest>();
		tokenRequestDTO.setId(environment.getProperty("token.request.id"));
		tokenRequestDTO.setMetadata(new Metadata());

		tokenRequestDTO.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
//		tokenRequestDTO.setRequest(setPasswordRequestDTO());
		tokenRequestDTO.setRequest(setSecretKeyRequestDTO());
		tokenRequestDTO.setVersion(environment.getProperty("token.request.version"));

		Gson gson = new Gson();
		HttpClient httpClient = HttpClientBuilder.create().build();
//		HttpPost post = new HttpPost(environment.getProperty("PASSWORDBASEDTOKENAPI"));
		HttpPost post = new HttpPost(environment.getProperty("KEYBASEDTOKENAPI"));
		try {
			StringEntity postingString = new StringEntity(gson.toJson(tokenRequestDTO));
			post.setEntity(postingString);
			post.setHeader("Content-type", "application/json");
			HttpResponse response = httpClient.execute(post);
			Header[] cookie = response.getHeaders("Set-Cookie");
			if (cookie.length == 0)
				throw new TokenGenerationFailedException();
			String token = response.getHeaders("Set-Cookie")[0].getValue();

			return token.substring(0, token.indexOf(';'));
		} catch (IOException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}

	private SecretKeyRequest setSecretKeyRequestDTO() {
		SecretKeyRequest request = new SecretKeyRequest();
		request.setAppId(environment.getProperty("token.request.appid"));
		request.setClientId(environment.getProperty("token.request.clientId"));
		request.setSecretKey(environment.getProperty("token.request.secretKey"));
		return request;
	}

	private PasswordRequest setPasswordRequestDTO() {

		PasswordRequest request = new PasswordRequest();
		request.setAppId(environment.getProperty("token.request.appid"));
		request.setPassword(environment.getProperty("token.request.password"));
		request.setUserName(environment.getProperty("token.request.username"));
		return request;
	}

}
