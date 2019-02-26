package io.mosip.authentication.service.impl.id.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
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
import org.springframework.beans.factory.annotation.Autowired;
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

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.constant.RequestType;
import io.mosip.authentication.core.dto.indauth.IdType;
import io.mosip.authentication.core.dto.otpgen.OtpRequestDTO;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.spi.id.service.IdAuthService;
import io.mosip.authentication.core.spi.id.service.IdRepoService;
import io.mosip.authentication.service.entity.AutnTxn;
import io.mosip.authentication.service.entity.VIDEntity;
import io.mosip.authentication.service.factory.AuditRequestFactory;
import io.mosip.authentication.service.factory.RestRequestFactory;
import io.mosip.authentication.service.helper.RestHelper;
import io.mosip.authentication.service.impl.id.service.impl.IdAuthServiceImpl;
import io.mosip.authentication.service.repository.AutnTxnRepository;
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
	
	@Mock
	AutnTxnRepository autntxnrepository;
	@Mock
	AutnTxn autnTxn;

	@Autowired
	Environment env;

	@Before
	public void before() {
		ReflectionTestUtils.setField(idAuthServiceImpl, "idRepoService", idRepoService);
		ReflectionTestUtils.setField(idAuthServiceImpl, "auditFactory", auditFactory);
		ReflectionTestUtils.setField(idAuthServiceImpl, "restFactory", restFactory);
		ReflectionTestUtils.setField(idAuthServiceImpl, "vidRepository", vidRepository);
		ReflectionTestUtils.setField(idAuthServiceImpl, "env", env);

		/*
		 * ReflectionTestUtils.setField(idAuthServiceImplMock, "idRepoService",
		 * idRepoService); ReflectionTestUtils.setField(idAuthServiceImplMock,
		 * "auditFactory", auditFactory);
		 * ReflectionTestUtils.setField(idAuthServiceImplMock, "restFactory",
		 * restFactory); ReflectionTestUtils.setField(idAuthServiceImplMock,
		 * "uinRepository", uinRepository);
		 * ReflectionTestUtils.setField(idAuthServiceImplMock, "vidRepository",
		 * vidRepository);testProcessIdType_IdTypeIsD
		 */
	}

	@Ignore
	@Test
	public void testGetIdRepoByUinNumber() throws IdAuthenticationBusinessException {

		ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "getIdRepoByUinNumber", Mockito.anyString(),
				Mockito.anyBoolean());

	}

	@Test
	public void testAuditData() {
		ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "auditData");
	}

	@Test(expected=IdAuthenticationBusinessException.class)
	public void testGetIdRepoByVidNumberVIDExpired() throws Throwable {
		try {
			ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "getIdRepoByVID", "232343234", false);
		} catch (UndeclaredThrowableException e) {
			throw e.getCause();
		}
	}

	@Ignore
	@Test
	public void testGetIdRepoByVidAsRequest_IsNotNull() throws IdAuthenticationBusinessException {
		Map<String, Object> idRepo = new HashMap<>();
		idRepo.put("uin", "476567");
		Mockito.when(idRepoService.getIdenity(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(idRepo);
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

		ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "processIdType", idvIdType, idvId, false);
	}

	@Test
	public void testProcessIdType_IdTypeIsV() throws IdAuthenticationBusinessException {
		String idvIdType = "V";
		String idvId = "875948796";
		Map<String, Object> idRepo = new HashMap<>();
		idRepo.put("uin", "476567");
		VIDEntity vidEntity =new VIDEntity(); 
		vidEntity.setExpiryDate(LocalDateTime.of(2100, 12, 31, 6, 45));
		vidEntity.setActive(true);
		vidEntity.setUin("476567");
		Optional<VIDEntity> optVID=Optional.of(vidEntity);
		Mockito.when(vidRepository.findUinByVid(Mockito.any())).thenReturn(optVID);
		Mockito.when(idRepoService.getIdenity(Mockito.any(),Mockito.anyBoolean())).thenReturn(idRepo);
		Map<String,Object> idResponseMap=	(Map<String,Object>)ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "processIdType", idvIdType, idvId, false);
		assertEquals("476567", idResponseMap.get("uin"));
	}

	@Ignore
	@Test(expected = IdAuthenticationBusinessException.class)
	public void processIdtypeVIDFailed() throws IdAuthenticationBusinessException {
		String idvIdType = "V";
		String idvId = "875948796";

		IdAuthenticationBusinessException idBusinessException = new IdAuthenticationBusinessException(
				IdAuthenticationErrorConstants.INVALID_VID);

		Mockito.when(idRepoService.getIdenity(Mockito.anyString(), Mockito.anyBoolean()))
				.thenThrow(idBusinessException);

		Mockito.when(idAuthService.getIdRepoByVID(Mockito.anyString(), Mockito.anyBoolean()))
				.thenThrow(idBusinessException);
		Mockito.when(idAuthServiceImpl.processIdType(idvIdType, idvId, false)).thenThrow(idBusinessException);

	}

	@Test(expected = IdAuthenticationBusinessException.class)
	public void processIdtypeUINFailed() throws IdAuthenticationBusinessException {
		String idvIdType = "D";
		String idvId = "875948796";

		IdAuthenticationBusinessException idBusinessException = new IdAuthenticationBusinessException(
				IdAuthenticationErrorConstants.INVALID_UIN);

		Mockito.when(idRepoService.getIdenity(Mockito.anyString(), Mockito.anyBoolean()))
				.thenThrow(idBusinessException);

		Mockito.when(idAuthService.getIdRepoByVID(Mockito.anyString(), Mockito.anyBoolean()))
				.thenThrow(idBusinessException);
		Mockito.when(idAuthServiceImpl.processIdType(idvIdType, idvId, false)).thenThrow(idBusinessException);

	}

	@Test
	public void testSaveAutnTxn() {
		OtpRequestDTO otpRequestDto = getOtpRequestDTO();
		String idvId = otpRequestDto.getIdvId();
		String idvIdType = otpRequestDto.getIdvIdType();
		String reqTime = otpRequestDto.getReqTime();
		String txnId = otpRequestDto.getTxnID();

		RequestType requestType = RequestType.OTP_AUTH;

		String uin = "8765";
		String status = "Y";
		String comment = "OTP_GENERATED";
		ReflectionTestUtils.invokeMethod(autntxnrepository, "saveAndFlush", autnTxn);
		ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "saveAutnTxn", autnTxn);
	}

	// =========================================================
	// ************ Helping Method *****************************
	// =========================================================
	private OtpRequestDTO getOtpRequestDTO() {
		OtpRequestDTO otpRequestDto = new OtpRequestDTO();
		otpRequestDto.setId("id");
		otpRequestDto.setTspID("2345678901234");
		otpRequestDto.setIdvIdType(IdType.UIN.getType());
		otpRequestDto.setReqTime(new SimpleDateFormat(env.getProperty("datetime.pattern")).format(new Date()));
		otpRequestDto.setTxnID("2345678901234");
		otpRequestDto.setIdvId("2345678901234");
		// otpRequestDto.setVer("1.0");

		return otpRequestDto;
	}
	
	
	@Test
	public void testGetIdInfo()
			throws IdAuthenticationBusinessException, JsonParseException, JsonMappingException, IOException {

		// String res =
		// "{\"id\":\"mosip.id.read\",\"timestamp\":\"2019-01-02T08:59:58.547\",\"registrationId\":\"1234234320000920181212010055\",\"status\":\"REGISTERED\",\"response\":{\"identity\":{\"identity\":{\"firstName\":[{\"language\":\"AR\",\"label\":\"الاسم
		// الاول\",\"value\":\"ابراهيم\"},{\"language\":\"FR\",\"label\":\"Prénom\",\"value\":\"Ibrahim\"}],\"middleName\":[{\"language\":\"AR\",\"label\":\"الاسم
		// الأوسط\",\"value\":\"بن\"},{\"language\":\"FR\",\"label\":\"deuxième
		// nom\",\"value\":\"Ibn\"}],\"lastName\":[{\"language\":\"AR\",\"label\":\"الكنية\",\"value\":\"علي\"},{\"language\":\"FR\",\"label\":\"nom
		// de
		// famille\",\"value\":\"Ali\"}],\"dateOfBirth\":[{\"language\":\"AR\",\"label\":\"تاريخ
		// الولادة\",\"value\":\"1955-04-16\"},{\"language\":\"FR\",\"label\":\"date de
		// naissance\",\"value\":\"1955-04-16\"}],\"dateOfBirthType\":[{\"language\":\"AR\",\"label\":\"تاريخ
		// الولادة\",\"value\":\"V\"},{\"language\":\"FR\",\"label\":\"date de
		// naissance\",\"value\":\"V\"}],\"gender\":[{\"language\":\"AR\",\"label\":\"جنس\",\"value\":\"M\"},{\"language\":\"FR\",\"label\":\"le
		// sexe\",\"value\":\"M\"}],\"addressLine1\":[{\"language\":\"AR\",\"label\":\"العنوان
		// السطر 1\",\"value\":\"عنوان العينة سطر
		// 1\"},{\"language\":\"FR\",\"label\":\"Adresse 1\",\"value\":\"exemple
		// d'adresse ligne
		// 1\"}],\"addressLine2\":[{\"language\":\"AR\",\"label\":\"العنوان السطر
		// 2\",\"value\":\"عنوان العينة سطر
		// 2\"},{\"language\":\"FR\",\"label\":\"Adresse 2\",\"value\":\"exemple
		// d'adresse ligne
		// 2\"}],\"addressLine3\":[{\"language\":\"AR\",\"label\":\"العنوان السطر
		// 3\",\"value\":\"عنوان العينة سطر
		// 3\"},{\"language\":\"FR\",\"label\":\"Adresse 3\",\"value\":\"exemple
		// d'adresse ligne
		// 3\"}],\"region\":[{\"language\":\"AR\",\"label\":\"رمنطقة\",\"value\":\"طنجة
		// - تطوان -
		// الحسيمة\"},{\"language\":\"FR\",\"label\":\"Région\",\"value\":\"Tanger-Tétouan-Al
		// Hoceima\"}],\"province\":[{\"language\":\"AR\",\"label\":\"المحافظة\",\"value\":\"فاس-مكناس\"},{\"language\":\"FR\",\"label\":\"province\",\"value\":\"Fès-Meknès\"}],\"city\":[{\"language\":\"AR\",\"label\":\"مدينة\",\"value\":\"فاس-الدار
		// البيضاء\"},{\"language\":\"FR\",\"label\":\"ville\",\"value\":\"Casablanca\"}],\"localAdministrativeAuthority\":[{\"language\":\"AR\",\"label\":\"الهيئة
		// الإدارية المحلية\",\"value\":\"طنجة - تطوان -
		// الحسيمة\"},{\"language\":\"FR\",\"label\":\"Autorité administrative
		// locale\",\"value\":\"Tanger-Tétouan-Al
		// Hoceima\"}],\"mobileNumber\":[{\"language\":\"AR\",\"label\":\"رقم الهاتف
		// المحمول\",\"value\":\"9655007862\"},{\"language\":\"FR\",\"label\":\"numéro
		// de
		// portable\",\"value\":\"9655007862\"}],\"face\":[{\"language\":\"AR\",\"label\":\"عنوان
		// الايميل\",\"value\":\"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\"}],\"emailId\":[{\"language\":\"AR\",\"label\":\"عنوان
		// الايميل\",\"value\":\"sample@samplamail.com\"},{\"language\":\"FR\",\"label\":\"identifiant
		// email\",\"value\":\"sample@samplamail.com\"}],\"CNEOrPINNumber\":[{\"language\":\"AR\",\"label\":\"رقم
		// CNE / PIN\",\"value\":\"AB453625\"},{\"language\":\"FR\",\"label\":\"Numéro
		// CNE /
		// PIN\",\"value\":\"AB453625\"}],\"parentOrGuardianName\":[{\"language\":\"AR\",\"label\":\"اسم
		// ولي الأمر / الوصي\",\"value\":\"سلمى\"},{\"language\":\"FR\",\"label\":\"Nom
		// du parent /
		// tuteur\",\"value\":\"salma\"}],\"parentOrGuardianRIDOrUIN\":[{\"language\":\"AR\",\"label\":\"الوالد
		// / الوصي RID /
		// UIN\",\"value\":\"123456789123\"},{\"language\":\"FR\",\"label\":\"parent /
		// tuteur RID /
		// UIN\",\"value\":\"123456789123\"}],\"leftEye\":[{\"language\":\"AR\",\"label\":\"العين
		// اليسرى\",\"value\":\"hashed_fileName.png\"},{\"language\":\"FR\",\"label\":\"oeil
		// gauche\",\"value\":\"hashed_fileName.png\"}],\"rightEye\":[{\"language\":\"AR\",\"label\":\"العين
		// اليمنى\",\"value\":\"hashed_fileName.png\"},{\"language\":\"FR\",\"label\":\"l'œil
		// droit\",\"value\":\"hashed_fileName.png\"}],\"leftIndex\":[{\"language\":\"FR\",\"value\":\"Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA\",\"label\":\"string\"}],\"rightIndex\":[{\"language\":\"FR\",\"value\":\"Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA\",\"label\":\"string\"}]}}}}";
		String res = "{\r\n" + "  \"id\": \"mosip.id.read\",\r\n" + "  \"version\": \"1.0\",\r\n"
				+ "  \"timestamp\": \"2019-01-18T05:13:22.710Z\",\r\n" + "  \"status\": \"ACTIVATED\",\r\n"
				+ "  \"response\": {\r\n" + "    \"identity\": {\r\n" + "      \"IDSchemaVersion\": 1,\r\n"
				+ "      \"UIN\": 201786049258,\r\n" + "      \"fullName\": [\r\n" + "        {\r\n"
				+ "          \"language\": \"ara\",\r\n" + "          \"value\": \"ابراهيم بن علي\"\r\n"
				+ "        },\r\n" + "        {\r\n" + "          \"language\": \"fre\",\r\n"
				+ "          \"value\": \"Ibrahim Ibn Ali\"\r\n" + "        }\r\n" + "      ],\r\n"
				+ "      \"dateOfBirth\": \"1955/04/15\",\r\n" + "      \"age\": 45,\r\n" + "      \"gender\": [\r\n"
				+ "        {\r\n" + "          \"language\": \"ara\",\r\n" + "          \"value\": \"الذكر\"\r\n"
				+ "        },\r\n" + "        {\r\n" + "          \"language\": \"fre\",\r\n"
				+ "          \"value\": \"mâle\"\r\n" + "        }\r\n" + "      ],\r\n"
				+ "      \"addressLine1\": [\r\n" + "        {\r\n" + "          \"language\": \"ara\",\r\n"
				+ "          \"value\": \"عنوان العينة سطر 1\"\r\n" + "        },\r\n" + "        {\r\n"
				+ "          \"language\": \"fre\",\r\n" + "          \"value\": \"exemple d'adresse ligne 1\"\r\n"
				+ "        }\r\n" + "      ],\r\n" + "      \"addressLine2\": [\r\n" + "        {\r\n"
				+ "          \"language\": \"ara\",\r\n" + "          \"value\": \"عنوان العينة سطر 2\"\r\n"
				+ "        },\r\n" + "        {\r\n" + "          \"language\": \"fre\",\r\n"
				+ "          \"value\": \"exemple d'adresse ligne 2\"\r\n" + "        }\r\n" + "      ],\r\n"
				+ "      \"addressLine3\": [\r\n" + "        {\r\n" + "          \"language\": \"ara\",\r\n"
				+ "          \"value\": \"عنوان العينة سطر 2\"\r\n" + "        },\r\n" + "        {\r\n"
				+ "          \"language\": \"fre\",\r\n" + "          \"value\": \"exemple d'adresse ligne 2\"\r\n"
				+ "        }\r\n" + "      ],\r\n" + "      \"region\": [\r\n" + "        {\r\n"
				+ "          \"language\": \"ara\",\r\n" + "          \"value\": \"طنجة - تطوان - الحسيمة\"\r\n"
				+ "        },\r\n" + "        {\r\n" + "          \"language\": \"fre\",\r\n"
				+ "          \"value\": \"Tanger-Tétouan-Al Hoceima\"\r\n" + "        }\r\n" + "      ],\r\n"
				+ "      \"province\": [\r\n" + "        {\r\n" + "          \"language\": \"ara\",\r\n"
				+ "          \"value\": \"فاس-مكناس\"\r\n" + "        },\r\n" + "        {\r\n"
				+ "          \"language\": \"fre\",\r\n" + "          \"value\": \"Fès-Meknès\"\r\n" + "        }\r\n"
				+ "      ],\r\n" + "      \"city\": [\r\n" + "        {\r\n" + "          \"language\": \"ara\",\r\n"
				+ "          \"value\": \"الدار البيضاء\"\r\n" + "        },\r\n" + "        {\r\n"
				+ "          \"language\": \"fre\",\r\n" + "          \"value\": \"Casablanca\"\r\n" + "        }\r\n"
				+ "      ],\r\n" + "      \"postalCode\": \"570004\",\r\n" + "      \"phone\": \"9876543210\",\r\n"
				+ "      \"email\": \"abc@xyz.com\",\r\n" + "      \"CNIENumber\": 6789545678909,\r\n"
				+ "      \"localAdministrativeAuthority\": [\r\n" + "        {\r\n"
				+ "          \"language\": \"ara\",\r\n" + "          \"value\": \"سلمى\"\r\n" + "        },\r\n"
				+ "        {\r\n" + "          \"language\": \"fre\",\r\n" + "          \"value\": \"salma\"\r\n"
				+ "        }\r\n" + "      ],\r\n" + "      \"parentOrGuardianRIDOrUIN\": 212124324784912,\r\n"
				+ "      \"parentOrGuardianName\": [\r\n" + "        {\r\n" + "          \"language\": \"ara\",\r\n"
				+ "          \"value\": \"سلمى\"\r\n" + "        },\r\n" + "        {\r\n"
				+ "          \"language\": \"fre\",\r\n" + "          \"value\": \"salma\"\r\n" + "        }\r\n"
				+ "      ],\r\n" + "      \"proofOfAddress\": {\r\n" + "        \"format\": \"pdf\",\r\n"
				+ "        \"type\": \"drivingLicense\",\r\n" + "        \"value\": \"fileReferenceID\"\r\n"
				+ "      },\r\n" + "      \"proofOfIdentity\": {\r\n" + "        \"format\": \"txt\",\r\n"
				+ "        \"type\": \"passport\",\r\n" + "        \"value\": \"fileReferenceID\"\r\n" + "      },\r\n"
				+ "      \"proofOfRelationship\": {\r\n" + "        \"format\": \"pdf\",\r\n"
				+ "        \"type\": \"passport\",\r\n" + "        \"value\": \"fileReferenceID\"\r\n" + "      },\r\n"
				+ "      \"proofOfDateOfBirth\": {\r\n" + "        \"format\": \"pdf\",\r\n"
				+ "        \"type\": \"passport\",\r\n" + "        \"value\": \"fileReferenceID\"\r\n" + "      },\r\n"
				+ "      \"individualBiometrics\": {\r\n" + "        \"format\": \"cbeff\",\r\n"
				+ "        \"version\": 1,\r\n" + "        \"value\": \"fileReferenceID\"\r\n" + "      },\r\n"
				+ "      \"parentOrGuardianBiometrics\": {\r\n" + "        \"format\": \"cbeff\",\r\n"
				+ "        \"version\": 1,\r\n" + "        \"value\": \"fileReferenceID\"\r\n" + "      }\r\n"
				+ "    },\r\n" + "    \"documents\": [\r\n" + "      {\r\n"
				+ "        \"category\": \"individualBiometrics\",\r\n"
				+ "        \"value\": \"ew0KCQkJImxlZnRJbmRleCI6IFt7DQoJCQkJInZhbHVlIjogIlJrMVNBQ0F5TUFBQUFBRmNBQUFCUEFGaUFNVUF4UUVBQUFBb05VQjlBTUYwVjRDQkFLQkJQRUMwQUw2OFpJQzRBS2pOWkVCaUFKdldYVUJQQU5QV05VRFNBSzdSVUlDMkFRSWZaRURKQVBNeFBFQnlBR3dQWFlDcEFSWVBaRUNmQUZqb1pFQ0dBRXY5WkVCRUFGbXRWMEJwQVVHTlhVQy9BVUVFU1VDVUFWSUVQRUMyQVZOeFBJQ2NBTFd1WklDdUFMbTNaRUNOQUpxeFEwQ1VBSTNHUTBDWEFQZ2hWMEJWQUtET1pFQmZBUHFIWFVCREFLZS9aSUI5QUczeFhVRFBBSWJaVUVCY0FHWWhaRUNJQVNnSFhZQkpBR0FuVjBEakFSNGpHMERLQVRxSklVQ0dBREdTWkVEU0FVWUdJVUF4QUQrblYwQ1hBSytvU1VCb0FMcjZRNENTQU91S1hVQ2lBSXZOWkVDOUFKelFaSUJOQUxiVFhVQkJBTDY4VjBDZUFIRFpaRUN3QUhQYVpFQlJBUHdIVUlCSEFIVzJYVURYQVJBVURVQzRBUzRIWkVEWEFTMENRMENZQURMNFpFQ3NBVXp1UEVCa0FDZ1JaQUFBIg0KCQkJfV0sDQoJCQkicmlnaHRJbmRleCI6IFt7DQoJCQkJInZhbHVlIjogIlJrMVNBQ0F5TUFBQUFBRmNBQUFCUEFGaUFNVUF4UUVBQUFBb05VQ3RBTXZsWklDUkFPbFhYWURCQVBCcVpFQ2tBS1BXWFlEWkFOOW9aRUJ6QU1YUlNZQzZBSk5jVjRETUFKZFpYVURwQU85elpJQnlBUUptWkVDREFKQzdaSURmQVE1NlhVQmVBUFpnVjBCaEFKd3lWMERWQVNzQlhVRUVBSi9SWkVCWkFJcTZVRUMrQVZWN1NVQ2pBT2hlWFVDSEFObmNWMERGQUs1Y1hVRFpBTkpmWFVCM0FOQlJTVUNkQUpyS1hVQ0pBSnZFWFVDeEFROTFYVUNqQVJqN1hZQ3VBSVhlVjRDVUFJQzRaSURSQVJyM1hVREZBSDVqVjBCb0FKRzFaRURjQVNXQ1VFRFdBSEZmWkVCbkFTcDRTVURoQVZkMUZFQ01BTHpXVjBES0FMeFlYVUN3QUtiZlYwRFNBT3BzWkVDQUFLODlWNENTQVFkcFpJQ3BBSlBXVjBEb0FPUmlYVUNtQUlhelYwQ3hBUnQ3WFVDREFScDBaRUQ5QU5OVFpJRGhBSXpTWklDU0FIU2paSUJYQVFweFVFQ2dBR2FJWFlDQkFVSi9aQUFBIg0KCQkJfV0NCgkJfQ0KCX0\"\r\n"
				+ "      }\r\n" + "    ]\r\n" + "  }\r\n" + "}";
		ObjectMapper mapper = new ObjectMapper();
		byte resByte[] = res.getBytes();
		String value = new String(resByte, "UTF-8");

		@SuppressWarnings("unchecked")
		Map<String, Object> idResponseDTO = mapper.readValue(value, Map.class);
		ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "getIdInfo", idResponseDTO);
	}

	@Test
	public void testGetIdInfo1()
			throws IdAuthenticationBusinessException, JsonParseException, JsonMappingException, IOException {

		String res = "{\r\n" + "	\"id\": \"mosip.id.read\",\r\n" + "	\"timestamp\": \"2019-01-02T09:10:05.506\",\r\n"
				+ "	\"registrationId\": \"1234234320000920181212010055\",\r\n" + "	\"status\": \"REGISTERED\",\r\n"
				+ "	\"response\": {}\r\n" + "}";

		ObjectMapper mapper = new ObjectMapper();
		byte resByte[] = res.getBytes();
		String value = new String(resByte, "UTF-8");

		@SuppressWarnings("unchecked")
		Map<String, Object> idResponseDTO = mapper.readValue(value, Map.class);
		ReflectionTestUtils.invokeMethod(idAuthServiceImpl, "getIdInfo", idResponseDTO);
	}
}
