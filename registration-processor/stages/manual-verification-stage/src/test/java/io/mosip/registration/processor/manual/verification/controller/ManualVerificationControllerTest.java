package io.mosip.registration.processor.manual.verification.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.filesystem.ceph.adapter.impl.exception.PacketNotFoundException;
import io.mosip.registration.processor.manual.verification.controller.ManualVerificationController;
import io.mosip.registration.processor.manual.verification.dto.FileRequestDto;
import io.mosip.registration.processor.manual.verification.dto.ManualVerificationDTO;
import io.mosip.registration.processor.manual.verification.dto.ManualVerificationStatus;
import io.mosip.registration.processor.manual.verification.dto.UserDto;
import io.mosip.registration.processor.manual.verification.exception.InvalidFileNameException;
import io.mosip.registration.processor.manual.verification.exception.InvalidUpdateException;
import io.mosip.registration.processor.manual.verification.exception.NoRecordAssignedException;
import io.mosip.registration.processor.manual.verification.service.ManualVerificationService;
import io.mosip.registration.processor.manual.verification.stage.ManualVerificationStage;

@RunWith(SpringRunner.class)
@WebMvcTest(ManualVerificationController.class)
public class ManualVerificationControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	ManualVerificationService manualAdjudicationService;
	
	@MockBean
	ManualVerificationStage manualVerificationStage;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
    private WebApplicationContext wac;
	
	private UserDto userDto;
	
	private ManualVerificationDTO manualVerificationDTO;
	
	private JacksonTester<UserDto> jsonUserDto;
	
	private JacksonTester<ManualVerificationDTO> jsonManualVerificationDto;
	
	private JacksonTester<FileRequestDto> jsonRequestDto;
	
	@Before
	public void setup() {
		mockMvc = webAppContextSetup(this.wac).build();
		JacksonTester.initFields(this, objectMapper);
		this.userDto = new UserDto();
		this.userDto.setUserId("USER1");
		manualVerificationDTO = new ManualVerificationDTO();
		manualVerificationDTO.setRegId("123456789");
		manualVerificationDTO.setMvUsrId("USER1");
		manualVerificationDTO.setMatchedRefId("987654321");
	}
	
	@Test
	public void assignmentSuccessTest() {
		Mockito.when(manualAdjudicationService.assignApplicant(userDto)).thenReturn(manualVerificationDTO);
		String userDto = null;
		try {
			userDto = jsonUserDto.write(this.userDto).getJson();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v0.1/registration-processor/manual-verification/assignment").content(userDto).contentType(MediaType.APPLICATION_JSON);
		try {
			this.mockMvc.perform(requestBuilder).andExpect(status().isOk());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void updatePacketStatusSuccessTest() {
		Mockito.when(manualAdjudicationService.updatePacketStatus(manualVerificationDTO)).thenReturn(manualVerificationDTO);
		String manualVerificationDto = null;
		this.manualVerificationDTO.setStatusCode(ManualVerificationStatus.APPROVED.name());
		try {
			manualVerificationDto = jsonManualVerificationDto.write(this.manualVerificationDTO).getJson();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v0.1/registration-processor/manual-verification/decision").content(manualVerificationDto).contentType(MediaType.APPLICATION_JSON);
		try {
			this.mockMvc.perform(requestBuilder).andExpect(status().isOk());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getFileSuccessTest() {
		String jsonfileRequestDto = null;
		FileRequestDto fileRequestDto = new FileRequestDto();
		fileRequestDto.setRegId("12345");
		fileRequestDto.setFileName("APPLICANTPHOTO");
		byte[] sampleFile = "test".getBytes();
		Mockito.when(manualAdjudicationService.getApplicantFile(fileRequestDto.getRegId(), fileRequestDto.getFileName())).thenReturn(sampleFile);
		try {
			jsonfileRequestDto = this.jsonRequestDto.write(fileRequestDto).getJson();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v0.1/registration-processor/manual-verification/applicantBiometric").content(jsonfileRequestDto).contentType(MediaType.APPLICATION_JSON);
		try {
			this.mockMvc.perform(requestBuilder).andExpect(status().isOk());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getDataSuccessTest() {
		String jsonfileRequestDto = null;
		FileRequestDto fileRequestDto = new FileRequestDto();
		fileRequestDto.setRegId("12345");
		fileRequestDto.setFileName("APPLICANTPHOTO");
		byte[] sampleFile = "test".getBytes();
		Mockito.when(manualAdjudicationService.getApplicantFile(fileRequestDto.getRegId(), fileRequestDto.getFileName())).thenReturn(sampleFile);
		try {
			jsonfileRequestDto = this.jsonRequestDto.write(fileRequestDto).getJson();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v0.1/registration-processor/manual-verification/applicantDemographic").content(jsonfileRequestDto).contentType(MediaType.APPLICATION_JSON);
		try {
			this.mockMvc.perform(requestBuilder).andExpect(status().isOk());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void invalidFileNameExceptionHandlerTest() {
		String jsonfileRequestDto = null;
		FileRequestDto fileRequestDto = new FileRequestDto();
		fileRequestDto.setRegId("12345");
		fileRequestDto.setFileName("APPLICANTPHOTO");
		Mockito.when(manualAdjudicationService.getApplicantFile(fileRequestDto.getRegId(), fileRequestDto.getFileName())).thenThrow(new InvalidFileNameException(PlatformErrorMessages.RPR_MVS_INVALID_FILE_REQUEST.getCode(),
				PlatformErrorMessages.RPR_MVS_INVALID_FILE_REQUEST.getMessage()));
		try {
			jsonfileRequestDto = this.jsonRequestDto.write(fileRequestDto).getJson();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v0.1/registration-processor/manual-adjudication/applicantBiometric").content(jsonfileRequestDto).contentType(MediaType.APPLICATION_JSON);
		try {
			this.mockMvc.perform(requestBuilder).andExpect(status().isNotFound());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void fileNotPresentExceptionHandlerTest() {
		String jsonfileRequestDto = null;
		FileRequestDto fileRequestDto = new FileRequestDto();
		fileRequestDto.setRegId("12345");
		fileRequestDto.setFileName("APPLICANTPHOTO");
		Mockito.when(manualAdjudicationService.getApplicantFile(fileRequestDto.getRegId(), fileRequestDto.getFileName())).thenThrow(new PacketNotFoundException());
		try {
			jsonfileRequestDto = this.jsonRequestDto.write(fileRequestDto).getJson();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v0.1/registration-processor/manual-adjudication/applicantDemographic").content(jsonfileRequestDto).contentType(MediaType.APPLICATION_JSON);
		try {
			this.mockMvc.perform(requestBuilder).andExpect(status().isNotFound());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void noRecordAssignedExceptionHandlerTest() {
		Mockito.when(manualAdjudicationService.assignApplicant(any())).thenThrow(new NoRecordAssignedException(PlatformErrorMessages.RPR_MVS_NO_ASSIGNED_RECORD.getCode(), PlatformErrorMessages.RPR_MVS_NO_ASSIGNED_RECORD.getMessage()));
		String userDto = null;
		try {
			userDto = jsonUserDto.write(this.userDto).getJson();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v0.1/registration-processor/manual-adjudication/assignment").content(userDto).contentType(MediaType.APPLICATION_JSON);
		try {
			this.mockMvc.perform(requestBuilder).andExpect(status().isNotFound());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void invalidStatusUpdateExceptionTest() {
		String manualVerificationDto = null;
		Mockito.when(manualAdjudicationService.updatePacketStatus(any())).thenThrow(new InvalidUpdateException(PlatformErrorMessages.RPR_MVS_INVALID_STATUS_UPDATE.getCode(), PlatformErrorMessages.RPR_MVS_INVALID_STATUS_UPDATE.getMessage()));
		this.manualVerificationDTO.setStatusCode(ManualVerificationStatus.APPROVED.name());
		try {
			manualVerificationDto = jsonManualVerificationDto.write(this.manualVerificationDTO).getJson();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v0.1/registration-processor/manual-verification/decision").content(manualVerificationDto).contentType(MediaType.APPLICATION_JSON);
		try {
			this.mockMvc.perform(requestBuilder).andExpect(status().isForbidden());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
