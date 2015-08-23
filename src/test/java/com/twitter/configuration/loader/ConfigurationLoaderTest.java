package com.twitter.configuration.loader;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for ConfigurationLoader.
 */
public class ConfigurationLoaderTest {

	private static ConfigurationLoader config;

	@BeforeClass
	public static void init() {
		try {
			config = new ConfigurationLoader(ConfigurationLoader.class
					.getResource("/config.ini").toURI().getPath(),
					new String[] { "test" });
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
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
	public void testOverrideProperties() {
		Assert.assertEquals("/etc/var/uploads", config.get("ftp.path"));
	}

	@Test
	public void testSectionProperties() {
		Assert.assertEquals(
				"{path=/tmp/, path=/etc/var/uploads, name=hello there, ftp uploading, enabled=no}",
				config.get("ftp"));
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
