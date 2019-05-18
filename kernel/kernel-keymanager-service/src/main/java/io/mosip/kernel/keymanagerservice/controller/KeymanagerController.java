package io.mosip.kernel.keymanagerservice.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.keymanagerservice.dto.EncryptDataRequestDto;
import io.mosip.kernel.keymanagerservice.dto.EncryptDataResponseDto;
import io.mosip.kernel.keymanagerservice.dto.PublicKeyResponse;
import io.mosip.kernel.keymanagerservice.dto.SignatureRequestDto;
import io.mosip.kernel.keymanagerservice.dto.SignatureResponseDto;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyRequestDto;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyResponseDto;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

/**
 * This class provides controller methods for Key manager.
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@CrossOrigin
@RestController
@Api(tags = { "keymanager" }, value = "Operation related to Keymanagement")
public class KeymanagerController {

	/**
	 * Instance for KeymanagerService
	 */
	@Autowired
	KeymanagerService keymanagerService;

	/**
	 * Request mapping to get Public Key
	 * 
	 * @param applicationId Application id of the application requesting publicKey
	 * @param timeStamp     Timestamp of the request
	 * @param referenceId   Reference id of the application requesting publicKey
	 * @return {@link PublicKeyResponse} instance
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_PROCESSOR','REGISTRATION_ADMIN','REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','ID_AUTHENTICATION','TEST')")
	@ResponseFilter
	@GetMapping(value = "/publickey/{applicationId}")
	public ResponseWrapper<PublicKeyResponse<String>> getPublicKey(
			@ApiParam("Id of application") @PathVariable("applicationId") String applicationId,
			@ApiParam("Timestamp as metadata") @RequestParam("timeStamp") String timeStamp,
			@ApiParam("Refrence Id as metadata") @RequestParam("referenceId") Optional<String> referenceId) {

		ResponseWrapper<PublicKeyResponse<String>> response = new ResponseWrapper<>();
		response.setResponse(keymanagerService.getPublicKey(applicationId, timeStamp, referenceId));

		return response;
	}

	/**
	 * Request mapping to decrypt symmetric key
	 * 
	 * @param symmetricKeyRequestDto having encrypted symmetric key
	 * 
	 * @return {@link SymmetricKeyResponseDto} symmetricKeyResponseDto
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL','ID_AUTHENTICATION','TEST', 'REGISTRATION_ADMIN', 'REGISTRATION_SUPERVISOR', 'REGISTRATION_OFFICER', 'REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@PostMapping(value = "/decrypt")
	public ResponseWrapper<SymmetricKeyResponseDto> decryptSymmetricKey(
			@ApiParam("Data to decrypt in BASE64 encoding with meta-data") @RequestBody RequestWrapper<SymmetricKeyRequestDto> symmetricKeyRequestDto) {

		ResponseWrapper<SymmetricKeyResponseDto> response = new ResponseWrapper<>();
		response.setResponse(keymanagerService.decryptSymmetricKey(symmetricKeyRequestDto.getRequest()));
		return response;
	}

	// TODO: To be removed added for debugging
	@ApiIgnore
	@GetMapping(value = "/alias")
	public ResponseEntity<List<String>> getAllAlias() {
		return new ResponseEntity<>(keymanagerService.getAllAlias(), HttpStatus.OK);
	}

	/**
	 *  Encrypt data with private key
	 * @param encryptDataRequestDto
	 * @return {@link EncryptDataResponseDto}
	 */
	@ResponseFilter
	@ApiOperation(value = "Encrypt data")
	@PostMapping("/encrypt")
	public ResponseWrapper<EncryptDataResponseDto> encrypt(
			@RequestBody RequestWrapper<EncryptDataRequestDto> encryptDataRequestDto) {
		ResponseWrapper<EncryptDataResponseDto> response = new ResponseWrapper<>();
		response.setResponse(keymanagerService.encrypt(encryptDataRequestDto.getRequest()));
		return response;

	}
	
	
	@ResponseFilter
	@ApiOperation(value = "Sign Data Using Certificate")
	@PostMapping("/sign")
	public ResponseWrapper<SignatureResponseDto> signature(
			@RequestBody RequestWrapper<SignatureRequestDto> signatureResponseDto) {
		ResponseWrapper<SignatureResponseDto> response = new ResponseWrapper<>();
		response.setResponse(keymanagerService.sign(signatureResponseDto.getRequest()));
		return response;
    }
	
	@ResponseFilter
	@ApiOperation(value = "Validate Signature Data Using Certificate")
	@PostMapping("validate/sign")
	public ResponseWrapper<SignatureResponseDto> validateSignature(
			@RequestBody RequestWrapper<SignatureRequestDto> signatureResponseDto) {
		ResponseWrapper<SignatureResponseDto> response = new ResponseWrapper<>();
		response.setResponse(keymanagerService.validate(signatureResponseDto.getRequest()));
		return response;
    }
}
