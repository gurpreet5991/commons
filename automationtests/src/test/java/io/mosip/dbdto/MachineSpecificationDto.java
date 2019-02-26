package io.mosip.dbdto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Response dto for Machine History Detail
 * 
 * @author Arjun
 * @since 1.0.0
 *
 */
@Data
public class MachineSpecificationDto {

	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "id", required = true, dataType = "java.lang.String")
	private String id;

	@NotBlank
	@Size(min = 1, max = 64)
	@ApiModelProperty(value = "name", required = true, dataType = "java.lang.String")
	private String name;

	@NotBlank
	@Size(min = 1, max = 32)
	@ApiModelProperty(value = "brand", required = true, dataType = "java.lang.String")
	private String brand;

	@NotBlank
	@Size(min = 1, max = 16)
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;

	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "machineTypeCode", required = true, dataType = "java.lang.String")
	private String machineTypeCode;

	@NotBlank
	@Size(min = 1, max = 16)
	@ApiModelProperty(value = "minDriverversion", required = true, dataType = "java.lang.String")
	private String minDriverversion;

	@Size(min = 1, max = 256)
	@ApiModelProperty(value = "description", required = true, dataType = "java.lang.String")
	private String description;

	@NotBlank
	@Size(min = 1, max = 3)
	@ApiModelProperty(value = "langCode", required = true, dataType = "java.lang.String")
	private String langCode;

	@NotNull
	@ApiModelProperty(value = "isActive", required = true, dataType = "java.lang.Boolean")
	private Boolean isActive;

}
