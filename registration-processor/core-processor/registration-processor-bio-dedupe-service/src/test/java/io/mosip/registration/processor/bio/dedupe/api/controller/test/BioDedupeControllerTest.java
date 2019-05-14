/**
 * 
 */
package io.mosip.registration.processor.bio.dedupe.api.controller.test;

import static org.mockito.ArgumentMatchers.anyString;

import javax.servlet.http.Cookie;

import io.mosip.registration.processor.packet.storage.utils.Utilities;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import io.mosip.registration.processor.bio.dedupe.api.BioDedupeApiTestApplication;
import io.mosip.registration.processor.bio.dedupe.api.config.BioDedupeConfigTest;
import io.mosip.registration.processor.bio.dedupe.api.controller.BioDedupeController;
import io.mosip.registration.processor.core.spi.biodedupe.BioDedupeService;
import io.mosip.registration.processor.core.token.validation.TokenValidator;
import io.mosip.registration.processor.packet.storage.utils.Utilities;

/**
 * @author M1022006
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BioDedupeApiTestApplication.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = BioDedupeConfigTest.class)
@TestPropertySource(locations = "classpath:application.properties")
public class BioDedupeControllerTest {

	@InjectMocks
	private BioDedupeController bioDedupeController = new BioDedupeController();

	@MockBean
	private BioDedupeService bioDedupeService;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private Utilities utilities;

	@MockBean
	private TokenValidator tokenValidator;

	String regId;

	byte[] file;

	@Before
	public void setUp() {
		regId = "1234";
		file = regId.getBytes();
		Mockito.when(bioDedupeService.getFile(anyString())).thenReturn(file);
		Mockito.doNothing().when(tokenValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	@Ignore
	public void getFileSuccessTest() throws Exception {

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/biometricfile/1234")
						.cookie(new Cookie("Authorization", "token")).param("regId", regId).accept(MediaType.ALL_VALUE).contentType(MediaType.ALL_VALUE))

				.andExpect(MockMvcResultMatchers.status().isOk());

	}
}
