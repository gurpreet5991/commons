package io.mosip.registration.test.packetStatusSync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dao.RegPacketStatusDAO;
import io.mosip.registration.dao.RegistrationDAO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.entity.Registration;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.packet.impl.RegPacketStatusServiceImpl;
import io.mosip.registration.service.sync.PacketSynchService;
import io.mosip.registration.util.restclient.ServiceDelegateUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ io.mosip.registration.context.ApplicationContext.class })
public class RegPacketStatusServiceTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	@Mock
	private ServiceDelegateUtil serviceDelegateUtil;
	@Mock
	private RegPacketStatusDAO packetStatusDao;
	@Mock
	private PacketSynchService packetSynchService;
	@InjectMocks
	private RegPacketStatusServiceImpl packetStatusService;

	@Mock
	RegistrationDAO registrationDAO;
	
	@Before
	public void initiate() {
		Map<String,Object> applicationMap =new HashMap<>();
		applicationMap.put(RegistrationConstants.REG_DELETION_CONFIGURED_DAYS, "5");
	
		PowerMockito.mockStatic(io.mosip.registration.context.ApplicationContext.class);
		when(io.mosip.registration.context.ApplicationContext.map()).thenReturn(applicationMap);
		SessionContext.getInstance();

	}

	@AfterClass
	public static void destroy() {
		SessionContext.destroySession();
	}

	@Test
	public void packetSyncStatusSuccessTest()
			throws HttpClientErrorException, RegBaseCheckedException, SocketTimeoutException {
		List<LinkedHashMap<String, String>> registrations = new ArrayList<>();
		LinkedHashMap<String, String> registration = new LinkedHashMap<>();
		registration.put("registrationId", "12345");
		registration.put("statusCode", RegistrationConstants.PACKET_STATUS_CODE_PROCESSED);
		registrations.add(registration);

		LinkedHashMap<String, String> registration12 = new LinkedHashMap<>();

		registration12.put("registrationId", "12345");
		registration12.put("statusCode", RegistrationConstants.PACKET_STATUS_CODE_PROCESSED + "123");
		registrations.add(registration12);

		List<Registration> list = new LinkedList<>();
		Registration regis = new Registration();
		regis.setId("12345");
		regis.setAckFilename("..//PacketStore/02-Jan-2019/2018782130000102012019115112_Ack.png");
		regis.setClientStatusCode(RegistrationConstants.PACKET_STATUS_CODE_PROCESSED);
		list.add(regis);

		when(packetStatusDao.getPacketIdsByStatusUploaded()).thenReturn(list);

		when(serviceDelegateUtil.get(Mockito.anyString(), Mockito.anyMap(), Mockito.anyBoolean()))
				.thenReturn(registrations);
		Assert.assertNotNull(packetStatusService.packetSyncStatus().getSuccessResponseDTO());

		when(packetStatusDao.update(Mockito.any())).thenThrow(RuntimeException.class);
		packetStatusService.packetSyncStatus();

	}

	@Test
	public void packetSyncStatusFailureTest()
			throws HttpClientErrorException, RegBaseCheckedException, SocketTimeoutException {
		List<LinkedHashMap<String, String>> registrations = new ArrayList<>();
		when(serviceDelegateUtil.get(Mockito.anyString(), Mockito.anyMap(), Mockito.anyBoolean()))
				.thenReturn(registrations);
		Assert.assertNotNull(packetStatusService.packetSyncStatus().getErrorResponseDTOs());

		when(serviceDelegateUtil.get(Mockito.anyString(), Mockito.anyMap(), Mockito.anyBoolean()))
				.thenThrow(HttpClientErrorException.class);
		packetStatusService.packetSyncStatus();

	}

	@Test
	public void deleteReRegistrationPacketsTest() {
		List<Registration> list = new LinkedList<>();
		Registration regis = new Registration();
		regis.setId("12345");
		regis.setAckFilename("..//PacketStore/02-Jan-2019/2018782130000102012019115112_Ack.png");
		regis.setClientStatusCode(RegistrationConstants.PACKET_STATUS_CODE_PROCESSED);
		regis.setStatusCode(RegistrationConstants.PACKET_STATUS_CODE_PROCESSED);
		list.add(regis);
		SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
		successResponseDTO.setMessage(RegistrationConstants.REGISTRATION_DELETION_BATCH_JOBS_SUCCESS);

		when(registrationDAO.get(Mockito.any(),Mockito.anyString())).thenReturn(list);

		Mockito.doNothing().when(packetStatusDao).delete(Mockito.any());

		assertSame(successResponseDTO.getMessage(),
				packetStatusService.deleteRegistrationPackets().getSuccessResponseDTO().getMessage());

	}

	@Test
	public void deleteReRegistrationPacketsFailureTest() {
		List<Registration> list = new LinkedList<>();
		Registration regis = new Registration();
		regis.setId("12345");
		regis.setAckFilename("..//PacketStore/02-Jan-2019/2018782130000102012019115112_Ack.png");
		regis.setClientStatusCode(RegistrationConstants.PACKET_STATUS_CODE_PROCESSED);
		regis.setStatusCode(RegistrationConstants.PACKET_STATUS_CODE_PROCESSED);
		list.add(regis);

		when(registrationDAO.get(Mockito.any(),Mockito.anyString()))
				.thenThrow(RuntimeException.class);

		assertSame( RegistrationConstants.REGISTRATION_DELETION_BATCH_JOBS_FAILURE, packetStatusService.deleteRegistrationPackets().getErrorResponseDTOs().get(0).getMessage());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void syncPacketTest() throws HttpClientErrorException, ResourceAccessException, SocketTimeoutException, RegBaseCheckedException, JsonProcessingException, URISyntaxException {
		List<Registration> packetsToBeSynched=new ArrayList<>();
		Registration reg=new Registration();
		packetsToBeSynched.add(reg);
		Mockito.when(registrationDAO.getPacketsToBeSynched(Mockito.anyList())).thenReturn(packetsToBeSynched);
		Mockito.when(packetSynchService.syncPacketsToServer(Mockito.anyList())).thenReturn(new Object());
		Mockito.when(packetSynchService.updateSyncStatus(Mockito.anyList())).thenReturn(true);
		assertEquals("Success", packetStatusService.syncPacket().getSuccessResponseDTO().getMessage());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void syncPacketNegativeTest() {
		List<Registration> packetsToBeSynched=new ArrayList<>();
		Mockito.when(registrationDAO.getPacketsToBeSynched(Mockito.anyList())).thenReturn(packetsToBeSynched);
		assertEquals("Success", packetStatusService.syncPacket().getSuccessResponseDTO().getMessage());
	}

}
