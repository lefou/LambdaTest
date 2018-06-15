package de.tobiasroeser.lambdatest;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;
import static de.tobiasroeser.lambdatest.Expect.expectNotEquals;

/**
 * Various self descriptive methods to check properties of a given string.
 *
 */
public class ExpectString extends ExpectBase<ExpectString> {

	private String actual;

	public ExpectString(final String actual) {
		check(actual != null, "Actual is not a String but null");
		this.actual = actual;
	}

	public ExpectString isEqual(final String expected) {
		expectEquals(actual, expected);
		return this;
	}

	public ExpectString isNotEqual(final String expected) {
		expectNotEquals(actual, expected);
		return this;
	}

	public ExpectString isEqualIgnoreCase(final String expected) {
		return check(actual.equalsIgnoreCase(expected),
				"Actual string is not equal to \"{0}\" (ignore case), actual: \"{1}\"",
				expected, actual);
	}

	public ExpectString isNotEqualIgnoreCase(final String expected) {
		return check(!actual.equalsIgnoreCase(expected),
				"Actual string must not be equal to \"{0}\" (ignore case), actual: \"{1}\"", expected,
				actual);
	}

	public ExpectString startsWith(final String prefix) {
		return check(actual.startsWith(prefix), "Actual string does not start with \"{0}\", actual: \"{1}\"", prefix,
				actual);
	}

	public ExpectString startsWithNot(final String prefix) {
		return check(!actual.startsWith(prefix), "Actual string must not start with \"{0}\", actual: \"{1}\"", prefix,
				actual);
	}

	public ExpectString endsWith(final String suffix) {
		return check(actual.endsWith(suffix), "Actual string does not end with \"{0}\", actual: \"{1}\"", suffix,
				actual);
	}

	public ExpectString endsWithNot(final String suffix) {
		return check(!actual.endsWith(suffix), "Actual string must not end with \"{0}\", actual: \"{1}\"", suffix,
				actual);
	}

	public ExpectString matches(final String regex) {
		return check(actual.matches(regex), "Actual string does not match regular expression \"{0}\", actual: \"{1}\"",
				regex,
				actual);
	}

	public ExpectString matchesNot(final String regex) {
		return check(!actual.matches(regex), "Actual string must not match regular expression \"{0}\", actual: \"{1}\"",
				regex,
				actual);
	}

	public ExpectString hasLength(final int length) {
		return check(actual.length() == length, "Actual string has not a length of {0}, actual: {1}", length,
				actual.length());
	}

	public ExpectString hasLengthNot(final int length) {
		return check(actual.length() != length, "Actual string must not have a length of {0}, actual: {1}", length,
				actual.length());
	}

	public ExpectString isLongerThan(final int length) {
		return check(actual.length() > length, "Actual string is not longer than {0}, actual: {1}", length,
				actual.length());
	}

	public ExpectString isShorterThan(final int length) {
		return check(actual.length() < length, "Actual string is not shorter than {0}, actual: {1}", length,
				actual.length());
	}

	public ExpectString isTrimmed() {
		return check(actual.equals(actual.trim()), "Actual string is not trimmed, actual: {0}", actual.length());
	}

	public ExpectString contains(final String fragment) {
		return check(actual.contains(fragment), "Actual string does not contain fragment \"{0}\", actual: \"{1}\"",
				fragment,
				actual);
	}

	public ExpectString containsNot(final String fragment) {
		return check(!actual.contains(fragment), "Actual string must not contain fragment \"{0}\", actual: \"{1}\"",
				fragment,
				actual);
	}

	public ExpectString containsIgnoreCase(final String fragment) {
		return check(actual.toLowerCase().contains(fragment.toLowerCase()),
				"Actual string does not contain fragment \"{0}\" (ignore case), actual: \"{1}\"", fragment,
				actual);
	}

	public ExpectString containsIgnoreCaseNot(final String fragment) {
		return check(!actual.toLowerCase().contains(fragment.toLowerCase()),
				"Actual string must not contain fragment \"{0}\" (ignore case), actual: \"{1}\"", fragment,
				actual);
	}

}
