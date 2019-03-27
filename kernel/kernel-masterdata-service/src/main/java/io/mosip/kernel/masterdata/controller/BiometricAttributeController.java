package io.mosip.kernel.masterdata.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.masterdata.dto.BioTypeCodeAndLangCodeAndAttributeCode;
import io.mosip.kernel.masterdata.dto.BiometricAttributeDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.masterdata.dto.getresponse.BiometricAttributeResponseDto;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.service.BiometricAttributeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * @author Uday Kumar
 * @since 1.0.0
 *
 */
@RestController
@Api(tags = { "BiometricAttribute" })
public class BiometricAttributeController {
	/**
	 * biometric attribute controller with api to get list of biometric attributes
	 * best on biometric code type and language code.
	 * 
	 * @param langCode
	 *            input from user
	 * @param biometricTypeCode
	 *            input from user
	 * @return {@link BiometricAttributeResponseDto}
	 * 
	 */
	@Autowired
	BiometricAttributeService biometricAttributeService;

	@ResponseFilter
	@ApiOperation(value = "Fetch all the biometric attributes avialbale for specific BiometricType")
	@GetMapping("/getbiometricattributesbyauthtype/{langcode}/{biometrictypecode}")
	public BiometricAttributeResponseDto getBiometricAttributesByBiometricType(
			@PathVariable("langcode") String langCode, @PathVariable("biometrictypecode") String biometricTypeCode) {
		List<BiometricAttributeDto> biomentricAttributes = biometricAttributeService
				.getBiometricAttribute(biometricTypeCode, langCode);
		return new BiometricAttributeResponseDto(biomentricAttributes);
	}

	/**
	 * Service to create Biometric Attribute in table.
	 * 
	 * @param biometricAttribute
	 *            Input from user Biometric Attribute DTO
	 * @return {@link BioTypeCodeAndLangCodeAndAttributeCode}
	 */
	@ResponseFilter
	@PostMapping("/biometricattributes")
	@ApiOperation(value = "Service to create Biometric Attributes", notes = "create Biometric Attributes  and return  code and LangCode", response = CodeAndLanguageCodeID.class)
	@ApiResponses({ @ApiResponse(code = 201, message = " successfully created", response = CodeAndLanguageCodeID.class),
			@ApiResponse(code = 400, message = " Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = " creating any error occured") })
	public ResponseEntity<CodeAndLanguageCodeID> createBiometricAttribute(
			@Valid @RequestBody RequestWrapper<BiometricAttributeDto> biometricAttribute) {
		return new ResponseEntity<>(biometricAttributeService.createBiometricAttribute(biometricAttribute.getRequest()),
				HttpStatus.OK);
	}

}
