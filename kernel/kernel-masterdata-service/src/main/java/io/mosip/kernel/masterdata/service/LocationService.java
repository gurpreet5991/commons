package io.mosip.kernel.masterdata.service;

import java.util.List;
import java.util.Map;

import io.mosip.kernel.masterdata.dto.LocationDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.masterdata.dto.getresponse.LocationHierarchyResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.LocationResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.StatusResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.PostLocationCodeResponseDto;
import io.mosip.kernel.masterdata.entity.Location;

/**
 * Interface class from which various implementation can be performed
 * 
 * @author Srinivasan
 * @author Tapaswini
 *
 */
public interface LocationService {

	/**
	 * this method will fetch LocationHierarchyDetails
	 * 
	 * @param langCode
	 *            - language code
	 * @return LocationHierarchyResponseDto -location response
	 */
	public LocationHierarchyResponseDto getLocationDetails(String langCode);

	/**
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return location response dto
	 */
	public LocationResponseDto getLocationHierarchyByLangCode(String locCode, String langCode);

	/**
	 * 
	 * @param locationRequestDto
	 *            - location request object
	 * @return {@link PostLocationCodeResponseDto}
	 */
	public PostLocationCodeResponseDto createLocationHierarchy(RequestWrapper<LocationDto> locationRequestDto);

	/**
	 * 
	 * @param hierarchyName
	 *            - hierarchyName
	 * @return location response dto
	 */
	public LocationResponseDto getLocationDataByHierarchyName(String hierarchyName);

	/**
	 * 
	 * @param locationRequestDto
	 *            - location request DTO
	 * @return {@link PostLocationCodeResponseDto}
	 */
	public PostLocationCodeResponseDto updateLocationDetails(RequestWrapper<LocationDto> locationRequestDto);

	/**
	 * 
	 * @param locationCode
	 *            - location code
	 * @return {@link CodeResponseDto}
	 */
	public CodeResponseDto deleteLocationDetials(String locationCode);

	/**
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return location response dto
	 */
	public LocationResponseDto getImmediateChildrenByLocCodeAndLangCode(String locCode, String langCode);

	/**
	 * 
	 * @param langCode
	 *            - language code
	 * @param hierarchyLevel
	 *            - hierarchyLevel
	 * @return map contain key as parentCode and value as List of Location
	 * 
	 */
	public Map<Integer, List<Location>> getLocationByLangCodeAndHierarchyLevel(String langCode, Integer hierarchyLevel);
	
	/**
	 * checks whether the given location name is valid or not
	 * @param locationName
	 * @return StatusResponseDto
	 */
	public StatusResponseDto validateLocationName(String locationName);

}
