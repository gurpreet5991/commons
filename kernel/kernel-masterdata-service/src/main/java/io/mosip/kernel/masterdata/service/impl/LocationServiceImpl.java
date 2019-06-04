package io.mosip.kernel.masterdata.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.masterdata.constant.LocationErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.LocationDto;
import io.mosip.kernel.masterdata.dto.getresponse.LocationHierarchyDto;
import io.mosip.kernel.masterdata.dto.getresponse.LocationHierarchyResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.LocationResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.StatusResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.PostLocationCodeResponseDto;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.LocationRepository;
import io.mosip.kernel.masterdata.service.LocationService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 * Class will fetch Location details based on various parameters this class is
 * implemented from {@link LocationService}}
 * 
 * @author Srinivasan
 * @author Tapaswini
 * @since 1.0.0
 *
 */
@Service
public class LocationServiceImpl implements LocationService {

	/**
	 * creates an instance of repository class {@link LocationRepository}}
	 */
	@Autowired
	private LocationRepository locationRepository;

	private List<Location> childHierarchyList = null;
	private List<Location> hierarchyList = null;
	private List<Location> parentHierarchyList = null;

	/**
	 * This method will all location details from the Database. Refers to
	 * {@link LocationRepository} for fetching location hierarchy
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LocationService#getLocationDetails(java.
	 * lang.String)
	 */
	@Override
	public LocationHierarchyResponseDto getLocationDetails(String langCode) {
		List<LocationHierarchyDto> responseList = null;
		LocationHierarchyResponseDto locationHierarchyResponseDto = new LocationHierarchyResponseDto();
		List<Object[]> locations = null;
		try {

			locations = locationRepository.findDistinctLocationHierarchyByIsDeletedFalse(langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		if (!locations.isEmpty()) {

			responseList = MapperUtils.objectToDtoConverter(locations);

		} else {
			throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		locationHierarchyResponseDto.setLocations(responseList);
		return locationHierarchyResponseDto;
	}

	/**
	 * This method will fetch location hierarchy based on location code and language
	 * code Refers to {@link LocationRepository} for fetching location hierarchy
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return LocationHierarchyResponseDto-
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.LocationService#
	 * getLocationHierarchyByLangCode(java.lang.String, java.lang.String)
	 */
	@Override
	public LocationResponseDto getLocationHierarchyByLangCode(String locCode, String langCode) {
		List<Location> childList = null;
		List<Location> parentList = null;
		childHierarchyList = new ArrayList<>();
		parentHierarchyList = new ArrayList<>();
		LocationResponseDto locationHierarchyResponseDto = new LocationResponseDto();
		try {

			List<Location> locHierList = getLocationHierarchyList(locCode, langCode);
			if (locHierList != null && !locHierList.isEmpty()) {
				for (Location locationHierarchy : locHierList) {
					String currentParentLocCode = locationHierarchy.getParentLocCode();
					childList = getChildList(locCode, langCode);
					parentList = getParentList(currentParentLocCode, langCode);

				}
				locHierList.addAll(childList);
				locHierList.addAll(parentList);

				List<LocationDto> locationHierarchies = MapperUtils.mapAll(locHierList, LocationDto.class);
				locationHierarchyResponseDto.setLocations(locationHierarchies);

			} else {
				throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
						LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
		}

		catch (DataAccessException | DataAccessLayerException e) {

			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));

		}
		return locationHierarchyResponseDto;
	}

	/**
	 * Method creates location hierarchy data into the table based on the request
	 * parameter sent {@inheritDoc}
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LocationService#createLocationHierarchy(io
	 * .mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	@Transactional
	public PostLocationCodeResponseDto createLocationHierarchy(LocationDto locationRequestDto) {

		Location location = null;
		Location locationResultantEntity = null;
		PostLocationCodeResponseDto locationCodeDto = null;

		location = MetaDataUtils.setCreateMetaData(locationRequestDto, Location.class);
		try {
			locationResultantEntity = locationRepository.create(location);
		} catch (DataAccessLayerException | DataAccessException ex) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_INSERT_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_INSERT_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(ex));
		}

		locationCodeDto = MapperUtils.map(locationResultantEntity, PostLocationCodeResponseDto.class);
		return locationCodeDto;
	}

	/**
	 * {@inheritDoc}
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LocationService#updateLocationDetails(io.
	 * mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	@Transactional
	public PostLocationCodeResponseDto updateLocationDetails(LocationDto locationDto) {
		PostLocationCodeResponseDto postLocationCodeResponseDto = new PostLocationCodeResponseDto();
		CodeAndLanguageCodeID locationId = new CodeAndLanguageCodeID();
		locationId.setCode(locationDto.getCode());
		locationId.setLangCode(locationDto.getLangCode());

		try {
			Location location = locationRepository.findById(Location.class, locationId);

			if (location == null) {
				throw new RequestException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
						LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
			if (!locationDto.getIsActive() && findIsActiveInHierarchy(location)) {
				throw new RequestException(LocationErrorCode.LOCATION_CHILD_STATUS_EXCEPTION.getErrorCode(),
						LocationErrorCode.LOCATION_CHILD_STATUS_EXCEPTION.getErrorMessage());
			}
			location = MetaDataUtils.setUpdateMetaData(locationDto, location, true);
			locationRepository.update(location);
			MapperUtils.map(location, postLocationCodeResponseDto);

		} catch (DataAccessException | DataAccessLayerException ex) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_UPDATE_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_UPDATE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(ex));
		}

		return postLocationCodeResponseDto;
	}

	/**
	 * {@inheritDoc}
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LocationService#deleteLocationDetials(java
	 * .lang.String)
	 */
	@Override
	@Transactional
	public CodeResponseDto deleteLocationDetials(String locationCode) {
		List<Location> locations = null;
		CodeResponseDto codeResponseDto = new CodeResponseDto();
		try {
			locations = locationRepository.findByCode(locationCode);
			if (!locations.isEmpty()) {

				locations.stream().map(MetaDataUtils::setDeleteMetaData)
						.forEach(location -> locationRepository.update(location));

			} else {
				throw new RequestException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
						LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
			}

		} catch (DataAccessException | DataAccessLayerException ex) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_UPDATE_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_UPDATE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(ex));
		}
		codeResponseDto.setCode(locationCode);
		return codeResponseDto;
	}

	/**
	 * Method creates location hierarchy data into the table based on the request
	 * parameter sent {@inheritDoc}
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.LocationService#
	 * getLocationDataByHierarchyName(java.lang.String)
	 */
	@Override
	public LocationResponseDto getLocationDataByHierarchyName(String hierarchyName) {
		List<Location> locationlist = null;
		LocationResponseDto locationHierarchyResponseDto = new LocationResponseDto();
		try {
			locationlist = locationRepository.findAllByHierarchyNameIgnoreCase(hierarchyName);

		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}

		if (!(locationlist.isEmpty())) {
			List<LocationDto> hierarchyList = MapperUtils.mapAll(locationlist, LocationDto.class);
			locationHierarchyResponseDto.setLocations(hierarchyList);

		} else {

			throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		return locationHierarchyResponseDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.LocationService#
	 * getImmediateChildrenByLocCodeAndLangCode(java.lang.String, java.lang.String)
	 */
	@Override
	public LocationResponseDto getImmediateChildrenByLocCodeAndLangCode(String locCode, String langCode) {
		List<Location> locationlist = null;
		LocationResponseDto locationHierarchyResponseDto = new LocationResponseDto();
		try {
			locationlist = locationRepository.findLocationHierarchyByParentLocCodeAndLanguageCode(locCode, langCode);

		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}

		if (locationlist.isEmpty()) {
			throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		List<LocationDto> locationDtoList = MapperUtils.mapAll(locationlist, LocationDto.class);
		locationHierarchyResponseDto.setLocations(locationDtoList);
		return locationHierarchyResponseDto;
	}

	/**
	 * fetches location hierarchy details from database based on location code and
	 * language code
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<LocationHierarchy>
	 */
	private List<Location> getLocationHierarchyList(String locCode, String langCode) {

		return locationRepository.findLocationHierarchyByCodeAndLanguageCode(locCode, langCode);
	}

	/**
	 * fetches location hierarchy details from database based on parent location
	 * code and language code
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<LocationHierarchy>
	 */
	private List<Location> getLocationChildHierarchyList(String locCode, String langCode) {

		return locationRepository.findLocationHierarchyByParentLocCodeAndLanguageCode(locCode, langCode);

	}

	/**
	 * This method fetches child hierarchy details of the location based on location
	 * code
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<Location>
	 */
	private List<Location> getChildList(String locCode, String langCode) {

		if (locCode != null && !locCode.isEmpty()) {
			List<Location> childLocHierList = getLocationChildHierarchyList(locCode, langCode);
			childHierarchyList.addAll(childLocHierList);
			childLocHierList.parallelStream().filter(entity -> entity.getCode() != null && !entity.getCode().isEmpty())
					.map(entity -> getChildList(entity.getCode(), langCode)).collect(Collectors.toList());

		}

		return childHierarchyList;
	}

	/**
	 * This method fetches parent hierarchy details of the location based on parent
	 * Location code
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<LocationHierarcy>
	 */
	private List<Location> getParentList(String locCode, String langCode) {

		if (locCode != null && !locCode.isEmpty()) {
			List<Location> parentLocHierList = getLocationHierarchyList(locCode, langCode);
			parentHierarchyList.addAll(parentLocHierList);

			parentLocHierList.parallelStream()
					.filter(entity -> entity.getParentLocCode() != null && !entity.getParentLocCode().isEmpty())
					.map(entity -> getParentList(entity.getParentLocCode(), langCode)).collect(Collectors.toList());
		}

		return parentHierarchyList;
	}

	@Override
	public Map<Short, List<Location>> getLocationByLangCodeAndHierarchyLevel(String langCode, Short hierarchyLevel) {
		Map<Short, List<Location>> map = new TreeMap<>();
		List<Location> locations = locationRepository.getAllLocationsByLangCodeAndLevel(langCode, hierarchyLevel);
		if (!EmptyCheckUtils.isNullEmpty(locations)) {
			for (Location location : locations) {
				if (map.containsKey(location.getHierarchyLevel())) {
					map.get(location.getHierarchyLevel()).add(location);
				} else {
					List<Location> list = new ArrayList<>();
					list.add(location);
					map.put(location.getHierarchyLevel(), list);
				}
			}
			return map;
		} else {
			throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LocationService#validateLocationName(java.
	 * lang.String)
	 */
	@Override
	public StatusResponseDto validateLocationName(String locationName) {
		StatusResponseDto statusResponseDto = null;
		boolean isPresent = false;
		try {
			statusResponseDto = new StatusResponseDto();
			statusResponseDto.setStatus(MasterDataConstant.INVALID);
			isPresent = locationRepository.isLocationNamePresent(locationName);
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage());
		}
		if (isPresent) {
			statusResponseDto.setStatus(MasterDataConstant.VALID);
		}
		return statusResponseDto;
	}

	/**
	 * This method to find, is there any child of given Location isActive is true
	 * then return true and break the loop. Otherwise if all children of the given
	 * location are false the return false.
	 * 
	 * @param location
	 * @return boolean return true or false
	 */
	public boolean findIsActiveInHierarchy(Location location) {
		boolean flag = false;
		String locCode = location.getCode();
		String langCode = location.getLangCode();
		
		List<Location> childList = new ArrayList<>();
		hierarchyList = new ArrayList<>();
		childList = getIsActiveChildList(locCode, langCode);
		System.out.println("===children===="+childList.size());
		for(Location child1 : childList) {
			System.out.println("===children===="+child1);
		}
		/*for(Location child1 : childList) {
			System.out.println("===children===="+child1);
		}
		boolean flag = false;
		for (Location child : childList) {
			System.out.println("child is active===="+child.getIsActive());
			if (child.getIsActive()) {
				flag = true;
				break;
			}
		}*/
		return flag;
	}
	/**
	 * This method fetches child hierarchy details of the location based on location
	 * code, here child isActive can true or false
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<Location>
	 */
	private List<Location> getIsActiveChildList(String locCode, String langCode) {

		if (locCode != null && !locCode.isEmpty()) {
			List<Location> childLocHierList = getIsActiveLocationChildHierarchyList(locCode, langCode);
			hierarchyList.addAll(childLocHierList);
			if(childLocHierList!=null && !childLocHierList.isEmpty())
			childLocHierList.parallelStream().filter(entity -> entity.getCode() != null && !entity.getCode().isEmpty())
					.map(entity -> getIsActiveChildList(entity.getCode(), langCode)).collect(Collectors.toList());
			}
		return hierarchyList;
		
	}
	/**
	 * fetches location hierarchy details from database based on parent location
	 * code and language code, children's isActive is either true or false 
	 * 
	 * @param locCode
	 *            - location code
	 * @param langCode
	 *            - language code
	 * @return List<LocationHierarchy>
	 */
	private List<Location> getIsActiveLocationChildHierarchyList(String locCode, String langCode) {

		return locationRepository.findDistinctByparentLocCode(locCode, langCode);

	}
	
	
	
	

}
