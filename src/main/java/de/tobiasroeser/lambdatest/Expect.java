package de.tobiasroeser.lambdatest;

import java.util.Collections;
import java.util.List;

import de.tobiasroeser.lambdatest.internal.Util;

/**
 * Various assertion methods plus the ability to disable default fail-fast
 * behavior to collect as much assertion errors as possible.
 *
 * All expect-methods of this class support fail-late behavior.
 *
 */
public class Expect {

	private static ThreadLocal<ExpectContext> threadContext = new ThreadLocal<ExpectContext>();

	/* package */ static ExpectContext __TEST_threadContext() {
		return threadContext.get();
	}

	public static void setup(final boolean failEarly) {
		if (threadContext.get() != null) {
			System.out.println("Warning: Overriding already setup expect context");
		}
		threadContext.set(new ExpectContext(failEarly));
	}

	public static void finish() {
		final ExpectContext context = threadContext.get();
		threadContext.set(null);
		if (context != null) {
			final List<AssertionError> errors = context.getErrors();
			if (errors.isEmpty()) {
				return;
			} else if (errors.size() == 1) {
				throw errors.get(0);
			} else {
				// TODO: create a multi-Exception
				final List<String> formatted = Util.map(errors, error -> {
					final String msg = error.getClass().getName() + ": " + error.getMessage() + "\n\tat " +
							Util.mkString(error.getStackTrace(), "\n\tat ");
					return msg;
				});

				throw new AssertionError("" + errors.size()
						+ " expectations failed\n--------------------------------------------------\n" +
						Util.mkString(formatted, "\n\n") + "\n--------------------------------------------------");
			}
		}
	}

	public static void clear() {
		threadContext.set(null);
	}

	/**
	 * Expect that the given actual value is null.
	 * 
	 * @param actual
	 *            The actual value to check.
	 * @param msg
	 *            The message to print in case of a failed expectation.
	 * 
	 * @since 0.3.0
	 */
	public static void expectNull(final Object actual, String msg) {
		try {
			Assert.assertNull(actual, msg);
		} catch (AssertionError e) {
			handleAssertionError(e);
		}
	}

	/**
	 * Expect that the given actual value is null.
	 * 
	 * @param actual
	 *            The actual value to check.
	 * 
	 * @since 0.3.0
	 */
	public static void expectNull(final Object actual) {
		expectNull(actual, null);
	}

	/**
	 * Expect that the given actual value is not null.
	 * 
	 * @param actual
	 *            The actual value to check.
	 * @param msg
	 *            The message to print in case of a failed expectation.
	 * 
	 * @since 0.3.0
	 */
	public static void expectNotNull(final Object actual, String msg) {
		try {
			Assert.assertNull(actual, msg);
		} catch (AssertionError e) {
			handleAssertionError(e);
		}
	}

	/**
	 * Expect that the given actual value is not null.
	 * 
	 * @param actual
	 *            The actual value to check.
	 * 
	 * @since 0.3.0
	 */
	public static void expectNotNull(final Object actual) {
		expectNotNull(actual, null);
	}

	/**
	 * Check object equality.
	 *
	 * @param actual
	 *            The actual object.
	 * @param expected
	 *            The expected object.
	 * @param msg
	 *            A message to output if the expectation failed.
	 */
	public static void expectEquals(final Object actual, final Object expected, final String msg) {
		try {
			Assert.assertEquals(actual, expected, msg);
		} catch (final AssertionError e) {
			handleAssertionError(e);
		}
	}

	/**
	 * Check object equality.
	 *
	 * In case the expectation failed, it tried to provide detailed information
	 * about the differences.
	 *
	 * @param actual
	 *            The actual object.
	 * @param expected
	 *            The expected object.
	 */
	public static void expectEquals(final Object actual, final Object expected) {
		expectEquals(actual, expected, null);
	}

	public static void expectNotEquals(final Object actual, final Object expected, final String msg) {
		try {
			Assert.assertNotEquals(actual, expected, msg);
		} catch (final AssertionError e) {
			handleAssertionError(e);
		}
	}

	/**
	 * Check for non-equal objects.
	 *
	 * @param actual
	 *            The actual object.
	 * @param expected
	 *            The expected object.
	 */
	public static void expectNotEquals(final Object actual, final Object expected) {
		expectNotEquals(actual, expected, null);
	}

	public static void expectTrue(final boolean actual, final String msg) {
		try {
			Assert.assertTrue(actual, msg);
		} catch (final AssertionError e) {
			handleAssertionError(e);
		}
	}

	public static void expectTrue(final boolean actual) {
		expectTrue(actual, null);
	}

	public static void expectFalse(final boolean actual, final String msg) {
		try {
			Assert.assertFalse(actual, msg);
		} catch (final AssertionError e) {
			handleAssertionError(e);
		}
	}

	public static void expectFalse(final boolean actual) {
		expectFalse(actual, null);
	}

	/**
	 * Check for non-null {@link String} and provided further checks on the actual
	 * string in a fluent API.
	 *
	 * @see ExpectString
	 *
	 * @param actual
	 * @return A {@link ExpectString} to express further expectations on the actual
	 *         string.
	 */
	public static ExpectString expectString(final String actual) {
		return new ExpectString(actual);
	}

	public static <T extends Throwable> T intercept(final Class<T> exceptionType,
			final RunnableWithException throwing) throws Exception {
		return intercept(exceptionType, ".*", throwing);
	}

	public static <T extends Throwable> T intercept(final Class<T> exceptionType,
			final String messageRegex, final RunnableWithException throwing)
			throws Exception {
		try {
			return Intercept.intercept(exceptionType, messageRegex, throwing);
		} catch (final AssertionError e) {
			final ExpectContext context = threadContext.get();
			if (context != null && !context.getFailEarly()) {
				context.addAssertionError(e);
				// this throws in any case, but the compiler doesn't know
				finish();
				// so we throw nevertheless in the outer block
			}
			throw e;
		}
	}

	public static List<AssertionError> getContextErrors() {
		final ExpectContext context = threadContext.get();
		if (context != null) {
			return context.getErrors();
		} else {
			return Collections.emptyList();
		}
	}

	private static void handleAssertionError(AssertionError e) {
		final ExpectContext context = threadContext.get();
		if (context != null && !context.getFailEarly()) {
			context.addAssertionError(e);
		} else {
			throw e;
		}
	}

}
