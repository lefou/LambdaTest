package de.tobiasroeser.lambdatest.testng;

import static de.tobiasroeser.lambdatest.internal.Util.find;

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

import de.tobiasroeser.lambdatest.Section;
import de.tobiasroeser.lambdatest.Expect;
import de.tobiasroeser.lambdatest.Intercept;
import de.tobiasroeser.lambdatest.LambdaTest;
import de.tobiasroeser.lambdatest.Reporter;
import de.tobiasroeser.lambdatest.RunnableWithException;
import de.tobiasroeser.lambdatest.internal.DefaultReporter;
import de.tobiasroeser.lambdatest.internal.DefaultTestCase;
import de.tobiasroeser.lambdatest.internal.Util;

/**
 * Inherit from this class to create a new TestNG test suite and use the
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
 * before it's usage including asserts will be executed, but code after it will
 * be skipped. Thus you can mark a test also as work-in-progress.</li>
 * </ul>
 *
 * TODO: example
 *
 */
public class FreeSpec implements LambdaTest {

	private static final String PENDING_DEFAULT_MSG = "Pending";

	private final List<DefaultTestCase> testCases = new LinkedList<>();
	private volatile boolean testNeverRun = true;
	private boolean runInParallel = false;
	private boolean expectFailFast;
	private Reporter reporter = new DefaultReporter();

	private String suiteName = getClass().getName();
	private static final ThreadLocal<Section> sectionHolder = new ThreadLocal<Section>();

	@Override
	public void setRunInParallel(final boolean runInParallel) {
		if (!testNeverRun) {
			reporter.suiteWarning(suiteName, "Tests already started. Cannot change settings.");
			return;
		}
		this.runInParallel = runInParallel;
	}

	@Override
	public void setExpectFailFast(final boolean failFast) {
		if (!testNeverRun) {
			System.out.println("Tests already started. Cannot change settings.");
			return;
		}
		this.expectFailFast = failFast;
	}

	@Override
	public Reporter getReporter() {
		return reporter;
	}

	@Override
	public void setReporter(Reporter reporter) {
		this.reporter = reporter;
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
		if (find(testCases, tc -> name.equals(tc.getName())).isDefined()) {
			reporter.suiteWarning(suiteName, "Test with non-unique name added: " + name);
		}
		this.testCases.add(new DefaultTestCase(sectionHolder.get(), name, suiteName, testCase));
	}

	/**
	 * Marks the test as pending. Instructions after <code>pending()</code> will not
	 * be executed and TestNG marks the test as skipped.
	 */
	@Override
	public void pending() {
		throw new SkipException(PENDING_DEFAULT_MSG);
	}

	/**
	 * Marks the test as pending and uses the given <code>reason</code> as message.
	 * Instructions after <code>pending()</code> will not be executed and TestNG
	 * marks the test as skipped.
	 */
	@Override
	public void pending(final String reason) {
		throw new SkipException(reason);
	}

	/**
	 * Intercept exceptions of type <code>exceptionType</code> and fail if no such
	 * exception or an exception with an incompatible type was thrown.
	 *
	 * @param exceptionType
	 *            The exception type to intercept.
	 * @param throwing
	 *            The execution block which is expected to throw the exception.
	 * @return The intercepted exception.
	 * @throws Exception
	 *             If no exception was thrown or an exception with an incompatible
	 *             type was thrown.
	 */
	@Override
	public <T extends Throwable> T intercept(final Class<T> exceptionType,
			final RunnableWithException throwing) throws Exception {
		return Intercept.intercept(exceptionType, throwing);
	}

	/**
	 * Intercept exceptions of type <code>exceptionType</code> and fail if no such
	 * exception or an exception with an incompatible type was thrown or it the
	 * message does not match a given pattern.
	 *
	 * @param exceptionType
	 *            The exception type to intercept.
	 * @param messageRegex
	 *            A regular expression pattern to match the expected message. See
	 *            {@link Pattern} for details.
	 * @param throwing
	 *            The execution block which is expected to throw the exception.
	 * @return The intercepted exception.
	 * @throws Exception
	 *             If no exception was thrown or an exception with an incompatible
	 *             type was thrown or if the message of the exception did not match
	 *             the expected pattern.
	 */
	@Override
	public <T extends Throwable> T intercept(final Class<T> exceptionType,
			final String messageRegex, final RunnableWithException throwing)
			throws Exception {
		return Intercept.intercept(exceptionType, messageRegex, throwing);
	}

	private void runTestCase(final DefaultTestCase testCase) throws Throwable {
		if (testNeverRun) {
			synchronized (this) {
				if (testNeverRun) {
					reporter.suiteStart(suiteName, testCases);
					testNeverRun = false;
				}
			}
		}

		try {
			Expect.setup(expectFailFast);
			Throwable uncatchedTestError = null;
			Throwable delayedTestError = null;
			try {
				reporter.testStart(testCase);
				testCase.getTest().run();
			} catch (final Throwable t) {
				uncatchedTestError = t;
			}
			try {
				Expect.finish();
			} catch (final Throwable t) {
				delayedTestError = t;
			}
			if (uncatchedTestError != null && delayedTestError != null) {
				throw new AssertionError(
						"An error occured (see root cause) after some expectations failed. Failed Expectations:\n"
								+ delayedTestError.getMessage(),
						uncatchedTestError);
			} else if (uncatchedTestError != null) {
				// if this was a SkipException, we still detect it, else some
				// other errors occurred before
				throw uncatchedTestError;
			} else if (delayedTestError != null) {
				throw delayedTestError;
			}
			reporter.testSucceeded(testCase);
		} catch (final SkipException e) {
			reporter.testSkipped(testCase, e.getMessage());
			throw e;
		} catch (final Throwable e) {
			reporter.testFailed(testCase, e);
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
	public void runFreeSpecTestCases(final DefaultTestCase testCase) throws Throwable {
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
	public void runFreeSpecParallelTestCases(final DefaultTestCase testCase) throws Throwable {
		runTestCase(testCase);
	}

	public void section(String section, Runnable code) {
		Section parent = sectionHolder.get();
		sectionHolder.set(new Section(section, parent));
		code.run();
		if (parent == null) {
			sectionHolder.remove();
		} else {
			sectionHolder.set(parent);
		}
	}

}
