/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.cryptomanager.utils;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.crypto.exception.InvalidKeyException;
import io.mosip.kernel.core.datamapper.spi.DataMapper;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.cryptomanager.constant.CryptomanagerErrorCode;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.dto.KeymanagerPublicKeyResponseDto;
import io.mosip.kernel.cryptomanager.dto.KeymanagerSymmetricKeyRequestDto;
import io.mosip.kernel.cryptomanager.dto.KeymanagerSymmetricKeyResponseDto;
import io.mosip.kernel.cryptomanager.exception.KeymanagerServiceException;
import io.mosip.kernel.cryptomanager.exception.ParseResponseException;

/**
 * Util class for this project
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
@RefreshScope
@Component
public class CryptomanagerUtil {

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Asymmetric Algorithm Name
	 */
	@Value("${mosip.kernel.crypto.asymmetric-algorithm-name}")
	private String asymmetricAlgorithmName;

	/**
	 * Symmetric Algorithm Name
	 */
	@Value("${mosip.kernel.crypto.symmetric-algorithm-name}")
	private String symmetricAlgorithmName;

	/**
	 * Keymanager URL to Get PublicKey
	 */
	@Value("${mosip.kernel.keymanager-service-publickey-url}")
	private String getPublicKeyUrl;

	/**
	 * Keymanager URL to Decrypt Symmetric key
	 */
	@Value("${mosip.kernel.keymanager-service-decrypt-url}")
	private String decryptSymmetricKeyUrl;

	/**
	 * Key Splitter
	 */
	@Value("${mosip.kernel.data-key-splitter}")
	private String keySplitter;

	@Value("${mosip.kernel.cryptomanager.request_id}")
	private String cryptomanagerRequestID;

	@Value("${mosip.kernel.cryptomanager.request_version}")
	private String cryptomanagerRequestVersion;
	/**
	 * {@link DataMapper} instance.
	 */
	@Autowired
	private DataMapper<CryptomanagerRequestDto, KeymanagerSymmetricKeyRequestDto> dataMapper;

	/**
	 * {@link RestTemplate} instance
	 */
	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Calls Key-Manager-Service to get public key of an application
	 * 
	 * @param cryptomanagerRequestDto {@link CryptomanagerRequestDto} instance
	 * @return {@link PublicKey} returned by Key Manager Service
	 */
	public PublicKey getPublicKey(CryptomanagerRequestDto cryptomanagerRequestDto) {
		PublicKey key = null;
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put("applicationId", cryptomanagerRequestDto.getApplicationId());
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getPublicKeyUrl)
				.queryParam("timeStamp", cryptomanagerRequestDto.getTimeStamp().atOffset(ZoneOffset.UTC))
				.queryParam("referenceId", cryptomanagerRequestDto.getReferenceId());

		ResponseEntity<String> response = restTemplate.exchange(builder.buildAndExpand(uriParams).toUri(),
				HttpMethod.GET, null, String.class);

		String responseBody = response.getBody();
		List<ServiceError> validationErrorsList = null;
		validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);

		if (!validationErrorsList.isEmpty()) {
			throw new KeymanagerServiceException(validationErrorsList);
		}
		KeymanagerPublicKeyResponseDto keyManagerResponseDto;
		ResponseWrapper<?> responseObject;
		try {
			responseObject = objectMapper.readValue(response.getBody(), ResponseWrapper.class);
			keyManagerResponseDto = objectMapper.readValue(
					objectMapper.writeValueAsString(responseObject.getResponse()),
					KeymanagerPublicKeyResponseDto.class);
		} catch (IOException | NullPointerException exception) {
			throw new ParseResponseException(CryptomanagerErrorCode.RESPONSE_PARSE_ERROR.getErrorCode(),
					CryptomanagerErrorCode.RESPONSE_PARSE_ERROR.getErrorMessage() + exception.getMessage(), exception);
		}

		try {
			key = KeyFactory.getInstance(asymmetricAlgorithmName).generatePublic(
					new X509EncodedKeySpec(CryptoUtil.decodeBase64(keyManagerResponseDto.getPublicKey())));
		} catch (InvalidKeySpecException e) {
			throw new InvalidKeyException(CryptomanagerErrorCode.INVALID_SPEC_PUBLIC_KEY.getErrorCode(),
					CryptomanagerErrorCode.INVALID_SPEC_PUBLIC_KEY.getErrorMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new io.mosip.kernel.core.exception.NoSuchAlgorithmException(
					CryptomanagerErrorCode.NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					CryptomanagerErrorCode.NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage());
		}
		return key;
	}

	/**
	 * Calls Key-Manager-Service to decrypt symmetric key
	 * 
	 * @param cryptomanagerRequestDto {@link CryptomanagerRequestDto} instance
	 * @return Decrypted {@link SecretKey} from Key Manager Service
	 */
	public SecretKey getDecryptedSymmetricKey(CryptomanagerRequestDto cryptomanagerRequestDto) {
		RequestWrapper<KeymanagerSymmetricKeyRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId(cryptomanagerRequestID);
		requestWrapper.setVersion(cryptomanagerRequestVersion);
		KeymanagerSymmetricKeyRequestDto keyManagerSymmetricKeyRequestDto = new KeymanagerSymmetricKeyRequestDto();
		dataMapper.map(cryptomanagerRequestDto, keyManagerSymmetricKeyRequestDto,
				new KeymanagerSymmetricKeyConverter());
		requestWrapper.setRequest(keyManagerSymmetricKeyRequestDto);

		HttpHeaders keyManagerRequestHeaders = new HttpHeaders();
		keyManagerRequestHeaders.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<RequestWrapper<KeymanagerSymmetricKeyRequestDto>> keyManagerRequestEntity = new HttpEntity<>(
				requestWrapper, keyManagerRequestHeaders);

		ResponseEntity<String> response = restTemplate.exchange(decryptSymmetricKeyUrl, HttpMethod.POST,
				keyManagerRequestEntity, String.class);

		String responseBody = response.getBody();
		List<ServiceError> validationErrorsList = null;
		validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);

		if (!validationErrorsList.isEmpty()) {
			throw new KeymanagerServiceException(validationErrorsList);
		}
		KeymanagerSymmetricKeyResponseDto keyManagerSymmetricKeyResponseDto;
		ResponseWrapper<?> responseObject = null;
		try {
			responseObject = objectMapper.readValue(response.getBody(), ResponseWrapper.class);
			keyManagerSymmetricKeyResponseDto = objectMapper.readValue(
					objectMapper.writeValueAsString(responseObject.getResponse()),
					KeymanagerSymmetricKeyResponseDto.class);
		} catch (IOException | NullPointerException exception) {
			throw new ParseResponseException(CryptomanagerErrorCode.RESPONSE_PARSE_ERROR.getErrorCode(),
					CryptomanagerErrorCode.RESPONSE_PARSE_ERROR.getErrorMessage() + exception.getMessage(), exception);
		}
		byte[] symmetricKey = CryptoUtil.decodeBase64(keyManagerSymmetricKeyResponseDto.getSymmetricKey());
		return new SecretKeySpec(symmetricKey, 0, symmetricKey.length, symmetricAlgorithmName);
	}

}
