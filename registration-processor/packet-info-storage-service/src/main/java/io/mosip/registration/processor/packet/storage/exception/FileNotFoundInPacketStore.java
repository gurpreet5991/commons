package io.mosip.registration.processor.packet.storage.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.registration.processor.packet.storage.exception.code.PacketMetaInfoErrorCode;

public class FileNotFoundInPacketStore extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public FileNotFoundInPacketStore() {
		super();
	}
	
	public FileNotFoundInPacketStore(String errorMessage) {
		super(PacketMetaInfoErrorCode.FILE_NOT_FOUND+ EMPTY_SPACE, errorMessage);
	}

	public FileNotFoundInPacketStore(String message, Throwable cause) {
		super(PacketMetaInfoErrorCode.FILE_NOT_FOUND + EMPTY_SPACE, message, cause);
	}
	

}
