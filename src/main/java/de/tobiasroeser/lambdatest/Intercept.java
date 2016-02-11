package de.tobiasroeser.lambdatest;

import java.util.regex.Pattern;

public class Intercept {

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
	public static <T extends Throwable> T intercept(final Class<T> exceptionType,
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
	public static <T extends Throwable> T intercept(final Class<T> exceptionType,
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
					throw new AssertionError(
							"Exception was thrown with the wrong message: Expected: '" + messageRegex
									+ "' but got '" + msg + "'.", e);
				}
			}
			throw new AssertionError("Thrown exception of type [" + exceptionType.getName()
					+ "] does not match expected type [" + e.getClass().getName() + "]", e);
		}
		throw new AssertionError("Expected exception of type [" + exceptionType.getName() + "] was not thrown");
	}

}
