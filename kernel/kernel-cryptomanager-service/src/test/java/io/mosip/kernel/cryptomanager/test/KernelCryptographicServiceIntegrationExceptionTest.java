package io.mosip.kernel.cryptomanager.test;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.crypto.spi.Decryptor;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.cryptomanager.KernelCryptomanagerBootApplication;
import io.mosip.kernel.cryptomanager.dto.KeymanagerPublicKeyResponseDto;

@SpringBootTest(classes = KernelCryptomanagerBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class KernelCryptographicServiceIntegrationExceptionTest {

	@Value("${mosip.kernel.keymanager-service-publickey-url}")
	private String publicKeyUrl;
	
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RestTemplate restTemplate;

	@MockBean
	Decryptor<PrivateKey, PublicKey, SecretKey> decryptor;

	private MockRestServiceServer server;
	
	private UriComponentsBuilder builder;
	
	private Map<String, String> uriParams;

	@Before
	public void setUp() {

		server = MockRestServiceServer.bindTo(restTemplate).build();
		uriParams = new HashMap<>();
		uriParams.put("applicationId", "REGISTRATION");
		builder = UriComponentsBuilder.fromUriString(publicKeyUrl)
				.queryParam("timeStamp", "2018-12-06T12:07:44.403Z")
				.queryParam("referenceId","ref123");
		
	}

	@Test
	public void testInvalidSpecEncrypt() throws Exception {
		KeymanagerPublicKeyResponseDto keymanagerPublicKeyResponseDto = new KeymanagerPublicKeyResponseDto(
				CryptoUtil.encodeBase64("badprivatekey".getBytes()), LocalDateTime.now(),
				LocalDateTime.now().plusDays(100));
		server.expect(requestTo(builder.buildAndExpand(uriParams).toUriString()
				))
				.andRespond(withSuccess(objectMapper.writeValueAsString(keymanagerPublicKeyResponseDto),
						MediaType.APPLICATION_JSON));
		String requestBody = "{\"applicationId\": \"REGISTRATION\",\"data\": \"dXJ2aWw\",\"referenceId\": \"ref123\",\"timeStamp\": \"2018-12-06T12:07:44.403Z\"}";
		mockMvc.perform(post("/v1.0/encrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isInternalServerError());
	}

	@Test
	public void testMethodArgumentNotValidException() throws Exception {
		String requestBody = "{\"applicationId\": \"\",\"data\": \"\",\"referenceId\": \"ref123\",\"timeStamp\": \"2018-12-06T12:07:44.403Z\"}";
		mockMvc.perform(post("/v1.0/encrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testHttpClientErrorException() throws Exception {
		server.expect(requestTo(builder.buildAndExpand(uriParams).toUriString()))
				.andRespond(withBadRequest());
		String requestBody = "{\"applicationId\": \"REGISTRATION\",\"data\": \"dXJ2aWw\",\"referenceId\": \"ref123\",\"timeStamp\": \"2018-12-06T12:07:44.403Z\"}";
		mockMvc.perform(post("/v1.0/encrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isInternalServerError());
	}

	@Test
	public void testHttpServerErrorException() throws Exception {
		server.expect(requestTo(builder.buildAndExpand(uriParams).toUriString()))
				.andRespond(withServerError());
		String requestBody = "{\"applicationId\": \"REGISTRATION\",\"data\": \"dXJ2aWw\",\"referenceId\": \"ref123\",\"timeStamp\": \"2018-12-06T12:07:44.403Z\"}";
		mockMvc.perform(post("/v1.0/encrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isInternalServerError());
	}

	@Test
	public void testInvalidFormatException() throws Exception {
		server.expect(requestTo(builder.buildAndExpand(uriParams).toUriString()))
				.andRespond(withServerError());
		String requestBody = "{\"applicationId\": \"REGISTRATION\",\"data\": \"dXJ2aWw\",\"referenceId\": \"ref123\",\"timeStamp\": \"2018-12-0\"}";
		mockMvc.perform(post("/v1.0/encrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testIllegalArgumentException() throws Exception {
		String requestBody = "{\"applicationId\": \"REGISTRATION\",\"data\": \"dXJ2aWw\",\"referenceId\": \"ref123\",\"timeStamp\": \"2018-12-06T12:07:44.403Z\"}";
		mockMvc.perform(post("/v1.0/decrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest());
	}

}
