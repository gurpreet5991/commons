package io.mosip.registration.util.advice;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.crypto.spi.Decryptor;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.LoggerConstants;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dao.PolicySyncDAO;
import io.mosip.registration.entity.KeyStore;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.util.restclient.RequestHTTPDTO;

/**
 * The Class ResponseSignatureAdvice will be called after a rest services call.
 *
 * @author Sreekar Chukka
 * @version 1.0
 */
@Aspect
@Component
public class ResponseSignatureAdvice {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = AppConfig.getLogger(ResponseSignatureAdvice.class);

	/** The decryptor. */
	@Autowired
	Decryptor<PrivateKey, PublicKey, SecretKey> decryptor;

	/** The key generator. */
	@Autowired
	KeyGenerator keyGenerator;

	/** The SignatureUtil. */
	@Autowired
	SignatureUtil signatureUtil;

	/** The policy sync DAO. */
	@Autowired
	private PolicySyncDAO policySyncDAO;

	/**
	 * Response signature.
	 *
	 * @param joinPoint the join point
	 * @param result    the result
	 * @return the map
	 * @throws RegBaseCheckedException the reg base checked exception
	 */
	@SuppressWarnings("unchecked")
	@AfterReturning(pointcut = "execution(* io.mosip.registration.util.restclient.RestClientUtil.invoke(..))", returning = "result")
	public Map<String, Object> responseSignatureValidation(JoinPoint joinPoint, Object result)
			throws RegBaseCheckedException {

		LOGGER.info(LoggerConstants.RESPONSE_SIGNATURE_VALIDATION, APPLICATION_ID, APPLICATION_NAME,
				"Entering into response signature method");

		Object[] requestHTTPDTO = joinPoint.getArgs();
		RequestHTTPDTO requestDto = (RequestHTTPDTO) requestHTTPDTO[0];
		LinkedHashMap<String, Object> restClientResponse = null;
		String publicKey = RegistrationConstants.EMPTY;

		try {

			restClientResponse = (LinkedHashMap<String, Object>) result;

			if (null != requestDto && requestDto.getIsSignRequired()) {

				KeyStore keyStore = policySyncDAO.getPublicKey(RegistrationConstants.KER);

				if (null != keyStore && null != keyStore.getPublicKey()) {
					publicKey = new String(keyStore.getPublicKey());
				} else {
					LinkedHashMap<String, Object> keyResponse = (LinkedHashMap<String, Object>) restClientResponse
							.get(RegistrationConstants.REST_RESPONSE_BODY);

					if (null != keyResponse && keyResponse.size() > 0
							&& null != keyResponse.get(RegistrationConstants.PACKET_STATUS_READER_RESPONSE)) {
						LinkedHashMap<String, Object> resp = (LinkedHashMap<String, Object>) keyResponse
								.get(RegistrationConstants.PACKET_STATUS_READER_RESPONSE);
						publicKey = (String) resp.get(RegistrationConstants.PUBLIC_KEY);
					}
				}

				LOGGER.info(LoggerConstants.RESPONSE_SIGNATURE_VALIDATION, APPLICATION_ID, APPLICATION_NAME,
						requestDto.getUri().getPath().replaceAll("/", "====>"));

				LinkedHashMap<String, Object> responseBodyMap = (LinkedHashMap<String, Object>) restClientResponse
						.get(RegistrationConstants.REST_RESPONSE_BODY);

				LOGGER.info(LoggerConstants.RESPONSE_SIGNATURE_VALIDATION, APPLICATION_ID, APPLICATION_NAME,
						"Getting public key");

				Map<String, Object> responseMap = (Map<String, Object>) restClientResponse
						.get(RegistrationConstants.REST_RESPONSE_HEADERS);

				if (signatureUtil.validateWithPublicKey(
						responseMap.get("response-signature").toString().replace("[", "").replace("]", ""),
						new ObjectMapper().writeValueAsString(responseBodyMap), publicKey)) {
					LOGGER.info(LoggerConstants.RESPONSE_SIGNATURE_VALIDATION, APPLICATION_ID, APPLICATION_NAME,
							"response signature is valid...");
					return restClientResponse;
				} else {
					LOGGER.info(LoggerConstants.RESPONSE_SIGNATURE_VALIDATION, APPLICATION_ID, APPLICATION_NAME,
							"response signature is Invalid...");
					restClientResponse.put(RegistrationConstants.REST_RESPONSE_BODY, new LinkedHashMap<>());
					restClientResponse.put(RegistrationConstants.REST_RESPONSE_HEADERS, new LinkedHashMap<>());
				}
			}

		} catch (InvalidKeySpecException | NoSuchAlgorithmException | JsonProcessingException
				| RuntimeException regBaseCheckedException) {
			LOGGER.error(LoggerConstants.RESPONSE_SIGNATURE_VALIDATION, APPLICATION_ID, APPLICATION_NAME,
					ExceptionUtils.getStackTrace(regBaseCheckedException));
			throw new RegBaseCheckedException("Exception in response signature", regBaseCheckedException.getMessage());
		}

		LOGGER.info(LoggerConstants.RESPONSE_SIGNATURE_VALIDATION, APPLICATION_ID, APPLICATION_NAME,
				"succesfully leaving response signature method...");

		return restClientResponse;

	}

}
