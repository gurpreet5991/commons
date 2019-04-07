package io.mosip.authentication.service.impl.indauth.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.authentication.common.config.IDAMappingConfig;
import io.mosip.authentication.common.factory.BiometricProviderFactory;
import io.mosip.authentication.common.impl.indauth.service.IdInfoFetcherImpl;
import io.mosip.authentication.core.dto.indauth.AuthRequestDTO;
import io.mosip.authentication.core.dto.indauth.AuthStatusInfo;
import io.mosip.authentication.core.dto.indauth.AuthTypeDTO;
import io.mosip.authentication.core.dto.indauth.BioIdentityInfoDTO;
import io.mosip.authentication.core.dto.indauth.DataDTO;
import io.mosip.authentication.core.dto.indauth.IdType;
import io.mosip.authentication.core.dto.indauth.IdentityDTO;
import io.mosip.authentication.core.dto.indauth.IdentityInfoDTO;
import io.mosip.authentication.core.dto.indauth.RequestDTO;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.service.helper.IdInfoHelper;
import io.mosip.authentication.service.impl.face.CogentFaceProvider;
import io.mosip.authentication.service.impl.face.MorphoFaceProvider;
import io.mosip.authentication.service.impl.fingerauth.provider.impl.CogentFingerprintProvider;
import io.mosip.authentication.service.impl.fingerauth.provider.impl.MantraFingerprintProvider;
import io.mosip.authentication.service.impl.indauth.builder.MatchInputBuilder;
import io.mosip.authentication.service.impl.indauth.service.BioAuthServiceImpl;
import io.mosip.authentication.service.impl.iris.CogentIrisProvider;
import io.mosip.authentication.service.impl.iris.MorphoIrisProvider;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;

@RunWith(SpringRunner.class)
@WebMvcTest
@Import(IDAMappingConfig.class)
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
public class BioAuthServiceTest {

	@InjectMocks
	private BioAuthServiceImpl bioAuthServiceImpl;

	@InjectMocks
	private IdInfoHelper idInfoHelper;

	@InjectMocks
	private MatchInputBuilder matchInputBuilder;

	@InjectMocks
	private IdInfoFetcherImpl idInfoFetcherImpl;

	@InjectMocks
	private BiometricProviderFactory biometricProviderFactory;

	@InjectMocks
	private MantraFingerprintProvider mantraFingerprintProvider;

	@InjectMocks
	private CogentFingerprintProvider cogentFingerprintProvider;

	@InjectMocks
	private CogentIrisProvider cogentIrisProvider;

	@InjectMocks
	private MorphoIrisProvider morphoIrisProvider;

	@InjectMocks
	private CogentFaceProvider cogentFaceProvider;

	@InjectMocks
	private MorphoFaceProvider morphoFaceProvider;

	@Autowired
	Environment environment;

	@Autowired
	private IDAMappingConfig idMappingConfig;

	@Mock
	private CbeffUtil cbeffUtil;

	@Before
	public void before() {
		ReflectionTestUtils.setField(bioAuthServiceImpl, "idInfoHelper", idInfoHelper);
		ReflectionTestUtils.setField(bioAuthServiceImpl, "matchInputBuilder", matchInputBuilder);
		ReflectionTestUtils.setField(matchInputBuilder, "idInfoHelper", idInfoHelper);
		ReflectionTestUtils.setField(matchInputBuilder, "idInfoFetcher", idInfoFetcherImpl);
		ReflectionTestUtils.setField(matchInputBuilder, "environment", environment);
		ReflectionTestUtils.setField(idInfoHelper, "idInfoFetcher", idInfoFetcherImpl);
		ReflectionTestUtils.setField(idInfoHelper, "environment", environment);
		ReflectionTestUtils.setField(idInfoHelper, "idMappingConfig", idMappingConfig);
		ReflectionTestUtils.setField(idInfoFetcherImpl, "biometricProviderFactory", biometricProviderFactory);
		ReflectionTestUtils.setField(biometricProviderFactory, "mantraFingerprintProvider", mantraFingerprintProvider);
		ReflectionTestUtils.setField(biometricProviderFactory, "cogentFingerProvider", cogentFingerprintProvider);
		ReflectionTestUtils.setField(biometricProviderFactory, "cogentIrisProvider", cogentIrisProvider);
		ReflectionTestUtils.setField(biometricProviderFactory, "morphoIrisProvider", morphoIrisProvider);
		ReflectionTestUtils.setField(biometricProviderFactory, "cogentFaceProvider", cogentFaceProvider);
		ReflectionTestUtils.setField(biometricProviderFactory, "morphoFaceProvider", morphoFaceProvider);
		ReflectionTestUtils.setField(biometricProviderFactory, "environment", environment);
		ReflectionTestUtils.setField(cogentIrisProvider, "environment", environment);
		ReflectionTestUtils.setField(morphoIrisProvider, "environment", environment);
		ReflectionTestUtils.setField(cogentFaceProvider, "environment", environment);
		ReflectionTestUtils.setField(morphoFaceProvider, "environment", environment);
	}

	@Test(expected = IdAuthenticationBusinessException.class)
	public void TestInvalidateBioDetails() throws IdAuthenticationBusinessException {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		Map<String, List<IdentityInfoDTO>> bioIdentity = null;
		bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
	}

	@Test
	public void TestvalidateBioDetails() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setRequestHMAC("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		// authRequestDTO.setVer("1.0");

		List<BioIdentityInfoDTO> leftIndexList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO dataDTO = new DataDTO();
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		dataDTO.setBioType("FMR");
		dataDTO.setBioSubType("LEFT_INDEX");
		dataDTO.setDeviceProviderID("provider001");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		leftIndexList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(leftIndexList);
		authRequestDTO.setRequest(request);

		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		map.put("FINGER_Left IndexFinger_2", new SimpleEntry<>("leftIndex", identityList));
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FINGER_Left IndexFinger_2", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo validateBioDetails = bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
		assertTrue(validateBioDetails.isStatus());
	}

	@Test
	public void TestvalidateBioDetails_Iris() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setRequestHMAC("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		// authRequestDTO.setVer("1.0");

		List<BioIdentityInfoDTO> leftIndexList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO dataDTO = new DataDTO();
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		dataDTO.setBioType("IIR");
		dataDTO.setBioSubType("LEFT");
		dataDTO.setDeviceProviderID("morpho");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		leftIndexList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(leftIndexList);
		authRequestDTO.setRequest(request);

		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("IRIS_Left_9", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo validateBioDetails = bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
		assertTrue(validateBioDetails.isStatus());
	}

	@Test
	public void TestvalidateBioDetails_Multi_Iris() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setRequestHMAC("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		// authRequestDTO.setVer("1.0");

		List<BioIdentityInfoDTO> leftIndexList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO dataDTO = new DataDTO();
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		dataDTO.setBioType("IIR");
		dataDTO.setBioSubType("LEFT");
		dataDTO.setDeviceProviderID("morpho");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		leftIndexList.add(bioIdentityInfoDTO);

		bioIdentityInfoDTO = new BioIdentityInfoDTO();
		dataDTO = new DataDTO();
		dataDTO.setBioType("IIR");
		dataDTO.setBioSubType("RIGHT");
		dataDTO.setDeviceProviderID("morpho");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		leftIndexList.add(bioIdentityInfoDTO);

		request.setDemographics(identity);
		request.setBiometrics(leftIndexList);
		authRequestDTO.setRequest(request);

		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("IRIS_Left_9", value);
		cbeffValueMap.put("IRIS_Right_9", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo validateBioDetails = bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
		assertTrue(validateBioDetails.isStatus());
	}

	@Test
	public void TestValidateBioAuthDetails() throws Exception {

		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setTransactionID("1234567890");
		BioIdentityInfoDTO fingerValue = new BioIdentityInfoDTO();
		DataDTO dataDTOFinger = new DataDTO();
		dataDTOFinger.setBioValue(
				"Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT");
		dataDTOFinger.setBioSubType("LEFT_INDEX");
		dataDTOFinger.setBioType("FMR");
		dataDTOFinger.setDeviceProviderID("provider001");
		fingerValue.setData(dataDTOFinger);
		List<BioIdentityInfoDTO> leftIndexList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		DataDTO dataDTOFinger2 = new DataDTO();
		dataDTOFinger2.setBioValue(value);
		dataDTOFinger2.setBioSubType("LEFT_INDEX");
		dataDTOFinger2.setBioType("FMR");
		dataDTOFinger2.setDeviceProviderID("provider001");
		bioIdentityInfoDTO.setData(dataDTOFinger2);
		leftIndexList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(leftIndexList);
		authRequestDTO.setRequest(request);

		// Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FINGER_Left IndexFinger_2", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo validateBioDetails = bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
		System.err.println(validateBioDetails.isStatus());
		System.err.println(validateBioDetails.getErr());
	}

	@Test
	public void TestMatchImage() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		// authRequestDTO.setReqHmac("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		authRequestDTO.setVersion("1.0");
		List<BioIdentityInfoDTO> bioIdentityInfoDTOList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO bioInfo = new DataDTO();
		bioInfo.setBioType("FMR");
		bioInfo.setDeviceProviderID("provider001");
		// bioInfo.setDeviceInfo(deviceInfo);
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		bioInfo.setBioSubType("LEFT_INDEX");
		bioInfo.setBioValue(value);
		bioIdentityInfoDTO.setData(bioInfo);
		bioIdentityInfoDTOList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(bioIdentityInfoDTOList);
		authRequestDTO.setRequest(request);
		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		// bioIdentity.put("leftIndex", identityList);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		map.put("FINGER_Left IndexFinger_2", new SimpleEntry<>("leftIndex", identityList));
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FINGER_Left IndexFinger_2", value);
		cbeffValueMap.put("FINGER_Left ThumbFinger_2", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
	}

	@Test
	public void TestMatchFingerPrintMantra() throws Exception {

		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		authRequestDTO.setId("mosip.identity.auth");
		authRequestDTO.setIndividualId("274390482564");
		authRequestDTO.setIndividualIdType(IdType.UIN.getType());
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setRequestHMAC("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		authRequestDTO.setVersion("1.0");
		List<BioIdentityInfoDTO> bioIdentityInfoDTOList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO bioInfo = new DataDTO();
		bioInfo.setBioType("FMR");
		bioInfo.setDeviceProviderID("provider001");
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		bioInfo.setBioSubType("LEFT_INDEX");
		bioInfo.setBioValue(value);
		bioIdentityInfoDTO.setData(bioInfo);
		bioIdentityInfoDTOList.add(bioIdentityInfoDTO);
		RequestDTO request = new RequestDTO();
		request.setBiometrics(bioIdentityInfoDTOList);
		authRequestDTO.setRequest(request);
		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FINGER_Left IndexFinger_2", value);
		cbeffValueMap.put("FINGER_Left ThumbFinger_2", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
	}

	@Test
	public void TestMatchFingerPrintCogent() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		// authRequestDTO.setReqHmac("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		authRequestDTO.setVersion("1.0");
		List<BioIdentityInfoDTO> bioIdentityInfoDTOList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO bioInfo = new DataDTO();
		bioInfo.setBioType("FMR");
		bioInfo.setDeviceProviderID("provider001");
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		bioInfo.setBioSubType("LEFT_INDEX");
		bioInfo.setBioValue(value);
		bioIdentityInfoDTO.setData(bioInfo);
		bioIdentityInfoDTOList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(bioIdentityInfoDTOList);
		authRequestDTO.setRequest(request);
		// Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		map.put("FINGER_Left IndexFinger_2", new SimpleEntry<>("leftIndex", identityList));
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FINGER_Left IndexFinger_2", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
	}

	@Test
	public void TestvalidateBioDetailsMulti() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		// authRequestDTO.setReqHmac("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		authRequestDTO.setVersion("1.0");
		List<BioIdentityInfoDTO> bioIdentityInfoDTOList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO bioInfo = new DataDTO();
		bioInfo.setBioType("FMR");
		bioInfo.setDeviceProviderID("provider001");
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		bioInfo.setBioSubType("LEFT_INDEX");
		bioInfo.setBioValue(value);
		bioIdentityInfoDTO.setData(bioInfo);
		bioIdentityInfoDTOList.add(bioIdentityInfoDTO);
		DataDTO dataDTO = new DataDTO();
		/*
		 * dataDTO.setBioType("FMR"); dataDTO.setBioSubType("LEFT_INDEX");
		 * dataDTO.setBioValue(value); dataDTO.setDeviceProviderID("mantra");
		 * bioIdentityInfoDTO.setData(dataDTO);
		 * bioIdentityInfoDTOList.add(bioIdentityInfoDTO);
		 */
		bioIdentityInfoDTO = new BioIdentityInfoDTO();
		dataDTO = new DataDTO();
		dataDTO.setBioType("FMR");
		dataDTO.setBioSubType("LEFT_THUMB");
		dataDTO.setDeviceProviderID("mantra");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		bioIdentityInfoDTOList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(bioIdentityInfoDTOList);
		authRequestDTO.setRequest(request);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		IdentityInfoDTO identityInfoDTOList1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		identityInfoDTOList1.setLanguage("ara");
		identityInfoDTOList1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		List<IdentityInfoDTO> identityLists = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		identityLists.add(identityInfoDTOList1);

		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		map.put("FINGER_Left IndexFinger_2", new SimpleEntry<>("leftIndex", identityList));
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FINGER_Left IndexFinger_2", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo validateBioDetails = bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
		assertTrue(validateBioDetails.isStatus());
	}

	/*
	 * @Test public void TestIrisMatchMorpho() throws Exception { AuthRequestDTO
	 * authRequestDTO = new AuthRequestDTO(); AuthTypeDTO authTypeDTO = new
	 * AuthTypeDTO(); authTypeDTO.setBio(true);
	 * authRequestDTO.setAuthType(authTypeDTO);
	 * authRequestDTO.setId("mosip.identity.auth");
	 * authRequestDTO.setIdvId("516283648960"); authRequestDTO.setIdvIdType("D");
	 * authRequestDTO.setKey(new AuthSecureDTO()); List<MatchInfo> matchInfoList =
	 * new ArrayList<>(); MatchInfo matchInfo = new MatchInfo();
	 * matchInfo.setAuthType("bio");
	 * matchInfo.setMatchingStrategy(MatchingStrategyType.PARTIAL.getType());
	 * matchInfoList.add(matchInfo); authRequestDTO.setMatchInfo(matchInfoList);
	 * authRequestDTO.setTspID("1234567890"); ZoneOffset offset = ZoneOffset.MAX;
	 * authRequestDTO.setReqTime(Instant.now().atOffset(offset)
	 * .format(DateTimeFormatter.ofPattern(environment.getProperty(
	 * "datetime.pattern"))).toString()); //
	 * authRequestDTO.setReqHmac("1234567890");
	 * authRequestDTO.setTxnID("1234567890"); // authRequestDTO.setVer("1.0");
	 * List<BioInfo> bioInfoList = new ArrayList<>(); BioInfo bioInfo = new
	 * BioInfo(); bioInfo.setBioType("irisImg"); DeviceInfo deviceInfo = new
	 * DeviceInfo(); deviceInfo.setDeviceId("Test1"); deviceInfo.setMake("morpho");
	 * deviceInfo.setModel("1.0"); bioInfo.setDeviceInfo(deviceInfo);
	 * bioInfoList.add(bioInfo); authRequestDTO.setBioInfo(bioInfoList); RequestDTO
	 * requestDTO = new RequestDTO(); IdentityDTO identity = new IdentityDTO();
	 * List<IdentityInfoDTO> leftEyeList = new ArrayList<>(); IdentityInfoDTO
	 * identityInfoDTO = new IdentityInfoDTO(); String value =
	 * "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
	 * identityInfoDTO.setLanguage("ara"); identityInfoDTO.setValue(value);
	 * leftEyeList.add(identityInfoDTO); identity.setLeftEye(leftEyeList);
	 * requestDTO.setIdentity(identity); authRequestDTO.setRequest(requestDTO);
	 * Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
	 * IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
	 * identityInfoDTO1.setLanguage("ara"); identityInfoDTO1.setValue(value);
	 * List<IdentityInfoDTO> identityList = new ArrayList<>();
	 * identityList.add(identityInfoDTO1); bioIdentity.put("leftEye", identityList);
	 * bioIdentity.put("documents.individualBiometrics", identityList); String refId
	 * = "274390482564"; Map<String, String> cbeffValueMap = new HashMap<>();
	 * cbeffValueMap.put("IRIS_Left_9", value);
	 * Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(),
	 * Mockito.any())) .thenReturn(cbeffValueMap); AuthStatusInfo validateBioDetails
	 * = bioAuthServiceImpl.authenticate(authRequestDTO,"", bioIdentity);
	 * System.err.println(validateBioDetails.isStatus());
	 * System.err.println(validateBioDetails.getErr()); }
	 */

	@Test
	public void TestIrisMultiMatch() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setTransactionID("1234567890");
		authRequestDTO.setVersion("1.0");
		List<BioIdentityInfoDTO> bioIdentityInfoDTOList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO bioInfo = new DataDTO();
		bioInfo.setBioType("FMR");
		bioInfo.setDeviceProviderID("provider001");
		bioIdentityInfoDTO.setData(bioInfo);
		bioIdentityInfoDTOList.add(bioIdentityInfoDTO);
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		bioInfo.setBioSubType("LEFT_INDEX");
		bioInfo.setBioValue(value);
		bioIdentityInfoDTOList.add(bioIdentityInfoDTO);
		value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		String value1 = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		DataDTO dataDTO = new DataDTO();
		dataDTO.setBioType("FMR");
		dataDTO.setBioSubType("LEFT_INDEX");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		bioIdentityInfoDTOList.add(bioIdentityInfoDTO);
		dataDTO.setBioType("FMR");
		dataDTO.setBioSubType("LEFT_THUMB");
		dataDTO.setBioValue(value1);
		bioIdentityInfoDTO.setData(dataDTO);
		bioIdentityInfoDTOList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(bioIdentityInfoDTOList);

		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		bioIdentity.put("leftEye", identityList);
		bioIdentity.put("rightEye", identityList);
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, String> cbeffValueMap = new HashMap<>();
		cbeffValueMap.put("IRIS_Left_9", value);
		cbeffValueMap.put("IRIS_Right_9", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo validateBioDetails = bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
		System.err.println(validateBioDetails.isStatus());
		System.err.println(validateBioDetails.getErr());
	}

	@Test
	public void TestValidBioAuth() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		String individualId = "274390482564";
		authRequestDTO.setIndividualId(individualId);
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		authRequestDTO.setIndividualIdType(IdType.UIN.getType());
		AuthTypeDTO requestedAuth = new AuthTypeDTO();
		requestedAuth.setBio(true);
		authRequestDTO.setRequestedAuth(requestedAuth);
		RequestDTO request = new RequestDTO();
		List<BioIdentityInfoDTO> biometricsList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO datadto = new DataDTO();
		datadto.setBioType("FMR");
		datadto.setDeviceServiceID("1234");
		datadto.setDeviceProviderID("mantra");
		datadto.setBioValue(value);
		datadto.setBioSubType("LEFT_INDEX");
		bioIdentityInfoDTO.setData(datadto);
		biometricsList.add(bioIdentityInfoDTO);
		request.setBiometrics(biometricsList);
		authRequestDTO.setRequest(request);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		IdentityInfoDTO identityInfoDTOList1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		identityInfoDTOList1.setLanguage("ara");
		String value1 = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		identityInfoDTOList1.setValue(value1);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		List<IdentityInfoDTO> identityLists = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		identityLists.add(identityInfoDTOList1);
		bioIdentity.put("leftIndex", identityList);
		bioIdentity.put("rightIndex", identityLists);
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		map.put("FINGER_Left IndexFinger_2", new SimpleEntry<>("leftIndex", identityList));
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FINGER_Left IndexFinger_2", value);
		cbeffValueMap.put("FINGER_Right IndexFinger_2", value1);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo authenticate = bioAuthServiceImpl.authenticate(authRequestDTO, individualId, bioIdentity,
				"1234567890");
		assertTrue(authenticate.isStatus());
	}

	@Test
	public void TestvalidateBioDetails2() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setRequestHMAC("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		// authRequestDTO.setVer("1.0");

		List<BioIdentityInfoDTO> leftIndexList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO dataDTO = new DataDTO();
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		dataDTO.setBioType("FIR");
		dataDTO.setBioSubType("LEFT_INDEX");
		dataDTO.setDeviceProviderID("provider001");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		leftIndexList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(leftIndexList);
		authRequestDTO.setRequest(request);

		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		map.put("FINGER_Left IndexFinger_7", new SimpleEntry<>("leftIndex", identityList));
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FINGER_Left IndexFinger_7", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
	}

	@Test
	public void TestvalidateBioDetails_IrisUnknown() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setRequestHMAC("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		// authRequestDTO.setVer("1.0");

		List<BioIdentityInfoDTO> leftIndexList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO dataDTO = new DataDTO();
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		dataDTO.setBioType("IIR");
		dataDTO.setBioSubType("UNKNOWN");
		dataDTO.setDeviceProviderID("morpho");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		leftIndexList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(leftIndexList);
		authRequestDTO.setRequest(request);

		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("IRIS_Left_9", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo validateBioDetails = bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
		assertTrue(validateBioDetails.isStatus());
	}

	@Test
	public void TestvalidateBioDetails_FGRUnknown() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setRequestHMAC("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		// authRequestDTO.setVer("1.0");

		List<BioIdentityInfoDTO> leftIndexList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO dataDTO = new DataDTO();
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		dataDTO.setBioType("FMR");
		dataDTO.setBioSubType("UNKNOWN");
		dataDTO.setDeviceProviderID("provider001");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		leftIndexList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(leftIndexList);
		authRequestDTO.setRequest(request);

		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage("ara");
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		map.put("FINGER_Left IndexFinger_2", new SimpleEntry<>("leftIndex", identityList));
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FINGER_Left IndexFinger_2", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo validateBioDetails = bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
		assertTrue(validateBioDetails.isStatus());
	}

	@Test
	public void TestValidFaceAuthentication() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setRequestHMAC("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		List<BioIdentityInfoDTO> faceList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO dataDTO = new DataDTO();
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		dataDTO.setBioType("FID");
		dataDTO.setDeviceProviderID("provider001");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		faceList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(faceList);
		authRequestDTO.setRequest(request);

		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage(null);
		identityInfoDTO1.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		map.put("FACE__8", new SimpleEntry<>("face", identityList));
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FACE__8", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo validateBioDetails = bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
		assertTrue(validateBioDetails.isStatus());
	}

	@Test(expected = IdAuthenticationBusinessException.class)
	public void TestInvalidFaceDetails() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("274390482564");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setRequestHMAC("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		List<BioIdentityInfoDTO> faceList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO dataDTO = new DataDTO();
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		dataDTO.setBioType("FID");
		dataDTO.setDeviceProviderID("provider001");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		faceList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(faceList);
		authRequestDTO.setRequest(request);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", null);
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		map.put("FACE__8", new SimpleEntry<>("face", null));
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FACE__8", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo validateBioDetails = bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
		assertFalse(validateBioDetails.isStatus());
	}

	@Test
	public void TestFaceDetailsNotMatched() throws Exception {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		IdentityDTO identity = new IdentityDTO();
		RequestDTO request = new RequestDTO();
		request.setDemographics(identity);
		authRequestDTO.setIndividualId("2812936903");
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(true);
		authRequestDTO.setRequestedAuth(authTypeDTO);
		authRequestDTO.setId("mosip.identity.auth");
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setRequestTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authRequestDTO.setRequestHMAC("1234567890");
		authRequestDTO.setTransactionID("1234567890");
		List<BioIdentityInfoDTO> faceList = new ArrayList<>();
		BioIdentityInfoDTO bioIdentityInfoDTO = new BioIdentityInfoDTO();
		DataDTO dataDTO = new DataDTO();
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		dataDTO.setBioType("FID");
		dataDTO.setDeviceProviderID("provider001");
		dataDTO.setBioValue(value);
		bioIdentityInfoDTO.setData(dataDTO);
		faceList.add(bioIdentityInfoDTO);
		request.setDemographics(identity);
		request.setBiometrics(faceList);
		authRequestDTO.setRequest(request);
		IdentityInfoDTO identityInfoDTO1 = new IdentityInfoDTO();
		identityInfoDTO1.setLanguage(null);
		identityInfoDTO1.setValue(value + "dGVzdA==");
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO1);
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		map.put("FACE__8", new SimpleEntry<>("face", null));
		Map<String, String> cbeffValueMap = new HashMap<String, String>();
		cbeffValueMap.put("FACE__8", value);
		Mockito.when(cbeffUtil.getBDBBasedOnType(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(cbeffValueMap);
		AuthStatusInfo validateBioDetails = bioAuthServiceImpl.authenticate(authRequestDTO, "", bioIdentity, "");
		assertFalse(validateBioDetails.isStatus());
	}

}
