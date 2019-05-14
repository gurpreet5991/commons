package io.mosip.registration.processor.bio.dedupe.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.mosip.registration.processor.core.spi.biodedupe.BioDedupeService;
import io.mosip.registration.processor.core.token.validation.TokenValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The Class BioDedupeController.
 *
 * @author M1022006
 */
@RefreshScope
@RestController
@Api(tags = "Biodedupe")
public class BioDedupeController {

	/** The bio dedupe service. */
	@Autowired
	private BioDedupeService bioDedupeService;

	/** Token validator class */
	@Autowired
	TokenValidator tokenValidator;
	
	/**
	 * Gets the file.
	 *
	 * @param regId the reg id
	 * @return the file
	 */

	@GetMapping(path = "/biometricfile/{regId}", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
	@ApiOperation(value = "Get the CBEF XML file  of packet", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "CBEF Xml file is successfully fetched"),
			@ApiResponse(code = 400, message = "Unable to fetch the CBEF XML file"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<byte[]> getFile(@PathVariable("regId") String regId,
			@CookieValue(value = "Authorization", required = true) String token) {

		tokenValidator.validate("Authorization=" + token, "bio");
		byte[] file = bioDedupeService.getFile(regId);
		return ResponseEntity.status(HttpStatus.OK).body(file);

	}
}