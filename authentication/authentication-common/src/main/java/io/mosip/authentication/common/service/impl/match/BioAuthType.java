package io.mosip.authentication.common.service.impl.match;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

import org.springframework.core.env.Environment;

import io.mosip.authentication.common.service.impl.AuthTypeImpl;
import io.mosip.authentication.core.indauth.dto.AuthRequestDTO;
import io.mosip.authentication.core.indauth.dto.BioIdentityInfoDTO;
import io.mosip.authentication.core.indauth.dto.DataDTO;
import io.mosip.authentication.core.spi.indauth.match.AuthType;
import io.mosip.authentication.core.spi.indauth.match.IdInfoFetcher;
import io.mosip.authentication.core.spi.indauth.match.MatchType;
import io.mosip.authentication.core.spi.indauth.match.MatchingStrategyType;
import io.mosip.authentication.core.spi.provider.bio.FaceProvider;
import io.mosip.authentication.core.spi.provider.bio.FingerprintProvider;
import io.mosip.authentication.core.spi.provider.bio.IrisProvider;

/**
 * The Enum BioAuthType.
 *
 * @author Dinesh Karuppiah.T
 */

public enum BioAuthType implements AuthType {

	FGR_MIN("FMR",
			AuthType.setOf(BioMatchType.FGRMIN_LEFT_THUMB, BioMatchType.FGRMIN_LEFT_INDEX,
					BioMatchType.FGRMIN_LEFT_MIDDLE, BioMatchType.FGRMIN_LEFT_RING, BioMatchType.FGRMIN_LEFT_LITTLE,
					BioMatchType.FGRMIN_RIGHT_THUMB, BioMatchType.FGRMIN_RIGHT_INDEX, BioMatchType.FGRMIN_RIGHT_MIDDLE,
					BioMatchType.FGRMIN_RIGHT_RING, BioMatchType.FGRMIN_RIGHT_LITTLE, BioMatchType.FGRMIN_UNKNOWN),
			getFingerprint(), count -> count == 1) {

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
			return BioAuthType.getFPValuesCountInIdentity(reqDTO, helper, BioMatchType.FGRMIN_COMPOSITE);
		}
	},
	FGR_IMG("FIR",
			AuthType.setOf(BioMatchType.FGRIMG_LEFT_THUMB, BioMatchType.FGRIMG_LEFT_INDEX,
					BioMatchType.FGRIMG_LEFT_MIDDLE, BioMatchType.FGRIMG_LEFT_RING, BioMatchType.FGRIMG_LEFT_LITTLE,
					BioMatchType.FGRIMG_RIGHT_THUMB, BioMatchType.FGRIMG_RIGHT_INDEX, BioMatchType.FGRIMG_RIGHT_MIDDLE,
					BioMatchType.FGRIMG_RIGHT_RING, BioMatchType.FGRIMG_RIGHT_LITTLE),
			getFingerprint(), value -> value == 1) {

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
			return BioAuthType.getFPValuesCountInIdentity(reqDTO, helper, BioMatchType.FGRIMG_COMPOSITE);
		}
	},
	FGR_MIN_COMPOSITE("FMR", AuthType.setOf(BioMatchType.FGRMIN_COMPOSITE), getFingerprint(), value -> value == 2) {

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
				Environment environment, IdInfoFetcher idInfoFetcher) {
			return idInfoFetcher.getMatchingThreshold(getType().toLowerCase().concat(COMPOSITE_THRESHOLD));
		}

		@Override
		protected Long getBioIdentityValuesCount(AuthRequestDTO reqDTO, IdInfoFetcher helper) {
			return BioAuthType.getFPValuesCountInIdentity(reqDTO, helper, BioMatchType.FGRMIN_COMPOSITE);
		}
	},
	FGR_MIN_MULTI("FMR", AuthType.setOf(BioMatchType.FGRMIN_MULTI), getFingerprint(),
			value -> value >= 3 && value <= 10) {
		@Override
		public Map<String, Object> getMatchProperties(AuthRequestDTO authRequestDTO, IdInfoFetcher idInfoFetcher,
				String language) {
			Map<String, Object> valueMap = new HashMap<>();
			authRequestDTO.getRequest().getBiometrics().stream().map(BioIdentityInfoDTO::getData)
					.filter(bioinfo -> bioinfo.getBioType().equals(this.getType())).forEach((DataDTO bioinfovalue) -> {
						BiFunction<Map<String, String>, Map<String, String>, Double> func = idInfoFetcher
								.getFingerPrintProvider(bioinfovalue)::matchMultiMinutaeAverage;
						valueMap.put(FingerprintProvider.class.getSimpleName(), func);
					});
			return valueMap;
		}

		@Override
		public Optional<Integer> getMatchingThreshold(AuthRequestDTO authReq, String languageInfoFetcher,
				Environment environment, IdInfoFetcher idInfoFetcher) {
			return idInfoFetcher.getMatchingThreshold(getType().toLowerCase().concat(MULTI_THRESHOLD));
		}

		@Override
		protected Long getBioIdentityValuesCount(AuthRequestDTO reqDTO, IdInfoFetcher helper) {
			return BioAuthType.getFPValuesCountInIdentity(reqDTO, helper, BioMatchType.FGRMIN_COMPOSITE);
		}
	},
	IRIS_COMP_IMG("IIR", AuthType.setOf(BioMatchType.IRIS_COMP), "Iris", value -> value == 2) {

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
				Environment environment, IdInfoFetcher idInfoFetcher) {
			return idInfoFetcher.getMatchingThreshold(getType().toLowerCase().concat(COMPOSITE_THRESHOLD));
		}

		@Override
		protected Long getBioIdentityValuesCount(AuthRequestDTO reqDTO, IdInfoFetcher helper) {
			return BioAuthType.getIrisValuesCountInIdentity(reqDTO, helper);
		}

	},
	IRIS_IMG("IIR", AuthType.setOf(BioMatchType.RIGHT_IRIS, BioMatchType.LEFT_IRIS, BioMatchType.IRIS_UNKNOWN), "Iris",
			value -> value == 1) {

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
	FACE_IMG("FID", AuthType.setOf(BioMatchType.FACE, BioMatchType.FACE_UNKNOWN), "face", value -> value == 1) {

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
			long entries = 0;
			for (MatchType matchType : AuthType.setOf(BioMatchType.FACE, BioMatchType.FACE_UNKNOWN)) {
				entries += (long) helper.getIdentityRequestInfo(matchType, reqDTO.getRequest(), null).size();
			}
			return entries;

		}
	};

	/** The Constant SINGLE_THRESHOLD. */
	private static final String SINGLE_THRESHOLD = ".single.threshold";

	/** The Constant COMPOSITE_THRESHOLD. */
	private static final String COMPOSITE_THRESHOLD = ".composite.threshold";
	
	/** The Constant MULTI_THRESHOLD. */
	private static final String MULTI_THRESHOLD = ".multi.threshold";

	/** The Constant FINGERPRINT. */
	private static final String FINGERPRINT = "Fingerprint";

	private AuthTypeImpl authTypeImpl;

	/** The count. */
	private int count;

	private IntPredicate intPredicate;

	/**
	 * Instantiates a new bio auth type.
	 *
	 * @param type                 the type
	 * @param associatedMatchTypes the associated match types
	 * @param displayName          the display name
	 * @param count                the count
	 */
	private BioAuthType(String type, Set<MatchType> associatedMatchTypes, String displayName,
			IntPredicate intPredicate) {
		authTypeImpl = new AuthTypeImpl(type, associatedMatchTypes, displayName);
		this.intPredicate = intPredicate;
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

	/**
	 * Gets the iris values count in identity.
	 *
	 * @param reqDTO the req DTO
	 * @param helper the helper
	 * @return the iris values count in identity
	 */
	private static Long getIrisValuesCountInIdentity(AuthRequestDTO reqDTO, IdInfoFetcher helper) {
		return (long) helper.getIdentityRequestInfo(BioMatchType.IRIS_COMP, reqDTO.getRequest(), null).size();
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
		return authReq.getRequestedAuth().isBio()
				&& intPredicate.test(getBioIdentityValuesCount(authReq, helper).intValue());
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
			Environment environment, IdInfoFetcher idInfoFetcher) {
		return idInfoFetcher.getMatchingThreshold(getType().toLowerCase().concat(SINGLE_THRESHOLD));
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

	@Override
	public AuthType getAuthTypeImpl() {
		return authTypeImpl;
	}

}
