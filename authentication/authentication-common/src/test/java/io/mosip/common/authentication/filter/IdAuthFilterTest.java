/*package io.mosip.common.authentication.filter;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.authentication.common.authentication.filter.IdAuthFilter;
import io.mosip.authentication.common.authentication.filter.ResettableStreamHttpServletRequest;
import io.mosip.authentication.common.integration.KeyManager;
import io.mosip.authentication.core.exception.IdAuthenticationAppException;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@WebMvcTest
@AutoConfigureMockMvc
public class IdAuthFilterTest {

	@Autowired
	private Environment env;

	@Autowired
	private ObjectMapper mapper;

	IdAuthFilter filter = new IdAuthFilter();

	Map<String, Object> requestBody = new HashMap<>();

	Map<String, Object> responseBody = new HashMap<>();

	@Before
	public void before() {
		ReflectionTestUtils.setField(filter, "mapper", mapper);
		ReflectionTestUtils.setField(filter, "env", env);
	}

	@Test
	public void testSetTxnId() throws IdAuthenticationAppException, ServletException {
		requestBody.put("txnId", null);
		responseBody.put("txnId", "1234");
		assertEquals(responseBody.toString(), filter.setResponseParams(requestBody, responseBody).toString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDecodedRequest() throws IdAuthenticationAppException, ServletException, JsonParseException,
			JsonMappingException, IOException {
		KeyManager keyManager = Mockito.mock(KeyManager.class);
		ReflectionTestUtils.setField(filter, "keyManager", keyManager);
		requestBody.put("request",
				"ew0KCSJhdXRoVHlwZSI6IHsNCgkJImFkZHJlc3MiOiAidHJ1ZSIsDQoJCSJiaW8iOiAidHJ1ZSIsDQoJCSJmYWNlIjogInRydWUiLA0KCQkiZmluZ2VycHJpbnQiOiAidHJ1ZSIsDQoJCSJmdWxsQWRkcmVzcyI6ICJ0cnVlIiwNCgkJImlyaXMiOiAidHJ1ZSIsDQoJCSJvdHAiOiAidHJ1ZSIsDQoJCSJwZXJzb25hbElkZW50aXR5IjogInRydWUiLA0KCQkicGluIjogInRydWUiDQoJfQ0KfQ==");
		requestBody.put("requestHMAC","B93ACCB8D7A0B005864F684FB1F53A833BAF547ED4D610C5057DE6B55A4EF76C");
		responseBody.put("request",
				"{authType={address=true, bio=true, face=true, fingerprint=true, fullAddress=true, iris=true, otp=true, personalIdentity=true, pin=true}}");
		String dicipheredreq = "{\"authType\":{\"address\":\"true\",\"bio\":\"true\",\"face\":\"true\",\"fingerprint\":\"true\",\"fullAddress\":\"true\",\"iris\":\"true\",\"otp\":\"true\",\"personalIdentity\":\"true\",\"pin\":\"true\"}}";
		Mockito.when(keyManager.requestData(Mockito.any(), Mockito.any())).thenReturn(new ObjectMapper().readValue(
				dicipheredreq
						.getBytes(),
				Map.class));
		Map<String, Object> decipherRequest = filter.decipherRequest(requestBody);
		decipherRequest.remove("requestHMAC");
		assertEquals(responseBody.toString(), decipherRequest.toString());
	}

	@Test(expected = IdAuthenticationAppException.class)
	public void testInValidDecodedRequest()
			throws IdAuthenticationAppException, JsonParseException, JsonMappingException, IOException {
		KeyManager keyManager = Mockito.mock(KeyManager.class);
		ReflectionTestUtils.setField(filter, "keyManager", keyManager);
		requestBody.put("request", 123214214);
		filter.decipherRequest(requestBody);
	}

	@Test
	public void testEncodedResponse() throws IdAuthenticationAppException, ServletException {
		
		 * requestBody.put("request",
		 * "e2F1dGhUeXBlPXthZGRyZXNzPXRydWUsIGJpbz10cnVlLCBmYWNlPXRydWUsIGZpbmdlcnByaW50PXRydWUsIGZ1bGxBZGRyZXNzPXRydWUsIGlyaXM9dHJ1ZSwgb3RwPXRydWUsIHBlcnNvbmFsSWRlbnRpdHk9dHJ1ZSwgcGluPXRydWV9fQ=="
		 * );
		 
		requestBody.put("request",
				"{authType={address=true, bio=true, face=true, fingerprint=true, fullAddress=true, iris=true, otp=true, personalIdentity=true, pin=true}}");
		responseBody.put("request",
				"{authType={address=true, bio=true, face=true, fingerprint=true, fullAddress=true, iris=true, otp=true, personalIdentity=true, pin=true}}");
		assertEquals(requestBody.toString(), filter.encipherResponse(responseBody).toString());
	}

	@Test
	public void testSign() throws IdAuthenticationAppException {
		assertEquals(true, filter.validateSignature("something", "something".getBytes()));
	}
	
	@Test
	public void testValidMispLicenseKey() throws IdAuthenticationAppException {
		String mispId=ReflectionTestUtils.invokeMethod(filter,"licenseKeyMISPMapping","735899345" );
		assertEquals("5479834598", mispId);
	}
	
	@Test(expected=IdAuthenticationAppException.class)
	public void testExpiredLicenseKey() throws IdAuthenticationAppException {
	 try {	
		 ReflectionTestUtils.invokeMethod(filter,"licenseKeyMISPMapping","135898653" );
	 }
	 catch(UndeclaredThrowableException ex) {
		 throw new IdAuthenticationAppException();
	 }
	}
	
	@Test(expected=IdAuthenticationAppException.class)
	public void testInactiveLicenseKey() throws IdAuthenticationAppException {
	  try {	
		ReflectionTestUtils.invokeMethod(filter,"licenseKeyMISPMapping","635899234" );
	  }	
		catch(UndeclaredThrowableException ex) {
			 throw new IdAuthenticationAppException();
		 }
	}
	
	public void validPartnerIdTest() throws IdAuthenticationAppException {
		ReflectionTestUtils.invokeMethod(filter,"validPartnerId","1873299273" );
	}
	
	@Test(expected=IdAuthenticationAppException.class)
	public void inValidPartnerIdTest() throws IdAuthenticationAppException {
		 try {	
			 ReflectionTestUtils.invokeMethod(filter,"validPartnerId","18732937232" );
		 }
		 catch(UndeclaredThrowableException ex) {
			 throw new IdAuthenticationAppException();
		 }
	}
	
	
	@Test(expected=IdAuthenticationAppException.class)
	public void inactivePartnerIdTest() throws IdAuthenticationAppException {
		 try {	
			 ReflectionTestUtils.invokeMethod(filter,"validPartnerId","1873293764" );
		 }
		 catch(UndeclaredThrowableException ex) {
			 throw new IdAuthenticationAppException();
		 }
	}
	
	@Test(expected=IdAuthenticationAppException.class)
	public void policyUnmappedPartnerIdTest() throws IdAuthenticationAppException {
		 try {	
			 ReflectionTestUtils.invokeMethod(filter,"validPartnerId","18248239994" );
		 }
		 catch(UndeclaredThrowableException ex) {
			 throw new IdAuthenticationAppException();
		 }
	}
	
	@Test
	public void validMISPPartnerIdTest() throws IdAuthenticationAppException {
		String policyId= ReflectionTestUtils.invokeMethod(filter,"validMISPPartnerMapping","1873299273","5479834598" );
		assertEquals("92834787293", policyId);
	}
	
	
	@Test(expected=IdAuthenticationAppException.class)
	public void inValidMISPPartnerIdTest() throws IdAuthenticationAppException {
		 try {	
			ReflectionTestUtils.invokeMethod(filter,"validMISPPartnerMapping","1873299300","9870862555" );
		 }
		 catch(UndeclaredThrowableException ex) {
			 throw new IdAuthenticationAppException();
		 }
	}
	
	@Test(expected=IdAuthenticationAppException.class)
	public void mandatoryAuthPolicyTestOTPcheck() throws IdAuthenticationAppException {
		String policyId="92834787293";
		String requestedAuth="{\"requestedAuth\": {\r\n" + 
				"                             \"bio\": true,\r\n" + 
				"                             \"demo\": false,\r\n" + 
				"                             \"otp\": false,\r\n" + 
				"                             \"pin\": false\r\n" + 
				"              }}";
		try {
			Map<String,Object> requestBodyMap=mapper.readValue(requestedAuth.getBytes("UTF-8"), HashMap.class);
			
			filter.checkAllowedAuthTypeBasedOnPolicy(policyId, requestBodyMap);
		} catch (IOException e) {
			throw new IdAuthenticationAppException();
		}
	}
	
	@Test(expected=IdAuthenticationAppException.class)
	public void mandatoryAuthPolicyTestbiocheck() throws IdAuthenticationAppException {
		String policyId="92834787293";
		String requestedAuth="{\"requestedAuth\": {\r\n" + 
				"                             \"bio\": false,\r\n" + 
				"                             \"demo\": false,\r\n" + 
				"                             \"otp\": true,\r\n" + 
				"                             \"pin\": false\r\n" + 
				"              }}";
		try {
			Map<String,Object> requestBodyMap=mapper.readValue(requestedAuth.getBytes("UTF-8"), HashMap.class);
			
			filter.checkAllowedAuthTypeBasedOnPolicy(policyId, requestBodyMap);
		} catch (IOException e) {
			throw new IdAuthenticationAppException();
		}
	}


@Test(expected=IdAuthenticationAppException.class)
public void mandatoryAuthPolicyTestpincheck() throws IdAuthenticationAppException {
	String policyId="0983222";
	String requestedAuth="{\"requestedAuth\": {\r\n" + 
			"                             \"bio\": false,\r\n" + 
			"                             \"demo\": false,\r\n" + 
			"                             \"otp\": true,\r\n" + 
			"                             \"pin\": false\r\n" + 
			"              }}";
	try {
		Map<String,Object> requestBodyMap=mapper.readValue(requestedAuth.getBytes("UTF-8"), HashMap.class);
		
		filter.checkAllowedAuthTypeBasedOnPolicy(policyId, requestBodyMap);
	} catch (IOException e) {
		throw new IdAuthenticationAppException();
	}
}

@Test(expected=IdAuthenticationAppException.class)
public void mandatoryAuthPolicyTestdemocheck() throws IdAuthenticationAppException {
	String policyId="0983252";
	String requestedAuth="{\"requestedAuth\": {\r\n" + 
			"                             \"bio\": false,\r\n" + 
			"                             \"demo\": false,\r\n" + 
			"                             \"otp\": false,\r\n" + 
			"                             \"pin\": true\r\n" + 
			"              }}";
	try {
		Map<String,Object> requestBodyMap=mapper.readValue(requestedAuth.getBytes("UTF-8"), HashMap.class);
		filter.checkAllowedAuthTypeBasedOnPolicy(policyId, requestBodyMap);
	} catch (IOException e) {
		throw new IdAuthenticationAppException();
	}
}

@Test(expected=IdAuthenticationAppException.class)
public void allowedAuthPolicyTestOTPCheck() throws IdAuthenticationAppException {
	String policyId="0983252";
	String requestedAuth="{\"requestedAuth\": {\r\n" + 
			"                             \"bio\": false,\r\n" + 
			"                             \"demo\": false,\r\n" + 
			"                             \"otp\": true,\r\n" + 
			"                             \"pin\": false\r\n" + 
			"              }}";
	try {
		Map<String,Object> requestBodyMap=mapper.readValue(requestedAuth.getBytes("UTF-8"), HashMap.class);
		filter.checkAllowedAuthTypeBasedOnPolicy(policyId, requestBodyMap);
	} catch (IOException e) {
		throw new IdAuthenticationAppException();
	}
}

@Test(expected=IdAuthenticationAppException.class)
public void allowedAuthPolicyTestDemoCheck() throws IdAuthenticationAppException {
	String policyId="0983222";
	String requestedAuth="{\"requestedAuth\": {\r\n" + 
			"                             \"bio\": false,\r\n" + 
			"                             \"demo\": true,\r\n" + 
			"                             \"otp\": false,\r\n" + 
			"                             \"pin\": false\r\n" + 
			"              }}";
	try {
		Map<String,Object> requestBodyMap=mapper.readValue(requestedAuth.getBytes("UTF-8"), HashMap.class);
		filter.checkAllowedAuthTypeBasedOnPolicy(policyId, requestBodyMap);
	} catch (IOException e) {
		throw new IdAuthenticationAppException();
	}
}

@Test(expected=IdAuthenticationAppException.class)
public void allowedAuthPolicyTestPinCheck() throws IdAuthenticationAppException {
	String policyId="0983754";
	String requestedAuth="{\"requestedAuth\": {\r\n" + 
			"                             \"bio\": false,\r\n" + 
			"                             \"demo\": false,\r\n" + 
			"                             \"otp\": false,\r\n" + 
			"                             \"pin\": true\r\n" + 
			"              }}";
	try {
		Map<String,Object> requestBodyMap=mapper.readValue(requestedAuth.getBytes("UTF-8"), HashMap.class);
		filter.checkAllowedAuthTypeBasedOnPolicy(policyId, requestBodyMap);
	} catch (IOException e) {
		throw new IdAuthenticationAppException();
	}
}

@Test(expected=IdAuthenticationAppException.class)
public void allowedAuthPolicyTestBioCheck() throws IdAuthenticationAppException {
	String policyId="0123456";
	String requestedAuth="{\"requestedAuth\": {\r\n" + 
			"                             \"bio\": true,\r\n" + 
			"                             \"demo\": true,\r\n" + 
			"                             \"otp\": true,\r\n" + 
			"                             \"pin\": true\r\n" + 
			"              }}";
	try {
		Map<String,Object> requestBodyMap=mapper.readValue(requestedAuth.getBytes("UTF-8"), HashMap.class);
		filter.checkAllowedAuthTypeBasedOnPolicy(policyId, requestBodyMap);
	} catch (IOException e) {
		throw new IdAuthenticationAppException();
	}
}



@Test(expected=IdAuthenticationAppException.class)
public void validateDecipheredRequestTest() throws IdAuthenticationAppException {
	ResettableStreamHttpServletRequest requestWrapper=new ResettableStreamHttpServletRequest(new HttpServletRequest() {
		
		@Override
		public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
				throws IllegalStateException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public AsyncContext startAsync() throws IllegalStateException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void setAttribute(String name, Object o) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void removeAttribute(String name) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean isSecure() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isAsyncSupported() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isAsyncStarted() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public ServletContext getServletContext() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public int getServerPort() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public String getServerName() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getScheme() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public RequestDispatcher getRequestDispatcher(String path) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public int getRemotePort() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public String getRemoteHost() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getRemoteAddr() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getRealPath(String path) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public BufferedReader getReader() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getProtocol() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String[] getParameterValues(String name) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Enumeration<String> getParameterNames() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Map<String, String[]> getParameterMap() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getParameter(String name) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Enumeration<Locale> getLocales() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Locale getLocale() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public int getLocalPort() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public String getLocalName() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getLocalAddr() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public ServletInputStream getInputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public DispatcherType getDispatcherType() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getContentType() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public long getContentLengthLong() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public int getContentLength() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public String getCharacterEncoding() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Enumeration<String> getAttributeNames() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Object getAttribute(String name) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public AsyncContext getAsyncContext() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass)
				throws IOException, ServletException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void logout() throws ServletException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void login(String username, String password) throws ServletException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean isUserInRole(String role) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isRequestedSessionIdValid() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isRequestedSessionIdFromUrl() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isRequestedSessionIdFromURL() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isRequestedSessionIdFromCookie() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public Principal getUserPrincipal() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public HttpSession getSession(boolean create) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public HttpSession getSession() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getServletPath() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getRequestedSessionId() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public StringBuffer getRequestURL() {
			// TODO Auto-generated method stub
			return new StringBuffer("localhost:8090/identity/auth/0.8/1873299273/735899345");
		}
		
		@Override
		public String getRequestURI() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getRemoteUser() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getQueryString() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getPathTranslated() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getPathInfo() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Collection<Part> getParts() throws IOException, ServletException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Part getPart(String name) throws IOException, ServletException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getMethod() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public int getIntHeader(String name) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public Enumeration<String> getHeaders(String name) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Enumeration<String> getHeaderNames() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getHeader(String name) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public long getDateHeader(String name) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public Cookie[] getCookies() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getContextPath() {
			// TODO Auto-generated method stub
			return "identity";
		}
		
		@Override
		public String getAuthType() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String changeSessionId() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
			// TODO Auto-generated method stub
			return false;
		}
	});
	String requestedAuth="{\"requestedAuth\": {\r\n" + 
			"                             \"bio\": true,\r\n" + 
			"                             \"demo\": false,\r\n" + 
			"                             \"otp\": false,\r\n" + 
			"                             \"pin\": false\r\n" + 
			"              }}";
	
		Map<String, Object> requestBodyMap;
		try {
			requestBodyMap = mapper.readValue(requestedAuth.getBytes("UTF-8"), HashMap.class);
			filter.validateDecipheredRequest(requestWrapper, requestBodyMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new IdAuthenticationAppException();
		}
		
	
}



}
*/