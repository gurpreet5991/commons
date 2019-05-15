package io.mosip.admin.masterdata.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.admin.masterdata.config.MasterDataCardProperties;

@Component
public class MasterDataCardUtil {

	private Map<String, Map<String, String>> cardMap;

	@Autowired
	public MasterDataCardUtil(MasterDataCardProperties props) {
		cardMap = new HashMap<>();
		String langCode = props.getLangCode();
		List<String> languages = null;
		if (langCode != null && !langCode.isEmpty()) {
			languages = Arrays.asList(langCode.split(","));
			Map<String, String> map = props.getCard();
			if (map != null && !map.isEmpty()) {
				Set<String> keys = map.keySet();
				for (String lang : languages) {
					for (String key : keys) {
						if (key.endsWith(lang)) {
							Map<String, String> dataMap = cardMap.get(lang);
							if (dataMap == null) {
								dataMap = new HashMap<>();
								cardMap.put(lang, dataMap);
							}
							dataMap.put((key.substring(0, (key.length() - (lang.length() + 1)))), map.get(key));
						}
					}
				}
			}
		}

	}

	public Map<String, String> getMasterDataCards(String langCode) {
		return cardMap.get(langCode);
	}
}
