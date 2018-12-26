package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.masterdata.dto.RegistrationCenterMachineDto;
import io.mosip.kernel.masterdata.dto.RequestDto;
import io.mosip.kernel.masterdata.dto.ResponseRrgistrationCenterMachineDto;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterMachineID;
import io.mosip.kernel.masterdata.service.RegistrationCenterMachineService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1.0/registrationcentermachine")
@Api(tags = { "RegistrationCenterMachine" })
public class RegistrationCenterMachineController {

	@Autowired
	private RegistrationCenterMachineService registrationCenterMachineService;

	@PostMapping
	@ApiOperation(value = "Map provided registration center and machine", notes = "Map provided registration center id and machine id", response = ResponseRrgistrationCenterMachineDto.class)
	@ApiResponses({
			@ApiResponse(code = 201, message = "When registration center and machine mapped", response = ResponseRrgistrationCenterMachineDto.class),
			@ApiResponse(code = 400, message = "When Request body passed  is invalid"),
			@ApiResponse(code = 500, message = "While mapping registration center and machine") })
	public ResponseEntity<ResponseRrgistrationCenterMachineDto> createRegistrationCenterAndMachine(
			@Valid @RequestBody RequestDto<RegistrationCenterMachineDto> requestDto) {
		return new ResponseEntity<>(registrationCenterMachineService.createRegistrationCenterAndMachine(requestDto),
				HttpStatus.CREATED);
	}

	@DeleteMapping("/{regCenterId}/{machineId}")
	public ResponseEntity<RegistrationCenterMachineID> deleteRegistrationCenterMachineMapping(@PathVariable String regCenterId,@PathVariable  String machineId){
		return new ResponseEntity<>(registrationCenterMachineService.deleteRegistrationCenterMachineMapping(regCenterId,machineId),
				HttpStatus.OK);
	}
}
