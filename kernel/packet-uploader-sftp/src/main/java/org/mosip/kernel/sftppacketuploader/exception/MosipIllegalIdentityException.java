package org.mosip.kernel.sftppacketuploader.exception;

import org.mosip.kernel.core.exception.BaseUncheckedException;
import org.mosip.kernel.sftppacketuploader.constant.PacketUploaderConstants;
import org.mosip.kernel.sftppacketuploader.constant.PacketUploaderExceptionConstants;

/**
 * Exception to be thrown when Private key is not valid
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class MosipIllegalIdentityException extends BaseUncheckedException {

	/**
	 * Constant id for serialization
	 */
	private static final long serialVersionUID = -7665593898258210837L;

	/**
	 * Constructor for this class
	 * 
	 * @param exceptionConstants
	 *            exception code constant
	 * @param cause
	 *            cause of exception
	 */
	public MosipIllegalIdentityException(PacketUploaderExceptionConstants exceptionConstants, Throwable cause) {
		super(exceptionConstants.getErrorCode(), exceptionConstants.getErrorMessage()
				+ PacketUploaderConstants.EXCEPTTION_BREAKER.getValue() + cause.getMessage());
	}

}
