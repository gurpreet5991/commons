package io.mosip.registration.util.restclient;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;

/**
 * This is a general method which gives the response for all httpmethod
 * designators
 * 
 * @author Yaswanth S
 * @since 1.0.0
 *
 */
@Service
public class RestClientUtil {

	/**
	 * Rest Template is a interaction with HTTP servers and enforces RESTful systems
	 */
	@Autowired
	RestTemplate restTemplate;

	private static final Logger LOGGER = AppConfig.getLogger(RestClientUtil.class);

	/**
	 * Actual exchange using rest template
	 * 
	 * @param requestDto
	 * @return ResponseEntity<?> response entity obtained from api
	 * @throws HttpClientErrorException when client error exception from server
	 * @throws HttpServerErrorException when server exception from server
	 */
	public Map<String, Object> invoke(RequestHTTPDTO requestHTTPDTO)
			throws HttpClientErrorException, HttpServerErrorException, SocketTimeoutException, ResourceAccessException {
		LOGGER.debug("REGISTRATION - REST_CLIENT_UTIL - INVOKE", APPLICATION_NAME, APPLICATION_ID,
				"invoke method called");

		ResponseEntity<?> responseEntity = null;
		Map<String, Object> responseMap = null;
		restTemplate.setRequestFactory(requestHTTPDTO.getSimpleClientHttpRequestFactory());
		try {
			if (requestHTTPDTO.getUri().toString().contains("https"))
				turnOffSslChecking();
		} catch (KeyManagementException keyManagementException) {
			LOGGER.error("REGISTRATION - REST_CLIENT_UTIL - INVOKE", APPLICATION_NAME, APPLICATION_ID,
					keyManagementException.getMessage() + ExceptionUtils.getStackTrace(keyManagementException));
		} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			LOGGER.error("REGISTRATION - REST_CLIENT_UTIL - INVOKE", APPLICATION_NAME, APPLICATION_ID,
					noSuchAlgorithmException.getMessage() + ExceptionUtils.getStackTrace(noSuchAlgorithmException));
		}

		responseEntity = restTemplate.exchange(requestHTTPDTO.getUri(), requestHTTPDTO.getHttpMethod(),
				requestHTTPDTO.getHttpEntity(), requestHTTPDTO.getClazz());

		if (responseEntity != null && responseEntity.hasBody()) {
			responseMap = new HashMap<>();
			responseMap.put(RegistrationConstants.REST_RESPONSE_BODY, responseEntity.getBody());
			responseMap.put(RegistrationConstants.REST_RESPONSE_HEADERS, responseEntity.getHeaders());
		}

		LOGGER.debug("REGISTRATION - REST_CLIENT_UTIL - INVOKE", APPLICATION_NAME, APPLICATION_ID,
				"invoke method ended");

		return responseMap;
	}

	public static void turnOffSslChecking() throws NoSuchAlgorithmException, KeyManagementException {
		// Install the all-trusting trust manager
		final SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws java.security.cert.CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws java.security.cert.CertificateException {
			// TODO Auto-generated method stub

		}
	} };

}
