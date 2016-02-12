package de.tobiasroeser.lambdatest;

import java.io.File;
import java.io.IOException;

public class TempFile {

//	/**
//	 * Apply <code>p</code> to a newly created temporary directory which is
//	 * deleted after <code>p</code> returns.
//	 */
//	public static void withTempDir(final ProcedureWithException<File> p) throws Exception {
//		withTempDir(dir -> {
//			p.apply(dir);
//			return null;
//		});
//	}

	/**
	 * Apply <code>f</code> to a newly created temporary directory which is
	 * deleted after <code>f</code> returns.
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
	 * Apply <code>f</code> to the given file <code>file</code> and delete the
	 * file after the function returns.
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
	 * @return <code>true</code> if the deletion was successful.
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
