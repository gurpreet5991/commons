package io.mosip.authentication.core.spi.fingerprintauth.provider;

import java.util.Base64;
import java.util.Map;

import com.google.gson.JsonSyntaxException;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;

/**
 * The Class FingerprintProvider - An Abstract class which contains default
 * implementation for calculating score based on ISO Template and Fingerprint
 * minutiae in Json format and also provides support for adding new fingerprint
 * providers.
 *
 * @author Manoj SP
 */
public abstract class FingerprintProvider implements MosipFingerprintProvider {

	/** The Constant UNKNOWN. */
	private static final String UNKNOWN = "UNKNOWN";

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.authentication.core.spi.fingerprintauth.provider.
	 * MosipFingerprintProvider#scoreCalculator(byte[], byte[])
	 */
	@Override
	public double matchScoreCalculator(byte[] isoImage1, byte[] isoImage2) {
		try {
			FingerprintTemplate template1 = new FingerprintTemplate().convert(isoImage1);
			FingerprintTemplate template2 = new FingerprintTemplate().convert(isoImage2);
			FingerprintMatcher matcher = new FingerprintMatcher();
			System.err.println("Threshold Value >>>" + matcher.index(template1).match(template2));
			return matcher.index(template1).match(template2);
		} catch (IllegalArgumentException e) {
			throw e;
			// TODO need to create and add exception
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.authentication.core.spi.fingerprintauth.provider.
	 * MosipFingerprintProvider#scoreCalculator(java.lang.String, java.lang.String)
	 */
	@Override
	public double matchScoreCalculator(String fingerImage1, String fingerImage2) {
		try {
			FingerprintTemplate template1 = new FingerprintTemplate().deserialize(fingerImage1);
			FingerprintTemplate template2 = new FingerprintTemplate().deserialize(fingerImage2);
			FingerprintMatcher matcher = new FingerprintMatcher();
			System.err.println("Threshold Value >>>" + matcher.index(template1).match(template2));
			return matcher.index(template1).match(template2);
		} catch (IllegalArgumentException | JsonSyntaxException e) {
			throw e;
			// TODO need to create and add exception
		}
	}

	/**
	 * Match minutiae.
	 *
	 * @param reqInfo
	 *            the req info
	 * @param entityInfo
	 *            the entity info
	 * @return the double
	 */
	public double matchMinutiae(Object reqInfo, Object entityInfo) {
		if (reqInfo instanceof String && entityInfo instanceof String) {
			String reqInfoStr = (String) reqInfo;
			String entityInfoStr = (String) entityInfo;
			byte[] decodedrefInfo = decodeValue(reqInfoStr);
			byte[] decodeEntityInfo = decodeValue(entityInfoStr);
			FingerprintTemplate template1 = new FingerprintTemplate().convert(decodedrefInfo);
			FingerprintTemplate template2 = new FingerprintTemplate().convert(decodeEntityInfo);
			return this.matchScoreCalculator(template1.serialize(), template2.serialize());

		}
		return 0;
	}

	/**
	 * Match image.
	 *
	 * @param reqInfo
	 *            the req info
	 * @param entityInfo
	 *            the entity info
	 * @return the double
	 */
	public double matchImage(Object reqInfo, Object entityInfo) {
		if (reqInfo instanceof String && entityInfo instanceof String) {

			byte[] decodedrefInfo = decodeValue((String) reqInfo);
			byte[] decodeEntityInfo = decodeValue((String) entityInfo);
			return this.matchScoreCalculator(decodedrefInfo, decodeEntityInfo);
		}
		return 0;
	}

	/**
	 * Decode value.
	 *
	 * @param value
	 *            the value
	 * @return the byte[]
	 */
	static byte[] decodeValue(String value) {
		return Base64.getDecoder().decode(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.authentication.core.spi.bioauth.provider.MosipBiometricProvider#
	 * matchMultiMinutae(java.util.Map, java.util.Map)
	 */
	public double matchMultiMinutae(Map<String, String> reqInfo, Map<String, String> entityInfo) {
		if (reqInfo.keySet().stream().noneMatch(key -> key.startsWith(UNKNOWN))) {
			double matchScore = 0;
			for (Map.Entry<String, String> e : reqInfo.entrySet()) {
				String key = e.getKey();
				String value1 = e.getValue();
				String value2 = entityInfo.get(key);
				matchScore += matchMinutiae(value1, value2);
			} 
			return matchScore;
		} else {
			double maxMatchScore = 0;
			double individualScore=0;
			double matchScore = 0;
			for (Map.Entry<String, String> reqInfoEntry : reqInfo.entrySet()) {
				for (Map.Entry<String, String> e : entityInfo.entrySet()) {
					String value1 = e.getValue();
					String value2 = reqInfoEntry.getValue();
					individualScore = matchMinutiae(value1, value2);
					if (individualScore > matchScore) {
						matchScore = individualScore;
					}
				}
				maxMatchScore += matchScore;
			}
			return maxMatchScore;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.authentication.core.spi.bioauth.provider.MosipBiometricProvider#
	 * matchMultiImage(java.util.Map, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public double matchMultiImage(Object reqInfo, Object entityInfo) {
		double matchScore = 0;
		if (reqInfo instanceof Map && entityInfo instanceof Map) {

			Map<String, String> reqInfoMap = (Map<String, String>) reqInfo;
			Map<String, String> entityInfoMap = (Map<String, String>) entityInfo;
			for (Map.Entry<String, String> e : reqInfoMap.entrySet()) {
				String key = e.getKey();
				String value1 = e.getValue();
				String value2 = entityInfoMap.get(key);
				matchScore += matchImage(value1, value2);
			}
			return matchScore;
		}
		return matchScore;
	}

}
