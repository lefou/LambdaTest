package de.tobiasroeser.lambdatest;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.Arrays;

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

		if (expected.getClass().isArray()) {
			if (!actual.getClass().isArray()) {
				fail(msg, "Expected an array, but got a {0}", actual.getClass().getName());
			}

			final int expectedLength = Array.getLength(expected);
			final int actualLength = Array.getLength(actual);
			if (expectedLength != actualLength) {
				fail(msg, "Array length of {0} does not match expected length of {1}. Expected {2} but was {3}",
						actualLength, expectedLength, expected, actual);
			}
			for (int i = 0; i < expectedLength; i++) {
				final Object exp = Array.get(expected, i);
				final Object act = Array.get(actual, i);
				try {
					assertEquals(act, exp);
				} catch (final AssertionError e) {
					fail(msg, "Arrays differ at index {0}. Expected {1} but was {2}. Element difference error: {3}", i,
							expected, actual, e.getMessage());
				}
			}
			return;
		}
		if (actual.getClass().isArray()) {
			fail(msg, "Got an array, but did not expected one. Expected a {0}", expected.getClass().getName());
		}

		if (!expected.equals(actual)) {
			fail(msg, "Actual {0} is not equal to {1}", actual, expected);
		}

		// TODO: array
		// TODO: Set
		// TODO: iterable
	}
}
