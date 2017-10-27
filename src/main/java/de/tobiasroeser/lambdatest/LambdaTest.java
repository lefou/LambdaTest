package de.tobiasroeser.lambdatest;

import java.util.regex.Pattern;

public interface LambdaTest {

	/**
	 * Marks the test as pending. Instructions after <code>pending()</code> will
	 * not be executed.
	 */
	void pending();

	/**
	 * Marks the test as pending and uses the given <code>reason</code> as
	 * message. Instructions after <code>pending()</code> will not be executed.
	 */
	void pending(String reason);

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
	<T extends Throwable> T intercept(Class<T> exceptionType,
			RunnableWithException throwing) throws Exception;

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
	<T extends Throwable> T intercept(Class<T> exceptionType,
			String messageRegex, RunnableWithException throwing)
			throws Exception;

	void setRunInParallel(boolean runInParallel);

	void setExpectFailFast(boolean failFast);

	void setReporter(Reporter reporter);
	
	Reporter getReporter();
	
}
