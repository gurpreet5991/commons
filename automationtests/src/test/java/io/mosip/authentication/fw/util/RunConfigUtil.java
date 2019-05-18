
package io.mosip.authentication.fw.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;

import io.mosip.authentication.fw.dto.TokenIdDto;
import io.mosip.authentication.fw.dto.UinDto;
import io.mosip.authentication.fw.dto.UinStaticPinDto;
import io.mosip.authentication.fw.dto.VidDto;
import io.mosip.authentication.fw.dto.VidStaticPinDto;

/**
 * The class perform picking up UIN,VID,TokenID,PartnerID,LicenseKey,StaticPin
 * 
 * @author Vignesh
 *
 */
public class RunConfigUtil {
	
	/**
	 * The method get UIN property file path
	 * 
	 * @return string, property file path
	 */
	public static String getUinPropertyPath() {
		return "ida/" + RunConfig.getTestDataFolderName() + "/RunConfig/uin.properties";
	}
	/**
	 * The method get static pin UIN property path
	 * 
	 * @return string, property file path
	 */
	public static String getStaticPinUinPropertyPath() {
		return "ida/" + RunConfig.getTestDataFolderName() + "/RunConfig/uinStaticPin.properties";
	}
	/**
	 * The method return VID property file path
	 * 
	 * @return string, property file path
	 */
	public static String getVidPropertyPath() {
		return "ida/" + RunConfig.getTestDataFolderName() + "/RunConfig/vid.properties";
	}
	/**
	 * The method get static pin VID property file path
	 * 
	 * @return string, property file path
	 */
	public static String getStaticPinVidPropertyPath() {
		return "ida/" + RunConfig.getTestDataFolderName() + "/RunConfig/vidStaticPin.properties";
	}
	/**
	 * The method get tokenId property file path
	 * 
	 * @return string, property file path
	 */
	public static String getTokenIdPropertyPath() {
		return "ida/" + RunConfig.getTestDataFolderName() + "/RunConfig/static-tokenId.properties";
	}
	/**
	 * The method get partnerID and Misp License key value property file path
	 * 
	 * @return string, property file path
	 */
	public static String getPartnerIDMispLKPropertyPath() {
		return "ida/" + RunConfig.getTestDataFolderName() + "/RunConfig/parter-license-id.properties";
	}
	/**
	 * The method get partnerID and License key value for the key
	 * 
	 * @param key
	 * @return string, value of partner ID and License key
	 */
	public static String getPartnerIDMispLKValue(String key) {
		return IdaScriptsUtil.getPropertyFromRelativeFilePath(getPartnerIDMispLKPropertyPath()).get(key).toString();
	}
	/**
	 * The method get token ID for UIN and PartnerID
	 * 
	 * @param uin
	 * @param partnerID
	 * @return tokenID
	 */
	public static String getTokenId(String uin, String partnerID) {		
		getTokenIdPropertyValue(getTokenIdPropertyPath());
		if (TokenIdDto.getTokenId().containsKey(uin + "." + partnerID))
			return TokenIdDto.getTokenId().get(uin + "." + partnerID);
		else
			return "TOKENID:"+uin + "." + partnerID;
	}
	/**
	 * The method return random UIN from property file
	 * 
	 * @return Random UIN
	 */
	public static String getRandomUINKey() {
		getUinPropertyValue(getUinPropertyPath());
		int count = 1;
		while (count > 0) {
			Object[] randomKeys = UinDto.getUinData().keySet().toArray();
			Object key = randomKeys[new Random().nextInt(randomKeys.length)];
			if (UinDto.getUinData().get(key).toString().contains("valid")) {
				return key.toString();
			}
			count++;
		}
		return "NoUINFound";
	}
	/**
	 * The method get UIN number using keyword from property file
	 * 
	 * @param keyword
	 * @return UIN number
	 */
	public static String getUinNumber(String keyword) {
		if (keyword.contains("EVEN")) {
			int count = 1;
			while (count > 0) {
				String key = getRandomUINKey();
				String lastNumberAsString = key.substring(key.length() - 1, key.length());
				int lastNum = Integer.parseInt(lastNumberAsString);
				if (lastNum % 2 == 0)
					return key;
				else
					count++;
			}
		} else if (keyword.contains("ODD")) {
			int count = 1;
			while (count > 0) {
				String key = getRandomUINKey();
				String lastNumberAsString = key.substring(key.length() - 1, key.length());
				int lastNum = Integer.parseInt(lastNumberAsString);
				if (lastNum % 2 != 0)
					return key;
				else
					count++;
			}
		} else if (keyword.equals("$UIN$")) {
			String key = getRandomUINKey();
			return key;
		} else {
			keyword = keyword.replace("$", "");
			String keys[] = keyword.split(":");
			String keywrdToFind = keys[2];
			return getUINKey(keywrdToFind);
		}
		return "NoLoadedUINFound";
	}
	/**
	 * The method get static pin for UIN
	 * 
	 * @return static pin
	 */
	public static String getRandomStaticPinUINKey() {
		getStaticPinUinPropertyValue(getStaticPinUinPropertyPath());
		Object[] randomKeys = UinStaticPinDto.getUinStaticPin().keySet().toArray();
		Object key = randomKeys[new Random().nextInt(randomKeys.length)];
		return key.toString();
	}
	/**
	 * The method get static pin for VID
	 * 
	 * @return static pin
	 */
	public static String getRandomStaticPinVIDKey() {
		getStaticPinVidPropertyValue(getStaticPinVidPropertyPath());
		Object[] randomKeys = VidStaticPinDto.getVidStaticPin().keySet().toArray();
		Object key = randomKeys[new Random().nextInt(randomKeys.length)];
		return key.toString();
	}
	/**
	 * The method get random VID from property file
	 * 
	 * @return VID number
	 */
	public static String getRandomVidKey() {
		getVidPropertyValue(getVidPropertyPath());
		Object[] randomKeys = VidDto.getVid().keySet().toArray();
		Object key = randomKeys[new Random().nextInt(randomKeys.length)];
		return key.toString();
	}
	/**
	 * The method get VID for UIN
	 * 
	 * @param uin
	 * @return VID
	 */
	public static String getVidKey(String uin) {
		for (Entry<String, String> entry : VidDto.getVid().entrySet()) {
			if (entry.getValue().contains(uin))
				return entry.getKey();
		}
		return "NoLoadedVIDFound";
	}
	/**
	 * The method get UIN using keyword from property file
	 * 
	 * @param keywordToFind
	 * @return UIN
	 */
	private static String getUINKey(String keywordToFind) {
		getUinPropertyValue(getUinPropertyPath());
		for (Entry<String, String> entry : UinDto.getUinData().entrySet()) {
			if (entry.getValue().contains(keywordToFind))
				return entry.getKey();
		}
		return "NoLoadedUINFound";
	}
	/**
	 * The method get UIN property value from property file
	 * 
	 * @param path
	 */
	protected static void getUinPropertyValue(String path) {
		Properties prop = IdaScriptsUtil.getPropertyFromRelativeFilePath(path);
		Map<String, String> map = new HashMap<String, String>();
		for (String key : prop.stringPropertyNames()) {
			String value = prop.getProperty(key);
			map.put(key, value);
		}
		UinDto.setUinData(map);
	}
	/**
	 * The method get static pin for UIN property value
	 * 
	 * @param path
	 */
	public static void getStaticPinUinPropertyValue(String path) {
		Properties prop = IdaScriptsUtil.getPropertyFromRelativeFilePath(path);
		Map<String, String> map = new HashMap<String, String>();
		for (String key : prop.stringPropertyNames()) {
			String value = prop.getProperty(key);
			map.put(key, value);
		}
		UinStaticPinDto.setUinStaticPin(map);
	}
	/**
	 * The method get static pin VID property value
	 * 
	 * @param path
	 */
	public static void getStaticPinVidPropertyValue(String path) {
		Properties prop = IdaScriptsUtil.getPropertyFromRelativeFilePath(path);
		Map<String, String> map = new HashMap<String, String>();
		for (String key : prop.stringPropertyNames()) {
			String value = prop.getProperty(key);
			map.put(key, value);
		}
		VidStaticPinDto.setVidStaticPin(map);
	}
	/**
	 * The method get Vid property value 
	 * 
	 * @param path
	 */
	protected static void getVidPropertyValue(String path) {
		Properties prop = IdaScriptsUtil.getPropertyFromRelativeFilePath(path);
		Map<String, String> map = new HashMap<String, String>();
		for (String key : prop.stringPropertyNames()) {
			String value = prop.getProperty(key);
			map.put(key, value);
		}
		VidDto.setVid(map);
	}
	/**
	 * The methof get tokenID property value
	 * 
	 * @param path
	 */
	public static void getTokenIdPropertyValue(String path) {
		Properties prop = IdaScriptsUtil.getPropertyFromRelativeFilePath(path);
		Map<String, String> map = new HashMap<String, String>();
		for (String key : prop.stringPropertyNames()) {
			String value = prop.getProperty(key);
			map.put(key, value);
		}
		TokenIdDto.setTokenId(map);
	}
}

