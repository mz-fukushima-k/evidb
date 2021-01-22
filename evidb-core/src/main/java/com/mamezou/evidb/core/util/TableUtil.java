package com.mamezou.evidb.core.util;

import org.apache.commons.lang3.StringUtils;

public class TableUtil {

	/**
	 * 完全なテーブル名を返します。
	 *
	 * @param catalogName
	 * @param schemaName
	 * @param tableName
	 * @return
	 */
	public static String getQualifiedTableName(String catalogName, String schemaName, String tableName) {

		StringBuilder result = new StringBuilder();

		if (!StringUtils.isEmpty(catalogName)) {
			result.append(catalogName).append('.');
		}

		if (!StringUtils.isEmpty(schemaName)) {
			result.append(schemaName).append('.');
		}

		return result.append(tableName).toString();
	}
}
