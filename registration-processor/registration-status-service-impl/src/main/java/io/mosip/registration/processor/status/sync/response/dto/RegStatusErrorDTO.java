package io.mosip.registration.processor.status.sync.response.dto;

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
public class RegStatusErrorDTO {

	/** The errorcode. */
	private String errorcode;
	
	/** The message. */
	private String message;
}
