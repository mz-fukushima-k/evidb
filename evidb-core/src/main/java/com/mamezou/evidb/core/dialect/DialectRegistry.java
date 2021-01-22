package com.mamezou.evidb.core.dialect;

import java.util.HashMap;
import java.util.Map;

public class DialectRegistry {

	private static Map<String, Dialect> registry = new HashMap<>();

	static {
		register(new Db2GenDialect());
		register(new H2GenDialect());
		register(new HsqldbGenDialect());
		register(new Mssql2008GenDialect());
		register(new MssqlGenDialect());
		register(new MysqlGenDialect());
		register(new Oracle11GenDialect());
		register(new OracleGenDialect());
		register(new PostgresGenDialect());
	}

	/**
	 * Dialectの登録
	 *
	 * @param dialect
	 */
	private static void register(Dialect dialect) {
		registry.put(dialect.getName(), dialect);
	}

	/**
	 * Dialectの検索
	 *
	 * @param dialectName
	 * @return
	 */
	public static Dialect lookup(String dialectName) {
		return registry.get(dialectName);
	}

}
