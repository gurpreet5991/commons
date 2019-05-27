package io.mosip.registration.processor.stages.uingenerator.idrepo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The Class IdResponseDTO.
 *
 * @author M1049387
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonFilter("responseFilter")
public class IdResponseDTO extends BaseIdRequestResponseDTO {
	
	/** The err. */
	private List<ErrorDTO> error;
	
	/** The uin id. */
	private String uin;
	
	/** The status. */
	private String status;
	
	/** The response. */
	@JsonFilter("responseFilter")
	private ResponseDTO response;
}
