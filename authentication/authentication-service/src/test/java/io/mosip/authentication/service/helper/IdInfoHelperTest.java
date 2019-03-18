package io.mosip.authentication.service.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.AbstractMap.SimpleEntry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.deser.DataFormatReaders.Match;

import io.mosip.authentication.core.dto.indauth.AuthRequestDTO;
import io.mosip.authentication.core.dto.indauth.IdType;
import io.mosip.authentication.core.dto.indauth.IdentityInfoDTO;
import io.mosip.authentication.core.dto.indauth.LanguageType;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.spi.indauth.match.IdMapping;
import io.mosip.authentication.core.spi.indauth.match.MatchType;
import io.mosip.authentication.service.impl.indauth.service.bio.BioMatchType;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class IdInfoHelperTest {

	@InjectMocks
	IdInfoHelper idInfoHelper;

	@Autowired
	private Environment environment;

	@Before
	public void before() {
		ReflectionTestUtils.setField(idInfoHelper, "environment", environment);
	}

	@Test
	public void TestgetLanguageName() {
		String langCode = "ara";
		MockEnvironment mockenv = new MockEnvironment();
		mockenv.merge(((AbstractEnvironment) environment));
		mockenv.setProperty("mosip.phonetic.lang.".concat(langCode.toLowerCase()), "arabic-ar");
		mockenv.setProperty("mosip.phonetic.lang.ar", "arabic-ar");
		ReflectionTestUtils.setField(idInfoHelper, "environment", mockenv);
		Optional<String> languageName = idInfoHelper.getLanguageName(langCode);
		String value = languageName.get();
		assertEquals("arabic", value);
	}

	@Test
	public void TestgetLanguageCode() {
		String priLangCode = "mosip.primary.lang-code";
		String secLangCode = "mosip.secondary.lang-code";
		MockEnvironment mockenv = new MockEnvironment();
		mockenv.merge(((AbstractEnvironment) environment));
		mockenv.setProperty(priLangCode, "ara");
		mockenv.setProperty(secLangCode, "fra");
		String languageCode = idInfoHelper.getLanguageCode(LanguageType.PRIMARY_LANG);
		assertEquals("ara", languageCode);
		String languageCode2 = idInfoHelper.getLanguageCode(LanguageType.SECONDARY_LANG);
		assertEquals("fra", languageCode2);
	}

	@Test
	public void TestValidgetIdentityValuefromMap() {
		List<IdentityInfoDTO> identityList = getValueList();
		Map<String, List<IdentityInfoDTO>> bioIdentity = new HashMap<>();
		String key = "FINGER_Left IndexFinger_2";
		bioIdentity.put("documents.individualBiometrics", identityList);
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		map.put("FINGER_Left IndexFinger_2", new SimpleEntry<>("leftIndex", identityList));
		ReflectionTestUtils.invokeMethod(idInfoHelper, "getIdentityValueFromMap", key, "ara", map);
	}

	@Test
	public void TestInvalidtIdentityValuefromMap() {
		String language = "ara";
		String key = "FINGER_Left IndexFinger_2";
		Map<String, Entry<String, List<IdentityInfoDTO>>> map = new HashMap<>();
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		map.put("FINGER_Left IndexFinger_2", new SimpleEntry<>("leftIndex", identityList));
		ReflectionTestUtils.invokeMethod(idInfoHelper, "getIdentityValueFromMap", key, language, map);
	}

	@Test
	public void TestgetIdentityValue() {
		List<IdentityInfoDTO> identityInfoList = getValueList();
		String language = "ara";
		String key = "FINGER_Left IndexFinger_2";
		Map<String, List<IdentityInfoDTO>> demoInfo = new HashMap<>();
		demoInfo.put(key, identityInfoList);
		ReflectionTestUtils.invokeMethod(idInfoHelper, "getIdentityValue", key, language, demoInfo);
	}

	@Test
	public void TestInvalidIdentityValue() {
		String key = "FINGER_Left IndexFinger_2";
		List<IdentityInfoDTO> identityInfoList = getValueList();
		Map<String, List<IdentityInfoDTO>> demoInfo = new HashMap<>();
		demoInfo.put(key, identityInfoList);
		ReflectionTestUtils.invokeMethod(idInfoHelper, "getIdentityValue", key, "ara", demoInfo);
	}

	@Test
	public void checkLanguageType() {
		ReflectionTestUtils.invokeMethod(idInfoHelper, "checkLanguageType", null, null);
	}

	@Test
	public void TestgetUinType() {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.UIN.getType());
		IdType uinType = idInfoHelper.getUinOrVidType(authRequestDTO);
		assertEquals(IdType.UIN, uinType);
	}

	@Test
	public void TestgetVidType() {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.VID.getType());
		IdType uinType = idInfoHelper.getUinOrVidType(authRequestDTO);
		assertEquals(IdType.VID, uinType);
	}

	@Test
	public void TestgetUinorVid() {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		authRequestDTO.setIndividualId("274390482564");
		Optional<String> uinOrVid = idInfoHelper.getUinOrVid(authRequestDTO);
		assertNotEquals(Optional.empty(), uinOrVid.get());
	}

	@Test
	public void TestgetUtcTime() {
		String utcTime = idInfoHelper.getUTCTime(Instant.now().toString());
		assertNotNull(utcTime);
	}

	private List<IdentityInfoDTO> getValueList() {
		String value = "Rk1SACAyMAAAAAEIAAABPAFiAMUAxQEAAAAoJ4CEAOs8UICiAQGXUIBzANXIV4CmARiXUEC6AObFZIB3ALUSZEBlATPYZICIAKUCZEBmAJ4YZEAnAOvBZIDOAKTjZEBCAUbQQ0ARANu0ZECRAOC4NYBnAPDUXYCtANzIXUBhAQ7bZIBTAQvQZICtASqWZEDSAPnMZICaAUAVZEDNAS63Q0CEAVZiSUDUAT+oNYBhAVprSUAmAJyvZICiAOeyQ0CLANDSPECgAMzXQ0CKAR8OV0DEAN/QZEBNAMy9ZECaAKfwZEC9ATieUEDaAMfWUEDJAUA2NYB5AVttSUBKAI+oZECLAG0FZAAA";
		IdentityInfoDTO identityInfoDTO = new IdentityInfoDTO();
		String language = "ara";
		identityInfoDTO.setLanguage(language);
		identityInfoDTO.setValue(value);
		List<IdentityInfoDTO> identityList = new ArrayList<>();
		identityList.add(identityInfoDTO);
		return identityList;
	}

}
