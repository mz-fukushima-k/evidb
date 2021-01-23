package com.mamezou.evidb.dump.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.seasar.doma.internal.WrapException;
import org.seasar.doma.internal.util.ClassUtil;
import org.seasar.doma.jdbc.tx.TransactionManager;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.mamezou.evidb.core.exception.ApplicationException;
import com.mamezou.evidb.dump.config.AppConfig;
import com.mamezou.evidb.dump.dao.GenericDao;
import com.mamezou.evidb.dump.model.Item;
import com.mamezou.evidb.dump.model.Settings;
import com.opencsv.CSVWriter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DumpTask extends Task {

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
	private String nullValue = "\u200B(NULL)\u200B";

	/** 設定ファイル */
	private String configFile;

	private GenericDao genericDao;

	/* (非 Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {

		// 設定ファイル読み込み
		setupConfig();

		// JDBCドライバーのロード
		loadDriver();

		// Dao 取得
		genericDao = getGenericDao();

		Settings settings = Settings.get();

		// 出力フォルダ作成
		File outputDir = new File(getOutputDir(),
				DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
		outputDir.mkdirs();

		TransactionManager tm = AppConfig.singleton().getTransactionManager();

		tm.required(() -> {
			for (Item item : settings.getItems()) {
				outputFile(outputDir, item);
			}
		});

	}

	/**
	 * 設定ファイル読み込み
	 */
	@SuppressWarnings("unchecked")
	private void setupConfig() {

		Settings settings = Settings.get();

		settings.setDriver(driver);
		settings.setUrl(url);
		settings.setUser(user);
		settings.setPassword(password);
		settings.setOutputDir(outputDir);
		settings.setNullValue(nullValue);

		Yaml yaml = new Yaml(new Constructor());

		settings.setItems(new ArrayList<Item>());

		try (InputStream in = new FileInputStream(configFile);
				InputStreamReader reader = new InputStreamReader(in, "utf-8")) {

			Map<String, Object> yamlMap = yaml.load(in);

			Map<String, Object> settingsMap = (Map<String, Object>) yamlMap.get("settings");

			for (Map<String, Object> itemMap : (List<Map<String, Object>>) settingsMap.get("items")) {
				Item item = new Item();
				BeanUtils.copyProperty(item, "tableName", itemMap.get("table-name"));
				BeanUtils.copyProperty(item, "tableComment", itemMap.get("table-comment"));
				BeanUtils.copyProperty(item, "columnName", itemMap.get("column-name"));
				BeanUtils.copyProperty(item, "query", itemMap.get("query"));
				settings.getItems().add(item);
			}

		} catch (IOException | IllegalAccessException | InvocationTargetException e) {
			throw new ApplicationException(e.getMessage(), e);
		}

	}

	/**
	 * ドライバーロード
	 * @return
	 */
	private void loadDriver() {

		try {
			Class.forName(driver);

		} catch (ClassNotFoundException e) {
			throw new ApplicationException(e.getMessage(), e);
		}

	}

	/**
	 * 検索結果をファイルへ出力する。
	 *
	 * @param item
	 * @throws Exception
	 */
	private void outputFile(File outputDir, Item item) {

		// 出力ファイルパス生成
		File outputFile = new File(outputDir, item.getTableName() + ".csv");

		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8");
				CSVWriter csvWriter = new CSVWriter(osw)) {

			// BOM
			osw.write("\ufeff");

			// ヘッダー出力
			csvWriter.writeNext(item.getColumnName().keySet().toArray(new String[0]));

			genericDao.selectAll(item.getQuery(), stream -> {
				stream.forEach(entity -> {
					String[] array = entity.values().stream().map(mapper -> {
						if (mapper != null) {
							return mapper.toString();
						}
						return getNullValue();
					}).toArray(String[]::new);
					csvWriter.writeNext(array);
				});
				return null;
			});

		} catch (IOException e) {
			throw new ApplicationException(e.getMessage(), e);
		}

	}

	private GenericDao getGenericDao() {

		try {
			Object newInstance = ClassUtil.newInstance(Class.forName("com.mamezou.evidb.dump.dao.GenericDaoImpl"));
			return (GenericDao) newInstance;
		} catch (ClassNotFoundException | WrapException e) {
			throw new ApplicationException(e.getMessage(), e);
		}

	}

}
