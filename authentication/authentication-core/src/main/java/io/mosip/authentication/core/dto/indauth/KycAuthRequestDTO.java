package io.mosip.authentication.core.dto.indauth;

import java.util.List;

import lombok.Data;

/**
 * 
 * @author Prem Kumar
 *
 *         The Class For KycAuthRequestDTO extending BaseAuthRequestDTO
 */
@Data
public class KycAuthRequestDTO extends AuthRequestDTO {

	/** The value for secondary language code. */
	private String secondaryLangCode;

	private List<String> allowedKycAttributes;

}
