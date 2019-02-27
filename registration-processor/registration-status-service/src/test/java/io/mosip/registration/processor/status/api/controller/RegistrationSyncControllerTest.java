package io.mosip.registration.processor.status.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.registration.processor.status.api.config.RegistrationStatusConfigTest;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusRequestDTO;
import io.mosip.registration.processor.status.dto.RegistrationSyncRequestDTO;
import io.mosip.registration.processor.status.dto.SyncRegistrationDto;
import io.mosip.registration.processor.status.dto.SyncResponseDto;
import io.mosip.registration.processor.status.service.RegistrationStatusService;
import io.mosip.registration.processor.status.service.SyncRegistrationService;
import io.mosip.registration.processor.status.validator.RegistrationSyncRequestValidator;

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
public class RegistrationSyncControllerTest {

	/** The registration status controller. */
	@InjectMocks
	RegistrationSyncController registrationSyncController = new RegistrationSyncController();

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

	/** The list. */
	private List<SyncRegistrationDto> list;

	/** The SyncResponseDtoList. */
	private List<SyncResponseDto> syncResponseDtoList;

	/** The array to json. */
	private String arrayToJson;

	/** The ridValidator. */
	@MockBean
	private RidValidator<String> ridValidator;

	@Mock
	private Environment env;

	RegistrationSyncRequestDTO registrationSyncRequestDTO;

	@Mock
	RegistrationSyncRequestValidator registrationSyncRequestValidator;

	Gson gson = new GsonBuilder().serializeNulls().create();

	/**
	 * Sets the up.
	 *
	 * @throws JsonProcessingException
	 */
	@Before
	public void setUp() throws JsonProcessingException {
		when(env.getProperty("mosip.registration.processor.registration.sync.id"))
				.thenReturn("mosip.registration.sync");
		when(env.getProperty("mosip.registration.processor.datetime.pattern"))
				.thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		when(env.getProperty("mosip.registration.processor.application.version")).thenReturn("1.0");

		list = new ArrayList<>();
		SyncRegistrationDto syncRegistrationDto = new SyncRegistrationDto();
		syncRegistrationDto = new SyncRegistrationDto();
		syncRegistrationDto.setRegistrationId("1002");
		syncRegistrationDto.setLangCode("eng");
		syncRegistrationDto.setIsActive(true);
		list.add(syncRegistrationDto);

		registrationSyncRequestDTO = new RegistrationSyncRequestDTO();
		registrationSyncRequestDTO.setRequest(list);
		registrationSyncRequestDTO.setId("mosip.registration.sync");
		registrationSyncRequestDTO.setVersion("1.0");
		registrationSyncRequestDTO.setRequestTimestamp(DateUtils.getUTCCurrentDateTimeString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
		arrayToJson=gson.toJson(registrationSyncRequestDTO);
		SyncResponseDto syncResponseDto = new SyncResponseDto();

		syncResponseDto.setRegistrationId("1001");
		syncResponseDto.setParentRegistrationId("12334");
		syncResponseDto.setMessage("Registartion Id's are successfully synched in Sync table");
		syncResponseDto.setStatus("Success");

		syncResponseDtoList = new ArrayList<>();
		syncResponseDtoList.add(syncResponseDto);
	}

	/**
	 * Test creation of A new project succeeds.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void syncRegistrationControllerSuccessTest() throws Exception {
		Mockito.when(syncRegistrationService.sync(ArgumentMatchers.any())).thenReturn(syncResponseDtoList);

		this.mockMvc
				.perform(
						post("/v0.1/registration-processor/registration-status/sync").accept(MediaType.APPLICATION_JSON)
								.contentType(MediaType.APPLICATION_JSON).content(arrayToJson))
				.andExpect(status().isOk());
	}

	/**
	 * Sync registration controller failure check.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void syncRegistrationControllerFailureTest() throws Exception {

		Mockito.when(syncRegistrationService.sync(ArgumentMatchers.any())).thenReturn(syncResponseDtoList);
		this.mockMvc
				.perform(post("/v0.1/registration-processor/registration-status/sync")
						.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

}
