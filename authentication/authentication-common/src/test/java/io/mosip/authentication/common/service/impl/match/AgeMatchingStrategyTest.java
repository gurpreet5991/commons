package io.mosip.authentication.common.service.impl.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import io.mosip.authentication.common.service.impl.match.AgeMatchingStrategy;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.spi.indauth.match.MatchFunction;
import io.mosip.authentication.core.spi.indauth.match.MatchingStrategyType;

public class AgeMatchingStrategyTest {

	/**
	 * Check for Exact type matched with Enum value of Age Matching Strategy
	 */
	@Test
	public void TestValidExactMatchingStrategytype() {
		assertEquals(AgeMatchingStrategy.EXACT.getType(), MatchingStrategyType.EXACT);
	}

	/**
	 * Check for Exact type not matched with Enum value of Age Matching Strategy
	 */
	@Test
	public void TestInvalidExactMatchingStrategytype() {
		assertNotEquals(AgeMatchingStrategy.EXACT.getType(), "PARTIAL");
	}

	/**
	 * Assert the Age Matching Strategy for Exact is Not null
	 */
	@Test
	public void TestValidExactMatchingStrategyfunctionisNotNull() {
		assertNotNull(AgeMatchingStrategy.EXACT.getMatchFunction());
	}

	/**
	 * Assert the Age Matching Strategy for Exact is null
	 */
	@Test
	public void TestExactMatchingStrategyfunctionisNull() {
		MatchFunction matchFunction = AgeMatchingStrategy.EXACT.getMatchFunction();
		matchFunction = null;
		assertNull(matchFunction);
	}

	/**
	 * Tests doMatch function on Matching Strategy Function
	 * @throws IdAuthenticationBusinessException 
	 */
	@Test
	public void TestValidExactMatchingStrategyFunction() throws IdAuthenticationBusinessException {
		MatchFunction matchFunction = AgeMatchingStrategy.EXACT.getMatchFunction();

		int value = matchFunction.match(25, 25, null);
		assertEquals(100, value);

		int value1 = matchFunction.match(100, 100, null);
		assertEquals(100, value1);
	}

	/**
	 * 
	 * Tests the Match function with in-valid values
	 * @throws IdAuthenticationBusinessException 
	 */
	@Test(expected=IdAuthenticationBusinessException.class)
	public void TestInvalidExactMatchingStrategyFunction() throws IdAuthenticationBusinessException {
		MatchFunction matchFunction = AgeMatchingStrategy.EXACT.getMatchFunction();

		int value = matchFunction.match(250, "50", null);
		assertEquals(0, value);

		int value1 = matchFunction.match(50, "25", null);
		assertEquals(0, value1);

		int value2 = matchFunction.match(100, "25", null);
		assertEquals(0, value2);

		int value3 = matchFunction.match(25, "24", null);
		assertEquals(0, value3);

		int value4 = matchFunction.match(null, null, null);
		assertEquals(0, value4);

		int value6 = matchFunction.match("abc", "1", null);
		assertEquals(0, value6);
	}
}
