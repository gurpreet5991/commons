package io.mosip.authentication.core.dto.indauth;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The Class For KycAuthResponseDTO extending {@link AuthResponseDTO}
 * 
 * @author Prem Kumar
 *
 *
 */

@Data
@EqualsAndHashCode(callSuper=true	)
public class KycAuthResponseDTO extends AuthResponseDTO {
	
	/** The KycResponseDTO */
	private KycResponseDTO response;

}
