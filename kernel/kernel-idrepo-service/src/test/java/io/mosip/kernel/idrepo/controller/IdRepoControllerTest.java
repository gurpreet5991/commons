package io.mosip.kernel.idrepo.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.collect.Lists;

import io.mosip.kernel.core.idrepo.constant.IdRepoErrorConstants;
import io.mosip.kernel.core.idrepo.exception.IdRepoAppException;
import io.mosip.kernel.core.idrepo.spi.IdRepoService;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.idrepo.dto.IdRequestDTO;
import io.mosip.kernel.idrepo.dto.IdResponseDTO;
import io.mosip.kernel.idrepo.validator.IdRequestValidator;
import io.mosip.kernel.idvalidator.uin.impl.UinValidatorImpl;

/**
 * The Class IdRepoControllerTest.
 *
 * @author Manoj SP
 */
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
@ActiveProfiles("test")
@ConfigurationProperties("mosip.kernel.idrepo")
public class IdRepoControllerTest {

	private Map<String, String> id;

	@Mock
	private IdRepoService<IdRequestDTO, IdResponseDTO> idRepoService;

	@Mock
	private IdRequestValidator validator;

	@Mock
	private UinValidatorImpl uinValidatorImpl;

	@InjectMocks
	IdRepoController controller;

	@Mock
	HttpServletRequest request;

	@Before
	public void before() {
		ReflectionTestUtils.setField(controller, "id", id);
		ReflectionTestUtils.setField(controller, "allowedTypes", Lists.newArrayList("bio", "demo", "all"));
	}

	@Test
	public void testAddIdentity() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		IdRequestDTO request = new IdRequestDTO();
		when(idRepoService.addIdentity(any(), any())).thenReturn(response);
		ResponseEntity<IdResponseDTO> responseEntity = controller.addIdentity("1234", request,
				new BeanPropertyBindingResult(request, "IdRequestDTO"));
		assertEquals(response, responseEntity.getBody());
		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
	}
	
	@Test(expected = IdRepoAppException.class)
	public void testAddIdentityFailed() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		IdRequestDTO request = new IdRequestDTO();
		when(idRepoService.addIdentity(any(), any()))
				.thenThrow(new IdRepoAppException(IdRepoErrorConstants.UNKNOWN_ERROR));
		ResponseEntity<IdResponseDTO> responseEntity = controller.addIdentity("1234", request,
				new BeanPropertyBindingResult(request, "IdRequestDTO"));
		assertEquals(response, responseEntity.getBody());
		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
	}

	/**
	 * Test add identity exception.
	 *
	 * @throws IdRepoAppException
	 *             the id repo app exception
	 */
	@Test(expected = IdRepoAppException.class)
	public void testAddIdentityException() throws IdRepoAppException {
		IdRequestDTO request = new IdRequestDTO();
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "IdRequestDTO");
		errors.reject("errorCode");
		controller.addIdentity("1234", request, errors);
	}

	@Test(expected = IdRepoAppException.class)
	public void testAddIdentityExceptionInvalidUin() throws IdRepoAppException {
		IdRequestDTO request = new IdRequestDTO();
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "IdRequestDTO");
		when(uinValidatorImpl.validateId(anyString())).thenThrow(new InvalidIDException(null, null));
		controller.addIdentity("1234", request, errors);
	}

	/**
	 * Test retrieve identity.
	 *
	 * @throws IdRepoAppException
	 *             the id repo app exception
	 */
	@Test
	public void testRetrieveIdentity() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
		when(idRepoService.retrieveIdentity(any(), any())).thenReturn(response);
		ResponseEntity<IdResponseDTO> responseEntity = controller.retrieveIdentity("1234", "demo", request);
		assertEquals(response, responseEntity.getBody());
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}
	
	@Test
	public void testRetrieveIdentityAll() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
		when(idRepoService.retrieveIdentity(any(), any())).thenReturn(response);
		ResponseEntity<IdResponseDTO> responseEntity = controller.retrieveIdentity("1234", "demo,all", request);
		assertEquals(response, responseEntity.getBody());
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	/**
	 * Test retrieve identity.
	 *
	 * @throws IdRepoAppException
	 *             the id repo app exception
	 */
	@Test(expected = IdRepoAppException.class)
	public void testRetrieveIdentityInvalidUin() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		when(uinValidatorImpl.validateId(anyString())).thenThrow(new InvalidIDException(null, null));
		when(idRepoService.retrieveIdentity(any(), any())).thenReturn(response);
		controller.retrieveIdentity("1234", "demo", request);
	}

	@Test(expected = IdRepoAppException.class)
	public void testRetrieveIdentityInvalidType() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		when(uinValidatorImpl.validateId(anyString())).thenThrow(new InvalidIDException(null, null));
		when(idRepoService.retrieveIdentity(any(), any())).thenReturn(response);
		controller.retrieveIdentity("1234", "dem", request);
	}

	@Test(expected = IdRepoAppException.class)
	public void testRetrieveIdentityMultipleInvalidType() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		when(uinValidatorImpl.validateId(anyString())).thenThrow(new InvalidIDException(null, null));
		when(idRepoService.retrieveIdentity(any(), any())).thenReturn(response);
		controller.retrieveIdentity("1234", "dem, abc", request);
	}
	
	@Test
	public void testRetrieveIdentityMultipleValidType() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
		when(idRepoService.retrieveIdentity(any(), any())).thenReturn(response);
		controller.retrieveIdentity("1234", "demo,all,bio", request);
	}

	@Test(expected = IdRepoAppException.class)
	public void testRetrieveIdentityRequestParameterMap() throws IdRepoAppException {
		Map<String, String[]> paramMap = new HashMap<>();
		paramMap.put("k", new String[] { "v" });
		when(request.getParameterMap()).thenReturn(paramMap);
		controller.retrieveIdentity("1234", "dem, abc", request);
	}
	
	@Test(expected = IdRepoAppException.class)
	public void testRetrieveIdentityRequestParameterMapValid() throws IdRepoAppException {
		Map<String, String[]> paramMap = new HashMap<>();
		paramMap.put("type", new String[] { "demo" });
		when(request.getParameterMap()).thenReturn(paramMap);
		controller.retrieveIdentity("1234", "demo, bio", request);
	}
	
	@Test(expected = IdRepoAppException.class)
	public void testRetrieveIdentityRequestMultiParameterMap() throws IdRepoAppException {
		Map<String, String[]> paramMap = new HashMap<>();
		paramMap.put("type", new String[] { "demo" });
		paramMap.put("k", new String[] { "v" });
		when(request.getParameterMap()).thenReturn(paramMap);
		controller.retrieveIdentity("1234", "demo,bio,all", request);
	}

	/**
	 * Test retrieve identity null id.
	 *
	 * @throws IdRepoAppException
	 *             the id repo app exception
	 */
	@Test(expected = IdRepoAppException.class)
	public void testRetrieveIdentityNullId() throws IdRepoAppException {
		when(uinValidatorImpl.validateId(any())).thenThrow(new InvalidIDException(null, null));
		controller.retrieveIdentity(null, null, request);
	}

	/**
	 * Test init binder.
	 */
	@Test
	public void testInitBinder() {
		ReflectionTestUtils.setField(controller, "validator", new IdRequestValidator());
		WebDataBinder binder = new WebDataBinder(new IdRequestDTO());
		controller.initBinder(binder);
	}
	
	@Test
	public void updateIdentity() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
		when(idRepoService.updateIdentity(any(), any())).thenReturn(response);
		IdRequestDTO request = new IdRequestDTO();
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "IdRequestDTO");
		controller.updateIdentity("1234", request, errors);
	}
	
	@Test(expected = IdRepoAppException.class)
	public void updateIdentityInvalidId() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		when(uinValidatorImpl.validateId(any())).thenThrow(new InvalidIDException(null, null));
		when(idRepoService.updateIdentity(any(), any())).thenReturn(response);
		IdRequestDTO request = new IdRequestDTO();
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "IdRequestDTO");
		controller.updateIdentity("1234", request, errors);
	}
	
	@Test(expected = IdRepoAppException.class)
	public void updateIdentityIdRepoDataValidationException() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		when(uinValidatorImpl.validateId(anyString())).thenReturn(true);
		when(idRepoService.updateIdentity(any(), any())).thenReturn(response);
		IdRequestDTO request = new IdRequestDTO();
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(request, "IdRequestDTO");
		errors.reject("");
		controller.updateIdentity("1234", request, errors);
	}
	
	@Test(expected = IdRepoAppException.class)
	public void testUpdateIdentityFailed() throws IdRepoAppException {
		IdResponseDTO response = new IdResponseDTO();
		IdRequestDTO request = new IdRequestDTO();
		when(idRepoService.updateIdentity(any(), any()))
				.thenThrow(new IdRepoAppException(IdRepoErrorConstants.UNKNOWN_ERROR));
		ResponseEntity<IdResponseDTO> responseEntity = controller.updateIdentity("1234", request,
				new BeanPropertyBindingResult(request, "IdRequestDTO"));
		assertEquals(response, responseEntity.getBody());
		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
	}

	public Map<String, String> getId() {
		return id;
	}

	public void setId(Map<String, String> id) {
		this.id = id;
	}
}
