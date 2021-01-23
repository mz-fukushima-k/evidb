package com.mamezou.evidb.dump.model;

import java.util.List;

import lombok.Data;

@Data
public class Settings {

	private static Settings instance = new Settings();

	private Settings() {
	}

	public static Settings get() {
		return instance;
	}

	/** DB driver */
	private String driver;

	/** DB url */
	private String url;

	/** User name */
	private String user;

	/** Password */
	private String password;

	/** outputDir */
	private String outputDir;

	/** null value */
	private String nullValue;

	/** SQL CONFIG */
	private List<Item> items;

}
