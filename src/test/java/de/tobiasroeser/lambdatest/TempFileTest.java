package de.tobiasroeser.lambdatest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.testng.annotations.Test;
import org.testng.reporters.Files;

public class TempFileTest {

	@Test(groups = { "tempfile" })
	public void test_withTempDir_creates_and_deletes_a_dir() throws Exception {
		final File dir = TempFile.withTempDir(d -> {
			assertTrue(d.exists());
			assertTrue(d.isDirectory());
			return d;
		});
		assertNotNull(dir);
		assertFalse(dir.exists());
	}

	@Test(groups = { "tempfile" })
	public void test_withTempDirP_creates_and_deletes_a_dir() throws Exception {
		final File[] dir = new File[] { null };
		TempFile.withTempDirP(d -> {
			assertTrue(d.exists());
			assertTrue(d.isDirectory());
			dir[0] = d;
		});
		assertNotNull(dir[0]);
		assertFalse(dir[0].exists());
	}

	@Test(groups = { "tempfile" })
	public void test_withTempFile_creates_and_deletes_a_file() throws Exception {
		final File file = TempFile.withTempFile("content\n1", f -> {
			assertTrue(f.exists());
			assertTrue(f.isFile());
			assertEquals(Files.readFile(f), "content\n1\n");
			return f;
		});
		assertNotNull(file);
		assertFalse(file.exists());
	}

	@Test(groups = { "tempfile" })
	public void test_withTempFileP_creates_and_deletes_a_file() throws Exception {
		final File[] file = new File[] { null };
		TempFile.withTempFileP("content\n2", f -> {
			assertTrue(f.exists());
			assertTrue(f.isFile());
			assertEquals(Files.readFile(f), "content\n2\n");
			file[0] = f;
		});
		assertNotNull(file[0]);
		assertFalse(file[0].exists());
	}

}
