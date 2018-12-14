package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * Response dto for Document Category Detail
 * 
 * @author Neha
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@Data


public class DocumentCategoryData {
	
	private DocumentCategoryDto documentcategorytype;
/*
	*//**
	 * Document category code.
	 *//*
	@NotNull
	@Size(min = 1, max = 3)
	private String code;

	*//**
	 * Document category name.
	 *//*
	private String name;

	*//**
	 * Document category description
	 *//*
	private String description;

	*//**
	 * The Language Code.
	 *//*
	private String langCode;

	*//**
	 * Is active or not.
	 *//*
	private Boolean isActive;
*/
}
