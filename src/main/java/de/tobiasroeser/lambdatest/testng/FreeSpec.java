package de.tobiasroeser.lambdatest.testng;

import static de.tobiasroeser.lambdatest.internal.Util.find;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.TestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import de.tobiasroeser.lambdatest.LambdaTest;
import de.tobiasroeser.lambdatest.internal.AnsiColor;
import de.tobiasroeser.lambdatest.internal.AnsiColor.Color;
import de.tobiasroeser.lambdatest.internal.Util;

/**
 * Inherit from this class to create a new test suite and use the
 * {@link FreeSpec#test} method to add test cases.
 * <p>
 * It provides the following methods:
 * <ul>
 * <li>{@link FreeSpec#test(String, RunnableWithException)} to declare a new
 * test case
 * <li>{@link FreeSpec#intercept(Class, RunnableWithException)} and
 * {@link FreeSpec#intercept(Class, String, RunnableWithException)} to intercept
 * and assert expected exceptions.</li>
 * <li>{@link FreeSpec#pending()} to mark a test case as pending. All code
 * before it's usage including assert will be executed, but code after it will
 * be skipped. Thus you can mark a test also as work-in-progress.</li>
 * </ul>
 *
 * TODO: example
 *
 */
public class FreeSpec implements LambdaTest {

	private List<LambdaTestCase> testCases = new LinkedList<>();
	private volatile boolean testNeverRun = true;
	private boolean runInParallel = false;

	public void setRunInParallel(final boolean runInParallel) {
		this.runInParallel = runInParallel;
	}

	/**
	 * Adds a test to the test suite.
	 *
	 * @param name
	 *            The name of the new test.
	 * @param testCase
	 *            The test case. It should return when it is successful, else it
	 *            should throw an exception. Exceptions of type
	 *            {@link TestException} which are typically thrown by
	 *            Assert.assertXXX methods (e.g.
	 *            {@link Assert#assertEquals(String, String)}) are specially
	 *            recognized by TestNG.
	 */
	public void test(final String name, final RunnableWithException testCase) {
		final String testName = getClass().getSimpleName() + ": " + name;

		if (find(testCases, tc -> testName.equals(tc.getName())).isDefined()) {
			System.out.println("Test with non-unique name added: " + testName);
		}
		this.testCases.add(new LambdaTestCase(testName, testCase));
	}

	/**
	 * Marks the test as pending. Instructions after <code>pending()</code> will
	 * not be executed and TestNG marks the test as skipped.
	 */
	@Override
	public void pending() {
		throw new SkipException("Pending");
	}

	/**
	 * Intercept exceptions of type <code>exceptionType</code> and fail if no
	 * such exception or an exception with an incompatible type was thrown.
	 *
	 * @param exceptionType
	 *            The exception type to intercept.
	 * @param throwing
	 *            The execution block which is expected to throw the exception.
	 * @return The intercepted exception.
	 * @throws Exception
	 *             If no exception was thrown or an exception with an
	 *             incompatible type was thrown.
	 */
	@Override
	public <T extends Throwable> T intercept(final Class<T> exceptionType,
			final RunnableWithException throwing) throws Exception {
		return intercept(exceptionType, ".*", throwing);
	}

	/**
	 * Intercept exceptions of type <code>exceptionType</code> and fail if no
	 * such exception or an exception with an incompatible type was thrown or it
	 * the message does not match a given pattern.
	 *
	 * @param exceptionType
	 *            The exception type to intercept.
	 * @param messageRegex
	 *            A regular expression pattern to match the expected message.
	 *            See {@link Pattern} for details.
	 * @param throwing
	 *            The execution block which is expected to throw the exception.
	 * @return The intercepted exception.
	 * @throws Exception
	 *             If no exception was thrown or an exception with an
	 *             incompatible type was thrown or if the message of the
	 *             exception did not match the expected pattern.
	 */
	@Override
	public <T extends Throwable> T intercept(final Class<T> exceptionType,
			final String messageRegex, final RunnableWithException throwing)
			throws Exception {
		try {
			throwing.run();
		} catch (final Throwable e) {
			if (exceptionType.isAssignableFrom(e.getClass())) {
				final String msg = e.getMessage();
				final boolean matches;
				{
					if (".*".equals(messageRegex)) {
						matches = true;
					} else {
						if (msg == null) {
							matches = false;
						} else {
							matches = Pattern.matches(messageRegex, msg);
						}
					}
				}
				if (matches) {
					// safe cast, as I check isAssignableFrom before
					@SuppressWarnings("unchecked")
					final T t = (T) e;
					return t;
				} else {
					throw new TestException(
							"Exception was thrown with the wrong message: Expected: '" + messageRegex
									+ "' but got '" + msg + "'.", e);
				}
			}
			throw new TestException("Thrown exception of type [" + exceptionType.getName()
					+ "] does not match expected type [" + e.getClass().getName() + "]", e);
		}
		throw new TestException("Expected exception of type [" + exceptionType.getName() + "] was not thrown");
	}

	private void runTestCase(final LambdaTestCase testCase) throws Exception {
		final AnsiColor ansi = new AnsiColor();
		final PrintStream out = System.out;

		if (testNeverRun) {
			synchronized (this) {
				if (testNeverRun) {
					out.println("Running " + ansi.fg(Color.CYAN) + testCases.size()
							+ ansi.reset() + " tests in " + ansi.fg(Color.CYAN)
							+ getClass().getName() + ansi.reset() + ":");
					testNeverRun = false;
				}
			}
		}

		final String testName = testCase.getName();
		try {
			testCase.getTest().run();
			out.println(ansi.fg(Color.GREEN) + "-- SUCCESS " + testName + ansi.reset());
		} catch (final SkipException e) {
			out.println(ansi.fg(Color.YELLOW) + "-- SKIPPED " + testName + " (pending)" + ansi.reset());
			throw e;
		} catch (final Throwable e) {
			try {
				out.println(ansi.fg(Color.RED) + "-- FAILED  " + testName);
				// System.out.println(e.getMessage());
				e.printStackTrace(out);
				Throwable oldCause = e;
				Throwable cause = e.getCause();
				// unpack exception stack
				while (cause != null && cause != oldCause) {
					out.print("Caused by: ");
					cause.printStackTrace(out);
					oldCause = cause;
					cause = cause.getCause();
				}
			} catch (final Throwable t) {
				// ignore any further errors, just in case
			} finally {
				System.out.print(ansi.reset());
			}
			throw e;
		}
	}

	@DataProvider(name = "freeSpecTestCases", parallel = false)
	public Iterator<Object[]> freeSpecTestCases() {
		if (!runInParallel) {
			return Util.map(testCases, (tc) -> new Object[] { tc }).iterator();
		} else {
			return Collections.<Object[]> emptyList().iterator();
		}
	}

	@Test(dataProvider = "freeSpecTestCases")
	public void runFreeSpecTestCases(final LambdaTestCase testCase) throws Exception {
		runTestCase(testCase);
	}

	@DataProvider(name = "freeSpecParallelTestCases", parallel = true)
	public Iterator<Object[]> freeSpecParallelTestCases() {
		if (runInParallel) {
			return Util.map(testCases, (tc) -> new Object[] { tc }).iterator();
		} else {
			return Collections.<Object[]> emptyList().iterator();
		}
	}

	@Test(dataProvider = "freeSpecParallelTestCases")
	public void runFreeSpecParallelTestCases(final LambdaTestCase testCase) throws Exception {
		runTestCase(testCase);
	}

}
