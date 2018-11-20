package io.mosip.kernel.masterdata.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.masterdata.dto.DeviceLangCodeDtypeDto;
import io.mosip.kernel.masterdata.dto.DeviceSpecificationDto;
import io.mosip.kernel.masterdata.dto.DeviceTypeDto;
import io.mosip.kernel.masterdata.dto.HolidayDto;
import io.mosip.kernel.masterdata.dto.ReasonCategoryDto;
import io.mosip.kernel.masterdata.dto.ReasonListDto;
import io.mosip.kernel.masterdata.entity.DeviceSpecification;
import io.mosip.kernel.masterdata.entity.DeviceType;
import io.mosip.kernel.masterdata.entity.Holiday;
import io.mosip.kernel.masterdata.entity.HolidayId;
import io.mosip.kernel.masterdata.entity.ReasonCategory;

@Component
public class ObjectMapperUtil {
	@Autowired
	private ModelMapper mapper;

	public <D, T> D map(final T entity, Class<D> outCLass) {
		return mapper.map(entity, outCLass);
	}

	public <D, T> List<D> mapAll(final Collection<T> entityList, Class<D> outCLass) {
		return entityList.stream().map(entity -> mapper.map(entity, outCLass)).collect(Collectors.toList());
	}

	public List<HolidayDto> mapHolidays(List<Holiday> holidays) {
		Objects.requireNonNull(holidays);
		List<HolidayDto> holidayDtos = new ArrayList<>();
		holidays.forEach(holiday -> {
			LocalDate date = holiday.getHolidayId().getHolidayDate();
			HolidayId holidayId = holiday.getHolidayId();
			HolidayDto dto = new HolidayDto();
			dto.setHolidayId(String.valueOf(holidayId.getId()));
			dto.setHolidayDate(String.valueOf(date));
			dto.setHolidayName(holiday.getHolidayName());
			dto.setLanguageCode(holidayId.getLangCode());
			dto.setHolidayYear(String.valueOf(date.getYear()));
			dto.setHolidayMonth(String.valueOf(date.getMonth().getValue()));
			dto.setHolidayDay(String.valueOf(date.getDayOfWeek().getValue()));

			holidayDtos.add(dto);
		});

		return holidayDtos;
	}

	public List<ReasonCategoryDto> reasonConverter(List<ReasonCategory> reasonCategories) {
		Objects.requireNonNull(reasonCategories, "list cannot be null");
		List<ReasonCategoryDto> reasonCategoryDtos = null;
		reasonCategoryDtos = reasonCategories.stream()
				.map(reasonCategory -> new ReasonCategoryDto(reasonCategory.getCode(), reasonCategory.getName(),
						reasonCategory.getDescription(), reasonCategory.getLanguageCode(),
						mapAll(reasonCategory.getReasons(), ReasonListDto.class)))
				.collect(Collectors.toList());

		return reasonCategoryDtos;

	}

	public List<DeviceLangCodeDtypeDto> mapDeviceDto(List<Object[]> objects) {

		List<DeviceLangCodeDtypeDto> deviceLangCodeDtypeDtoList = new ArrayList<>();
		objects.forEach(arr -> {
			DeviceLangCodeDtypeDto deviceLangCodeDtypeDto = new DeviceLangCodeDtypeDto();
			deviceLangCodeDtypeDto.setId((String) arr[0]);
			deviceLangCodeDtypeDto.setName((String) arr[1]);
			deviceLangCodeDtypeDto.setMacAddress((String) arr[2]);
			deviceLangCodeDtypeDto.setSerialNum((String) arr[3]);
			deviceLangCodeDtypeDto.setIpAddress((String) arr[4]);
			deviceLangCodeDtypeDto.setDspecId((String) arr[5]);
			deviceLangCodeDtypeDto.setLangCode((String) arr[6]);
			deviceLangCodeDtypeDto.setActive((boolean) arr[7]);
			deviceLangCodeDtypeDto.setDeviceTypeCode((String) arr[8]);
			deviceLangCodeDtypeDtoList.add(deviceLangCodeDtypeDto);
		});
		return deviceLangCodeDtypeDtoList;
	}
	
	public List<DeviceTypeDto> mapDeviceTyepDto(List<DeviceType> deviceTypes){
		List<DeviceTypeDto> deviceTypeDtoList = new ArrayList<>();
		DeviceTypeDto deviceTypeDto = new DeviceTypeDto();
		
		for(DeviceType deviceType :deviceTypes) {

			deviceTypeDto.setName(deviceType.getName());
			deviceTypeDto.setDescription(deviceType.getDescription());
			deviceTypeDto.setCode(deviceType.getDeviceTypeId().getCode());
			deviceTypeDto.setLangCode(deviceType.getDeviceTypeId().getLangCode());	
			/*deviceTypeDto.setCode(deviceType.getCode());
			deviceTypeDto.setLangCode(deviceType.getLangCode());*/
		}
		deviceTypeDtoList.add(deviceTypeDto);
		return deviceTypeDtoList;
	}
	
	public List<DeviceSpecificationDto> mapDeviceSpecification(List<DeviceSpecification>  deviceSpecificationList){
		 List<DeviceSpecificationDto>  deviceSpecificationDtoList = new  ArrayList<>();
		 DeviceSpecificationDto deviceSpecificationDto= new DeviceSpecificationDto();
		
		 for(DeviceSpecification deviceSpecification :deviceSpecificationList) {

			 deviceSpecificationDto.setName(deviceSpecification.getName());
			 deviceSpecificationDto.setDescription(deviceSpecification.getDescription());
			 deviceSpecificationDto.setLangCode(deviceSpecification.getLangCode());	
			 deviceSpecificationDto.setBrand(deviceSpecification.getBrand());
			 deviceSpecificationDto.setDescription(deviceSpecification.getDescription());
			 deviceSpecificationDto.setDeviceTypeCode(deviceSpecification.getDeviceTypeCode());
			 deviceSpecificationDto.setModel(deviceSpecification.getModel());
			 deviceSpecificationDto.setMinDriverversion(deviceSpecification.getMinDriverversion()); 
			 
			}
		 deviceSpecificationDtoList.add(deviceSpecificationDto);
		return deviceSpecificationDtoList;
	}
}
