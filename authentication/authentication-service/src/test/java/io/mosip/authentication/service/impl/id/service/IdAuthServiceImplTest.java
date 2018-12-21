package io.mosip.authentication.service.impl.id.service;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.spi.id.service.IdAuthService;
import io.mosip.authentication.core.spi.id.service.IdRepoService;
import io.mosip.authentication.service.factory.AuditRequestFactory;
import io.mosip.authentication.service.factory.RestRequestFactory;
import io.mosip.authentication.service.helper.RestHelper;
import io.mosip.authentication.service.impl.id.service.impl.IdAuthServiceImpl;
import io.mosip.authentication.service.repository.VIDRepository;

/**
 * IdAuthServiceImplTest test class.
 *
 * @author Rakesh Roshan
 */
@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
public class IdAuthServiceImplTest {

	@Mock
	private IdRepoService idRepoService;
	@Mock
	private AuditRequestFactory auditFactory;
	@Mock
	private RestRequestFactory restFactory;
	@Mock
	private RestHelper restHelper;
	@Mock
	private VIDRepository vidRepository;

	@InjectMocks
	IdAuthServiceImpl idAuthServiceImpl;

	@Mock
	IdAuthServiceImpl idAuthServiceImplMock;

	@Mock
	IdAuthService idAuthService;

	@Before
	public void before() {
		ReflectionTestUtils.setField(idAuthServiceImpl, "idRepoService", idRepoService);
		ReflectionTestUtils.setField(idAuthServiceImpl, "auditFactory", auditFactory);
		ReflectionTestUtils.setField(idAuthServiceImpl, "restFactory", restFactory);
		ReflectionTestUtils.setField(idAuthServiceImpl, "vidRepository", vidRepository);

		/*
		 * ReflectionTestUtils.setField(idAuthServiceImplMock, "idRepoService",
		 * idRepoService); ReflectionTestUtils.setField(idAuthServiceImplMock,
		 * "auditFactory", auditFactory);
		 * ReflectionTestUtils.setField(idAuthServiceImplMock, "restFactory",
		 * restFactory); ReflectionTestUtils.setField(idAuthServiceImplMock,
		 * "uinRepository", uinRepository);
		 * ReflectionTestUtils.setField(idAuthServiceImplMock, "vidRepository",
		 * vidRepository);
		 */
	}

	@Test
	public void testGetIdRepoByUinNumber() throws IdAuthenticationBusinessException {

		ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "getIdRepoByUinNumber", Mockito.anyString());

	}

	@Test
	public void testAuditData() {
		ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "auditData");
	}

	@Test
	public void testGetIdRepoByVidNumber() throws IdAuthenticationBusinessException {

		ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "getIdRepoByVidNumber", Mockito.anyString());
	}

	@Ignore
	@Test
	public void testGetIdRepoByVidAsRequest_IsNotNull() throws IdAuthenticationBusinessException {
		Map<String, Object> idRepo = new HashMap<>();
		idRepo.put("uin", "476567");

		Mockito.when(idRepoService.getIdRepo(Mockito.anyString())).thenReturn(idRepo);
		Object invokeMethod = ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "getIdRepoByVidAsRequest",
				Mockito.anyString());
		assertNotNull(invokeMethod);
	}

	@Test
	public void testProcessIdType_IdTypeIsD() throws IdAuthenticationBusinessException {
		String idvIdType = "D";
		String idvId = "875948796";
		Map<String, Object> idRepo = new HashMap<>();
		idRepo.put("uin", "476567");

		ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "processIdType", idvIdType, idvId);
	}

	@Test
	public void testProcessIdType_IdTypeIsV() throws IdAuthenticationBusinessException {
		String idvIdType = "V";
		String idvId = "875948796";

		ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "processIdType", idvIdType, idvId);
	}

	@Ignore
	@Test(expected = IdAuthenticationBusinessException.class)
	public void processIdtypeVIDFailed() throws IdAuthenticationBusinessException {
		String idvIdType = "V";
		String idvId = "875948796";

		IdAuthenticationBusinessException idBusinessException = new IdAuthenticationBusinessException(
				IdAuthenticationErrorConstants.INVALID_VID);

		Mockito.when(idRepoService.getIdRepo(Mockito.anyString())).thenThrow(idBusinessException);

		Mockito.when(idAuthService.getIdRepoByVidNumber(Mockito.anyString())).thenThrow(idBusinessException);
		Mockito.when(idAuthServiceImpl.processIdType(idvIdType, idvId)).thenThrow(idBusinessException);

	}

	@Test(expected = IdAuthenticationBusinessException.class)
	public void processIdtypeUINFailed() throws IdAuthenticationBusinessException {
		String idvIdType = "D";
		String idvId = "875948796";

		IdAuthenticationBusinessException idBusinessException = new IdAuthenticationBusinessException(
				IdAuthenticationErrorConstants.INVALID_UIN);

		Mockito.when(idRepoService.getIdRepo(Mockito.anyString())).thenThrow(idBusinessException);

		Mockito.when(idAuthService.getIdRepoByVidNumber(Mockito.anyString())).thenThrow(idBusinessException);
		Mockito.when(idAuthServiceImpl.processIdType(idvIdType, idvId)).thenThrow(idBusinessException);

	}
}
