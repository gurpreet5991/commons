package io.mosip.registration.processor.manual.verification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.registration.processor.core.constant.PacketFiles;
import io.mosip.registration.processor.core.packet.dto.PacketMetaInfo;
import io.mosip.registration.processor.manual.verification.dto.FileRequestDto;
import io.mosip.registration.processor.manual.verification.dto.ManualVerificationDTO;
import io.mosip.registration.processor.manual.verification.dto.PacketInfoRequestDto;
import io.mosip.registration.processor.manual.verification.dto.UserDto;
import io.mosip.registration.processor.manual.verification.service.ManualVerificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The controller class for Manual Adjudication.
 *
 * @author Shuchita
 * @author Pranav Kumar
 * @since 0.0.1
 */
@RestController
@RequestMapping("/v0.1/registration-processor/manual-verification")
@Api(tags = "Manual Adjudication")
@CrossOrigin
public class ManualVerificationController {
	
	/** The manual adjudication service. */
	@Autowired
	private ManualVerificationService manualAdjudicationService;

	/**
	 * Assign applicant.
	 *
	 * @param userDto the user dto
	 * @return the response entity
	 */
	@PostMapping(path = "/assignment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponse(code = 200, message = "status successfully updated")

	public ResponseEntity<ManualVerificationDTO> assignApplicant(@RequestBody(required = true) UserDto userDto) {
		ManualVerificationDTO manualVerificationDTO = manualAdjudicationService.assignApplicant(userDto);
		return ResponseEntity.status(HttpStatus.OK).body(manualVerificationDTO);
	}
	
	/**
	 * Update packet status.
	 *
	 * @param manualVerificationDTO the manual verification DTO
	 * @return the response entity
	 */
	@PostMapping(path = "/decision", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponse(code = 200, message = "status successfully updated")
	public ResponseEntity<ManualVerificationDTO> updatePacketStatus(@RequestBody(required = true) ManualVerificationDTO manualVerificationDTO) {
		ManualVerificationDTO updatedManualVerificationDTO = manualAdjudicationService.updatePacketStatus(manualVerificationDTO);
		return ResponseEntity.status(HttpStatus.OK).body(updatedManualVerificationDTO);
	}

	/**
	 * Gets the applicant biometric.
	 *
	 * @param dto the dto
	 * @return the applicant biometric
	 */
	@PostMapping(value = "/applicantBiometric")
	@ApiResponses({ @ApiResponse(code = 200, message = "file fetching successful"),
			@ApiResponse(code = 400, message = "Invalid file requested"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<byte[]> getApplicantBiometric(@RequestBody(required=true)FileRequestDto dto) {
		byte[] packetInfo = manualAdjudicationService.getApplicantFile(dto.getRegId(),dto.getFileName());
		return ResponseEntity.status(HttpStatus.OK).body(packetInfo);
	}
	
	/**
	 * Gets the applicant demographic.
	 *
	 * @param packetInfoRequestDto the packet info request dto
	 * @return the applicant demographic
	 */
	@PostMapping(value = "/applicantDemographic", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({ @ApiResponse(code = 200, message = "data fetching successful"),
			@ApiResponse(code = 400, message = "Invalid file requested"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<byte[]> getApplicantDemographic(@RequestBody(required=true)PacketInfoRequestDto packetInfoRequestDto) {
		byte[] packetInfo = manualAdjudicationService.getApplicantFile(packetInfoRequestDto.getRegId(), PacketFiles.ID.name());
		return ResponseEntity.status(HttpStatus.OK).body(packetInfo);
	}

	/**
	 * Gets the packet info.
	 *
	 * @param packetInfoRequestDto the packet info request dto
	 * @return the packet info
	 */
	@PostMapping(value = "/packetInfo")
	@ApiResponses({ @ApiResponse(code = 200, message = "data fetching successful"),
			@ApiResponse(code = 400, message = "Invalid file requested"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<PacketMetaInfo> getPacketInfo(@RequestBody(required=true)PacketInfoRequestDto packetInfoRequestDto) {
		PacketMetaInfo packetInfo = manualAdjudicationService.getApplicantPacketInfo(packetInfoRequestDto.getRegId());
		return ResponseEntity.status(HttpStatus.OK).body(packetInfo);
	}
}

