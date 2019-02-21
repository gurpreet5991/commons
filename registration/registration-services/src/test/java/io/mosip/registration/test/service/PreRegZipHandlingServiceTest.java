package io.mosip.registration.test.service;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.jsonvalidator.exception.FileIOException;
import io.mosip.kernel.core.jsonvalidator.exception.JsonIOException;
import io.mosip.kernel.core.jsonvalidator.exception.JsonSchemaIOException;
import io.mosip.kernel.core.jsonvalidator.exception.JsonValidationProcessingException;
import io.mosip.kernel.core.jsonvalidator.model.ValidationReport;
import io.mosip.kernel.core.jsonvalidator.spi.JsonValidator;
import io.mosip.kernel.core.security.constants.MosipSecurityMethod;
import io.mosip.kernel.core.util.FileUtils;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.OSIDataDTO;
import io.mosip.registration.dto.PreRegistrationDTO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.RegistrationMetaDataDTO;
import io.mosip.registration.dto.biometric.BiometricDTO;
import io.mosip.registration.dto.biometric.BiometricInfoDTO;
import io.mosip.registration.dto.demographic.ApplicantDocumentDTO;
import io.mosip.registration.dto.demographic.DemographicDTO;
import io.mosip.registration.dto.demographic.DemographicInfoDTO;
import io.mosip.registration.dto.demographic.Identity;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.external.impl.PreRegZipHandlingServiceImpl;
import io.mosip.registration.util.kernal.RIDGenerator;

public class PreRegZipHandlingServiceTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Mock
	private KeyGenerator keyGenerator;

	@Mock
	private JsonValidator jsonValidator;

	@InjectMocks
	private PreRegZipHandlingServiceImpl preRegZipHandlingServiceImpl;

	static byte[] preRegPacket;

	static byte[] preRegPacketEncrypted;

	static MosipSecurityMethod mosipSecurityMethod;

	@BeforeClass
	public static void initialize() throws IOException, java.io.IOException {
		createRegistrationDTOObject();
		URL url = PreRegZipHandlingServiceTest.class.getResource("/preRegSample.zip");
		File packetZipFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		preRegPacket = FileUtils.readFileToByteArray(packetZipFile);

		mosipSecurityMethod = MosipSecurityMethod.AES_WITH_CBC_AND_PKCS7PADDING;

	}

	@Test
	public void extractPreRegZipFileTest() throws RegBaseCheckedException, IOException,
			JsonValidationProcessingException, JsonIOException, JsonSchemaIOException, FileIOException {
		Mockito.when(jsonValidator.validateJson(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(new ValidationReport());
		RegistrationDTO registrationDTO = preRegZipHandlingServiceImpl.extractPreRegZipFile(preRegPacket);

		assertNotNull(registrationDTO);

	}

	@Test(expected = RegBaseCheckedException.class)
	public void extractPreRegZipFileTestNegative() throws RegBaseCheckedException, IOException,
			JsonValidationProcessingException, JsonIOException, JsonSchemaIOException, FileIOException {
		Mockito.when(jsonValidator.validateJson(Mockito.anyString(), Mockito.anyString()))
				.thenThrow(new JsonValidationProcessingException("", ""));
		preRegZipHandlingServiceImpl.extractPreRegZipFile(preRegPacket);

	}

	@Test
	public void encryptAndSavePreRegPacketTest() throws RegBaseCheckedException {

		PreRegistrationDTO preRegistrationDTO = encryptPacket();
		assertNotNull(preRegistrationDTO);
	}

	private PreRegistrationDTO encryptPacket() throws RegBaseCheckedException {

		ReflectionTestUtils.setField(preRegZipHandlingServiceImpl, "preRegPacketLocation", "..//PreRegPacketStore");

		mockSecretKey();

		PreRegistrationDTO preRegistrationDTO = preRegZipHandlingServiceImpl
				.encryptAndSavePreRegPacket("89149679063970", preRegPacket);
		return preRegistrationDTO;
	}

	@Test
	public void decryptPreRegPacketTest() throws RegBaseCheckedException {

		final byte[] decrypted = preRegZipHandlingServiceImpl.decryptPreRegPacket("0E8BAAEB3CED73CBC9BF4964F321824A",
				encryptPacket().getEncryptedPacket());
		assertNotNull(decrypted);
	}
	//
	// @Test(expected = RegBaseCheckedException.class)
	// public void extractPreRegZipFileTestNegative() throws RegBaseCheckedException
	// {
	// byte[] packetValue = "sampleTestForNegativeCase".getBytes();
	// preRegZipHandlingServiceImpl.extractPreRegZipFile(packetValue);
	//
	// }

	@Test(expected = RegBaseUncheckedException.class)
	public void encryptAndSavePreRegPacketTestNegative() throws RegBaseCheckedException {
		mockSecretKey();
		preRegZipHandlingServiceImpl.encryptAndSavePreRegPacket("89149679063970", preRegPacket);
	}

	@Test(expected = RegBaseUncheckedException.class)
	public void extractPreRegZipFileNegative() throws RegBaseCheckedException {
		mockSecretKey();
		preRegZipHandlingServiceImpl.extractPreRegZipFile(null);
	}

	private void mockSecretKey() {
		byte[] decodedKey = Base64.getDecoder().decode("0E8BAAEB3CED73CBC9BF4964F321824A");
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		Mockito.when(keyGenerator.getSymmetricKey()).thenReturn(originalKey);
	}

	private static void createRegistrationDTOObject() {
		RegistrationDTO registrationDTO = new RegistrationDTO();

		// Set the RID
		registrationDTO.setRegistrationId(RIDGenerator.nextRID());

		// Create objects for Biometric DTOS
		BiometricDTO biometricDTO = new BiometricDTO();
		biometricDTO.setApplicantBiometricDTO(createBiometricInfoDTO());
		biometricDTO.setIntroducerBiometricDTO(createBiometricInfoDTO());
		biometricDTO.setOperatorBiometricDTO(createBiometricInfoDTO());
		biometricDTO.setSupervisorBiometricDTO(createBiometricInfoDTO());
		registrationDTO.setBiometricDTO(biometricDTO);

		// Create object for Demographic DTOS
		DemographicDTO demographicDTO = new DemographicDTO();
		ApplicantDocumentDTO applicantDocumentDTO = new ApplicantDocumentDTO();
		demographicDTO.setApplicantDocumentDTO(applicantDocumentDTO);
		
		applicantDocumentDTO.setDocuments(new  HashMap<>());

		DemographicInfoDTO demographicInfoDTOLocal = new DemographicInfoDTO();
		Identity identity = new Identity();
		demographicInfoDTOLocal.setIdentity(identity);

		demographicDTO.setDemographicInfoDTO(demographicInfoDTOLocal);
		registrationDTO.setDemographicDTO(demographicDTO);

		// Create object for OSIData DTO
		registrationDTO.setOsiDataDTO(new OSIDataDTO());

		// Create object for RegistrationMetaData DTO
		RegistrationMetaDataDTO registrationMetaDataDTO = new RegistrationMetaDataDTO();
		registrationMetaDataDTO.setRegistrationCategory("New");
		registrationDTO.setRegistrationMetaDataDTO(registrationMetaDataDTO);

		// Put the RegistrationDTO object to SessionContext Map
		SessionContext.getInstance().setMapObject(new HashMap<String, Object>());
		SessionContext.getInstance().getMapObject().put(RegistrationConstants.REGISTRATION_DATA, registrationDTO);
	}

	private static BiometricInfoDTO createBiometricInfoDTO() {
		BiometricInfoDTO biometricInfoDTO = new BiometricInfoDTO();
		biometricInfoDTO.setBiometricExceptionDTO(new ArrayList<>());
		biometricInfoDTO.setFingerprintDetailsDTO(new ArrayList<>());
		biometricInfoDTO.setIrisDetailsDTO(new ArrayList<>());
		return biometricInfoDTO;
	}
}
