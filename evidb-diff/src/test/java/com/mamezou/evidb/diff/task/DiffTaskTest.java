package com.mamezou.evidb.diff.task;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class DiffTaskTest {

	@Test
	public void testGetRecentFiles() throws Exception {

		DiffTask diffTask = new DiffTask();

		String dumpDir = this.getClass().getResource("./dump").toURI().getPath();

		diffTask.setDumpDir(dumpDir);

		List<File> dirs = diffTask.getRecentFiles();

		Assert.assertEquals("20190107004829", dirs.get(0).getName());
		Assert.assertEquals("20190107004008", dirs.get(1).getName());

	}

	@Test
	public void testGetFilenameList() throws Exception {

		DiffTask diffTask = new DiffTask();

		String dumpDir = this.getClass().getResource("./dump").toURI().getPath();

		List<String> result = diffTask.getFilenameList(new File(dumpDir, "20190107004829"));

		Assert.assertEquals(Arrays.asList("address.csv"), result);

	}

	@Test
	public void testReadCSVFile() throws Exception {

		DiffTask diffTask = new DiffTask();

		String dumpDir = this.getClass().getResource("./dump").toURI().getPath();

		File inFile = new File(new File(dumpDir, "20190107004008"), "address.csv");

		List<String> results = diffTask.readCSVFile(inFile);

		String expected = "\"\ufeffaddress_id\",\"address\",\"address2\",\"district\",\"city_id\",\"postal_code\",\"phone\",\"last_update\"";

		Assert.assertEquals(expected, results.get(0));

	}

	@Test
	public void testDiffAll() throws Exception {

		DiffTask diffTask = new DiffTask();

		String dumpDir = this.getClass().getResource("./dump").toURI().getPath();
		String reportDir = this.getClass().getResource("./report").toURI().getPath();

		System.out.println(reportDir);

		diffTask.setReportDir(reportDir);
		diffTask.setBefore(new File(dumpDir, "20190107004008").getAbsolutePath());
		diffTask.setAfter(new File(dumpDir, "20190107004829").getAbsolutePath());

		diffTask.diffAll();

	}

}
