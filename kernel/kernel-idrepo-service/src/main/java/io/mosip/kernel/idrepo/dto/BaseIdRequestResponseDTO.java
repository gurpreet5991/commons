package io.mosip.kernel.idrepo.dto;

import lombok.Data;

/**
 * The Class BaseIdRequestResponseDTO.
 *
 * @author Manoj SP
 */
@Data
public class BaseIdRequestResponseDTO {
	
	/** The id. */
	private String id;
	
	/** The ver. */
	private String version;
	
	/** The timestamp. */
	private String timestamp;
}
