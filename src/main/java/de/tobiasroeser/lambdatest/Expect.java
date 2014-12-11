package de.tobiasroeser.lambdatest;

import java.util.Collections;
import java.util.List;

import de.tobiasroeser.lambdatest.internal.Util;
import de.tobiasroeser.lambdatest.testng.RunnableWithException;

/**
 * Idea for context (held in a threadlocal)
 * <ul>
 * <li>collect some test context (e.g. the source code) and when errors occur,
 * also output the failing code line
 * <li>continue with wrong expectations, e.g. collect more than one assertion
 * error/exception
 * </ul>
 */
public class Expect {

	private static ThreadLocal<ExpectContext> threadContext = new ThreadLocal<ExpectContext>();

	static ExpectContext __TEST_threadContext() {
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

	public static void expectEquals(final Object actual, final Object expected, final String msg) {
		try {
			Assert.assertEquals(actual, expected, msg);
		} catch (final AssertionError e) {
			final ExpectContext context = threadContext.get();
			if (context != null && context.getFailEarly()) {
				context.addAssertionError(e);
			} else {
				throw e;
			}
		}
	}

	public static void expectEquals(final Object actual, final Object expected) {
		expectEquals(actual, expected, null);
	}

	// public static void expectEquals(final boolean actual, final boolean
	// expected, final String msg) {
	// expectEquals(Boolean.valueOf(actual), Boolean.valueOf(expected), msg);
	// }
	//
	// public static void expectEquals(final boolean actual, final boolean
	// expected) {
	// expectEquals(actual, expected, null);
	// }

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
			if (context != null && context.getFailEarly()) {
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


}
