package io.mosip.kernel.masterdata.test.controller;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import io.mosip.kernel.masterdata.dto.BiometricAttributeDto;
import io.mosip.kernel.masterdata.dto.BiometricTypeResponseDto;
import io.mosip.kernel.masterdata.exception.BiometricAttributeNotFoundException;
import io.mosip.kernel.masterdata.exception.BiometricTypeFetchException;
import io.mosip.kernel.masterdata.service.BiometricAttributeService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class BiometricAttributeControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	private BiometricAttributeService biometricAttributeService;

	private final String expected = "{ \"biometricattributes\": [ { \"code\": \"iric_black\", \"name\": \"black\", \"description\": null, \"active\": true},{\"code\": \"iric_brown\", \"name\": \"brown\", \"description\": null,\"active\": true } ] }";

	BiometricTypeResponseDto biometricTypeResponseDto = null;
	List<BiometricAttributeDto> biometricattributes = null;

	@Before
	public void Setup() {

		biometricattributes = new ArrayList<>();
		BiometricAttributeDto biometricAttribute = new BiometricAttributeDto();
		biometricAttribute.setCode("iric_black");
		biometricAttribute.setName("black");
		biometricAttribute.setDescription(null);
		biometricAttribute.setActive(true);
		biometricattributes.add(biometricAttribute);
		BiometricAttributeDto biometricAttribute1 = new BiometricAttributeDto();
		biometricAttribute1.setCode("iric_brown");
		biometricAttribute1.setName("brown");
		biometricAttribute.setDescription(null);
		biometricAttribute1.setActive(true);
		biometricattributes.add(biometricAttribute1);
		biometricTypeResponseDto = new BiometricTypeResponseDto(biometricattributes);

	}

	@Test
	public void testGetBiometricAttributesByBiometricType() throws Exception {

		Mockito.when(biometricAttributeService.getBiometricAttribute(Mockito.anyString(), Mockito.anyString()))
				.thenReturn((biometricattributes));
		mockMvc.perform(MockMvcRequestBuilders.get("/biometricattributes/iric/eng"))
				.andExpect(MockMvcResultMatchers.content().json(expected))
				.andExpect(MockMvcResultMatchers.status().isOk());

	}

	@Test
	public void testBiometricTypeBiometricAttributeNotFoundException() throws Exception {
		Mockito.when(biometricAttributeService.getBiometricAttribute(Mockito.anyString(), Mockito.anyString()))
				.thenThrow(new BiometricAttributeNotFoundException("KER-MAS-00000",
						"No biometric attributes found for specified biometric code type and language code"));
		mockMvc.perform(MockMvcRequestBuilders.get("/biometricattributes/face/eng"))
				.andExpect(MockMvcResultMatchers.status().isNotAcceptable());
	}

	@Test
	public void testBiometricTypeFetchException() throws Exception {
		Mockito.when(biometricAttributeService.getBiometricAttribute(Mockito.anyString(), Mockito.anyString()))
				.thenThrow(new BiometricTypeFetchException("KER-DOC-00000", "exception duringfatching data from db"));
		mockMvc.perform(MockMvcRequestBuilders.get("/biometricattributes/iric/eng"))
				.andExpect(MockMvcResultMatchers.status().isNotAcceptable());
	}

}
