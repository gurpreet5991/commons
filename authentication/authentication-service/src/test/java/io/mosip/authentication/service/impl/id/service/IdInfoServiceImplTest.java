package io.mosip.authentication.service.impl.id.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.authentication.core.exception.IdAuthenticationDaoException;
import io.mosip.authentication.service.integration.IdRepoManager;

//@RunWith(SpringRunner.class)

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:sample-output-test.properties")
public class IdInfoServiceImplTest {

	IdRepoManager IdInfoService = new IdRepoManager();

	@Value("${sample.demo.entity}")
	String value;

	@Before
	public void before() {
		ReflectionTestUtils.setField(IdInfoService, "value", value);
	}

	@Ignore
	@Test
	public void getMapvalue() throws IdAuthenticationDaoException {
		//Map valuemap = IdInfoService.getIdInfo("12232323");
	}
}
