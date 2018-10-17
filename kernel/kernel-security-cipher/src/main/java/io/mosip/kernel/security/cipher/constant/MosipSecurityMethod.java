/*
 * 
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.security.cipher.constant;

/**
 * Mosip Security methods {@link #AES_WITH_CBC_AND_PKCS5PADDING} ,
 * {@link #RSA_WITH_PKCS1PADDING}
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public enum MosipSecurityMethod {

	/**
	 * <b>Advanced Encryption Standard</b> symmetric-key block cipher security
	 * method with PKCS7 Padding
	 */
	AES_WITH_CBC_AND_PKCS5PADDING("AES/CBC/PKCS5Padding"),
	/**
	 * RSA cryptosystem security method with PKCS1 Padding
	 */
	RSA_WITH_PKCS1PADDING("RSA/ECB/PKCS1Padding");

	/**
	 * Constructor for this class
	 */
	private MosipSecurityMethod() {
	}

	/**
	 * Constructor for this class
	 * 
	 * @param value
	 *            value of security method
	 */
	private MosipSecurityMethod(String value) {
		this.setValue(value);
	}

	/**
	 * Value of security methods
	 */
	String value;

	/**
	 * Setter for {@link #value}
	 * 
	 * @param algorithm
	 *            {@link #value}
	 */
	private void setValue(String value) {
		this.value = value;
	}

	/**
	 * Getter for {@link #value}
	 * 
	 * @return {@link #value}
	 */
	public String getValue() {
		return value;
	}
}
