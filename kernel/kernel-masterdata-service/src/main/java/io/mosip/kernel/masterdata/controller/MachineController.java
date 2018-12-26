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
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.masterdata.dto.MachineDto;
import io.mosip.kernel.masterdata.dto.RequestDto;
import io.mosip.kernel.masterdata.dto.getresponse.MachineResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.IdResponseDto;
import io.mosip.kernel.masterdata.service.MachineService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * Controller with api to save and get Machine Details
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

@RestController
@Api(tags = { "Machine" })
public class MachineController {

	/**
	 * Reference to MachineService.
	 */
	@Autowired
	private MachineService machineService;

	/**
	 * 
	 * Function to fetch machine detail based on given Machine ID and Language code.
	 * 
	 * @param machineId
	 *            pass Machine ID as String
	 * @param langCode
	 *            pass language code as String
	 * @return MachineResponseDto machine detail based on given Machine ID and
	 *         Language code {@link MachineResponseDto}
	 */
	@GetMapping(value = "/v1.0/machines/{id}/{langcode}")
	@ApiOperation(value = "Retrieve all Machine Details for given Languge Code", notes = "Retrieve all Machine Detail for given Languge Code and ID", response = MachineResponseDto.class)
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Machine Details retrieved from database for the given Languge Code and ID", response = MachineResponseDto.class),
			@ApiResponse(code = 404, message = "When No Machine Details found for the given Languge Code and ID"),
			@ApiResponse(code = 500, message = "While retrieving Machine Details any error occured") })
	public MachineResponseDto getMachineIdLangcode(@PathVariable("id") String machineId,
			@PathVariable("langcode") String langCode) {
		return machineService.getMachine(machineId, langCode);

	}

	/**
	 * 
	 * Function to fetch machine detail based on given Language code
	 * 
	 * @param langCode
	 *            pass language code as String
	 * 
	 * @return MachineResponseDto machine detail based on given Language code
	 *         {@link MachineResponseDto}
	 */

	@GetMapping(value = "/v1.0/machines/{langcode}")
	@ApiOperation(value = "Retrieve all Machine Details for given Languge Code", notes = "Retrieve all Machine Detail for given Languge Code", response = MachineResponseDto.class)
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Machine Details retrieved from database for the given Languge Code", response = MachineResponseDto.class),
			@ApiResponse(code = 404, message = "When No Machine Details found for the given Languge Code"),
			@ApiResponse(code = 500, message = "While retrieving Machine Details any error occured") })
	public MachineResponseDto getMachineLangcode(@PathVariable("langcode") String langCode) {
		return machineService.getMachine(langCode);

	}

	/**
	 * Function to fetch a all machines details
	 * 
	 * @return MachineResponseDto all machines details {@link MachineResponseDto}
	 */
	@GetMapping(value = "/v1.0/machines")
	@ApiOperation(value = "Retrieve all Machine Details", notes = "Retrieve all Machine Detail", response = MachineResponseDto.class)
	@ApiResponses({
			@ApiResponse(code = 200, message = "When all Machine retrieved from database", response = MachineResponseDto.class),
			@ApiResponse(code = 404, message = "When No Machine found"),
			@ApiResponse(code = 500, message = "While retrieving Machine any error occured") })
	public MachineResponseDto getMachineAll() {
		return machineService.getMachineAll();

	}

	/**
	 * Post API to insert a new row of Machine data
	 * 
	 * @param machine
	 *            input from user Machine DTO
	 * 
	 * @return ResponseEntity Machine Id which is inserted successfully
	 *         {@link ResponseEntity}
	 */
	@PostMapping("/v1.0/machines")
	@ApiOperation(value = "Service to save Machine", notes = "Saves Machine Detail and return Machine id", response = IdResponseDto.class)
	@ApiResponses({
			@ApiResponse(code = 201, message = "When Machine successfully created", response = IdResponseDto.class),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When No Machine found"),
			@ApiResponse(code = 500, message = "While creating Machine any error occured") })
	public ResponseEntity<IdResponseDto> createMachine(@Valid @RequestBody RequestDto<MachineDto> machine) {
		return new ResponseEntity<>(machineService.createMachine(machine), HttpStatus.CREATED);
	}

	/**
	 * Post API to update a row of Machine data
	 * 
	 * @param machine
	 *            input from user Machine DTO
	 * 
	 * @return ResponseEntity Machine Id which is update successfully
	 *         {@link ResponseEntity}
	 */
	@PutMapping("/v1.0/machines")
	@ApiOperation(value = "Service to update Machine", notes = "update Machine Detail and return Machine id", response = IdResponseDto.class)
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Machine successfully udated", response = IdResponseDto.class),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When No Machine found"),
			@ApiResponse(code = 500, message = "While updating Machine any error occured") })
	public ResponseEntity<IdResponseDto> updateMachine(@Valid @RequestBody RequestDto<MachineDto> machine) {
		return new ResponseEntity<>(machineService.updateMachine(machine), HttpStatus.OK);
	}

	/**
	 * Post API to deleted a row of Machine data
	 * 
	 * @param id
	 *            input from user Machine id
	 * 
	 * @return ResponseEntity Machine Id which is deleted successfully
	 *         {@link ResponseEntity}
	 */
	@DeleteMapping("/v1.0/machines/{id}")
	@ApiOperation(value = "Service to delete Machine ", notes = "Delete Machine  and return Machine  Id ", response = IdResponseDto.class)
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Machine successfully deleted", response = IdResponseDto.class),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When No Machine found"),
			@ApiResponse(code = 500, message = "While deleting Machine any error occured") })
	public ResponseEntity<IdResponseDto> deleteMachine(@Valid @PathVariable("id") String id) {

		return new ResponseEntity<>(machineService.deleteMachine(id), HttpStatus.OK);
	}

}
