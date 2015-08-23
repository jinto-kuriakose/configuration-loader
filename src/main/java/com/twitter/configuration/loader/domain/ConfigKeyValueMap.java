/**
 * 
 */
package com.twitter.configuration.loader.domain;

import java.util.HashMap;

/**
 * @author 
 *
 */
public class ConfigKeyValueMap {

	private HashMap<String, ConfigValue> keyValueMap = new HashMap<String, ConfigValue>();

	public ConfigValue get(String key) {
		return keyValueMap.get(key);
	}

	public void put(String key, ConfigValue value) {
		keyValueMap.put(key, value);
	}

	@Override
	public String toString() {
		return keyValueMap.toString();
	}

	public HashMap<String, ConfigValue> asMap() {
		return keyValueMap;
	}
}
