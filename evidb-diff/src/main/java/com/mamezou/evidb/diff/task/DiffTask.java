package com.mamezou.evidb.diff.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.mamezou.evidb.core.exception.ApplicationException;
import com.mamezou.evidb.diff.model.DeltaItems;
import com.mamezou.evidb.diff.model.Item;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.jett.model.FillPattern;
import net.sf.jett.transform.ExcelTransformer;

/**
 * DiffTask
 *
 * @author koshiro-fukushima
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DiffTask extends Task {

	private String dumpDir;

	private String reportDir;

	private String before;

	private String after;

	private enum FILL_PATTERN {

		NOTHING(FillPattern.NOFILL.toString(), "white", "normal"), //
		HEADER(FillPattern.SOLID.toString(), "lightgreen", "bold"), //
		CHANGE(FillPattern.SOLID.toString(), "gold", "normal"), //
		ADD(FillPattern.SOLID.toString(), "skyblue", "normal"), //
		DELETE(FillPattern.SOLID.toString(), "grey25percent", "normal");

		private String fillPattern;
		private String fillColor;
		private String fontWeight;

		private FILL_PATTERN(String fillPattern, String fillColor, String fontWeight) {
			this.fillPattern = fillPattern;
			this.fillColor = fillColor;
			this.fontWeight = fontWeight;
		}

		public String getText() {
			return "fill-pattern: " + fillPattern + ";" + //
					"fill-foreground-color:" + fillColor + ";" + //
					"fill-background-color: white;" + //
					"font-weight: " + fontWeight + ";" + //
					"";
		}
	};

	/* (非 Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {

		if (StringUtils.isEmpty(before) && StringUtils.isEmpty(after)) {
			// 両方 null
			List<File> recentFiles = getRecentFiles();

			if (recentFiles.size() < 2) {
				throw new ApplicationException("There is no data");
			}

			after = recentFiles.get(0).getAbsolutePath();
			before = recentFiles.get(1).getAbsolutePath();

		} else if (StringUtils.isEmpty(before) || StringUtils.isEmpty(after)) {
			// 片方 null
			List<File> recentFiles = getRecentFiles();

			if (recentFiles.size() < 1) {
				throw new ApplicationException("There is no data");
			}

			if (StringUtils.isNotEmpty(after)) {
				before = after;
			}

			after = recentFiles.get(0).getAbsolutePath();

		} else {
		}

		// 差分データをレポート
		diffAll();
	}

	/**
	 * データ差分を出力
	 */
	protected void diffAll() {

		List<DeltaItems> deltaItemsList = new ArrayList<>();

		File beforeDir = new File(before);
		File afterDir = new File(after);

		List<String> beforeFiles = getFilenameList(beforeDir);

		for (String beforeFile : beforeFiles) {

			// 変更前CSV読み込み
			List<String> beforeCsv = readCSVFile(new File(beforeDir, beforeFile));

			// 変更後CSV読み込み
			List<String> afterCsv = readCSVFile(new File(afterDir, beforeFile));

			// diff
			Patch<String> patch = DiffUtils.diff(beforeCsv, afterCsv);

			DeltaItems deltaItems = reportDiff(beforeFile, patch, beforeCsv, afterCsv);

			deltaItems.setBefore(beforeCsv.size());
			deltaItems.setAfter(afterCsv.size());

			deltaItemsList.add(deltaItems);
		}

		Map<String, Object> beans = null;

		// 出力フォルダ作成
		File reportDir = new File(getReportDir());
		reportDir.mkdirs();

		// 出力ファイル名作成
		String reportFile = String.format("%s.xlsx",
				DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));

		List<String> templateSheetNames = new ArrayList<>();
		List<String> sheetNames = new ArrayList<>();
		List<Map<String, Object>> params = new ArrayList<>();

		templateSheetNames.add("summary");
		sheetNames.add("summary");
		beans = new HashMap<>();
		beans.put("deltas", deltaItemsList);
		params.add(beans);

		for (DeltaItems d : deltaItemsList) {
			if (!d.getDetails().isEmpty()) {
				templateSheetNames.add("report");
				sheetNames.add(d.getName());
				beans = new HashMap<>();
				beans.put("delta", d);
				params.add(beans);
			}
		}

		ExcelTransformer transformer = new ExcelTransformer();

		try (InputStream in = this.getClass().getResourceAsStream("/com/mamezou/evidb/diff/template/report.xlsx");
				OutputStream out = new FileOutputStream(new File(reportDir, reportFile))) {
			Workbook workbook = transformer.transform(in, templateSheetNames, sheetNames, params);
			if (sheetNames.size() == 1) {
				workbook.removeSheetAt(1);
			} else {
				for (int i = 1; i < sheetNames.size(); i++) {
					Sheet sheet = workbook.getSheetAt(i);
					Row row = sheet.getRow(0);
					short colSize = row.getLastCellNum();
					for (short col = 0; col < colSize; col++) {
						sheet.autoSizeColumn(col, true);
					}
				}
			}
			workbook.write(out);
			workbook.close();
		} catch (InvalidFormatException | IOException e) {
			throw new ApplicationException(e.getMessage(), e);
		}

	}

	/**
	 * データ差分をレポート
	 *
	 * @param fileName
	 * @param patch
	 */
	private DeltaItems reportDiff(String fileName, Patch<String> patch,
			List<String> beforeCsv, List<String> afterCsv) {

		CSVParser csvParser = new CSVParser();

		DeltaItems deltaItems = new DeltaItems();

		List<Delta<String>> deltas = patch.getDeltas();

		deltaItems.setCreate(0);
		deltaItems.setUpdate(0);
		deltaItems.setDelete(0);
		deltaItems.setDetails(new ArrayList<>());

		if (deltas.size() > 0) {
			// ヘッダ行の追加
			List<Item> columns = new ArrayList<>();
			String[] beforeItems = new String[0];

			try {
				beforeItems = csvParser.parseLine(beforeCsv.get(0));
			} catch (IOException e) {
				throw new ApplicationException(e.getMessage(), e);
			}

			columns.add(new Item("ステータス", FILL_PATTERN.HEADER.getText()));

			for (String s : beforeItems) {
				columns.add(new Item(s, FILL_PATTERN.HEADER.getText()));
			}

			deltaItems.getDetails().add(columns);

		}

		for (Delta<String> d : deltas) {

			int before = d.getOriginal().size();
			int after = d.getRevised().size();

			if (before == after) {

				deltaItems.setUpdate(deltaItems.getUpdate() + before);

				for (int i = 0; i < before; i++) {

					// 変更前のレコード
					String beforeStr = d.getOriginal().getLines().get(i);
					String[] beforeItems = new String[0];

					try {
						beforeItems = csvParser.parseLine(beforeStr);
					} catch (IOException e) {
						throw new ApplicationException(e.getMessage(), e);
					}

					// 変更後のレコード
					String afterStr = d.getRevised().getLines().get(i);
					String[] afterItems = new String[0];

					try {
						afterItems = csvParser.parseLine(afterStr);
					} catch (IOException e) {
						throw new ApplicationException(e.getMessage(), e);
					}

					List<Item> beforeColumns = new ArrayList<>();
					beforeColumns.add(new Item("Change(変更前)", FILL_PATTERN.NOTHING.getText()));

					for (int j = 0; j < beforeItems.length; j++) {
						beforeColumns.add(new Item(beforeItems[j], FILL_PATTERN.NOTHING.getText()));
					}

					List<Item> afterColumns = new ArrayList<>();
					afterColumns.add(new Item("Change(変更後)", FILL_PATTERN.NOTHING.getText()));

					for (int j = 0; j < afterItems.length; j++) {
						afterColumns.add(new Item(afterItems[j], FILL_PATTERN.NOTHING.getText()));
					}

					// before/after の異なる値のみ色替え
					for (int j = 0; j < beforeItems.length; j++) {
						if (!StringUtils.equals(getElement(beforeItems, j), getElement(afterItems, j))) {
							beforeColumns.get(j + 1).setStyle(FILL_PATTERN.CHANGE.getText());
							afterColumns.get(j + 1).setStyle(FILL_PATTERN.CHANGE.getText());
						}
					}

					deltaItems.getDetails().add(beforeColumns);
					deltaItems.getDetails().add(afterColumns);

				}

			} else if (before < after) {

				deltaItems.setCreate(deltaItems.getCreate() + after - before);

				// 追加されたレコード
				for (int i = 0; i < after; i++) {

					String afterStr = d.getRevised().getLines().get(i);
					String[] afterItems = new String[0];

					try {
						afterItems = csvParser.parseLine(afterStr);
					} catch (IOException e) {
						throw new ApplicationException(e.getMessage(), e);
					}

					List<Item> columns = new ArrayList<>();

					columns.add(new Item("Add", FILL_PATTERN.NOTHING.getText()));

					for (int j = 0; j < afterItems.length; j++) {
						columns.add(new Item(getElement(afterItems, j), FILL_PATTERN.ADD.getText()));
					}

					deltaItems.getDetails().add(columns);

				}

			} else if (before > after) {
				deltaItems.setDelete(deltaItems.getDelete() + before - after);

				for (int i = 0; i < before; i++) {

					// 変更前のレコード
					String beforeStr = d.getOriginal().getLines().get(i);
					String[] beforeItems = new String[0];

					try {
						beforeItems = csvParser.parseLine(beforeStr);
					} catch (IOException e) {
						throw new ApplicationException(e.getMessage(), e);
					}

					List<Item> columns = new ArrayList<>();

					columns.add(new Item("Delete", FILL_PATTERN.NOTHING.getText()));

					for (int j = 0; j < beforeItems.length; j++) {
						columns.add(new Item(getElement(beforeItems, j), FILL_PATTERN.DELETE.getText()));
					}
					deltaItems.getDetails().add(columns);
				}

			}

		}

		int dot = StringUtils.indexOf(fileName, ".");
		deltaItems.setName(dot >= 0 ? StringUtils.substring(fileName, 0, dot) : fileName);

		return deltaItems;

	}

	private String getElement(String[] array, int index) {

		if (ArrayUtils.isNotEmpty(array)) {
			if (0 <= index && index < array.length) {
				return array[index];
			}
		}

		return null;
	}

	/**
	 * 名前順（降順）にディレクトリを取得
	 *
	 * @return
	 */
	protected List<File> getRecentFiles() {

		File dumpDir = new File(this.dumpDir);

		File[] dumpDirs = dumpDir.listFiles();

		if (dumpDirs == null) {
			return Collections.emptyList();
		}

		List<File> resultList = Arrays.asList(dumpDirs);
		Collections.sort(resultList, NameFileComparator.NAME_REVERSE);

		return resultList;

	}

	/**
	 * ファイル名一覧取得
	 *
	 * @param targetDir
	 * @return
	 */
	protected List<String> getFilenameList(File targetDir) {

		File[] targetFiles = targetDir.listFiles();

		List<File> resultList = Arrays.asList(targetFiles);
		Collections.sort(resultList, NameFileComparator.NAME_COMPARATOR);

		List<String> filenameList = new ArrayList<String>();

		for (File file : resultList) {
			filenameList.add(file.getName());
		}

		return filenameList;

	}

	/**
	 * CSVファイル読み込み
	 *
	 * @param inFile
	 * @return
	 */
	protected List<String> readCSVFile(File inFile) {

		List<String> csvList = new ArrayList<String>();

		try (
				CSVReader csvReader = new CSVReader(
						new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "utf-8"))); //
		) {
			while (true) {
				String[] s = csvReader.readNext();
				if (s == null) {
					break;
				}
				StringWriter sw = new StringWriter();
				CSVWriter csvWriter = new CSVWriter(sw);
				csvWriter.writeNext(s);
				csvWriter.flush();
				csvList.add(StringUtils.substring(sw.toString(), 0, sw.toString().length() - 1));
				csvWriter.close();
			}

			return csvList;

		} catch (IOException e) {
			throw new ApplicationException(e.getMessage(), e);
		}

	}

}
