package com.twitter.configuration.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.twitter.configuration.loader.domain.ConfigKeyValueMap;
import com.twitter.configuration.loader.domain.ConfigValue;

/**
 * A Simple Configuration Loader.Load key and value as String.
 * 
 *
 *
 */
public class ConfigurationLoader extends AbstractConfigurationLoader {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConfigurationLoader.class);

	private static final String COMMENT_CHARACTERS = ";";

	private static final String KEY_VALUE_SEPARATOR_CHAR = "=";

	private static final String KEY_OVERRIDE_START_CHAR = "<";

	/**
	 * Assuming max cached entries would be around 1000
	 */
	private static final int MAX_CACHED_ENTRIES = 1000;

	/**
	 * Assuming the number of group would be at least 75.
	 */
	private static final int SECTION_CACHE_INITIAL_SIZE = 100;

	private MostRecentlyUsedConfigCache cache;

	private HashMap<String, ConfigKeyValueMap> configMap;

	private HashMap<String, Integer> overridesMap;

	/**
	 * Assuming configuration file path will be complete
	 * 
	 * @param path
	 * @param overrides
	 * @throws Exception
	 */
	public ConfigurationLoader(String path, String[] overrides)
			throws Exception {
		LOGGER.info(
				"Initializing ConfigurationLoader with config file={} and overrides={}",
				path, overrides);

		cache = new MostRecentlyUsedConfigCache(MAX_CACHED_ENTRIES);
		configMap = new HashMap<String, ConfigKeyValueMap>(
				SECTION_CACHE_INITIAL_SIZE);
		initializeOverrides(overrides);
		load(path);
	}

	private void initializeOverrides(String[] overrides) {
		int overridesSize = (null == overrides) ? 0 : overrides.length;

		LOGGER.info("OverridesSize={}", overridesSize);

		overridesMap = new HashMap<String, Integer>(overridesSize);
		if (null != overrides) {
			for (int i = 0; i < overridesSize; i++) {
				overridesMap.put(overrides[i], i + 1);
			}
		}
	}

	private void load(String path) throws Exception {
		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(path))))) {
			String line = bufferedReader.readLine();
			LOGGER.debug("contents = {}", line);
			String group = "";
			while (line != null) {
				line = line.trim();
				if (!isCommentLine(line)) {
					if (isGroupLine(line)) {
						group = line.substring(1, line.length() - 1);
					} else {
						String key = "";
						String value = "";
						int index = line.indexOf(KEY_VALUE_SEPARATOR_CHAR);
						if (index > 0) {
							key = line.substring(0, index).trim();
							value = getConvertedValue(getParsedValue(line
									.substring(index + 1)));
							ConfigKeyValueMap keyValueMap = configMap
									.get(group);
							if (keyValueMap == null) {
								LOGGER.info("Loading a new group : {}", group);
								keyValueMap = new ConfigKeyValueMap();
								configMap.put(group, keyValueMap);
							}
							addUpdateConfigValue(key, keyValueMap, value);
						}
					}
				}
				line = bufferedReader.readLine();
			}
		} catch (MalformedURLException e) {
			LOGGER.error(
					"Unparsable file path : {}. Failed to initialize configuraion loader",
					path, e);
			throw e;
		} catch (IOException e) {
			LOGGER.error(
					"Unable to read file={}. Failed to initialize configuraion loader",
					path, e);
			throw e;
		} catch (Exception e) {
			LOGGER.error("Failed to initialize configuraion loader", e);
			throw e;
		}
	}

	private String getConvertedValue(String value) {
		if ("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)
				|| "1".equalsIgnoreCase(value)) {
			return "true";
		} else if ("no".equalsIgnoreCase(value)
				|| "false".equalsIgnoreCase(value)
				|| "0".equalsIgnoreCase(value)) {
			return "false";
		} else {
			return value;
		}
	}

	private void addUpdateConfigValue(String key,
			ConfigKeyValueMap keyValueMap, String newValue) {

		int index = key.indexOf(KEY_OVERRIDE_START_CHAR);
		String newKey = key;
		if (index > 0 && overridesMap.size() > 0) {
			String ovverride = key.substring(index + 1, key.length() - 1)
					.trim();
			// If this override is specified in initialized overrides continue
			// other wise ignore this property.
			if (overridesMap.get(ovverride) != null) {
				newKey = key.substring(0, index).trim();
				ConfigValue existingConfigKey = keyValueMap.get(newKey);
				if (existingConfigKey == null) {
					keyValueMap.put(newKey, new ConfigValue(newValue,
							overridesMap.get(ovverride)));
				} else if (existingConfigKey.getConfigOverrideOrder() <= overridesMap
						.get(ovverride)) {
					// overrides already exist. If this override is there in
					// given form continue other wise
					existingConfigKey.setConfigOverrideOrder(overridesMap
							.get(ovverride));
					existingConfigKey.setValue(newValue);
				}
			}
		} else {
			// Inserting to map. In case of duplicate property last one will
			// overwrite others.
			keyValueMap.put(newKey, new ConfigValue(newValue));
		}
	}

	/**
	 * This method will first try to get the value from a most recently used
	 * cache without doing any string operations on key. If failed then try to
	 * get from master cache. Now master cache is loaded into memory.
	 * 
	 * @param key
	 * @return null or associated string value
	 */
	public String get(String key) {
		if (null == key || key.length() <= 0)
			return null;
		try {
			String value = cache.get(key);
			if (value != null) {
				LOGGER.info("Serving from cache for key={}", key);
				return value;
			} else {
				LOGGER.info("Cache miss for key={}", key);
				value = getInternal(key);
				cache.put(key, value);
				return value;
			}
		} catch (Exception ex) {
			LOGGER.error("Unexpected exception while serving cache for key={}",
					key, ex);
			return null;
		}
	}

	/**
	 * If configuration file is too big to keep in memory then in future this
	 * could be from disk.
	 * 
	 * @param key
	 * @return
	 */
	private String getInternal(String key) {
		String[] keys = key.split("\\.");
		if (keys.length <= 1) {
			return (configMap.get(key) == null) ? null : configMap.get(key)
					.asMap().toString();
		}
		ConfigKeyValueMap keyValueMap = configMap.get(keys[0].trim());
		if (keyValueMap == null)
			return null;
		ConfigValue configValue = keyValueMap.get(keys[1].trim());
		return (null == configValue) ? null : configValue.getValue();
	}

	private String getParsedValue(String value) {

		value = value.trim();
		boolean quoted = value.startsWith("\"") || value.startsWith("'");
		boolean stop = false;
		boolean escape = false;

		char quote = quoted ? value.charAt(0) : 0;

		int i = quoted ? 1 : 0;

		StringBuilder result = new StringBuilder();
		while (i < value.length() && !stop) {
			char c = value.charAt(i);

			if (quoted) {
				if ('\\' == c && !escape) {
					escape = true;
				} else if (!escape && quote == c) {
					stop = true;
				} else if (escape && quote == c) {
					escape = false;
					result.append(c);
				} else {
					if (escape) {
						escape = false;
						result.append('\\');
					}
					result.append(c);
				}
			} else {
				if (COMMENT_CHARACTERS.indexOf(c) == -1) {
					result.append(c);
				} else {
					stop = true;
				}
			}
			i++;
		}
		String v = result.toString();
		if (!quoted) {
			v = v.trim();
		}
		return v;
	}

	/**
	 * Determine if the given line is a group.
	 *
	 * @param line
	 *            The line to check.
	 * @return true if the line contains a group
	 */
	private boolean isGroupLine(String line) {
		if (line == null) {
			return false;
		}
		return line.startsWith("[") && line.endsWith("]");
	}

	/**
	 * Determine if the given line is a comment line.
	 *
	 * @param line
	 *            The line to check.
	 * @return true if the line is empty or starts with one of the comment
	 *         characters
	 */
	private boolean isCommentLine(String line) {
		if (null == line) {
			return false;
		}
		return line.length() < 1
				|| COMMENT_CHARACTERS.indexOf(line.charAt(0)) >= 0;
	}

	/**
	 * Not implemented
	 * 
	 * @param key
	 * @return null
	 */
	public String[] getStringArray(String key) {
		return null;
	}

	/**
	 * Not implemented
	 * 
	 * @param key
	 * @return
	 */
	public List<String> getStringList(String key) {
		return null;
	}

	/**
	 * Not implemented
	 * 
	 * @param key
	 * @return
	 */
	public Boolean getBoolean(String key) {
		return null;
	}

	/**
	 * Not implemented
	 * 
	 * @param key
	 * @return
	 */
	public Map<String, Object> getMap(String key) {
		return null;
	}
}
