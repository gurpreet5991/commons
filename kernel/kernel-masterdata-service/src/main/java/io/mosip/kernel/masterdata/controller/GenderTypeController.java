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

import io.mosip.kernel.masterdata.dto.GenderTypeDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.masterdata.dto.getresponse.GenderTypeResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.StatusResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.service.GenderTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Controller class for fetching gender data from DB
 * 
 * @author Urvil Joshi
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
@CrossOrigin
@RestController
@Api(value = "Operation related to Gender Type", tags = { "GenderType" })
public class GenderTypeController {
	@Autowired
	private GenderTypeService genderTypeService;

	/**
	 * Get API to fetch all gender types
	 * 
	 * @return list of all gender types
	 */
	@GetMapping("/gendertypes")
	public GenderTypeResponseDto getAllGenderType() {
		return genderTypeService.getAllGenderTypes();
	}

	/**
	 * Get API to fetch all gender types for a particular language code
	 * 
	 * @param langCode
	 *            the language code whose gender is to be returned
	 * @return list of all gender types for the given language code
	 */
	@GetMapping(value = "/gendertypes/{langcode}")
	public GenderTypeResponseDto getGenderBylangCode(@PathVariable("langcode") String langCode) {
		return genderTypeService.getGenderTypeByLangCode(langCode);
	}

	/**
	 * Post API to enter a new Gender Type Data
	 * 
	 * @param gender
	 *            input dto to enter a new gender data
	 * @return primary key of entered row of gender
	 */
	@ResponseFilter
	@PostMapping("/gendertypes")
	public ResponseEntity<CodeAndLanguageCodeID> saveGenderType(@Valid @RequestBody RequestWrapper<GenderTypeDto> gender) {
		return new ResponseEntity<>(genderTypeService.saveGenderType(gender.getRequest()), HttpStatus.OK);

	}

	/**
	 * Update a Gender Type
	 * 
	 * @param gender
	 *            input dto to update a gender data
	 * @return key of updated row
	 */
	@ResponseFilter
	@ApiOperation(value = "Update Gender Type", response = CodeAndLanguageCodeID.class)
	@PutMapping("/gendertypes")
	public ResponseEntity<CodeAndLanguageCodeID> updateGenderType(
			@ApiParam("Data to update with metadata") @Valid @RequestBody RequestWrapper<GenderTypeDto> gender) {
		return new ResponseEntity<>(genderTypeService.updateGenderType(gender.getRequest()), HttpStatus.OK);

	}

	/**
	 * Delete a Gender Type
	 * 
	 * @param code
	 *            the code whose gender is to be deleted
	 * @return code of deleted rows
	 */
	@ApiOperation(value = "Delete Gender Type", response = CodeAndLanguageCodeID.class)
	@DeleteMapping("/gendertypes/{code}")
	public ResponseEntity<CodeResponseDto> deleteGenderType(
			@ApiParam("Gender type Code of gender to be deleted") @PathVariable("code") String code) {
		return new ResponseEntity<>(genderTypeService.deleteGenderType(code), HttpStatus.OK);
	}
	
	/**
	 * Validate Gender name
	 * @param genderName
	 * @return StatusResponseDto
	 */
	@ApiOperation(value="validate gender name")
	@GetMapping("/gendertypes/validate/{gendername}")
	public StatusResponseDto valdiateGenderName(@PathVariable("gendername") String genderName) {
		return genderTypeService.validateGender(genderName);
	}
}
