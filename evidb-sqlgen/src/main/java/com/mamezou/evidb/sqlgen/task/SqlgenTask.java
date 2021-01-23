package com.mamezou.evidb.sqlgen.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import com.mamezou.evidb.core.dialect.Dialect;
import com.mamezou.evidb.core.dialect.DialectRegistry;
import com.mamezou.evidb.core.exception.ApplicationException;
import com.mamezou.evidb.core.meta.TableMeta;
import com.mamezou.evidb.core.meta.TableMetaReader;
import com.mamezou.evidb.sqlgen.thymeleaf.dialect.HelperDialect;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Sqlgen Task
 *
 * @author fukushima
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SqlgenTask extends Task {

	/** DB driver */
	private String driver;

	/** DB url */
	private String url;

	/** User name */
	private String user;

	/** Password */
	private String password;

	/** スキーマ名 */
	private String schemaName;

	/** 対象テーブル名のパターン */
	private String tableNamePattern;

	/** 除外テーブル名のパターン */
	private String ignoredTableNamePattern;

	/** ＤＢ方言 */
	private String dialect;

	/** テーブル種別 (TABLE or VIEW or MATERIALIZED VIEW) */
	private String tableTypes;

	/** テンプレート格納ディレクトリ */
	private String templateDir;

	/** 出力ファイル名 */
	private String outputPath;

	/**
	 * anttask-main
	 */
	@Override
	public void execute() throws BuildException {

		Dialect dialect = DialectRegistry.lookup(this.dialect);

		List<String> tableTypes = Arrays.asList(
				StringUtils.stripAll(
						StringUtils.split(this.tableTypes, ',')));

		TableMetaReader reader = new TableMetaReader(dialect, schemaName, tableNamePattern, ignoredTableNamePattern,
				tableTypes);

		TemplateEngine templateEngine = new TemplateEngine();
		addTemplateResolver(templateEngine);
		templateEngine.addDialect(new HelperDialect());

		Context context = new Context();

		try (Connection conn = getConnection()) {
			List<TableMeta> tableMetas = reader.read(conn);
			context.setVariable("tableMetas", tableMetas);
		} catch (SQLException e) {
			throw new ApplicationException(e.getMessage(), e);
		}

		try (
				Writer writer = new BufferedWriter( //
						new OutputStreamWriter( //
								new FileOutputStream(new File(outputPath)), "utf-8"))) {

			templateEngine.process("sqlgen.txt", context, writer);

		} catch (UnsupportedEncodingException e) {
			throw new ApplicationException(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			throw new ApplicationException(e.getMessage(), e);
		} catch (IOException e) {
			throw new ApplicationException(e.getMessage(), e);
		}

	}

	/**
	 * ＤＢコネクション取得
	 * @return
	 */
	private Connection getConnection() {

		try {
			Class.forName(driver);

			return DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			throw new ApplicationException(e.getMessage(), e);
		} catch (SQLException e) {
			throw new ApplicationException(e.getMessage(), e);
		}

	}

	/**
	 * テンプレートディレクトリの設定
	 * @param templateEngine
	 */
	private void addTemplateResolver(TemplateEngine templateEngine) {

		int order = 1;

		if (StringUtils.isNotEmpty(templateDir)) {
			FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();

			fileTemplateResolver.setOrder(order++);
			fileTemplateResolver.setTemplateMode(TemplateMode.TEXT);
			fileTemplateResolver.setCharacterEncoding("utf-8");
			fileTemplateResolver.setCacheable(false);
			fileTemplateResolver.setPrefix(templateDir);

			templateEngine.addTemplateResolver(fileTemplateResolver);
		}

		ClassLoaderTemplateResolver classLoaderTemplateResolver = new ClassLoaderTemplateResolver();

		classLoaderTemplateResolver.setOrder(order++);
		classLoaderTemplateResolver.setTemplateMode(TemplateMode.TEXT);
		classLoaderTemplateResolver.setCharacterEncoding("utf-8");
		classLoaderTemplateResolver.setCacheable(false);
		classLoaderTemplateResolver.setPrefix("/com/mamezou/evidb/sqlgen/template/");

		templateEngine.addTemplateResolver(classLoaderTemplateResolver);

	}

}
