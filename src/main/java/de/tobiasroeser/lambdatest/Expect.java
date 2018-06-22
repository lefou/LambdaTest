package de.tobiasroeser.lambdatest;

import java.util.Collection;
import java.util.Map;

/**
 * Various assertion methods plus the ability to disable default fail-fast
 * behavior to collect as much assertion errors as possible.
 *
 * All expect-methods of this class support fail-late behavior.
 *
 */
public class Expect {

	/**
	 * Expect that the given actual value is `null`.
	 * 
	 * @param actual
	 *            The actual value to check.
	 * @param msg
	 *            The additional message to print in case of a failed
	 *            expectation.
	 * 
	 * @since 0.3.0
	 */
	public static void expectNull(final Object actual, final String msg) {
		try {
			Assert.assertNull(actual, msg);
		} catch (final AssertionError e) {
			ExpectContext.handleAssertionError(e);
		}
	}

	/**
	 * Expect that the given actual value is `null`.
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
	 * Expect that the given actual value is not `null`.
	 * 
	 * @param actual
	 *            The actual value to check.
	 * @param msg
	 *            The additional message to print in case of a failed
	 *            expectation.
	 * 
	 * @since 0.3.0
	 */
	public static void expectNotNull(final Object actual, final String msg) {
		try {
			Assert.assertNotNull(actual, msg);
		} catch (final AssertionError e) {
			ExpectContext.handleAssertionError(e);
		}
	}

	/**
	 * Expect that the given actual value is not `null`.
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
	 *            An additional message to output if the expectation failed.
	 */
	public static void expectEquals(final Object actual, final Object expected, final String msg) {
		try {
			Assert.assertEquals(actual, expected, msg);
		} catch (final AssertionError e) {
			ExpectContext.handleAssertionError(e);
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
			ExpectContext.handleAssertionError(e);
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
			ExpectContext.handleAssertionError(e);
		}
	}

	/**
	 * Expect that the given value is `true`.
	 * 
	 * @param actual
	 *            The value th check.
	 */
	public static void expectTrue(final boolean actual) {
		expectTrue(actual, null);
	}

	/**
	 * Expect that the given value is `false`.
	 * 
	 * @param actual
	 *            The value th check.
	 * @param msg
	 *            An additional message to output if the expectation failed.
	 */
	public static void expectFalse(final boolean actual, final String msg) {
		try {
			Assert.assertFalse(actual, msg);
		} catch (final AssertionError e) {
			ExpectContext.handleAssertionError(e);
		}
	}

	public static void expectFalse(final boolean actual) {
		expectFalse(actual, null);
	}

	/**
	 * Check for non-null {@link String} and provided further checks on the
	 * actual string in a fluent API.
	 *
	 * @see ExpectString
	 *
	 * @param actual
	 *            The string to check.
	 * @return A {@link ExpectString} to express further expectations on the
	 *         actual string.
	 */
	public static ExpectString expectString(final String actual) {
		return new ExpectString(actual);
	}

	/**
	 * Check for non-null {@link Collection} and provides further checks on the
	 * actual collection in a fluent API.
	 *
	 * @see ExpectCollection
	 *
	 * @param actual
	 *            The Collection<T> to check.
	 * @return A {@link ExpectCollection} to express further expectations on the
	 *         actual collection.
	 */
	public static <T> ExpectCollection<T> expectCollection(final Collection<T> actual) {
		return ExpectCollection.expectCollection(actual);
	}

	/**
	 * Check for non-null {@link Map} and provides further checks on the actual
	 * map in a fluent API.
	 *
	 * @see ExpectMap
	 *
	 * @param actual
	 *            The Map<K,V> to check.
	 * @return A {@link ExpectMap} to express further expectations on the actual
	 *         map.
	 */
	public static <K, V> ExpectMap<K, V> expectMap(final Map<K, V> actual) {
		return ExpectMap.expectMap(actual);
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
			final ExpectContext context = ExpectContext.threadContext();
			if (context != null && !context.getFailEarly()) {
				context.addAssertionError(e);
				// this throws in any case, but the compiler doesn't know
				ExpectContext.finish();
				// so we throw nevertheless in the outer block
			}
			throw e;
		}
	}

	// public static List<AssertionError> getContextErrors() {
	// final ExpectContext context = threadContext.get();
	// if (context != null) {
	// return context.getErrors();
	// } else {
	// return Collections.emptyList();
	// }
	// }

}
