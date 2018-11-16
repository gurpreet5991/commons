package io.mosip.registration.processor.packet.archiver.util.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorCodes;

/**
 * The Class PacketNotFoundException.
 * 
 * @author M1039285
 */
public class PacketNotFoundException extends BaseCheckedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new packet not found exception.
	 */
	public PacketNotFoundException() {
		super();

	}

	/**
	 * Instantiates a new packet not found exception.
	 *
	 * @param code
	 *            the code
	 * @param message
	 *            the message
	 */
	public PacketNotFoundException(String message) {
		super(PlatformErrorCodes.RPR_PDJ_PACKET_NOT_FOUND, message);

	}

	/**
	 * Instantiates a new packet not found exception.
	 *
	 * @param code
	 *            the code
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public PacketNotFoundException(String message, Throwable cause) {
		super(PlatformErrorCodes.RPR_PDJ_PACKET_NOT_FOUND, message, cause);

	}
}
