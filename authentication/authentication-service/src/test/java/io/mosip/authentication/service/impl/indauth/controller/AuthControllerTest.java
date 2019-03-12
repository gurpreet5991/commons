package io.mosip.authentication.service.impl.indauth.controller;

import org.junit.Before;
import org.junit.Ignore;
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
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.dto.indauth.AuthRequestDTO;
import io.mosip.authentication.core.dto.indauth.AuthResponseDTO;
import io.mosip.authentication.core.dto.indauth.KycAuthRequestDTO;
import io.mosip.authentication.core.exception.IdAuthenticationAppException;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.exception.IdAuthenticationDaoException;
import io.mosip.authentication.service.factory.AuditRequestFactory;
import io.mosip.authentication.service.factory.RestRequestFactory;
import io.mosip.authentication.service.helper.RestHelper;
import io.mosip.authentication.service.impl.indauth.facade.AuthFacadeImpl;
import io.mosip.authentication.service.impl.indauth.service.KycServiceImpl;
import io.mosip.authentication.service.impl.indauth.validator.AuthRequestValidator;
import io.mosip.authentication.service.impl.indauth.validator.KycAuthRequestValidator;

/**
 * This code tests the AuthController
 * 
 * @author Arun Bose
 * 
 * @author Prem Kumar
 */

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
public class AuthControllerTest {

	@Mock
	private RestHelper restHelper;

	@Autowired
	Environment env;

	@InjectMocks
	private RestRequestFactory restFactory;

	@InjectMocks
	private AuditRequestFactory auditFactory;

	@Mock
	private AuthFacadeImpl authFacade;

	@InjectMocks
	private AuthController authController;

	@Mock
	WebDataBinder binder;

	@InjectMocks
	private KycAuthRequestValidator KycAuthRequestValidator;

	@InjectMocks
	private AuthRequestValidator authRequestValidator;

	Errors error = new BindException(AuthRequestDTO.class, "authReqDTO");
	Errors errors = new BindException(KycAuthRequestDTO.class, "kycAuthReqDTO");

	/** The Kyc Service */
	@Mock
	private KycServiceImpl kycService;

	@Before
	public void before() {
		ReflectionTestUtils.setField(auditFactory, "env", env);
		ReflectionTestUtils.setField(restFactory, "env", env);
		ReflectionTestUtils.invokeMethod(authController, "initAuthRequestBinder", binder);
		ReflectionTestUtils.invokeMethod(authController, "initKycBinder", binder);
		ReflectionTestUtils.setField(authController, "authFacade", authFacade);

		ReflectionTestUtils.setField(KycAuthRequestValidator, "env", env);
		ReflectionTestUtils.setField(authFacade, "kycService", kycService);
		ReflectionTestUtils.setField(authFacade, "env", env);
		ReflectionTestUtils.setField(KycAuthRequestValidator, "authRequestValidator", authRequestValidator);
	}

	/*
	 * 
	 * Errors in the AuthRequestValidator is handled here and exception is thrown
	 */
	@Test(expected = IdAuthenticationAppException.class)
	public void showRequestValidator()
			throws IdAuthenticationAppException, IdAuthenticationBusinessException, IdAuthenticationDaoException {
		AuthRequestDTO authReqDTO = new AuthRequestDTO();
		Errors error = new BindException(authReqDTO, "authReqDTO");
		error.rejectValue("id", "errorCode", "defaultMessage");
		authController.authenticateApplication(authReqDTO, error,"123456","123456");

	}

	@Test(expected = IdAuthenticationAppException.class)
	public void authenticationFailed()
			throws IdAuthenticationAppException, IdAuthenticationBusinessException, IdAuthenticationDaoException {
		AuthRequestDTO authReqDTO = new AuthRequestDTO();
		Mockito.when(authFacade.authenticateApplicant(authReqDTO, true,"123456","123456"))
				.thenThrow(new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UIN_DEACTIVATED));
		authController.authenticateApplication(authReqDTO, error,"123456","123456");

	}

	@Test
	public void authenticationSuccess()
			throws IdAuthenticationAppException, IdAuthenticationBusinessException, IdAuthenticationDaoException {
		AuthRequestDTO authReqDTO = new AuthRequestDTO();
		Mockito.when(authFacade.authenticateApplicant(authReqDTO, true,"123456","123456")).thenReturn(new AuthResponseDTO());
		authController.authenticateApplication(authReqDTO, error,"123456","123456");

	}

	@Test(expected = IdAuthenticationAppException.class)
	public void showProcessKycValidator()
			throws IdAuthenticationBusinessException, IdAuthenticationAppException, IdAuthenticationDaoException {
		KycAuthRequestDTO kycAuthReqDTO = new KycAuthRequestDTO();
		Errors errors = new BindException(kycAuthReqDTO, "kycAuthReqDTO");
		errors.rejectValue("id", "errorCode", "defaultMessage");
//		authFacade.authenticateApplicant(kycAuthReqDTO.getAuthRequest(), true);
		authController.processKyc(kycAuthReqDTO, errors,"123456","123456");
	}

	@Test
	public void processKycSuccess()
			throws IdAuthenticationBusinessException, IdAuthenticationAppException, IdAuthenticationDaoException {

//		KycAuthRequestDTO kycAuthRequestDTO = new KycAuthRequestDTO();
//		kycAuthRequestDTO.setConsentReq(true);
//		kycAuthRequestDTO.setEPrintReq(true);
//		kycAuthRequestDTO.setId("id");
//		// kycAuthRequestDTO.setVer("1.1");
//		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
//		authRequestDTO.setIdvIdType(IdType.UIN.getType());
//		authRequestDTO.setIdvId("234567890123");
//		ZoneOffset offset = ZoneOffset.MAX;
//		authRequestDTO.setReqTime(Instant.now().atOffset(offset)
//				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
//		authRequestDTO.setId("id");
//		// authRequestDTO.setVer("1.1");
//		authRequestDTO.setTspID("1234567890");
//		authRequestDTO.setTxnID("1234567890");
//		// authRequestDTO.setReqHmac("zdskfkdsnj");
//		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
//		authTypeDTO.setPersonalIdentity(true);
//		authTypeDTO.setOtp(true);
//		IdentityInfoDTO idInfoDTO = new IdentityInfoDTO();
//		idInfoDTO.setLanguage("EN");
//		idInfoDTO.setValue("John");
//		IdentityInfoDTO idInfoDTO1 = new IdentityInfoDTO();
//		idInfoDTO1.setLanguage("fre");
//		idInfoDTO1.setValue("Mike");
//		List<IdentityInfoDTO> idInfoList = new ArrayList<>();
//		idInfoList.add(idInfoDTO);
//		idInfoList.add(idInfoDTO1);
//		IdentityDTO idDTO = new IdentityDTO();
//		idDTO.setName(idInfoList);
//		RequestDTO reqDTO = new RequestDTO();
//		reqDTO.setIdentity(idDTO);
//		authRequestDTO.setAuthType(authTypeDTO);
//		authRequestDTO.setRequest(reqDTO);
//		kycAuthRequestDTO.setAuthRequest(authRequestDTO);
//		kycAuthRequestDTO.setEKycAuthType("O");
//		PinInfo pinInfo = new PinInfo();
//		pinInfo.setType("OTP");
//		pinInfo.setValue("123456");
//		List<PinInfo> otplist = new ArrayList<>();
//		otplist.add(pinInfo);
//		authRequestDTO.setPinInfo(otplist);
//		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
//		authResponseDTO.setErrors(null);
//		//authResponseDTO.setInfo(null);
//		authResponseDTO.setResponseTime("");
//		authResponseDTO.setStatus("Y");
//		authResponseDTO.setTransactionID("34567");
//		AuthRequestDTO authRequestDTOs = new AuthRequestDTO();
//		authRequestDTOs.setIdvIdType(IdType.UIN.getType());
//		authRequestDTOs.setId("1234567");
//		AuthTypeDTO authTypeDTOs = new AuthTypeDTO();
//		authTypeDTOs.setOtp(true);
//		authRequestDTOs.setAuthType(authTypeDTO);
//		kycAuthRequestDTO.setAuthRequest(authRequestDTO);
//		Mockito.when(authFacade.authenticateApplicant(kycAuthRequestDTO.getAuthRequest(), true))
//				.thenReturn(authResponseDTO);
//		KycAuthResponseDTO kycAuthResponseDTO = new KycAuthResponseDTO();
//		kycAuthResponseDTO.setResponseTime(Instant.now().atOffset(offset)
//				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
//		kycAuthResponseDTO.setStatus("Y");
//		kycAuthResponseDTO.setTxnID("34567");
//		kycAuthResponseDTO.setErrors(null);
//		KycResponseDTO response = new KycResponseDTO();
//		response.setAuth(null);
//		response.setKyc(null);
//		kycAuthResponseDTO.setResponse(response);
//		kycAuthResponseDTO.setTtl("2");
//		Mockito.when(authFacade.processKycAuth(kycAuthRequestDTO, authResponseDTO)).thenReturn(kycAuthResponseDTO);
//		authController.processKyc(kycAuthRequestDTO, errors);
//		assertFalse(error.hasErrors());
	}

	@Ignore
	@Test(expected = IdAuthenticationAppException.class)
	public void processKycFailure()
			throws IdAuthenticationBusinessException, IdAuthenticationAppException, IdAuthenticationDaoException {
		KycAuthRequestDTO kycAuthRequestDTO = new KycAuthRequestDTO();

//		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
//		authResponseDTO.setStatus("Y");
//		authResponseDTO.setResponseTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
//		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
//		authRequestDTO.setIdvIdType(IdType.UIN.getType());
//		authRequestDTO.setId("1234567");
//		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
//		authTypeDTO.setOtp(true);
//		authRequestDTO.setAuthType(authTypeDTO);
//		kycAuthRequestDTO.setAuthRequest(authRequestDTO);
//		Mockito.when(authFacade.authenticateApplicant(kycAuthRequestDTO.getAuthRequest(), true))
//				.thenReturn(authResponseDTO);
//		KycAuthResponseDTO kycAuthResponseDTO = new KycAuthResponseDTO();
//		kycAuthResponseDTO.setStatus("Y");
//		kycAuthResponseDTO.setTxnID("34567");
//		kycAuthResponseDTO.setErrors(null);
//		KycResponseDTO response = new KycResponseDTO();
//		response.setAuth(null);
//		response.setKyc(null);
//		kycAuthResponseDTO.setResponse(response);
//		kycAuthResponseDTO.setTtl("2");
//		Mockito.when(authFacade.processKycAuth(kycAuthRequestDTO, authResponseDTO))
//				.thenThrow(new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.AUTHENTICATION_FAILED));
//		authController.processKyc(kycAuthRequestDTO, errors);
	}

	@Ignore
	@Test(expected = IdAuthenticationAppException.class)
	public void showAuthenticateTspValidator() throws IdAuthenticationAppException, IdAuthenticationDaoException {

		AuthRequestDTO authReqestsDTO = new AuthRequestDTO();
		Errors error = new BindException(authReqestsDTO, "authReqDTO");
		error.rejectValue("id", "errorCode", "defaultMessage");
		// authController.authenticateTsp(authReqestsDTO, error);
	}

}
