package io.mosip.kernel.masterdata.test.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.MappingException;
import org.springframework.dao.DataRetrievalFailureException;

import io.mosip.kernel.masterdata.dto.DeviceDto;
import io.mosip.kernel.masterdata.dto.DeviceLangCodeDtypeDto;
import io.mosip.kernel.masterdata.dto.DeviceLangCodeResponseDto;
import io.mosip.kernel.masterdata.dto.DeviceResponseDto;
import io.mosip.kernel.masterdata.entity.Device;
import io.mosip.kernel.masterdata.exception.DeviceFetchException;
import io.mosip.kernel.masterdata.exception.DeviceMappingException;
import io.mosip.kernel.masterdata.exception.DeviceNotFoundException;
import io.mosip.kernel.masterdata.repository.DeviceRepository;
import io.mosip.kernel.masterdata.service.impl.DeviceServiceImpl;
import io.mosip.kernel.masterdata.utils.ObjectMapperUtil;

@RunWith(MockitoJUnitRunner.class)
public class DeviceServiceImplTest {

	@InjectMocks
	DeviceServiceImpl deviceServiceImpl;

	@Mock
	private DeviceRepository deviceRepository;

	@Mock
	private ObjectMapperUtil objectMapperUtil;

	@Before
	public void setUp() {
		deviceServiceImpl = new DeviceServiceImpl();
		MockitoAnnotations.initMocks(this);
	}

	List<Device> deviceList = new ArrayList<>();
	List<DeviceDto> deviceDtoList = new ArrayList<>();

	@Test
	public void testGetDeviceLangCode() {

		Device device = new Device();
		device.setId("1001");
		device.setName("Laptop");
		device.setLangCode("ENG");
		List<Device> deviceList = new ArrayList<>();
		deviceList.add(device);

		DeviceDto deviceDto = new DeviceDto();
		deviceDto.setId("1001");
		deviceDto.setName("Laptop");
		deviceDto.setName("ENG");
		List<DeviceDto> deviceDtoList = new ArrayList<>();
		deviceDtoList.add(deviceDto);
		Mockito.when(deviceRepository.findByLangCode(Mockito.anyString())).thenReturn(deviceList);
		Mockito.when(objectMapperUtil.mapAll(deviceList, DeviceDto.class)).thenReturn(deviceDtoList);
		DeviceResponseDto actual = deviceServiceImpl.getDeviceLangCode("ENG");
		assertNotNull(actual);
		assertTrue(actual.getDevices().size() > 0);
	}

	@Test(expected = DeviceNotFoundException.class)
	public void testGetDeviceLangCodeThrowsDeviceNotFoundException() {
		doReturn(null).when(deviceRepository).findByLangCode(Mockito.anyString());
		deviceServiceImpl.getDeviceLangCode("ENG");

	}

	@Test(expected = DeviceFetchException.class)
	public void testGetMachineDetailAllThrowsDataAccessExcetion() {
		Mockito.when(deviceRepository.findByLangCode(Mockito.anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		deviceServiceImpl.getDeviceLangCode("ENG");

	}

	@Test(expected = DeviceMappingException.class)
	public void testGetMachineDetailAllThrowsIllegalArgumentExcetion() {
		Device device = new Device();
		device.setId("1001");
		device.setName("Laptop");
		List<Device> deviceList = new ArrayList<>();
		deviceList.add(device);
		Mockito.when(deviceRepository.findByLangCode(Mockito.anyString())).thenReturn(deviceList);
		Mockito.when(objectMapperUtil.mapAll(deviceList, DeviceDto.class)).thenThrow(IllegalArgumentException.class);
		deviceServiceImpl.getDeviceLangCode("ENG");

	}

	@Test(expected = DeviceMappingException.class)
	public void testGetMachineDetailAllThrowsMappingExcetion() {
		Device device = new Device();
		device.setId("1001");
		device.setName("Laptop");
		List<Device> deviceList = new ArrayList<>();
		deviceList.add(device);
		Mockito.when(deviceRepository.findByLangCode(Mockito.anyString())).thenReturn(deviceList);
		Mockito.when(objectMapperUtil.mapAll(deviceList, DeviceDto.class)).thenThrow(MappingException.class);
		deviceServiceImpl.getDeviceLangCode("ENG");

	}

	@Test
	public void testGetDeviceLangCodeAndDeviceType() {

		Object[] objects = { "1001", "Laptop" };
		List<Object[]> objectList = new ArrayList<>();
		objectList.add(objects);

		DeviceLangCodeDtypeDto deviceLangCodeDtypeDto = new DeviceLangCodeDtypeDto();
		deviceLangCodeDtypeDto.setId("1001");
		deviceLangCodeDtypeDto.setName("Laptop");
		List<DeviceLangCodeDtypeDto> deviceLangCodeDtypeDtoList = new ArrayList<>();
		deviceLangCodeDtypeDtoList.add(deviceLangCodeDtypeDto);
		Mockito.when(deviceRepository.findByLangCodeAndDtypeCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(objectList);

		Mockito.when(objectMapperUtil.mapDeviceDto(Mockito.anyList())).thenReturn(deviceLangCodeDtypeDtoList);
		DeviceLangCodeResponseDto actual = deviceServiceImpl.getDeviceLangCodeAndDeviceType("ENG", "laptop_code");
		assertNotNull(actual);
		assertTrue(actual.getDevices().size() > 0);
	}

	@Test(expected = DeviceNotFoundException.class)
	public void testGetDeviceLangCodeAndDeviceTypeDeviceNotFoundException() {
		doReturn(null).when(deviceRepository).findByLangCodeAndDtypeCode("ENG", "laptop_code");
		deviceServiceImpl.getDeviceLangCodeAndDeviceType("ENG", "laptop_code");

	}

	@Test(expected = DeviceFetchException.class)
	public void testGetDeviceLangCodeAndDeviceTypeThrowsDataAccessExcetion() {
		Mockito.when(deviceRepository.findByLangCodeAndDtypeCode(Mockito.anyString(), Mockito.anyString()))
				.thenThrow(DataRetrievalFailureException.class);
		deviceServiceImpl.getDeviceLangCodeAndDeviceType("ENG", "laptop_code");

	}

	@Test(expected = DeviceMappingException.class)
	public void testGetDeviceLangCodeAndDeviceTypeThrowsMappingExcetion() {

		Object[] objects = { "1001", "Laptop" };
		List<Object[]> objectList = new ArrayList<>();
		objectList.add(objects);
		Mockito.when(deviceRepository.findByLangCodeAndDtypeCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(objectList);
		Mockito.when(objectMapperUtil.mapDeviceDto(Mockito.anyList())).thenThrow(MappingException.class);
		deviceServiceImpl.getDeviceLangCodeAndDeviceType("ENG", "laptop_code");
	}

	@Test(expected = DeviceMappingException.class)
	public void testGetDeviceLangCodeAndDeviceTypeThrowsIllegalArgumentException() {

		Object[] objects = { "1001", "Laptop" };
		List<Object[]> objectList = new ArrayList<>();
		objectList.add(objects);
		Mockito.when(deviceRepository.findByLangCodeAndDtypeCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(objectList);
		Mockito.when(objectMapperUtil.mapDeviceDto(Mockito.anyList())).thenThrow(IllegalArgumentException.class);
		deviceServiceImpl.getDeviceLangCodeAndDeviceType("ENG", "laptop_code");
	}

}
