package com.mamezou.evidb.sqlgen.thymeleaf.util;

import java.util.regex.Pattern;

public class StringHelper {

	private static final Pattern REMOVE_NEW_LINE_PATTERN = Pattern.compile("[\\r\\n]");

	/**
	 * 改行コードを半角スペースにします。
	 *
	 * @param text
	 * @return
	 */
	public String removeNewLine(String text) {
		if (text != null) {
			text = REMOVE_NEW_LINE_PATTERN.matcher(text).replaceAll(" ");
		}
		return text;

	}

}
