package io.mosip.authentication.core.dto.indauth;

import lombok.Data;

/**
 * 
 * @author Prem Kumar
 *
 */

@Data
public class KycAuthRequestDTO extends BaseAuthRequestDTO {
	
	private boolean consentReq;
	
	private boolean ePrintReq;
	
	private AuthRequestDTO authRequest; 
	
	
}
