package io.mosip.registration.processor.core.common.rest.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Instantiates a new error DTO.
 *
 * @param errorcode the errorcode
 * @param message the message
 * 
 * @author Rishabh Keshari
 */
@Data
@AllArgsConstructor
public class ErrorDTO implements Serializable {

	private static final long serialVersionUID = 2452990684776944908L;

	/** The errorcode. */
	private String errorcode;
	
	/** The message. */
	private String message;
}
