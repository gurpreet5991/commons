package io.mosip.registration.processor.printing.api.controller.test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.registration.processor.core.constant.IdType;
import io.mosip.registration.processor.core.spi.print.service.PrintService;
import io.mosip.registration.processor.core.token.validation.TokenValidator;
import io.mosip.registration.processor.printing.api.controller.PrintApiController;
import io.mosip.registration.processor.printing.api.dto.PrintRequest;
import io.mosip.registration.processor.printing.api.dto.RequestDTO;
import io.mosip.registration.processor.printing.api.util.PrintServiceRequestValidator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = PrintServiceConfigTest.class)
@TestPropertySource(locations = "classpath:application.properties")
public class PrintApiControllerTest {

	@InjectMocks
	private PrintApiController printapicontroller = new PrintApiController();

	@MockBean
	private PrintService<Map<String, byte[]>> printservice;

	@Mock
	private Environment env;

	@Autowired
	private MockMvc mockMvc;

	@Mock
	private PrintServiceRequestValidator validator;

	@Mock
	private TokenValidator tokenValidator;

	/** The rid validator. */
	@Mock
	private RidValidator<String> ridValidator;

	@Mock
	private UinValidator<String> uinValidatorImpl;

	@Mock
	private Errors errors;

	private Map<String, byte[]> map = new HashMap<>();

	private String json;

	@Before
	public void setup() throws JsonProcessingException {
		when(env.getProperty("mosip.registration.processor.print.service.id")).thenReturn("mosip.registration.print");
		when(env.getProperty("mosip.registration.processor.datetime.pattern"))
				.thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		when(env.getProperty("mosip.registration.processor.application.version")).thenReturn("1.0");
		doNothing().when(tokenValidator).validate(ArgumentMatchers.any(), ArgumentMatchers.any());

		PrintRequest request = new PrintRequest();
		request.setId("mosip.registration.print");
		RequestDTO dto = new RequestDTO();
		dto.setIdtype(IdType.RID);
		dto.setIdValue("10003100030000720190416061449");
		request.setRequest(dto);
		request.setRequesttime("2019-03-15T09:08:38.548Z");
		request.setVersion("1.0");
		Gson gson = new GsonBuilder().serializeNulls().create();
		json = gson.toJson(request);

		byte[] pdfbyte = "pdf bytes".getBytes();
		map.put("uinPdf", pdfbyte);
	}

	@Test
	@Ignore
	public void testpdfSuccess() throws Exception {
		Mockito.when(printservice.getDocuments(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(map);

		this.mockMvc.perform(post("/uincard")
				.cookie(new Cookie("Authorization", json))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isOk());
	}

	@Test
	@Ignore
	public void testPdfFailure() throws Exception {
		this.mockMvc.perform(post("/uincard")
				.cookie(new Cookie("Authorization", json))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest());
	}
}
