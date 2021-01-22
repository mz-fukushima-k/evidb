package com.mamezou.evidb.core.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mamezou.evidb.core.util.TableUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * データベースのテーブルメタデータです。
 *
 * @author fukushima
 *
 */
@Data
@EqualsAndHashCode
public class TableMeta {

	/** カタログ名 */
	protected String catalogName;

	/** スキーマ名 */
	protected String schemaName;

	/** 名前 */
	protected String name;

	/** コメント */
	protected String comment;

	/** カラムメタデータのリスト */
	protected final List<ColumnMeta> columnMetas = new ArrayList<ColumnMeta>();

	/** 主キーのカラムメタデータのリスト */
	protected final List<ColumnMeta> primaryKeyColumnMetas = new ArrayList<ColumnMeta>();

	/**
	 * カラムのメタデータのリストを返します。
	 *
	 * @return カラムのメタデータのリスト
	 */
	public List<ColumnMeta> getColumnMetas() {
		return Collections.unmodifiableList(columnMetas);
	}

	/**
	 * カラムのメタデータを追加します。
	 *
	 * @param columnMeta
	 *            カラム記述
	 */
	public void addColumnMeta(ColumnMeta columnMeta) {
		columnMetas.add(columnMeta);
		columnMeta.setTableMeta(this);
		if (columnMeta.isPrimaryKey()) {
			primaryKeyColumnMetas.add(columnMeta);
		}
	}

	/**
	 * 主キーのカラムメタデータのリストを返します。
	 *
	 * @return 主キーのカラムメタデータのリスト
	 */
	public List<ColumnMeta> getPrimaryKeyColumnMetas() {
		return Collections.unmodifiableList(primaryKeyColumnMetas);
	}

	/**
	 * 完全なテーブル名を返します。
	 *
	 * @return 完全なテーブル名
	 */
	public String getQualifiedTableName() {
		return TableUtil.getQualifiedTableName(catalogName, schemaName, name);
	}

	/**
	 * 複合主キーを持つ場合{@code true}を返します。
	 *
	 * @return 複合主キーを持つ場合{@code true}、そうでない場合{@code false}
	 */
	public boolean hasCompositePrimaryKey() {
		return primaryKeyColumnMetas.size() > 1;
	}

}
