package io.mosip.authentication.service.filter;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.exception.IdAuthenticationAppException;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;

/**
 * The Class KycAuthFilter.
 * 
 * @author Sanjay Murali
 */
@Component
public class KycAuthFilter extends BaseAuthFilter {
	
	/** The Constant AUTH_REQUEST. */
	private static final String AUTH_REQUEST = "authRequest";
	
	/** The Constant REQUEST. */
	private static final String REQUEST = "request";
	
	/** The Constant RESPONSE. */
	private static final String RESPONSE = "response";
	
	/** The Constant AUTH. */
	private static final String AUTH = "auth";
	
	/** The Constant KYC. */
	private static final String KYC = "kyc";

	/* (non-Javadoc)
	 * @see io.mosip.authentication.service.filter.BaseAuthFilter#decodedRequest(java.util.Map)
	 */
	@Override
	protected Map<String, Object> decodedRequest(Map<String, Object> requestBody) throws IdAuthenticationAppException {
		try {
			Map<String, Object> authRequest = (Map<String, Object>) decodeToMap((String) requestBody.get(AUTH_REQUEST));
			authRequest.replace(REQUEST, decode((String) authRequest.get(REQUEST)));
			if(null != authRequest.get(REQUEST)) {
				authRequest.replace(REQUEST, keyManager.requestData(authRequest, mapper));				
			}
			requestBody.replace(AUTH_REQUEST, authRequest);
			return requestBody;
		} catch (ClassCastException e) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorCode(),
					IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorMessage());
		}
	}

	/* (non-Javadoc)
	 * @see io.mosip.authentication.service.filter.BaseAuthFilter#encodedResponse(java.util.Map)
	 */
	@Override
	protected Map<String, Object> encodedResponse(Map<String, Object> responseBody)
			throws IdAuthenticationAppException {
		try {
			Map<String, Object> response = (Map<String, Object>) responseBody.get(RESPONSE);
			if (response != null) {
				Object kyc = response.get(KYC);
				
				if (kyc != null) {
					response.replace(KYC, encode(toJsonString(kyc)));
				}
				
				Object auth = response.get(AUTH);
				if (auth != null) {
					response.replace(AUTH, encode(toJsonString(auth)));
				}
				
				responseBody.replace(RESPONSE, encode(toJsonString(responseBody.get(RESPONSE))));
			}
			return responseBody;
		} catch (ClassCastException | JsonProcessingException e) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorCode(),
					IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorMessage());
		}
	}

	private String toJsonString(Object map) throws JsonProcessingException {
		return mapper.writerFor(Map.class).writeValueAsString(map);
	}

	/* (non-Javadoc)
	 * @see io.mosip.authentication.service.filter.BaseAuthFilter#setTxnId(java.util.Map, java.util.Map)
	 */
	@Override
	protected Map<String, Object> setTxnId(Map<String, Object> requestBody, Map<String, Object> responseBody) {
		Map<String, Object> authReq = (Map<String, Object>) requestBody.get(AUTH_REQUEST);
		responseBody.replace("txnID", authReq.get("txnID"));
		return responseBody;
	}
	
	protected Object decodeToMap(String stringToDecode)
			throws IdAuthenticationAppException {
		try {
			if (stringToDecode != null) {
				return mapper.readValue(
						Base64.getDecoder().decode(stringToDecode),
						new TypeReference<Map<String, Object>>() {
						});
			} else {
				return stringToDecode;
			}
		} catch (IllegalArgumentException | IOException e) {
			throw new IdAuthenticationAppException(
					IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST
							.getErrorCode(),
					IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST
							.getErrorMessage());
		}
	}
	
	@Override
	protected boolean validateSignature(String signature, byte[] requestAsByte) throws IdAuthenticationAppException {
		return true;
	}

}
