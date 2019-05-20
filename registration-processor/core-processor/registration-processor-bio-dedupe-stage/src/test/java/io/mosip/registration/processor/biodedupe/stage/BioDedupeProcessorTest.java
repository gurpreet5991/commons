package io.mosip.registration.processor.biodedupe.stage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.dao.DataAccessException;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.code.ApiName;
import io.mosip.registration.processor.core.code.EventId;
import io.mosip.registration.processor.core.code.EventName;
import io.mosip.registration.processor.core.code.EventType;
import io.mosip.registration.processor.core.constant.AbisConstant;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.http.ResponseWrapper;
import io.mosip.registration.processor.core.packet.dto.demographicinfo.identify.Identity;
import io.mosip.registration.processor.core.packet.dto.demographicinfo.identify.RegistrationProcessorIdentity;
import io.mosip.registration.processor.core.spi.biodedupe.BioDedupeService;
import io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager;
import io.mosip.registration.processor.core.spi.restclient.RegistrationProcessorRestClientService;
import io.mosip.registration.processor.core.util.RegistrationExceptionMapperUtil;
import io.mosip.registration.processor.packet.storage.dao.PacketInfoDao;
import io.mosip.registration.processor.packet.storage.dto.ApplicantInfoDto;
import io.mosip.registration.processor.packet.storage.utils.ABISHandlerUtil;
import io.mosip.registration.processor.packet.storage.utils.Utilities;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.rest.client.audit.dto.AuditResponseDto;
import io.mosip.registration.processor.status.dao.RegistrationStatusDao;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.entity.RegistrationStatusEntity;
import io.mosip.registration.processor.status.service.RegistrationStatusService;

/**
 * The Class BioDedupeStageTest.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.net.ssl.*" })
@PrepareForTest({ Utilities.class })
public class BioDedupeProcessorTest {

	/** The Constant ERROR. */
	private static final String ERROR = "ERROR";

	/** The Constant IDENTITY. */
	private static final String IDENTITY = "identity";

	/** The Constant ABIS_HANDLER_BUS_IN. */
	private static final String ABIS_HANDLER_BUS_IN = "abis-handler-bus-in";

	/** The registration status service. */
	@Mock
	RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	/** The packet info manager. */
	@Mock
	private PacketInfoManager<Identity, ApplicantInfoDto> packetInfoManager;

	/** The bio dedupe service. */
	@Mock
	private BioDedupeService bioDedupeService;

	/** The registration status dao. */
	@Mock
	private RegistrationStatusDao registrationStatusDao;

	/** The packet info dao. */
	@Mock
	private PacketInfoDao packetInfoDao;

	/** The dto. */
	MessageDTO dto = new MessageDTO();

	/** The registration status dto. */
	InternalRegistrationStatusDto registrationStatusDto = new InternalRegistrationStatusDto();

	/** The audit log request builder. */
	@Mock
	private AuditLogRequestBuilder auditLogRequestBuilder;

	/** The matched reg ids. */
	List<String> matchedRegIds = new ArrayList<String>();

	/** The registration status mapper util. */
	@Mock
	RegistrationExceptionMapperUtil registrationStatusMapperUtil;

	/** The bio dedupe processor. */
	@InjectMocks
	private BioDedupeProcessor bioDedupeProcessor;

	/** The adapter. */
	@Mock
	private FileSystemAdapter adapter;

	/** The stage name. */
	private String stageName = "BioDedupeStage";

	/** The utilities. */
	@Mock
	Utilities utilities;

	/** The rest client service. */
	@Mock
	private RegistrationProcessorRestClientService<Object> restClientService;

	/** The entity. */
	@Mock
	RegistrationStatusEntity entity = new RegistrationStatusEntity();

	/** The abis handler util. */
	@Mock
	private ABISHandlerUtil abisHandlerUtil;

	/** The Constant NEW. */
	private static final String NEW = "NEW";

	/** The reg processor identity json. */
	@Mock
	RegistrationProcessorIdentity regProcessorIdentityJson = new RegistrationProcessorIdentity();

	/** The map identity json string to object. */
	@Mock
	ObjectMapper mapIdentityJsonStringToObject;

	/**
	 * Sets the up.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		ReflectionTestUtils.setField(bioDedupeProcessor, "ageLimit", "4");

		AuditResponseDto auditResponseDto = new AuditResponseDto();
		ResponseWrapper<AuditResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(auditResponseDto);
		Mockito.doReturn(responseWrapper).when(auditLogRequestBuilder).createAuditRequestBuilder(
				"test case description", EventId.RPR_405.toString(), EventName.UPDATE.toString(),
				EventType.BUSINESS.toString(), "1234testcase", ApiName.AUDIT);

		dto.setRid("reg1234");
		registrationStatusDto.setRegistrationId("reg1234");
		registrationStatusDto.setRegistrationType("new");
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString())).thenReturn(registrationStatusDto);
		Mockito.when(abisHandlerUtil.getPacketStatus(any())).thenReturn(AbisConstant.PRE_ABIS_IDENTIFICATION);
		Mockito.when(restClientService.getApi(any(), any(), any(), any(), any()))
				.thenReturn("1233445566".getBytes("UTF-16"));
		Mockito.when(registrationStatusMapperUtil.getStatusCode(any())).thenReturn(ERROR);
		Mockito.doNothing().when(packetInfoManager).saveManualAdjudicationData(any(), any(), any());
		Identity identity = new Identity();
		regProcessorIdentityJson.setIdentity(identity);

	}

	/**
	 * Test bio dedupe success.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testNewInsertionPostProcessing() throws Exception {

		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);
		assertTrue(messageDto.getMessageBusAddress().toString()
				.equalsIgnoreCase(MessageBusAddress.ABIS_HANDLER_BUS_IN.toString()));

	}

	/**
	 * Test new insertion to uin success.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testNewInsertionToUinSuccess() throws Exception {

		Mockito.when(restClientService.getApi(any(), any(), any(), any(), any())).thenReturn(null);
		Mockito.when(utilities.getApplicantAge(any())).thenReturn(2);
		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);

		assertTrue(messageDto.getIsValid());

	}

	/**
	 * Test new insertion adult CBEFF not found exception.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testNewInsertionAdultCBEFFNotFoundException() throws Exception {

		Mockito.when(restClientService.getApi(any(), any(), any(), any(), any())).thenReturn(null);
		Mockito.when(utilities.getApplicantAge(any())).thenReturn(12);
		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);
		assertTrue(messageDto.getInternalError());
	}

	/**
	 * Test new exception.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testNewException() throws Exception {
		ReflectionTestUtils.setField(bioDedupeProcessor, "ageLimit", "age");
		Mockito.when(restClientService.getApi(any(), any(), any(), any(), any())).thenReturn(null);
		Mockito.when(utilities.getApplicantAge(any())).thenReturn(12);
		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);
		assertTrue(messageDto.getInternalError());
	}

	/**
	 * Test new insertion IO exception.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testNewInsertionIOException() throws Exception {

		Mockito.when(restClientService.getApi(any(), any(), any(), any(), any())).thenReturn(null);
		Mockito.when(utilities.getApplicantAge(any())).thenThrow(new IOException("IOException"));
		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);
		assertTrue(messageDto.getInternalError());
	}

	/**
	 * Test data access exception.
	 */
	@Test
	public void testDataAccessException() {
		Mockito.when(registrationStatusService.getRegistrationStatus(any()))
				.thenThrow(new DataAccessException("DataAccessException") {
				});
		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);
		assertTrue(messageDto.getInternalError());
	}

	/**
	 * Test new insertion API resourse exception.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testNewInsertionAPIResourseException() throws Exception {

		Mockito.when(restClientService.getApi(any(), any(), any(), any(), any()))
				.thenThrow(new ApisResourceAccessException());
		Mockito.when(utilities.getApplicantAge(any())).thenReturn(12);
		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);
		assertTrue(messageDto.getInternalError());
	}

	/**
	 * Test new identify to UIN stage.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testNewIdentifyToUINStage() throws Exception {
		Mockito.when(abisHandlerUtil.getPacketStatus(any())).thenReturn(AbisConstant.POST_ABIS_IDENTIFICATION);
		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);

		assertTrue(messageDto.getIsValid());

	}

	/**
	 * Test new identify to manual stage.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testNewIdentifyToManualStage() throws Exception {

		List<String> list = new ArrayList<>();
		list.add("1");
		Mockito.when(abisHandlerUtil.getPacketStatus(any())).thenReturn(AbisConstant.POST_ABIS_IDENTIFICATION);

		Mockito.when(abisHandlerUtil.getUniqueRegIds(any(), any())).thenReturn(list);
		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);

		assertFalse(messageDto.getIsValid());

	}

	/**
	 * Test update insertion to handler.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testUpdateInsertionToHandler() throws Exception {

		InputStream inputStream = new FileInputStream("src/test/resources/ID.json");
		PowerMockito.mockStatic(Utilities.class);
		Mockito.when(utilities.getGetRegProcessorDemographicIdentity()).thenReturn(IDENTITY);

		Mockito.when(adapter.getFile(any(), any())).thenReturn(inputStream);

		registrationStatusDto.setRegistrationId("reg1234");
		registrationStatusDto.setRegistrationType("Update");
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString())).thenReturn(registrationStatusDto);

		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);

		assertEquals(messageDto.getMessageBusAddress().getAddress(), ABIS_HANDLER_BUS_IN);
	}

	/**
	 * Test update insertion to UIN.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testUpdateInsertionToUIN() throws Exception {

		InputStream inputStream = new FileInputStream("src/test/resources/ID2.json");
		PowerMockito.mockStatic(Utilities.class);
		Mockito.when(utilities.getGetRegProcessorDemographicIdentity()).thenReturn(IDENTITY);

		Mockito.when(adapter.getFile(any(), any())).thenReturn(inputStream);

		registrationStatusDto.setRegistrationId("reg1234");
		registrationStatusDto.setRegistrationType("Update");
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString())).thenReturn(registrationStatusDto);

		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);

		assertTrue(messageDto.getIsValid());
	}

	/**
	 * Test identity not found exception.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testIdentityNotFoundException() throws Exception {

		InputStream inputStream = new FileInputStream("src/test/resources/ID3.json");
		PowerMockito.mockStatic(Utilities.class);
		Mockito.when(utilities.getGetRegProcessorDemographicIdentity()).thenReturn(IDENTITY);

		Mockito.when(adapter.getFile(any(), any())).thenReturn(inputStream);

		registrationStatusDto.setRegistrationId("reg1234");
		registrationStatusDto.setRegistrationType("Update");
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString())).thenReturn(registrationStatusDto);

		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);

		assertTrue(messageDto.getInternalError());
	}

	/**
	 * Test bio de dup update packet handler processing success.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testBioDeDupUpdatePacketHandlerProcessingSuccess() throws ApisResourceAccessException, IOException {

		registrationStatusDto.setRegistrationId("reg1234");
		registrationStatusDto.setRegistrationType("UPDATE");
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString())).thenReturn(registrationStatusDto);
		Mockito.when(abisHandlerUtil.getPacketStatus(any())).thenReturn(AbisConstant.POST_ABIS_IDENTIFICATION);

		List<String> matchedRidList = new ArrayList<>();
		Mockito.when(abisHandlerUtil.getUniqueRegIds(any(), any())).thenReturn(matchedRidList);
		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);
		assertTrue(messageDto.getIsValid());
	}

	/**
	 * Test bio de dup update packet handler processing failure.
	 *
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testBioDeDupUpdatePacketHandlerProcessingFailure() throws ApisResourceAccessException, IOException {
		registrationStatusDto.setRegistrationId("reg1234");
		registrationStatusDto.setRegistrationType("UPDATE");
		Mockito.when(registrationStatusService.getRegistrationStatus(anyString())).thenReturn(registrationStatusDto);
		Mockito.when(abisHandlerUtil.getPacketStatus(any())).thenReturn(AbisConstant.POST_ABIS_IDENTIFICATION);

		List<String> matchedRidList = new ArrayList<>();
		matchedRidList.add("27847657360002520190320095010");
		Mockito.when(abisHandlerUtil.getUniqueRegIds(any(), any())).thenReturn(matchedRidList);

		MessageDTO messageDto = bioDedupeProcessor.process(dto, stageName);
		assertFalse(messageDto.getIsValid());
	}

}
