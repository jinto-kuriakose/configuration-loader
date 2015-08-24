package com.twitter.configuration.loader;

import java.util.List;
import java.util.Map;

public abstract class AbstractConfigurationLoader {

	public abstract String get(String key);

	public abstract String[] getStringArray(String key);

	public abstract List<String> getStringList(String key);

	public abstract Boolean getBoolean(String key);

	public abstract Map<String, Object> getMap(String key);

}
