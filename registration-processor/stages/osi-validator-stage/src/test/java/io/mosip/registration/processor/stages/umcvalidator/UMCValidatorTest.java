package io.mosip.registration.processor.stages.umcvalidator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.packet.dto.Identity;
import io.mosip.registration.processor.core.packet.dto.RegOsiDto;
import io.mosip.registration.processor.core.packet.dto.RegistrationCenterMachineDto;
import io.mosip.registration.processor.core.packet.dto.regcentermachine.DeviceHistoryDto;
import io.mosip.registration.processor.core.packet.dto.regcentermachine.DeviceHistoryResponseDto;
import io.mosip.registration.processor.core.packet.dto.regcentermachine.MachineHistoryDto;
import io.mosip.registration.processor.core.packet.dto.regcentermachine.MachineHistoryResponseDto;
import io.mosip.registration.processor.core.packet.dto.regcentermachine.RegistartionCenterTimestampResponseDto;
import io.mosip.registration.processor.core.packet.dto.regcentermachine.RegistrationCenterDeviceHistoryDto;
import io.mosip.registration.processor.core.packet.dto.regcentermachine.RegistrationCenterDeviceHistoryResponseDto;
import io.mosip.registration.processor.core.packet.dto.regcentermachine.RegistrationCenterDto;
import io.mosip.registration.processor.core.packet.dto.regcentermachine.RegistrationCenterResponseDto;
import io.mosip.registration.processor.core.packet.dto.regcentermachine.RegistrationCenterUserMachineMappingHistoryDto;
import io.mosip.registration.processor.core.packet.dto.regcentermachine.RegistrationCenterUserMachineMappingHistoryResponseDto;
import io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager;
import io.mosip.registration.processor.core.spi.restclient.RegistrationProcessorRestClientService;
import io.mosip.registration.processor.packet.storage.dto.ApplicantInfoDto;
import io.mosip.registration.processor.stages.osivalidator.UMCValidator;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;

/**
 * The Class UMCValidatorTest.
 */
@RunWith(PowerMockRunner.class)
public class UMCValidatorTest {

	/** The umc validator. */
	@InjectMocks
	UMCValidator umcValidator;

	/** The packet info manager. */
	@Mock
	PacketInfoManager<Identity, ApplicantInfoDto> packetInfoManager;

	/** The adapter. */
	@Mock
	private FileSystemAdapter adapter;

	/** The registration processor rest service. */
	@Mock
	private RegistrationProcessorRestClientService<Object> registrationProcessorRestService;

	/** The rcm dto. */
	RegistrationCenterMachineDto rcmDto = new RegistrationCenterMachineDto();

	/** The reg osi. */
	RegOsiDto regOsi;

	/**
	 * Sets the up.
	 *
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	@Before
	public void setUp() throws FileNotFoundException {
		InternalRegistrationStatusDto registrationStatusDto = new InternalRegistrationStatusDto();
		umcValidator.setRegistrationStatusDto(registrationStatusDto);
		rcmDto = new RegistrationCenterMachineDto();
		regOsi = new RegOsiDto();
		rcmDto.setIsActive(true);
		rcmDto.setLatitude("13.0049");
		rcmDto.setLongitude("80.24492");
		rcmDto.setMachineId("yyeqy26356");
		rcmDto.setPacketCreationDate("2018-11-28T15:34:20.122");
		rcmDto.setRegcntrId("12245");
		rcmDto.setRegId("2018782130000121112018103016");

		regOsi.setOfficerId("O1234");

		regOsi.setSupervisorId("S1234");

		ClassLoader classLoader = getClass().getClassLoader();
		File idJsonFile = new File(classLoader.getResource("packet_meta_info.json").getFile());
		InputStream packetMetaInfoStream = new FileInputStream(idJsonFile);

		Mockito.when(adapter.getFile(any(), any())).thenReturn(packetMetaInfoStream);

		Mockito.when(packetInfoManager.getOsi(anyString())).thenReturn(regOsi);

	}

	/**
	 * Checks if is valid UMC success test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void isValidUMCSuccessTest() throws ApisResourceAccessException, JsonParseException, JsonMappingException,
			IOException, java.io.IOException {
		RegistrationCenterDto rcdto = new RegistrationCenterDto();
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("12334");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();

		offrepdto.setRegistrationCenters(officerucmdtos);

		RegistartionCenterTimestampResponseDto test = new RegistartionCenterTimestampResponseDto();
		test.setStatus("Accepted");

		List<DeviceHistoryDto> deviceHistoryDetails = new ArrayList<>();
		DeviceHistoryDto deviceHistoryDto = new DeviceHistoryDto();
		deviceHistoryDto.setIsActive(true);
		deviceHistoryDetails.add(deviceHistoryDto);

		DeviceHistoryResponseDto deviceHistoryResponsedto = new DeviceHistoryResponseDto();
		deviceHistoryResponsedto.setDeviceHistoryDetails(deviceHistoryDetails);

		RegistrationCenterDeviceHistoryResponseDto registrationCenterDeviceHistoryResponseDto = new RegistrationCenterDeviceHistoryResponseDto();
		RegistrationCenterDeviceHistoryDto registrationCenterDeviceHistoryDetails = new RegistrationCenterDeviceHistoryDto();

		registrationCenterDeviceHistoryDetails.setIsActive(true);
		registrationCenterDeviceHistoryResponseDto
				.setRegistrationCenterDeviceHistoryDetails(registrationCenterDeviceHistoryDetails);
		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto).thenReturn(offrepdto).thenReturn(test)
				.thenReturn(deviceHistoryResponsedto).thenReturn(registrationCenterDeviceHistoryResponseDto);
		// UMC validation successfull;
		assertTrue(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * UMC mapping not active test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void UMCMappingNotActiveTest() throws ApisResourceAccessException, JsonParseException, JsonMappingException,
			IOException, java.io.IOException {
		RegistrationCenterDto rcdto = new RegistrationCenterDto();
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("yyeqy26356");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(false);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();
		offrepdto.setRegistrationCenters(officerucmdtos);

		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto);
		// UMC validation Failure;
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Machine id not found test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void machineIdNotFoundTest() throws ApisResourceAccessException, JsonParseException, JsonMappingException,
			IOException, java.io.IOException {
		RegistrationCenterDto rcdto = new RegistrationCenterDto();
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);
		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();
		offrepdto.setRegistrationCenters(officerucmdtos);

		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto);
		// UMC validation Failure;
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Machine not active test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void machineNotActiveTest() throws ApisResourceAccessException, JsonParseException, JsonMappingException,
			IOException, java.io.IOException {
		RegistrationCenterDto rcdto = new RegistrationCenterDto();
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(false);
		mcdto.setId("yyeqy26356");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();
		offrepdto.setRegistrationCenters(officerucmdtos);
		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto);
		// UMC validation Failure;
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Gps datanot present in master test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void gpsDatanotPresentInMasterTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {
		RegistrationCenterDto rcdto = new RegistrationCenterDto();
		rcdto.setIsActive(true);
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("yyeqy26356");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();
		offrepdto.setRegistrationCenters(officerucmdtos);

		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto);
		// UMC validation Failure;
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Wronggps data present in master test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void WronggpsDataPresentInMasterTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {
		RegistrationCenterDto rcdto = new RegistrationCenterDto();
		rcdto.setIsActive(true);
		rcdto.setId("12245");
		rcdto.setLongitude("80.21492");
		rcdto.setLatitude("13.10049");
		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("yyeqy26356");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();
		offrepdto.setRegistrationCenters(officerucmdtos);

		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto);
		// UMC validation Failure;
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Gps datanot present in packet test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void gpsDatanotPresentInPacketTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {
		RegistrationCenterMachineDto rcmDto = new RegistrationCenterMachineDto();
		rcmDto.setIsActive(true);
		rcmDto.setLatitude("13.0049");
		rcmDto.setLongitude("");
		rcmDto.setMachineId(" ");
		rcmDto.setPacketCreationDate("2018-11-28T15:34:20");
		rcmDto.setRegcntrId("12245");
		rcmDto.setRegId("2018782130000121112018103016");

		RegistrationCenterDto rcdto = new RegistrationCenterDto();
		rcdto.setIsActive(true);
		rcdto.setId("12245");
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.10049");
		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("yyeqy26356");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();
		offrepdto.setRegistrationCenters(officerucmdtos);

		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto);
		// UMC validation Failure;
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Registration centernot active test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void registrationCenternotActiveTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {

		RegistrationCenterDto rcdto = new RegistrationCenterDto();
		rcdto.setIsActive(false);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);

		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("yyeqy26356");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();
		offrepdto.setRegistrationCenters(officerucmdtos);

		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto);
		// UMC validation Failure;
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Checks if is valid UMC failure for timestamp test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void isValidUMCFailureForTimestampTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {
		RegistrationCenterDto rcdto = new RegistrationCenterDto();
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("12334");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();

		offrepdto.setRegistrationCenters(officerucmdtos);

		byte[] responseBody = "{\"timestamp\":1548931133376,\"status\":400,\"errors\":[{\"errorCode\":\"KER-MSD-033\",\"errorMessage\":\"Invalid date format Text '2019-01-23T17:15:15.463' could not be parsed at index 23\"}]}"
				.getBytes();

		ApisResourceAccessException apisResourceAccessException = Mockito.mock(ApisResourceAccessException.class);
		HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST,
				"Invalid request", null, responseBody, null);
		Mockito.when(apisResourceAccessException.getCause()).thenReturn(httpClientErrorException);

		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto).thenReturn(offrepdto)
				.thenThrow(apisResourceAccessException);
		// UMC validation Failure;
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Checks if is valid UMC failure for registration center ID test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void isValidUMCFailureForRegistrationCenterIDTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {
		RegistrationCenterDto rcdto = new RegistrationCenterDto();
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("12334");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();

		offrepdto.setRegistrationCenters(officerucmdtos);

		byte[] responseBody = "{\"timestamp\":1548931752579,\"status\":404,\"errors\":[{\"errorCode\":\"KER-MSD-042\",\"errorMessage\":\"Registration Center not found\"}]}"
				.getBytes();

		ApisResourceAccessException apisResourceAccessException = Mockito.mock(ApisResourceAccessException.class);
		HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST,
				"Invalid request", null, responseBody, null);
		Mockito.when(apisResourceAccessException.getCause()).thenReturn(httpClientErrorException);

		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto).thenReturn(offrepdto)
				.thenThrow(apisResourceAccessException);

		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Checks if is valid UMC center id validation rejected test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void isValidUMCCenterIdValidationRejectedTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {
		RegistrationCenterDto rcdto = new RegistrationCenterDto();
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("12334");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();

		offrepdto.setRegistrationCenters(officerucmdtos);

		RegistartionCenterTimestampResponseDto test = new RegistartionCenterTimestampResponseDto();
		test.setStatus("Rejected");

		List<DeviceHistoryDto> deviceHistoryDetails = new ArrayList<>();
		DeviceHistoryDto deviceHistoryDto = new DeviceHistoryDto();
		deviceHistoryDto.setIsActive(true);
		deviceHistoryDetails.add(deviceHistoryDto);

		DeviceHistoryResponseDto deviceHistoryResponsedto = new DeviceHistoryResponseDto();
		deviceHistoryResponsedto.setDeviceHistoryDetails(deviceHistoryDetails);

		RegistrationCenterDeviceHistoryResponseDto registrationCenterDeviceHistoryResponseDto = new RegistrationCenterDeviceHistoryResponseDto();
		RegistrationCenterDeviceHistoryDto registrationCenterDeviceHistoryDetails = new RegistrationCenterDeviceHistoryDto();

		registrationCenterDeviceHistoryDetails.setIsActive(true);
		registrationCenterDeviceHistoryResponseDto
				.setRegistrationCenterDeviceHistoryDetails(registrationCenterDeviceHistoryDetails);
		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto).thenReturn(offrepdto).thenReturn(test)
				.thenReturn(deviceHistoryResponsedto).thenReturn(registrationCenterDeviceHistoryResponseDto);

		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Validate device mapped with center failure test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void validateDeviceMappedWithCenterFailureTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {

		ApisResourceAccessException apisResourceAccessException = Mockito.mock(ApisResourceAccessException.class);
		byte[] response = "{\"timestamp\":1548930810031,\"status\":404,\"errors\":[{\"errorCode\":\"KER-MSD-133\",\"errorMessage\":\"Device History not found\"}]}"
				.getBytes();
		HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST,
				"Invalid request", response, StandardCharsets.UTF_8);
		RegistrationCenterDto rcdto = new RegistrationCenterDto();

		Mockito.when(apisResourceAccessException.getCause()).thenReturn(httpClientErrorException);
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("12334");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();

		offrepdto.setRegistrationCenters(officerucmdtos);

		RegistartionCenterTimestampResponseDto test = new RegistartionCenterTimestampResponseDto();
		test.setStatus("Accepted");

		List<DeviceHistoryDto> deviceHistoryDetails = new ArrayList<>();
		DeviceHistoryDto deviceHistoryDto = new DeviceHistoryDto();
		deviceHistoryDto.setIsActive(true);
		deviceHistoryDetails.add(deviceHistoryDto);

		DeviceHistoryResponseDto deviceHistoryResponsedto = new DeviceHistoryResponseDto();
		deviceHistoryResponsedto.setDeviceHistoryDetails(deviceHistoryDetails);

		RegistrationCenterDeviceHistoryResponseDto registrationCenterDeviceHistoryResponseDto = new RegistrationCenterDeviceHistoryResponseDto();
		RegistrationCenterDeviceHistoryDto registrationCenterDeviceHistoryDetails = new RegistrationCenterDeviceHistoryDto();

		registrationCenterDeviceHistoryDetails.setIsActive(true);
		registrationCenterDeviceHistoryResponseDto
				.setRegistrationCenterDeviceHistoryDetails(registrationCenterDeviceHistoryDetails);
		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto).thenReturn(offrepdto).thenReturn(test)
				.thenReturn(deviceHistoryResponsedto).thenThrow(apisResourceAccessException);
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Validate device failure test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void validateDeviceFailureTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {

		ApisResourceAccessException apisResourceAccessException = Mockito.mock(ApisResourceAccessException.class);
		byte[] response = "{\"timestamp\":1548930810031,\"status\":404,\"errors\":[{\"errorCode\":\"KER-MSD-129\",\"errorMessage\":\"Device History not found\"}]}"
				.getBytes();
		HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST,
				"Invalid request", response, StandardCharsets.UTF_8);
		RegistrationCenterDto rcdto = new RegistrationCenterDto();

		Mockito.when(apisResourceAccessException.getCause()).thenReturn(httpClientErrorException);
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("12334");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();

		offrepdto.setRegistrationCenters(officerucmdtos);

		RegistartionCenterTimestampResponseDto test = new RegistartionCenterTimestampResponseDto();
		test.setStatus("Accepted");

		List<DeviceHistoryDto> deviceHistoryDetails = new ArrayList<>();
		DeviceHistoryDto deviceHistoryDto = new DeviceHistoryDto();
		deviceHistoryDto.setIsActive(true);
		deviceHistoryDetails.add(deviceHistoryDto);

		DeviceHistoryResponseDto deviceHistoryResponsedto = new DeviceHistoryResponseDto();
		deviceHistoryResponsedto.setDeviceHistoryDetails(deviceHistoryDetails);

		RegistrationCenterDeviceHistoryResponseDto registrationCenterDeviceHistoryResponseDto = new RegistrationCenterDeviceHistoryResponseDto();
		RegistrationCenterDeviceHistoryDto registrationCenterDeviceHistoryDetails = new RegistrationCenterDeviceHistoryDto();

		registrationCenterDeviceHistoryDetails.setIsActive(true);
		registrationCenterDeviceHistoryResponseDto
				.setRegistrationCenterDeviceHistoryDetails(registrationCenterDeviceHistoryDetails);
		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenReturn(offrepdto).thenReturn(offrepdto).thenReturn(test)
				.thenThrow(apisResourceAccessException).thenReturn(registrationCenterDeviceHistoryResponseDto);
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Validate registration center failure test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void validateRegistrationCenterFailureTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {

		ApisResourceAccessException apisResourceAccessException = Mockito.mock(ApisResourceAccessException.class);
		byte[] response = "{\"timestamp\":1548930810031,\"status\":404,\"errors\":[{\"errorCode\":\"KER-MSD-129\",\"errorMessage\":\"center History not found\"}]}"
				.getBytes();
		HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST,
				"Invalid request", response, StandardCharsets.UTF_8);
		RegistrationCenterDto rcdto = new RegistrationCenterDto();

		Mockito.when(apisResourceAccessException.getCause()).thenReturn(httpClientErrorException);
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("12334");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();

		offrepdto.setRegistrationCenters(officerucmdtos);

		RegistartionCenterTimestampResponseDto test = new RegistartionCenterTimestampResponseDto();
		test.setStatus("Accepted");

		List<DeviceHistoryDto> deviceHistoryDetails = new ArrayList<>();
		DeviceHistoryDto deviceHistoryDto = new DeviceHistoryDto();
		deviceHistoryDto.setIsActive(true);
		deviceHistoryDetails.add(deviceHistoryDto);

		DeviceHistoryResponseDto deviceHistoryResponsedto = new DeviceHistoryResponseDto();
		deviceHistoryResponsedto.setDeviceHistoryDetails(deviceHistoryDetails);

		RegistrationCenterDeviceHistoryResponseDto registrationCenterDeviceHistoryResponseDto = new RegistrationCenterDeviceHistoryResponseDto();
		RegistrationCenterDeviceHistoryDto registrationCenterDeviceHistoryDetails = new RegistrationCenterDeviceHistoryDto();

		registrationCenterDeviceHistoryDetails.setIsActive(true);
		registrationCenterDeviceHistoryResponseDto
				.setRegistrationCenterDeviceHistoryDetails(registrationCenterDeviceHistoryDetails);
		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any()))
				.thenThrow(apisResourceAccessException).thenReturn(mhrepdto).thenReturn(offrepdto).thenReturn(offrepdto)
				.thenReturn(test).thenReturn(deviceHistoryResponsedto)
				.thenReturn(registrationCenterDeviceHistoryResponseDto);
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Validate machine failure test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void validateMachineFailureTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {

		ApisResourceAccessException apisResourceAccessException = Mockito.mock(ApisResourceAccessException.class);
		byte[] response = "{\"timestamp\":1548930810031,\"status\":404,\"errors\":[{\"errorCode\":\"KER-MSD-129\",\"errorMessage\":\"center History not found\"}]}"
				.getBytes();
		HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST,
				"Invalid request", response, StandardCharsets.UTF_8);
		RegistrationCenterDto rcdto = new RegistrationCenterDto();

		Mockito.when(apisResourceAccessException.getCause()).thenReturn(httpClientErrorException);
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("12334");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();

		offrepdto.setRegistrationCenters(officerucmdtos);

		RegistartionCenterTimestampResponseDto test = new RegistartionCenterTimestampResponseDto();
		test.setStatus("Accepted");

		List<DeviceHistoryDto> deviceHistoryDetails = new ArrayList<>();
		DeviceHistoryDto deviceHistoryDto = new DeviceHistoryDto();
		deviceHistoryDto.setIsActive(true);
		deviceHistoryDetails.add(deviceHistoryDto);

		DeviceHistoryResponseDto deviceHistoryResponsedto = new DeviceHistoryResponseDto();
		deviceHistoryResponsedto.setDeviceHistoryDetails(deviceHistoryDetails);

		RegistrationCenterDeviceHistoryResponseDto registrationCenterDeviceHistoryResponseDto = new RegistrationCenterDeviceHistoryResponseDto();
		RegistrationCenterDeviceHistoryDto registrationCenterDeviceHistoryDetails = new RegistrationCenterDeviceHistoryDto();

		registrationCenterDeviceHistoryDetails.setIsActive(true);
		registrationCenterDeviceHistoryResponseDto
				.setRegistrationCenterDeviceHistoryDetails(registrationCenterDeviceHistoryDetails);
		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenThrow(apisResourceAccessException).thenReturn(offrepdto).thenReturn(offrepdto).thenReturn(test)
				.thenReturn(deviceHistoryResponsedto).thenReturn(registrationCenterDeviceHistoryResponseDto);
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

	/**
	 * Validate UM cmapping failure test.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws JsonParseException
	 *             the json parse exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void validateUMCmappingFailureTest() throws ApisResourceAccessException, JsonParseException,
			JsonMappingException, IOException, java.io.IOException {

		ApisResourceAccessException apisResourceAccessException = Mockito.mock(ApisResourceAccessException.class);
		byte[] response = "{\"timestamp\":1548930810031,\"status\":404,\"errors\":[{\"errorCode\":\"KER-MSD-129\",\"errorMessage\":\"center History not found\"}]}"
				.getBytes();
		HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST,
				"Invalid request", response, StandardCharsets.UTF_8);
		RegistrationCenterDto rcdto = new RegistrationCenterDto();

		Mockito.when(apisResourceAccessException.getCause()).thenReturn(httpClientErrorException);
		rcdto.setIsActive(true);
		rcdto.setLongitude("80.24492");
		rcdto.setLatitude("13.0049");
		rcdto.setId("12245");

		List<RegistrationCenterDto> rcdtos = new ArrayList<>();
		rcdtos.add(rcdto);
		RegistrationCenterResponseDto regrepdto = new RegistrationCenterResponseDto();
		regrepdto.setRegistrationCentersHistory(rcdtos);

		MachineHistoryDto mcdto = new MachineHistoryDto();
		mcdto.setIsActive(true);
		mcdto.setId("12334");

		List<MachineHistoryDto> mcdtos = new ArrayList<>();
		mcdtos.add(mcdto);
		MachineHistoryResponseDto mhrepdto = new MachineHistoryResponseDto();
		mhrepdto.setMachineHistoryDetails(mcdtos);

		RegistrationCenterUserMachineMappingHistoryDto officerucmdto = new RegistrationCenterUserMachineMappingHistoryDto();
		officerucmdto.setIsActive(true);
		officerucmdto.setCntrId("12245");
		officerucmdto.setMachineId("yyeqy26356");
		officerucmdto.setUsrId("O1234");

		List<RegistrationCenterUserMachineMappingHistoryDto> officerucmdtos = new ArrayList<>();
		officerucmdtos.add(officerucmdto);

		RegistrationCenterUserMachineMappingHistoryResponseDto offrepdto = new RegistrationCenterUserMachineMappingHistoryResponseDto();

		offrepdto.setRegistrationCenters(officerucmdtos);

		RegistartionCenterTimestampResponseDto test = new RegistartionCenterTimestampResponseDto();
		test.setStatus("Accepted");

		List<DeviceHistoryDto> deviceHistoryDetails = new ArrayList<>();
		DeviceHistoryDto deviceHistoryDto = new DeviceHistoryDto();
		deviceHistoryDto.setIsActive(true);
		deviceHistoryDetails.add(deviceHistoryDto);

		DeviceHistoryResponseDto deviceHistoryResponsedto = new DeviceHistoryResponseDto();
		deviceHistoryResponsedto.setDeviceHistoryDetails(deviceHistoryDetails);

		RegistrationCenterDeviceHistoryResponseDto registrationCenterDeviceHistoryResponseDto = new RegistrationCenterDeviceHistoryResponseDto();
		RegistrationCenterDeviceHistoryDto registrationCenterDeviceHistoryDetails = new RegistrationCenterDeviceHistoryDto();

		registrationCenterDeviceHistoryDetails.setIsActive(true);
		registrationCenterDeviceHistoryResponseDto
				.setRegistrationCenterDeviceHistoryDetails(registrationCenterDeviceHistoryDetails);
		Mockito.when(registrationProcessorRestService.getApi(any(), any(), any(), any(), any())).thenReturn(regrepdto)
				.thenReturn(mhrepdto).thenThrow(apisResourceAccessException).thenReturn(offrepdto).thenReturn(test)
				.thenReturn(deviceHistoryResponsedto).thenReturn(registrationCenterDeviceHistoryResponseDto);
		assertFalse(umcValidator.isValidUMC("2018782130000121112018103016"));
	}

}
