package de.tobiasroeser.lambdatest.junit;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;

import de.tobiasroeser.lambdatest.LambdaTest;
import de.tobiasroeser.lambdatest.Reporter;
import de.tobiasroeser.lambdatest.RunnableWithException;
import de.tobiasroeser.lambdatest.generic.FreeSpecBase;

/**
 * Inherit from this class to create a new JUnit test suite and use the
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
@RunWith(FreeSpecRunner.class)
public class FreeSpec extends FreeSpecBase implements LambdaTest {

	private final String suiteName;

	public FreeSpec() {
		suiteName = getClass().getName();
	}

	@Override
	public void setRunInParallel(final boolean runInParallel) {
		getReporter().suiteWarning(suiteName, "RunInParallel not supported under JUnit.");
	}

	/**
	 * Marks the test as pending. Instructions after <code>pending()</code> will not
	 * be executed.
	 */
	@Override
	public void pending() {
		throw new AssumptionViolatedException(Reporter.PENDING_DEFAULT_MSG);
	}

	/**
	 * Marks the test as pending and uses the given <code>reason</code> as message.
	 * Instructions after <code>pending()</code> will not be executed.
	 */
	@Override
	public void pending(final String reason) {
		throw new AssumptionViolatedException(reason);
	}

	// protected List<DefaultTestCase> getTestCases() {
	// return testCases;
	// }

}
