package io.mosip.authentication.demo.service.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.authentication.demo.service.dto.CryptomanagerRequestDto;
import io.mosip.authentication.demo.service.dto.EncryptionRequestDto;
import io.mosip.authentication.demo.service.dto.EncryptionResponseDto;
import io.mosip.authentication.demo.service.helper.CryptoUtility;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils;
import io.swagger.annotations.ApiOperation;

/**
 * The Class Encrypt is used to encrypt the identity block using Kernel Api.
 *
 * @author Dinesh Karuppiah
 */

@RestController
public class Encrypt {

	@Autowired
	private Environment env;

	private static final String ASYMMETRIC_ALGORITHM_NAME = "RSA";

	/** The Constant ASYMMETRIC_ALGORITHM. */
	private static final String SSL = "SSL";

	/** The obj mapper. */
	@Autowired
	private ObjectMapper objMapper;

	/** KeySplitter. */

	@Value("${mosip.kernel.data-key-splitter}")
	private String keySplitter;

	/** The encrypt URL. */
	@Value("${mosip.kernel.publicKey-url}")
	private String publicKeyURL;

	/** The app ID. */
	@Value("${application.id}")
	private String appID;

	/** The IDA Public Key. */
	@Value("${mosip.ida.publickey}")
	private String publicKeyId;

	/**
	 * Encrypt.
	 *
	 * @param encryptionRequestDto
	 *            the encryption request dto
	 * @return the encryption response dto
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws InvalidKeySpecException
	 *             the invalid key spec exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws KeyManagementException
	 *             the key management exception
	 * @throws RestClientException
	 *             the rest client exception
	 * @throws JSONException
	 *             the JSON exception
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 */
	@PostMapping(path = "/encrypt")
	@ApiOperation(value = "Encrypt Identity with sessionKey and Encrypt Session Key with Public Key", response = EncryptionResponseDto.class)
	public EncryptionResponseDto encrypt(@RequestBody EncryptionRequestDto encryptionRequestDto)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, KeyManagementException,
			JSONException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		return kernelEncrypt(encryptionRequestDto);
	}

	/**
	 * this method is used to call Kernel encrypt api.
	 *
	 * @param encryptionRequestDto
	 *            the encryption request dto
	 * @return the encryption response dto
	 * @throws KeyManagementException
	 *             the key management exception
	 * @throws RestClientException
	 *             the rest client exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws JsonProcessingException
	 *             the json processing exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws JSONException
	 *             the JSON exception
	 */
	private EncryptionResponseDto kernelEncrypt(EncryptionRequestDto encryptionRequestDto)
			throws KeyManagementException, NoSuchAlgorithmException, IOException, JSONException, InvalidKeyException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException,
			InvalidKeySpecException {
		String identityBlock = objMapper.writeValueAsString(encryptionRequestDto.getIdentityRequest());
		CryptoUtility cryptoUtil = new CryptoUtility(); // TODO FIXME
		SecretKey secretKey = cryptoUtil.genSecKey();
		EncryptionResponseDto encryptionResponseDto = new EncryptionResponseDto();
		byte[] encryptedIdentityBlock = cryptoUtil.symmetricEncrypt(identityBlock.getBytes(), secretKey);
		encryptionResponseDto.setEncryptedIdentity(Base64.encodeBase64URLSafeString(encryptedIdentityBlock));
		String publicKeyStr = getPublicKey(identityBlock);
		PublicKey publicKey = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM_NAME)
				.generatePublic(new X509EncodedKeySpec(CryptoUtil.decodeBase64(publicKeyStr)));
		byte[] encryptedSessionKeyByte = cryptoUtil.asymmetricEncrypt((secretKey.getEncoded()), publicKey);
		encryptionResponseDto.setEncryptedSessionKey(Base64.encodeBase64URLSafeString(encryptedSessionKeyByte));
		byte[] byteArr = cryptoUtil.symmetricEncrypt(
				HMACUtils.digestAsPlainText(HMACUtils.generateHash(identityBlock.getBytes())).getBytes(), secretKey);
		encryptionResponseDto.setRequestHMAC(Base64.encodeBase64URLSafeString(byteArr));
		return encryptionResponseDto;
	}

	/**
	 * Gets the encrypted value.
	 *
	 * @param data
	 *            the data
	 * @param tspID
	 *            the tsp ID
	 * @return the encrypted value
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws KeyManagementException
	 *             the key management exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws RestClientException
	 *             the rest client exception
	 * @throws JSONException
	 *             the JSON exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getPublicKey(String data)
			throws IOException, KeyManagementException, NoSuchAlgorithmException, RestClientException, JSONException {
		turnOffSslChecking();
		RestTemplate restTemplate = new RestTemplate();
		ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {

			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				String authToken = generateAuthToken();
				if(authToken != null && !authToken.isEmpty()) {
					request.getHeaders().set("Cookie", "Authorization=" + authToken);
				}
				return execution.execute(request, body);
			}
		};

		restTemplate.setInterceptors(Collections.singletonList(interceptor));

		CryptomanagerRequestDto request = new CryptomanagerRequestDto();
		request.setApplicationId(appID);
		request.setData(Base64.encodeBase64URLSafeString(data.getBytes(StandardCharsets.UTF_8)));
		request.setReferenceId(publicKeyId);
		String utcTime = DateUtils.getUTCCurrentDateTimeString();
		request.setTimeStamp(utcTime);
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put("appId", appID);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(publicKeyURL)
				.queryParam("timeStamp", DateUtils.getUTCCurrentDateTimeString())
				.queryParam("referenceId", publicKeyId);
		ResponseEntity<Map> response = restTemplate.exchange(builder.build(uriParams), HttpMethod.GET, null, Map.class);
		return (String) ((Map<String, Object>) response.getBody().get("response")).get("publicKey");
	}

	private String generateAuthToken() {
		ObjectNode requestBody = objMapper.createObjectNode();
		requestBody.put("clientId", env.getProperty("auth-token-generator.rest.clientId"));
		requestBody.put("secretKey", env.getProperty("auth-token-generator.rest.secretKey"));
		requestBody.put("appId", env.getProperty("auth-token-generator.rest.appId"));
		RequestWrapper<ObjectNode> request = new RequestWrapper<>();
		request.setRequesttime(DateUtils.getUTCCurrentDateTime());
		request.setRequest(requestBody);
		ClientResponse response = WebClient.create(env.getProperty("auth-token-generator.rest.uri")).post()
				.syncBody(request)
				.exchange().block();
		System.out.println("AuthResponse :" +  response.toEntity(String.class).block().getBody());
		List<ResponseCookie> list = response.cookies().get("Authorization");
		if(list != null && !list.isEmpty()) {
			ResponseCookie responseCookie = list.get(0);
			return responseCookie.getValue();
		}
		return "";
	}

	/**
	 * Gets the headers.
	 *
	 * @param req
	 *            the req
	 * @return the headers
	 */
	@SuppressWarnings("unused")
	private HttpEntity<CryptomanagerRequestDto> getHeaders(CryptomanagerRequestDto req) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		return new HttpEntity<CryptomanagerRequestDto>(req, headers);
	}

	/**
	 * The Constant UNQUESTIONING_TRUST_MANAGER nullifies the check for certificates
	 * for SSL Connection
	 */
	private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String arg1)
				throws CertificateException {
		}
	} };

	/**
	 * Turns off the ssl checking.
	 *
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws KeyManagementException
	 *             the key management exception
	 */
	public static void turnOffSslChecking() throws NoSuchAlgorithmException, KeyManagementException {
		// Install the all-trusting trust manager
		final SSLContext sc = SSLContext.getInstance(Encrypt.SSL);
		sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

}
