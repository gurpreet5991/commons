package io.mosip.authentication.service.impl.indauth.service.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * 
 * @author Dinesh Karuppiah
 */
public class FullAddressMatchingStrategyTest {

	/**
	 * Check for Exact type matched with Enum value of Name Matching Strategy
	 */
	@Test
	public void TestValidExactMatchingStrategytype() {
		assertEquals(FullAddressMatchingStrategy.EXACT.getType(), MatchingStrategyType.EXACT);
	}

	/**
	 * Check for Exact type not matched with Enum value of Name Matching Strategy
	 */
	@Test
	public void TestInvalidExactMatchingStrategytype() {
		assertNotEquals(FullAddressMatchingStrategy.EXACT.getType(), "PARTIAL");
	}

	/**
	 * Assert the Name Matching Strategy for Exact is Not null
	 */
	@Test
	public void TestValidExactMatchingStrategyfunctionisNotNull() {
		assertNotNull(FullAddressMatchingStrategy.EXACT.getMatchFunction());
	}

	/**
	 * Assert the Name Matching Strategy for Exact is null
	 */
	@Test
	public void TestExactMatchingStrategyfunctionisNull() {
		MatchFunction matchFunction = FullAddressMatchingStrategy.EXACT.getMatchFunction();
		matchFunction = null;
		assertNull(matchFunction);
	}

	/**
	 * Tests doMatch function on Matching Strategy Function
	 */
	@Test
	public void TestValidExactMatchingStrategyFunction() {
		MatchFunction matchFunction = FullAddressMatchingStrategy.EXACT.getMatchFunction();
		int value = matchFunction.doMatch("a k Chowdary Beach view colony apt 12 main st TamilNadu 560055",
				"a k Chowdary Beach view colony apt 12 main st TamilNadu 560055");
		assertEquals(100, value);
	}

	/**
	 * 
	 * Tests the Match function with in-valid values
	 */
	@Test
	public void TestInvalidExactMatchingStrategyFunction() {
		MatchFunction matchFunction = FullAddressMatchingStrategy.EXACT.getMatchFunction();
		int value = matchFunction.doMatch(2, 2);
		assertEquals(0, value);

		int value1 = matchFunction.doMatch(2, "no 1 second street chennai");
		assertEquals(0, value1);

		int value2 = matchFunction.doMatch("no 1 second street chennai", 2);
		assertEquals(0, value2);
	}

	/**
	 * Assert Partial type matched with Enum value of Name Matching Strategy
	 */
	@Test
	public void TestValidPartialMatchingStrategytype() {
		assertEquals(FullAddressMatchingStrategy.PARTIAL.getType(), MatchingStrategyType.PARTIAL);
	}

	/**
	 * Assert Partial type not-matched with Enum value of Name Matching Strategy
	 */
	@Test
	public void TestInvalidPartialMatchingStrategytype() {
		assertNotEquals(FullAddressMatchingStrategy.PARTIAL.getType(), "EXACT");
	}

	/**
	 * Assert Partial Match function is not null
	 */
	@Test
	public void TestValidPartialMatchingStrategyfunctionisNotNull() {
		assertNotNull(FullAddressMatchingStrategy.PARTIAL.getMatchFunction());
	}

	/**
	 * Assert Partial Match function is null
	 */
	@Test
	public void TestPartialMatchingStrategyfunctionisNull() {
		MatchFunction matchFunction = FullAddressMatchingStrategy.PARTIAL.getMatchFunction();
		matchFunction = null;
		assertNull(matchFunction);
	}

	/**
	 * Assert the Partial Match Function with Do-Match Method
	 */
	@Test
	public void TestValidPartialMatchingStrategyFunction() {
		MatchFunction matchFunction = FullAddressMatchingStrategy.PARTIAL.getMatchFunction();
		int value = matchFunction.doMatch("no 1 second street chennai", "no 1 second");
		assertEquals(50, value);
	}

	/**
	 * Assert the Partial Match Function not matched via Do-Match Method
	 */
	@Test
	public void TestInvalidPartialMatchingStrategyFunction() {
		MatchFunction matchFunction = FullAddressMatchingStrategy.PARTIAL.getMatchFunction();
		int value = matchFunction.doMatch(2, 2);
		assertEquals(0, value);

		int value1 = matchFunction.doMatch(2, "no 1 second street chennai");
		assertEquals(0, value1);

		int value2 = matchFunction.doMatch("no 1 second street chennai", 2);
		assertEquals(0, value2);
	}

	/**
	 * Assert Phonetics Match Value is not-null on Name Matching Strategy
	 */
	@Test
	public void TestPhoneticsMatch() {
		assertNotNull(FullAddressMatchingStrategy.PHONETICS);
	}

	/**
	 * Assert Phonetics Match Type is equals with Matching Strategy type
	 */
	@Test
	public void TestPhoneticsMatchStrategyType() {
		assertEquals(FullAddressMatchingStrategy.PHONETICS.getType(), MatchingStrategyType.PHONETICS);
	}

	/**
	 * Assert Phonetics Match Value via doMatch method
	 */
	@Test
	public void TestPhoneticsMatchValue() {
		MatchFunction matchFunction = FullAddressMatchingStrategy.PHONETICS.getMatchFunction();
		int value = matchFunction.doMatch(2, 2);
		assertEquals(0, value);
	}
}
