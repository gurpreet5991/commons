package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.ValidLangCode;
import lombok.Data;

@Data
public class ReasonListDto {

	@NotBlank
	@Size(min = 1, max = 36)
	private String code;

	@NotBlank
	@Size(min = 1, max = 64)
	private String name;

	@Size(min = 1, max = 128)
	private String description;

	@NotBlank
	@Size(min = 1, max = 36)
	private String rsnCatCode;

	@ValidLangCode
	@NotBlank
	@Size(min = 1, max = 3)
	private String langCode;

	@NotNull
	private Boolean isActive;

}
