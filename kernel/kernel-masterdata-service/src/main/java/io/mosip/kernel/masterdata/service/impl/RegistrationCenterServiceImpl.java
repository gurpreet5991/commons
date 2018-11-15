package io.mosip.kernel.masterdata.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.modelmapper.ConfigurationException;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.HolidayErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.RegistrationCenterErrorCode;
import io.mosip.kernel.masterdata.dto.HolidayDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterHolidayDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterResponseDto;
import io.mosip.kernel.masterdata.entity.Holiday;
import io.mosip.kernel.masterdata.entity.RegistrationCenter;
import io.mosip.kernel.masterdata.exception.HolidayFetchException;
import io.mosip.kernel.masterdata.exception.RegistrationCenterFetchException;
import io.mosip.kernel.masterdata.exception.RegistrationCenterMappingException;
import io.mosip.kernel.masterdata.exception.RegistrationCenterNotFoundException;
import io.mosip.kernel.masterdata.repository.HolidayRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.masterdata.service.RegistrationCenterService;
import io.mosip.kernel.masterdata.utils.ObjectMapperUtil;

/**
 * This service class contains methods that provides registration centers
 * details based on user provided data.
 * 
 * @author Dharmesh Khandelwal
 * @author Abhishek Kumar
 * @author Urvil Joshi
 * @author Ritesh Sinha
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@Service
public class RegistrationCenterServiceImpl implements RegistrationCenterService {

	/**
	 * Reference to model mapper.
	 */
	@Autowired
	ModelMapper modelMapper;

	/**
	 * Reference to RegistrationCenterRepository.
	 */
	@Autowired
	private RegistrationCenterRepository registrationCenterRepository;

	/**
	 * Reference to HolidayRepository.
	 */
	@Autowired
	private HolidayRepository holidayRepository;

	/**
	 * Reference to ObjectMapperUtil.
	 */
	@Autowired
	private ObjectMapperUtil mapperUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * getRegistrationCenterHolidays(java.lang.String, int, java.lang.String)
	 */
	@Override
	public RegistrationCenterHolidayDto getRegistrationCenterHolidays(String registrationCenterId, int year,
			String langCode) {
		RegistrationCenterHolidayDto registrationCenterHolidayResponse = null;
		RegistrationCenterDto registrationCenterDto = null;
		RegistrationCenter registrationCenter = null;
		List<HolidayDto> holidayDto = null;
		List<Holiday> holidays = null;
		String holidayLocationCode = "";

		Objects.requireNonNull(registrationCenterId);
		Objects.requireNonNull(year);
		Objects.requireNonNull(langCode);
		try {
			registrationCenter = registrationCenterRepository
					.findByIdAndLanguageCodeAndIsActiveTrueAndIsDeletedFalse(registrationCenterId, langCode);
		} catch (DataAccessException dataAccessException) {
			throw new RegistrationCenterFetchException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage());
		}
		if (registrationCenter == null) {
			throw new RegistrationCenterNotFoundException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		} else {
			try {
				registrationCenterDto = modelMapper.map(registrationCenter, RegistrationCenterDto.class);
			} catch (IllegalArgumentException | ConfigurationException | MappingException exception) {
				throw new RegistrationCenterMappingException(
						RegistrationCenterErrorCode.REGISTRATION_CENTER_MAPPING_EXCEPTION.getErrorCode(),
						RegistrationCenterErrorCode.REGISTRATION_CENTER_MAPPING_EXCEPTION.getErrorMessage());
			}
			try {
				holidayLocationCode = registrationCenterDto.getHolidayLocationCode();
				holidays = holidayRepository.findAllByLocationCodeYearAndLangCode(holidayLocationCode, langCode, year);
			} catch (DataAccessException dataAccessException) {
				throw new HolidayFetchException(HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorCode(),
						HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorMessage());

			}
			holidayDto = mapperUtil.mapHolidays(holidays);
		}
		registrationCenterHolidayResponse = new RegistrationCenterHolidayDto();
		registrationCenterHolidayResponse.setRegistrationCenter(registrationCenterDto);
		registrationCenterHolidayResponse.setHolidays(holidayDto);

		return registrationCenterHolidayResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * getRegistrationCentersByCoordinates(double, double, int, java.lang.String)
	 */
	@Override
	public RegistrationCenterResponseDto getRegistrationCentersByCoordinates(double longitude, double latitude,
			int proximityDistance, String langCode) {
		List<RegistrationCenter> centers = null;
		try {
			centers = registrationCenterRepository.findRegistrationCentersByLat(latitude, longitude,
					proximityDistance * MasterDataConstant.METERTOMILECONVERSION, langCode);
		} catch (DataAccessLayerException dataAccessLayerException) {
			throw new RegistrationCenterFetchException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage());
		}
		if (centers.isEmpty()) {
			throw new RegistrationCenterNotFoundException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}
		List<RegistrationCenterDto> registrationCenters = null;
		try {
			registrationCenters = modelMapper.map(centers, new TypeToken<List<RegistrationCenterDto>>() {
			}.getType());
		} catch (IllegalArgumentException | ConfigurationException | MappingException exception) {
			throw new RegistrationCenterMappingException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_MAPPING_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_MAPPING_EXCEPTION.getErrorMessage());
		}
		RegistrationCenterResponseDto registrationCenterResponseDto = new RegistrationCenterResponseDto();
		registrationCenterResponseDto.setRegistrationCenters(registrationCenters);
		return registrationCenterResponseDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * getRegistrationCentersByLocationCodeAndLanguageCode(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public RegistrationCenterResponseDto getRegistrationCentersByLocationCodeAndLanguageCode(String locationCode,
			String langCode) {
		List<RegistrationCenter> registrationCentersList = null;
		try {
			registrationCentersList = registrationCenterRepository
					.findByLocationCodeAndLanguageCodeAndIsActiveTrueAndIsDeletedFalse(locationCode, langCode);

		} catch (DataAccessLayerException dataAccessLayerException) {
			throw new RegistrationCenterFetchException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage());
		}
		if (registrationCentersList.isEmpty()) {
			throw new RegistrationCenterNotFoundException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}
		List<RegistrationCenterDto> registrationCentersDtoList = null;
		try {
			registrationCentersDtoList = modelMapper.map(registrationCentersList,
					new TypeToken<List<RegistrationCenterDto>>() {
					}.getType());
		} catch (IllegalArgumentException | ConfigurationException | MappingException exception) {
			throw new RegistrationCenterMappingException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_MAPPING_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_MAPPING_EXCEPTION.getErrorMessage());
		}
		RegistrationCenterResponseDto registrationCenterResponseDto = new RegistrationCenterResponseDto();
		registrationCenterResponseDto.setRegistrationCenters(registrationCentersDtoList);
		return registrationCenterResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * getRegistrationCentersByIDAndLangCode(java.lang.String, java.lang.String)
	 */
	@Override
	public RegistrationCenterResponseDto getRegistrationCentersByIDAndLangCode(String registrationCenterId,
			String langCode) {
		List<RegistrationCenterDto> registrationCenters = new ArrayList<>();
		RegistrationCenter registrationCenter = null;
		try {
			registrationCenter = registrationCenterRepository
					.findByIdAndLanguageCodeAndIsActiveTrueAndIsDeletedFalse(registrationCenterId, langCode);
		} catch (DataAccessLayerException dataAccessLayerException) {
			throw new RegistrationCenterFetchException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage());
		}
		if (registrationCenter == null) {
			throw new RegistrationCenterNotFoundException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}
		RegistrationCenterDto registrationCenterDto = null;
		try {
			registrationCenterDto = modelMapper.map(registrationCenter, RegistrationCenterDto.class);
		} catch (IllegalArgumentException | ConfigurationException | MappingException exception) {
			throw new RegistrationCenterMappingException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_MAPPING_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_MAPPING_EXCEPTION.getErrorMessage());
		}
		registrationCenters.add(registrationCenterDto);
		RegistrationCenterResponseDto response = new RegistrationCenterResponseDto();
		response.setRegistrationCenters(registrationCenters);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * getAllRegistrationCenters()
	 */
	@Override
	public RegistrationCenterResponseDto getAllRegistrationCenters() {
		List<RegistrationCenter> registrationCentersList = null;
		try {
			registrationCentersList = registrationCenterRepository.findAllByIsActiveTrueAndIsDeletedFalse(RegistrationCenter.class);
		} catch (DataAccessLayerException dataAccessLayerException) {
			throw new RegistrationCenterFetchException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage());
		}

		if (registrationCentersList.isEmpty()) {
			throw new RegistrationCenterNotFoundException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}

		List<RegistrationCenterDto> registrationCenters = null;
		try {
			registrationCenters = modelMapper.map(registrationCentersList,
					new TypeToken<List<RegistrationCenterDto>>() {
					}.getType());
		} catch (IllegalArgumentException | ConfigurationException | MappingException exception) {
			throw new RegistrationCenterMappingException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_MAPPING_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_MAPPING_EXCEPTION.getErrorMessage());
		}
		RegistrationCenterResponseDto registrationCenterResponseDto = new RegistrationCenterResponseDto();
		registrationCenterResponseDto.setRegistrationCenters(registrationCenters);
		return registrationCenterResponseDto;

	}
}
