package io.mosip.authentication.service.impl.indauth.service.bio;
import static io.mosip.authentication.core.spi.indauth.match.MatchType.setOf;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.mosip.authentication.core.dto.indauth.BioIdentityInfoDTO;
import io.mosip.authentication.core.dto.indauth.DataDTO;
import io.mosip.authentication.core.dto.indauth.IdentityDTO;
import io.mosip.authentication.core.dto.indauth.IdentityInfoDTO;
import io.mosip.authentication.core.dto.indauth.RequestDTO;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.spi.bioauth.CbeffDocType;
import io.mosip.authentication.core.spi.indauth.match.AuthType;
import io.mosip.authentication.core.spi.indauth.match.IdInfoFetcher;
import io.mosip.authentication.core.spi.indauth.match.IdMapping;
import io.mosip.authentication.core.spi.indauth.match.MatchType;
import io.mosip.authentication.core.spi.indauth.match.MatchingStrategy;
import io.mosip.authentication.core.spi.indauth.match.MatchingStrategyType;
import io.mosip.authentication.service.impl.indauth.match.IdaIdMapping;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleAnySubtypeType;;

/**
 * 
 *
 * @author Rakesh Roshan
 */
public enum BioMatchType implements MatchType {

	// Left Finger Minutiea
	FGRMIN_LEFT_THUMB(IdaIdMapping.LEFTTHUMB, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FMR, 
			SingleAnySubtypeType.LEFT,SingleAnySubtypeType.THUMB),
	FGRMIN_LEFT_INDEX(IdaIdMapping.LEFTINDEX, setOf(FingerPrintMatchingStrategy.PARTIAL), 
			CbeffDocType.FMR,SingleAnySubtypeType.LEFT,SingleAnySubtypeType.INDEX_FINGER),
	FGRMIN_LEFT_MIDDLE(IdaIdMapping.LEFTMIDDLE, setOf(FingerPrintMatchingStrategy.PARTIAL), 
			CbeffDocType.FMR,SingleAnySubtypeType.LEFT,SingleAnySubtypeType.MIDDLE_FINGER),
	FGRMIN_LEFT_RING(IdaIdMapping.LEFTRING, setOf(FingerPrintMatchingStrategy.PARTIAL), 
			CbeffDocType.FMR,SingleAnySubtypeType.LEFT,SingleAnySubtypeType.RING_FINGER),
	FGRMIN_LEFT_LITTLE(IdaIdMapping.LEFTLITTLE, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FMR,SingleAnySubtypeType.LEFT,SingleAnySubtypeType.LITTLE_FINGER),
	// Right Finger Minutiea
	FGRMIN_RIGHT_THUMB(IdaIdMapping.RIGHTTHUMB, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FMR,SingleAnySubtypeType.RIGHT,SingleAnySubtypeType.THUMB),
	FGRMIN_RIGHT_INDEX(IdaIdMapping.RIGHTINDEX, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FMR,SingleAnySubtypeType.RIGHT,SingleAnySubtypeType.INDEX_FINGER),
	FGRMIN_RIGHT_MIDDLE(IdaIdMapping.RIGHTMIDDLE, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FMR,SingleAnySubtypeType.RIGHT,SingleAnySubtypeType.MIDDLE_FINGER),
	FGRMIN_RIGHT_RING(IdaIdMapping.RIGHTRING, setOf(FingerPrintMatchingStrategy.PARTIAL), 
			CbeffDocType.FMR,SingleAnySubtypeType.RIGHT,SingleAnySubtypeType.RING_FINGER),
	FGRMIN_RIGHT_LITTLE(IdaIdMapping.RIGHTLITTLE, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FMR,SingleAnySubtypeType.RIGHT, SingleAnySubtypeType.LITTLE_FINGER),
	//unknown finger Minutiea
	FGRMIN_UNKNOWN(IdaIdMapping.UNKNOWN_FINGER,
			CbeffDocType.FMR, null, null, setOf(MultiFingerprintMatchingStrategy.PARTIAL)),

	// Left Finger Image FGRIMG
	FGRIMG_LEFT_THUMB(IdaIdMapping.LEFTTHUMB, setOf(FingerPrintMatchingStrategy.PARTIAL), 
			CbeffDocType.FIR,SingleAnySubtypeType.LEFT,
			SingleAnySubtypeType.THUMB),
	FGRIMG_LEFT_INDEX(IdaIdMapping.LEFTINDEX, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FIR,SingleAnySubtypeType.LEFT,
			SingleAnySubtypeType.INDEX_FINGER),
	FGRIMG_LEFT_MIDDLE(IdaIdMapping.LEFTMIDDLE, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FIR,SingleAnySubtypeType.LEFT,
			SingleAnySubtypeType.MIDDLE_FINGER),
	FGRIMG_LEFT_RING(IdaIdMapping.LEFTRING, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FIR,SingleAnySubtypeType.LEFT,
			SingleAnySubtypeType.RING_FINGER),
	FGRIMG_LEFT_LITTLE(IdaIdMapping.LEFTLITTLE, setOf(FingerPrintMatchingStrategy.PARTIAL), 
			CbeffDocType.FIR,SingleAnySubtypeType.LEFT,
			SingleAnySubtypeType.LITTLE_FINGER),

	// Right Finger Image
	FGRIMG_RIGHT_THUMB(IdaIdMapping.RIGHTTHUMB, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FIR,SingleAnySubtypeType.RIGHT,
			SingleAnySubtypeType.THUMB),
	FGRIMG_RIGHT_INDEX(IdaIdMapping.RIGHTINDEX, setOf(FingerPrintMatchingStrategy.PARTIAL), 
			CbeffDocType.FIR,SingleAnySubtypeType.RIGHT,
			SingleAnySubtypeType.INDEX_FINGER),
	FGRIMG_RIGHT_MIDDLE(IdaIdMapping.RIGHTMIDDLE, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FIR,SingleAnySubtypeType.RIGHT, SingleAnySubtypeType.MIDDLE_FINGER),
	FGRIMG_RIGHT_RING(IdaIdMapping.RIGHTRING, setOf(FingerPrintMatchingStrategy.PARTIAL), 
			CbeffDocType.FIR,SingleAnySubtypeType.RIGHT,
			SingleAnySubtypeType.RING_FINGER),
	FGRIMG_RIGHT_LITTLE(IdaIdMapping.RIGHTLITTLE, setOf(FingerPrintMatchingStrategy.PARTIAL),
			CbeffDocType.FIR,SingleAnySubtypeType.RIGHT, SingleAnySubtypeType.LITTLE_FINGER),

	// Multi-fingerPrint
	//FIXME get Bio ID info of all fingers and return the map
	FGRMIN_MULTI(IdaIdMapping.FINGERPRINT, setOf(MultiFingerprintMatchingStrategy.PARTIAL),
			CbeffDocType.FMR, null, null),
	
	FGRIMG_MULTI(IdaIdMapping.FINGERPRINT, setOf(MultiFingerprintMatchingStrategy.PARTIAL),
			CbeffDocType.FIR, null, null),

	RIGHT_IRIS(IdaIdMapping.RIGHTIRIS, setOf(IrisMatchingStrategy.PARTIAL),
			CbeffDocType.IRIS, SingleAnySubtypeType.RIGHT,null),

	LEFT_IRIS(IdaIdMapping.LEFTEYE, setOf(IrisMatchingStrategy.PARTIAL), 
			CbeffDocType.IRIS,SingleAnySubtypeType.LEFT, null),
	
	//FIXME get Bio ID info of all eyes and return the map
	IRIS_COMP(IdaIdMapping.IRIS, setOf(CompositeIrisMatchingStrategy.PARTIAL), CbeffDocType.IRIS, null, null),
	//unknown IRIS
	IRIS_UNKNOWN(IdaIdMapping.UNKNOWN_IRIS,
			CbeffDocType.IRIS, null, null, setOf(CompositeIrisMatchingStrategy.PARTIAL)),
	
	FACE(IdaIdMapping.FACE, Collections.emptySet(), CbeffDocType.FACE, null, null);

	/** The allowed matching strategy. */
	private Set<MatchingStrategy> allowedMatchingStrategy;

	private Function<RequestDTO, Map<String, List<IdentityInfoDTO>>> identityInfoFunction;

	private IdMapping idMapping;

	private CbeffDocType cbeffDocType;

	private SingleAnySubtypeType subType;

	private SingleAnySubtypeType singleAnySubtype;

	private BioMatchType(IdMapping idMapping, Set<MatchingStrategy> allowedMatchingStrategy, CbeffDocType cbeffDocType,
			SingleAnySubtypeType subType, SingleAnySubtypeType singleAnySubtype) {
		this(idMapping, allowedMatchingStrategy, cbeffDocType, subType, singleAnySubtype, null);
		Set<IdMapping> subIdMappings = idMapping.getSubIdMappings();
		if(subIdMappings.isEmpty()) {
			this.identityInfoFunction = requestDto -> getIdInfoFromBioIdInfo(requestDto.getBiometrics());
		} else {
			this.identityInfoFunction = requestDto -> getIdInfoFromSubIdMappings(requestDto, subIdMappings);
		}
	}
	
	private BioMatchType(IdMapping idMapping,  CbeffDocType cbeffDocType,
			SingleAnySubtypeType subType, SingleAnySubtypeType singleAnySubtype, Set<MatchingStrategy> allowedMatchingStrategy) {
		this(idMapping, allowedMatchingStrategy, cbeffDocType, subType, singleAnySubtype, null);
		this.identityInfoFunction = requestDto -> getIdInfoFromBioIdInfo(requestDto.getBiometrics());
	}

	private BioMatchType(IdMapping idMapping, Set<MatchingStrategy> allowedMatchingStrategy,CbeffDocType cbeffDocType, SingleAnySubtypeType subType, SingleAnySubtypeType singleAnySubtype,
			Function<IdentityDTO, Map<String, List<IdentityInfoDTO>>> identityInfoFunction) {
		this.idMapping = idMapping;
		this.cbeffDocType = cbeffDocType;
		this.subType = subType;
		this.singleAnySubtype = singleAnySubtype;
		this.identityInfoFunction = requestDto -> identityInfoFunction.apply(requestDto.getDemographics());
		this.allowedMatchingStrategy = Collections.unmodifiableSet(allowedMatchingStrategy);
	}
	
	private Map<String, List<IdentityInfoDTO>> getIdInfoFromBioIdInfo(List<BioIdentityInfoDTO> biometrics) {
		AtomicInteger count = new AtomicInteger(0);
		return biometrics.stream().filter(bioId -> {
					Optional<AuthType> authType = AuthType.getAuthTypeForMatchType(this, BioAuthType.values());
					if (authType.isPresent() && bioId.getData().getBioType().equalsIgnoreCase(authType.get().getType())) {
						return bioId.getData().getBioSubType().equalsIgnoreCase(getIdMapping().getIdname());
					}
					return false;
				}).map(BioIdentityInfoDTO::getData).map(DataDTO::getBioValue)
				.map(value -> Arrays.asList(new IdentityInfoDTO(null, value)))
				.collect(Collectors.toMap(value -> {
					String idname = idMapping.getIdname();
					if(idname.startsWith("UNKNOWN")) {
						idname += count.incrementAndGet();
					}
					return idname;
				}, value -> value));
	}
	
	private Map<String, List<IdentityInfoDTO>> getIdInfoFromSubIdMappings(RequestDTO identityDto, Set<IdMapping> subIdMappings) {
		BioMatchType[] subMatchTypes = getMatchTypesForSubIdMappings(subIdMappings);
		return getIdValuesMap(identityDto, subMatchTypes);
	}

	public BioMatchType[] getMatchTypesForSubIdMappings(Set<IdMapping> subIdMappings) {
		return Arrays.stream(BioMatchType.values())
											 .filter(bioMatchType -> subIdMappings.contains(bioMatchType.getIdMapping()))
											 .filter(bioMatchType -> bioMatchType.getCbeffDocType() == this.getCbeffDocType())
											 .toArray(size -> new BioMatchType[size]);
	}

	/**
	 * Gets the allowed matching strategy.
	 *
	 * @param matchStrategyType the match strategy type
	 * @return the allowed matching strategy
	 */
	public Optional<MatchingStrategy> getAllowedMatchingStrategy(MatchingStrategyType matchStrategyType) {
		return allowedMatchingStrategy.stream().filter(ms -> ms.getType().equals(matchStrategyType)).findAny();
	}

	/**
	 * Gets the entity info.
	 *
	 * @return the entity info
	 */
	public Function<Map<String, String>, Map<String, String>> getEntityInfoMapper() {
		return Function.identity();
	}

	public IdMapping getIdMapping() {
		return idMapping;
	}

	@Override
	public Function<RequestDTO, Map<String, List<IdentityInfoDTO>>> getIdentityInfoFunction() {
		return identityInfoFunction;
	}

	@Override
	public Category getCategory() {
		return Category.BIO;
	}

	@Override
	public Map<String, Entry<String, List<IdentityInfoDTO>>> mapEntityInfo(Map<String, List<IdentityInfoDTO>> idEntity,
			IdInfoFetcher idinfoFetcher) throws IdAuthenticationBusinessException {
		return idinfoFetcher.getCbeffValues(idEntity, cbeffDocType, this);
	}

	public CbeffDocType getCbeffDocType() {
		return cbeffDocType;
	}
	
	public static Map<String, List<IdentityInfoDTO>> getIdValuesMap(RequestDTO identityDto, BioMatchType... bioMatchTypes) {
	  return Stream.of(bioMatchTypes)
			  		.flatMap(bioMatchType -> 
			  						bioMatchType.getIdentityInfoFunction()
			  									.apply(identityDto)
			  									.entrySet()
			  									.stream())
			  		.collect(Collectors.toMap(Entry::getKey, Entry::getValue, 
			  										(list1, list2) 
			  											-> Stream.concat(list1.stream(), list1.stream()).collect(Collectors.toList())));	
	}
	
	public SingleAnySubtypeType getSubType() {
		return subType;
	}
	
	public SingleAnySubtypeType getSingleAnySubtype() {
		return singleAnySubtype;
	}
	
	
}
