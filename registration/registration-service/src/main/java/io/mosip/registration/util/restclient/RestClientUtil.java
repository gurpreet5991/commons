package io.mosip.registration.util.restclient;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.spi.logger.MosipLogger;
import io.mosip.kernel.logger.appender.MosipRollingFileAppender;
import io.mosip.kernel.logger.factory.MosipLogfactory;

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
		 
		 private static MosipLogger LOGGER;

			@Autowired
			private void initializeLogger(MosipRollingFileAppender mosipRollingFileAppender) {
				LOGGER = MosipLogfactory.getMosipDefaultRollingFileLogger(mosipRollingFileAppender, this.getClass());
			}
		 
	/**
	 * Actual exchange using rest template
	 * 
	 * @param requestDto
	 * @return ResponseEntity<?> response entity obtained from api
	 * @throws HttpClientErrorException when client error exception from server
	 * @throws HttpServerErrorException when server exception from server
	 */
	public Object invoke(RequestHTTPDTO requestHTTPDTO) throws HttpClientErrorException, HttpServerErrorException {
		LOGGER.debug("REGISTRATION - REST_CLIENT_UTIL - INVOKE", APPLICATION_NAME,
				APPLICATION_ID, "invoke method called");

		ResponseEntity<?> responseEntity = null;
		Object responseBody=null;
			responseEntity = restTemplate.exchange(requestHTTPDTO.getUri(), requestHTTPDTO.getHttpMethod(),
					requestHTTPDTO.getHttpEntity(), requestHTTPDTO.getClazz());
			if(responseEntity!=null) {
				if(responseEntity.hasBody()) {
					responseBody=responseEntity.getBody();
				}
			}
			LOGGER.debug("REGISTRATION - REST_CLIENT_UTIL - INVOKE", APPLICATION_NAME,
					APPLICATION_ID, "invoke method ended");

			System.out.println(responseBody);
			
		return responseBody;

	}

}
