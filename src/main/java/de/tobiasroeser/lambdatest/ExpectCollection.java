package de.tobiasroeser.lambdatest;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tobiasroeser.lambdatest.internal.Util;

/**
 * Check for non-null {@link Collection} and provides further checks on the
 * actual collection in a fluent API.
 *
 * @param <T>
 *            The type of the collection elements.
 */
public class ExpectCollection<T> extends ExpectBase<ExpectCollection<T>> {

	/**
	 * Check for non-null {@link Collection} and provides further checks on the
	 * actual collection in a fluent API.
	 *
	 * @see ExpectCollection
	 *
	 * @param actual
	 *            The Collection to check.
	 * @return A {@link ExpectCollection} to express further expectations on the
	 *         actual collection.
	 */
	public static <T> ExpectCollection<T> expectCollection(final Collection<T> actual) {
		return new ExpectCollection<>(actual);
	}

	private final Collection<T> actual;

	/**
	 * Creates an instance for the non-null collection `actual`.
	 *
	 * @param actual
	 *            The Collection to check.
	 */
	public ExpectCollection(final Collection<T> actual) {
		check(actual != null, "Actual is not a Collection but null.");
		this.actual = actual;
	}

	public ExpectCollection<T> isEmpty() {
		return check(actual.isEmpty(), "Actual collection is not empty but has a size of {0}.\nActual: {1}",
				actual.size(), actual);
	}

	/**
	 * Checks, that the collection has the expected site.
	 * 
	 * @param expectedSize
	 */
	public ExpectCollection<T> hasSize(final int expectedSize) {
		if (expectedSize < 0) {
			throw new IllegalArgumentException("Parameter `count` must be not negative");
		}
		return check(actual.size() == expectedSize,
				"Actual collection has not expected size of {0}, actual size: {1}.\nActual: {2}",
				expectedSize, actual.size(), actual);
	}

	/**
	 * Check, that the collection has `count` duplicates.
	 * 
	 * @param expectedCount
	 *            The number of expected duplicates.
	 */
	public ExpectCollection<T> hasDuplicates(final int expectedCount) {
		if (expectedCount < 0) {
			throw new IllegalArgumentException("Parameter `count` must be not negative");
		}
		final HashSet<T> set = new LinkedHashSet<>(actual);
		final int duplicates = actual.size() - set.size();
		return check(duplicates == expectedCount,
				"Actual collection has not the expected count of duplicates of {0}, actual: {1}",
				expectedCount, duplicates);
	}

	/**
	 * Check, that the collection has no duplicates.
	 */
	public ExpectCollection<T> hasNoDuplicates() {
		final HashSet<T> set = new LinkedHashSet<>(actual);
		final boolean cond = actual.size() == set.size();
		if (cond) {
			return this;
		}

		final List<String> duplicatesAsString = new LinkedList<>();
		final Set<T> seen = new LinkedHashSet<>();

		int pos = 0;
		for (final T e : actual) {
			if (seen.contains(e)) {
				duplicatesAsString.add("[" + pos + "] = " + e);
			} else {
				seen.add(e);
			}
			pos++;
		}

		return check(false, "Actual collection has duplicates: ", Util.mkString(duplicatesAsString, "{", ",", "}"));
	}

	/**
	 * Checks, that the collection contains the given element.
	 * 
	 * @param element
	 *            The element that must be contained in the collection.
	 */
	public ExpectCollection<T> contains(final T element) {
		return check(actual.contains(element),
				"Actual collection does not contain expected element \"{0}\", actual: \"{1}\"", element, actual);
	}

	/**
	 * Same as {@link #contains(Object)}, but not using {@link #equals(Object)}
	 * to compare the contained elements but the `==` operation.
	 * 
	 * @param element
	 *            The element that must be contained in the collection.
	 */
	public ExpectCollection<T> containsIdentical(final T element) {
		return check(Util.exists(actual, e -> e == element),
				"Actual collection does not contain expected element \"{0}\", actual: \"{1}\"", element, actual);
	}

	/**
	 * Checks, that the collection does not contain the given element.
	 * 
	 * @param fragment
	 *            The element that must be not contained in the collection.
	 */
	public ExpectCollection<T> containsNot(final T fragment) {
		return check(!actual.contains(fragment),
				"Actual must not contain expected element \"{0}\", actual: \"{1}\"", fragment, actual);
	}

	/**
	 * Same as {@link #containsNot(Object)}, but not using
	 * {@link #equals(Object)} to compare the contained elements but the `==`
	 * operation.
	 * 
	 * @param element
	 *            The element that must be contained in the collection.
	 */
	public ExpectCollection<T> containsNotIdentical(final T element) {
		return check(Util.forall(actual, e -> e != element),
				"Actual must not contain expected element \"{0}\", actual: \"{1}\"", element, actual);
	}

}
