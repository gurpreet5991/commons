/**
 * 
 */
package io.mosip.kernel.auth.exception;

import java.util.ArrayList;
import java.util.List;

import io.mosip.kernel.core.exception.ServiceError;

/**
 * @author M1049825
 *
 */
public class AuthManagerException extends RuntimeException{

	/**
	 * 
	 */
	
	private String errorCode;
	private static final long serialVersionUID = 4060346018688709387L;
	
	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode
	 *            The error code for this exception
	 * @param errorMessage
	 *            The error message for this exception
	 */
	public AuthManagerException(String errorCode, String errorMessage) {
		super(errorMessage);
		this.errorCode = errorCode;
	}

	/**
	 * This variable holds the MosipErrors list.
	 */
	private List<ServiceError> list = new ArrayList<>();

	/**
	 * @param list
	 *            The error list.
	 */
	public AuthManagerException(List<ServiceError> list) {
		this.list = list;
	}

	/**
	 * Getter for error list.
	 * 
	 * @return The error list.
	 */
	public List<ServiceError> getList() {
		return list;
	}
}
