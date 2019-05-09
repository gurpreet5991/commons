package io.mosip.registration.test.service;

import static org.mockito.Mockito.doNothing;

import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.web.client.HttpClientErrorException;

import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dao.PolicySyncDAO;
import io.mosip.registration.entity.KeyStore;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.impl.PolicySyncServiceImpl;
import io.mosip.registration.service.impl.PublicKeySyncImpl;
import io.mosip.registration.util.restclient.ServiceDelegateUtil;

public class PublicKeySyncTest {

	@Rule
	public MockitoRule MockitoRule = MockitoJUnit.rule();

	@Mock
	private PolicySyncDAO policySyncDAO;

	@Mock
	private ServiceDelegateUtil serviceDelegateUtil;

	@InjectMocks
	private PublicKeySyncImpl publicKeySyncImpl;

	@Test
	public void getPublicKey()
			throws ParseException, HttpClientErrorException, SocketTimeoutException, RegBaseCheckedException {

		KeyStore keys = new KeyStore();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
		Date date = dateFormat.parse("2019-4-5");
		Timestamp timestamp = new Timestamp(date.getTime());
		keys.setValidTillDtimes(timestamp);

		Map<String, Object> responseMap = new LinkedHashMap<>();
		LinkedHashMap<String, Object> valuesMap = new LinkedHashMap<>();
		valuesMap.put("publicKey",
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtCR2L_MwUv4ctfGulWf4ZoWkSyBHbfkVtE_xAmzzIDWHP1V5hGxg8jt8hLtYYFwBNj4l_PTZGkblcVg-IePHilmQiVDptTVVA2PGtwRdud7QL4xox8RXmIf-xa-JmP2E804iVM-Ki8aPf1yuxXNUwLxZsflFww73lc-SGVUHupD8Os0qNZbbJl0BYioNG4WmPMHy3WJ-7jGN0HEV-9E18yf_enR0YewUmUI6Rxxb606-w8iQyWfSJq6UOfFmH5WAn-oTOoTIwg_fBxXuG_FlDoNWs6N5JtI18BMsUQA_GQZJct6TyXcBNUrcBYhZERvPlRGqIOoTl-T2sPJ5ST9eswIDAQAB");
		valuesMap.put("issuedAt", "2020-04-09T05:51:17.334");
		valuesMap.put("expiryAt", "2020-04-09T05:51:17.334");
		responseMap.put(RegistrationConstants.PACKET_STATUS_READER_RESPONSE, valuesMap);

		Mockito.when(policySyncDAO.getPublicKey(Mockito.anyString())).thenReturn(keys);
		doNothing().when(policySyncDAO).updatePolicy(keys);
		Mockito.when(serviceDelegateUtil.get(Mockito.anyString(), Mockito.anyMap(), Mockito.anyBoolean(),
				Mockito.anyString())).thenReturn(responseMap);

		publicKeySyncImpl.getPublicKey(Mockito.anyString());

	}
	
	@Test
	public void getPublicKeyLogin()
			throws ParseException, HttpClientErrorException, SocketTimeoutException, RegBaseCheckedException {

		KeyStore keys = null;;
		Map<String, Object> responseMap = new LinkedHashMap<>();
		LinkedHashMap<String, Object> valuesMap = new LinkedHashMap<>();
		valuesMap.put("publicKey",
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtCR2L_MwUv4ctfGulWf4ZoWkSyBHbfkVtE_xAmzzIDWHP1V5hGxg8jt8hLtYYFwBNj4l_PTZGkblcVg-IePHilmQiVDptTVVA2PGtwRdud7QL4xox8RXmIf-xa-JmP2E804iVM-Ki8aPf1yuxXNUwLxZsflFww73lc-SGVUHupD8Os0qNZbbJl0BYioNG4WmPMHy3WJ-7jGN0HEV-9E18yf_enR0YewUmUI6Rxxb606-w8iQyWfSJq6UOfFmH5WAn-oTOoTIwg_fBxXuG_FlDoNWs6N5JtI18BMsUQA_GQZJct6TyXcBNUrcBYhZERvPlRGqIOoTl-T2sPJ5ST9eswIDAQAB");
		valuesMap.put("issuedAt", "2020-04-09T05:51:17.334");
		valuesMap.put("expiryAt", "2020-04-09T05:51:17.334");
		responseMap.put(RegistrationConstants.PACKET_STATUS_READER_RESPONSE, valuesMap);

		Mockito.when(policySyncDAO.getPublicKey(Mockito.anyString())).thenReturn(keys);
		doNothing().when(policySyncDAO).updatePolicy(keys);
		Mockito.when(serviceDelegateUtil.get(Mockito.anyString(), Mockito.anyMap(), Mockito.anyBoolean(),
				Mockito.anyString())).thenReturn(responseMap);

		publicKeySyncImpl.getPublicKey(Mockito.anyString());

	}
	
	@Test
	public void getPublicKeyLoginFailure()
			throws ParseException, HttpClientErrorException, SocketTimeoutException, RegBaseCheckedException {

		KeyStore keys = null;;
		Map<String, Object> responseMap = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> valuesMap = new ArrayList<>();
		LinkedHashMap<String, Object> errorMap = new LinkedHashMap<>();
		errorMap.put("errorCode", "KER-KMS-005");
		errorMap.put("message", "Required String parameter 'timeStamp' is not present");
		valuesMap.add(errorMap);
		responseMap.put(RegistrationConstants.PACKET_STATUS_READER_RESPONSE, null);
		responseMap.put(RegistrationConstants.ERRORS, valuesMap);

		Mockito.when(policySyncDAO.getPublicKey(Mockito.anyString())).thenReturn(keys);
		doNothing().when(policySyncDAO).updatePolicy(keys);
		Mockito.when(serviceDelegateUtil.get(Mockito.anyString(), Mockito.anyMap(), Mockito.anyBoolean(),
				Mockito.anyString())).thenReturn(responseMap);

		publicKeySyncImpl.getPublicKey(Mockito.anyString());

	}

	@Test
	public void getPublicKeyError()
			throws ParseException, HttpClientErrorException, SocketTimeoutException, RegBaseCheckedException {

		KeyStore keys = new KeyStore();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
		Date date = dateFormat.parse("2019-4-5");
		Timestamp timestamp = new Timestamp(date.getTime());
		keys.setValidTillDtimes(timestamp);

		Map<String, Object> responseMap = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> valuesMap = new ArrayList<>();
		LinkedHashMap<String, Object> errorMap = new LinkedHashMap<>();
		errorMap.put("errorCode", "KER-KMS-005");
		errorMap.put("message", "Required String parameter 'timeStamp' is not present");
		valuesMap.add(errorMap);
		responseMap.put(RegistrationConstants.PACKET_STATUS_READER_RESPONSE, null);
		responseMap.put(RegistrationConstants.ERRORS, valuesMap);

		Mockito.when(policySyncDAO.getPublicKey(Mockito.anyString())).thenReturn(keys);
		doNothing().when(policySyncDAO).updatePolicy(keys);
		Mockito.when(serviceDelegateUtil.get(Mockito.anyString(), Mockito.anyMap(), Mockito.anyBoolean(),
				Mockito.anyString())).thenReturn(responseMap);

		publicKeySyncImpl.getPublicKey(Mockito.anyString());

	}
	
	@Test
	public void getPublicKeyException()
			throws ParseException, HttpClientErrorException, SocketTimeoutException, RegBaseCheckedException {

		KeyStore keys = new KeyStore();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
		Date date = dateFormat.parse("2019-4-5");
		Timestamp timestamp = new Timestamp(date.getTime());
		keys.setValidTillDtimes(timestamp);

		Map<String, Object> responseMap = new LinkedHashMap<>();
		List<LinkedHashMap<String, Object>> valuesMap = new ArrayList<>();
		LinkedHashMap<String, Object> errorMap = new LinkedHashMap<>();
		errorMap.put("errorCode", "KER-KMS-005");
		errorMap.put("message", "Required String parameter 'timeStamp' is not present");
		valuesMap.add(errorMap);
		responseMap.put(RegistrationConstants.PACKET_STATUS_READER_RESPONSE, null);
		responseMap.put(RegistrationConstants.ERRORS, valuesMap);

		Mockito.when(policySyncDAO.getPublicKey(Mockito.anyString())).thenReturn(keys);
		doNothing().when(policySyncDAO).updatePolicy(keys);
		Mockito.when(serviceDelegateUtil.get(Mockito.anyString(), Mockito.anyMap(), Mockito.anyBoolean(),
				Mockito.anyString())).thenThrow(SocketTimeoutException.class);

		publicKeySyncImpl.getPublicKey(Mockito.anyString());

	}

}
