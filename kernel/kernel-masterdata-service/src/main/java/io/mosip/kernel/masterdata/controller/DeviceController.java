package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.masterdata.dto.DeviceDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.masterdata.dto.getresponse.DeviceLangCodeResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.DeviceResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.IdResponseDto;
import io.mosip.kernel.masterdata.entity.id.IdAndLanguageCodeID;
import io.mosip.kernel.masterdata.service.DeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller with api to save and get Device Details
 * 
 * @author Megha Tanga
 * @author Sidhant Agarwal
 * @author Neha Sinha
 * @since 1.0.0
 *
 */

@RestController
@RequestMapping(value = "/devices")
@Api(tags = { "Device" })
public class DeviceController {

	/**
	 * Reference to DeviceService.
	 */
	@Autowired
	private DeviceService deviceService;

	/**
	 * Get api to fetch a all device details based on language code
	 * 
	 * @param langCode
	 *            pass language code as String
	 * 
	 * @return DeviceResponseDto all device details based on given language code
	 *         {@link DeviceResponseDto}
	 */
	@ResponseFilter
	@GetMapping(value = "/{languagecode}")
	@ApiOperation(value = "Retrieve all Device for the given Languge Code", notes = "Retrieve all Device for the given Languge Code", response = DeviceResponseDto.class)
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Device retrieved from database for the given Languge Code", response = DeviceResponseDto.class),
			@ApiResponse(code = 404, message = "When No Device Details found for the given Languge Code"),
			@ApiResponse(code = 500, message = "While retrieving Device any error occured") })
	public DeviceResponseDto getDeviceLang(@PathVariable("languagecode") String langCode) {
		return deviceService.getDeviceLangCode(langCode);
	}

	/**
	 * Get api to fetch a all device details based on device type and language code
	 * 
	 * @param langCode
	 *            pass language code as String
	 * 
	 * @param deviceType
	 *            pass device Type id as String
	 * 
	 * @return DeviceLangCodeResponseDto all device details based on given device
	 *         type and language code {@link DeviceLangCodeResponseDto}
	 */
	@ResponseFilter
	@GetMapping(value = "/{languagecode}/{deviceType}")
	@ApiOperation(value = "Retrieve all Device for the given Languge Code and Device Type", notes = "Retrieve all Device for the given Languge Code and Device Type", response = DeviceLangCodeResponseDto.class)
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Device retrieved from database for the given Languge Code", response = DeviceLangCodeResponseDto.class),
			@ApiResponse(code = 404, message = "When No Device Details found for the given Languge Code and Device Type"),
			@ApiResponse(code = 500, message = "While retrieving Device any error occured") })
	public DeviceLangCodeResponseDto getDeviceLangCodeAndDeviceType(@PathVariable("languagecode") String langCode,
			@PathVariable("deviceType") String deviceType) {
		return deviceService.getDeviceLangCodeAndDeviceType(langCode, deviceType);

	}

	/**
	 * Post API to insert a new row of Device data
	 * 
	 * @param deviceRequestDto
	 *            input parameter deviceRequestDto
	 * 
	 * @return ResponseEntity Device Id which is inserted successfully
	 *         {@link ResponseEntity}
	 */
	@ResponseFilter
	@PostMapping
	@ApiOperation(value = "Service to save Device", notes = "Saves Device and return Device id", response = IdResponseDto.class)
	@ApiResponses({
			@ApiResponse(code = 201, message = "When Device successfully created", response = IdResponseDto.class),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating device any error occured") })
	public ResponseEntity<IdAndLanguageCodeID> createDevice(@Valid @RequestBody RequestWrapper<DeviceDto> deviceRequestDto) {

		return new ResponseEntity<>(deviceService.createDevice(deviceRequestDto.getRequest()), HttpStatus.OK);
	}

	/**
	 * API to update an existing row of Device data
	 * 
	 * @param deviceRequestDto
	 *            input parameter deviceRequestDto
	 * 
	 * @return ResponseEntity Device Id which is updated successfully
	 *         {@link ResponseEntity}
	 */
	@ResponseFilter
	@PutMapping
	@ApiOperation(value = "Service to update Device", notes = "Update Device and return Device id", response = IdResponseDto.class)
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Device updated successfully", response = IdResponseDto.class),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When Device is not found"),
			@ApiResponse(code = 500, message = "While updating device any error occured") })
	public ResponseEntity<IdAndLanguageCodeID> updateDevice(@Valid @RequestBody RequestWrapper<DeviceDto> deviceRequestDto) {

		return new ResponseEntity<>(deviceService.updateDevice(deviceRequestDto.getRequest()), HttpStatus.OK);
	}

	/**
	 * API to delete Device
	 * 
	 * @param id
	 *            The Device Id
	 * 
	 * @return {@link ResponseEntity} The id of the Device which is deleted
	 */
	@ResponseFilter
	@DeleteMapping("/{id}")
	@ApiOperation(value = "Service to delete device", notes = "Delete Device and return Device Id", response = IdResponseDto.class)
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Device deleted successfully", response = IdResponseDto.class),
			@ApiResponse(code = 404, message = "When Device not found"),
			@ApiResponse(code = 500, message = "Error occurred while deleting Device") })
	public ResponseEntity<IdResponseDto> deleteDevice(@PathVariable("id") String id) {

		return new ResponseEntity<>(deviceService.deleteDevice(id), HttpStatus.OK);
	}
}
