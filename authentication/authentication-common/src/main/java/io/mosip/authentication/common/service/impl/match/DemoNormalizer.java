package io.mosip.authentication.common.service.impl.match;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.spi.indauth.match.MasterDataFetcher;

/**
 * Generic class to normalize individual name, address.
 *
 * @author Rakesh Roshan
 */
public final class DemoNormalizer {


	/** The Constant REGEX_SPECIAL_CHARACTERS. */
	private static final String REGEX_SPECIAL_CHARACTERS = "[\\.|,|\\-|\\*|\\(|\\)|\\[|\\]|`|\\'|/|\\|#|\"]";
	
	/** The Constant REGEX_SALUTATION. */
	private static final String REGEX_SALUTATION = "(M|m|D|d)(rs?)(.)";
	
	/** The Constant REGEX_CARE_OF_LABLE. */
	private static final String REGEX_CARE_OF_LABLE = "[CcSsDdWwHh]/[Oo]";
	
	/** The Constant OTHER. */
	private static final String OTHER = "(N|n)(O|o)(\\.)?";
	
	/** The Constant REGEX_WHITE_SPACE. */
	private static final String REGEX_WHITE_SPACE = "\\s+";
	
	/** The Constant APARTMENT. */
	private static final String APARTMENT = "[aA][pP][aA][rR][tT][mM][eE][nN][tT]";
	
	/** The Constant STREET. */
	private static final String STREET = "[sS][tT][rR][eE][eE][tT]";
	
	/** The Constant ROAD. */
	private static final String ROAD = "[rR][oO][aA][dD]";
	
	/** The Constant MAIN. */
	private static final String MAIN = "[mM][aA][iI][nN]";
	
	/** The Constant CROSS. */
	private static final String CROSS = "[cC][rR][oO][sS][sS]";
	
	/** The Constant SECTOR. */
	private static final String SECTOR = "[sS][eE][cC][tT][oO][rR]";
	
	/** The Constant OPPOSITE. */
	private static final String OPPOSITE = "[oO][pP][pP][oO][sS][iI][tT][eE]";
	
	/** The Constant MARKET. */
	private static final String MARKET = "[mM][aA][rR][kK][eE][tT]";
	
	/** The Constant patterns. */
	private static final Map<Pattern, String> ADDRESS_NORM_PATTERNS;
	
	static {
		ADDRESS_NORM_PATTERNS  =  new LinkedHashMap<>();
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(REGEX_CARE_OF_LABLE), "");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(REGEX_SALUTATION), "");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(OTHER), "");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(REGEX_SPECIAL_CHARACTERS), "");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(REGEX_WHITE_SPACE), " ");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(APARTMENT), "apt");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(STREET), "st");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(ROAD), "rd");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(MAIN), "mn");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(CROSS), "crs");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(SECTOR), "sec");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(OPPOSITE), "opp");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile(MARKET), "mkt");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("1[sS][tT]"), "1");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("1[tT][hH]"), "1");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("2[nN][dD]"), "2");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("2[tT][hH]"), "2");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("3[rR][dD]"), "3");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("3[tT][hH]"), "3");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("4[tT][hH]"), "4");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("5[tT][hH]"), "5");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("6[tT][hH]"), "6");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("7[tT][hH]"), "7");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("8[tT][hH]"), "8");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("9[tT][hH]"), "9");
		ADDRESS_NORM_PATTERNS.put(Pattern.compile("0[tT][hH]"), "0");
	}

	/**
	 * Instantiates a new demo normalizer.
	 */
	private DemoNormalizer() {
		
	}
	


	/**
	 * This method is used to normalize name.
	 *
	 * @param nameInfo the name info
	 * @param language the language
	 * @param titleFetcher the title fetcher
	 * @return the string
	 * @throws IdAuthenticationBusinessException the id authentication business exception
	 */
	public static String normalizeName(String nameInfo, String language, MasterDataFetcher titleFetcher) throws IdAuthenticationBusinessException {
		Map<String, List<String>> fetchTitles = titleFetcher.get();
		
		String name = nameInfo;
		List<String> titlesList = fetchTitles.get(language);
		if (null != titlesList) {
			Collections.sort(titlesList, Comparator.comparing(String::length).reversed());
			for (String title : titlesList) {
				String title1 = title + ".";
				if (name.toLowerCase().contains(title1.toLowerCase())) {
					name = name.replace(title1, "").replace(title1.toLowerCase(), "").replace(title1.toUpperCase(), "");
				}
				
				if (name.toLowerCase().contains(title.toLowerCase())) {
					name = name.replace(title, "").replace(title.toLowerCase(), "").replace(title.toUpperCase(), "");
				}
			} 
		}
		name = name.replaceAll(REGEX_SPECIAL_CHARACTERS, "")
				.replaceAll(REGEX_WHITE_SPACE, " ")
				.trim();
		return name;
	}

	/**
	 * This method is used to normalize address.
	 *
	 * @param address the address received from request or entity
	 * @return the string output after normalization
	 */
	public static String normalizeAddress(String address) {
		StringBuilder addressBuilder = new StringBuilder(address);
		for(Map.Entry<Pattern,String> addressEntry : ADDRESS_NORM_PATTERNS.entrySet()) {
			Matcher m = addressEntry.getKey().matcher(addressBuilder);
			//Find from start
			int findStart = 0;
			while(m.find(findStart)) {
				int start = m.start();
				String replacement = addressEntry.getValue();
				addressBuilder.replace(m.start(), m.end(), replacement);
				//Find next from the replacement index
				findStart = start + replacement.length();
			}
		}
		return addressBuilder.toString().trim();
	}
}
