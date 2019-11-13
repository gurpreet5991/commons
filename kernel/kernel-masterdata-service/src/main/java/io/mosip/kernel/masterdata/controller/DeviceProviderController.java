package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.deviceprovidermanager.spi.DeviceProviderService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.dto.DeviceProviderDto;
import io.mosip.kernel.masterdata.dto.ValidateDeviceDto;
import io.mosip.kernel.masterdata.dto.ValidateDeviceHistoryDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.DeviceProviderExtnDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Device provider controller for CURD operation
 * 
 * @author Megha Tanga
 *
 */
@RestController
@RequestMapping(value = "/deviceprovider")
@Api(tags = { "DeviceProvider" })
public class DeviceProviderController {

	@Autowired
	private DeviceProviderService<ResponseDto,ValidateDeviceDto,ValidateDeviceHistoryDto,DeviceProviderDto,DeviceProviderExtnDto> deviceProviderSerice;

	@PreAuthorize("hasAnyRole('ZONAL_ADMIN')")
	@PostMapping
	@ApiOperation(value = "Service to save Device Provide", notes = "Saves Device Provider Detail and return Device Provider")
	@ApiResponses({ @ApiResponse(code = 201, message = "When Device Provider successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When No Device Provider found"),
			@ApiResponse(code = 500, message = "While creating Device Provider any error occured") })
	public ResponseWrapper<DeviceProviderExtnDto> createDeviceProvider(
			@Valid @RequestBody RequestWrapper<DeviceProviderDto> deviceProviderDto) {
		ResponseWrapper<DeviceProviderExtnDto> response = new ResponseWrapper<>();
		response.setResponse(deviceProviderSerice.createDeviceProvider(deviceProviderDto.getRequest()));
		return response;
	}
	
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN')")
	@PutMapping
	@ApiOperation(value = "Service to save Device Provide", notes = "Saves Device Provider Detail and return Device Provider")
	@ApiResponses({ @ApiResponse(code = 201, message = "When Device Provider successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When No Device Provider found"),
			@ApiResponse(code = 500, message = "While creating Device Provider any error occured") })
	public ResponseWrapper<DeviceProviderExtnDto> updateDeviceProvider(
			@Valid @RequestBody RequestWrapper<DeviceProviderDto> deviceProviderDto) {
		ResponseWrapper<DeviceProviderExtnDto> response = new ResponseWrapper<>();
		response.setResponse(deviceProviderSerice.updateDeviceProvider(deviceProviderDto.getRequest()));
		return response;
	}

}
