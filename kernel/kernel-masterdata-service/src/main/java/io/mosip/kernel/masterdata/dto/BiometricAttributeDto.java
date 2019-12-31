package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Uday Kumar
 * @since 1.0.0
 */
@Data
//@ApiModel(value = "BiometricAttribute", description = "BiometricAttribute resource representation")
public class BiometricAttributeDto {
	/**
	 * Field for code
	 */
	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "code", required = true, dataType = "java.lang.String")
	private String code;
	/**
	 * Field for name
	 */
	@NotBlank
	@Size(min = 1, max = 64)
	@ApiModelProperty(value = "name", required = true, dataType = "java.lang.String")
	private String name;

	/**
	 * Field for description
	 */
	@Size(min = 0, max = 128)
	@ApiModelProperty(value = "Biometric Attribute desc", required = false, dataType = "java.lang.String")
	private String description;
	/**
	 * Field for biometricTypecode
	 */
	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "Biometric Type code", required = true, dataType = "java.lang.String")
	private String biometricTypeCode;
	/**
	 * Field for language code
	 */
	@NotBlank
	@Size(min = 1, max = 3)
	@ValidLangCode
	@ApiModelProperty(value = "Language Code", required = true, dataType = "java.lang.String")
	private String langCode;
	/**
	 * Field for the status of data.
	 */
	@NotNull
	@ApiModelProperty(value = "BiometricAttribute isActive status", required = true, dataType = "java.lang.Boolean")
	private Boolean isActive;

}
