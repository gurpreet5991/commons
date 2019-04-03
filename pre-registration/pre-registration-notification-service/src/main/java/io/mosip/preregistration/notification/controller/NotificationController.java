package io.mosip.preregistration.notification.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.NotificationDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.notification.dto.QRCodeResponseDTO;
import io.mosip.preregistration.notification.service.NotificationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller class for notification triggering.
 * 
 * @author Sanober Noor
 * @since 1.0.0
 */
@RestController
@RequestMapping("/")
@CrossOrigin("*")
public class NotificationController {

	/**
	 * Reference to {@link NotificationService}.
	 */
	@Autowired
	private NotificationService notificationService;
	
	private Logger log = LoggerConfiguration.logConfig(NotificationController.class);

	/**
	 * Api to Trigger notification.
	 * 
	 * @param jsonbObject
	 *            the json string.
	 * @param langCode
	 *            the language code.
	 * @param file
	 *            the file to send.
	 * @return the response entity.
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PostMapping(path = "/notify", consumes = {
			"multipart/form-data" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Trigger notification")
	public ResponseEntity<MainResponseDTO<NotificationDTO>> sendNotification(
			@RequestPart(value = "NotificationDTO", required = true) String jsonbObject,
			@RequestPart(value = "langCode", required = true) String langCode,
			@RequestPart(value = "file", required = false) MultipartFile file) {
		log.info("sessionId", "idType", "id",
				"In notification controller for send notification with request notification dto " + jsonbObject);
		return new ResponseEntity<>(notificationService.sendNotification(jsonbObject, langCode, file), HttpStatus.OK);
	}
	
	/**
	 * @param Json Stirng data
	 * @return the response entity
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@PostMapping(path="/generateQRCode")
	public ResponseEntity<MainResponseDTO<QRCodeResponseDTO>> generateQRCode(@RequestBody String data) {
		log.info("sessionId", "idType", "id",
				"In notification controller for generateQRCode generation with request " + data);
		return  new ResponseEntity<>( notificationService.generateQRCode(data),HttpStatus.OK);
		
	}
	
	
}
