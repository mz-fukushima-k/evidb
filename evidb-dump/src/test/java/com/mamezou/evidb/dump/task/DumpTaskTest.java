package com.mamezou.evidb.dump.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import com.mamezou.evidb.dump.task.DumpTask;

@RunWith(BlockJUnit4ClassRunner.class)
public class DumpTaskTest {

	@Test
	public void testExecute() throws Exception {

		DumpTask dumptask = new DumpTask();

		String configFile = this.getClass().getResource("./sqlgen.yml").toURI().getPath();
		dumptask.setConfigFile(configFile);

		String dumpDir = this.getClass().getResource("./dump").toURI().getPath();
		dumptask.setOutputDir(dumpDir);

		dumptask.setUser("postgres");
		dumptask.setPassword("admin");
		dumptask.setUrl("jdbc:postgresql://localhost:5432/dvdrental");
		dumptask.setDriver("org.postgresql.Driver");

		dumptask.execute();
	}

}
