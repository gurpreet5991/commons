package io.mosip.registration.processor.status.api.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.auth.adapter.filter.AuthFilter;
import io.mosip.kernel.auth.adapter.handler.AuthHandler;
import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.registration.processor.core.token.validation.TokenValidator;
import io.mosip.registration.processor.status.api.config.RegistrationStatusConfigTest;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusRequestDTO;
import io.mosip.registration.processor.status.dto.RegistrationStatusSubRequestDto;
import io.mosip.registration.processor.status.dto.SyncRegistrationDto;
import io.mosip.registration.processor.status.dto.SyncResponseDto;
import io.mosip.registration.processor.status.exception.RegStatusAppException;
import io.mosip.registration.processor.status.service.RegistrationStatusService;
import io.mosip.registration.processor.status.service.SyncRegistrationService;
import io.mosip.registration.processor.status.validator.RegistrationStatusRequestValidator;

/**
 * The Class RegistrationStatusControllerTest.
 * 
 * @author M1047487
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = RegistrationStatusConfigTest.class)
@TestPropertySource(locations = "classpath:application.properties")
public class RegistrationStatusControllerTest {

	/** The registration status controller. */
	@InjectMocks
	RegistrationStatusController registrationStatusController = new RegistrationStatusController();

	/** The registration status service. */
	@MockBean
	RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	/** The sync registration service. */
	@MockBean
	SyncRegistrationService<SyncResponseDto, SyncRegistrationDto> syncRegistrationService;

	/** The sync registration dto. */
	@MockBean
	SyncRegistrationDto syncRegistrationDto;

	RegistrationStatusRequestDTO registrationStatusRequestDTO;
	/** The mock mvc. */
	@Autowired
	private MockMvc mockMvc;

	/** The registration dto list. */
	private List<InternalRegistrationStatusDto> registrationDtoList;

	/** The array to json. */
	private String regStatusToJson;

	@Mock
	private Environment env;

	@Mock
	private TokenValidator tokenValidator;

	@MockBean
	RegistrationStatusRequestValidator registrationStatusRequestValidator;

	Gson gson = new GsonBuilder().serializeNulls().create();

	@Mock
	SignatureUtil signatureUtil;
	@Mock
	io.mosip.kernel.core.signatureutil.model.SignatureResponse signatureResponse;

	@Autowired
	private WebApplicationContext wac;

	@Mock
	AuthFilter filter;

	/**
	 * Sets the up.
	 *
	 * @throws JsonProcessingException
	 */
	@Before
	public void setUp() throws JsonProcessingException {
		when(env.getProperty("mosip.registration.processor.registration.status.id"))
		.thenReturn("mosip.registration.status");
		when(env.getProperty("mosip.registration.processor.datetime.pattern"))
		.thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		when(env.getProperty("mosip.registration.processor.application.version")).thenReturn("1.0");
		doNothing().when(tokenValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		List<RegistrationStatusSubRequestDto> request = new ArrayList<>();
		RegistrationStatusSubRequestDto regitrationid1 = new RegistrationStatusSubRequestDto();
		RegistrationStatusSubRequestDto regitrationid2 = new RegistrationStatusSubRequestDto();
		regitrationid1.setRegistrationId("1001");
		regitrationid2.setRegistrationId("1002");
		request.add(regitrationid1);
		request.add(regitrationid2);
		registrationStatusRequestDTO = new RegistrationStatusRequestDTO();
		registrationStatusRequestDTO.setRequest(request);
		registrationStatusRequestDTO.setId("mosip.registration.status");
		registrationStatusRequestDTO.setVersion("1.0");
		registrationStatusRequestDTO
		.setRequesttime(DateUtils.getUTCCurrentDateTimeString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
		regStatusToJson = gson.toJson(registrationStatusRequestDTO);
		registrationDtoList = new ArrayList<>();
		InternalRegistrationStatusDto registrationStatusDto1 = new InternalRegistrationStatusDto();
		registrationStatusDto1.setRegistrationId("1001");
		registrationStatusDto1.setRegistrationType("NEW");
		registrationStatusDto1.setLangCode("EN");
		registrationStatusDto1.setIsActive(true);
		registrationStatusDto1.setCreatedBy("MOSIP_SYSTEM");

		InternalRegistrationStatusDto registrationStatusDto2 = new InternalRegistrationStatusDto();
		registrationStatusDto2.setRegistrationId("1002");
		registrationStatusDto2.setRegistrationType("NEW");
		registrationStatusDto2.setLangCode("EN");
		registrationStatusDto2.setIsActive(true);
		registrationStatusDto2.setCreatedBy("MOSIP_SYSTEM");

		registrationDtoList.add(registrationStatusDto1);
		registrationDtoList.add(registrationStatusDto2);

		Mockito.doReturn(registrationDtoList).when(registrationStatusService).getByIds(ArgumentMatchers.any());

		signatureResponse=Mockito.mock(SignatureResponse.class);//new SignatureResponse();
		when(signatureUtil.signResponse(Mockito.any(String.class))).thenReturn(signatureResponse);
		when(signatureResponse.getData()).thenReturn("gdshgsahjhghgsad");

		this.mockMvc = webAppContextSetup (this.wac).addFilters(filter).build();
	}

	/**
	 * Search success test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Ignore
	public void searchSuccessTest() throws Exception {
		doNothing().when(registrationStatusRequestValidator).validate((registrationStatusRequestDTO),
				"mosip.registration.status");

		this.mockMvc
		.perform(MockMvcRequestBuilders.get("/search")
				.cookie(new Cookie("Authorization", regStatusToJson)).param("request", regStatusToJson).accept(MediaType.ALL_VALUE).contentType(MediaType.ALL_VALUE))
		.andExpect(MockMvcResultMatchers.status().isOk());
	}

	/**
	 * Search failure test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Ignore
	public void searchFailureTest() throws Exception {
		this.mockMvc
		.perform(MockMvcRequestBuilders.get("/search")
				.accept(MediaType.APPLICATION_ATOM_XML).contentType(MediaType.ALL_VALUE))
		.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	@Ignore
	public void searchRegstatusException() throws Exception {

		Mockito.doThrow(new RegStatusAppException()).when(registrationStatusRequestValidator)
		.validate(ArgumentMatchers.any(), ArgumentMatchers.any());
		this.mockMvc.perform(MockMvcRequestBuilders.get("/search")
				.cookie(new Cookie("Authorization", regStatusToJson)).param("request", regStatusToJson).accept(MediaType.ALL_VALUE).contentType(MediaType.ALL_VALUE));
	}

}
