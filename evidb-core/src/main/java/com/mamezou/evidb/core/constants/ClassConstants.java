package com.mamezou.evidb.core.constants;

public enum ClassConstants {

	/** */
	StandardDialect("com.mamezou.evidb.core.dialect", "StandardDialect"),
	/** */
	HsqldbDialect("com.mamezou.evidb.core.dialect", "HsqldbDialect"),
	/** */
	H2Dialect("com.mamezou.evidb.core.dialect", "H2Dialect"),
	/** */
	MysqlDialect("com.mamezou.evidb.core.dialect", "MysqlDialect"),
	/** */
	PostgresDialect("com.mamezou.evidb.core.dialect", "PostgresDialect"),
	/** */
	Db2Dialect("com.mamezou.evidb.core.dialect", "Db2Dialect"),
	/** */
	Mssql2008("com.mamezou.evidb.core.dialect", "Mssql2008Dialect"),
	/** */
	Mssql("com.mamezou.evidb.core.dialect", "MssqlDialect"),
	/** */
	Oracle11Dialect("com.mamezou.evidb.core.dialect", "Oracle11Dialect"),
	/** */
	OracleDialect("com.mamezou.evidb.core.dialect", "OracleDialect"),
	/** */
	bytes("", "byte[]"),;

	private final String packageName;

	private final String simpleName;

	private ClassConstants(String packageName, String simpleName) {
		this.packageName = packageName;
		this.simpleName = simpleName;
	}

	/**
	 * 完全修飾名を返します。
	 *
	 * @return 完全修飾名
	 */
	public String getQualifiedName() {
		return packageName + "." + simpleName;
	}

	/**
	 * パッケージ名を返します。
	 *
	 * @return パッケージ名
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * 単純名を返します。
	 *
	 * @return 単純名
	 */
	public String getSimpleName() {
		return simpleName;
	}

}
