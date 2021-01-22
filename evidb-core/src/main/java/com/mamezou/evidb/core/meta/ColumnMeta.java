package com.mamezou.evidb.core.meta;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * データベースのカラムメタデータです。
 *
 * @author fukushima
 *
 */
@Data
@EqualsAndHashCode
public class ColumnMeta {

	/** 名前 */
	protected String name;

	/** SQL型 */
	protected int sqlType;

	/** 型名 */
	protected String typeName;

	/** 長さ */
	protected int length;

	/** スケール */
	protected int scale;

	/** デフォルト値 */
	protected String defaultValue;

	/** NULL可能の場合 */
	protected boolean nullable;

	/** 主キーの場合 */
	protected boolean primaryKey;

	/** 値が自動的に増分される場合 */
	protected boolean autoIncrement;

	/** 一意の場合{@code true} */
	protected boolean unique;

	/** コメント */
	protected String comment;

	/** テーブルメタデータ */
	protected TableMeta tableMeta;

}
