package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.masterdata.dto.DocumentCategoryDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.masterdata.dto.getresponse.DocumentCategoryResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.service.DocumentCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Controller class to fetch or create document categories.
 * 
 * @author Neha
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@CrossOrigin
@RestController
@Api(tags = { "DocumentCategory" })
public class DocumentCategoryController {

	@Autowired
	DocumentCategoryService documentCategoryService;

	/**
	 * API to fetch all Document categories details
	 * 
	 * @return All Document categories
	 */
	@GetMapping("/documentcategories")
	public DocumentCategoryResponseDto getAllDocumentCategory() {
		return documentCategoryService.getAllDocumentCategory();
	}

	/**
	 * API to fetch all Document categories details based on language code
	 * 
	 * @param langCode
	 *            the language code
	 * 
	 * @return {@link DocumentCategoryResponseDto}
	 */
	@GetMapping("/documentcategories/{langcode}")
	public DocumentCategoryResponseDto getAllDocumentCategoryByLaguageCode(@PathVariable("langcode") String langCode) {
		return documentCategoryService.getAllDocumentCategoryByLaguageCode(langCode);
	}

	/**
	 * API to fetch all Document categories details based on code and language code
	 * 
	 * @param code
	 *            the code
	 * @param langCode
	 *            the language code
	 * @return {@link DocumentCategoryResponseDto}
	 */
	@GetMapping("/documentcategories/{code}/{langcode}")
	public DocumentCategoryResponseDto getDocumentCategoryByCodeAndLangCode(@PathVariable("code") String code,
			@PathVariable("langcode") String langCode) {
		return documentCategoryService.getDocumentCategoryByCodeAndLangCode(code, langCode);
	}

	/**
	 * API to create Document category
	 * 
	 * @param category
	 *            is of type {@link DocumentCategoryDto}
	 * 
	 * @return {@link CodeAndLanguageCodeID}
	 */
	@ResponseFilter
	@PostMapping("/documentcategories")
	@ApiOperation(value = "Service to create document category", notes = "Create document category and return composite id", response = CodeAndLanguageCodeID.class)
	public ResponseEntity<CodeAndLanguageCodeID> createDocumentCategory(
			@ApiParam("Document category DTO to create") @Valid @RequestBody RequestWrapper<DocumentCategoryDto> category) {
		return new ResponseEntity<>(documentCategoryService.createDocumentCategory(category.getRequest()), HttpStatus.OK);
	}

	/**
	 * Api to update Document category.
	 * 
	 * @param category
	 *            is of type {@link DocumentCategoryDto}
	 * @return {@link CodeAndLanguageCodeID}
	 */
	@ResponseFilter
	@PutMapping("/documentcategories")
	@ApiOperation(value = "Service to update document category", notes = "Update document category and return composite id", response = CodeAndLanguageCodeID.class)
	public ResponseEntity<CodeAndLanguageCodeID> updateDocumentCategory(
			@ApiParam("Document category DTO to update") @Valid @RequestBody RequestWrapper<DocumentCategoryDto> category) {
		return new ResponseEntity<>(documentCategoryService.updateDocumentCategory(category.getRequest()), HttpStatus.OK);
	}

	/**
	 * Api to delete Document Category.
	 * 
	 * @param code
	 *            the document category code.
	 * @return the code.
	 */
	@DeleteMapping("/documentcategories/{code}")
	@ApiOperation(value = "Service to delete document category", notes = "Delete document category and return composite id", response = CodeAndLanguageCodeID.class)
	public ResponseEntity<CodeResponseDto> deleteDocumentCategory(@PathVariable("code") String code) {
		return new ResponseEntity<>(documentCategoryService.deleteDocumentCategory(code), HttpStatus.OK);
	}
}
