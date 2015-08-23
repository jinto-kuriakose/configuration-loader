/**
 * 
 */
package com.twitter.configuration.loader.domain;

/**
 * @author 
 *
 */
public class ConfigValue {

	String value;
	int configOverrideOrder;

	public ConfigValue(String value, int configOverrideOrder) {
		this.value = value;
		this.configOverrideOrder = configOverrideOrder;
	}

	public ConfigValue(String value) {
		this.value = value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setConfigOverrideOrder(int configOverrideOrder) {
		this.configOverrideOrder = configOverrideOrder;
	}

	public String getValue() {
		return value;
	}

	public int getConfigOverrideOrder() {
		return configOverrideOrder;
	}

	@Override
	public String toString() {
		return value;
	}
}
