/**
 * 
 */
package com.twitter.configuration.loader;

import java.util.LinkedHashMap;

/**
 * @author
 *
 */
public class MostRecentlyUsedConfigCache extends LinkedHashMap<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int maxEntries;

	public MostRecentlyUsedConfigCache(int maxEntries) {

		super(maxEntries, 0.75f, true);
		this.maxEntries = maxEntries;
	}

	@Override
	protected boolean removeEldestEntry(
			java.util.Map.Entry<String, String> eldest) {
		return size() > maxEntries;
	}
}
