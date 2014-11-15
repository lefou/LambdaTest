package de.tobiasroeser.lambdatest.testng;

import static de.tobiasroeser.lambdatest.internal.Util.find;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.TestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import de.tobiasroeser.lambdatest.internal.AnsiColor;
import de.tobiasroeser.lambdatest.internal.AnsiColor.Color;

/**
 * Inherit from this class to create a new test suite and use the
 * {@link FreeSpec#test} method to add test cases.
 * <p>
 * It provides the following methods:
 * <ul>
 * <li>@linke {@link FreeSpec#test(String, RunnableWithException)} to declare a test case
 * <li>{@link FreeSpec#intercept(Class, RunnableWithException)} and
 * {@link FreeSpec#intercept(Class, String, RunnableWithException)} to intercept
 * and assert expected exceptions.</li>
 * <li>{@link FreeSpec#pending()} to mark a test case as pending</li>
 * </ul>
 * 
 * TODO: example
 *
 */
public class FreeSpec {

	private List<Object[]> testCases = new LinkedList<>();
	private volatile boolean testNeverRun = true;

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

		if (find(testCases, tc -> testName.equals(tc[0])).isDefined()) {
			System.out.println("Test with non-unique name added: " + testName);
		}
		this.testCases.add(new Object[] { testName, testCase });
	}

	/**
	 * Marks the test as pending. Instructions after <code>pending()</code> will
	 * not be executed and TestNG marks the test as skipped.
	 */
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
	 * @throws Exception
	 *             If no exception was thrown or an exception with an
	 *             incompatible type was thrown.
	 */
	public void intercept(final Class<? extends Exception> exceptionType,
			final RunnableWithException throwing) throws Exception {
		intercept(exceptionType, ".*", throwing);
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
	 * @throws Exception
	 *             If no exception was thrown or an exception with an
	 *             incompatible type was thrown or if the message of the
	 *             exception did not match the expected pattern.
	 */
	public void intercept(final Class<? extends Exception> exceptionType,
			String messageRegex, final RunnableWithException throwing)
			throws Exception {
		try {
			throwing.run();
		} catch (Exception e) {
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
				if (matches)
					return;
				else {
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

	@DataProvider(name = "freeSpecTestCases")
	public Iterator<Object[]> freeSpecTestCases() {
		return testCases.iterator();
	}

	@Test(dataProvider = "freeSpecTestCases")
	public void runFreeSpecTestCases(final String name, final RunnableWithException testCase) throws Exception {
		AnsiColor ansi = new AnsiColor();
		final PrintStream out = System.out;

		if (testNeverRun) {
			out.println("Running " + ansi.fg(Color.CYAN) + testCases.size()
					+ ansi.reset() + " tests in " + ansi.fg(Color.CYAN)
					+ getClass().getName() + ansi.reset() + ":");
			testNeverRun = false;
		}

		final String testName = name;
		try {
			testCase.run();
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
			} catch (Throwable t) {
				// ignore any further errors, just in case
			} finally {
				System.out.print(ansi.reset());
			}
			throw e;
		}
	}
}
