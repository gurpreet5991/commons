package io.mosip.kernel.masterdata.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.masterdata.dto.RegistrationCenterDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterHolidayDto;
import io.mosip.kernel.masterdata.dto.RequestDto;
import io.mosip.kernel.masterdata.dto.getresponse.RegistrationCenterResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResgistrationCenterStatusResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.IdResponseDto;
import io.mosip.kernel.masterdata.service.RegistrationCenterService;
import io.swagger.annotations.Api;

/**
 * This controller class provides registration centers details based on user
 * provided data.
 * 
 * @author Dharmesh Khandelwal
 * @author Abhishek Kumar
 * @author Urvil Joshi
 * @author Ritesh Sinha
 * @author Sagar Mahapatra
 * @author Sidhant Agarwal
 * @author Srinivasan
 * @author Uday Kumar
 * @since 1.0.0
 *
 */
@RestController
@Api(tags = { "Registration" })
public class RegistrationCenterController {

	/**
	 * Reference to RegistrationCenterService.
	 */
	@Autowired
	RegistrationCenterService registrationCenterService;

	/**
	 * Function to fetch registration centers list using location code and language
	 * code.
	 * 
	 * @param langCode
	 *            language code for which the registration center needs to be
	 *            searched.
	 * @param locationCode
	 *            location code for which the registration center needs to be
	 *            searched.
	 * @return {@link RegistrationCenterResponseDto}.
	 */
	@GetMapping("/v1.0/getlocspecificregistrationcenters/{langcode}/{locationcode}")
	public RegistrationCenterResponseDto getRegistrationCenterDetailsByLocationCode(
			@PathVariable("langcode") String langCode, @PathVariable("locationcode") String locationCode) {
		return registrationCenterService.getRegistrationCentersByLocationCodeAndLanguageCode(locationCode, langCode);
	}

	/**
	 * Function to fetch specific registration center holidays by registration
	 * center id , year and language code.
	 * 
	 * @param langCode
	 *            langCode of required center.
	 * @param registrationCenterId
	 *            centerId of required center
	 * @param year
	 *            the year provided by user.
	 * @return {@link RegistrationCenterHolidayDto}
	 */
	@GetMapping("/v1.0/getregistrationcenterholidays/{langcode}/{registrationcenterid}/{year}")
	public RegistrationCenterHolidayDto getRegistrationCenterHolidays(@PathVariable("langcode") String langCode,
			@PathVariable("registrationcenterid") String registrationCenterId, @PathVariable("year") int year) {
		return registrationCenterService.getRegistrationCenterHolidays(registrationCenterId, year, langCode);
	}

	/**
	 * Function to fetch nearby registration centers using coordinates
	 * 
	 * @param langCode
	 *            langCode of required centers.
	 * @param longitude
	 *            the longitude provided by user.
	 * @param latitude
	 *            the latitude provided by user.
	 * @param proximityDistance
	 *            the proximity distance provided by user.
	 * @return {@link RegistrationCenterResponseDto}
	 */
	@GetMapping("/v1.0/getcoordinatespecificregistrationcenters/{langcode}/{longitude}/{latitude}/{proximitydistance}")
	public RegistrationCenterResponseDto getCoordinateSpecificRegistrationCenters(
			@PathVariable("langcode") String langCode, @PathVariable("longitude") double longitude,
			@PathVariable("latitude") double latitude, @PathVariable("proximitydistance") int proximityDistance) {
		return registrationCenterService.getRegistrationCentersByCoordinates(longitude, latitude, proximityDistance,
				langCode);
	}

	/**
	 * Function to fetch registration center using centerId and language code.
	 * 
	 * @param registrationCenterId
	 *            centerId of required center.
	 * @param langCode
	 *            langCode of required center.
	 * @return {@link RegistrationCenterResponseDto}
	 */
	@GetMapping("/v1.0/registrationcenters/{id}/{langcode}")
	public RegistrationCenterResponseDto getSpecificRegistrationCenterById(
			@PathVariable("id") String registrationCenterId, @PathVariable("langcode") String langCode) {
		return registrationCenterService.getRegistrationCentersByIDAndLangCode(registrationCenterId, langCode);
	}

	/**
	 * Function to fetch all registration centers.
	 * 
	 * @return {@link RegistrationCenterResponseDto}
	 */
	@GetMapping("/v1.0/registrationcenters")
	public RegistrationCenterResponseDto getAllRegistrationCentersDetails() {
		return registrationCenterService.getAllRegistrationCenters();
	}

	/**
	 * Function to fetch list of registration centers based on hierarchy level,text
	 * and language code
	 * 
	 * @param langCode
	 *            input from user
	 * @param hierarchyLevel
	 *            input from user
	 * @param name
	 *            input from user
	 * @return {@link RegistrationCenterResponseDto}
	 */
	@GetMapping("/v1.0/registrationcenters/{langcode}/{hierarchylevel}/{name}")
	public RegistrationCenterResponseDto getRegistrationCenterByHierarchyLevelAndTextAndlangCode(
			@PathVariable("langcode") String langCode, @PathVariable("hierarchylevel") Integer hierarchyLevel,
			@PathVariable("name") String name) {
		return registrationCenterService.findRegistrationCenterByHierarchyLevelandTextAndLanguageCode(langCode,
				hierarchyLevel, name);

	}

	/**
	 * Check whether the time stamp sent for the given registration center id is not
	 * a holiday and is in between working hours.
	 * 
	 * @param regId
	 *            - registration id
	 * @param timeStamp
	 *            - timestamp based on the format YYYY-MM-ddTHH:mm:ss.SSSZ
	 * @return RegistrationCenterStatusResponseDto
	 */
	@GetMapping("/v1.0/registrationcenters/validate/{id}/{langCode}/{timestamp}")
	public ResgistrationCenterStatusResponseDto validateTimestamp(@PathVariable("id") String regId,
			@PathVariable("langCode") String langCode, @PathVariable("timestamp") String timeStamp) {
		return registrationCenterService.validateTimeStampWithRegistrationCenter(regId, langCode, timeStamp);

	}

	/**
	 * This method creates registration center.
	 * 
	 * @param registrationCenterDto
	 *            the request DTO for creating registration center.
	 * @return the response i.e. the id of the registration center created.
	 */
	@PostMapping("/v1.0/registrationcenters")
	public ResponseEntity<IdResponseDto> createRegistrationCenter(
			@RequestBody @Valid RequestDto<RegistrationCenterDto> registrationCenterDto) {
		return new ResponseEntity<>(registrationCenterService.createRegistrationCenter(registrationCenterDto),
				HttpStatus.OK);
	}

	/**
	 * This method updates registration center.
	 * 
	 * @param registrationCenterDto
	 *            the request DTO for updating registration center.
	 * @return the response i.e. the id of the registration center updated.
	 */
	@PutMapping("/v1.0/registrationcenters")
	public ResponseEntity<IdResponseDto> updateRegistrationCenter(
			@RequestBody @Valid RequestDto<RegistrationCenterDto> registrationCenterDto) {
		return new ResponseEntity<>(registrationCenterService.updateRegistrationCenter(registrationCenterDto),
				HttpStatus.OK);

	}

	@DeleteMapping("/v1.0/registrationcenters/{registrationCenterId}")
	public ResponseEntity<IdResponseDto> deleteRegistrationCenter(
			@PathVariable("registrationCenterId") String registrationCenterId) {
		return new ResponseEntity<>(registrationCenterService.deleteRegistrationCenter(registrationCenterId),
				HttpStatus.OK);

	}

	/**
	 * Function to fetch list of registration centers based on hierarchy level,List
	 * of text and language code
	 * 
	 * @param langCode
	 *            input from user
	 * @param hierarchyLevel
	 *            input from user
	 * @param names
	 *            input from user
	 * @return {@link RegistrationCenterResponseDto}
	 */
	@GetMapping("/v1.0/registrationcenters/{langcode}/{hierarchylevel}/names")
	public RegistrationCenterResponseDto getRegistrationCenterByHierarchyLevelAndListTextAndlangCode(
			@PathVariable("langcode") String langCode, @PathVariable("hierarchylevel") Integer hierarchyLevel,
			@RequestParam("name") List<String> names) {
		return registrationCenterService.findRegistrationCenterByHierarchyLevelAndListTextAndlangCode(langCode,
				hierarchyLevel, names);
	}

}
