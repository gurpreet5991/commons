package io.mosip.kernel.syncdata.test.service;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.syncdata.dto.ApplicationDto;
import io.mosip.kernel.syncdata.dto.HolidayDto;
import io.mosip.kernel.syncdata.dto.MachineDto;
import io.mosip.kernel.syncdata.dto.MachineSpecificationDto;
import io.mosip.kernel.syncdata.dto.MachineTypeDto;
import io.mosip.kernel.syncdata.dto.PublicKeyResponse;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserDto;
import io.mosip.kernel.syncdata.dto.response.MasterDataResponseDto;
import io.mosip.kernel.syncdata.dto.response.RegistrationCenterUserResponseDto;
import io.mosip.kernel.syncdata.entity.Machine;
import io.mosip.kernel.syncdata.exception.SyncDataServiceException;
import io.mosip.kernel.syncdata.exception.SyncInvalidArgumentException;
import io.mosip.kernel.syncdata.repository.MachineRepository;
import io.mosip.kernel.syncdata.service.RegistrationCenterUserService;
import io.mosip.kernel.syncdata.service.SyncConfigDetailsService;
import io.mosip.kernel.syncdata.service.SyncMasterDataService;
import io.mosip.kernel.syncdata.service.SyncRolesService;
import io.mosip.kernel.syncdata.service.SyncUserDetailsService;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;
import net.minidev.json.JSONObject;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SyncDataServiceTest {

	@MockBean
	private SyncMasterDataServiceHelper masterDataServiceHelper;

	@Autowired
	private SyncMasterDataService masterDataService;

	@Autowired
	RestTemplate restTemplate;

	@MockBean
	private RegistrationCenterUserService registrationCenterUserService;

	@MockBean
	private ObjectMapper objectMapper;

	@Autowired
	private SyncUserDetailsService syncUserDetailsService;

	@MockBean
	MachineRepository machineRespository;

	@Autowired
	private SyncRolesService syncRolesService;

	/**
	 * Environment instance
	 */
	@Autowired
	private Environment env;

	/**
	 * file name referred from the properties file
	 */
	@Value("${mosip.kernel.syncdata.registration-center-config-file}")
	private String regCenterfileName;

	/**
	 * file name referred from the properties file
	 */
	@Value("${mosip.kernel.syncdata.global-config-file}")
	private String globalConfigFileName;

	@Value("${mosip.kernel.syncdata.auth-manager-base-uri}")
	private String authUserDetailsBaseUri;

	@Value("${mosip.kernel.syncdata.auth-user-details:/userdetails}")
	private String authUserDetailsUri;

	@Value("${mosip.kernel.syncdata.auth-manager-base-uri}")
	private String authBaseUri;

	@Value("${mosip.kernel.syncdata.auth-manager-roles}")
	private String authAllRolesUri;

	@Value("${mosip.kernel.syncdata.public-key-url}")
	private String publicKeyUrl;

	private String configServerUri = null;
	private String configLabel = null;
	private String configProfile = null;
	private String configAppName = null;

	private StringBuilder uriBuilder;

	StringBuilder userDetailsUri;

	private StringBuilder builder;

	@Autowired
	private SyncConfigDetailsService syncConfigDetailsService;
	private MasterDataResponseDto masterDataResponseDto;
	private List<ApplicationDto> applications;
	List<HolidayDto> holidays;
	List<MachineDto> machines;
	List<MachineSpecificationDto> machineSpecifications;
	List<MachineTypeDto> machineTypes;
	Map<String, String> uriParams = null;

	JSONObject globalConfigMap = null;
	JSONObject regCentreConfigMap = null;

	@Before
	public void setup() {
		masterDataSyncSetup();
		configDetialsSyncSetup();
		userDetailsUri = new StringBuilder();
		userDetailsUri.append(authUserDetailsBaseUri).append(authUserDetailsUri);

	}

	public void masterDataSyncSetup() {
		masterDataResponseDto = new MasterDataResponseDto();
		applications = new ArrayList<>();
		applications.add(new ApplicationDto("01", "REG FORM", "REG Form"));
		masterDataResponseDto.setApplications(applications);
		holidays = new ArrayList<>();
		holidays.add(new HolidayDto("1", "2018-01-01", "01", "01", "2018", "NEW YEAR", "LOC01"));
		masterDataResponseDto.setHolidays(holidays);
		machines = new ArrayList<>();
		machines.add(new MachineDto("1001", "Laptop", "QWE23456", "1223:23:31:23", "172.12.128.1", "1",
				LocalDateTime.parse("2018-01-01T01:01:01")));
		masterDataResponseDto.setMachineDetails(machines);
		machineSpecifications = new ArrayList<>();
		machineSpecifications
				.add(new MachineSpecificationDto("1", "lenovo Thinkpad", "Lenovo", "T480", "1", "1.0.1", "Thinkpad"));
		masterDataResponseDto.setMachineSpecification(machineSpecifications);
		machineTypes = new ArrayList<>();
		machineTypes.add(new MachineTypeDto("1", "Laptop", "Laptop"));
		masterDataResponseDto.setMachineType(machineTypes);
	}

	public void configDetialsSyncSetup() {
		globalConfigMap = new JSONObject();
		globalConfigMap.put("archivalPolicy", "arc_policy_2");
		globalConfigMap.put("otpTimeOutInMinutes", 2);
		globalConfigMap.put("numberOfWrongAttemptsForOtp", 5);
		globalConfigMap.put("uinLength", 24);

		regCentreConfigMap = new JSONObject();

		regCentreConfigMap.put("fingerprintQualityThreshold", 120);
		regCentreConfigMap.put("irisQualityThreshold", 25);
		regCentreConfigMap.put("irisRetryAttempts", 10);
		regCentreConfigMap.put("faceQualityThreshold", 25);
		regCentreConfigMap.put("faceRetry", 12);
		regCentreConfigMap.put("supervisorVerificationRequiredForExceptions", true);
		regCentreConfigMap.put("operatorRegSubmissionMode", "fingerprint");
		configServerUri = env.getProperty("spring.cloud.config.uri");
		configLabel = env.getProperty("spring.cloud.config.label");
		configProfile = env.getProperty("spring.profiles.active");
		configAppName = env.getProperty("spring.application.name");
		uriBuilder = new StringBuilder();
		uriBuilder.append("/" + configServerUri + "/").append(configAppName + "/").append(configProfile + "/")
				.append(configLabel + "/");

		builder = new StringBuilder();
		builder.append(authBaseUri).append(authAllRolesUri);

	}

	

	// @Test
	public void getConfigurationSuccess() {
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo(uriBuilder.append(globalConfigFileName).toString())).andRespond(withSuccess());
		uriBuilder = new StringBuilder();
		uriBuilder.append(configServerUri + "/").append(configAppName + "/").append(configProfile + "/")
				.append(configLabel + "/");
		server.expect(requestTo(uriBuilder.append(regCenterfileName).toString())).andRespond(withSuccess());
		syncConfigDetailsService.getConfiguration("1");
	}

	// ------------------------------------------UserDetails--------------------------//
	@Test
	public void getAllUserDetail() {
		String response = "{ \"mosipUserDtoList\": [ { \"userName\": \"individual\", \"mail\": \"individual@mosip.io\", \"mobile\": \"8976394859\", \"langCode\": null, \"userPassword\": \"e1NTSEE1MTJ9TkhVb1c2WHpkZVJCa0drbU9tTk9ZcElvdUlNRGl5ODlJK3RhNm04d0FlTWhMSEoyTG4wSVJkNEJ2dkNqVFg4bTBuV2ZySStneXBTVittbVJKWnAxTkFwT3BWY3MxTVU5\", \"name\": \"individual\", \"role\": \"REGISTRATION_ADMIN,INDIVIDUAL\"  } ] }";
		String regId = "10044";
		RegistrationCenterUserResponseDto registrationCenterUserResponseDto = new RegistrationCenterUserResponseDto();
		List<RegistrationCenterUserDto> registrationCenterUserDtos = new ArrayList<>();
		RegistrationCenterUserDto registrationCenterUserDto = new RegistrationCenterUserDto();
		registrationCenterUserDto.setIsActive(true);
		registrationCenterUserDto.setRegCenterId(regId);
		registrationCenterUserDto.setUserId("M10411022");
		registrationCenterUserDtos.add(registrationCenterUserDto);
		registrationCenterUserResponseDto.setRegistrationCenterUsers(registrationCenterUserDtos);

		when(registrationCenterUserService.getUsersBasedOnRegistrationCenterId(regId))
				.thenReturn(registrationCenterUserResponseDto);

		MockRestServiceServer mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
		mockRestServiceServer.expect(requestTo(userDetailsUri.toString()))
				.andRespond(withSuccess().body(response).contentType(MediaType.APPLICATION_JSON));
		syncUserDetailsService.getAllUserDetail(regId);
	}

	@Test(expected = SyncDataServiceException.class)
	public void getAllUserDetailExcp() {
		String response = "{ \"userDetails\": [ { \"userName\": \"individual\", \"mail\": \"individual@mosip.io\", \"mobile\": \"8976394859\", \"langCode\": null, \"userPassword\": \"e1NTSEE1MTJ9TkhVb1c2WHpkZVJCa0drbU9tTk9ZcElvdUlNRGl5ODlJK3RhNm04d0FlTWhMSEoyTG4wSVJkNEJ2dkNqVFg4bTBuV2ZySStneXBTVittbVJKWnAxTkFwT3BWY3MxTVU5\", \"name\": \"individual\", \"roles\": [ \"REGISTRATION_ADMIN\", \"INDIVIDUAL\" ] } ] }";
		String regId = "10044";
		RegistrationCenterUserResponseDto registrationCenterUserResponseDto = new RegistrationCenterUserResponseDto();
		List<RegistrationCenterUserDto> registrationCenterUserDtos = new ArrayList<>();
		RegistrationCenterUserDto registrationCenterUserDto = new RegistrationCenterUserDto();
		registrationCenterUserDto.setIsActive(true);
		registrationCenterUserDto.setRegCenterId(regId);
		registrationCenterUserDto.setUserId("M10411022");
		registrationCenterUserDtos.add(registrationCenterUserDto);
		registrationCenterUserResponseDto.setRegistrationCenterUsers(registrationCenterUserDtos);

		when(registrationCenterUserService.getUsersBasedOnRegistrationCenterId(regId))
				.thenReturn(registrationCenterUserResponseDto);

		MockRestServiceServer mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
		mockRestServiceServer.expect(requestTo(userDetailsUri.toString()))
				.andRespond(withServerError().body(response).contentType(MediaType.APPLICATION_JSON));
		syncUserDetailsService.getAllUserDetail(regId);
	}

	@Test
	public void getAllUserDetailNoDetail() {
		// String response = "{ \"userDetails\": [] }";
		String regId = "10044";
		RegistrationCenterUserResponseDto registrationCenterUserResponseDto = new RegistrationCenterUserResponseDto();
		List<RegistrationCenterUserDto> registrationCenterUserDtos = new ArrayList<>();
		RegistrationCenterUserDto registrationCenterUserDto = new RegistrationCenterUserDto();
		registrationCenterUserDto.setIsActive(true);
		registrationCenterUserDto.setRegCenterId(regId);
		registrationCenterUserDto.setUserId("M10411022");
		registrationCenterUserDtos.add(registrationCenterUserDto);
		registrationCenterUserResponseDto.setRegistrationCenterUsers(registrationCenterUserDtos);

		when(registrationCenterUserService.getUsersBasedOnRegistrationCenterId(regId))
				.thenReturn(registrationCenterUserResponseDto);

		MockRestServiceServer mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
		mockRestServiceServer.expect(requestTo(userDetailsUri.toString())).andRespond(withSuccess());
		assertNull(syncUserDetailsService.getAllUserDetail(regId));
	}

	// ------------------------------------------AllRolesSync--------------------------//

	@Test
	public void getAllRoles() {

		MockRestServiceServer mockRestServer = MockRestServiceServer.bindTo(restTemplate).build();

		mockRestServer.expect(requestTo(builder.toString())).andRespond(withSuccess());
		syncRolesService.getAllRoles();
	}

	@Test(expected = SyncDataServiceException.class)
	public void getAllRolesException() {

		MockRestServiceServer mockRestServer = MockRestServiceServer.bindTo(restTemplate).build();
		mockRestServer.expect(requestTo(builder.toString())).andRespond(withServerError());
		syncRolesService.getAllRoles();
	}

	// -----------------------------------------publicKey-----------------------//

	@Test
	public void getPublicKey() throws JsonParseException, JsonMappingException, IOException {

		uriParams = new HashMap<>();
		uriParams.put("applicationId", "REGISTRATION");

		// Query parameters
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(publicKeyUrl)
				// Add query parameter
				.queryParam("referenceId", Optional.of("referenceId"))
				.queryParam("timeStamp", "2019-09-09T09:00:00.000Z");
		MockRestServiceServer mockRestServer = MockRestServiceServer.bindTo(restTemplate).build();

		mockRestServer.expect(MockRestRequestMatchers.requestTo(builder.buildAndExpand(uriParams).toString()))
				.andRespond(withSuccess().body(
						"{\"publicKey\": \"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAw2OmxIpq_BL9iYbL2nb845hNM86I0ujhC4CCkuOrVjHjw1zoOOSN2bPR-hitfZBGxgxnANZ4h63EJgCBXZRr0vaUJHbjhDp_hn0ETu1b2yqeJEFsIIm_SCg4no-EKVB77u59TFAZgDlkAbE21AJAUzC_B00QLlRB47VkLUwLL0kE9pctcmblJIr3iFKMBfGMBcIbs795RsLH-FsYFWQCcNqg4ku6LPlJZ2sOIDGPgHzx7ruH5t0RRCoUVmwqTQsdCqF7618m_W8N10S54aItUQaERqGs6gRj56f9-6tt-yyxFwm4qxv5UWyN9aGBxSEV-lNta074NTYpG-6qCKr3AwIDAQAB\", \"issuedAt\": \"2019-09-09T09:00:00\", \"expiryAt\": \"2020-09-08T09:00:00\"}"));
		PublicKeyResponse<String> publicKeyResp = new PublicKeyResponse<>();
		publicKeyResp.setExpiryAt(LocalDateTime.parse("2020-09-08T09:00:00"));
		publicKeyResp.setIssuedAt(LocalDateTime.parse("2019-09-09T09:00:00"));
		publicKeyResp.setPublicKey("sdfsfsdfsadfdsfsdfasf");

		when(objectMapper.readValue(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(publicKeyResp);
		syncConfigDetailsService.getPublicKey("REGISTRATION", "2019-09-09T09:00:00.000Z", Optional.of("referenceId"));
	}

	@Test(expected = SyncDataServiceException.class)
	public void getPublicKeyServiceExceptionTest() {

		uriParams = new HashMap<>();
		uriParams.put("applicationId", "REGISTRATION");

		// Query parameters
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(publicKeyUrl)
				// Add query parameter
				.queryParam("referenceId", Optional.of("referenceId"))
				.queryParam("timeStamp", "2019-09-09T09:00:00.000Z");
		MockRestServiceServer mockRestServer = MockRestServiceServer.bindTo(restTemplate).build();

		mockRestServer.expect(MockRestRequestMatchers.requestTo(builder.buildAndExpand(uriParams).toString()))
				.andRespond(withServerError());

		syncConfigDetailsService.getPublicKey("REGISTRATION", "2019-09-09T09:00:00.000Z", Optional.of("referenceId"));
	}

	@Test(expected = SyncInvalidArgumentException.class)
	public void getPublicIoException() throws IOException {
		uriParams = new HashMap<>();
		uriParams.put("applicationId", "REGISTRATION");

		// Query parameters
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(publicKeyUrl)
				// Add query parameter
				.queryParam("referenceId", Optional.of("referenceId"))
				.queryParam("timeStamp", "2019-09-09T09:00:00.000Z");
		MockRestServiceServer mockRestServer = MockRestServiceServer.bindTo(restTemplate).build();

		mockRestServer.expect(MockRestRequestMatchers.requestTo(builder.buildAndExpand(uriParams).toString()))
				.andRespond(withSuccess().body(
						"{ \"errors\": [ {   \"errorCode\": \"KER-KMS-002\",  \"errorMessage\": \"ApplicationId not found in Key Policy\" }] }"));

		syncConfigDetailsService.getPublicKey("REGISTRATION", "2019-09-09T09:00:00.000Z", Optional.of("referenceId"));
	}

	@Test(expected = SyncDataServiceException.class)
	public void getPublicServiceException() throws IOException {
		uriParams = new HashMap<>();
		uriParams.put("applicationId", "REGISTRATION");

		// Query parameters
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(publicKeyUrl)
				// Add query parameter
				.queryParam("referenceId", Optional.of("referenceId"))
				.queryParam("timeStamp", "2019-09-09T09:00:00.000Z");
		MockRestServiceServer mockRestServer = MockRestServiceServer.bindTo(restTemplate).build();

		mockRestServer.expect(MockRestRequestMatchers.requestTo(builder.buildAndExpand(uriParams).toString()))
				.andRespond(withSuccess().body(
						"{\"publicKey\": \"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAw2OmxIpq_BL9iYbL2nb845hNM86I0ujhC4CCkuOrVjHjw1zoOOSN2bPR-hitfZBGxgxnANZ4h63EJgCBXZRr0vaUJHbjhDp_hn0ETu1b2yqeJEFsIIm_SCg4no-EKVB77u59TFAZgDlkAbE21AJAUzC_B00QLlRB47VkLUwLL0kE9pctcmblJIr3iFKMBfGMBcIbs795RsLH-FsYFWQCcNqg4ku6LPlJZ2sOIDGPgHzx7ruH5t0RRCoUVmwqTQsdCqF7618m_W8N10S54aItUQaERqGs6gRj56f9-6tt-yyxFwm4qxv5UWyN9aGBxSEV-lNta074NTYpG-6qCKr3AwIDAQAB\", \"issuedAt\": \"2019-09-09T09:00:00\", \"expiryAt\": \"2020-09-08T09:00:00\"}"));
		when(objectMapper.readValue(Mockito.anyString(), Mockito.any(Class.class))).thenThrow(new IOException());

		syncConfigDetailsService.getPublicKey("REGISTRATION", "2019-09-09T09:00:00.000Z", Optional.of("referenceId"));
	}

}
