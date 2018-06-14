package de.tobiasroeser.lambdatest;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Utility methods to work with temporary files and directories.
 *
 */
public class TempFile {

	/**
	 * Apply `f` to a newly created temporary directory which is deleted after
	 * `f` returns.
	 */
	public static <T> T withTempDir(final FunctionWithException<File, T> f) throws Exception {
		File file = null;
		try {
			file = File.createTempFile("lambdatest", "");
		} catch (final IOException e) {
			// We ingore it, as we test afterwards, if the file exists
		}
		if (file == null || !file.exists()) {
			throw new AssertionError("Just created file does not exist: " + file);
		}
		file.delete();
		file.mkdir();
		return deleteAfter(file, f);
	}

	/**
	 * Apply `p` to a newly created temporary directory which is deleted after
	 * `p` returns.
	 *
	 * This is the procedural equivalent of
	 * {@link #withTempDir(FunctionWithException)}.
	 */
	public static void withTempDirP(final ProcedureWithException<File> p) throws Exception {
		withTempDir(dir -> {
			p.apply(dir);
			return null;
		});
	}

	/**
	 * Creates a new temporary file with the given content `content`. After
	 * execution of the given function `f` the file is deleted.
	 */
	public static <T> T withTempFile(final String content, final FunctionWithException<File, T> f) throws Exception {
		final File tmpFile = File.createTempFile("lambdatest", "");
		return deleteAfter(tmpFile, file -> {
			writeToFile(file, content);
			return f.apply(file);
		});
	}

	/**
	 * Creates a new temporary file with the given content `content`. After
	 * execution of the given procedure `p` the file is deleted.
	 *
	 * This is the procedural equivalent of
	 * {@link #withTempFile(String, FunctionWithException)}.
	 */
	public static void withTempFileP(final String content, final ProcedureWithException<File> p) throws Exception {
		withTempFile(content, file -> {
			p.apply(file);
			return null;
		});
	}

	public static void writeToFile(final File file, final String content) throws IOException {
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}
		try (FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				PrintStream ps = new PrintStream(bos, false, "UTF-8")) {
			if (content != null) {
				ps.print(content);
				ps.flush();
				bos.flush();
				fos.flush();
			}
		}
	}

	/**
	 * Reads the given text file and return it's contents.
	 * 
	 * @param file
	 *            The text file.
	 * @return The text contents.
	 * @throws IOException
	 *             In case the file is missing or can't be read as text.
	 */
	public static String readFile(final File file) throws IOException {
		final Writer writer = new StringWriter();
		try (
				final InputStream fis = new FileInputStream(file);
				final InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
				final BufferedReader br = new BufferedReader(isr)) {

			final char[] buffer = new char[1024];
			int n;
			while ((n = br.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		}
		return writer.toString();
	}

	/**
	 * Apply `f` to the given file `file` and delete the file after the function
	 * returns.
	 */
	public static <T> T deleteAfter(final File file, final FunctionWithException<File, T> f) throws Exception {
		try {
			return f.apply(file);
		} finally {
			deleteRecursive(file);
		}
	}

	/**
	 * Delete a file recursively.
	 *
	 * @return `true` if the deletion was successful.
	 */
	public static boolean deleteRecursive(final File file) {
		boolean result = true;
		if (file.isDirectory()) {
			final File[] files = file.listFiles();
			if (files != null) {
				for (final File f : files) {
					result = deleteRecursive(f) && result;
				}
			}
		}
		// don't try to delete, if result was unsuccessful
		return result && file.delete();
	}

}
