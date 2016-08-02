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

import de.tobiasroeser.lambdatest.ProcedureWithException;
import de.tobiasroeser.lambdatest.TempFile;
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

	public static class SimplePendingWithReasonTest extends FreeSpec {
		public SimplePendingWithReasonTest() {
			if (runInnerTests) {
				test("should be pending with reason", () -> {
					pending("The Reason");
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
	private JvmResult testInJvm(final String className, final File testDir) throws Exception {
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
			pb.directory(testDir);
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

	private void testInJvm(final String className, final ProcedureWithException<JvmResult> f) throws Exception {
		TempFile.withTempDirP(dir -> {
			final JvmResult jvmResult = testInJvm(className, dir);
			f.apply(jvmResult);
		});
	}

	@Test(groups = { "testng" }, dependsOnGroups = { "tempfile" })
	public void testSuccessInSubProcess() throws Exception {
		testInJvm(SimpleSuccessTest.class.getName(), result -> {
			assertEquals(result.exitCode, 0);
			final Optional<String> line = Util.find(result.output, l -> l.startsWith("Total tests run"));
			assertTrue(line.isDefined());
			assertEquals(line.get(), "Total tests run: 1, Failures: 0, Skips: 0");
		});
	}

	@Test(groups = { "testng" }, dependsOnGroups = { "tempfile" })
	public void testFailureInSubProcess() throws Exception {
		testInJvm(SimpleFailureTest.class.getName(), result -> {
			assertEquals(result.exitCode, 1);
			final Optional<String> line = Util.find(result.output, l -> l.startsWith("Total tests run"));
			assertTrue(line.isDefined());
			assertEquals(line.get(), "Total tests run: 1, Failures: 1, Skips: 0");
		});
	}

	@Test(groups = { "testng" }, dependsOnGroups = { "tempfile" })
	public void testPendingInSubProcess() throws Exception {
		testInJvm(SimplePendingTest.class.getName(), result -> {
			assertNotEquals(result.exitCode, 0);
			final Optional<String> line = Util.find(result.output, l -> l.startsWith("Total tests run"));
			assertTrue(line.isDefined());
			assertEquals(line.get(), "Total tests run: 1, Failures: 0, Skips: 1");
		});
	}

	@Test(groups = { "testng" }, dependsOnGroups = { "tempfile" })
	public void testPendingWithReasonInSubProcess() throws Exception {
		testInJvm(SimplePendingWithReasonTest.class.getName(), result -> {
			assertNotEquals(result.exitCode, 0);
			final Optional<String> line = Util.find(result.output, l -> l.startsWith("Total tests run"));
			assertTrue(line.isDefined());
			assertEquals(line.get(), "Total tests run: 1, Failures: 0, Skips: 1");
		});
	}

}