package io.mosip.kernel.idrepo.interceptor;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.kernel.core.idrepo.exception.IdRepoAppUncheckedException;
import io.mosip.kernel.core.idrepo.exception.IdRepoDataValidationException;
import io.mosip.kernel.core.idrepo.exception.RestServiceException;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.idrepo.builder.RestRequestBuilder;
import io.mosip.kernel.idrepo.dto.RestRequestDTO;
import io.mosip.kernel.idrepo.entity.Uin;
import io.mosip.kernel.idrepo.entity.UinHistory;
import io.mosip.kernel.idrepo.helper.RestHelper;
import io.mosip.kernel.idrepo.security.IdRepoSecurityManager;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
@ActiveProfiles("test")
public class IdRepoEntityInterceptorTest {

	@Autowired
	Environment env;

	@InjectMocks
	IdRepoEntityInterceptor interceptor;
	
	@InjectMocks
	IdRepoSecurityManager securityManager;

	@Mock
	RestHelper restHelper;
	
	@Mock
	RestRequestBuilder restBuilder;

	@InjectMocks
	ObjectMapper mapper;

	@Before
	public void setup() {
		ReflectionTestUtils.setField(securityManager, "env", env);
		ReflectionTestUtils.setField(securityManager, "mapper", mapper);
		ReflectionTestUtils.invokeMethod(securityManager, "buildRequest");
		ReflectionTestUtils.setField(interceptor, "securityManager", securityManager);
	}
	
	@Test
	public void testOnSaveUin() throws RestClientException, JsonParseException, JsonMappingException, IOException, RestServiceException, IdRepoDataValidationException {
		when(restBuilder.buildRequest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new RestRequestDTO());
		when(restHelper.requestSync(Mockito.any()))
				.thenReturn(mapper.readValue("{\"data\":\"1234\"}".getBytes(), ObjectNode.class));
		Uin uin = new Uin();
		uin.setUinData(new byte[] { 0 });
		Object[] state = new Object[] { new byte[] { 0 } };
		String[] propertyNames = new String[] { "uinData" };
		interceptor.onSave(uin, null, state, propertyNames, null);
	}
	
	@Test
	public void testOnSaveUinHistory() throws RestClientException, JsonParseException, JsonMappingException, IOException, RestServiceException, IdRepoDataValidationException {
		when(restBuilder.buildRequest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new RestRequestDTO());
		when(restHelper.requestSync(Mockito.any()))
				.thenReturn(mapper.readValue("{\"data\":\"1234\"}".getBytes(), ObjectNode.class));
		UinHistory uin = new UinHistory();
		uin.setUinData(new byte[] { 0 });
		Object[] state = new Object[] { new byte[] { 0 } };
		String[] propertyNames = new String[] { "uinData" };
		interceptor.onSave(uin, null, state, propertyNames, null);
	}
	
	@Test(expected = IdRepoAppUncheckedException.class)
	public void testOnSaveUinException() throws RestClientException, JsonParseException, JsonMappingException, IOException, RestServiceException, IdRepoDataValidationException {
		when(restBuilder.buildRequest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new RestRequestDTO());
		when(restHelper.requestSync(Mockito.any()))
				.thenReturn(mapper.readValue("{\"value\":\"1234\"}".getBytes(), ObjectNode.class));
		Uin uin = new Uin();
		uin.setUinData(new byte[] { 0 });
		Object[] state = new Object[] { new byte[] { 0 } };
		String[] propertyNames = new String[] { "uinData" };
		interceptor.onSave(uin, null, state, propertyNames, null);
	}
	
	@Test
	public void testOnLoadUin() throws RestClientException, JsonParseException, JsonMappingException, IOException, IdRepoDataValidationException, RestServiceException {
		when(restBuilder.buildRequest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new RestRequestDTO());
		when(restHelper.requestSync(Mockito.any()))
				.thenReturn(mapper.readValue("{\"data\":\"1234\"}".getBytes(), ObjectNode.class));
		Uin uin = new Uin();
		uin.setUinData(new byte[] { 0 });
		Object[] state = new Object[] { new byte[] { 0 } , "5B72C3B57A72C6497461289FCA7B1F865ED6FB0596B446FEA1F92AF931A5D4B7"};
		String[] propertyNames = new String[] { "uinData", "uinDataHash" };
		interceptor.onLoad(uin, null, state, propertyNames, null);
	}
	
	@Test(expected = IdRepoAppUncheckedException.class)
	public void testOnLoadUinFailedEncrption() throws RestClientException, JsonParseException, JsonMappingException, IOException, IdRepoDataValidationException, RestServiceException {
		when(restBuilder.buildRequest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new RestRequestDTO());
		when(restHelper.requestSync(Mockito.any()))
				.thenThrow(new RestServiceException());
		Uin uin = new Uin();
		uin.setUinData(new byte[] { 0 });
		Object[] state = new Object[] { new byte[] { 0 } , "5B72C3B57A72C6497461289FCA7B1F865ED6FB0596B446FEA1F92AF931A5D4B7"};
		String[] propertyNames = new String[] { "uinData", "uinDataHash" };
		interceptor.onLoad(uin, null, state, propertyNames, null);
	}
	
	@Test(expected = IdRepoAppUncheckedException.class)
	public void testOnLoadUinException() throws RestClientException, JsonParseException, JsonMappingException, IOException, IdRepoDataValidationException, RestServiceException {
		when(restBuilder.buildRequest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new RestRequestDTO());
		when(restHelper.requestSync(Mockito.any()))
				.thenReturn(mapper.readValue("{\"value\":\"1234\"}".getBytes(), ObjectNode.class));
		Uin uin = new Uin();
		uin.setUinData(new byte[] { 0 });
		Object[] state = new Object[] { new byte[] { 0 } , "W3LDtXpyxkl0YSifynsfhl7W-wWWtEb-ofkq-TGl1Lc"};
		String[] propertyNames = new String[] { "uinData", "uinDataHash" };
		interceptor.onLoad(uin, null, state, propertyNames, null);
	}
	
	@Test(expected = IdRepoAppUncheckedException.class)
	public void testOnLoadUinHashMismatch() throws RestClientException, JsonParseException, JsonMappingException, IOException, IdRepoDataValidationException, RestServiceException {
		when(restBuilder.buildRequest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new RestRequestDTO());
		when(restHelper.requestSync(Mockito.any()))
				.thenReturn(mapper.readValue("{\"data\":\"1234\"}".getBytes(), ObjectNode.class));
		Uin uin = new Uin();
		uin.setUinData(new byte[] { 0 });
		Object[] state = new Object[] { new byte[] { 0 } , "W3LDtXpyxkl0YSifynsfhl7W-wWWtEb-ofkq-TGl1L"};
		String[] propertyNames = new String[] { "uinData", "uinDataHash" };
		interceptor.onLoad(uin, null, state, propertyNames, null);
	}
	
	@Test
	public void testOnFlushDirtyUin() throws RestClientException, JsonParseException, JsonMappingException, IOException, IdRepoDataValidationException, RestServiceException {
		when(restBuilder.buildRequest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new RestRequestDTO());
		when(restHelper.requestSync(Mockito.any()))
				.thenReturn(mapper.readValue("{\"data\":\"1234\"}".getBytes(), ObjectNode.class));
		Uin uin = new Uin();
		uin.setUinData(new byte[] { 0 });
		Object[] state = new Object[] { new byte[] { 0 } };
		String[] propertyNames = new String[] { "uinData" };
		interceptor.onFlushDirty(uin, null, state, state, propertyNames, null);
	}
	
	@Test
	public void testOnFlushDirtyUinHistory() throws RestClientException, JsonParseException, JsonMappingException, IOException, IdRepoDataValidationException, RestServiceException {
		when(restBuilder.buildRequest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new RestRequestDTO());
		when(restHelper.requestSync(Mockito.any()))
				.thenReturn(mapper.readValue("{\"data\":\"1234\"}".getBytes(), ObjectNode.class));
		UinHistory uinH = new UinHistory();
		uinH.setUinData(new byte[] { 0 });
		Object[] state = new Object[] { new byte[] { 0 } };
		String[] propertyNames = new String[] { "uinData" };
		interceptor.onFlushDirty(uinH, null, state, state, propertyNames, null);
	}
	
	@Test(expected = IdRepoAppUncheckedException.class)
	public void testOnFlushDirtyException() throws RestClientException, JsonParseException, JsonMappingException, IOException, IdRepoDataValidationException, RestServiceException {
		when(restBuilder.buildRequest(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new RestRequestDTO());
		when(restHelper.requestSync(Mockito.any()))
		.thenThrow(new RestServiceException());
		Uin uin = new Uin();
		uin.setUinData(new byte[] { 0 });
		Object[] state = new Object[] { new byte[] { 0 } };
		String[] propertyNames = new String[] { "uinData" };
		interceptor.onFlushDirty(uin, null, state, state, propertyNames, null);
	}
}
