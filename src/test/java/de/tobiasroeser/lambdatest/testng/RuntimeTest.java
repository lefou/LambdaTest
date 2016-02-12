package de.tobiasroeser.lambdatest.testng;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import de.tobiasroeser.lambdatest.internal.Optional;
import de.tobiasroeser.lambdatest.internal.Util;

public class RuntimeTest {

	private static final boolean runInnerTests = System.getProperty("RUN_INNER_TEST", "0").equals("1");

	public static class SimpleFailureTest extends FreeSpec {
		public SimpleFailureTest() {
			if (runInnerTests) {
				test("should fail", () -> {
					Assert.assertTrue(false);
				});
			}
		}
	}

	public static class SimplePendingTest extends FreeSpec {
		public SimplePendingTest() {
			if (runInnerTests) {
				test("should be pending", () -> {
					pending();
					Assert.fail("should not be reached");
				});
			}
		}
	}

	public static class SimpleSuccessTest extends FreeSpec {
		public SimpleSuccessTest() {
			if (runInnerTests) {
				test("should succeed", () -> {
					Assert.assertTrue(true);
				});
			}
		}
	}

	static class JvmResult {
		int exitCode;
		List<String> output;
	}

	/**
	 * Need to test in separate process/JVM to avoid classes of the outer and
	 * inner TestNG instances. Thank you global singletons. :(
	 */
	private JvmResult testInJvm(final String className) throws Exception {
		final ClassLoader cl = getClass().getClassLoader();
		if (cl instanceof URLClassLoader) {
			final URL[] origUrLs = ((URLClassLoader) cl).getURLs();
			final URL[] urls = Arrays.copyOf(origUrLs, origUrLs.length);

			final String jvm = System.getProperty("java.home");

			final ProcessBuilder pb = new ProcessBuilder(
					jvm + "/bin/java",
					"-classpath",
					Util.mkString(urls, File.pathSeparator),
					"-DRUN_INNER_TEST=1",
					"org.testng.TestNG",
					"-testclass",
					className);
			pb.directory(new File("."));
			final Process p = pb.start();

			final List<String> output = new LinkedList<>();

			final InputStream is = p.getInputStream();
			final InputStreamReader isr = new InputStreamReader(is);
			final BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				output.add(line);
			}

			final JvmResult result = new JvmResult();
			result.exitCode = p.waitFor();
			result.output = output;
			return result;
		}
		throw new AssertionError("Could not run JVM");
	}

	@Test(groups = { "testng" })
	public void testSuccessInSubProcess() throws Exception {
		final JvmResult result = testInJvm(SimpleSuccessTest.class.getName());
		assertEquals(result.exitCode, 0);
		final Optional<String> line = Util.find(result.output, l -> l.startsWith("Total tests run"));
		assertTrue(line.isDefined());
		assertEquals(line.get(), "Total tests run: 1, Failures: 0, Skips: 0");
	}

	@Test(groups = { "testng" })
	public void testFailureInSubProcess() throws Exception {
		final JvmResult result = testInJvm(SimpleFailureTest.class.getName());
		assertEquals(result.exitCode, 1);
		final Optional<String> line = Util.find(result.output, l -> l.startsWith("Total tests run"));
		assertTrue(line.isDefined());
		assertEquals(line.get(), "Total tests run: 1, Failures: 1, Skips: 0");
	}

	@Test(groups = { "testng" })
	public void testPendingInSubProcess() throws Exception {
		final JvmResult result = testInJvm(SimplePendingTest.class.getName());
		assertNotEquals(result.exitCode, 0);
		final Optional<String> line = Util.find(result.output, l -> l.startsWith("Total tests run"));
		assertTrue(line.isDefined());
		assertEquals(line.get(), "Total tests run: 1, Failures: 0, Skips: 1");
	}

}