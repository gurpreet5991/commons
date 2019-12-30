package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegProcCommonResponseDto {
	private String registrationId;
	private String status;
	private String message;
}
