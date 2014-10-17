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

public class FreeSpec extends Assert {

	private List<Object[]> testCases = new LinkedList<>();
	private boolean testNeverRun = true;

	public void test(final String name, final RunnableWithException testCase) {
		final String testName = getClass().getSimpleName() + ": " + name;

		if (find(testCases, tc -> testName.equals(tc[0])).isDefined()) {
			System.out.println("Test with non-unique name added: " + testName);
		}
		this.testCases.add(new Object[] { testName, testCase });
	}

	@DataProvider
	public Iterator<Object[]> testCases() {
		return testCases.iterator();
	}

	public void pending() {
		throw new SkipException("Pending");
	}

	/**
	 * Intercept exceptions of type <code>exceptionType</code> and fail if no
	 * such exception or an exception with an incompatible type was thrown.
	 * 
	 * @param exceptionType
	 *            The exception type to intercept.
	 * @throws TextException
	 *             If no exception or an exception with an incompatible type was
	 *             thrown.
	 */
	public void intercept(final Class<? extends Exception> exceptionType,
			final RunnableWithException throwing) throws Exception {
		intercept(exceptionType, ".*", throwing);
	}

	public void intercept(final Class<? extends Exception> exceptionType,
			String messageRegex, final RunnableWithException throwing)
			throws Exception {
		try {
			throwing.run();
		} catch (Exception e) {
			if (exceptionType.isAssignableFrom(e.getClass())) {
				final boolean matches;
				if (".*".equals(messageRegex)) {
					matches = true;
				} else {
					final String msg = e.getMessage();
					if (msg == null) {
						matches = false;
					} else {
						matches = Pattern.matches(messageRegex, msg);
					}
					if (!matches)
						return;
					else {
						throw new TestException(
								"Exception was thrown with the wrong message: Expected: '" + messageRegex
										+ "' but got '" + msg + "'", e);
					}
				}
			}
			throw new TestException("Thrown exception of type [" + exceptionType.getName()
					+ "] does not match expected type [" + e.getClass().getName() + "]", e);
		}
		throw new TestException("Expected exception of type [" + exceptionType.getName() + "] was not thrown");
	}

	@Test(dataProvider = "testCases")
	public void runTests(final String name, final RunnableWithException testCase) throws Exception {
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

		} catch (final Throwable e) {
			out.println(ansi.fg(Color.RED) + "-- FAILED  " + testName);
			// System.out.println(e.getMessage());
			e.printStackTrace(out);
			Throwable oldCause = e;
			Throwable cause = e.getCause();
			while (cause != null && cause != oldCause) {
				out.print("Caused by: ");
				e.printStackTrace(out);
				oldCause = cause;
				cause = cause.getCause();
			}
			System.out.print(ansi.reset());
			throw e;
		} finally {
			out.close();
		}
	}
}
