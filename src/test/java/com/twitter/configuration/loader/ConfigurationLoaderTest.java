package com.twitter.configuration.loader;

import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for ConfigurationLoader.
 */
public class ConfigurationLoaderTest {

	private ConfigurationLoader config;

	@Before
	public void init() {
		try {
			config = new ConfigurationLoader(ConfigurationLoaderTest.class
					.getResource("/config.ini").toURI().getPath(),
					new String[] { "test" });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testOverridePropertiesWithUnKnowOverride() {
		try {
			config = new ConfigurationLoader(ConfigurationLoader.class
					.getResource("/config.ini").toURI().getPath(),
					new String[] { "test12121" });
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertEquals("/tmp/", config.get("ftp.path"));
	}

	@Test
	public void testOverridePropertiesWithProductionOverride() {
		try {
			config = new ConfigurationLoader(ConfigurationLoader.class
					.getResource("/config.ini").toURI().getPath(),
					new String[] { "production" });
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertEquals("/srv/var/tmp/", config.get("ftp.path"));
	}

	@Test
	public void testOverridePropertiesWithStagingOverride() {
		try {
			config = new ConfigurationLoader(ConfigurationLoader.class
					.getResource("/config.ini").toURI().getPath(),
					new String[] { "staging" });
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertEquals("/srv/uploads/", config.get("ftp.path"));
	}

	@Test
	public void testOverridePropertiesWithUbuntuOverride() {
		try {
			config = new ConfigurationLoader(ConfigurationLoader.class
					.getResource("/config.ini").toURI().getPath(),
					new String[] { "ubuntu" });
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertEquals("/etc/var/uploads", config.get("ftp.path"));
	}

	@Test
	public void testWithUnknownProperty() {
		Assert.assertEquals(null,
				config.get("common.unknownProperty_123456_789"));
	}

	@Test
	public void testCommonProperties() {
		Assert.assertEquals("2147483648",
				config.get("common.paid_users_size_limit"));
	}

	@Test
	public void testStringProperties() {
		Assert.assertEquals("hello there, ftp uploading",
				config.get("ftp.name"));
	}

	@Test
	public void testArrayProperties() {
		Assert.assertEquals("array,of,values", config.get("http.params"));
	}

	@Test
	public void testNullProperties() {
		Assert.assertEquals(null, config.get("ftp.lastname"));
	}

	@Test
	public void tesBooleanProperties() {
		Assert.assertEquals("false", config.get("ftp.enabled"));
	}

	@Test
	public void testGroupProperties() {

		try {
			config = new ConfigurationLoader(ConfigurationLoader.class
					.getResource("/config.ini").toURI().getPath(),
					new String[] { "production" });
		} catch (Exception e) {
			e.printStackTrace();
		}

		LinkedHashMap<String, String> expected = new LinkedHashMap<String, String>(
				3);
		expected.put("enabled", "false");
		expected.put("name", "hello there, ftp uploading");
		expected.put("path", "/srv/var/tmp/");

		Assert.assertEquals(expected.toString(), config.get("ftp"));
	}

	@Test
	public void testPropertiesCache() {
		Assert.assertEquals("2147483648",
				config.get("common.paid_users_size_limit"));
		Assert.assertEquals("2147483648",
				config.get("common.paid_users_size_limit"));
		Assert.assertEquals("2147483648",
				config.get("common.paid_users_size_limit"));
	}
}
