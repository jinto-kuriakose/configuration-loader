/**
 * 
 */
package com.twitter.configuration.loader;

import org.junit.Assert;
import org.junit.Test;

import com.twitter.configuration.loader.domain.ConfigKeyValueMap;
import com.twitter.configuration.loader.domain.ConfigValue;

/**
 * @author 
 *
 */
public class ConfigKeyValueMapTest {

	@Test
	public void testConfigKeyValueMap() {

		ConfigKeyValueMap configKeyValueMap = new ConfigKeyValueMap();
		ConfigValue configKey1 = new ConfigValue("value1");
		ConfigValue configKey2 = new ConfigValue("value2");
		configKeyValueMap.put("key1", configKey1);
		configKeyValueMap.put("key2", configKey2);

		Assert.assertEquals(configKey1, configKeyValueMap.get("key1"));
		Assert.assertEquals(configKey2, configKeyValueMap.get("key2"));

	}

}
