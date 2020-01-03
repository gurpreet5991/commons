package io.mosip.authentication.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.authentication.common.service.entity.AutnTxn;
import io.mosip.authentication.common.service.repository.AutnTxnRepository;
import io.mosip.authentication.common.service.repository.UinHashSaltRepo;
import io.mosip.authentication.core.autntxn.dto.AutnTxnRequestDto;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.indauth.dto.IdType;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
public class AuthTxnServiceImplTest {


	@Mock
	private IdServiceImpl idService;
	
	@Autowired
	Environment env;

	@Mock
	private AutnTxnRepository authtxnRepo;
	
	@InjectMocks
	private AuthTxnServiceImpl authTxnServiceImpl;
	
	@Mock
	private UinHashSaltRepo uinHashSaltRepo;
	
	@Before
	public void before() {
		ReflectionTestUtils.setField(authTxnServiceImpl, "env", env);
		ReflectionTestUtils.setField(authTxnServiceImpl, "authtxnRepo", authtxnRepo);
		ReflectionTestUtils.setField(authTxnServiceImpl, "uinHashSaltRepo", uinHashSaltRepo);
	}

	@Test
	public void TestfetchAuthTxnDetails() throws IdAuthenticationBusinessException {
		AutnTxnRequestDto authtxnrequestdto = getAuthTxnDto();
		Map<String, Object> value = new HashMap<>();
		value.put("uin", "9172985031");
		Mockito.when(idService.processIdType(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
				.thenReturn(value);
		List<AutnTxn> valueList = getAuthTxnList();
		Mockito.when(uinHashSaltRepo.retrieveSaltById(Mockito.anyLong())).thenReturn("1234");
		Mockito.when(authtxnRepo.findByUin(Mockito.anyString(), Mockito.any())).thenReturn(valueList);
		authTxnServiceImpl.fetchAuthTxnDetails(authtxnrequestdto);

	}

	private AutnTxnRequestDto getAuthTxnDto() {
		AutnTxnRequestDto authtxnrequestdto = new AutnTxnRequestDto();
		authtxnrequestdto.setIndividualId("9172985031");
		authtxnrequestdto.setIndividualIdType(IdType.UIN.getType());
		authtxnrequestdto.setPageStart(1);
		authtxnrequestdto.setPageFetch(10);
		return authtxnrequestdto;
	}

	private List<AutnTxn> getAuthTxnList() {
		List<AutnTxn> valueList = new ArrayList<>();
		AutnTxn autnTxn = new AutnTxn();
		autnTxn.setRequestTrnId("1234567890");
		autnTxn.setRequestDTtimes(null);
		autnTxn.setAuthTypeCode("UIN");
		autnTxn.setStatusCode("Y");
		autnTxn.setStatusComment("success");
		autnTxn.setRefIdType("test");
		autnTxn.setEntityName("test");
		valueList.add(autnTxn);
		return valueList;
	}

}
