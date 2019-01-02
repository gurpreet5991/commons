package io.mosip.authentication.service.impl.indauth.service.demo;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.mosip.authentication.core.dto.indauth.AuthUsageDataBit;
import io.mosip.authentication.core.dto.indauth.IdentityDTO;
import io.mosip.authentication.core.dto.indauth.IdentityInfoDTO;
import io.mosip.authentication.core.dto.indauth.LanguageType;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.core.spi.indauth.match.IdMapping;
import io.mosip.authentication.core.spi.indauth.match.MatchType;
import io.mosip.authentication.core.spi.indauth.match.MatchingStrategy;
import io.mosip.authentication.core.spi.indauth.match.MatchingStrategyType;
import io.mosip.authentication.service.impl.indauth.match.IdaIdMapping;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * @author Arun Bose The Enum DemoMatchType.
 */

public enum DemoMatchType implements MatchType {

	// @formatter:off

	/**  Primary Name Match Type. */
	NAME_PRI(IdaIdMapping.NAME, setOf(NameMatchingStrategy.EXACT, NameMatchingStrategy.PARTIAL, NameMatchingStrategy.PHONETICS), IdentityDTO::getName,
			LanguageType.PRIMARY_LANG, AuthUsageDataBit.USED_PI_NAME_PRI, AuthUsageDataBit.MATCHED_PI_NAME_PRI),

	/**  Secondary Name Match Type. */
	NAME_SEC(IdaIdMapping.NAME, setOf(NameMatchingStrategy.EXACT, NameMatchingStrategy.PARTIAL, NameMatchingStrategy.PHONETICS), IdentityDTO::getName,
			LanguageType.SECONDARY_LANG, AuthUsageDataBit.USED_PI_NAME_SEC, AuthUsageDataBit.MATCHED_PI_NAME_SEC),

	/**  Secondary Date of Birth Match Type. */
	DOB(IdaIdMapping.DOB, setOf(DOBMatchingStrategy.EXACT), IdentityDTO::getDob, LanguageType.PRIMARY_LANG,
			AuthUsageDataBit.USED_PI_DOB, AuthUsageDataBit.MATCHED_PI_DOB),

	/**  Secondary Date of Birth Type Match. */
	DOBTYPE(IdaIdMapping.DOBTYPE, setOf(DOBTypeMatchingStrategy.EXACT), IdentityDTO::getDobType,
			LanguageType.PRIMARY_LANG, AuthUsageDataBit.USED_PI_DOBTYPE, AuthUsageDataBit.MATCHED_PI_DOB_TYPE),

	/**  Secondary Date of Birth Type Match. */
	AGE(IdaIdMapping.AGE, setOf(AgeMatchingStrategy.EXACT), IdentityDTO::getAge, LanguageType.PRIMARY_LANG,
			AuthUsageDataBit.USED_PI_AGE, AuthUsageDataBit.MATCHED_PI_AGE, entityInfoMap -> {
				int age = -1;
				try {
					String value = entityInfoMap.values().stream().findFirst().orElse("");
					age = Period.between(DOBMatchingStrategy.getDateFormat().parse(value).toInstant()
							.atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()).getYears();
				} catch (ParseException e) {
					getLogger().error("sessionId", "IdType", "Id", e.getMessage());
				}
				Map<String, String> map = new LinkedHashMap<>();
				map.put(IdaIdMapping.AGE.getIdname(), String.valueOf(age));
				return map;
			}),

	/**  Gender Match Type. */
	GENDER(IdaIdMapping.GENDER, setOf(GenderMatchingStrategy.EXACT), IdentityDTO::getGender, LanguageType.PRIMARY_LANG,
			AuthUsageDataBit.USED_PI_GENDER, AuthUsageDataBit.MATCHED_PI_GENDER),
	
	/**  Phone Match Type. */
	PHONE(IdaIdMapping.PHONE, setOf(PhoneNoMatchingStrategy.EXACT), IdentityDTO::getPhoneNumber, LanguageType.PRIMARY_LANG,
			AuthUsageDataBit.USED_PI_PHONE, AuthUsageDataBit.MATCHED_PI_PHONE),
	
	/**  E-mail Match Type. */
	EMAIL(IdaIdMapping.EMAIL, setOf(EmailMatchingStrategy.EXACT), IdentityDTO::getEmailId, LanguageType.PRIMARY_LANG,
			AuthUsageDataBit.USED_PI_EMAIL, AuthUsageDataBit.MATCHED_PI_EMAIL),

	/**  */
	ADDR_LINE1_PRI(IdaIdMapping.ADDRESSLINE1, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getAddressLine1,
			LanguageType.PRIMARY_LANG, AuthUsageDataBit.USED_AD_ADDR_LINE1_PRI,
			AuthUsageDataBit.MATCHED_AD_ADDR_LINE1_PRI),

	/** The addr line1 pri. */
	ADDR_LINE1_SEC(IdaIdMapping.ADDRESSLINE1, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getAddressLine1,
			LanguageType.SECONDARY_LANG, AuthUsageDataBit.USED_AD_ADDR_LINE1_SEC,
			AuthUsageDataBit.MATCHED_AD_ADDR_LINE1_SEC),

	/** The addr line2 pri. */
	ADDR_LINE2_PRI(IdaIdMapping.ADDRESSLINE2, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getAddressLine2,
			LanguageType.PRIMARY_LANG, AuthUsageDataBit.USED_AD_ADDR_LINE2_PRI,
			AuthUsageDataBit.MATCHED_AD_ADDR_LINE2_PRI),

	/** The addr line2 pri. */
	ADDR_LINE2_SEC(IdaIdMapping.ADDRESSLINE2, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getAddressLine2,
			LanguageType.SECONDARY_LANG, AuthUsageDataBit.USED_AD_ADDR_LINE2_SEC,
			AuthUsageDataBit.MATCHED_AD_ADDR_LINE2_SEC),

	/** The addr line3 pri. */
	ADDR_LINE3_PRI(IdaIdMapping.ADDRESSLINE3, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getAddressLine3,
			LanguageType.PRIMARY_LANG, AuthUsageDataBit.USED_AD_ADDR_LINE3_PRI,
			AuthUsageDataBit.MATCHED_AD_ADDR_LINE3_PRI),

	/** The addr line3 pri. */
	ADDR_LINE3_SEC(IdaIdMapping.ADDRESSLINE3, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getAddressLine3,
			LanguageType.SECONDARY_LANG, AuthUsageDataBit.USED_AD_ADDR_LINE3_SEC,
			AuthUsageDataBit.MATCHED_AD_ADDR_LINE3_SEC),
	
	/**  Location1 Match Type primary. */
	LOCATION1_PRI(IdaIdMapping.LOCATION1, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getLocation1,
			LanguageType.PRIMARY_LANG, AuthUsageDataBit.USED_AD_LOCATION1_PRI, AuthUsageDataBit.MATCHED_AD_LOCATION1_PRI),
	
	/**  Location1 Match Type secondary. */
	LOCATION1_SEC(IdaIdMapping.LOCATION1, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getLocation1,
			LanguageType.SECONDARY_LANG, AuthUsageDataBit.USED_AD_LOCATION1_SEC, AuthUsageDataBit.MATCHED_AD_LOCATION1_SEC),
	
	/**  Location2 Match Type primary. */
	LOCATION2_PRI(IdaIdMapping.LOCATION2, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getLocation2,
			LanguageType.PRIMARY_LANG, AuthUsageDataBit.USED_AD_LOCATION2_PRI, AuthUsageDataBit.MATCHED_AD_LOCATION2_PRI),
	
	/**  Location2 Match Type secondary. */
	LOCATION2_SEC(IdaIdMapping.LOCATION2, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getLocation2,
			LanguageType.SECONDARY_LANG, AuthUsageDataBit.USED_AD_LOCATION2_SEC, AuthUsageDataBit.MATCHED_AD_LOCATION2_SEC),
	
	/**  Location3 Match Type primary. */
	LOCATION3_PRI(IdaIdMapping.LOCATION3, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getLocation3,
			LanguageType.PRIMARY_LANG, AuthUsageDataBit.USED_AD_LOCATION3_PRI, AuthUsageDataBit.MATCHED_AD_LOCATION3_PRI),
	
	/**  Location3 Match Type secondary. */
	LOCATION3_SEC(IdaIdMapping.LOCATION3, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getLocation3,
			LanguageType.SECONDARY_LANG, AuthUsageDataBit.USED_AD_LOCATION3_SEC, AuthUsageDataBit.MATCHED_AD_LOCATION3_SEC),

	/** The pincode pri. */
	PINCODE_PRI(IdaIdMapping.PINCODE, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getPinCode,
			LanguageType.PRIMARY_LANG, AuthUsageDataBit.USED_AD_ADDR_PINCODE_PRI,
			AuthUsageDataBit.MATCHED_AD_ADDR_PINCODE_PRI),

	/** The pincode pri. */
	PINCODE_SEC(IdaIdMapping.PINCODE, setOf(AddressMatchingStrategy.EXACT), IdentityDTO::getPinCode,
			LanguageType.SECONDARY_LANG, AuthUsageDataBit.USED_AD_ADDR_PINCODE_SEC,
			AuthUsageDataBit.MATCHED_AD_ADDR_PINCODE_SEC),

	/**  Primary Address MatchType. */
	ADDR_PRI(IdaIdMapping.FULLADDRESS, setOf(FullAddressMatchingStrategy.EXACT, FullAddressMatchingStrategy.PARTIAL, FullAddressMatchingStrategy.PHONETICS),
			IdentityDTO::getFullAddress, LanguageType.PRIMARY_LANG, AuthUsageDataBit.USED_FAD_ADDR_PRI,
			AuthUsageDataBit.MATCHED_FAD_ADDR_PRI),
	
	/**  Secondary Address MatchType. */
	ADDR_SEC(IdaIdMapping.FULLADDRESS, setOf(FullAddressMatchingStrategy.EXACT, FullAddressMatchingStrategy.PARTIAL, FullAddressMatchingStrategy.PHONETICS),
			IdentityDTO::getFullAddress, LanguageType.SECONDARY_LANG, AuthUsageDataBit.USED_FAD_ADDR_SEC,
			AuthUsageDataBit.MATCHED_FAD_ADDR_SEC)

	/**  */
	// @formatter:on
	;
	
	/** The mosipLogger. */
	private static final Logger mosipLogger = IdaLogger.getLogger(DemoMatchType.class);


	/** The allowed matching strategy. */
	private Set<MatchingStrategy> allowedMatchingStrategy;

	/** The entity info. */
	private Function<Map<String, String>, Map<String, String>> entityInfoFetcher;

	/** The used bit. */
	private AuthUsageDataBit usedBit;

	/** The matched bit. */
	private AuthUsageDataBit matchedBit;

	/**  */
	private LanguageType langType;

	/**  */
	private Function<IdentityDTO, Map<String, List<IdentityInfoDTO>>> identityInfoFunction;

	/**  */
	private IdMapping idMapping;

	/**
	 *  Instantiates a new demo match type.
	 *
	 * @param idMapping 
	 * @param allowedMatchingStrategy 
	 * @param identityInfoFunction 
	 * @param langType 
	 * @param usedBit 
	 * @param matchedBit 
	 * @param entityInfoFetcher 
	 */
	private DemoMatchType(IdMapping idMapping, Set<MatchingStrategy> allowedMatchingStrategy,
			Function<IdentityDTO, List<IdentityInfoDTO>> identityInfoFunction, LanguageType langType,
			AuthUsageDataBit usedBit, AuthUsageDataBit matchedBit, Function<Map<String, String>, Map<String, String>> entityInfoFetcher) {
		this.idMapping = idMapping;
		this.identityInfoFunction =  (IdentityDTO identityDTO) -> {
			Map<String, List<IdentityInfoDTO>> map = new HashMap<>();
			map.put(idMapping.getIdname(), identityInfoFunction.apply(identityDTO));
			return map;
		};
		this.langType = langType;
		this.allowedMatchingStrategy = Collections.unmodifiableSet(allowedMatchingStrategy);
		this.entityInfoFetcher = entityInfoFetcher;
		this.usedBit = usedBit;
		this.matchedBit = matchedBit;
	}
	
	/**
	 * Instantiates a new demo match type.
	 *
	 * @param idMapping 
	 * @param allowedMatchingStrategy 
	 * @param identityInfoFunction 
	 * @param langType 
	 * @param usedBit 
	 * @param matchedBit 
	 */
	private DemoMatchType(IdMapping idMapping, Set<MatchingStrategy> allowedMatchingStrategy,
			Function<IdentityDTO, List<IdentityInfoDTO>> identityInfoFunction, LanguageType langType,
			AuthUsageDataBit usedBit, AuthUsageDataBit matchedBit) {
		this(idMapping, allowedMatchingStrategy, identityInfoFunction, langType, usedBit, matchedBit, Function.identity());
	}
	
	
	/**
	 * 
	 *
	 * @return 
	 */
	public LanguageType getLanguageType() {
		return langType;
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
		return entityInfoFetcher;
	}

	/**
	 * Gets the used bit.
	 *
	 * @return the used bit
	 */
	public AuthUsageDataBit getUsedBit() {
		return usedBit;
	}

	/**
	 * Gets the matched bit.
	 *
	 * @return the matched bit
	 */
	public AuthUsageDataBit getMatchedBit() {
		return matchedBit;
	}

	/**
	 * Sets the of.
	 *
	 * @param matchingStrategies the matching strategies
	 * @return the sets the
	 */
	public static Set<MatchingStrategy> setOf(MatchingStrategy... matchingStrategies) {
		return Stream.of(matchingStrategies).collect(Collectors.toSet());

	}

	/* (non-Javadoc)
	 * @see io.mosip.authentication.service.impl.indauth.service.demo.MatchType#getIdMapping()
	 */
	public IdMapping getIdMapping() {
		return idMapping;
	}


	/* (non-Javadoc)
	 * @see io.mosip.authentication.service.impl.indauth.service.demo.MatchType#getIdentityInfoFunction()
	 */
	@Override
	public Function<IdentityDTO, Map<String, List<IdentityInfoDTO>>> getIdentityInfoFunction() {
		return identityInfoFunction;
	}
	
	/**
	 * 
	 *
	 * @return 
	 */
	private static Logger getLogger() {
		return mosipLogger;
	}

	@Override
	public Category getCategory() {
		return Category.DEMO;
	}

}
