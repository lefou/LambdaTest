package de.tobiasroeser.lambdatest.junit;

import static de.tobiasroeser.lambdatest.internal.Util.find;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.runner.RunWith;
import org.testng.Assert;
import org.testng.TestException;

import de.tobiasroeser.lambdatest.Intercept;
import de.tobiasroeser.lambdatest.LambdaTest;
import de.tobiasroeser.lambdatest.RunnableWithException;
import de.tobiasroeser.lambdatest.shared.LambdaTestCase;

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
public class FreeSpec implements LambdaTest {

	private final List<LambdaTestCase> testCases = new LinkedList<>();
	private boolean expectFailFast;

	@Override
	public void setRunInParallel(final boolean runInParallel) {
		System.out.println("RunInParallel not supported under JUnit.");
	}

	@Override
	public void setExpectFailFast(final boolean failFast) {
		this.expectFailFast = failFast;
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

	public static class SkipException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public SkipException() {
			super("Pending");
		}

	}

	/**
	 * Marks the test as pending. Instructions after <code>pending()</code> will
	 * not be executed and TestNG marks the test as skipped.
	 */
	@Override
	public void pending() {
		throw new SkipException();
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
		return Intercept.intercept(exceptionType, throwing);
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
		return Intercept.intercept(exceptionType, messageRegex, throwing);
	}

	protected List<LambdaTestCase> getTestCases() {
		return testCases;
	}

	protected boolean getExpectFailFast() {
		return expectFailFast;
	}

}
