package io.mosip.preregistration.application.test.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.adapter.AuthUserDetails;
import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.idgenerator.spi.PridGenerator;
import io.mosip.kernel.core.jsonvalidator.exception.HttpRequestException;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.jsonvalidator.impl.JsonValidatorImpl;
import io.mosip.preregistration.application.DemographicTestApplication;
import io.mosip.preregistration.application.dto.DeletePreRegistartionDTO;
import io.mosip.preregistration.application.dto.DemographicCreateResponseDTO;
import io.mosip.preregistration.application.dto.DemographicRequestDTO;
import io.mosip.preregistration.application.dto.DemographicUpdateResponseDTO;
import io.mosip.preregistration.application.dto.PreRegistrationViewDTO;
import io.mosip.preregistration.application.entity.DemographicEntity;
import io.mosip.preregistration.application.errorcodes.ErrorCodes;
import io.mosip.preregistration.application.errorcodes.ErrorMessages;
import io.mosip.preregistration.application.exception.BookingDeletionFailedException;
import io.mosip.preregistration.application.exception.DocumentFailedToDeleteException;
import io.mosip.preregistration.application.exception.RecordFailedToDeleteException;
import io.mosip.preregistration.application.exception.RecordFailedToUpdateException;
import io.mosip.preregistration.application.exception.RecordNotFoundException;
import io.mosip.preregistration.application.exception.RecordNotFoundForPreIdsException;
import io.mosip.preregistration.application.exception.RestCallException;
import io.mosip.preregistration.application.exception.system.DateParseException;
import io.mosip.preregistration.application.exception.system.JsonValidationException;
import io.mosip.preregistration.application.exception.system.SystemUnsupportedEncodingException;
import io.mosip.preregistration.application.repository.DemographicRepository;
import io.mosip.preregistration.application.service.DemographicService;
import io.mosip.preregistration.application.service.util.DemographicServiceUtil;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.BookingRegistrationDTO;
import io.mosip.preregistration.core.common.dto.DeleteBookingDTO;
import io.mosip.preregistration.core.common.dto.DemographicResponseDTO;
import io.mosip.preregistration.core.common.dto.DocumentDeleteDTO;
import io.mosip.preregistration.core.common.dto.DocumentDeleteResponseDTO;
import io.mosip.preregistration.core.common.dto.ExceptionJSONInfoDTO;
import io.mosip.preregistration.core.common.dto.MainListResponseDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.preregistration.core.common.dto.PreRegIdsByRegCenterIdDTO;
import io.mosip.preregistration.core.common.dto.PreRegistartionStatusDTO;
import io.mosip.preregistration.core.exception.HashingException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.CryptoUtil;
import io.mosip.preregistration.core.util.HashUtill;

/**
 * Test class to test the PreRegistration Service methods
 * 
 * @author Rajath KR
 * @author Sanober Noor
 * @author Ravi C Balaji
 * @since 1.0.0
 * 
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DemographicTestApplication.class })
public class DemographicServiceTest {

	/**
	 * Mocking the DemographicRepository bean
	 */
	@MockBean
	private DemographicRepository demographicRepository;

	/**
	 * Mocking the RestTemplateBuilder bean
	 */
	@MockBean(name = "restTemplateConfig")
	RestTemplate restTemplate;

	/**
	 * Mocking the PridGenerator bean
	 */
	@MockBean
	private PridGenerator<String> pridGenerator;

	/**
	 * Mocking the JsonValidatorImpl bean
	 */
	@MockBean
	private JsonValidatorImpl jsonValidator;

	/**
	 * Autowired reference for $link{DemographicServiceUtil}
	 */
	@Autowired
	DemographicServiceUtil serviceUtil;

	JSONParser parser = new JSONParser();

	/**
	 * Autowired reference for $link{DemographicService}
	 */
	@Autowired
	private DemographicService preRegistrationService;

	@MockBean
	private AuditLogUtil auditLogUtil;

	@MockBean
	private CryptoUtil cryptoUtil;

	List<DemographicEntity> userEntityDetails = new ArrayList<>();
	List<PreRegistrationViewDTO> responseViewList = new ArrayList<PreRegistrationViewDTO>();
	private PreRegistrationViewDTO preRegistrationViewDTO;
	private DemographicEntity preRegistrationEntity;
	private JSONObject jsonObject;
	private JSONObject jsonTestObject;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	File fileCr = null;
	File fileUp = null;
	MainRequestDTO<DemographicRequestDTO> request = null;
	DemographicRequestDTO createPreRegistrationDTO = null;
	DemographicResponseDTO demographicResponseDTO = null;
	DemographicCreateResponseDTO demographicResponseForCreateDTO = null;
	DemographicUpdateResponseDTO demographicResponseForUpdateDTO = null;
	boolean requestValidatorFlag = false;
	Map<String, String> requestMap = new HashMap<>();
	Map<String, String> requiredRequestMap = new HashMap<>();
	LocalDateTime times = null;
	BookingRegistrationDTO bookingRegistrationDTO;
	MainListResponseDTO<DemographicResponseDTO> responseDTO = null;
	MainListResponseDTO<DemographicCreateResponseDTO> responseCreateDTO = null;
	AuditRequestDto auditRequestDto = new AuditRequestDto();

	@Value("${ver}")
	String versionUrl;

	/**
	 * Reference for ${createId} from property file
	 */
	@Value("${mosip.preregistration.demographic.create.id}")
	private String createId;

	/**
	 * Reference for ${updateId} from property file
	 */
	@Value("${mosip.preregistration.demographic.update.id}")
	private String updateId;

	/**
	 * Reference for ${retrieveId} from property file
	 */
	@Value("${mosip.preregistration.demographic.retrieve.basic.id}")
	private String retrieveId;
	/**
	 * Reference for ${retrieveDetailsId} from property file
	 */
	@Value("${mosip.preregistration.demographic.retrieve.details.id}")
	private String retrieveDetailsId;

	/**
	 * Reference for ${retrieveStatusId} from property file
	 */
	@Value("${mosip.preregistration.demographic.retrieve.status.id}")
	private String retrieveStatusId;

	/**
	 * Reference for ${deleteId} from property file
	 */
	@Value("${mosip.preregistration.demographic.delete.id}")
	private String deleteId;

	/**
	 * Reference for ${updateStatusId} from property file
	 */
	@Value("${mosip.preregistration.demographic.update.status.id}")
	private String updateStatusId;

	/**
	 * Reference for ${dateId} from property file
	 */
	@Value("${mosip.preregistration.demographic.retrieve.date.id}")
	private String dateId;

	LocalDate fromDate = LocalDate.now();
	LocalDate toDate = LocalDate.now();
	@Autowired
	private ObjectMapper mapper;
	JSONArray fullname;
	LocalDateTime encryptionDateTime = DateUtils.getUTCCurrentDateTime();
	DemographicService spyDemographicService;
	String preId = "";

	/**
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws org.json.simple.parser.ParseException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws ParseException, FileNotFoundException, IOException,
			org.json.simple.parser.ParseException, URISyntaxException {

		preRegistrationEntity = new DemographicEntity();
		ClassLoader classLoader = getClass().getClassLoader();
		URI uri = new URI(
				classLoader.getResource("pre-registration-crby.json").getFile().trim().replaceAll("\\u0020", "%20"));
		fileCr = new File(uri.getPath());
		uri = new URI(
				classLoader.getResource("pre-registration-upby.json").getFile().trim().replaceAll("\\u0020", "%20"));
		fileUp = new File(uri.getPath());

		File file = new File(classLoader.getResource("pre-registration-test.json").getFile());
		jsonObject = (JSONObject) parser.parse(new FileReader(file));

		File fileTest = new File(classLoader.getResource("pre-registration-test.json").getFile());
		jsonTestObject = (JSONObject) parser.parse(new FileReader(fileTest));

		times = LocalDateTime.now();
		preRegistrationEntity.setCreateDateTime(times);
		preRegistrationEntity.setCreatedBy("9988905444");
		preRegistrationEntity.setStatusCode("Pending_Appointment");
		preRegistrationEntity.setUpdateDateTime(times);

		// byte[] encryptedDemographicDetails =
		// cryptoUtil.encrypt(jsonTestObject.toJSONString().getBytes(),
		// encryptionDateTime);
		// preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		preRegistrationEntity.setPreRegistrationId("98746563542672");
		preRegistrationEntity
				.setDemogDetailHash(HashUtill.hashUtill(jsonTestObject.toJSONString().getBytes()).toString());
		userEntityDetails.add(preRegistrationEntity);

		logger.info("Entity " + preRegistrationEntity);

		preRegistrationViewDTO = new PreRegistrationViewDTO();
		preRegistrationViewDTO.setFullname(null);
		preRegistrationViewDTO.setStatusCode("Pending_Appointment");
		preRegistrationViewDTO.setPreRegistrationId("98746563542672");
		responseViewList.add(preRegistrationViewDTO);

		createPreRegistrationDTO = new DemographicRequestDTO();
		createPreRegistrationDTO.setDemographicDetails(jsonObject);
		preId = "98746563542672";

		request = new MainRequestDTO<DemographicRequestDTO>();
		request.setId("mosip.pre-registration.demographic.create");
		request.setVersion("1.0");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		mapper.setDateFormat(df);

		request.setRequesttime(df.parse("2019-01-22T07:22:57.186Z"));
		request.setRequesttime(new Timestamp(System.currentTimeMillis()));
		request.setRequest(createPreRegistrationDTO);

		bookingRegistrationDTO = new BookingRegistrationDTO();
		bookingRegistrationDTO.setRegDate("2018-12-10");
		bookingRegistrationDTO.setRegistrationCenterId("1");
		bookingRegistrationDTO.setSlotFromTime("09:00");
		bookingRegistrationDTO.setSlotToTime("09:13");

		requestMap.put("id", request.getId());
		requestMap.put("ver", request.getVersion());
		requestMap.put("reqTime", request.getRequesttime().toString());
		requestMap.put("request", request.getRequest().toString());

		// requestMap.put(RequestCodes.FROM_DATE.getCode(), fromDate);
		// requestMap.put(RequestCodes.TO_DATE.getCode(), toDate);

		// requiredRequestMap.put("id", idUrl);
		requiredRequestMap.put("ver", versionUrl);

		responseDTO = new MainListResponseDTO<DemographicResponseDTO>();
		responseCreateDTO = new MainListResponseDTO<DemographicCreateResponseDTO>();

		responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		// responseDTO.setStatus(Boolean.TRUE);

		responseDTO.setErr(null);
		responseCreateDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
		responseCreateDTO.setErr(null);

		auditRequestDto.setActionTimeStamp(LocalDateTime.now(ZoneId.of("UTC")));
		auditRequestDto.setApplicationId(AuditLogVariables.MOSIP_1.toString());
		auditRequestDto.setApplicationName(AuditLogVariables.PREREGISTRATION.toString());
		auditRequestDto.setCreatedBy(AuditLogVariables.SYSTEM.toString());
		auditRequestDto.setHostIp(auditLogUtil.getServerIp());
		auditRequestDto.setHostName(auditLogUtil.getServerName());
		auditRequestDto.setId(AuditLogVariables.NO_ID.toString());
		auditRequestDto.setIdType(AuditLogVariables.PRE_REGISTRATION_ID.toString());
		auditRequestDto.setSessionUserId(AuditLogVariables.SYSTEM.toString());
		auditRequestDto.setSessionUserName(AuditLogVariables.SYSTEM.toString());
		AuthUserDetails applicationUser = Mockito.mock(AuthUserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);

		spyDemographicService = Mockito.spy(preRegistrationService);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void successSaveImplTest() throws Exception {

		byte[] encryptedDemographicDetails = { 1, 0, 1, 0, 1, 0 };

		Mockito.when(cryptoUtil.encrypt(Mockito.any(), Mockito.any())).thenReturn(encryptedDemographicDetails);

		preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		Mockito.when(pridGenerator.generateId()).thenReturn("67547447647457");
		Mockito.when(jsonValidator.validateJson(jsonObject.toString())).thenReturn(null);
		Mockito.when(demographicRepository.save(Mockito.any())).thenReturn(preRegistrationEntity);
		demographicResponseForCreateDTO = new DemographicCreateResponseDTO();
		demographicResponseForCreateDTO.setDemographicDetails(jsonObject);
		demographicResponseForCreateDTO.setPreRegistrationId("67547447647457");
		demographicResponseForCreateDTO
				.setCreatedDateTime(serviceUtil.getLocalDateString(LocalDateTime.now(ZoneId.of("UTC"))));
		demographicResponseForCreateDTO.setStatusCode("Pending_Appointment");
		createPreRegistrationDTO = new DemographicRequestDTO();
		createPreRegistrationDTO.setDemographicDetails(jsonObject);

		request.setRequest(createPreRegistrationDTO);
		List<DemographicCreateResponseDTO> listOfCreatePreRegistrationDTO = new ArrayList<>();
		listOfCreatePreRegistrationDTO.add(demographicResponseForCreateDTO);
		responseCreateDTO.setResponse(listOfCreatePreRegistrationDTO);

		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any())).thenReturn(jsonObject.toString().getBytes());
		MainListResponseDTO<DemographicCreateResponseDTO> actualRes = preRegistrationService
				.addPreRegistration(request);
		assertEquals(actualRes.getResponse().get(0).getStatusCode(),
				responseCreateDTO.getResponse().get(0).getStatusCode());
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = TableNotAccessibleException.class)
	public void saveFailureCheck() throws Exception {
		DataAccessLayerException exception = new DataAccessLayerException(ErrorCodes.PRG_PAM_APP_002.toString(),
				ErrorMessages.PRE_REGISTRATION_TABLE_NOT_ACCESSIBLE.toString(), null);
		byte[] encryptedDemographicDetails = { 1, 0, 1, 0, 1, 0 };

		Mockito.when(cryptoUtil.encrypt(Mockito.any(), Mockito.any())).thenReturn(encryptedDemographicDetails);

		preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		Mockito.when(jsonValidator.validateJson(jsonObject.toString())).thenReturn(null);
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any())).thenReturn(jsonObject.toString().getBytes());
		Mockito.when(demographicRepository.save(Mockito.any())).thenThrow(exception);
		createPreRegistrationDTO = new DemographicRequestDTO();
		createPreRegistrationDTO.setDemographicDetails(jsonObject);
		request.setRequest(createPreRegistrationDTO);
		preRegistrationService.addPreRegistration(request);
	}

	@Test
	public void successUpdateTest() throws Exception {
		byte[] encryptedDemographicDetails = { 1, 0, 1, 0, 1, 0 };

		Mockito.when(cryptoUtil.encrypt(Mockito.any(), Mockito.any())).thenReturn(encryptedDemographicDetails);

		preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		preRegistrationEntity.setDemogDetailHash(HashUtill.hashUtill(preRegistrationEntity.getApplicantDetailJson()));
		Mockito.when(jsonValidator.validateJson(jsonTestObject.toString())).thenReturn(null);
		Mockito.when(demographicRepository.findBypreRegistrationId("98746563542672")).thenReturn(preRegistrationEntity);
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any())).thenReturn(jsonObject.toString().getBytes());
		Mockito.when(demographicRepository.update(Mockito.any())).thenReturn(preRegistrationEntity);
		createPreRegistrationDTO = new DemographicRequestDTO();
		createPreRegistrationDTO.setDemographicDetails(jsonTestObject);
		request.setRequest(createPreRegistrationDTO);
		MainListResponseDTO<DemographicUpdateResponseDTO> res = preRegistrationService.updatePreRegistration(request,
				preId);
		assertEquals("98746563542672", res.getResponse().get(0).getPreRegistrationId());
	}

	@Test(expected = JsonValidationException.class)
	public void updateFailureCheck() throws Exception {
		HttpRequestException exception = new HttpRequestException(ErrorCodes.PRG_PAM_APP_007.name(),
				ErrorMessages.JSON_PARSING_FAILED.name());
		byte[] encryptedDemographicDetails = { 1, 0, 1, 0, 1, 0 };

		Mockito.when(cryptoUtil.encrypt(Mockito.any(), Mockito.any())).thenReturn(encryptedDemographicDetails);

		preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		Mockito.when(jsonValidator.validateJson(jsonTestObject.toString())).thenReturn(null);
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any())).thenReturn(jsonObject.toString().getBytes());
		Mockito.when(demographicRepository.findBypreRegistrationId("98746563542672")).thenReturn(preRegistrationEntity);

		Mockito.when(demographicRepository.update(Mockito.any())).thenThrow(exception);
		createPreRegistrationDTO = new DemographicRequestDTO();
		createPreRegistrationDTO.setDemographicDetails(jsonTestObject);
		request.setRequest(createPreRegistrationDTO);
		preRegistrationService.updatePreRegistration(request, preId);
	}

	@Test(expected = NullPointerException.class)
	public void createByDateFailureTest() throws Exception {
		InvalidRequestParameterException exception = new InvalidRequestParameterException(
				ErrorCodes.PRG_PAM_APP_012.toString(), ErrorMessages.MISSING_REQUEST_PARAMETER.toString());
		jsonObject = (JSONObject) parser.parse(new FileReader(fileCr));
		Mockito.when(jsonValidator.validateJson(jsonObject.toString())).thenReturn(null);

		preRegistrationEntity.setCreateDateTime(null);
		preRegistrationEntity.setCreatedBy("");
		preRegistrationEntity.setPreRegistrationId("");
		Mockito.when(demographicRepository.save(preRegistrationEntity)).thenThrow(exception);
		demographicResponseDTO = new DemographicResponseDTO();
		demographicResponseDTO.setDemographicDetails(jsonObject);
		demographicResponseDTO.setPreRegistrationId("");
		demographicResponseDTO.setCreatedBy("9988905444");
		demographicResponseDTO.setCreatedDateTime(serviceUtil.getLocalDateString(times));
		demographicResponseDTO.setStatusCode("Pending_Appointment");
		createPreRegistrationDTO = new DemographicRequestDTO();
		createPreRegistrationDTO.setDemographicDetails(jsonObject);
		request.setRequest(createPreRegistrationDTO);
		List<DemographicResponseDTO> listOfCreatePreRegistrationDTO = new ArrayList<>();
		listOfCreatePreRegistrationDTO.add(demographicResponseDTO);
		responseDTO.setResponse(listOfCreatePreRegistrationDTO);
		MainListResponseDTO<DemographicCreateResponseDTO> actualRes = preRegistrationService
				.addPreRegistration(request);
		assertEquals(actualRes.getResponse().get(0).getStatusCode(), responseDTO.getResponse().get(0).getStatusCode());

	}

	@Test(expected = InvalidRequestParameterException.class)
	public void updateByDateFailureTest() throws Exception {
		InvalidRequestParameterException exception = new InvalidRequestParameterException(
				ErrorCodes.PRG_PAM_APP_012.toString(), ErrorMessages.MISSING_REQUEST_PARAMETER.toString());
		jsonObject = (JSONObject) parser.parse(new FileReader(fileUp));
		Mockito.when(demographicRepository.findBypreRegistrationId(preId)).thenReturn(preRegistrationEntity);
		Mockito.when(jsonValidator.validateJson(Mockito.anyString())).thenThrow(exception);
		Mockito.when(demographicRepository.findBypreRegistrationId(preId)).thenReturn(preRegistrationEntity);
		Mockito.when(jsonValidator.validateJson(jsonObject.toString())).thenThrow(exception);
		MainListResponseDTO<DemographicUpdateResponseDTO> res = preRegistrationService.updatePreRegistration(request,
				"");
		assertEquals("1.0", res.getVersion());
	}

	@Test
	public void getApplicationDetailsTest() throws ParseException {
		byte[] encryptedDemographicDetails = { 1, 0, 1, 0, 1, 0 };

		Mockito.when(cryptoUtil.encrypt(Mockito.any(), Mockito.any())).thenReturn(encryptedDemographicDetails);

		preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		String userId = "9988905444";
		MainListResponseDTO<PreRegistrationViewDTO> response = new MainListResponseDTO<>();
		List<PreRegistrationViewDTO> viewList = new ArrayList<>();
		PreRegistrationViewDTO viewDto = new PreRegistrationViewDTO();

		viewDto = new PreRegistrationViewDTO();
		viewDto.setPreRegistrationId("98746563542672");
		viewDto.setStatusCode(preRegistrationEntity.getStatusCode());
		viewDto.setBookingRegistrationDTO(bookingRegistrationDTO);

		viewList.add(viewDto);
		response.setResponse(viewList);
		// response.setStatus(Boolean.FALSE);
		MainResponseDTO<BookingRegistrationDTO> bookingResultDto = new MainResponseDTO<>();
		BookingRegistrationDTO bookingResponse = new BookingRegistrationDTO();
		bookingResponse.setRegDate("12/01/2018");
		bookingResponse.setRegistrationCenterId("1");
		bookingResponse.setSlotFromTime("9:00:00");
		bookingResponse.setSlotToTime("10:00:00");
		bookingResultDto.setResponse(bookingResponse);
		ResponseEntity<MainResponseDTO> res = new ResponseEntity<>(bookingResultDto, HttpStatus.OK);
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any()))
				.thenReturn(userEntityDetails.get(0).getApplicantDetailJson());
		Mockito.when(demographicRepository.findByCreatedBy(userId, "Consumed")).thenReturn(userEntityDetails);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(MainResponseDTO.class))).thenReturn(res);
		MainListResponseDTO<PreRegistrationViewDTO> actualRes = preRegistrationService.getAllApplicationDetails(userId);
		assertEquals(actualRes.getVersion(), response.getVersion());

	}

	@Test
	public void callGetAppointmentDetailsRestServiceTest1()
			throws ParseException, org.json.simple.parser.ParseException {
		byte[] encryptedDemographicDetails = jsonTestObject.toJSONString().getBytes();

		Mockito.when(cryptoUtil.encrypt(Mockito.any(), Mockito.any())).thenReturn(encryptedDemographicDetails);

		preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		String userId = "9988905444";
		MainListResponseDTO<PreRegistrationViewDTO> response = new MainListResponseDTO<>();
		List<PreRegistrationViewDTO> viewList = new ArrayList<>();
		PreRegistrationViewDTO viewDto = new PreRegistrationViewDTO();

		viewDto = new PreRegistrationViewDTO();
		viewDto.setPreRegistrationId("98746563542672");
		viewDto.setStatusCode(preRegistrationEntity.getStatusCode());
		viewDto.setBookingRegistrationDTO(bookingRegistrationDTO);

		viewList.add(viewDto);
		response.setResponse(viewList);
		response.setVersion("1.0");
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any()))
				.thenReturn(userEntityDetails.get(0).getApplicantDetailJson());
		Mockito.when(demographicRepository.findByCreatedBy(userId, "Consumed")).thenReturn(userEntityDetails);
		MainResponseDTO<BookingRegistrationDTO> dto = new MainResponseDTO<>();
		dto.setErrors(null);
		ResponseEntity<MainResponseDTO<BookingRegistrationDTO>> respEntity = new ResponseEntity<>(dto, HttpStatus.OK);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

		Mockito.when(restTemplate1.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<MainResponseDTO<BookingRegistrationDTO>>() {
				}))).thenReturn(respEntity);
		MainListResponseDTO<PreRegistrationViewDTO> actualRes = preRegistrationService.getAllApplicationDetails(userId);
		assertEquals(actualRes.getVersion(), response.getVersion());

	}

	@Test
	public void callGetAppointmentDetailsRestServiceTest() throws ParseException {
		byte[] encryptedDemographicDetails = { 1, 0, 1, 0, 1, 0 };

		Mockito.when(cryptoUtil.encrypt(Mockito.any(), Mockito.any())).thenReturn(encryptedDemographicDetails);

		preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		String userId = "9988905444";
		MainListResponseDTO<PreRegistrationViewDTO> response = new MainListResponseDTO<>();
		List<PreRegistrationViewDTO> viewList = new ArrayList<>();
		PreRegistrationViewDTO viewDto = new PreRegistrationViewDTO();

		viewDto = new PreRegistrationViewDTO();
		viewDto.setPreRegistrationId("98746563542672");
		viewDto.setStatusCode(preRegistrationEntity.getStatusCode());
		viewDto.setBookingRegistrationDTO(bookingRegistrationDTO);

		viewList.add(viewDto);
		response.setResponse(viewList);
		MainResponseDTO<BookingRegistrationDTO> bookingResultDto = new MainResponseDTO<>();
		BookingRegistrationDTO bookingResponse = new BookingRegistrationDTO();
		bookingResponse.setRegDate("12/01/2018");
		bookingResponse.setRegistrationCenterId("1");
		bookingResponse.setSlotFromTime("9:00:00");
		bookingResponse.setSlotToTime("10:00:00");
		bookingResultDto.setResponse(bookingResponse);
		ResponseEntity<MainResponseDTO> res = new ResponseEntity<>(bookingResultDto, HttpStatus.OK);
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any()))
				.thenReturn(userEntityDetails.get(0).getApplicantDetailJson());
		Mockito.when(demographicRepository.findByCreatedBy(userId, "Consumed")).thenReturn(userEntityDetails);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(MainResponseDTO.class))).thenReturn(res);
		MainListResponseDTO<PreRegistrationViewDTO> actualRes = preRegistrationService.getAllApplicationDetails(userId);
		assertEquals(actualRes.getVersion(), response.getVersion());

	}

	@Test(expected = RecordNotFoundException.class)
	public void getApplicationDetailsFailureTest() {
		String userId = "12345";
		Mockito.when(demographicRepository.findByCreatedBy(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		preRegistrationService.getAllApplicationDetails(userId);

	}

	@Test(expected = InvalidRequestParameterException.class)
	public void getApplicationDetailsInvalidRequestTest() {
		InvalidRequestParameterException exception = new InvalidRequestParameterException(
				ErrorCodes.PRG_PAM_APP_012.name(), ErrorMessages.MISSING_REQUEST_PARAMETER.name());
		Mockito.when(demographicRepository.findByCreatedBy("", "")).thenThrow(exception);
		preRegistrationService.getAllApplicationDetails("");
	}

	@Test
	public void getApplicationStatusTest() {
		String preId = "98746563542672";
		byte[] encryptedDemographicDetails = { 1, 0, 1, 0, 1, 0 };

		// Mockito.when(cryptoUtil.encrypt(Mockito.any(),Mockito.any())).thenReturn(encryptedDemographicDetails);

		preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		preRegistrationEntity.setDemogDetailHash(HashUtill.hashUtill(preRegistrationEntity.getApplicantDetailJson()));
		MainListResponseDTO<PreRegistartionStatusDTO> response = new MainListResponseDTO<>();
		List<PreRegistartionStatusDTO> statusList = new ArrayList<PreRegistartionStatusDTO>();
		PreRegistartionStatusDTO statusDto = new PreRegistartionStatusDTO();
		statusDto.setPreRegistartionId(preId);
		statusDto.setStatusCode("Pending_Appointment");
		statusList.add(statusDto);
		response.setResponse(statusList);

		Mockito.when(demographicRepository.findBypreRegistrationId(ArgumentMatchers.any()))
				.thenReturn(preRegistrationEntity);

		MainListResponseDTO<PreRegistartionStatusDTO> actualRes = preRegistrationService.getApplicationStatus(preId);
		assertEquals(response.getResponse().get(0).getStatusCode(), actualRes.getResponse().get(0).getStatusCode());

	}

	@Test(expected = HashingException.class)
	public void getApplicationStatusHashingExceptionTest() {
		String preId = "98746563542672";
		byte[] encryptedDemographicDetails = { 1, 0, 1, 0, 1, 0 };

		// Mockito.when(cryptoUtil.encrypt(Mockito.any(),Mockito.any())).thenReturn(encryptedDemographicDetails);

		preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		// preRegistrationEntity.setDemogDetailHash(new
		// String(HashUtill.hashUtill(preRegistrationEntity.getApplicantDetailJson())));
		MainListResponseDTO<PreRegistartionStatusDTO> response = new MainListResponseDTO<>();
		List<PreRegistartionStatusDTO> statusList = new ArrayList<PreRegistartionStatusDTO>();
		PreRegistartionStatusDTO statusDto = new PreRegistartionStatusDTO();
		statusDto.setPreRegistartionId(preId);
		statusDto.setStatusCode("Pending_Appointment");
		statusList.add(statusDto);
		response.setResponse(statusList);

		Mockito.when(demographicRepository.findBypreRegistrationId(ArgumentMatchers.any()))
				.thenReturn(preRegistrationEntity);

		MainListResponseDTO<PreRegistartionStatusDTO> actualRes = preRegistrationService.getApplicationStatus(preId);
		assertEquals(response.getResponse().get(0).getStatusCode(), actualRes.getResponse().get(0).getStatusCode());

	}

	@Test(expected = RecordNotFoundException.class)
	public void getApplicationStatusFailure() {
		String preId = "98746563542672";
		MainListResponseDTO<PreRegistartionStatusDTO> response = new MainListResponseDTO<>();
		List<PreRegistartionStatusDTO> statusList = new ArrayList<PreRegistartionStatusDTO>();
		PreRegistartionStatusDTO statusDto = new PreRegistartionStatusDTO();
		statusDto.setPreRegistartionId(preId);
		statusDto.setStatusCode("Pending_Appointment");
		statusList.add(statusDto);
		response.setResponse(statusList);

		Mockito.when(demographicRepository.findBypreRegistrationId(ArgumentMatchers.any())).thenReturn(null);

		MainListResponseDTO<PreRegistartionStatusDTO> actualRes = preRegistrationService.getApplicationStatus(preId);
		assertEquals(response.getResponse().get(0).getStatusCode(), actualRes.getResponse().get(0).getStatusCode());

	}

	@Test(expected = TableNotAccessibleException.class)
	public void getApplicationDetailsTransactionFailureCheck() throws Exception {
		String userId = "9988905444";
		DataAccessLayerException exception = new DataAccessLayerException(ErrorCodes.PRG_PAM_APP_002.toString(),
				ErrorMessages.PRE_REGISTRATION_TABLE_NOT_ACCESSIBLE.toString(), null);
		Mockito.when(demographicRepository.findByCreatedBy(Mockito.anyString(), Mockito.anyString()))
				.thenThrow(exception);
		preRegistrationService.getAllApplicationDetails(userId);
	}

	@Test(expected = RecordNotFoundException.class)
	public void deleteRecordNotFoundTest() {
		RecordNotFoundException exception = new RecordNotFoundException(ErrorCodes.PRG_PAM_APP_005.name(),
				ErrorMessages.UNABLE_TO_FETCH_THE_PRE_REGISTRATION.name());
		String preRegId = "98746563542672";

		DocumentDeleteDTO deleteDTO = new DocumentDeleteDTO();
		deleteDTO.setDocument_Id(String.valueOf("1"));
		List<DocumentDeleteDTO> deleteAllList = new ArrayList<>();
		deleteAllList.add(deleteDTO);

		MainListResponseDTO<DocumentDeleteDTO> delResponseDto = new MainListResponseDTO<>();
		// delResponseDto.setStatus(Boolean.TRUE);
		delResponseDto.setErr(null);
		delResponseDto.setResponse(deleteAllList);
		delResponseDto.setResponsetime(serviceUtil.getCurrentResponseTime());

		ResponseEntity<MainListResponseDTO> res = new ResponseEntity<>(delResponseDto, HttpStatus.OK);
		Mockito.when(demographicRepository.findBypreRegistrationId(preRegId)).thenReturn(null);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.DELETE), Mockito.any(),
				Mockito.eq(MainListResponseDTO.class))).thenThrow(exception);
		preRegistrationService.deleteIndividual(preRegId);

	}

	@Test(expected = RecordFailedToDeleteException.class)
	public void deleteRecordFailedToDeleteException() throws Exception {
		String preRegId = "98746563542672";
		ExceptionJSONInfoDTO err = new ExceptionJSONInfoDTO("PRG_PAM_APP_004",
				ErrorMessages.FAILED_TO_DELETE_THE_PRE_REGISTRATION_RECORD.name());
		Mockito.when(demographicRepository.findBypreRegistrationId(preRegId)).thenReturn(preRegistrationEntity);
		DocumentDeleteResponseDTO deleteDTO = new DocumentDeleteResponseDTO();
		deleteDTO.setDocument_Id(String.valueOf("1"));
		List<DocumentDeleteResponseDTO> deleteAllList = new ArrayList<>();
		deleteAllList.add(deleteDTO);
		MainListResponseDTO<DeleteBookingDTO> delBookingResponseDTO = new MainListResponseDTO<>();
		DeleteBookingDTO deleteBookingDTO = new DeleteBookingDTO();
		deleteBookingDTO.setPreRegistrationId("98746563542672");
		List<DeleteBookingDTO> list = new ArrayList<>();
		list.add(deleteBookingDTO);
		delBookingResponseDTO.setResponse(list);
		MainListResponseDTO<DocumentDeleteResponseDTO> delResponseDto = new MainListResponseDTO<>();
		List<ExceptionJSONInfoDTO> exceptionJSONInfoDTOs = new ArrayList<>();
		exceptionJSONInfoDTOs.add(err);

		delResponseDto.setErr(null);
		delResponseDto.setResponse(deleteAllList);
		delResponseDto.setResponsetime(serviceUtil.getCurrentResponseTime());

		ResponseEntity<MainListResponseDTO<DocumentDeleteResponseDTO>> res = new ResponseEntity<>(delResponseDto,
				HttpStatus.OK);
		ResponseEntity<MainListResponseDTO<DeleteBookingDTO>> res1 = new ResponseEntity<>(delBookingResponseDTO,
				HttpStatus.OK);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		Mockito.when(restTemplate1.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.DELETE), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<MainListResponseDTO<DocumentDeleteResponseDTO>>() {
				}))).thenReturn(res);
		Mockito.when(restTemplate1.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.DELETE), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<MainListResponseDTO<DeleteBookingDTO>>() {
				}))).thenReturn(res1);
		Mockito.when(demographicRepository.deleteByPreRegistrationId(preRegistrationEntity.getPreRegistrationId()))
				.thenReturn(0);
		preRegistrationService.deleteIndividual(preRegId);
	}

	@Test(expected = DocumentFailedToDeleteException.class)
	public void documentFailedToDeleteExceptionTest() throws Exception {
		String preRegId = "98746563542672";
		ExceptionJSONInfoDTO err = new ExceptionJSONInfoDTO("PRG_PAM_DOC_015", "");
		Mockito.when(demographicRepository.findBypreRegistrationId(preRegId)).thenReturn(preRegistrationEntity);
		DocumentDeleteResponseDTO deleteDTO = new DocumentDeleteResponseDTO();
		deleteDTO.setDocument_Id(String.valueOf("1"));
		List<DocumentDeleteResponseDTO> deleteAllList = new ArrayList<>();
		deleteAllList.add(deleteDTO);
		MainListResponseDTO<DeleteBookingDTO> delBookingResponseDTO = new MainListResponseDTO<>();
		DeleteBookingDTO deleteBookingDTO = new DeleteBookingDTO();
		deleteBookingDTO.setPreRegistrationId("98746563542672");
		List<DeleteBookingDTO> list = new ArrayList<>();
		list.add(deleteBookingDTO);
		delBookingResponseDTO.setResponse(list);
		MainListResponseDTO<DocumentDeleteResponseDTO> delResponseDto = new MainListResponseDTO<>();
		List<ExceptionJSONInfoDTO> exceptionJSONInfoDTOs = new ArrayList<>();
		exceptionJSONInfoDTOs.add(err);

		delResponseDto.setErr(err);
		delResponseDto.setResponse(deleteAllList);
		delResponseDto.setResponsetime(serviceUtil.getCurrentResponseTime());

		ResponseEntity<MainListResponseDTO<DocumentDeleteResponseDTO>> res = new ResponseEntity<>(delResponseDto,
				HttpStatus.OK);
		ResponseEntity<MainListResponseDTO<DeleteBookingDTO>> res1 = new ResponseEntity<>(delBookingResponseDTO,
				HttpStatus.OK);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		Mockito.when(restTemplate1.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.DELETE), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<MainListResponseDTO<DocumentDeleteResponseDTO>>() {
				}))).thenReturn(res);
		Mockito.when(restTemplate1.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.DELETE), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<MainListResponseDTO<DeleteBookingDTO>>() {
				}))).thenReturn(res1);
		Mockito.when(demographicRepository.deleteByPreRegistrationId(preRegistrationEntity.getPreRegistrationId()))
				.thenReturn(1);

		Mockito.when(demographicRepository.deleteByPreRegistrationId(preRegId)).thenReturn(0);
		preRegistrationService.deleteIndividual(preRegId);

	}

	@Test(expected = BookingDeletionFailedException.class)
	public void deleteRecordRestCallException() throws Exception {
		String preRegId = "98746563542672";
		ExceptionJSONInfoDTO err = new ExceptionJSONInfoDTO("PRG_PAM_DOC_015", "");
		Mockito.when(demographicRepository.findBypreRegistrationId(preRegId)).thenReturn(preRegistrationEntity);
		preRegistrationEntity.setCreateDateTime(times);
		preRegistrationEntity.setCreatedBy("9988905444");
		preRegistrationEntity.setStatusCode("Booked");
		preRegistrationEntity.setUpdateDateTime(times);
		preRegistrationEntity.setApplicantDetailJson(jsonTestObject.toJSONString().getBytes());
		preRegistrationEntity.setPreRegistrationId("98746563542672");

		DocumentDeleteResponseDTO deleteDTO = new DocumentDeleteResponseDTO();
		deleteDTO.setDocument_Id(String.valueOf("1"));
		List<DocumentDeleteResponseDTO> deleteAllList = new ArrayList<>();
		deleteAllList.add(deleteDTO);
		MainListResponseDTO<DeleteBookingDTO> delBookingResponseDTO = new MainListResponseDTO<>();
		DeleteBookingDTO deleteBookingDTO = new DeleteBookingDTO();
		deleteBookingDTO.setPreRegistrationId("98746563542672");
		List<DeleteBookingDTO> list = new ArrayList<>();
		list.add(deleteBookingDTO);
		delBookingResponseDTO.setResponse(list);
		delBookingResponseDTO.setErr(err);
		MainListResponseDTO<DocumentDeleteResponseDTO> delResponseDto = new MainListResponseDTO<>();

		delResponseDto.setErr(null);
		delResponseDto.setResponse(deleteAllList);
		delResponseDto.setResponsetime(serviceUtil.getCurrentResponseTime());

		ResponseEntity<MainListResponseDTO<DocumentDeleteResponseDTO>> res = new ResponseEntity<>(delResponseDto,
				HttpStatus.OK);
		ResponseEntity<MainListResponseDTO<DeleteBookingDTO>> res1 = new ResponseEntity<>(delBookingResponseDTO,
				HttpStatus.OK);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		Mockito.when(restTemplate1.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.DELETE), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<MainListResponseDTO<DocumentDeleteResponseDTO>>() {
				}))).thenReturn(res);
		Mockito.when(restTemplate1.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.DELETE), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<MainListResponseDTO<DeleteBookingDTO>>() {
				}))).thenReturn(res1);
		Mockito.when(demographicRepository.deleteByPreRegistrationId(preRegistrationEntity.getPreRegistrationId()))
				.thenReturn(1);

		Mockito.when(demographicRepository.deleteByPreRegistrationId(preRegId)).thenReturn(0);
		preRegistrationService.deleteIndividual(preRegId);

	}

	@MockBean
	private RestTemplate restTemplate1;

	@Test
	public void deleteIndividualSuccessTest() {
		String preRegId = "98746563542672";
		preRegistrationEntity.setCreateDateTime(times);
		preRegistrationEntity.setCreatedBy("9988905444");
		preRegistrationEntity.setStatusCode("Booked");
		preRegistrationEntity.setUpdateDateTime(times);
		preRegistrationEntity.setApplicantDetailJson(jsonTestObject.toJSONString().getBytes());
		preRegistrationEntity.setPreRegistrationId("98746563542672");

		DocumentDeleteResponseDTO deleteDTO = new DocumentDeleteResponseDTO();
		deleteDTO.setDocument_Id(String.valueOf("1"));
		List<DocumentDeleteResponseDTO> deleteAllList = new ArrayList<>();
		deleteAllList.add(deleteDTO);
		MainListResponseDTO<DeleteBookingDTO> delBookingResponseDTO = new MainListResponseDTO<>();
		DeleteBookingDTO deleteBookingDTO = new DeleteBookingDTO();
		deleteBookingDTO.setPreRegistrationId("98746563542672");
		List<DeleteBookingDTO> list = new ArrayList<>();
		list.add(deleteBookingDTO);
		delBookingResponseDTO.setResponse(list);
		MainListResponseDTO<DocumentDeleteResponseDTO> delResponseDto = new MainListResponseDTO<>();
		// delResponseDto.setStatus(Boolean.TRUE);
		delResponseDto.setErr(null);
		delResponseDto.setResponse(deleteAllList);
		delResponseDto.setResponsetime(serviceUtil.getCurrentResponseTime());

		Mockito.when(demographicRepository.findBypreRegistrationId(preRegId)).thenReturn(preRegistrationEntity);

		ResponseEntity<MainListResponseDTO<DocumentDeleteResponseDTO>> res = new ResponseEntity<>(delResponseDto,
				HttpStatus.OK);
		ResponseEntity<MainListResponseDTO<DeleteBookingDTO>> res1 = new ResponseEntity<>(delBookingResponseDTO,
				HttpStatus.OK);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		Mockito.when(restTemplate1.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.DELETE), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<MainListResponseDTO<DocumentDeleteResponseDTO>>() {
				}))).thenReturn(res);
		Mockito.when(restTemplate1.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.DELETE), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<MainListResponseDTO<DeleteBookingDTO>>() {
				}))).thenReturn(res1);
		Mockito.when(demographicRepository.deleteByPreRegistrationId(preRegistrationEntity.getPreRegistrationId()))
				.thenReturn(1);

		MainListResponseDTO<DeletePreRegistartionDTO> actualres = preRegistrationService.deleteIndividual(preRegId);

		assertEquals("1.0", actualres.getVersion());

	}

	@Test
	public void updateByPreIdTest() {
		Mockito.when(demographicRepository.findBypreRegistrationId("98746563542672")).thenReturn(preRegistrationEntity);
		createPreRegistrationDTO = new DemographicRequestDTO();
		createPreRegistrationDTO.setDemographicDetails(jsonTestObject);
		request.setRequest(createPreRegistrationDTO);
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any())).thenReturn(jsonObject.toString().getBytes());
		preRegistrationService.updatePreRegistration(request, preId);
	}

	@Test(expected = RestCallException.class)
	public void deleteIndividualRestExceptionTest() throws Exception {
		String preRegId = "98746563542672";
		preRegistrationEntity.setCreateDateTime(times);
		preRegistrationEntity.setCreatedBy("9988905444");
		preRegistrationEntity.setStatusCode("Booked");
		preRegistrationEntity.setUpdateDateTime(times);
		preRegistrationEntity.setApplicantDetailJson(jsonTestObject.toJSONString().getBytes());
		preRegistrationEntity.setPreRegistrationId("98746563542672");

		DocumentDeleteResponseDTO deleteDTO = new DocumentDeleteResponseDTO();
		deleteDTO.setDocument_Id(String.valueOf("1"));
		List<DocumentDeleteResponseDTO> deleteAllList = new ArrayList<>();
		deleteAllList.add(deleteDTO);

		MainListResponseDTO<DocumentDeleteResponseDTO> delResponseDto = new MainListResponseDTO<>();
		delResponseDto.setErr(null);
		delResponseDto.setResponse(deleteAllList);

		Mockito.when(demographicRepository.findBypreRegistrationId(preRegId)).thenReturn(preRegistrationEntity);

		ResponseEntity<MainListResponseDTO<DocumentDeleteResponseDTO>> res = new ResponseEntity<>(delResponseDto,
				HttpStatus.OK);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.DELETE), Mockito.any(),
				Mockito.eq(new ParameterizedTypeReference<MainListResponseDTO<DocumentDeleteResponseDTO>>() {
				}))).thenThrow(RestClientException.class);

		Mockito.when(demographicRepository.deleteByPreRegistrationId(preRegistrationEntity.getPreRegistrationId()))
				.thenReturn(1);

		MainListResponseDTO<DeletePreRegistartionDTO> actualres = preRegistrationService.deleteIndividual(preRegId);

		// assertEquals(true, actualres.isStatus());

	}

	@Test(expected = RecordNotFoundException.class)
	public void RecordNotFoundExceptionTest() {
		Mockito.when(demographicRepository.findBypreRegistrationId("98746563542672")).thenReturn(null);
		createPreRegistrationDTO = new DemographicRequestDTO();
		createPreRegistrationDTO.setDemographicDetails(jsonTestObject);
		request.setRequest(createPreRegistrationDTO);
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any())).thenReturn(jsonObject.toString().getBytes());
		preRegistrationService.updatePreRegistration(request, preId);
	}

	@Test
	public void getPreRegistrationTest() {
		byte[] encryptedDemographicDetails = { 1, 0, 1, 0, 1, 0 };

		// Mockito.when(cryptoUtil.encrypt(Mockito.any(),Mockito.any())).thenReturn(encryptedDemographicDetails);

		preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		preRegistrationEntity.setDemogDetailHash(HashUtill.hashUtill(preRegistrationEntity.getApplicantDetailJson()));
		Mockito.when(demographicRepository.findBypreRegistrationId("98746563542672")).thenReturn(preRegistrationEntity);
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any())).thenReturn(jsonObject.toString().getBytes());
		MainListResponseDTO<DemographicResponseDTO> res = preRegistrationService.getDemographicData("98746563542672");
		assertEquals("98746563542672", res.getResponse().get(0).getPreRegistrationId());
	}

	@Test(expected = HashingException.class)
	public void getPreRegistrationHashingExceptionTest() {
		byte[] encryptedDemographicDetails = { 1, 0, 1, 0, 1, 0 };

		// Mockito.when(cryptoUtil.encrypt(Mockito.any(),Mockito.any())).thenReturn(encryptedDemographicDetails);

		preRegistrationEntity.setApplicantDetailJson(encryptedDemographicDetails);
		// preRegistrationEntity.setDemogDetailHash(new
		// String(HashUtill.hashUtill(preRegistrationEntity.getApplicantDetailJson())));
		Mockito.when(demographicRepository.findBypreRegistrationId("98746563542672")).thenReturn(preRegistrationEntity);
		Mockito.when(cryptoUtil.decrypt(Mockito.any(), Mockito.any())).thenReturn(jsonObject.toString().getBytes());
		MainListResponseDTO<DemographicResponseDTO> res = preRegistrationService.getDemographicData("98746563542672");
		assertEquals("98746563542672", res.getResponse().get(0).getPreRegistrationId());
	}

	@Test(expected = RecordNotFoundException.class)
	public void getPreRegistrationFailureTest() {
		Mockito.when(demographicRepository.findBypreRegistrationId("98746563542672")).thenReturn(null);
		preRegistrationService.getDemographicData("98746563542672");
	}

	@Test
	public void updatePreRegistrationStatusTest() {
		Mockito.when(demographicRepository.findBypreRegistrationId("98746563542672")).thenReturn(preRegistrationEntity);
		MainResponseDTO<String> res = preRegistrationService.updatePreRegistrationStatus("98746563542672", "Booked");
	}

	@Test(expected = RecordNotFoundException.class)
	public void updatePreRegistrationStatusFailureTest1() {
		Mockito.when(demographicRepository.findBypreRegistrationId("98746563542672")).thenReturn(null);
		preRegistrationService.updatePreRegistrationStatus("98746563542672", "Booked");
	}

	@Test(expected = RecordFailedToUpdateException.class)
	public void updatePreRegistrationStatusFailureTest2() {
		Mockito.when(demographicRepository.findBypreRegistrationId("98746563542672")).thenReturn(preRegistrationEntity);
		preRegistrationService.updatePreRegistrationStatus("98746563542672", "NA");
	}

	@Mock
	private DemographicEntity entity;

	@Test
	public void getApplicationByDateTest() {
		List<String> list = new ArrayList<>();

		LocalDate fromDate = LocalDate.now();
		LocalDate toDate = LocalDate.now();
		MainListResponseDTO<String> response = new MainListResponseDTO<>();
		List<String> preIds = new ArrayList<>();
		List<DemographicEntity> details = new ArrayList<>();
		// DemographicEntity entity = new DemographicEntity();
		entity.setPreRegistrationId("98746563542672");
		details.add(entity);
		byte[] sampleAppJson = "JSON".getBytes();
		preIds.add("98746563542672");
		response.setResponse(preIds);
		response.setVersion("1.0");
		// response.setStatus(true);
		Mockito.doReturn(list).when(spyDemographicService).getPreRegistrationByDateEntityCheck(details);
		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		Date myFromDate;
		// myFromDate = DateUtils.parseToDate(URLDecoder.decode(fromDate, "UTF-8"),
		// dateFormat);

		// Date myToDate = DateUtils.parseToDate(URLDecoder.decode(toDate, "UTF-8"),
		// dateFormat);
		LocalDateTime fromLocaldate = fromDate.atStartOfDay();

		LocalDateTime toLocaldate = toDate.atTime(23, 59, 59);
		Mockito.when(demographicRepository.findBycreateDateTimeBetween(fromLocaldate, toLocaldate)).thenReturn(details);
		Mockito.when(entity.getApplicantDetailJson()).thenReturn(sampleAppJson);
		MainListResponseDTO<String> actualRes = spyDemographicService.getPreRegistrationByDate(fromDate, toDate);
		assertEquals(actualRes.getVersion(), response.getVersion());

	}

	@Test(expected = RecordNotFoundException.class)
	public void getApplicationByDateFailureTest() {

		LocalDate fromDate = LocalDate.now();
		LocalDate toDate = LocalDate.now();
		LocalDateTime fromLocaldate = fromDate.atStartOfDay();

		LocalDateTime toLocaldate = toDate.atTime(23, 59, 59);
		MainListResponseDTO<String> response = new MainListResponseDTO<>();
		List<String> preIds = new ArrayList<>();
		DemographicEntity entity = new DemographicEntity();
		entity.setPreRegistrationId("98746563542672");
		preIds.add("98746563542672");
		response.setResponse(preIds);
		// response.setStatus(true);

		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		Date myFromDate;

		Mockito.when(demographicRepository.findBycreateDateTimeBetween(fromLocaldate, toLocaldate)).thenReturn(null);
		preRegistrationService.getPreRegistrationByDate(fromDate, toDate);

	}

	@Test(expected = RecordNotFoundException.class)
	public void getPreRegistrationByDateExceptionTest() {
		LocalDate fromDate = LocalDate.now();
		LocalDate toDate = LocalDate.now();
		preRegistrationService.getPreRegistrationByDate(fromDate, toDate);
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = SystemUnsupportedEncodingException.class)
	public void getBydateFailureCheck() throws Exception {
		SystemUnsupportedEncodingException exception = new SystemUnsupportedEncodingException(
				ErrorCodes.PRG_PAM_APP_009.name(), ErrorMessages.UNSUPPORTED_ENCODING_CHARSET.name());

		MainListResponseDTO<String> response = new MainListResponseDTO<>();
		List<String> preIds = new ArrayList<>();
		List<DemographicEntity> details = new ArrayList<>();
		DemographicEntity entity = new DemographicEntity();
		entity.setPreRegistrationId("98746563542672");
		details.add(entity);
		preIds.add("98746563542672");
		response.setResponse(preIds);

		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		Date myFromDate;
		Date myToDate;
		LocalDateTime fromLocaldate = fromDate.atStartOfDay();

		LocalDateTime toLocaldate = toDate.atTime(23, 59, 59);

		Mockito.when(demographicRepository.findBycreateDateTimeBetween(fromLocaldate, toLocaldate))
				.thenThrow(exception);
		preRegistrationService.getPreRegistrationByDate(fromDate, toDate);

	}

	/**
	 * @throws Exception
	 */
	@Test(expected = DateParseException.class)
	public void getBydateFailureParseCheck() throws Exception {
		DateParseException exception = new DateParseException(ErrorCodes.PRG_PAM_APP_011.name(),
				ErrorMessages.UNSUPPORTED_DATE_FORMAT.name());

		MainListResponseDTO<String> response = new MainListResponseDTO<>();
		List<String> preIds = new ArrayList<>();
		List<DemographicEntity> details = new ArrayList<>();
		DemographicEntity entity = new DemographicEntity();
		entity.setPreRegistrationId("98746563542672");
		details.add(entity);

		preIds.add("98746563542672");
		response.setResponse(preIds);

		LocalDateTime fromLocaldate = fromDate.atStartOfDay();

		LocalDateTime toLocaldate = toDate.atTime(23, 59, 59);

		Mockito.when(demographicRepository.findBycreateDateTimeBetween(fromLocaldate, toLocaldate))
				.thenThrow(exception);
		preRegistrationService.getPreRegistrationByDate(fromDate, toDate);

	}

	@Test
	public void callGetUpdatedDateTimeRestServiceTest() throws ParseException, org.json.simple.parser.ParseException {
		MainResponseDTO<Map<String, String>> dto = new MainResponseDTO<>();
		List<String> preIds = new ArrayList<>();
		preIds.add("98746563542672");
		PreRegIdsByRegCenterIdDTO preRegIdsByRegCenterIdDTO = new PreRegIdsByRegCenterIdDTO();
		preRegIdsByRegCenterIdDTO.setPreRegistrationIds(preIds);

		dto.setErrors(null);
		dto.setResponsetime(LocalDateTime.now().toString());
		dto.setVersion("1.0");
		Map<String, String> map = new HashMap<>();
		map.put("98746563542672", LocalDateTime.now().toString());

		dto.setResponse(map);

		Mockito.when(demographicRepository.findByStatusCodeAndPreRegistrationIdIn(StatusCodes.BOOKED.getCode(), preIds))
				.thenReturn(userEntityDetails);
		ResponseEntity<MainResponseDTO> respEntity = new ResponseEntity<>(dto, HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(),
				Mockito.eq(MainResponseDTO.class))).thenThrow(RestClientException.class);
		MainResponseDTO<Map<String, String>> actualRes = preRegistrationService
				.getUpdatedDateTimeForPreIds(preRegIdsByRegCenterIdDTO);
		assertEquals(actualRes.getVersion(), dto.getVersion());

	}

	@Test(expected = RecordNotFoundForPreIdsException.class)
	public void invalidPreidFailureTest() {
		List<String> preIds = new ArrayList<>();
		preIds.add("");
		PreRegIdsByRegCenterIdDTO preRegIdsByRegCenterIdDTO = new PreRegIdsByRegCenterIdDTO();
		preRegIdsByRegCenterIdDTO.setPreRegistrationIds(null);
		Mockito.when(demographicRepository.findByStatusCodeAndPreRegistrationIdIn(StatusCodes.BOOKED.getCode(), preIds))
				.thenReturn(userEntityDetails);
		preRegistrationService.getUpdatedDateTimeForPreIds(preRegIdsByRegCenterIdDTO);

	}

	@Test(expected = RecordNotFoundForPreIdsException.class)
	public void recordeNotFoundTest() {
		List<String> preIds = new ArrayList<>();
		preIds.add("userEntityDetails");
		PreRegIdsByRegCenterIdDTO preRegIdsByRegCenterIdDTO = new PreRegIdsByRegCenterIdDTO();
		preRegIdsByRegCenterIdDTO.setPreRegistrationIds(preIds);
		userEntityDetails = null;
		Mockito.when(demographicRepository.findByStatusCodeAndPreRegistrationIdIn(StatusCodes.BOOKED.getCode(), preIds))
				.thenReturn(userEntityDetails);
		preRegistrationService.getUpdatedDateTimeForPreIds(preRegIdsByRegCenterIdDTO);

	}

	@Test(expected = RecordNotFoundForPreIdsException.class)
	public void recordeNotForPreIdFoundTest() {
		List<String> preIds = new ArrayList<>();
		preIds.add("userEntityDetails");
		PreRegIdsByRegCenterIdDTO preRegIdsByRegCenterIdDTO = new PreRegIdsByRegCenterIdDTO();
		preRegIdsByRegCenterIdDTO.setPreRegistrationIds(null);
		userEntityDetails = null;
		Mockito.when(demographicRepository.findByStatusCodeAndPreRegistrationIdIn(StatusCodes.BOOKED.getCode(), preIds))
				.thenReturn(userEntityDetails);
		preRegistrationService.getUpdatedDateTimeForPreIds(preRegIdsByRegCenterIdDTO);

	}

	@Test
	public void getPreRegistrationByDateEntityCheckSuccessTest() {
		DemographicEntity demoEntity = new DemographicEntity();
		demoEntity.setDemogDetailHash("E0B6CBDAC6D896BCB5061846A054CD1373397D02A9F83CEFDBC912317E6E0331");
		demoEntity.setApplicantDetailJson("MOSIP".getBytes());
		demoEntity.setPreRegistrationId("123456");
		List<DemographicEntity> list = new ArrayList<>();
		list.add(demoEntity);
		List<String> resList = preRegistrationService.getPreRegistrationByDateEntityCheck(list);
		assertEquals(resList.get(0), "123456");
	}

	@Test(expected = HashingException.class)
	public void getPreRegistrationByDateEntityCheckExceptionTest() {
		DemographicEntity demoEntity = new DemographicEntity();
		demoEntity.setDemogDetailHash("");
		demoEntity.setApplicantDetailJson("MOSIP".getBytes());
		demoEntity.setPreRegistrationId("123456");
		List<DemographicEntity> list = new ArrayList<>();
		list.add(demoEntity);
		List<String> resList = preRegistrationService.getPreRegistrationByDateEntityCheck(list);

	}

}