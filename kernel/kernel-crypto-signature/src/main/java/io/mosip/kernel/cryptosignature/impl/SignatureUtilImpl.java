package io.mosip.kernel.cryptosignature.impl;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.auth.adapter.exception.AuthNException;
import io.mosip.kernel.auth.adapter.exception.AuthZException;
import io.mosip.kernel.core.crypto.spi.Decryptor;
import io.mosip.kernel.core.crypto.spi.Encryptor;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.signatureutil.exception.ParseResponseException;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilClientException;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilException;
import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.kernel.cryptosignature.constant.SigningDataErrorCode;
import io.mosip.kernel.cryptosignature.dto.CryptoManagerRequestDto;
import io.mosip.kernel.cryptosignature.dto.KeymanagerPublicKeyResponseDto;
import io.mosip.kernel.cryptosignature.dto.PublicKeyResponse;
import io.mosip.kernel.cryptosignature.dto.SignatureRequestDto;
import io.mosip.kernel.cryptosignature.utils.CryptosignatureUtil;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;

/**
 * SignatureUtilImpl implements {@link SignatureUtil} .
 * 
 * @author Srinivasan
 * @author Urvil Joshi
 * @since 1.0.0
 */
@Component
public class SignatureUtilImpl implements SignatureUtil {

	@Value("${mosip.kernel.keygenerator.asymmetric-algorithm-name}")
	private String asymmetricAlgorithmName;
	
	
	/** The sync data request id. */
	@Value("${mosip.kernel.signature.signature-request-id}")
	private String syncDataRequestId;

	/** The sync data version id. */
	@Value("${mosip.kernel.signature.signature-version-id}")
	private String syncDataVersionId;

	/** The encrypt url. */
	@Value("${mosip.kernel.signature.cryptomanager-encrypt-url}")
	private String encryptUrl;

	/** The get public key url. */
	@Value("${mosip.kernel.keymanager-service-publickey-url}")
	private String getPublicKeyUrl;
	
	@Value("${mosip.sign-certificate-refid}")
	private String certificateSignRefID;

	/** The rest template. */
	@Autowired
	RestTemplate restTemplate;

	/** The object mapper. */
	@Autowired
	private ObjectMapper objectMapper;

	/** The signed header. */
	@Value("${mosip.sign.header:response-signature}")
	private String signedHeader;

	/** The sign applicationid. */
	@Value("${mosip.sign.applicationid:KERNEL}")
	private String signApplicationid;

	/** The sign refid. */
	@Value("${mosip.sign.refid:KER}")
	private String signRefid;
	

	@Value("${mosip.kernel.cryptomanager-service-encrypt-signature-url}")
	private String signaturePrivateEncryptURL;
	
	@Value("${mosip.kernel.cryptomanager-service-publickey-signature-url}")
	private String signaturePublicURL;
	
	private static final String CRYPTOMANAAGER_RESPONSE_SOURCE="Cryptomanager";

	/** The decryptor. */
	@Autowired
	Decryptor<PrivateKey, PublicKey, SecretKey> decryptor;

	/** The encryptor. */
	@Autowired
	Encryptor<PrivateKey, PublicKey, SecretKey> encryptor;

	/** The key gen. */
	@Autowired
	KeyGenerator keyGen;
	
	@Override
	public SignatureResponse sign(String data, String timestamp) {
		byte[] responseByteArray = HMACUtils.generateHash(data.getBytes());
		CryptoManagerRequestDto cryptoManagerRequestDto = new CryptoManagerRequestDto();
		cryptoManagerRequestDto.setApplicationId(signApplicationid);
		cryptoManagerRequestDto.setReferenceId(signRefid);
		cryptoManagerRequestDto.setData(CryptoUtil.encodeBase64(responseByteArray));
		cryptoManagerRequestDto.setTimeStamp(timestamp);
		RequestWrapper<CryptoManagerRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId(syncDataRequestId);
		requestWrapper.setVersion(syncDataVersionId);
		requestWrapper.setRequest(cryptoManagerRequestDto);
		ResponseEntity<String> responseEntity = null;

		try {
			responseEntity = restTemplate.postForEntity(encryptUrl, requestWrapper, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

			if (ex.getRawStatusCode() == 401) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthNException(validationErrorsList);
				} else {
					throw new BadCredentialsException("Authentication failed from CryptoManager");
				}
			}
			if (ex.getRawStatusCode() == 403) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthZException(validationErrorsList);
				} else {
					throw new AccessDeniedException("Access denied from CryptoManager");
				}
			}
			if (!validationErrorsList.isEmpty()) {
				throw new SignatureUtilClientException(validationErrorsList);
			} else {
				throw new SignatureUtilException(SigningDataErrorCode.REST_CRYPTO_CLIENT_EXCEPTION.getErrorCode(),
						SigningDataErrorCode.REST_CRYPTO_CLIENT_EXCEPTION.getErrorMessage());
			}
		}
		List<ServiceError> validationErrorsList = null;
		validationErrorsList = ExceptionUtils.getServiceErrorList(responseEntity.getBody());

		if (!validationErrorsList.isEmpty()) {
			throw new SignatureUtilClientException(validationErrorsList);
		}
		SignatureResponse SignatureResponse = null;
		ResponseWrapper<SignatureResponse> responseObject;
		try {

			responseObject = objectMapper.readValue(responseEntity.getBody(),
					new TypeReference<ResponseWrapper<SignatureResponse>>() {
					});

			SignatureResponse = responseObject.getResponse();
			SignatureResponse.setTimestamp(DateUtils.convertUTCToLocalDateTime(timestamp));
		} catch (IOException | NullPointerException exception) {
			throw new ParseResponseException(SigningDataErrorCode.RESPONSE_PARSE_EXCEPTION.getErrorCode(),
					SigningDataErrorCode.RESPONSE_PARSE_EXCEPTION.getErrorMessage());
		}

		return SignatureResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.signatureutil.spi.SignatureUtil#validateWithPublicKey(
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean validateWithPublicKey(String signature, String data, String publickey)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		byte[] syncDataBytearray = HMACUtils.generateHash(data.getBytes());
		String actualHash = CryptoUtil.encodeBase64(syncDataBytearray);
		// System.out.println("Actual Hash: " + actualHash);
		PublicKey key = null;
		key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(CryptoUtil.decodeBase64(publickey)));
		byte[] decodedEncryptedData = CryptoUtil.decodeBase64(signature);
		byte[] hashedEncodedData = decryptor.asymmetricPublicDecrypt(key, decodedEncryptedData);
		String signedHash = new String(hashedEncodedData);
		// System.out.println("Signed Hash: " + signedHash);
		return signedHash.equals(actualHash);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.signatureutil.spi.SignatureUtil#validateWithPublicKey(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public boolean validate(String signature, String data, String timestamp)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		Map<String, String> uriParams = new HashMap<>();
		ResponseEntity<String> keyManagerResponse = null;
		uriParams.put("applicationId", signApplicationid);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getPublicKeyUrl)
				.queryParam("timeStamp", timestamp).queryParam("referenceId", signRefid);

		try {
			keyManagerResponse = restTemplate.exchange(builder.buildAndExpand(uriParams).toUri(), HttpMethod.GET, null,
					String.class);
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

			if (ex.getRawStatusCode() == 401) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthNException(validationErrorsList);
				} else {
					throw new BadCredentialsException("Authentication failed for PublicKey");
				}
			}
			if (ex.getRawStatusCode() == 403) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthZException(validationErrorsList);
				} else {
					throw new AccessDeniedException("Access denied for PublicKey");
				}
			}
			if (!validationErrorsList.isEmpty()) {
				throw new SignatureUtilClientException(validationErrorsList);
			} else {
				throw new SignatureUtilException(SigningDataErrorCode.REST_KM_CLIENT_EXCEPTION.getErrorCode(),
						SigningDataErrorCode.REST_KM_CLIENT_EXCEPTION.getErrorMessage());
			}
		}
		String keyResponseBody = keyManagerResponse.getBody();
		ExceptionUtils.getServiceErrorList(keyResponseBody);
		KeymanagerPublicKeyResponseDto keyManagerResponseDto = null;
		ResponseWrapper<?> keyResponseWrp;
		try {
			keyResponseWrp = objectMapper.readValue(keyResponseBody, ResponseWrapper.class);
			objectMapper.registerModule(new JavaTimeModule());
			keyManagerResponseDto = objectMapper.readValue(
					objectMapper.writeValueAsString(keyResponseWrp.getResponse()),
					KeymanagerPublicKeyResponseDto.class);
		} catch (IOException e) {
			throw new SignatureUtilException(SigningDataErrorCode.RESPONSE_PARSE_EXCEPTION.getErrorCode(),
					SigningDataErrorCode.RESPONSE_PARSE_EXCEPTION.getErrorMessage());
		}
		byte[] syncDataBytearray = HMACUtils.generateHash(data.getBytes());
		String actualHash = CryptoUtil.encodeBase64(syncDataBytearray);
		// System.out.println("Actual Hash: " + actualHash);
		PublicKey key = null;
		key = KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(CryptoUtil.decodeBase64(keyManagerResponseDto.getPublicKey())));
		byte[] decodedEncryptedData = CryptoUtil.decodeBase64(signature);
		byte[] hashedEncodedData = decryptor.asymmetricPublicDecrypt(key, decodedEncryptedData);
		String signedHash = new String(hashedEncodedData);
		// System.out.println("Signed Hash: " + signedHash);
		return signedHash.equals(actualHash);
	}

	@Override
	public SignatureResponse signResponseByCertificate(String response) {
		String responseHash = CryptoUtil.encodeBase64(HMACUtils.generateHash(response.getBytes()));
		SignatureRequestDto signatureRequestDto = new SignatureRequestDto();
		signatureRequestDto.setApplicationId(signApplicationid);
		signatureRequestDto.setReferenceId(certificateSignRefID);
		signatureRequestDto.setData(responseHash);
		final LocalDateTime signTime=DateUtils.getUTCCurrentDateTime();
		signatureRequestDto.setTimeStamp(signTime);
		RequestWrapper<SignatureRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId(syncDataRequestId);
		requestWrapper.setVersion(syncDataVersionId);
		requestWrapper.setRequest(signatureRequestDto);
		ResponseEntity<String> responseEntity = null;

		try {
			responseEntity = restTemplate.postForEntity(signaturePrivateEncryptURL, requestWrapper, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());


			CryptosignatureUtil.authExceptionHandler(ex, validationErrorsList, CRYPTOMANAAGER_RESPONSE_SOURCE);
			
			if (!validationErrorsList.isEmpty()) {
				throw new SignatureUtilClientException(validationErrorsList);
			} else {
				throw new SignatureUtilException(SigningDataErrorCode.REST_CRYPTO_CLIENT_EXCEPTION.getErrorCode(),
						SigningDataErrorCode.REST_CRYPTO_CLIENT_EXCEPTION.getErrorMessage());
			}
		}
		CryptosignatureUtil.throwExceptionIfExist(responseEntity);
		SignatureResponse signatureResponse=CryptosignatureUtil.getResponse(objectMapper, responseEntity, SignatureResponse.class);
		signatureResponse.setData(signatureResponse.getData());
        return signatureResponse;
	}

	@Override
	public boolean validateWithCertificate(String signature, String actualData, LocalDateTime responseTime) throws InvalidKeySpecException, NoSuchAlgorithmException {
	
	ResponseEntity<String> response = null;
	Map<String, String> uriParams = new HashMap<>();
	uriParams.put("applicationId", signApplicationid);
	UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(signaturePublicURL)
			.queryParam("timeStamp", DateUtils.formatToISOString(responseTime))
			.queryParam("referenceId",certificateSignRefID);
	try {
		response = restTemplate.exchange(builder.buildAndExpand(uriParams).toUri(), HttpMethod.GET, null,
				String.class);
	} catch (HttpClientErrorException | HttpServerErrorException ex) {
		List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

		CryptosignatureUtil.authExceptionHandler(ex, validationErrorsList, CRYPTOMANAAGER_RESPONSE_SOURCE);
		
		if (!validationErrorsList.isEmpty()) {
			throw new SignatureUtilClientException(validationErrorsList);
		} else {
			throw new SignatureUtilException(SigningDataErrorCode.REST_CRYPTO_CLIENT_EXCEPTION.getErrorCode(),
					SigningDataErrorCode.REST_CRYPTO_CLIENT_EXCEPTION.getErrorMessage());
		}

	}
	CryptosignatureUtil.throwExceptionIfExist(response);
	PublicKeyResponse publicKeyResponse= CryptosignatureUtil.getResponse(objectMapper,response, PublicKeyResponse.class);
	PublicKey publicKey= KeyFactory.getInstance(asymmetricAlgorithmName)
			.generatePublic(new X509EncodedKeySpec(CryptoUtil.decodeBase64(publicKeyResponse.getPublicKey())));
	String decryptedSignature=CryptoUtil.encodeBase64(decryptor.asymmetricPublicDecrypt(publicKey, CryptoUtil.decodeBase64(signature)));
	String actualDataHash = CryptoUtil.encodeBase64(HMACUtils.generateHash(actualData.getBytes()));
	return decryptedSignature.equals(actualDataHash);
	}
}
