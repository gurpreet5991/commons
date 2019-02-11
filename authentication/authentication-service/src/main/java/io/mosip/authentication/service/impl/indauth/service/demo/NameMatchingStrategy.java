package io.mosip.authentication.service.impl.indauth.service.demo;

import java.util.Map;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.dto.indauth.LanguageType;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.spi.indauth.match.MatchFunction;
import io.mosip.authentication.core.spi.indauth.match.MatchingStrategyType;
import io.mosip.authentication.core.spi.indauth.match.TextMatchingStrategy;
import io.mosip.authentication.core.util.DemoMatcherUtil;

/**
 * 
 * @author Dinesh Karuppiah.T
 */

public enum NameMatchingStrategy implements TextMatchingStrategy {

	EXACT(MatchingStrategyType.EXACT, (Object reqInfo, Object entityInfo, Map<String, Object> props) -> {
		if (reqInfo instanceof String && entityInfo instanceof String) {
			String refInfoName = DemoNormalizer.normalizeName((String) reqInfo);
			String entityInfoName = DemoNormalizer.normalizeName((String) entityInfo);
			return DemoMatcherUtil.doExactMatch(refInfoName, entityInfoName);
		} else {
			return throwError(props);
		}

	}), PARTIAL(MatchingStrategyType.PARTIAL, (Object reqInfo, Object entityInfo, Map<String, Object> props) -> {
		if (reqInfo instanceof String && entityInfo instanceof String) {
			String refInfoName = DemoNormalizer.normalizeName((String) reqInfo);
			String entityInfoName = DemoNormalizer.normalizeName((String) entityInfo);
			return DemoMatcherUtil.doPartialMatch(refInfoName, entityInfoName);
		} else {
			return throwError(props);
		}
	}), PHONETICS(MatchingStrategyType.PHONETICS, (Object reqInfo, Object entityInfo, Map<String, Object> props) -> {
		if (reqInfo instanceof String && entityInfo instanceof String) {
			String refInfoName = DemoNormalizer.normalizeName((String) reqInfo);
			String entityInfoName = DemoNormalizer.normalizeName((String) entityInfo);
			String language = (String) props.get("language");
			return DemoMatcherUtil.doPhoneticsMatch(refInfoName, entityInfoName, language);
		} else {
			return throwError(props);
		}
	});

	private final MatchFunction matchFunction;

	private final MatchingStrategyType matchStrategyType;

	/**
	 * Constructor for Name Matching Strategy
	 * 
	 * @param matchStrategyType
	 * @param matchFunction
	 */
	NameMatchingStrategy(MatchingStrategyType matchStrategyType, MatchFunction matchFunction) {
		this.matchFunction = matchFunction;
		this.matchStrategyType = matchStrategyType;
	}

	private static int throwError(Map<String, Object> props) throws IdAuthenticationBusinessException {
		final Object object = props.get("languageType");
		if (object instanceof LanguageType) {
			final LanguageType langType = ((LanguageType) object);
			if (langType.equals(LanguageType.PRIMARY_LANG)) {
				throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.NAMEPRI_MISMATCH);
			} else {
				throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.NAMESEC_MISMATCH);
			}
		} else {
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNKNOWN_ERROR);
		}
	}

	@Override
	public MatchingStrategyType getType() {
		return matchStrategyType;
	}

	@Override
	public MatchFunction getMatchFunction() {
		return matchFunction;
	}

}
