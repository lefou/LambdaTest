package de.tobiasroeser.lambdatest;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.tobiasroeser.lambdatest.internal.Util;

/**
 * Idea for context (held in a threadlocal)
 * <ul>
 * <li>collect some test context (e.g. the source code) and when errors occur,
 * also output the failing code line
 * <li>continue with wrong expectations, e.g. collect more than one assertion
 * error/exception
 * </ul>
 */
public class Assert {

	private static void fail(final String msgOrNull, final String fallBackMsg, final Object... args) {
		final String msg;
		if (msgOrNull != null) {
			msg = msgOrNull;
		} else if (args == null || args.length == 0) {
			msg = fallBackMsg;
		} else {
			final Object[] niceArgs = new Object[args.length];
			for (int i = 0; i < args.length; ++i) {
				final Object arg = args[i];
				if (arg != null && arg.getClass().isArray()) {
					niceArgs[i] = Util.mkString(Arrays.asList((Object[]) arg), "[", ",", "]");
				} else {
					niceArgs[i] = arg;
				}
			}
			msg = MessageFormat.format(fallBackMsg, niceArgs);
		}
		throw new AssertionError(msg);
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

		if (expected.getClass().isArray()) {
			if (!actual.getClass().isArray()) {
				fail(msg, "Expected an array, but got a {0}", actual.getClass().getName());
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
		if (actual.getClass().isArray()) {
			fail(msg, "Got an array, but did not expected one. Expected a {0}", expected.getClass().getName());
		}

		// now check for equality, and if not introspect further
		if (expected.equals(actual)) {
			return;
		}

		// now we know actual and expected differ
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
			final List<?> missingInActual = Util.filter((Iterable<?>) expected, exp -> ((Set<?>) actual).contains(exp));
			final List<?> spareInActual = Util.filter((Iterable<?>) actual, act -> ((Set<?>) expected).contains(act));
			fail(msg,
					"Sets are not equal. Expected {0} but was {1}. Expected elements missing in actual set: {2}. Unexpected elements in actual set: {3}",
					expected, actual,
					Util.mkString(missingInActual, "[", ",", "]"),
					Util.mkString(spareInActual, "[", ",", "]"));
		}

		if (expected instanceof Iterable<?> && actual instanceof Iterable<?>) {
			itName = itName != null ? itName : "Iterables";
			expIt = ((Iterable<?>) expected).iterator();
			actIt = ((Iterable<?>) actual).iterator();
		}
		else if (expected instanceof Iterator<?> && actual instanceof Iterator<?>) {
			itName = itName != null ? itName : "Iterators";
			expIt = (Iterator<?>) expected;
			actIt = (Iterator<?>) actual;
		}
		else if (expected instanceof Enumeration<?> && actual instanceof Enumeration<?>) {
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

		fail(msg, "Actual {0} is not equal to {1}", actual, expected);
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
}
