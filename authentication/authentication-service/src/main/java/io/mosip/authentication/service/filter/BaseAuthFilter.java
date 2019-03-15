package io.mosip.authentication.service.filter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.exception.IdAuthenticationAppException;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.service.integration.KeyManager;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.kernel.crypto.jce.impl.EncryptorImpl;

/**
 * The Class BaseAuthFilter - The Base Auth Filter that does all necessary
 * authentication/authorization before allowing the request to the respective
 * controllers.
 * 
 * @author Manoj SP
 * @author Sanjay Murali
 */
@Component
public abstract class BaseAuthFilter extends BaseIDAFilter {

	/** The Constant BASE_AUTH_FILTER. */
	private static final String BASE_AUTH_FILTER = "BaseAuthFilter";

	/** The Constant EVENT_FILTER. */
	private static final String EVENT_FILTER = "Event_filter";

	/** The Constant SESSION_ID. */
	private static final String SESSION_ID = "SessionId";

	/** The mosip logger. */
	private static Logger mosipLogger = IdaLogger.getLogger(BaseAuthFilter.class);

	/** The Constant MOSIP_TSP_ORGANIZATION. */
	private static final String MOSIP_TSP_ORGANIZATION = "mosip.jws.certificate.organization";

	/** The Constant MOSIP_JWS_CERTIFICATE_ALGO. */
	private static final String MOSIP_JWS_CERTIFICATE_ALGM = "mosip.jws.certificate.algo";

	/** The public key. */
	protected PublicKey publicKey;

	/** The encryptor. */
	protected EncryptorImpl encryptor;

	/** The key manager. */
	protected KeyManager keyManager;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		WebApplicationContext context = WebApplicationContextUtils
				.getRequiredWebApplicationContext(filterConfig.getServletContext());
		encryptor = context.getBean(EncryptorImpl.class);
		keyManager = context.getBean(KeyManager.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.authentication.service.filter.BaseIDAFilter#consumeRequest(io.mosip.
	 * authentication.service.filter.ResettableStreamHttpServletRequest)
	 */
	@Override
	protected void consumeRequest(ResettableStreamHttpServletRequest requestWrapper)
			throws IdAuthenticationAppException {
		super.consumeRequest(requestWrapper);
		authenticateRequest(requestWrapper);

		try {
			requestWrapper.resetInputStream();
			Map<String, Object> requestBody = getRequestBody(requestWrapper.getInputStream());
			Map<String, Object> decipherRequest = decipherRequest(requestBody);
			validateDecipheredRequest(requestWrapper, decipherRequest);
			String requestAsString = mapper.writeValueAsString(decipherRequest);
			mosipLogger.info(SESSION_ID, EVENT_FILTER, BASE_AUTH_FILTER, "Input Request: \n" + requestAsString);
			requestWrapper.replaceData(requestAsString.getBytes());
		} catch (IOException e) {
			mosipLogger.error(SESSION_ID, EVENT_FILTER, BASE_AUTH_FILTER, e.getMessage());
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS);
		}

	}

	protected abstract void validateDecipheredRequest(ResettableStreamHttpServletRequest requestWrapper,
			Map<String, Object> decipherRequest) throws IdAuthenticationAppException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.authentication.service.filter.BaseIDAFilter#authenticateRequest(io.
	 * mosip.authentication.service.filter.ResettableStreamHttpServletRequest)
	 */
	@Override
	protected void authenticateRequest(ResettableStreamHttpServletRequest requestWrapper)
			throws IdAuthenticationAppException {
		String signature = requestWrapper.getHeader("Authorization");// FIXME header name
		try {
			requestWrapper.resetInputStream();
			if (!validateSignature(signature, IOUtils.toByteArray(requestWrapper.getInputStream()))) {
				mosipLogger.error(SESSION_ID, EVENT_FILTER, BASE_AUTH_FILTER, "Invalid Signature");
				throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.DSIGN_FALIED);
			}

		} catch (IOException e) {
			mosipLogger.error(SESSION_ID, EVENT_FILTER, BASE_AUTH_FILTER, e.getMessage());
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.DSIGN_FALIED, e);
		}
	}

	/**
	 * Validate signature.
	 *
	 * @param signature     the signature
	 * @param requestAsByte the request as byte
	 * @return true, if successful
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	protected boolean validateSignature(String signature, byte[] requestAsByte) throws IdAuthenticationAppException {
		boolean isSigned = false;
		JsonWebSignature jws = new JsonWebSignature();
		try {
			jws.setCompactSerialization(signature);
			List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
			if (certificateChainHeaderValue.size() == NumberUtils.INTEGER_ONE
					&& jws.getAlgorithmHeaderValue().equals(env.getProperty(MOSIP_JWS_CERTIFICATE_ALGM))) {
				X509Certificate certificate = certificateChainHeaderValue.get(0);
				certificate.checkValidity();
				publicKey = certificate.getPublicKey();
				certificate.verify(publicKey);
				jws.setKey(publicKey);
				isSigned = checkValidSign(requestAsByte, isSigned, certificate, jws);
			} else {
				mosipLogger.error(SESSION_ID, EVENT_FILTER, BASE_AUTH_FILTER, "certificate not present");
				throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.INVALID_CERTIFICATE);
			}
		} catch (JoseException | InvalidKeyException | CertificateException | NoSuchAlgorithmException
				| NoSuchProviderException | SignatureException e) {
			mosipLogger.error(SESSION_ID, EVENT_FILTER, BASE_AUTH_FILTER, "Invalid certificate");
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.INVALID_CERTIFICATE, e);
		}
		return isSigned;
	}

	/**
	 * Check valid sign.
	 *
	 * @param requestAsByte the request as byte
	 * @param isSigned      the is signed
	 * @param certificate   the certificate
	 * @param jws           the jws
	 * @return true, if successful
	 * @throws JoseException the jose exception
	 */
	private boolean checkValidSign(byte[] requestAsByte, boolean isSigned, X509Certificate certificate,
			JsonWebSignature jws) throws JoseException {
		if (jws.verifySignature() && validateOrg(certificate) && jws.getPayload()
				.equalsIgnoreCase(HMACUtils.digestAsPlainText(HMACUtils.generateHash((requestAsByte))))) {
			isSigned = true;
		}
		return isSigned;
	}

	/**
	 * Validate org.
	 *
	 * @param certNew the cert new
	 * @return true, if successful
	 */
	private boolean validateOrg(X509Certificate certNew) {
		String[] subject = certNew.getSubjectDN().getName().split(",");
		return Stream.of(subject).map(s -> s.split("=")).filter(ar -> ar.length == 2)
				.filter(ar -> ar[0].trim().equals("O"))
				.anyMatch(ar -> ar[1].trim().equals(env.getProperty(MOSIP_TSP_ORGANIZATION)));
	}


	/**
	 * Decode.
	 *
	 * @param stringToDecode the string to decode
	 * @return the object
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	protected static Object decode(String stringToDecode) throws IdAuthenticationAppException {
		try {
			if (Objects.nonNull(stringToDecode)) {
				return CryptoUtil.decodeBase64(stringToDecode);
			} else {
				return stringToDecode;
			}
		} catch (IllegalArgumentException e) {
			mosipLogger.error(SESSION_ID, EVENT_FILTER, BASE_AUTH_FILTER, e.getMessage());
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.DSIGN_FALIED, e);
		}
	}

	/**
	 * Encode.
	 *
	 * @param stringToEncode the string to encode
	 * @return the string
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	protected static String encode(String stringToEncode) throws IdAuthenticationAppException {
		try {
			if (Objects.nonNull(stringToEncode)) {
				return CryptoUtil.encodeBase64String(stringToEncode.getBytes());
			} else {
				return stringToEncode;
			}
		} catch (IllegalArgumentException e) {
			mosipLogger.error(SESSION_ID, EVENT_FILTER, BASE_AUTH_FILTER, e.getMessage());
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.DSIGN_FALIED, e);
		}
	}

	/**
	 * Decipher request.
	 *
	 * @param requestBody the request body
	 * @return the map
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	protected Map<String, Object> decipherRequest(Map<String, Object> requestBody) throws IdAuthenticationAppException {
		return requestBody;
	}

	/**
	 * Encipher response.
	 *
	 * @param responseBody the response body
	 * @return the map
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	protected Map<String, Object> encipherResponse(Map<String, Object> responseBody)
			throws IdAuthenticationAppException {
		return responseBody;
	}

	/* (non-Javadoc)
	 * @see io.mosip.authentication.service.filter.BaseIDAFilter#transformResponse(java.util.Map)
	 */
	@Override
	protected Map<String, Object> transformResponse(Map<String, Object> responseMap)
			throws IdAuthenticationAppException {
		return encipherResponse(responseMap);
	}
	
	protected void validateRequestHMAC(String requestHMAC, String generatedHMAC) throws IdAuthenticationAppException {
		if(!requestHMAC.equals(HMACUtils.digestAsPlainText(HMACUtils.generateHash(generatedHMAC.getBytes())))) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.HMAC_VALIDATION_FAILED);
		}
	}
}
