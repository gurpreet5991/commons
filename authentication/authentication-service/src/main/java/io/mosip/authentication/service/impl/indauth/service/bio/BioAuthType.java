package io.mosip.authentication.service.impl.indauth.service.bio;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.env.Environment;

import io.mosip.authentication.core.dto.indauth.AuthRequestDTO;
import io.mosip.authentication.core.dto.indauth.BioIdentityInfoDTO;
import io.mosip.authentication.core.dto.indauth.DataDTO;
import io.mosip.authentication.core.spi.faceauth.provider.FaceProvider;
import io.mosip.authentication.core.spi.fingerprintauth.provider.FingerprintProvider;
import io.mosip.authentication.core.spi.indauth.match.AuthType;
import io.mosip.authentication.core.spi.indauth.match.IdInfoFetcher;
import io.mosip.authentication.core.spi.indauth.match.MatchType;
import io.mosip.authentication.core.spi.indauth.match.MatchingStrategyType;
import io.mosip.authentication.core.spi.irisauth.provider.IrisProvider;

/**
 * The Enum BioAuthType.
 *
 * @author Dinesh Karuppiah.T
 */

public enum BioAuthType implements AuthType {

	FGR_MIN("FMR",
			setOf(BioMatchType.FGRMIN_LEFT_THUMB, BioMatchType.FGRMIN_LEFT_INDEX, BioMatchType.FGRMIN_LEFT_MIDDLE,
					BioMatchType.FGRMIN_LEFT_RING, BioMatchType.FGRMIN_LEFT_LITTLE, BioMatchType.FGRMIN_RIGHT_THUMB,
					BioMatchType.FGRMIN_RIGHT_INDEX, BioMatchType.FGRMIN_RIGHT_MIDDLE, BioMatchType.FGRMIN_RIGHT_RING,
					BioMatchType.FGRMIN_RIGHT_LITTLE, BioMatchType.FGRMIN_UNKNOWN),
			getFingerprint(), 1) {

		@Override
		public Map<String, Object> getMatchProperties(AuthRequestDTO authRequestDTO, IdInfoFetcher idInfoFetcher,
				String language) {
			Map<String, Object> valueMap = new HashMap<>();
			authRequestDTO.getRequest().getBiometrics().stream().map(BioIdentityInfoDTO::getData)
					.filter(bioinfo -> bioinfo.getBioType().equals(this.getType())).forEach((DataDTO bioinfovalue) -> {
						if (!bioinfovalue.getBioSubType().equalsIgnoreCase("UNKNOWN")) {
							BiFunction<String, String, Double> func = idInfoFetcher
									.getFingerPrintProvider(bioinfovalue)::matchMinutiae;
							valueMap.put(FingerprintProvider.class.getSimpleName(), func);
							valueMap.put(BioAuthType.class.getSimpleName(), this);
						} else {
							BiFunction<Map<String, String>, Map<String, String>, Double> func = idInfoFetcher
									.getFingerPrintProvider(bioinfovalue)::matchMultiMinutae;
							valueMap.put(FingerprintProvider.class.getSimpleName(), func);
						}
					});
			return valueMap;
		}

		@Override
		protected Long getBioIdentityValuesCount(AuthRequestDTO reqDTO, IdInfoFetcher helper) {
			return BioAuthType.getFPValuesCountInIdentity(reqDTO, helper, BioMatchType.FGRMIN_MULTI);
		}
	},
	FGR_IMG("FIR",
			setOf(BioMatchType.FGRIMG_LEFT_THUMB, BioMatchType.FGRIMG_LEFT_INDEX, BioMatchType.FGRIMG_LEFT_MIDDLE,
					BioMatchType.FGRIMG_LEFT_RING, BioMatchType.FGRIMG_LEFT_LITTLE, BioMatchType.FGRIMG_RIGHT_THUMB,
					BioMatchType.FGRIMG_RIGHT_INDEX, BioMatchType.FGRIMG_RIGHT_MIDDLE, BioMatchType.FGRIMG_RIGHT_RING,
					BioMatchType.FGRIMG_RIGHT_LITTLE),
			getFingerprint(), 1) {

		@Override
		public Map<String, Object> getMatchProperties(AuthRequestDTO authRequestDTO, IdInfoFetcher idInfoFetcher,
				String language) {
			Map<String, Object> valueMap = new HashMap<>();
			authRequestDTO.getRequest().getBiometrics().stream().map(BioIdentityInfoDTO::getData)
					.filter(bioinfo -> bioinfo.getBioType().equals(this.getType())).forEach((DataDTO bioinfovalue) -> {
						BiFunction<String, String, Double> func = idInfoFetcher
								.getFingerPrintProvider(bioinfovalue)::matchImage;
						valueMap.put(FingerprintProvider.class.getSimpleName(), func);
						valueMap.put(BioAuthType.class.getSimpleName(), this);
					});
			return valueMap;
		}

		@Override
		protected Long getBioIdentityValuesCount(AuthRequestDTO reqDTO, IdInfoFetcher helper) {
			return BioAuthType.getFPValuesCountInIdentity(reqDTO, helper, BioMatchType.FGRIMG_MULTI);
		}
	},
	FGR_MIN_MULTI("FMR", setOf(BioMatchType.FGRMIN_MULTI), getFingerprint(), 2) {

		@Override
		public Map<String, Object> getMatchProperties(AuthRequestDTO authRequestDTO, IdInfoFetcher idInfoFetcher,
				String language) {
			Map<String, Object> valueMap = new HashMap<>();
			authRequestDTO.getRequest().getBiometrics().stream().map(BioIdentityInfoDTO::getData)
					.filter(bioinfo -> bioinfo.getBioType().equals(this.getType())).forEach((DataDTO bioinfovalue) -> {
						BiFunction<Map<String, String>, Map<String, String>, Double> func = idInfoFetcher
								.getFingerPrintProvider(bioinfovalue)::matchMultiMinutae;
						valueMap.put(FingerprintProvider.class.getSimpleName(), func);
					});
			return valueMap;
		}

		@Override
		public Optional<Integer> getMatchingThreshold(AuthRequestDTO authReq, String languageInfoFetcher,
				Environment environment) {

			String bioType = getType();
			Integer threshold = null;
			String key = bioType.toLowerCase().concat(MULTI_MIN_MATCH_VALUE_SUFFIX);
			String property = environment.getProperty(key);
			if (property != null && !property.isEmpty()) {
				threshold = Integer.parseInt(property);
			}
			return Optional.ofNullable(threshold);
		}

		@Override
		protected Long getBioIdentityValuesCount(AuthRequestDTO reqDTO, IdInfoFetcher helper) {
			return BioAuthType.getFPValuesCountInIdentity(reqDTO, helper, BioMatchType.FGRMIN_MULTI);
		}
	},
	IRIS_COMP_IMG("IIR", setOf(BioMatchType.IRIS_COMP), "Iris", 2) {

		@Override
		public Map<String, Object> getMatchProperties(AuthRequestDTO authRequestDTO, IdInfoFetcher idInfoFetcher,
				String language) {
			Map<String, Object> valueMap = new HashMap<>();
			authRequestDTO.getRequest().getBiometrics().stream().map(BioIdentityInfoDTO::getData)
					.filter(bioinfo -> bioinfo.getBioType().equals(this.getType())).forEach((DataDTO bioinfovalue) -> {
						BiFunction<Map<String, String>, Map<String, String>, Double> func = idInfoFetcher
								.getIrisProvider(bioinfovalue)::matchMultiImage;// TODO add provider
						valueMap.put(IrisProvider.class.getSimpleName(), func);
					});
			valueMap.put("idvid", idInfoFetcher.getUinOrVid(authRequestDTO).orElse(""));
			return valueMap;
		}

		@Override
		public Optional<Integer> getMatchingThreshold(AuthRequestDTO authReq, String languageInfoFetcher,
				Environment environment) {

			String bioType = getType();
			Integer threshold = null;
			String key = bioType.toLowerCase().concat(MULTI_MIN_MATCH_VALUE_SUFFIX);
			String property = environment.getProperty(key);
			if (property != null && !property.isEmpty()) {
				threshold = Integer.parseInt(property);
			}
			return Optional.ofNullable(threshold);
		}

		@Override
		protected Long getBioIdentityValuesCount(AuthRequestDTO reqDTO, IdInfoFetcher helper) {
			return BioAuthType.getIrisValuesCountInIdentity(reqDTO, helper);
		}

	},
	IRIS_IMG("IIR", setOf(BioMatchType.RIGHT_IRIS, BioMatchType.LEFT_IRIS, BioMatchType.IRIS_UNKNOWN), "Iris", 1) {

		@Override
		public Map<String, Object> getMatchProperties(AuthRequestDTO authRequestDTO, IdInfoFetcher idInfoFetcher,
				String language) {
			Map<String, Object> valueMap = new HashMap<>();
			authRequestDTO.getRequest().getBiometrics().stream().map(BioIdentityInfoDTO::getData)
					.filter(bioinfo -> bioinfo.getBioType().equals(this.getType())).forEach((DataDTO bioinfovalue) -> {
						if (!bioinfovalue.getBioSubType().equalsIgnoreCase("UNKNOWN")) {
							BiFunction<Map<String, String>, Map<String, String>, Double> func = idInfoFetcher
									.getIrisProvider(bioinfovalue)::matchImage;// TODO add provider
							valueMap.put(IrisProvider.class.getSimpleName(), func);
						} else {
							BiFunction<Map<String, String>, Map<String, String>, Double> func = idInfoFetcher
									.getIrisProvider(bioinfovalue)::matchMultiImage;// TODO add provider
							valueMap.put(IrisProvider.class.getSimpleName(), func);
						}
					});
			valueMap.put("idvid", idInfoFetcher.getUinOrVid(authRequestDTO).orElse(""));
			return valueMap;
		}

		@Override
		protected Long getBioIdentityValuesCount(AuthRequestDTO reqDTO, IdInfoFetcher helper) {
			return BioAuthType.getIrisValuesCountInIdentity(reqDTO, helper);
		}
	},
	FACE_IMG("FID", setOf(BioMatchType.FACE, BioMatchType.FACE_UNKNOWN), "face", 1) {

		@Override
		public Map<String, Object> getMatchProperties(AuthRequestDTO authRequestDTO, IdInfoFetcher idInfoFetcher,
				String language) {
			Map<String, Object> valueMap = new HashMap<>();
			authRequestDTO.getRequest().getBiometrics().stream().map(BioIdentityInfoDTO::getData)
					.filter(bioinfo -> bioinfo.getBioType().equals(this.getType())).forEach((DataDTO bioinfovalue) -> {
						BiFunction<Map<String, String>, Map<String, String>, Double> func = idInfoFetcher
								.getFaceProvider(bioinfovalue)::matchImage;
						valueMap.put(FaceProvider.class.getSimpleName(), func);
					});
			valueMap.put("idvid", idInfoFetcher.getUinOrVid(authRequestDTO).orElse(""));
			return valueMap;
		}

		@Override
		protected Long getBioIdentityValuesCount(AuthRequestDTO reqDTO, IdInfoFetcher helper) {
			return BioAuthType.getFaceValuesCountInIdentity(reqDTO, helper, BioMatchType.FACE);
		}
	};

	private static final String MIN_MATCH_VALUE_SUFFIX = ".min.match.value";

	private static final String MULTI_MIN_MATCH_VALUE_SUFFIX = ".multi" + MIN_MATCH_VALUE_SUFFIX;

	private static final String FINGERPRINT = "Fingerprint";

	/** The type. */
	private String type;

	/** The associated match types. */
	private Set<MatchType> associatedMatchTypes;

	/** The display name. */
	private String displayName;

	/** The count. */
	private int count;

	/**
	 * Instantiates a new bio auth type.
	 *
	 * @param type                 the type
	 * @param associatedMatchTypes the associated match types
	 * @param displayName          the display name
	 * @param count                the count
	 */
	private BioAuthType(String type, Set<MatchType> associatedMatchTypes, String displayName, int count) {
		this.type = type;
		this.displayName = displayName;
		this.count = count;
		this.associatedMatchTypes = Collections.unmodifiableSet(associatedMatchTypes);
	}

	protected abstract Long getBioIdentityValuesCount(AuthRequestDTO reqDTO, IdInfoFetcher helper);

	/**
	 * Gets the FP values count in identity.
	 *
	 * @param reqDTO           the req DTO
	 * @param helper           the helper
	 * @param fpMultiMatchType
	 * @return the FP values count in identity
	 */
	private static Long getFPValuesCountInIdentity(AuthRequestDTO reqDTO, IdInfoFetcher helper,
			MatchType fpMultiMatchType) {
		return (long) helper.getIdentityRequestInfo(fpMultiMatchType, reqDTO.getRequest(), null).size();
	}

	private static Long getIrisValuesCountInIdentity(AuthRequestDTO reqDTO, IdInfoFetcher helper) {
		return (long) helper.getIdentityRequestInfo(BioMatchType.IRIS_COMP, reqDTO.getRequest(), null).size();
	}

	private static Long getFaceValuesCountInIdentity(AuthRequestDTO reqDTO, IdInfoFetcher helper,
			MatchType faceMatchType) {
		return (long) helper.getIdentityRequestInfo(faceMatchType, reqDTO.getRequest(), null).size();
	}

	/*
	 * To get Display name
	 */
	@Override
	public String getDisplayName() {
		return displayName;
	}

	/*
	 * To get type
	 */
	@Override
	public String getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.authentication.core.spi.indauth.match.AuthType#isAuthTypeEnabled(io.
	 * mosip.authentication.core.dto.indauth.AuthRequestDTO,
	 * io.mosip.authentication.core.spi.indauth.match.IdInfoFetcher)
	 */
	@Override
	public boolean isAuthTypeEnabled(AuthRequestDTO authReq, IdInfoFetcher helper) {
		return authReq.getRequestedAuth().isBio() && getBioIdentityValuesCount(authReq, helper) == getCount();
	}

	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	private int getCount() {
		return count;
	}

	/**
	 * To Get Matching Strategy
	 */
	@Override
	public Optional<String> getMatchingStrategy(AuthRequestDTO authReq, String languageInfoFetcher) {
		return Optional.of(MatchingStrategyType.PARTIAL.getType());
	}

	/**
	 * Get Matching Threshold
	 */
	@Override
	public Optional<Integer> getMatchingThreshold(AuthRequestDTO authReq, String languageInfoFetcher,
			Environment environment) {

		String bioType = getType();
		Integer threshold = null;
		String key = bioType.toLowerCase().concat(MIN_MATCH_VALUE_SUFFIX);
		String property = environment.getProperty(key);
		if (property != null && !property.isEmpty()) {
			threshold = Integer.parseInt(property);
		}
		return Optional.ofNullable(threshold);
	}

	/*
	 * Get Associated Matchtypes
	 */
	@Override
	public Set<MatchType> getAssociatedMatchTypes() {
		return Collections.unmodifiableSet(associatedMatchTypes);
	}

	/*
	 * Checks is Authtype information available based on authreqest
	 */
	@Override
	public boolean isAuthTypeInfoAvailable(AuthRequestDTO authRequestDTO) {
		return Optional.ofNullable(authRequestDTO.getRequest().getBiometrics())
				.flatMap(list -> list.stream().map(BioIdentityInfoDTO::getData)
						.filter(bioInfo -> bioInfo.getBioType().equalsIgnoreCase(getType())).findAny())
				.isPresent();
	}

	public static String getFingerprint() {
		return FINGERPRINT;
	}

	/**
	 * Returns the set of given match types
	 *
	 * @param supportedMatchTypes the supported match types
	 * @return the sets the
	 */
	public static Set<MatchType> setOf(MatchType... supportedMatchTypes) {
		return Stream.of(supportedMatchTypes).collect(Collectors.toSet());
	}

	/**
	 * This method accepts the bioType and it will return Optional of BioAuthType
	 * only when the count is single.
	 * 
	 * @param type
	 * @return
	 */
	public static Optional<BioAuthType> getSingleBioAuthTypeForType(String type) {
		BioAuthType[] values = BioAuthType.values();
		return Stream.of(values)
				.filter(authType -> authType.getType().equalsIgnoreCase(type) && authType.getCount() == 1).findAny();
	}
}
