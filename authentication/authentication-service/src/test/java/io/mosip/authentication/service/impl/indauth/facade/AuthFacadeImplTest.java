package io.mosip.authentication.service.impl.indauth.facade;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.authentication.core.dto.indauth.AuthRequestDTO;
import io.mosip.authentication.core.dto.indauth.AuthResponseDTO;
import io.mosip.authentication.core.dto.indauth.AuthStatusInfo;
import io.mosip.authentication.core.dto.indauth.AuthTypeDTO;
import io.mosip.authentication.core.dto.indauth.IdentityDTO;
import io.mosip.authentication.core.dto.indauth.IdentityInfoDTO;
import io.mosip.authentication.core.dto.indauth.RequestDTO;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.exception.IdAuthenticationDaoException;
import io.mosip.authentication.core.spi.id.service.IdAuthService;
import io.mosip.authentication.core.spi.id.service.IdRepoService;
import io.mosip.authentication.core.spi.indauth.service.BioAuthService;
import io.mosip.authentication.core.spi.indauth.service.DemoAuthService;
import io.mosip.authentication.core.spi.indauth.service.KycService;
import io.mosip.authentication.core.spi.indauth.service.OTPAuthService;
import io.mosip.authentication.core.spi.indauth.service.PinAuthService;
import io.mosip.authentication.service.config.IDAMappingConfig;
import io.mosip.authentication.service.entity.AutnTxn;
import io.mosip.authentication.service.factory.RestRequestFactory;
import io.mosip.authentication.service.helper.AuditHelper;
import io.mosip.authentication.service.helper.IdInfoHelper;
import io.mosip.authentication.service.helper.RestHelper;
import io.mosip.authentication.service.impl.indauth.builder.AuthStatusInfoBuilder;
import io.mosip.authentication.service.impl.indauth.service.KycServiceImpl;
import io.mosip.authentication.service.impl.indauth.service.demo.DemoMatchType;
import io.mosip.authentication.service.impl.notification.service.NotificationServiceImpl;
import io.mosip.authentication.service.integration.IdTemplateManager;
import io.mosip.authentication.service.integration.OTPManager;
import io.mosip.authentication.service.repository.AutnTxnRepository;
import io.mosip.kernel.core.idgenerator.spi.TokenIdGenerator;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;

/**
 * The class validates AuthFacadeImpl.
 *
 * @author Arun Bose
 * 
 * 
 * @author Prem Kumar
 */
@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class, TemplateManagerBuilderImpl.class })

public class AuthFacadeImplTest {

	private static final String STATUS_SUCCESS = "Y";
	/** The auth facade impl. */
	@InjectMocks
	private AuthFacadeImpl authFacadeImpl;
	@Mock
	private AuthFacadeImpl authFacadeMock;
	/** The env. */
	@Autowired
	private Environment env;

	@InjectMocks
	private KycServiceImpl kycServiceImpl;

	/** The otp auth service impl. */
	@Mock
	private OTPAuthService otpAuthServiceImpl;
	/** The IdAuthService */
	@Mock
	private IdAuthService<AutnTxn> idAuthService;
	/** The KycService **/
	@Mock
	private KycService kycService;
	/** The IdInfoHelper **/
	@Mock
	private IdInfoHelper idInfoHelper;
	/** The IdRepoService **/
	@Mock
	private IdAuthService idInfoService;
	/** The DemoAuthService **/
	@Mock
	private DemoAuthService demoAuthService;

	@InjectMocks
	private IdTemplateManager idTemplateManager;

	@Autowired
	private TemplateManagerBuilder templateManagerBuilder;

	@Mock
	private IDAMappingConfig idMappingConfig;

	@InjectMocks
	NotificationServiceImpl notificationService;

	@InjectMocks
	private RestRequestFactory restRequestFactory;

	@InjectMocks
	private RestHelper restHelper;

	@Mock
	private MessageSource messageSource;

	@InjectMocks
	private OTPManager otpManager;

	@Mock
	private BioAuthService bioAuthService;
	@Mock
	private AuditHelper auditHelper;
	@Mock
	private IdRepoService idRepoService;
	@Mock
	private AutnTxnRepository autntxnrepository;

	@Mock
	private PinAuthService pinAuthService;

	@Mock
	private TokenIdGenerator<String, String> tokenIdGenerator;

	/**
	 * Before.
	 */
	@Before
	public void before() {
		ReflectionTestUtils.setField(authFacadeImpl, "otpService", otpAuthServiceImpl);
		ReflectionTestUtils.setField(authFacadeImpl, "tokenIdGenerator", tokenIdGenerator);
		ReflectionTestUtils.setField(authFacadeImpl, "pinAuthService", pinAuthService);
		ReflectionTestUtils.setField(authFacadeImpl, "kycService", kycService);
		ReflectionTestUtils.setField(authFacadeImpl, "bioAuthService", bioAuthService);
		ReflectionTestUtils.setField(authFacadeImpl, "auditHelper", auditHelper);
		ReflectionTestUtils.setField(authFacadeImpl, "env", env);

		ReflectionTestUtils.setField(kycServiceImpl, "idInfoHelper", idInfoHelper);
//		ReflectionTestUtils.setField(kycServiceImpl, "idTemplateManager", idTemplateManager);
		ReflectionTestUtils.setField(kycServiceImpl, "env", env);
//		ReflectionTestUtils.setField(kycServiceImpl, "messageSource", messageSource);
		ReflectionTestUtils.setField(authFacadeImpl, "notificationService", notificationService);
		ReflectionTestUtils.setField(authFacadeImpl, "idInfoHelper", idInfoHelper);
	}

	/**
	 * This class tests the authenticateApplicant method where it checks the IdType
	 * and DemoAuthType.
	 *
	 * @throws IdAuthenticationBusinessException the id authentication business
	 *                                           exception
	 * @throws IdAuthenticationDaoException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */

	@Ignore
	@Test
	public void authenticateApplicantTest()
			throws IdAuthenticationBusinessException, IdAuthenticationDaoException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		authRequestDTO.setId("IDA");
		authRequestDTO.setTransactionID("1234567890");
		authRequestDTO.setPartnerID("64378643");
		authRequestDTO.setRequestTime(ZonedDateTime.now()
				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(false);
		authTypeDTO.setDemo(false);
		authTypeDTO.setOtp(true);
		authTypeDTO.setPin(false);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		Map<String, Object> idRepo = new HashMap<>();
		String uin = "274390482564";
		idRepo.put("uin", uin);
		idRepo.put("registrationId", "1234567890");
		AuthStatusInfo authStatusInfo = new AuthStatusInfo();
		authStatusInfo.setStatus(true);
		authStatusInfo.setErr(Collections.emptyList());
		List<IdentityInfoDTO> list = new ArrayList<IdentityInfoDTO>();
		list.add(new IdentityInfoDTO("en", "mosip"));
		Map<String, List<IdentityInfoDTO>> idInfo = new HashMap<>();
		idInfo.put("name", list);
		idInfo.put("email", list);
		idInfo.put("phone", list);
		Mockito.when(otpAuthServiceImpl.authenticate(authRequestDTO, uin, Collections.emptyMap()))
				.thenReturn(authStatusInfo);
		Mockito.when(idRepoService.getIdenity(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(idRepo);
		Mockito.when(idAuthService.processIdType(Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
				.thenReturn(idRepo);
		Mockito.when(idAuthService.getIdRepoByUIN(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(repoDetails());
		Mockito.when(idInfoService.getIdInfo(Mockito.any())).thenReturn(idInfo);
		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
		authResponseDTO.setStatus("y");

		authResponseDTO.setResponseTime(ZonedDateTime.now()
				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
		ReflectionTestUtils.setField(notificationService, "env", env);
		ReflectionTestUtils.setField(notificationService, "idTemplateManager", idTemplateManager);
		ReflectionTestUtils.setField(restRequestFactory, "env", env);
		ReflectionTestUtils.setField(idInfoHelper, "environment", env);
		ReflectionTestUtils.setField(idInfoHelper, "idMappingConfig", idMappingConfig);
		ReflectionTestUtils.setField(authFacadeImpl, "notificationService", notificationService);
		ReflectionTestUtils.setField(authFacadeImpl, "env", env);
		Mockito.when(idInfoService.getIdInfo(repoDetails())).thenReturn(idInfo);
		Mockito.when(idInfoHelper.getEntityInfoAsString(DemoMatchType.NAME, idInfo)).thenReturn("mosip");
		Mockito.when(idInfoHelper.getEntityInfoAsString(DemoMatchType.EMAIL, idInfo)).thenReturn("mosip");
		Mockito.when(idInfoHelper.getEntityInfoAsString(DemoMatchType.PHONE, idInfo)).thenReturn("mosip");
		Mockito.when(tokenIdGenerator.generateId(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("247334310780728918141754192454591343");
		try {
			Mockito.when(idInfoHelper.getUTCTime(Mockito.anyString())).thenReturn("2019-02-18T12:28:17.078");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		authFacadeImpl.authenticateApplicant(authRequestDTO, true);

	}

	/**
	 * This class tests the processAuthType (OTP) method where otp validation
	 * failed.
	 *
	 * @throws IdAuthenticationBusinessException the id authentication business
	 *                                           exception
	 */

	@Ignore
	@Test
	public void processAuthTypeTestFail() throws IdAuthenticationBusinessException {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setOtp(false);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("1234567");
		authRequestDTO.getRequest().getIdentity().setUin("457984792857");
		List<IdentityInfoDTO> list = new ArrayList<IdentityInfoDTO>();
		list.add(new IdentityInfoDTO("en", "mosip"));
		Map<String, List<IdentityInfoDTO>> idInfo = new HashMap<>();
		idInfo.put("name", list);
		idInfo.put("email", list);
		idInfo.put("phone", list);
		List<AuthStatusInfo> authStatusList = ReflectionTestUtils.invokeMethod(authFacadeImpl, "processAuthType",
				authRequestDTO, idInfo, "1233", true, "247334310780728918141754192454591343");

//		assertTrue(authStatusList.stream().noneMatch(
//				status -> status.getUsageDataBits().contains(AuthUsageDataBit.USED_OTP) || status.isStatus()));
	}

	/**
	 * This class tests the processAuthType (OTP) method where otp validation gets
	 * successful.
	 *
	 * @throws IdAuthenticationBusinessException the id authentication business
	 *                                           exception
	 */
	@Ignore

	@Test
	public void processAuthTypeTestSuccess() throws IdAuthenticationBusinessException {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		authRequestDTO.setId("1234567");
		authRequestDTO.getRequest().getIdentity().setUin("457984792857");
		authRequestDTO.setTransactionID("1234567890");
		authRequestDTO.setPartnerID("64378643");
		authRequestDTO.setRequestTime(ZonedDateTime.now()
				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setOtp(true);
		authTypeDTO.setBio(true);
		authTypeDTO.setDemo(true);
		IdentityInfoDTO idInfoDTO = new IdentityInfoDTO();
		idInfoDTO.setLanguage("EN");
		idInfoDTO.setValue("John");
		IdentityInfoDTO idInfoDTO1 = new IdentityInfoDTO();
		idInfoDTO1.setLanguage("fre");
		idInfoDTO1.setValue("Mike");
		List<IdentityInfoDTO> idInfoList = new ArrayList<>();
		idInfoList.add(idInfoDTO);
		idInfoList.add(idInfoDTO1);
		IdentityDTO idDTO = new IdentityDTO();
		idDTO.setName(idInfoList);
		RequestDTO reqDTO = new RequestDTO();
		reqDTO.setIdentity(idDTO);
		authRequestDTO.setId("1234567");
//		List<BioInfo> info = new ArrayList<>();
//		BioInfo bioInfo = new BioInfo();
//		bioInfo.setBioType("fgrMin");
//		DeviceInfo deviceInfo = new DeviceInfo();
//		deviceInfo.setDeviceId("42352");
//		deviceInfo.setMake("test");
//		deviceInfo.setModel("12165");
//		bioInfo.setDeviceInfo(deviceInfo);
//		BioInfo bioInfo1 = new BioInfo();
//		bioInfo1.setBioType("irisImg");
//		DeviceInfo deviceInfo1 = new DeviceInfo();
//		deviceInfo1.setDeviceId("42352");
//		deviceInfo1.setMake("test");
//		deviceInfo1.setModel("12165");
//		bioInfo1.setDeviceInfo(deviceInfo1);
//		BioInfo bioInfo2 = new BioInfo();
//		bioInfo2.setBioType("faceImg");
//		DeviceInfo deviceInfo2 = new DeviceInfo();
//		deviceInfo2.setDeviceId("42352");
//		deviceInfo2.setMake("test");
//		deviceInfo2.setModel("12165");
//		bioInfo2.setDeviceInfo(deviceInfo2);
//		BioInfo bioInfo3 = new BioInfo();
//		bioInfo3.setBioType("fgrImg");
//		DeviceInfo deviceInfo3 = new DeviceInfo();
//		deviceInfo3.setDeviceId("42352");
//		deviceInfo3.setMake("test");
//		deviceInfo3.setModel("12165");
//		bioInfo3.setDeviceInfo(deviceInfo3);
//
//		info.add(bioInfo);
//		info.add(bioInfo1);
//		info.add(bioInfo2);
//		info.add(bioInfo3);
//		authRequestDTO.setBioInfo(info);
		Mockito.when(otpAuthServiceImpl.authenticate(authRequestDTO, "1242", Collections.emptyMap()))
				.thenReturn(AuthStatusInfoBuilder.newInstance().setStatus(true).build());
		List<IdentityInfoDTO> list = new ArrayList<IdentityInfoDTO>();
		list.add(new IdentityInfoDTO("en", "mosip"));
		Map<String, List<IdentityInfoDTO>> idInfo = new HashMap<>();
		idInfo.put("name", list);
		idInfo.put("email", list);
		idInfo.put("phone", list);
		try {
			Mockito.when(idInfoHelper.getUTCTime(Mockito.anyString())).thenReturn("2019-02-18T12:28:17.078");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<AuthStatusInfo> authStatusList = ReflectionTestUtils.invokeMethod(authFacadeImpl, "processAuthType",
				authRequestDTO, idInfo, "1242", true, "247334310780728918141754192454591343");
		assertTrue(authStatusList.stream().anyMatch(status -> status.isStatus()));
	}

	@Test
	public void processAuthTypeTestFailure() throws IdAuthenticationBusinessException {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		authRequestDTO.setId("1234567");
//		authRequestDTO.setIdvId("457984792857");
//		authRequestDTO.setTxnID("1234567890");
//		authRequestDTO.setTspID("64378643");

//		authRequestDTO.setReqTime(ZonedDateTime.now()
//				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
//		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
//		authTypeDTO.setOtp(true);
//		authTypeDTO.setBio(true);
////		authTypeDTO.setPersonalIdentity(true);
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
////		authRequestDTO.setAuthType(authTypeDTO);
////		authRequestDTO.setIdvIdType(IdType.VID.getType());
//		authRequestDTO.setId("1234567");
////		authRequestDTO.setIdvId("457984792857");
////		List<BioInfo> info = new ArrayList<>();
////		BioInfo bioInfo = new BioInfo();
////		bioInfo.setBioType("fgrMin");
////		DeviceInfo deviceInfo = new DeviceInfo();
////		deviceInfo.setDeviceId("42352");
////		deviceInfo.setMake("test");
////		deviceInfo.setModel("12165");
////		BioInfo bioInfo4 = new BioInfo();
////		bioInfo4.setBioType("fgrImg");
////		DeviceInfo deviceInfo4 = new DeviceInfo();
////		deviceInfo4.setDeviceId("42352");
////		deviceInfo4.setMake("test");
////		deviceInfo4.setModel("12165");
////		bioInfo4.setDeviceInfo(deviceInfo4);
////		BioInfo bioInfo1 = new BioInfo();
////		bioInfo1.setBioType("irisImgs");
////		DeviceInfo deviceInfo1 = new DeviceInfo();
////		deviceInfo1.setDeviceId("42352");
////		deviceInfo1.setMake("test");
////		deviceInfo1.setModel("12165");
////		bioInfo1.setDeviceInfo(deviceInfo1);
////		BioInfo bioInfo2 = new BioInfo();
////		bioInfo2.setBioType("faceImgs");
////		DeviceInfo deviceInfo2 = new DeviceInfo();
////		deviceInfo2.setDeviceId("42352");
////		deviceInfo2.setMake("test");
////		deviceInfo2.setModel("12165");
////		bioInfo2.setDeviceInfo(deviceInfo2);
//
//		info.add(bioInfo);
//		info.add(bioInfo1);
//		info.add(bioInfo2);
//		info.add(bioInfo4);
//		authRequestDTO.setBioInfo(info);
//		Mockito.when(otpAuthServiceImpl.authenticate(authRequestDTO, "1242", Collections.emptyMap()))
//				.thenReturn(AuthStatusInfoBuilder.newInstance().setStatus(true)
//						.addAuthUsageDataBits(AuthUsageDataBit.USED_OTP).build());
//		List<IdentityInfoDTO> list = new ArrayList<IdentityInfoDTO>();
//		list.add(new IdentityInfoDTO("en", "mosip"));
//		Map<String, List<IdentityInfoDTO>> idInfo = new HashMap<>();
//		idInfo.put("name", list);
//		idInfo.put("email", list);
//		idInfo.put("phone", list);
//		try {
//			Mockito.when(idInfoHelper.getUTCTime(Mockito.anyString())).thenReturn("2019-02-18T12:28:17.078");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		List<AuthStatusInfo> authStatusList = ReflectionTestUtils.invokeMethod(authFacadeImpl, "processAuthType",
//				authRequestDTO, idInfo, "1242", false, "247334310780728918141754192454591343");
//		assertTrue(authStatusList.stream().anyMatch(
//				status -> status.getUsageDataBits().contains(AuthUsageDataBit.USED_OTP) && status.isStatus()));
	}

	@Test
	public void processKycAuthValid() throws IdAuthenticationBusinessException {
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
////		authRequestDTO.setReqHmac("zdskfkdsnj");
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
//		KycInfo info = new KycInfo();
//		info.setEPrint("y");
//		info.setIdvId("234567890123");
//		info.setIdentity(null);
//		String refId = "12343457";
//		Mockito.when(kycService.retrieveKycInfo(refId, KycType.LIMITED, kycAuthRequestDTO.isEPrintReq(),
//				kycAuthRequestDTO.isConsentReq(), null)).thenReturn(info);
//
//		KycAuthResponseDTO kycAuthResponseDTO = new KycAuthResponseDTO();
//		kycAuthResponseDTO.setResTime(Instant.now().atOffset(offset)
//				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
//		kycAuthResponseDTO.setStatus(STATUS_SUCCESS);
//		kycAuthResponseDTO.setTxnID("34567");
//		kycAuthResponseDTO.setErr(null);
//		KycResponseDTO response = new KycResponseDTO();
//		response.setAuth(null);
//		response.setKyc(null);
//		kycAuthResponseDTO.setResponse(response);
//		kycAuthResponseDTO.setTtl("2");
//		kycAuthResponseDTO.getResponse().setKyc(info);
//		kycAuthResponseDTO.setTtl(env.getProperty("ekyc.ttl.hours"));
//		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
//		authResponseDTO.setStatus(STATUS_SUCCESS);
//		authResponseDTO.setResTime(new SimpleDateFormat(env.getProperty("datetime.pattern")).format(new Date()));
//		authResponseDTO.setStatus("Y");
//		assertNotNull(authFacadeImpl.processKycAuth(kycAuthRequestDTO, authResponseDTO));

	}

	@Test
	public void processKycAuthInValid() throws IdAuthenticationBusinessException {
//		KycAuthRequestDTO kycAuthRequestDTO = new KycAuthRequestDTO();
//		kycAuthRequestDTO.setConsentReq(true);
//		kycAuthRequestDTO.setEPrintReq(true);
//		kycAuthRequestDTO.setId("id");
//		// kycAuthRequestDTO.setVer("1.1");
//		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
//		authRequestDTO.setIdvIdType(IdType.VID.getType());
//		authRequestDTO.setIdvId("234567890123");
//		ZoneOffset offset = ZoneOffset.MAX;
//		authRequestDTO.setReqTime(Instant.now().atOffset(offset)
//				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
//		authRequestDTO.setId("id");
//		// authRequestDTO.setVer("1.1");
//		authRequestDTO.setTspID("1234567890");
//		authRequestDTO.setTxnID("1234567890");
////		authRequestDTO.setReqHmac("zdskfkdsnj");
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
//		KycInfo info = new KycInfo();
//		info.setEPrint("y");
//		info.setIdvId("234567890123");
//		info.setIdentity(null);
//		String refId = "12343457";
//		Mockito.when(kycService.retrieveKycInfo(refId, KycType.LIMITED, kycAuthRequestDTO.isEPrintReq(),
//				kycAuthRequestDTO.isConsentReq(), null)).thenReturn(info);
//
//		KycAuthResponseDTO kycAuthResponseDTO = new KycAuthResponseDTO();
//		kycAuthResponseDTO.setResTime(Instant.now().atOffset(offset)
//				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
//		kycAuthResponseDTO.setStatus(STATUS_SUCCESS);
//		kycAuthResponseDTO.setTxnID("34567");
//		kycAuthResponseDTO.setErr(null);
//		KycResponseDTO response = new KycResponseDTO();
//		response.setAuth(null);
//		response.setKyc(null);
//		kycAuthResponseDTO.setResponse(response);
//		kycAuthResponseDTO.setTtl("2");
//		kycAuthResponseDTO.getResponse().setKyc(info);
//		kycAuthResponseDTO.setTtl(env.getProperty("ekyc.ttl.hours"));
//		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
//		authResponseDTO.setStatus(STATUS_SUCCESS);
//		authResponseDTO.setResTime(new SimpleDateFormat(env.getProperty("datetime.pattern")).format(new Date()));
//		Map<String, List<IdentityInfoDTO>> entityValue = new HashMap<>();
//		Mockito.when(idInfoService.getIdInfo(Mockito.any())).thenReturn(entityValue);
//		assertNotNull(authFacadeImpl.processKycAuth(kycAuthRequestDTO, authResponseDTO));

	}

	@Test
	public void processKycAuthRequestNull() throws IdAuthenticationBusinessException {
//		KycAuthRequestDTO kycAuthRequestDTO = new KycAuthRequestDTO();
//		ZoneOffset offset = ZoneOffset.MAX;
//		kycAuthRequestDTO.setEKycAuthType("O");
//		KycInfo info = new KycInfo();
//		info.setEPrint("y");
//		info.setIdvId("234567890123");
//		info.setIdentity(null);
//		String refId = "12343457";
//		Mockito.when(kycService.retrieveKycInfo(refId, KycType.LIMITED, kycAuthRequestDTO.isEPrintReq(),
//				kycAuthRequestDTO.isConsentReq(), null)).thenReturn(info);
//
//		KycAuthResponseDTO kycAuthResponseDTO = new KycAuthResponseDTO();
//		kycAuthResponseDTO.setResTime(Instant.now().atOffset(offset)
//				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
//		kycAuthResponseDTO.setStatus(STATUS_SUCCESS);
//		kycAuthResponseDTO.setTxnID("34567");
//		kycAuthResponseDTO.setErr(null);
//		KycResponseDTO response = new KycResponseDTO();
//		response.setAuth(null);
//		response.setKyc(null);
//		kycAuthResponseDTO.setResponse(response);
//		kycAuthResponseDTO.setTtl("2");
//		kycAuthResponseDTO.getResponse().setKyc(info);
//		kycAuthResponseDTO.setTtl(env.getProperty("ekyc.ttl.hours"));
//		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
//		authResponseDTO.setResTime(new SimpleDateFormat(env.getProperty("datetime.pattern")).format(new Date()));
//		assertNotNull(authFacadeImpl.processKycAuth(kycAuthRequestDTO, authResponseDTO));

	}

	@Test
	public void testGetAuditEvent() {
		ReflectionTestUtils.invokeMethod(authFacadeImpl, "getAuditEvent", true);
	}

	@Test
	public void testGetAuditEventInternal() {
		ReflectionTestUtils.invokeMethod(authFacadeImpl, "getAuditEvent", false);
	}

	@Test
	public void testProcessBioAuthType() {
//		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
//		authRequestDTO.setIdvIdType(IdType.VID.getType());
//		authRequestDTO.setId("1234567");
//		authRequestDTO.setIdvId("457984792857");
//		authRequestDTO.setTxnID("1234567890");
//		authRequestDTO.setTspID("64378643");
//		PinInfo pinInfo = new PinInfo();
//		pinInfo.setType(PinType.OTP.getType());
//		pinInfo.setValue("736643");
//		PinInfo pinInfo1 = new PinInfo();
//		pinInfo1.setType(PinType.PIN.getType());
//		pinInfo1.setValue("736643");
//		List<PinInfo> list1 = new ArrayList<>();
//		list1.add(pinInfo);
//		list1.add(pinInfo1);
//		authRequestDTO.setPinInfo(list1);
//		authRequestDTO.setReqTime(ZonedDateTime.now()
//				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
//		List<BioInfo> info = new ArrayList<>();
//		BioInfo bioInfo = new BioInfo();
//		bioInfo.setBioType("fgrMin");
//		DeviceInfo deviceInfo = new DeviceInfo();
//		deviceInfo.setDeviceId("42352");
//		deviceInfo.setMake("test");
//		deviceInfo.setModel("12165");
//		bioInfo.setDeviceInfo(deviceInfo);
//		BioInfo bioInfo1 = new BioInfo();
//		bioInfo1.setBioType("irisImg");
//		DeviceInfo deviceInfo1 = new DeviceInfo();
//		deviceInfo1.setDeviceId("42352");
//		deviceInfo1.setMake("test");
//		deviceInfo1.setModel("12165");
//		bioInfo1.setDeviceInfo(deviceInfo1);
//		BioInfo bioInfo2 = new BioInfo();
//		bioInfo2.setBioType("faceImg");
//		DeviceInfo deviceInfo2 = new DeviceInfo();
//		deviceInfo2.setDeviceId("42352");
//		deviceInfo2.setMake("test");
//		deviceInfo2.setModel("12165");
//		bioInfo2.setDeviceInfo(deviceInfo2);
//		BioInfo bioInfo4 = new BioInfo();
//		bioInfo4.setBioType("fgrImg");
//		DeviceInfo deviceInfo4 = new DeviceInfo();
//		deviceInfo4.setDeviceId("42352");
//		deviceInfo4.setMake("test");
//		deviceInfo4.setModel("12165");
//		bioInfo4.setDeviceInfo(deviceInfo4);
//
//		info.add(bioInfo);
//		info.add(bioInfo1);
//		info.add(bioInfo2);
//		info.add(bioInfo4);
//		authRequestDTO.setBioInfo(info);
//		boolean isAuth = true;
//		IdType idType = IdType.VID;
//		String uin = "1234567890";
//		try {
//			Mockito.when(idInfoHelper.getUTCTime(Mockito.anyString())).thenReturn("2019-02-18T12:28:17.078");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		ReflectionTestUtils.invokeMethod(authFacadeImpl, "saveAndAuditBioAuthTxn", authRequestDTO, isAuth, uin, idType,
//				true, "247334310780728918141754192454591343");
	}

	@Test
	public void testProcessBioAuthTypeFinImg() {
//		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
//		List<BioInfo> info = new ArrayList<>();
//		BioInfo bioInfo = new BioInfo();
//		bioInfo.setBioType("fgrMins");
//		DeviceInfo deviceInfo = new DeviceInfo();
//		deviceInfo.setDeviceId("42352");
//		deviceInfo.setMake("test");
//		deviceInfo.setModel("12165");
//		bioInfo.setDeviceInfo(deviceInfo);
//		BioInfo bioInfo1 = new BioInfo();
//		bioInfo1.setBioType("irisImgs");
//		DeviceInfo deviceInfo1 = new DeviceInfo();
//		deviceInfo1.setDeviceId("42352");
//		deviceInfo1.setMake("test");
//		deviceInfo1.setModel("12165");
//		bioInfo1.setDeviceInfo(deviceInfo1);
//		BioInfo bioInfo2 = new BioInfo();
//		bioInfo2.setBioType("faceImgs");
//		DeviceInfo deviceInfo2 = new DeviceInfo();
//		deviceInfo2.setDeviceId("42352");
//		deviceInfo2.setMake("test");
//		deviceInfo2.setModel("12165");
//		bioInfo2.setDeviceInfo(deviceInfo2);
//		BioInfo bioInfo3 = new BioInfo();
//		bioInfo3.setBioType("finImgs");
//		DeviceInfo deviceInfo3 = new DeviceInfo();
//		deviceInfo3.setDeviceId("42352");
//		deviceInfo3.setMake("test");
//		deviceInfo3.setModel("12165");
//		bioInfo3.setDeviceInfo(deviceInfo3);
//
//		info.add(bioInfo);
//		info.add(bioInfo1);
//		info.add(bioInfo2);
//		info.add(bioInfo3);
//		authRequestDTO.setBioInfo(info);
//		boolean isAuth = true;
//		IdType idType = IdType.VID;
//		String uin = "1234567890";
//		ReflectionTestUtils.invokeMethod(authFacadeImpl, "saveAndAuditBioAuthTxn", authRequestDTO, isAuth, uin, idType,
//				true, "247334310780728918141754192454591343");
	}

	@Test
	public void testProcessPinDetails_pinValidationStatusNull() throws IdAuthenticationBusinessException {
//		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
//		authRequestDTO.setId("mosip.identity.auth");
//		String uin = "794138547620";
//		authRequestDTO.setIdvIdType(IdType.UIN.getType());
//		authRequestDTO.setIdvId("284169042058");
//		authRequestDTO.setReqTime(Instant.now().atOffset(ZoneOffset.of("+0530")) // offset
//				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
//		authRequestDTO.setId("id");
//		authRequestDTO.setTspID("1234567890");
//		authRequestDTO.setTxnID("1234567890");
//		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
//		authTypeDTO.setPin(true);
//		authRequestDTO.setAuthType(authTypeDTO);
//		PinInfo info = new PinInfo();
//		info.setType("pin");
//		info.setValue("112233");
//		List<PinInfo> infoList = new ArrayList<PinInfo>();
//		infoList.add(info);
//		authRequestDTO.setPinInfo(infoList);
//		Map<String, Object> idRepo = new HashMap<>();
//		idRepo.put("uin", uin);
//		idRepo.put("registrationId", "1234567890");
//		List<IdentityInfoDTO> list = new ArrayList<IdentityInfoDTO>();
//		list.add(new IdentityInfoDTO("en", "mosip"));
//		try {
//			Mockito.when(idInfoHelper.getUTCTime(Mockito.anyString())).thenReturn("2019-02-18T12:28:17.078");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Map<String, List<IdentityInfoDTO>> idInfo = new HashMap<>();
//		idInfo.put("name", list);
//		idInfo.put("email", list);
//		idInfo.put("phone", list);
//		Mockito.when(idAuthService.processIdType(IdType.UIN.getType(), uin, false)).thenReturn(idRepo);
//		Mockito.when(idRepoService.getIdenity(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(idRepo);
//		Mockito.when(idAuthService.getIdRepoByUIN(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(repoDetails());
//
//		Mockito.when(idInfoService.getIdInfo(repoDetails())).thenReturn(idInfo);
//		List<AuthStatusInfo> authStatusList = new ArrayList<>();
//		AuthStatusInfo pinValidationStatus = null;
//		Mockito.when(pinAuthService.authenticate(authRequestDTO, uin, Collections.emptyMap()))
//				.thenReturn(pinValidationStatus);
//		ReflectionTestUtils.invokeMethod(authFacadeImpl, "processPinAuth", authRequestDTO, uin, true, authStatusList,
//				IdType.UIN, "247334310780728918141754192454591343");

	}

	@Test
	public void testProcessPinDetails_pinValidationStatusNotNull() throws IdAuthenticationBusinessException {
//		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
//		authRequestDTO.setId("mosip.identity.auth");
//		String uin = "794138547620";
//		authRequestDTO.setIdvIdType(IdType.UIN.getType());
//		authRequestDTO.setIdvId("284169042058");
//		authRequestDTO.setReqTime(Instant.now().atOffset(ZoneOffset.of("+0530")) // offset
//				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
//		authRequestDTO.setId("id");
//		authRequestDTO.setTspID("1234567890");
//		authRequestDTO.setTxnID("1234567890");
//		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
//		authTypeDTO.setPin(true);
//		authRequestDTO.setAuthType(authTypeDTO);
//		PinInfo info = new PinInfo();
//		info.setType("pin");
//		info.setValue("112233");
//		List<PinInfo> infoList = new ArrayList<PinInfo>();
//		infoList.add(info);
//		authRequestDTO.setPinInfo(infoList);
//		Map<String, Object> idRepo = new HashMap<>();
//		idRepo.put("uin", uin);
//		idRepo.put("registrationId", "1234567890");
//		try {
//			Mockito.when(idInfoHelper.getUTCTime(Mockito.anyString())).thenReturn("2019-02-18T12:28:17.078");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		List<IdentityInfoDTO> list = new ArrayList<IdentityInfoDTO>();
//		list.add(new IdentityInfoDTO("en", "mosip"));
//		Map<String, List<IdentityInfoDTO>> idInfo = new HashMap<>();
//		idInfo.put("name", list);
//		idInfo.put("email", list);
//		idInfo.put("phone", list);
//		Mockito.when(idAuthService.processIdType(IdType.UIN.getType(), uin, false)).thenReturn(idRepo);
//		Mockito.when(idRepoService.getIdenity(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(idRepo);
//		Mockito.when(idAuthService.getIdRepoByUIN(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(repoDetails());
//
//		Mockito.when(idInfoService.getIdInfo(repoDetails())).thenReturn(idInfo);
//		List<AuthStatusInfo> authStatusList = new ArrayList<>();
//		AuthStatusInfo pinValidationStatus = new AuthStatusInfo();
//		pinValidationStatus.setStatus(true);
//		pinValidationStatus.setErr(Collections.emptyList());
//		pinValidationStatus.setMatchInfos(Collections.emptyList());
//		pinValidationStatus.setUsageDataBits(Collections.emptyList());
//		Mockito.when(pinAuthService.authenticate(authRequestDTO, uin, Collections.emptyMap()))
//				.thenReturn(pinValidationStatus);
//		ReflectionTestUtils.invokeMethod(authFacadeImpl, "processPinAuth", authRequestDTO, uin, true, authStatusList,
//				IdType.UIN, "247334310780728918141754192454591343");

	}

	@Test
	public void testProcessPinDetails_pinValidationStatus_false() throws IdAuthenticationBusinessException {
//		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
//		authRequestDTO.setId("mosip.identity.auth");
//		String uin = "794138547620";
//		authRequestDTO.setIdvIdType(IdType.UIN.getType());
//		authRequestDTO.setIdvId("284169042058");
//		authRequestDTO.setReqTime(Instant.now().atOffset(ZoneOffset.of("+0530")) // offset
//				.format(DateTimeFormatter.ofPattern(env.getProperty("datetime.pattern"))).toString());
//		authRequestDTO.setId("id");
//		authRequestDTO.setTspID("1234567890");
//		authRequestDTO.setTxnID("1234567890");
//		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
//		authTypeDTO.setPin(true);
//		authRequestDTO.setAuthType(authTypeDTO);
//		PinInfo info = new PinInfo();
//		info.setType("pin");
//		info.setValue("112233");
//		List<PinInfo> infoList = new ArrayList<PinInfo>();
//		infoList.add(info);
//		authRequestDTO.setPinInfo(infoList);
//		Map<String, Object> idRepo = new HashMap<>();
//		idRepo.put("uin", uin);
//		idRepo.put("registrationId", "1234567890");
//		List<IdentityInfoDTO> list = new ArrayList<IdentityInfoDTO>();
//		list.add(new IdentityInfoDTO("en", "mosip"));
//		Map<String, List<IdentityInfoDTO>> idInfo = new HashMap<>();
//		idInfo.put("name", list);
//		idInfo.put("email", list);
//		idInfo.put("phone", list);
//		Mockito.when(idAuthService.processIdType(IdType.UIN.getType(), uin, false)).thenReturn(idRepo);
//		Mockito.when(idRepoService.getIdenity(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(idRepo);
//		Mockito.when(idAuthService.getIdRepoByUIN(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(repoDetails());
//
//		Mockito.when(idInfoService.getIdInfo(repoDetails())).thenReturn(idInfo);
//		try {
//			Mockito.when(idInfoHelper.getUTCTime(Mockito.anyString())).thenReturn("2019-02-18T12:28:17.078");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		List<AuthStatusInfo> authStatusList = new ArrayList<>();
//		AuthStatusInfo pinValidationStatus = new AuthStatusInfo();
//		pinValidationStatus.setStatus(false);
//		pinValidationStatus.setErr(Collections.emptyList());
//		pinValidationStatus.setMatchInfos(Collections.emptyList());
//		pinValidationStatus.setUsageDataBits(Collections.emptyList());
//		Mockito.when(pinAuthService.authenticate(authRequestDTO, uin, Collections.emptyMap()))
//				.thenReturn(pinValidationStatus);
//		ReflectionTestUtils.invokeMethod(authFacadeImpl, "processPinAuth", authRequestDTO, uin, true, authStatusList,
//				IdType.UIN, "247334310780728918141754192454591343");

	}

	private Map<String, Object> repoDetails() {
		Map<String, Object> map = new HashMap<>();
		map.put("uin", "863537");
		return map;
	}
}
