package io.mosip.pregistration.datasync.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.pregistration.datasync.dto.ResponseDTO;
import io.mosip.pregistration.datasync.dto.ReverseDataSyncDTO;
import io.mosip.pregistration.datasync.service.DataSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Data Sync Controller
 * 
 * @author M1046129 - Jagadishwari
 *
 */
@RestController
@RequestMapping("/v0.1/pre-registration/data-sync/")
@Api(tags = "Data Sync")
@CrossOrigin("*")
public class DataSyncController {

	@Autowired
	private DataSyncService dataSyncService;

	/**
	 * @param preIds
	 * @return responseDto
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	@GetMapping(path = "/datasync", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Retrieve Pre-Registrations")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Data Sync records fetched"),
			@ApiResponse(code = 400, message = "Unable to fetch the records") })
	public ResponseEntity<ResponseDTO> retrievePreRegistrations(@RequestParam(value = "preId") String preId)
			throws Exception {
		ResponseDTO responseDto = dataSyncService.getPreRegistration(preId);
		return ResponseEntity.status(HttpStatus.OK).body(responseDto);
	}

	@PostMapping(path = "/reverseDataSync", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Store consumed Pre-Registrations")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Consumed Pre-Registrations saved"),
			@ApiResponse(code = 400, message = "Unable to save the records") })
	public ResponseEntity<ResponseDTO<ReverseDataSyncDTO>> storeConsumedPreRegistrationsIds(
			@RequestBody(required = true) ReverseDataSyncDTO consumedData)
			throws JsonParseException, JsonMappingException, IOException {
		System.out.println("consumedData:" + consumedData);
		// ReverseDataSyncDto reverseDataSyncDto = (ReverseDataSyncDto) JsonUtils
		// .jsonStringToJavaObject(ReverseDataSyncDto.class, consumedData);
		ResponseDTO<ReverseDataSyncDTO> responseDto = dataSyncService.storeConsumedPreRegistrations(consumedData);
		return ResponseEntity.status(HttpStatus.OK).body(responseDto);

	}

}
