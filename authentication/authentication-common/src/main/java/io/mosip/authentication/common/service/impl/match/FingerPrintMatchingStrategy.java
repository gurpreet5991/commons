package io.mosip.authentication.common.service.impl.match;

import java.util.Map;
import java.util.function.BiFunction;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.core.spi.indauth.match.MatchFunction;
import io.mosip.authentication.core.spi.indauth.match.MatchingStrategy;
import io.mosip.authentication.core.spi.indauth.match.MatchingStrategyType;
import io.mosip.authentication.core.spi.provider.bio.FingerprintProvider;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * @author Dinesh Karuppiah.T
 *
 */
public enum FingerPrintMatchingStrategy implements MatchingStrategy {

	PARTIAL(MatchingStrategyType.PARTIAL, (Object reqInfo, Object entityInfo, Map<String, Object> props) -> {
		if (reqInfo instanceof Map && entityInfo instanceof Map) {
			String reqInfoValue = ((Map<String, String>) reqInfo).values().stream().findFirst().orElse("");
			String entityInfoValue = ((Map<String, String>) entityInfo).values().stream().findFirst().orElse("");
			Object object = props.get(FingerprintProvider.class.getSimpleName());
			if (object instanceof BiFunction) {
				BiFunction<String, String, Double> func = (BiFunction<String, String, Double>) object;
				return (int) func.apply((String) reqInfoValue, (String) entityInfoValue).doubleValue();
			} else {
				logError(IdAuthenticationErrorConstants.BIO_MISMATCH);
				throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.BIO_MISMATCH);
			}
		} else {
			Object object = props.get(BioAuthType.class.getSimpleName());
			if (object instanceof BioAuthType) {
				BioAuthType bioAuthType = ((BioAuthType) object);
				if (bioAuthType.equals(BioAuthType.FGR_MIN)) {
					logError(IdAuthenticationErrorConstants.BIO_MISMATCH);
					throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.BIO_MISMATCH);
				} else if (bioAuthType.equals(BioAuthType.FGR_IMG)) {
					logError(IdAuthenticationErrorConstants.BIO_MISMATCH);
					throw new IdAuthenticationBusinessException(
							IdAuthenticationErrorConstants.BIO_MISMATCH.getErrorCode(),
							String.format(IdAuthenticationErrorConstants.BIO_MISMATCH.getErrorMessage(),
									BioAuthType.FACE_IMG.getType()));
				} else {
					logError(IdAuthenticationErrorConstants.BIO_MISMATCH);
					throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.BIO_MISMATCH);
				}
			} else {
				logError(IdAuthenticationErrorConstants.BIO_MISMATCH);
				throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.BIO_MISMATCH);
			}
		}
	});

	private final MatchingStrategyType matchStrategyType;

	private final MatchFunction matchFunction;

	/** The mosipLogger. */
	private static Logger mosipLogger = IdaLogger.getLogger(FingerPrintMatchingStrategy.class);

	/** The Constant DEFAULT_SESSION_ID. */
	private static final String DEFAULT_SESSION_ID = "sessionId";

	/** The Constant AGE Matching strategy. */
	private static final String TYPE = "FingerPrintMatchingStrategy";

	private FingerPrintMatchingStrategy(MatchingStrategyType matchStrategyType, MatchFunction matchFunction) {
		this.matchStrategyType = matchStrategyType;
		this.matchFunction = matchFunction;
	}

	private static void logError(IdAuthenticationErrorConstants errorConstants) {
		mosipLogger.error(DEFAULT_SESSION_ID, TYPE, "Inside Fingerprint Strategy" + errorConstants.getErrorCode(),
				errorConstants.getErrorMessage());
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
