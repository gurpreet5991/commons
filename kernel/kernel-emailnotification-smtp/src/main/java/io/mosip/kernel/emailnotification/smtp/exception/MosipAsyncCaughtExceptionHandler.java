package io.mosip.kernel.emailnotification.smtp.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Exception class for {@link MosipAsyncCaughtExceptionHandler}.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
public class MosipAsyncCaughtExceptionHandler extends BaseUncheckedException {

	/**
	 * Generated serial version.
	 */
	private static final long serialVersionUID = 3949838534862481500L;

	/**
	 * Constructor for MosipAsyncCaughtExceptionHandler.
	 */
	public MosipAsyncCaughtExceptionHandler(Throwable e) {
		super(e.getLocalizedMessage(),e.getMessage());
	}
}
