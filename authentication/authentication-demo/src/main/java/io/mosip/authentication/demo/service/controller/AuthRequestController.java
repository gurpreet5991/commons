package io.mosip.authentication.demo.service.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.exception.IdAuthenticationAppException;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.spi.indauth.match.MatchType;
import io.mosip.authentication.demo.service.dto.EncryptionRequestDto;
import io.mosip.authentication.demo.service.dto.EncryptionResponseDto;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.kernel.core.util.DateUtils;

/**
 * The Class AuthRequestController is used to automate the creation of Auth Request.
 * @author Arun Bose S 
 */
@RestController
public class AuthRequestController {
	
	/** The Constant TEMPLATE. */
	private static final String TEMPLATE = "Template";
	
	private static final String PIN = "pin";

	private static final String BIO = "bio";

	private static final String DEMO = "demo";

	private static final String OTP = "otp";

	private static final String TIMESTAMP = "timestamp";

	private static final String TXN = "txn";

	private static final String VER = "ver";

	private static final String IDA_API_VERSION = "ida.api.version";

	private static final String AUTH_TYPE = "authType";

	private static final String UIN = "UIN";

	private static final String ID_TYPE = "idType";

	private static final String IDA_AUTH_REQUEST_TEMPLATE = "ida.authRequest.template";

	private static final String ID = "id";

	private static final String CLASSPATH = "classpath";

	private static final String ENCODE_TYPE = "UTF-8";

	@Autowired
	private Encrypt encrypt;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private TemplateManagerBuilder templateManagerBuilder;
	
	@Autowired
	private TemplateManager templateManager;
	
	@PostConstruct
	public void idTemplateManagerPostConstruct() {
		templateManager = templateManagerBuilder.encodingType(ENCODE_TYPE).enableCache(false).resourceLoader(CLASSPATH)
				.build();
	}
	
	/**
	 * this method is used to create  the auth request.
	 *
	 * @param id the id
	 * @param idType the id type
	 * @param identity the identity
	 * @return the string
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchPaddingException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws KeyManagementException 
	 * @throws IdAuthenticationAppException 
	 * @throws IdAuthenticationBusinessException 
	 */
	@PostMapping(path = "/identity/createAuthRequest",consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
	public String createAuthRequest(@RequestParam(name=ID,required=true) @Nullable String id, 
			@RequestParam(name=ID_TYPE,required=false) @Nullable String idType,
			@RequestParam(name="isKyc",required=false) @Nullable Boolean isKyc,
			@RequestParam(name="Authtype",required=false) @Nullable String reqAuth,
			@RequestParam(name="transactionId",required=false) @Nullable String transactionId,
			  @RequestBody Map<String,Object> request) throws KeyManagementException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException, JSONException, IdAuthenticationAppException, IdAuthenticationBusinessException {
		String authRequestTemplate=environment.getProperty(IDA_AUTH_REQUEST_TEMPLATE);
		Map<String,Object> reqValues=new HashMap<>();
		idValuesMap(id, idType, isKyc, reqValues, transactionId);
		encryptValuesMap(request, reqValues);
		reqValues.put(OTP,false);
		 reqValues.put(DEMO,false);
		 reqValues.put(BIO,false);
		 reqValues.put(PIN,false);
		getAuthTypeMap(reqAuth,reqValues, request);
		StringWriter writer = new StringWriter();
		InputStream templateValue;
		templateValue = templateManager
				.merge(new ByteArrayInputStream(authRequestTemplate.getBytes(StandardCharsets.UTF_8)), reqValues);
		if (templateValue != null) {
			IOUtils.copy(templateValue, writer, StandardCharsets.UTF_8);
			return writer.toString();
		} else {
			throw new IdAuthenticationBusinessException(
					IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					String.format(IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage(), TEMPLATE));
		}
		
	}

	/**
	 * 
	 *
	 * @param reqAuth 
	 * @param reqValues 
	 * @param request 
	 */
	private void getAuthTypeMap(String reqAuth, Map<String, Object> reqValues, Map<String, Object> request) {
		String[] reqAuthArr;
		if (reqAuth == null) {
			BiFunction<String, String, Optional<String>> authTypeMapFunction = (key, authType) -> Optional.ofNullable(request).filter(map -> map.containsKey(key)).map(map -> authType);
			reqAuthArr = Stream.of(
					authTypeMapFunction.apply("demographics",  "demo"),
					authTypeMapFunction.apply("biometrics",  "bio"),
					authTypeMapFunction.apply("otp",  "otp"),
					authTypeMapFunction.apply("staticPin",  "pin")
					).filter(Optional::isPresent)
					.map(Optional::get)
					.toArray(size -> new String[size]);
		} else {
			reqAuth = reqAuth.trim();
			if (reqAuth.contains(",")) {
				reqAuthArr = reqAuth.split(",");
			} else {
				reqAuthArr = new String[] { reqAuth };
			}
		}
		
		for (String authType : reqAuthArr) {
			authTypeSelectionMap(reqValues, authType);
		}
	}

	private void authTypeSelectionMap(Map<String, Object> reqValues, String authType) {

		if (authType.equalsIgnoreCase(MatchType.Category.OTP.getType())) {
			reqValues.put(OTP, true);
		} else if (authType.equalsIgnoreCase(MatchType.Category.DEMO.getType())) {
			reqValues.put(DEMO, true);
		} else if (authType.equalsIgnoreCase(MatchType.Category.BIO.getType())) {
			reqValues.put(BIO, true);
		} else if (authType.equalsIgnoreCase(MatchType.Category.SPIN.getType())) {
			reqValues.put("pin", true);
		}
	}




	private void encryptValuesMap(Map<String, Object> identity, Map<String, Object> reqValues)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, KeyManagementException,
			JSONException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		EncryptionRequestDto encryptionRequestDto=new EncryptionRequestDto();
		encryptionRequestDto.setIdentityRequest(identity);
		EncryptionResponseDto encryptionResponse=encrypt.encrypt(encryptionRequestDto);
		reqValues.put("encHmac", encryptionResponse.getRequestHMAC());
		reqValues.put("encSessionKey", encryptionResponse.getEncryptedSessionKey());
		reqValues.put("encRequest", encryptionResponse.getEncryptedIdentity());
	}


	private void idValuesMap(String id, String idType, Boolean isKyc, Map<String, Object> reqValues,
			String transactionId) {
		reqValues.put(ID, id);
		if (null != idType) {
			reqValues.put(ID_TYPE, idType);
		} else {
			reqValues.put(ID_TYPE, UIN);
		}
		if (isKyc != null && isKyc) {
			reqValues.put(AUTH_TYPE, "kyc");
			reqValues.put("secondaryLangCode", environment.getProperty("mosip.secondary-language"));
			
		} else {
			reqValues.put(AUTH_TYPE, "auth");
		}
		reqValues.put(TIMESTAMP, DateUtils.getUTCCurrentDateTimeString(environment.getProperty("datetime.pattern")));
		reqValues.put(TXN, transactionId == null ? "1234567890" : transactionId);
		reqValues.put(VER, environment.getProperty(IDA_API_VERSION));
	}

}
