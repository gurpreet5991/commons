package io.mosip.kernel.masterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response dto for Biometric Type Detail
 * 
 * @author Neha
 * @since 1.0.0
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiometricTypeDto {

	private String code;

	private String name;

	private String description;

	private String langCode;

	private boolean isActive;

	private String createdBy;

	private String updatedBy;

	private boolean isDeleted;

}
