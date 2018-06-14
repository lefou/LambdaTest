package de.tobiasroeser.lambdatest;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tobiasroeser.lambdatest.internal.LoggerFactory;
import de.tobiasroeser.lambdatest.internal.Util;

/**
 * Holds various assert-methods.
 * 
 * @see Expect
 *
 */
public class Assert {

	private static final List<Class<?>> LONG_TYPES = Arrays.asList(Byte.class, Short.class, Integer.class, Long.class);
	private static final List<Class<?>> DOUBLE_TYPES = Arrays.asList(Float.class, Double.class);

	private static void fail(final String userGivenMessageOrNull, final String msg, final Object... args) {
		final String formatted;
		if (args == null || args.length == 0) {
			formatted = msg;
		} else {
			final Object[] niceArgs = new Object[args.length];
			for (int i = 0; i < args.length; ++i) {
				final Object arg = args[i];
				final Class<? extends Object> argClass = arg.getClass();
				if (arg != null && argClass.isArray()) {
					List<Object> argObjects = new LinkedList<Object>();
					if (argClass.equals(boolean[].class)) {
						for (final boolean b : (boolean[]) arg) {
							argObjects.add(Boolean.valueOf(b).toString());
						}
					} else if (argClass.equals(byte[].class)) {
						for (final byte b : (byte[]) arg) {
							argObjects.add(Byte.valueOf(b).toString());
						}
					} else if (argClass.equals(short[].class)) {
						for (final short b : (short[]) arg) {
							argObjects.add(Short.valueOf(b).toString());
						}
					} else if (argClass.equals(int[].class)) {
						for (final int b : (int[]) arg) {
							argObjects.add(Integer.valueOf(b).toString());
						}
					} else if (argClass.equals(long[].class)) {
						for (final long b : (long[]) arg) {
							argObjects.add(Long.valueOf(b).toString());
						}
					} else if (argClass.equals(float[].class)) {
						for (final float b : (float[]) arg) {
							argObjects.add(Float.valueOf(b).toString());
						}
					} else if (argClass.equals(double[].class)) {
						for (final double b : (double[]) arg) {
							argObjects.add(Double.valueOf(b).toString());
						}
					} else if (argClass.equals(char[].class)) {
						for (final char b : (char[]) arg) {
							argObjects.add(Character.valueOf(b).toString());
						}
					} else {
						argObjects = Arrays.asList((Object[]) arg);
					}
					niceArgs[i] = Util.mkString(argObjects, "[", ",", "]");
				} else {
					niceArgs[i] = arg;
				}
			}
			formatted = MessageFormat.format(msg, niceArgs);
		}
		final String finalMsg;
		if (userGivenMessageOrNull == null) {
			finalMsg = formatted;
		} else {
			finalMsg = userGivenMessageOrNull + " -- Details: " + formatted;
		}
		LoggerFactory.getLogger(Assert.class).error("Assertion failed: {}", finalMsg);
		throw new AssertionError(finalMsg);
	}

	// TODO: add asserts for all primitive parameter types

	public static void assertEquals(final Object actual, final Object expected) {
		assertEquals(actual, expected, null);
	}

	public static void assertEquals(final Object actual, final Object expected, final String msg) {
		if (actual == expected) {
			return;
		}
		if (actual == null) {
			fail(msg, "Actual was null but expected: {0}", expected);
		}
		if (expected == null) {
			fail(msg, "Expected null but was: {0}", actual);
		}

		// from here on, actual and expected are not null

		final Class<? extends Object> actualClass = actual.getClass();
		final Class<? extends Object> expectedClass = expected.getClass();

		if (expectedClass.isArray()) {
			if (!actualClass.isArray()) {
				fail(msg, "Expected an array, but got a {0}", actualClass.getName());
			}

			final int expectedLength = Array.getLength(expected);
			final int actualLength = Array.getLength(actual);
			if (expectedLength != actualLength) {
				fail(msg, "Actual array length of {0} does not match expected length of {1}. Expected {2} but was {3}",
						actualLength, expectedLength, expected, actual);
			}
			for (int i = 0; i < expectedLength; i++) {
				final Object exp = Array.get(expected, i);
				final Object act = Array.get(actual, i);
				try {
					assertEquals(act, exp);
				} catch (final AssertionError e) {
					fail(msg,
							"Arrays differ at index {0}. Expected {1} but was {2}. Error for element at index {0}: {3}",
							i,
							expected, actual, e.getMessage());
				}
			}
			return;
		}

		// expected in not an array but actual is
		if (actualClass.isArray()) {
			fail(msg, "Got an array, but did not expected one. Expected a {0}", expectedClass.getName());
		}

		// now check for equality, and if not introspect further
		if (expected.equals(actual)) {
			return;
		}

		// now we know actual and expected differ

		if (expected instanceof String && actual instanceof String) {
			final char[] expChars = ((String) expected).toCharArray();
			final char[] actChars = ((String) actual).toCharArray();
			if (expChars.length > 0) {
				for (int i = 0; i < expChars.length; ++i) {
					if (actChars.length > i) {
						if (expChars[i] != actChars[i]) {
							final String expectedWithMarker = ((String) expected).substring(0, i) + "[*]"
									+ ((String) expected).substring(i);
							final String actualWithMarker = ((String) actual).substring(0, i) + "[*]"
									+ ((String) actual).substring(i);
							fail(msg, "Strings differ at index {0} (see [*] marker). Expected \"{1}\" but was \"{2}\".",
									i, expectedWithMarker, actualWithMarker);
						}
					} else {
						fail(msg, "Strings differ at index {0}. Actual is too short. Expected \"{1}\" but was \"{2}\".",
								i, expected, actual);
					}
				}
			}
			if (expChars.length < actChars.length) {
				fail(msg, "Strings differ at index {0}. Actual is too long. Expected \"{1}\" but was \"{2}\".",
						expChars.length, expected, actual);
			}
		}
		// Handle true and false values without any further toString examination
		if (Boolean.class.isAssignableFrom(expectedClass) && Boolean.class.isAssignableFrom(actualClass)) {
			fail(msg, "Actual {0} is not equal to {1}", actual, expected);
		}

		// we try to analyze some kind of collections and iterators

		String itName = null;
		Iterator<?> expIt = null;
		Iterator<?> actIt = null;

		// collections know their size up front
		if (expected instanceof Collection<?> && actual instanceof Collection<?>) {
			itName = itName != null ? itName : "Collections";
			final int expectedLength = ((Collection<?>) expected).size();
			final int actualLength = ((Collection<?>) actual).size();
			if (expectedLength != actualLength) {
				fail(msg,
						"Actual collection length of {0} does not match expected length of {1}. Expected {2} but was {3}",
						actualLength, expectedLength, expected, actual);
			}
		}

		if (expected instanceof Set<?> && actual instanceof Set<?>) {
			// we know they are not equal but have same size, so it is enough to
			// find the diff candidates
			final List<?> missingInActual = Util.filter((Iterable<?>) expected,
					exp -> !((Set<?>) actual).contains(exp));
			final List<?> spareInActual = Util.filter((Iterable<?>) actual,
					act -> !((Set<?>) expected).contains(act));
			if (missingInActual.size() == ((Set<?>) expected).size()) {
				fail(msg, "Sets are not equal. All elements differ. Expected {0} but was {1}",
						expected, actual);
			}
			fail(msg,
					"Sets are not equal. Expected {0} but was {1}. {2} expected elements missing in actual set: {3}. {4} unexpected elements in actual set: {5}",
					expected,
					actual,
					missingInActual.size(),
					Util.mkString(missingInActual, "[", ",", "]"),
					spareInActual.size(),
					Util.mkString(spareInActual, "[", ",", "]"));
		}

		if (expected instanceof Iterable<?> && actual instanceof Iterable<?>) {
			itName = itName != null ? itName : "Iterables";
			expIt = ((Iterable<?>) expected).iterator();
			actIt = ((Iterable<?>) actual).iterator();
		} else if (expected instanceof Iterator<?> && actual instanceof Iterator<?>) {
			itName = itName != null ? itName : "Iterators";
			expIt = (Iterator<?>) expected;
			actIt = (Iterator<?>) actual;
		} else if (expected instanceof Enumeration<?> && actual instanceof Enumeration<?>) {
			itName = itName != null ? itName : "Enumerations";
			expIt = new Iterator<Object>() {
				@Override
				public boolean hasNext() {
					return ((Enumeration<?>) expected).hasMoreElements();
				}

				@Override
				public Object next() {
					return ((Enumeration<?>) expected).nextElement();
				}
			};
			actIt = new Iterator<Object>() {
				@Override
				public boolean hasNext() {
					return ((Enumeration<?>) actual).hasMoreElements();
				}

				@Override
				public Object next() {
					return ((Enumeration<?>) actual).nextElement();
				}
			};
		}

		if (expIt != null && actIt != null && itName != null) {
			int i = -1;
			while (expIt.hasNext() && actIt.hasNext()) {
				i++;
				final Object exp = expIt.next();
				final Object act = actIt.next();
				try {
					assertEquals(act, exp);
				} catch (final AssertionError e) {
					fail(msg, "{0} differ at index {1}. Expected {2} but was {3}. Error for element at index {1}: {4}",
							itName, i,
							expected, actual, e.getMessage());
				}
			}
		}

		// compare the types
		if (!actualClass.equals(expectedClass)) {
			if (LONG_TYPES.contains(actualClass) && LONG_TYPES.contains(expectedClass)
					&& ((Number) actual).longValue() == ((Number) expected).longValue()) {
				fail(msg, "Actual {0} of type {1} is not equal to {2} of type {3}, but their long values are equal.",
						actual.toString(), actualClass.getName(),
						expected.toString(), expectedClass.getName());
			}

			if (DOUBLE_TYPES.contains(actualClass) && DOUBLE_TYPES.contains(expectedClass)
					&& ((Number) actual).doubleValue() == ((Number) expected).doubleValue()) {
				fail(msg, "Actual {0} of type {1} is not equal to {2} of type {3}, but their double values are equal.",
						actual.toString(), actualClass.getName(),
						expected.toString(), expectedClass.getName());
			}

			fail(msg, "Actual {0} of type {1} is not equal to {2} of type {3}.",
					actual.toString(), actualClass.getName(),
					expected.toString(), expectedClass.getName());
		}

		// also try to make a toString() output comparison,
		// but only if these are not simple numbers
		if (!LONG_TYPES.contains(expectedClass) && !LONG_TYPES.contains(actualClass)) {
			try {
				assertEquals(actual.toString(), expected.toString());
			} catch (final AssertionError e) {
				fail(msg, "Actual {0} is not equal to {1}. Also their toString() differ: {2}",
						actual.toString(), expected.toString(),
						e.getMessage());
			}
		}

		fail(msg, "Actual {0} is not equal to {1}.", actual, expected);
	}

	public static void assertNotEquals(final Object actual, final Object expected) {
		assertNotEquals(actual, expected, null);
	}

	public static void assertNotEquals(final Object actual, final Object expected, final String msg) {
		try {
			assertEquals(actual, expected);
		} catch (final AssertionError e) {
			return;
		}
		fail(msg, "Actual {0} is equal to {1} but shouldn't.", actual, expected);
	}

	public static void assertTrue(final boolean actual, final String msg) {
		if (!actual) {
			fail(msg, "Actual {0} is not true", actual);
		}
	}

	public static void assertTrue(final boolean actual) {
		assertTrue(actual, null);
	}

	public static void assertFalse(final boolean actual, final String msg) {
		if (actual) {
			fail(msg, "Actual {0} is not false", actual);
		}
	}

	public static void assertFalse(final boolean actual) {
		assertFalse(actual, null);
	}

	/**
	 * Assert that the given actual value is null.
	 * 
	 * @param actual
	 *            The actual value to check.
	 * @param msg
	 *            The message to print in case of a failed assertion.
	 * 
	 * @since 0.3.0
	 */
	public static void assertNull(final Object actual, final String msg) {
		if (actual != null) {
			fail(msg, "Actual [{0}] should be null", actual);
		}
	}

	/**
	 * Assert that the given actual value is null.
	 * 
	 * @param actual
	 *            The actual value to check.
	 * 
	 * @since 0.3.0
	 */
	public static void assertNull(final Object actual) {
		assertNull(actual, null);
	}

	/**
	 * Assert that the given actual value is not null.
	 * 
	 * @param actual
	 *            The actual value to check.
	 * @param msg
	 *            The message to print in case of a failed assertion.
	 * 
	 * @since 0.3.0
	 */
	public static void assertNotNull(final Object actual, final String msg) {
		if (actual == null) {
			fail(msg, "Actual should be not null");
		}
	}

	/**
	 * Assert that the given actual value is not null.
	 * 
	 * @param actual
	 *            The actual value to check.
	 * 
	 * @since 0.3.0
	 */
	public static void assertNotNull(final Object actual) {
		assertNotNull(actual, null);
	}
}
