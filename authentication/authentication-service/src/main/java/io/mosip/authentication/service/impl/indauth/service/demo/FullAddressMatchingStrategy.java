package io.mosip.authentication.service.impl.indauth.service.demo;

import java.util.function.ToIntBiFunction;

import org.hibernate.boot.jaxb.hbm.spi.EntityInfo;

import io.mosip.authentication.core.dto.indauth.IdentityValue;
import io.mosip.authentication.core.util.MatcherUtil;

public enum FullAddressMatchingStrategy implements MatchingStrategy {

	EXACT(MatchingStrategyType.EXACT, (Object reqInfo, IdentityValue entityInfo) -> {
		if (reqInfo instanceof String) {
			String refInfoName = DemoNormalizer.normalizeAddress((String) reqInfo);
			String entityInfoName = DemoNormalizer.normalizeAddress(entityInfo.getValue());
			return MatcherUtil.doExactMatch(refInfoName, entityInfoName);
		} else {
			return 0;
		}
	}), PARTIAL(MatchingStrategyType.PARTIAL, (Object reqInfo, IdentityValue entityInfo) -> {
		if (reqInfo instanceof String) {
			String refInfoName = DemoNormalizer.normalizeAddress((String) reqInfo);
			String entityInfoName = DemoNormalizer.normalizeAddress(entityInfo.getValue());
			return MatcherUtil.doPartialMatch(refInfoName, entityInfoName);
		} else {
			return 0;
		}
	}), PHONETICS(MatchingStrategyType.PHONETICS, (reqInfo, entityInfo) -> 0);

	private final ToIntBiFunction<Object, IdentityValue> matchFunction;

	private final MatchingStrategyType matchStrategyType;

	/**
	 * Constructor for Full Address Matching Strategy
	 * 
	 * @param matchStrategyType
	 * @param matchFunction
	 */
	FullAddressMatchingStrategy(MatchingStrategyType matchStrategyType,
			ToIntBiFunction<Object, IdentityValue> matchFunction) {
		this.matchFunction = matchFunction;
		this.matchStrategyType = matchStrategyType;
	}

	@Override
	public MatchingStrategyType getType() {
		return matchStrategyType;
	}

	@Override
	public ToIntBiFunction<Object, IdentityValue> getMatchFunction() {
		return matchFunction;
	}
}
