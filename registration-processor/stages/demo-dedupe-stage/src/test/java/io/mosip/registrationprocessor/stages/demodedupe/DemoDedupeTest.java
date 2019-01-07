package io.mosip.registrationprocessor.stages.demodedupe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;

import io.mosip.authentication.core.dto.indauth.AuthResponseDTO;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.packet.dto.Identity;
import io.mosip.registration.processor.core.packet.dto.demographicinfo.DemographicInfoDto;
import io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager;
import io.mosip.registration.processor.core.spi.restclient.RegistrationProcessorRestClientService;
import io.mosip.registration.processor.filesystem.ceph.adapter.impl.FilesystemCephAdapterImpl;
import io.mosip.registration.processor.packet.storage.dao.PacketInfoDao;
import io.mosip.registration.processor.packet.storage.dto.ApplicantInfoDto;
import io.mosip.registration.processor.stages.demodedupe.DemoDedupe;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.net.ssl.*" })
@PrepareForTest({ IOUtils.class, HMACUtils.class })
public class DemoDedupeTest {

	@Mock
	private PacketInfoManager<Identity, ApplicantInfoDto> packetInfoManager;

	@Mock
	private PacketInfoDao packetInfoDao;

	@Mock
	private InputStream inputStream;

	@Mock
	FilesystemCephAdapterImpl filesystemCephAdapterImpl;

	@Mock
	AuthResponseDTO authResponseDTO = new AuthResponseDTO();

	@Mock
	RegistrationProcessorRestClientService<Object> restClientService;
	
	@Mock 
	  Environment env;
	
	@InjectMocks
	private DemoDedupe demoDedupe;

	@Before
	public void setUp() throws Exception {

		List<String> fingers = new ArrayList<>();
		fingers.add("LEFTTHUMB");
		fingers.add("LEFTINDEX");
		fingers.add("LEFTMIDDLE");
		fingers.add("LEFTLITTLE");
		fingers.add("LEFTRING");
		fingers.add("RIGHTTHUMB");
		fingers.add("RIGHTINDEX");
		fingers.add("RIGHTMIDDLE");
		fingers.add("RIGHTLITTLE");
		fingers.add("RIGHTRING");
		
		List<String> iris = new ArrayList<>();
		iris.add("LEFTEYE");
		iris.add("RIGHTEYE");
		Mockito.when(env.getProperty("fingerType"))
        .thenReturn("LeftThumb");    
		Mockito.when(packetInfoManager.getApplicantFingerPrintImageNameById(anyString())).thenReturn(fingers);
		Mockito.when(packetInfoManager.getApplicantIrisImageNameById(anyString())).thenReturn(iris);

		Mockito.when(filesystemCephAdapterImpl.checkFileExistence(anyString(), anyString())).thenReturn(Boolean.TRUE);
		Mockito.when(filesystemCephAdapterImpl.getFile(anyString(), anyString())).thenReturn(inputStream);

		byte[] data = "1234567890".getBytes();
		PowerMockito.mockStatic(IOUtils.class);
		PowerMockito.when(IOUtils.class, "toByteArray", inputStream).thenReturn(data);

		authResponseDTO.setStatus("y");
		Mockito.when(restClientService.postApi(any(), anyString(), anyString(), anyString(), any()))
				.thenReturn(authResponseDTO);
	}

	@Test
	public void testDedupeDuplicateFound() {
		String regId = "1234567890";

		DemographicInfoDto dto1 = new DemographicInfoDto();
		DemographicInfoDto dto2 = new DemographicInfoDto();
		List<DemographicInfoDto> Dtos = new ArrayList<>();
		Dtos.add(dto1);
		Dtos.add(dto2);

		Mockito.when(packetInfoDao.findDemoById(regId)).thenReturn(Dtos);
		Mockito.when(packetInfoDao.getAllDemographicInfoDtos(anyString(), anyString(), any(), anyString()))
				.thenReturn(Dtos);

		List<DemographicInfoDto> duplicates = demoDedupe.performDedupe(regId);
		assertEquals(false, duplicates.isEmpty());
	}

	@Test
	public void testDemodedupeEmpty() {
		
		String regId = "1234567890";
		List<DemographicInfoDto> Dtos = new ArrayList<>();

		Mockito.when(packetInfoDao.findDemoById(regId)).thenReturn(Dtos);
		Mockito.when(packetInfoDao.getAllDemographicInfoDtos(anyString(), anyString(), any(), anyString()))
				.thenReturn(Dtos);

		List<DemographicInfoDto> duplicates = demoDedupe.performDedupe(regId);
		assertEquals(true, duplicates.isEmpty());
	}

	@Test
	public void testDemoDedupeAutheticationSucess() throws ApisResourceAccessException, IOException {

		String regId = "1234567890";

		List<String> duplicateIds = new ArrayList<>();
		duplicateIds.add("123456789");
		duplicateIds.add("987654321");

		boolean result = demoDedupe.authenticateDuplicates(regId, duplicateIds);

		assertTrue(result);
	}

	@Test
	public void testDemoDedupeAutheticationFailure() throws ApisResourceAccessException, IOException {

		String regId = "1234567890";

		List<String> duplicateIds = new ArrayList<>();
		duplicateIds.add("123456789");
		duplicateIds.add("987654321");

		authResponseDTO.setStatus("n");

		boolean result = demoDedupe.authenticateDuplicates(regId, duplicateIds);

		assertFalse(result);
	}

}
