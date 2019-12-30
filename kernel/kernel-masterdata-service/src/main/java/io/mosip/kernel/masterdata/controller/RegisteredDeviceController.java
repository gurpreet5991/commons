package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.dto.DeviceDeRegisterResponse;
import io.mosip.kernel.masterdata.dto.DeviceRegisterResponseDto;
import io.mosip.kernel.masterdata.dto.EncodedRegisteredDeviceResponse;
import io.mosip.kernel.masterdata.dto.RegisteredDevicePostReqDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.RegisteredDeviceExtnDto;
import io.mosip.kernel.masterdata.service.RegisteredDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller for CURD operation on Registered Device Details
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@RestController
@RequestMapping(value = "/registereddevices")
@Api(tags = { "Registered Device" })
public class RegisteredDeviceController {

	@Autowired
	RegisteredDeviceService registeredDeviceService;

	/**
	 * Api to Register Device.
	 * 
	 * @param registeredDevicePostReqDto
	 *            the request DTO.
	 * @return response
	 */
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN')")
	@PostMapping
	@ApiOperation(value = "Service to save Registered Device", notes = "Saves Registered Device Detail and return Registered Device")
	@ApiResponses({ @ApiResponse(code = 201, message = "When Registered Device successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When No Registered Device found"),
			@ApiResponse(code = 500, message = "While creating Registered Device any error occured") })
	public ResponseWrapper<RegisteredDeviceExtnDto> createRegisteredDevice(
			@Valid @RequestBody RequestWrapper<RegisteredDevicePostReqDto> registeredDevicePostReqDto) {
		ResponseWrapper<RegisteredDeviceExtnDto> response = new ResponseWrapper<>();
		response.setResponse(registeredDeviceService.createRegisteredDevice(registeredDevicePostReqDto.getRequest()));
		return response;
	}
	
	/**
	 * Api to de-register Device.
	 * 
	 * @param request
	 *            the request DTO.
	 * @return the {@link DeviceRegisterResponseDto}.
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN')")
	@ApiOperation(value = "DeRegister Device")
	@DeleteMapping("/deregister/{deviceCode}")
	@ResponseFilter
	public ResponseWrapper<DeviceDeRegisterResponse> deRegisterDevice(@Valid @PathVariable String deviceCode) {
		ResponseWrapper<DeviceDeRegisterResponse> response = new ResponseWrapper<>();
		response.setResponse(registeredDeviceService.deRegisterDevice(deviceCode));
		return response;
	}
	
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN')")
	@ApiOperation(value = "Update status of the devive")
	@PutMapping("/update/status")
	public ResponseEntity<ResponseDto> deRegisterDevice(@NotBlank @RequestParam(name="deviceCode",required=true) String deviceCode,
			@NotBlank @RequestParam(name="statusCode",required=true) String statusCode) {
		return new ResponseEntity<>(registeredDeviceService.updateStatus(deviceCode, statusCode), HttpStatus.OK);
	}

}
