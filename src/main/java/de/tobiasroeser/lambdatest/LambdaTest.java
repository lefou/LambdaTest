package de.tobiasroeser.lambdatest;

import java.util.regex.Pattern;

/**
 * Common interface shared between all supported test suites.
 * 
 * @see de.tobiasroeser.lambdatest.junit.FreeSpec
 * @see de.tobiasroeser.lambdatest.testng.FreeSpec
 *
 */
public interface LambdaTest {

	/**
	 * Marks the test as pending. Instructions after `pending()` will not be
	 * executed.
	 */
	void pending();

	/**
	 * Marks the test as pending and uses the given `reason` as message.
	 * Instructions after `pending()` will not be executed.
	 */
	void pending(String reason);

	/**
	 * Intercept exceptions of type `exceptionType` and fail if no such
	 * exception or an exception with an incompatible type was thrown.
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
	<T extends Throwable> T intercept(Class<T> exceptionType,
			RunnableWithException throwing) throws Exception;

	/**
	 * Intercept exceptions of type `exceptionType` and fail if no such
	 * exception or an exception with an incompatible type was thrown or it the
	 * message does not match a given pattern.
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
	<T extends Throwable> T intercept(Class<T> exceptionType,
			String messageRegex, RunnableWithException throwing)
			throws Exception;

	/**
	 * If `true` tests in this suite can be run in parallel.
	 * 
	 * @param runInParallel
	 *            If `true`, tests may execute in parallel.
	 * 
	 */
	void setRunInParallel(boolean runInParallel);

	/**
	 * If `true`, the first failed assertion will also fail the test. If
	 * `false`, the test runs as long as possible, potentially collection
	 * multipe failed assertions.
	 * 
	 * @param failFast
	 *            If `true`, to fail fast.
	 */
	void setExpectFailFast(boolean failFast);

	/**
	 * Set the {@link Reporter} to be used.
	 * 
	 * @param reporter
	 *            The {@link Reporter} to be used.
	 */
	void setReporter(Reporter reporter);

	/**
	 * Gets the currently used reporter.
	 */
	Reporter getReporter();

}
