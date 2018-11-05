package io.mosip.authentication.service.impl.indauth.builder;

import java.util.ArrayList;
import java.util.Arrays;

import io.mosip.authentication.core.dto.indauth.AuthError;
import io.mosip.authentication.core.dto.indauth.AuthStatusInfo;
import io.mosip.authentication.core.dto.indauth.AuthUsageDataBit;
import io.mosip.authentication.core.dto.indauth.MatchInfo;

/**
 * The builder class of AuthStatusInfo
 * 
 * @authour Loganathan Sekar
 */

public class AuthStatusInfoBuilder {

	private boolean built;

	/** The auth status info. */
	private AuthStatusInfo authStatusInfo;

	/**
	 * Instantiates a new auth status info builder.
	 */
	private AuthStatusInfoBuilder() {
		authStatusInfo = new AuthStatusInfo();
	}

	/**
	 * New instance.
	 *
	 * @return the auth status info builder
	 */
	public static AuthStatusInfoBuilder newInstance() {
		return new AuthStatusInfoBuilder();
	}

	/**
	 * Sets the status.
	 *
	 * @param status the status
	 * @return the auth status info builder
	 */
	public AuthStatusInfoBuilder setStatus(boolean status) {
		assertNotBuilt();
		authStatusInfo.setStatus(status);
		return this;
	}

	/**
	 * Adds the message info.
	 *
	 * @param matchInfoType     the match info type
	 * @param msType            the ms type
	 * @param matchingThreshold the mt
	 * @return the auth status info builder
	 */
	public AuthStatusInfoBuilder addMessageInfo(String authType, String matchingStrategy, Integer matchingThreshold,
			String language) {
		assertNotBuilt();
		if (authStatusInfo.getMatchInfos() == null) {
			authStatusInfo.setMatchInfos(new ArrayList<>());
		}
		authStatusInfo.getMatchInfos().add(new MatchInfo(authType, language, matchingStrategy, matchingThreshold));
		return this;
	}

	/**
	 * Adds the auth usage data bits.
	 *
	 * @param usageDataBits the usage data bits
	 * @return the auth status info builder
	 */
	public AuthStatusInfoBuilder addAuthUsageDataBits(AuthUsageDataBit... usageDataBits) {
		assertNotBuilt();
		if (authStatusInfo.getUsageDataBits() == null) {
			authStatusInfo.setUsageDataBits(new ArrayList<>());
		}

		authStatusInfo.getUsageDataBits().addAll(Arrays.asList(usageDataBits));
		return this;
	}

	/**
	 * Adds the errors.
	 *
	 * @param errors the errors
	 * @return the auth status info builder
	 */
	public AuthStatusInfoBuilder addErrors(AuthError... errors) {
		assertNotBuilt();
		if (authStatusInfo.getErr() == null) {
			authStatusInfo.setErr(new ArrayList<>());
		}

		authStatusInfo.getErr().addAll(Arrays.asList(errors));
		return this;
	}

	/**
	 * Builds the.
	 *
	 * @return the auth status info
	 */
	public AuthStatusInfo build() {
		assertNotBuilt();
		built = true;
		return authStatusInfo;
	}

	private void assertNotBuilt() {
		if (built) {
			throw new IllegalStateException();
		}
	}
}
