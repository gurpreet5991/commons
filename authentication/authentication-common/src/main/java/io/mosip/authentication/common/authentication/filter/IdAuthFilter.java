package io.mosip.authentication.common.authentication.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.authentication.common.policy.AuthPolicy;
import io.mosip.authentication.common.policy.KYCAttributes;
import io.mosip.authentication.common.policy.Policies;
import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.dto.indauth.AuthTypeDTO;
import io.mosip.authentication.core.dto.indauth.BioIdentityInfoDTO;
import io.mosip.authentication.core.exception.IdAuthenticationAppException;
import io.mosip.authentication.core.spi.indauth.match.MatchType;
//import io.mosip.authentication.service.impl.indauth.controller.AuthController;
import io.mosip.kernel.core.util.DateUtils;

/**
 * The Class IdAuthFilter - the implementation for deciphering and validation of
 * the authenticating partner done for request as AUTH and KYC
 * {@link AuthController}
 *
 * @author Manoj SP
 * @author Sanjay Murali
 */
@Component
public class IdAuthFilter extends BaseAuthFilter {

	/** The Constant UTF_8. */
	private static final String UTF_8 = "UTF-8";

	/** The Constant REQUEST_HMAC. */
	private static final String REQUEST_HMAC = "requestHMAC";

	/** The Constant SECRET_KEY. */
	private static final String SECRET_KEY = "secretKey";

	/** The Constant MISP_PARTNER_MAPPING. */
	private static final String MISP_PARTNER_MAPPING = "misp.partner.mapping.";

	/** The Constant PARTNER_KEY. */
	private static final String PARTNER_KEY = "partner.";

	/** The Constant LICENSE_KEY. */
	private static final String LICENSE_KEY = "licenseKey.";

	/** The Constant MISPLICENSE_KEY. */
	private static final String MISPLICENSE_KEY = "misplicenseKey";

	/** The Constant PARTNER_ID. */
	private static final String PARTNER_ID = "partnerId";

	/** The Constant MISP_ID. */
	private static final String MISP_ID = "mispId";

	/** The Constant POLICY_ID. */
	private static final String POLICY_ID = "policyId";

	/** The Constant ACTIVE_STATUS. */
	private static final String ACTIVE_STATUS = "active";

	/** The Constant EXPIRY_DT. */
	private static final String EXPIRY_DT = "expiryDt";

	/** The Constant STATUS. */
	private static final String STATUS = "status";

	/** The Constant REQUEST. */
	private static final String REQUEST = "request";

	/** The Constant KYC. */
	private static final String KYC = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.authentication.service.filter.BaseAuthFilter#decodedRequest(java.
	 * util.Map)
	 */
	@Override
	protected Map<String, Object> decipherRequest(Map<String, Object> requestBody) throws IdAuthenticationAppException {
		try {
			requestBody.replace(REQUEST, decode((String) requestBody.get(REQUEST)));
			requestBody.replace(REQUEST_HMAC, decode((String) requestBody.get(REQUEST_HMAC)));
			if (null != requestBody.get(REQUEST)) {
				Map<String, Object> request = keyManager.requestData(requestBody, mapper);
				if (null != request.get(SECRET_KEY)) {
					SecretKey secretKey = (SecretKey) request.get(SECRET_KEY);
					byte[] reqHMAC = keyManager.symmetricDecrypt(secretKey, (byte[]) requestBody.get(REQUEST_HMAC));
					request.remove(SECRET_KEY);
					validateRequestHMAC(new String(reqHMAC, StandardCharsets.UTF_8),
							mapper.writeValueAsString(request));

				}
				requestBody.replace(REQUEST, request);
			}
			return requestBody;
		} catch (ClassCastException | JsonProcessingException e) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS.getErrorCode(),
					IdAuthenticationErrorConstants.UNABLE_TO_PROCESS.getErrorMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.authentication.service.filter.BaseAuthFilter#
	 * validateDecipheredRequest(io.mosip.authentication.service.filter.
	 * ResettableStreamHttpServletRequest, java.util.Map)
	 */
	@Override
	protected void validateDecipheredRequest(ResettableStreamHttpServletRequest requestWrapper,
			Map<String, Object> requestBody) throws IdAuthenticationAppException {
		Map<String, String> partnerLkMap = getAuthPart(requestWrapper);
		String partnerId = partnerLkMap.get(PARTNER_ID);
		String licenseKey = partnerLkMap.get(MISPLICENSE_KEY);
		String mispId = licenseKeyMISPMapping(licenseKey);
		validPartnerId(partnerId);
		String policyId = validMISPPartnerMapping(partnerId, mispId);
		checkAllowedAuthTypeBasedOnPolicy(policyId, requestBody);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.authentication.service.filter.BaseAuthFilter#validateSignature(java.
	 * lang.String, byte[])
	 */
	@Override
	protected boolean validateSignature(String signature, byte[] requestAsByte) throws IdAuthenticationAppException {
		return true;
	}

	/**
	 * License key MISP mapping is associated with this method.It checks for the
	 * license key expiry and staus.
	 *
	 * @param licenseKey the license key
	 * @return the string
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	@SuppressWarnings("unchecked")
	private String licenseKeyMISPMapping(String licenseKey) throws IdAuthenticationAppException {
		String mispId = null;
		String licensekeyMappingJson = env.getProperty(LICENSE_KEY + licenseKey);
		if (Objects.nonNull(licensekeyMappingJson)) {
			Map<String, Object> licenseKeyMap = null;
			try {
				licenseKeyMap = mapper.readValue(licensekeyMappingJson.getBytes(UTF_8), HashMap.class);
				mispId = (String) licenseKeyMap.get(MISP_ID);
			} catch (IOException e) {
				throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS);
			}
			String lkExpiryDt = (String) licenseKeyMap.get(EXPIRY_DT);
			if (DateUtils.convertUTCToLocalDateTime(lkExpiryDt).isBefore(DateUtils.getUTCCurrentDateTime())) {
				throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.LICENSEKEY_EXPIRED);
			}
			String lkStatus = (String) licenseKeyMap.get(STATUS);
			if (!lkStatus.equalsIgnoreCase(ACTIVE_STATUS)) {
				throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.LICENSEKEY_SUSPENDED);
			}
		} else {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.INVALID_LICENSEKEY);
		}
		return mispId;
	}

	/**
	 * this method checks whether partner id is valid.
	 *
	 * @param partnerId the partner id
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	@SuppressWarnings("unchecked")
	private void validPartnerId(String partnerId) throws IdAuthenticationAppException {
		String partnerIdJson = env.getProperty(PARTNER_KEY + partnerId);
		if (null == partnerIdJson) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.PARTNER_NOT_REGISTERED);
		} else {
			Map<String, String> partnerIdMap = null;
			try {
				partnerIdMap = mapper.readValue(partnerIdJson.getBytes(UTF_8), Map.class);
			} catch (IOException e) {
				throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS);
			}
			String policyId = partnerIdMap.get(POLICY_ID);
			if (null == policyId || policyId.equalsIgnoreCase("")) {
				throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.PARTNER_POLICY_NOTMAPPED);
			}
			String partnerStatus = partnerIdMap.get(STATUS);
			if (!partnerStatus.equalsIgnoreCase(ACTIVE_STATUS)) {
				throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.PARTNER_DEACTIVATED);
			}
		}
	}

	/**
	 * Validates MISP partner mapping,if its valid it returns the policyId.
	 *
	 * @param partnerId the partner id
	 * @param mispId    the misp id
	 * @return the string
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	@SuppressWarnings("unchecked")
	private String validMISPPartnerMapping(String partnerId, String mispId) throws IdAuthenticationAppException {
		Map<String, String> partnerIdMap = null;
		String policyId = null;
		Boolean mispPartnerMappingJson = env.getProperty(MISP_PARTNER_MAPPING + mispId + "." + partnerId,
				boolean.class);
		if (null == mispPartnerMappingJson || !mispPartnerMappingJson) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.PARTNER_NOT_MAPPED);
		}
		String partnerIdJson = env.getProperty(PARTNER_KEY + partnerId);
		try {
			partnerIdMap = mapper.readValue(partnerIdJson.getBytes(UTF_8), Map.class);
			policyId = partnerIdMap.get(POLICY_ID);
		} catch (IOException e) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS);
		}
		return policyId;
	}

	/**
	 * Check allowed auth type based on policy.
	 *
	 * @param policyId    the policy id
	 * @param requestBody the request body
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	protected void checkAllowedAuthTypeBasedOnPolicy(String policyId, Map<String, Object> requestBody)
			throws IdAuthenticationAppException {
		try {
			String policyJson = getPolicy(policyId);
			Policies policies = null;
			policies = mapper.readValue(policyJson.getBytes(UTF_8), Policies.class);
			List<AuthPolicy> authPolicies = policies.getPolicies().getAuthPolicies();
			List<KYCAttributes> allowedKycAttributes = policies.getPolicies().getAllowedKycAttributes();
			List<String> allowedTypeList = allowedKycAttributes.stream().filter(KYCAttributes::isRequired)
					.map(KYCAttributes::getAttributeName).collect(Collectors.toList());
			if (allowedTypeList == null) {
				allowedTypeList = Collections.emptyList();
			}
			requestBody.put("allowedKycAttributes", allowedTypeList);
			checkAllowedAuthTypeBasedOnPolicy(requestBody, authPolicies);
			List<AuthPolicy> mandatoryAuthPolicies = authPolicies.stream().filter(AuthPolicy::isMandatory)
					.collect(Collectors.toList());
			checkMandatoryAuthTypeBasedOnPolicy(requestBody, mandatoryAuthPolicies);
		} catch (IOException e) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		}
	}

	/**
	 * Check allowed auth type based on policy.
	 *
	 * @param requestBody  the request body
	 * @param authPolicies the auth policies
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	protected void checkAllowedAuthTypeBasedOnPolicy(Map<String, Object> requestBody, List<AuthPolicy> authPolicies)
			throws IdAuthenticationAppException {
		try {
			AuthTypeDTO authType = mapper.readValue(mapper.writeValueAsBytes(requestBody.get("requestedAuth")),
					AuthTypeDTO.class);
			if (authType.isDemo() && !isAllowedAuthType(MatchType.Category.DEMO.getType(), authPolicies)) {
				throw new IdAuthenticationAppException(
						IdAuthenticationErrorConstants.AUTHTYPE_NOT_ALLOWED.getErrorCode(),
						String.format(IdAuthenticationErrorConstants.AUTHTYPE_NOT_ALLOWED.getErrorMessage(),
								MatchType.Category.DEMO.name()));
			}

			if (authType.isBio()) {
				checkAllowedAuthTypeForBio(requestBody, authPolicies);
			}

			if (authType.isPin() && !isAllowedAuthType(MatchType.Category.SPIN.getType(), authPolicies)) {
				throw new IdAuthenticationAppException(
						IdAuthenticationErrorConstants.AUTHTYPE_NOT_ALLOWED.getErrorCode(),
						String.format(IdAuthenticationErrorConstants.AUTHTYPE_NOT_ALLOWED.getErrorMessage(),
								MatchType.Category.SPIN.name()));
			}
			if (authType.isOtp() && !isAllowedAuthType(MatchType.Category.OTP.getType(), authPolicies)) {
				throw new IdAuthenticationAppException(
						IdAuthenticationErrorConstants.AUTHTYPE_NOT_ALLOWED.getErrorCode(),
						String.format(IdAuthenticationErrorConstants.AUTHTYPE_NOT_ALLOWED.getErrorMessage(),
								MatchType.Category.OTP.name()));
			}
		} catch (IOException e) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		}
	}

	/**
	 * Check allowed auth type for bio.
	 *
	 * @param requestBody  the request body
	 * @param authPolicies the auth policies
	 * @throws IdAuthenticationAppException the id authentication app exception
	 * @throws IOException                  Signals that an I/O exception has
	 *                                      occurred.
	 * @throws JsonParseException           the json parse exception
	 * @throws JsonMappingException         the json mapping exception
	 * @throws JsonProcessingException      the json processing exception
	 */
	@SuppressWarnings("unchecked")
	private void checkAllowedAuthTypeForBio(Map<String, Object> requestBody, List<AuthPolicy> authPolicies)
			throws IdAuthenticationAppException, IOException {

		Object value = Optional.ofNullable(requestBody.get(REQUEST)).filter(obj -> obj instanceof Map)
				.map(obj -> ((Map<String, Object>) obj).get("biometrics")).filter(obj -> obj instanceof List)
				.orElse(Collections.emptyList());
		List<BioIdentityInfoDTO> listBioInfo = mapper.readValue(mapper.writeValueAsBytes(value),
				new TypeReference<List<BioIdentityInfoDTO>>() {
				});

		List<String> bioTypeList = listBioInfo.stream()
				.filter(s -> s.getData() != null && s.getData().getBioType() != null).map(s -> s.getData().getBioType())
				.collect(Collectors.toList());
		if (bioTypeList.isEmpty()) {
			if (!isAllowedAuthType(MatchType.Category.BIO.getType(), authPolicies)) {
				throw new IdAuthenticationAppException(
						IdAuthenticationErrorConstants.AUTHTYPE_NOT_ALLOWED.getErrorCode(),
						String.format(IdAuthenticationErrorConstants.AUTHTYPE_NOT_ALLOWED.getErrorMessage(), "bio"));
			}
		} else {
			for (String bioType : bioTypeList) {
				if (bioType.equalsIgnoreCase("FIR") || bioType.equalsIgnoreCase("FMR")) {
					bioType = "Finger";
				} else if (bioType.equalsIgnoreCase("FID")) {
					bioType = "Face";
				} else if (bioType.equalsIgnoreCase("IIR")) {
					bioType = "Iris";
				}
				if (!isAllowedAuthType(MatchType.Category.BIO.getType(), bioType, authPolicies)) {
					String bioSubtype = MatchType.Category.BIO.name() + "-" + bioType;
					throw new IdAuthenticationAppException(
							IdAuthenticationErrorConstants.AUTHTYPE_NOT_ALLOWED.getErrorCode(), String.format(
									IdAuthenticationErrorConstants.AUTHTYPE_NOT_ALLOWED.getErrorMessage(), bioSubtype));
				}
			}
		}

	}

	/**
	 * Check mandatory auth type based on policy.
	 *
	 * @param requestBody           the request body
	 * @param mandatoryAuthPolicies the mandatory auth policies
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	@SuppressWarnings("unchecked")
	protected void checkMandatoryAuthTypeBasedOnPolicy(Map<String, Object> requestBody,
			List<AuthPolicy> mandatoryAuthPolicies) throws IdAuthenticationAppException {
		try {
			AuthTypeDTO authType = mapper.readValue(mapper.writeValueAsBytes(requestBody.get("requestedAuth")),
					AuthTypeDTO.class);
			Object value = Optional.ofNullable(requestBody.get(REQUEST)).filter(obj -> obj instanceof Map)
					.map(obj -> ((Map<String, Object>) obj).get("biometrics")).filter(obj -> obj instanceof List)
					.orElse(Collections.emptyList());
			List<BioIdentityInfoDTO> listBioInfo = mapper.readValue(mapper.writeValueAsBytes(value),
					new TypeReference<List<BioIdentityInfoDTO>>() {
					});
			List<String> bioTypeList = listBioInfo.stream().map(s -> s.getData().getBioType())
					.collect(Collectors.toList());
			if (bioTypeList.contains("FMR") || bioTypeList.contains("FIR")) {
				bioTypeList.add("FINGER");
			}
			for (AuthPolicy mandatoryAuthPolicy : mandatoryAuthPolicies) {
				validateAuthPolicy(requestBody, authType, bioTypeList, mandatoryAuthPolicy);
			}
		} catch (IOException e) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		}
	}

	/**
	 * Validate auth policy.
	 *
	 * @param requestBody         the request body
	 * @param authType            the auth type
	 * @param bioTypeList         the bio type list
	 * @param mandatoryAuthPolicy the mandatory auth policy
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	private void validateAuthPolicy(Map<String, Object> requestBody, AuthTypeDTO authType, List<String> bioTypeList,
			AuthPolicy mandatoryAuthPolicy) throws IdAuthenticationAppException {
		if (mandatoryAuthPolicy.getAuthType().equalsIgnoreCase(MatchType.Category.OTP.getType()) && !authType.isOtp()) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorCode(),
					String.format(IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorMessage(),
							MatchType.Category.OTP.getType()));
		} else if (mandatoryAuthPolicy.getAuthType().equalsIgnoreCase(MatchType.Category.DEMO.getType())
				&& !authType.isDemo()) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorCode(),
					String.format(IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorMessage(),
							MatchType.Category.DEMO.getType()));
		} else if (mandatoryAuthPolicy.getAuthType().equalsIgnoreCase(MatchType.Category.SPIN.getType())
				&& !authType.isPin()) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorCode(),
					String.format(IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorMessage(),
							MatchType.Category.SPIN.getType()));
		} else if (mandatoryAuthPolicy.getAuthType().equalsIgnoreCase(MatchType.Category.BIO.getType())) {
			if (!authType.isBio()) {
				throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorCode(),
						String.format(IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorMessage(),
								MatchType.Category.BIO.getType()));
			} else {
				if (!bioTypeList.contains(mandatoryAuthPolicy.getAuthSubType())) {
					throw new IdAuthenticationAppException(
							IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorCode(),
							String.format(IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorMessage(),
									MatchType.Category.BIO.getType() + "-" + mandatoryAuthPolicy.getAuthSubType()));
				}
			}
		} else if (mandatoryAuthPolicy.getAuthType().equalsIgnoreCase(KYC)
				&& !Optional.ofNullable(requestBody.get("id"))
						.filter(id -> id.equals(env.getProperty("mosip.ida.api.ids.ekyc"))).isPresent()) {
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorCode(),
					String.format(IdAuthenticationErrorConstants.AUTHTYPE_MANDATORY.getErrorMessage(), KYC));
		}
	}

	/**
	 * Checks if is allowed auth type.
	 *
	 * @param authType the auth type
	 * @param policies the policies
	 * @return true, if is allowed auth type
	 */
	protected boolean isAllowedAuthType(String authType, List<AuthPolicy> policies) {
		return isAllowedAuthType(authType, null, policies);
	}

	/**
	 * Checks if is allowed auth type.
	 *
	 * @param authType    the auth type
	 * @param subAuthType the sub auth type
	 * @param policies    the policies
	 * @return true, if is allowed auth type
	 */
	protected boolean isAllowedAuthType(String authType, String subAuthType, List<AuthPolicy> policies) {
		if (subAuthType == null) {
			return policies.stream().anyMatch(authPolicy -> authPolicy.getAuthType().equalsIgnoreCase(authType));
		} else {
			return policies.stream().anyMatch(authPolicy -> authPolicy.getAuthType().equalsIgnoreCase(authType)
					&& authPolicy.getAuthSubType().equalsIgnoreCase(subAuthType));
		}
	}

	/**
	 * Gets the policy.
	 *
	 * @param policyId the policy id
	 * @return the policy
	 */
	private String getPolicy(String policyId) {
		return env.getProperty("policy." + policyId);
	}

	/**
	 * Gets the auth part.
	 *
	 * @param requestWrapper the request wrapper
	 * @return the auth part
	 */
	protected Map<String, String> getAuthPart(ResettableStreamHttpServletRequest requestWrapper) {
		Map<String, String> params = new HashMap<>();
		if (requestWrapper instanceof HttpServletRequestWrapper) {
			String url = requestWrapper.getRequestURL().toString();
			String contextPath = requestWrapper.getContextPath();
			if ((Objects.nonNull(url) && !url.isEmpty()) && (Objects.nonNull(contextPath) && !contextPath.isEmpty())) {
				String[] splitedUrlByContext = url.split(contextPath);
				String[] paramsArray = Stream.of(splitedUrlByContext[1].split("/")).filter(str -> !str.isEmpty())
						.toArray(size -> new String[size]);

				params.put(PARTNER_ID, paramsArray[1]);
				params.put(MISPLICENSE_KEY, paramsArray[2]);
			}
		}
		return params;
	}

}
