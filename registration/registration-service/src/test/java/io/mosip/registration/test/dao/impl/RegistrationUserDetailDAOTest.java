package io.mosip.registration.test.dao.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.mosip.registration.dao.impl.RegistrationUserDetailDAOImpl;
import io.mosip.registration.entity.RegistrationUserDetail;
import io.mosip.registration.repositories.RegistrationUserDetailRepository;

public class RegistrationUserDetailDAOTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@InjectMocks
	private RegistrationUserDetailDAOImpl registrationUserDetailDAOImpl;

	@Mock
	private RegistrationUserDetailRepository registrationUserDetailRepository;

	@Test
	public void getUserDetailSuccessTest() {

		RegistrationUserDetail registrationUserDetail = new RegistrationUserDetail();
		registrationUserDetail.setName("Sravya");
		List<RegistrationUserDetail> registrationUserDetailList = new ArrayList<RegistrationUserDetail>();
		registrationUserDetailList.add(registrationUserDetail);

		Mockito.when(registrationUserDetailRepository.findByIdAndIsActiveTrue("mosip"))
				.thenReturn(registrationUserDetailList);
		assertTrue(!registrationUserDetailList.isEmpty());
		assertNotNull(registrationUserDetailDAOImpl.getUserDetail("mosip"));
	}

	@Test
	public void getUserDetailFailureTest() {

		RegistrationUserDetail registrationUserDetail = new RegistrationUserDetail();
		List<RegistrationUserDetail> registrationUserDetailList = new ArrayList<RegistrationUserDetail>();
		registrationUserDetailList.add(registrationUserDetail);

		Mockito.when(registrationUserDetailRepository.findByIdAndIsActiveTrue("mosip"))
				.thenReturn(registrationUserDetailList);
		assertFalse(registrationUserDetailList.isEmpty());
		assertNotNull(registrationUserDetailDAOImpl.getUserDetail("mosip"));
	}

}
