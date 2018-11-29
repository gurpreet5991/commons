package io.mosip.kernel.synchandler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationCodeDto {
	
	private String code;
	private Boolean isActive;

}
