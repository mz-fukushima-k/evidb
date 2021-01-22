/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.mamezou.evidb.core.dialect;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mamezou.evidb.core.constants.ClassConstants;
import com.mamezou.evidb.core.exception.ApplicationException;

/**
 * DB2用の方言です。
 *
 * @author taedium
 *
 */
public class Db2GenDialect extends StandardGenDialect {

	/**
	 * インスタンスを構築します。
	 */
	public Db2GenDialect() {
		classNameMap.put("char () for bit data", ClassConstants.bytes
				.getQualifiedName());
		classNameMap.put("decimal", BigDecimal.class.getName());
		classNameMap.put("long varchar for bit data", ClassConstants.bytes
				.getQualifiedName());
		classNameMap.put("long varchar", String.class.getName());
		classNameMap.put("varchar () for bit data", ClassConstants.bytes
				.getQualifiedName());
	}

	@Override
	public String getName() {
		return "db2";
	}

	@Override
	public String getDialectClassName() {
		return ClassConstants.Db2Dialect.getQualifiedName();
	}

	@Override
	public String getDefaultSchemaName(String userName) {
		return userName != null ? userName.toUpperCase() : null;
	}

	@Override
	public boolean supportsIdentity() {
		return true;
	}

	@Override
	public boolean supportsSequence() {
		return true;
	}

	@Override
	public boolean isAutoIncrement(Connection connection, String catalogName,
			String schemaName, String tableName, String columnName)
			throws SQLException {
		if (connection == null) {
			throw new ApplicationException("connection");
		}
		if (tableName == null) {
			throw new ApplicationException("tableName");
		}
		if (columnName == null) {
			throw new ApplicationException("columnName");
		}

		String sql = "select generated from syscat.columns where tabschema = ? and tabname = ? and colname = ?";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, schemaName);
			ps.setString(2, tableName);
			ps.setString(3, columnName);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String generated = rs.getString(1);
					return "A".equals(generated) || "D".equals(generated);
				}
				return false;
			}
		}

	}
}
