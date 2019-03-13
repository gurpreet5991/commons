package io.mosip.registration.test.clientmachinemapping;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doNothing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.registration.audit.AuditFactoryImpl;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.Components;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dao.MachineMappingDAO;
import io.mosip.registration.dao.UserOnboardDAO;
import io.mosip.registration.dto.DeviceDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.dto.UserMachineMappingDTO;
import io.mosip.registration.entity.RegCenterDevice;
import io.mosip.registration.entity.RegCentreMachineDevice;
import io.mosip.registration.entity.RegDeviceMaster;
import io.mosip.registration.entity.RegDeviceSpec;
import io.mosip.registration.entity.RegDeviceType;
import io.mosip.registration.entity.UserDetail;
import io.mosip.registration.entity.UserMachineMapping;
import io.mosip.registration.entity.UserRole;
import io.mosip.registration.entity.id.RegCenterDeviceId;
import io.mosip.registration.entity.id.RegCentreMachineDeviceId;
import io.mosip.registration.entity.id.RegDeviceTypeId;
import io.mosip.registration.entity.id.UserMachineMappingID;
import io.mosip.registration.entity.id.UserRoleID;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.mapping.impl.MapMachineServiceImpl;
import io.mosip.registration.util.healthcheck.RegistrationSystemPropertiesChecker;

public class UserClientMachineMappingServiceTest {

	@Mock
	MachineMappingDAO machineMappingDAO;
	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	@InjectMocks
	MapMachineServiceImpl mapMachineServiceImpl;
	@Mock
	private UserOnboardDAO userOnboardDAO;
	@Mock
	private AuditFactoryImpl auditFactory;

	@Before
	public void initialize() throws IOException, URISyntaxException {
		doNothing().when(auditFactory).audit(Mockito.any(AuditEvent.class), Mockito.any(Components.class),
				Mockito.anyString(), Mockito.anyString());
		ReflectionTestUtils.setField(SessionContext.class, "sessionContext", null);
		SessionContext.getInstance().userContext().setUserId("mosip");
	}

	@Test
	public void view() throws RegBaseCheckedException {

		String machineID = RegistrationSystemPropertiesChecker.getMachineId();

		Mockito.when(machineMappingDAO.getStationID(Mockito.anyString())).thenReturn("StationID");

		Mockito.when(machineMappingDAO.getCenterID(Mockito.anyString())).thenReturn("CenterID107");

		List<UserDetail> userDetailsList = new ArrayList<>();

		UserMachineMappingID machineMappingID = new UserMachineMappingID();
		machineMappingID.setUserID("ID123456");
		machineMappingID.setMachineID(machineID);

		UserMachineMapping userMachineMapping = new UserMachineMapping();
		userMachineMapping.setIsActive(true);
		userMachineMapping.setUserMachineMappingId(machineMappingID);

		UserRoleID registrationUserRoleID = new UserRoleID();
		registrationUserRoleID.setRoleCode("101");

		UserRole registrationUserRole = new UserRole();
		registrationUserRole.setUserRoleID(registrationUserRoleID);

		Set<UserRole> userRole = new HashSet();
		userRole.add(registrationUserRole);

		Set<UserMachineMapping> userMachine = new HashSet();
		userMachine.add(userMachineMapping);

		UserDetail registrationUserDetail = new UserDetail();
		registrationUserDetail.setId("ID123456");
		registrationUserDetail.setName("Registration");
		registrationUserDetail.setUserMachineMapping(userMachine);
		registrationUserDetail.setUserRole(userRole);
		userDetailsList.add(registrationUserDetail);

		Mockito.when(machineMappingDAO.getUsers(Mockito.anyString())).thenReturn(userDetailsList);

		ResponseDTO res = mapMachineServiceImpl.view();

		Assert.assertEquals("User Data Fetched Successfully", res.getSuccessResponseDTO().getMessage());
	}

	@Test
	public void viewFailureTest() throws RegBaseCheckedException {
		RegBaseCheckedException baseCheckedException = new RegBaseCheckedException("101", "No record Found");
		Mockito.when(machineMappingDAO.getStationID(Mockito.anyString())).thenReturn(baseCheckedException.getMessage());
		ResponseDTO res = mapMachineServiceImpl.view();
		Assert.assertEquals("No Records Found", res.getErrorResponseDTOs().get(0).getMessage());
	}

	@Test
	public void viewRegBaseUncheckedExceptionTest() throws RegBaseCheckedException {
		Mockito.when(machineMappingDAO.getStationID(Mockito.anyString())).thenThrow(RegBaseUncheckedException.class);
		try {
			mapMachineServiceImpl.view();
		} catch (RegBaseUncheckedException regBaseUncheckedException) {
			Assert.assertNotNull(regBaseUncheckedException);
		}
	}

	@Test
	public void viewRegBaseCheckedExceptionTest() throws RegBaseCheckedException {
		Mockito.when(machineMappingDAO.getStationID(Mockito.anyString())).thenThrow(RegBaseCheckedException.class);
		try {
			mapMachineServiceImpl.view();
		} catch (RegBaseUncheckedException regBaseUncheckedException) {
			Assert.assertNotNull(regBaseUncheckedException);
		}
	}

	@Test
	public void updateTest() {
		UserMachineMappingDTO machineMappingDTO = new UserMachineMappingDTO("ID123", "Nm123", "ADmin", "ACTIVE",
				"CNTR123", "STN123", "MCHN123");

		ResponseDTO responseDTO = new ResponseDTO();
		SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
		successResponseDTO.setInfoType(RegistrationConstants.ALERT_INFORMATION);
		successResponseDTO.setMessage(RegistrationConstants.MACHINE_MAPPING_SUCCESS_MESSAGE);
		responseDTO.setSuccessResponseDTO(successResponseDTO);

		Mockito.when(machineMappingDAO.update(Mockito.any(UserMachineMapping.class)))
				.thenReturn(RegistrationConstants.MACHINE_MAPPING_UPDATED);
		Mockito.when(machineMappingDAO.findByID(Mockito.any())).thenReturn(null);

		Assert.assertSame(mapMachineServiceImpl.saveOrUpdate(machineMappingDTO).getSuccessResponseDTO().getMessage(),
				responseDTO.getSuccessResponseDTO().getMessage());
	}

	@Test
	public void saveTest() {
		UserMachineMappingDTO machineMappingDTO = new UserMachineMappingDTO("ID123", "Nm123", "ADmin", "IN-ACTIVE",
				"CNTR123", "STN123", "MCHN123");
		UserMachineMapping user = new UserMachineMapping();

		ResponseDTO responseDTO = new ResponseDTO();
		SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
		successResponseDTO.setMessage(RegistrationConstants.MACHINE_MAPPING_SUCCESS_MESSAGE);
		responseDTO.setSuccessResponseDTO(successResponseDTO);

		Mockito.when(machineMappingDAO.save(Mockito.any(UserMachineMapping.class)))
				.thenReturn(RegistrationConstants.MACHINE_MAPPING_UPDATED);
		Mockito.when(machineMappingDAO.findByID(Mockito.any())).thenReturn(user);

		Assert.assertSame(mapMachineServiceImpl.saveOrUpdate(machineMappingDTO).getSuccessResponseDTO().getMessage(),
				responseDTO.getSuccessResponseDTO().getMessage());
	}

	@Test
	public void saveOrUpdateFailureTest() {
		UserMachineMappingDTO machineMappingDTO = new UserMachineMappingDTO("ID123", "Nm123", "ADmin", "IN-ACTIVE",
				"CNTR123", "STN123", "MCHN123");
		UserMachineMapping user = new UserMachineMapping();

		ResponseDTO responseDTO = new ResponseDTO();
		SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
		successResponseDTO.setInfoType(RegistrationConstants.ALERT_INFORMATION);
		successResponseDTO.setMessage(RegistrationConstants.MACHINE_MAPPING_SUCCESS_MESSAGE);
		responseDTO.setSuccessResponseDTO(successResponseDTO);

		Mockito.when(machineMappingDAO.findByID(Mockito.any())).thenThrow(RegBaseUncheckedException.class);
		Assert.assertEquals(
				mapMachineServiceImpl.saveOrUpdate(machineMappingDTO).getErrorResponseDTOs().get(0).getMessage(),
				"Unable to map user");
	}

	@Test
	public void getAllDeviceTypesTest() {

		// Add Device Types
		List<RegDeviceType> deviceTypes = new ArrayList<>();
		RegDeviceType deviceType = new RegDeviceType();
		RegDeviceTypeId deviceTypeId = new RegDeviceTypeId();

		deviceTypeId.setCode("FRS");
		deviceTypeId.setLangCode("eng");
		deviceType.setRegDeviceTypeId(deviceTypeId);
		deviceTypes.add(deviceType);

		deviceType = new RegDeviceType();
		deviceTypeId = new RegDeviceTypeId();
		deviceTypeId.setCode("IRS");
		deviceTypeId.setLangCode("eng");
		deviceType.setRegDeviceTypeId(deviceTypeId);
		deviceTypes.add(deviceType);

		deviceType = new RegDeviceType();
		deviceTypeId = new RegDeviceTypeId();
		deviceTypeId.setCode("SCN");
		deviceTypeId.setLangCode("eng");
		deviceType.setRegDeviceTypeId(deviceTypeId);
		deviceTypes.add(deviceType);

		// Mock DAO Call
		Mockito.when(machineMappingDAO.getAllDeviceTypes()).thenReturn(deviceTypes);

		List<String> types = mapMachineServiceImpl.getAllDeviceTypes();

	}

	@Test(expected = RegBaseUncheckedException.class)
	public void getAllDeviceTypesTestException() {
		// Mock DAO Call
		Mockito.when(machineMappingDAO.getAllDeviceTypes()).thenReturn(null);

		mapMachineServiceImpl.getAllDeviceTypes();
	}

	@Test(expected = RegBaseUncheckedException.class)
	public void getDeviceMappingListTestException() {

		Mockito.when(machineMappingDAO.getAllMappedDevices(Mockito.anyString(), Mockito.anyString()))
				.thenThrow(new RuntimeException("msg"));
		Mockito.when(machineMappingDAO.getAllValidDevicesByCenterId(Mockito.anyString())).thenReturn(new ArrayList<>());

		mapMachineServiceImpl.getDeviceMappingList("R001", "M001");

	}

	@Test
	public void getDeviceMappingListTest() {
		RegDeviceType deviceType = new RegDeviceType();
		RegDeviceTypeId deviceTypeId = new RegDeviceTypeId();
		RegDeviceSpec regDeviceSpec = new RegDeviceSpec();
		RegDeviceMaster regDeviceMaster = new RegDeviceMaster();
		List<RegCentreMachineDevice> devicesMapped = new ArrayList<>();
		RegCentreMachineDevice centreMachineDevice = new RegCentreMachineDevice();
		RegCentreMachineDeviceId centreMachineDeviceId = new RegCentreMachineDeviceId();

		deviceTypeId.setCode("Fingerprint");
		deviceType.setRegDeviceTypeId(deviceTypeId);
		regDeviceSpec.setRegDeviceType(deviceType);
		regDeviceSpec.setBrand("BrandA");
		regDeviceSpec.setModel("BM001");
		regDeviceMaster.setSerialNum("S001");
		regDeviceMaster.setRegDeviceSpec(regDeviceSpec);
		centreMachineDeviceId.setDeviceId("D001");
		centreMachineDeviceId.setMachineId("M001");
		centreMachineDeviceId.setRegCentreId("R001");
		centreMachineDevice.setRegCentreMachineDeviceId(centreMachineDeviceId);
		centreMachineDevice.setRegDeviceMaster(regDeviceMaster);

		devicesMapped.add(centreMachineDevice);

		centreMachineDevice = new RegCentreMachineDevice();
		centreMachineDeviceId = new RegCentreMachineDeviceId();
		centreMachineDeviceId.setDeviceId("D002");
		centreMachineDeviceId.setMachineId("M001");
		centreMachineDeviceId.setRegCentreId("R001");
		centreMachineDevice.setRegCentreMachineDeviceId(centreMachineDeviceId);
		centreMachineDevice.setRegDeviceMaster(regDeviceMaster);

		devicesMapped.add(centreMachineDevice);

		List<RegCenterDevice> centerDevices = new ArrayList<>();
		RegCenterDevice centerDevice = new RegCenterDevice();
		RegCenterDeviceId regCenterDeviceId = new RegCenterDeviceId();

		regCenterDeviceId.setDeviceId("D002");
		regCenterDeviceId.setRegCenterId("R001");
		centerDevice.setRegCenterDeviceId(regCenterDeviceId);
		centerDevice.setRegDeviceMaster(regDeviceMaster);

		centerDevices.add(centerDevice);

		Mockito.when(machineMappingDAO.getAllMappedDevices(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(devicesMapped);
		Mockito.when(machineMappingDAO.getAllValidDevicesByCenterId(Mockito.anyString())).thenReturn(centerDevices);

		Map<String, List<DeviceDTO>> devicesMap = mapMachineServiceImpl.getDeviceMappingList("R001", "M001");

		Map<String, List<DeviceDTO>> expectedDevicesMap = new HashMap<>();
		List<DeviceDTO> deviceDTOs = new ArrayList<>();
		DeviceDTO deviceDTO = new DeviceDTO();
		deviceDTO.setDeviceId("D001");
		deviceDTO.setDeviceType("Fingerprint");
		deviceDTO.setMachineId("M001");
		deviceDTO.setManufacturerName("BrandA");
		deviceDTO.setModelName("BM001");
		deviceDTO.setRegCenterId("R001");
		deviceDTO.setSerialNo("S001");
		deviceDTOs.add(deviceDTO);

		deviceDTO = new DeviceDTO();
		deviceDTO.setDeviceId("D002");
		deviceDTO.setDeviceType("Fingerprint");
		deviceDTO.setMachineId("M001");
		deviceDTO.setManufacturerName("BrandA");
		deviceDTO.setModelName("BM001");
		deviceDTO.setRegCenterId("R001");
		deviceDTO.setSerialNo("S001");
		deviceDTOs.add(deviceDTO);

		expectedDevicesMap.put(RegistrationConstants.ONBOARD_MAPPED_DEVICES, deviceDTOs);
		expectedDevicesMap.put(RegistrationConstants.ONBOARD_AVAILABLE_DEVICES, new ArrayList<>());

		Assert.assertThat(devicesMap, is(expectedDevicesMap));

	}

	@Test
	public void updateMappedDeviceTest() {
		List<DeviceDTO> deleteDevices = new ArrayList<>();
		List<DeviceDTO> mappedDevices = new ArrayList<>();
		DeviceDTO deviceDTO = new DeviceDTO();

		deviceDTO.setDeviceId("D001");
		deviceDTO.setMachineId("M001");
		deviceDTO.setRegCenterId("R001");
		deleteDevices.add(deviceDTO);

		deviceDTO = new DeviceDTO();
		deviceDTO.setDeviceId("D002");
		deviceDTO.setMachineId("M001");
		deviceDTO.setRegCenterId("R001");
		deleteDevices.add(deviceDTO);

		deviceDTO = new DeviceDTO();
		deviceDTO.setDeviceId("D003");
		deviceDTO.setMachineId("M001");
		deviceDTO.setRegCenterId("R001");
		mappedDevices.add(deviceDTO);

		Mockito.doNothing().when(machineMappingDAO).addedMappedDevice(Mockito.anyListOf(RegCentreMachineDevice.class));
		Mockito.doNothing().when(machineMappingDAO)
				.deleteUnMappedDevice(Mockito.anyListOf(RegCentreMachineDevice.class));

		mapMachineServiceImpl.updateMappedDevice(deleteDevices, mappedDevices);

	}

	@Test
	public void updateMappedDeviceTestException() {
		List<DeviceDTO> deleteDevices = new ArrayList<>();
		List<DeviceDTO> mappedDevices = new ArrayList<>();
		DeviceDTO deviceDTO = new DeviceDTO();

		deviceDTO.setDeviceId("D001");
		deviceDTO.setRegCenterId("R001");
		deleteDevices.add(deviceDTO);

		deviceDTO = new DeviceDTO();
		deviceDTO.setDeviceId("D002");
		deviceDTO.setRegCenterId("R001");
		deleteDevices.add(deviceDTO);

		deviceDTO = null;
		mappedDevices.add(deviceDTO);

		Mockito.doNothing().when(machineMappingDAO).addedMappedDevice(Mockito.anyListOf(RegCentreMachineDevice.class));
		Mockito.doNothing().when(machineMappingDAO)
				.deleteUnMappedDevice(Mockito.anyListOf(RegCentreMachineDevice.class));

		mapMachineServiceImpl.updateMappedDevice(deleteDevices, mappedDevices);

	}

}
