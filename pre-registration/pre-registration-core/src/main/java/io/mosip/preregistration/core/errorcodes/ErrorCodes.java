package io.mosip.preregistration.core.errorcodes;

/**
 * Error codes
 * 
 * @author M1037717
 *
 */
public enum ErrorCodes {
	PRG_CORE_REQ_001("PRG_CORE_REQ_001"), 
	PRG_CORE_REQ_002("PRG_CORE_REQ_002"), 
	PRG_CORE_REQ_003("PRG_CORE_REQ_003"),
	PRG_CORE_REQ_004("PRG_CORE_REQ_004"),
	PRG_CORE_REQ_005("PRG_CORE_REQ_005"),
	PRG_CORE_REQ_006("PRG_CORE_REQ_006"), 
	PRG_CORE_REQ_007("PRG_CORE_REQ_007"),
	PRG_CORE_REQ_008("PRG_CORE_REQ_008"),
	PRG_CORE_REQ_009("PRG_CORE_REQ_009"),
	PRG_CORE_REQ_010("PRG_CORE_REQ_010");

	private ErrorCodes(String code) {
		this.code = code;
	}

	private final String code;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
}
