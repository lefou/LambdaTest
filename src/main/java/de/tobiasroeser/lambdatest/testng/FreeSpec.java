package de.tobiasroeser.lambdatest.testng;

import java.util.Collections;
import java.util.Iterator;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.TestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import de.tobiasroeser.lambdatest.Expect;
import de.tobiasroeser.lambdatest.LambdaTest;
import de.tobiasroeser.lambdatest.RunnableWithException;
import de.tobiasroeser.lambdatest.generic.DefaultTestCase;
import de.tobiasroeser.lambdatest.generic.FreeSpecBase;
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
public class FreeSpec extends FreeSpecBase implements LambdaTest {

	private static final String PENDING_DEFAULT_MSG = "Pending";

	private volatile boolean testNeverRun = true;

	@Override
	public void setRunInParallel(final boolean runInParallel) {
		if (!testNeverRun) {
			getReporter().suiteWarning(getSuiteName(), "Tests already started. Cannot change settings.");
			return;
		}
		super.setRunInParallel(runInParallel);
	}

	@Override
	public void setExpectFailFast(final boolean failFast) {
		if (!testNeverRun) {
			getReporter().suiteWarning(getSuiteName(), "Tests already started. Cannot change settings.");
			return;
		}
		super.setExpectFailFast(failFast);
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
		super.test(name, testCase);
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

	private void runTestCase(final DefaultTestCase testCase) throws Throwable {
		if (testNeverRun) {
			synchronized (this) {
				if (testNeverRun) {
					getReporter().suiteStart(getSuiteName(), getTestCases());
					testNeverRun = false;
				}
			}
		}

		try {
			Expect.setup(getExpectFailFast());
			Throwable uncatchedTestError = null;
			Throwable delayedTestError = null;
			try {
				getReporter().testStart(testCase);
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
			getReporter().testSucceeded(testCase);
		} catch (final SkipException e) {
			getReporter().testSkipped(testCase, e.getMessage());
			throw e;
		} catch (final Throwable e) {
			getReporter().testFailed(testCase, e);
			throw e;
		}
	}

	@DataProvider(name = "freeSpecTestCases", parallel = false)
	public Iterator<Object[]> freeSpecTestCases() {
		if (!getRunInParallel()) {
			return Util.map(getTestCases(), (tc) -> new Object[] { tc }).iterator();
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
		if (getRunInParallel()) {
			return Util.map(getTestCases(), (tc) -> new Object[] { tc }).iterator();
		} else {
			return Collections.<Object[]> emptyList().iterator();
		}
	}

	@Test(dataProvider = "freeSpecParallelTestCases")
	public void runFreeSpecParallelTestCases(final DefaultTestCase testCase) throws Throwable {
		runTestCase(testCase);
	}

}
