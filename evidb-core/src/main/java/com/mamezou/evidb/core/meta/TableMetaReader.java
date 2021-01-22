package com.mamezou.evidb.core.meta;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.mamezou.evidb.core.dialect.Dialect;
import com.mamezou.evidb.core.exception.ApplicationException;

/**
 * テーブルメタデータのリーダです。
 *
 * @author fukushima
 */
public class TableMetaReader {

	/** 方言 */
	protected final Dialect dialect;

	/** スキーマ名 */
	protected final String schemaName;

	/** 読み取り対象とするテーブル名のパターン */
	protected final Pattern tableNamePattern;

	/** 読み取り非対象とするテーブル名のパターン */
	protected final Pattern ignoredTableNamePattern;

	protected final List<String> tableTypes;

	/**
	 * インスタンスを構築します。
	 *
	 * @param dialect
	 *            方言
	 * @param dataSource
	 *            データソース
	 * @param schemaName
	 *            スキーマ名
	 * @param tableNamePattern
	 *            読み取り対象とするテーブル名のパターン
	 * @param ignoredTableNamePattern
	 *            読み取り非対象とするテーブル名のパターン
	 * @param tableTypes
	 *            テーブルの型のリスト
	 */
	public TableMetaReader(Dialect dialect, String schemaName, String tableNamePattern,
			String ignoredTableNamePattern, List<String> tableTypes) {
		if (dialect == null) {
			throw new ApplicationException("dialect");
		}
		if (tableNamePattern == null) {
			throw new ApplicationException("tableNamePattern");
		}
		if (ignoredTableNamePattern == null) {
			throw new ApplicationException("ignoreTableNamePattern");
		}
		if (tableTypes == null) {
			throw new ApplicationException("tableTypes");
		}
		this.dialect = dialect;
		this.schemaName = schemaName;
		this.tableNamePattern = Pattern
				.compile(tableNamePattern, Pattern.CASE_INSENSITIVE);
		this.ignoredTableNamePattern = Pattern
				.compile(ignoredTableNamePattern, Pattern.CASE_INSENSITIVE);
		this.tableTypes = tableTypes;
	}

	/**
	 * テーブルメタデータを読み取ります。
	 *
	 * @return テーブルメタデータ
	 */
	public List<TableMeta> read(Connection con) {
		try {
			DatabaseMetaData metaData = con.getMetaData();
			List<TableMeta> tableMetas = getTableMetas(metaData, schemaName != null ? schemaName
					: getDefaultSchemaName(metaData));
			for (TableMeta tableMeta : tableMetas) {
				Set<String> primaryKeySet = getPrimaryKeys(metaData, tableMeta);
				handleColumnMeta(metaData, tableMeta, primaryKeySet);
			}
			if (dialect.isJdbcCommentUnavailable()) {
				readCommentFromDictinary(con, tableMetas);
			}
			return tableMetas;
		} catch (SQLException e) {
			throw new ApplicationException(e.getMessage(), e);
		}

	}

	protected void handleColumnMeta(DatabaseMetaData metaData,
			TableMeta tableMeta, Set<String> primaryKeySet) throws SQLException {
		for (ColumnMeta columnMeta : getDbColumnMetas(metaData, tableMeta)) {
			if (primaryKeySet.contains(columnMeta.getName())) {
				columnMeta.setPrimaryKey(true);
				if (primaryKeySet.size() == 1) {
					columnMeta
							.setAutoIncrement(isAutoIncrement(metaData, tableMeta, columnMeta
									.getName()));
				}
			}
			tableMeta.addColumnMeta(columnMeta);
		}
	}

	protected String getDefaultSchemaName(DatabaseMetaData metaData)
			throws SQLException {
		String userName = metaData.getUserName();
		return dialect.getDefaultSchemaName(userName);
	}

	protected List<TableMeta> getTableMetas(DatabaseMetaData metaData,
			String schemaName) throws SQLException {

		List<TableMeta> results = new ArrayList<TableMeta>();

		String[] types = this.tableTypes.toArray(new String[this.tableTypes.size()]);

		try (ResultSet rs = metaData.getTables(null, schemaName, null, types)) {
			while (rs.next()) {
				TableMeta tableMeta = new TableMeta();
				tableMeta.setCatalogName(rs.getString("TABLE_CAT"));
				tableMeta.setSchemaName(rs.getString("TABLE_SCHEM"));
				tableMeta.setName(rs.getString("TABLE_NAME"));
				tableMeta.setComment(rs.getString("REMARKS"));
				if (isTargetTable(tableMeta)) {
					results.add(tableMeta);
				}
			}
			return results;
		}
	}

	protected boolean isTargetTable(TableMeta dbTableMeta) {
		String name = dbTableMeta.getName();
		if (!tableNamePattern.matcher(name).matches()) {
			return false;
		}
		if (ignoredTableNamePattern.matcher(name).matches()) {
			return false;
		}
		return true;
	}

	protected List<ColumnMeta> getDbColumnMetas(DatabaseMetaData metaData,
			TableMeta tableMeta) throws SQLException {

		List<ColumnMeta> results = new ArrayList<ColumnMeta>();

		try (
				ResultSet rs = metaData
						.getColumns(tableMeta.getCatalogName(), tableMeta
								.getSchemaName(), tableMeta.getName(), null)) {
			while (rs.next()) {
				ColumnMeta columnMeta = new ColumnMeta();
				columnMeta.setName(rs.getString("COLUMN_NAME"));
				columnMeta.setSqlType(rs.getInt("DATA_TYPE"));
				columnMeta.setTypeName(rs.getString("TYPE_NAME").toLowerCase());
				columnMeta.setLength(rs.getInt("COLUMN_SIZE"));
				columnMeta.setScale(rs.getInt("DECIMAL_DIGITS"));
				columnMeta.setNullable(rs.getBoolean("NULLABLE"));
				columnMeta.setDefaultValue(rs.getString("COLUMN_DEF"));
				columnMeta.setComment(rs.getString("REMARKS"));
				results.add(columnMeta);
			}
			return results;
		}
	}

	protected Set<String> getPrimaryKeys(DatabaseMetaData metaData,
			TableMeta tableMeta) throws SQLException {
		Set<String> results = new HashSet<String>();
		try (ResultSet rs = metaData
				.getPrimaryKeys(tableMeta.getCatalogName(), tableMeta
						.getSchemaName(), tableMeta.getName());) {
			while (rs.next()) {
				results.add(rs.getString("COLUMN_NAME"));
			}
		}
		return results;
	}

	protected boolean isAutoIncrement(DatabaseMetaData metaData,
			TableMeta tableMeta, String columnName) throws SQLException {
		return dialect.isAutoIncrement(metaData.getConnection(), tableMeta
				.getCatalogName(), tableMeta.getSchemaName(),
				tableMeta
						.getName(),
				columnName);
	}

	protected void readCommentFromDictinary(Connection connection,
			List<TableMeta> dbTableMetaList) throws SQLException {
		for (TableMeta tableMeta : dbTableMetaList) {
			String tableComment = dialect.getTableComment(connection, tableMeta
					.getCatalogName(), tableMeta.getSchemaName(),
					tableMeta
							.getName());
			tableMeta.setComment(tableComment);
			Map<String, String> columnCommentMap = dialect
					.getColumnCommentMap(connection, tableMeta.getCatalogName(), tableMeta
							.getSchemaName(), tableMeta.getName());
			for (ColumnMeta columnMeta : tableMeta.getColumnMetas()) {
				String columnName = columnMeta.getName();
				if (columnCommentMap.containsKey(columnName)) {
					String columnComment = columnCommentMap.get(columnName);
					columnMeta.setComment(columnComment);
				}
			}
		}
	}
}
