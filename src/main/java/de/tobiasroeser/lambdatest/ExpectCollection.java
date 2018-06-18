package de.tobiasroeser.lambdatest;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tobiasroeser.lambdatest.internal.Util;

public class ExpectCollection<T> extends ExpectBase<ExpectCollection<T>> {
	final private Collection<T> actual;

	public ExpectCollection(final Collection<T> actual) {
		check(actual != null, "Actual is not a Collection but null");
		this.actual = actual;
	}

	public static <T> ExpectCollection<T> expectCollection(Collection<T> actual) {
		return new ExpectCollection<T>(actual);
	}

	public ExpectCollection<T> isEmpty() {
		return check(actual.isEmpty(), "Actual collection is not empty but has a size of {0}.", actual.size());
	}

	public ExpectCollection<T> hasSize(int expectedCount) {
		if (expectedCount < 0) {
			throw new IllegalArgumentException("Parameter `count` must be not negative");
		}
		return check(actual.size() == expectedCount,
				"Actual collection has not expected size of {0}, actual: {1}", expectedCount, actual.size());
	}

	/**
	 * Check, that the collection has `count` duplicates.
	 * 
	 * @param expectedCount
	 *            The number of expected duplicates.
	 * @return
	 */
	public ExpectCollection<T> hasDuplicates(int expectedCount) {
		if (expectedCount < 0) {
			throw new IllegalArgumentException("Parameter `count` must be not negative");
		}
		final HashSet<T> set = new LinkedHashSet<>(actual);
		final int duplicates = actual.size() - set.size();
		return check(duplicates == expectedCount,
				"Actual collection has not the expected count of duplicates of {0}, actual: {1}",
				expectedCount, duplicates);
	}

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

	public ExpectCollection<T> contains(final T fragment) {
		return check(actual.contains(fragment),
				"Actual collection does not contain expected element \"{0}\", actual: \"{1}\"", fragment, actual);
	}

	/**
	 * Same as {@link #contains(Object)}, but not using {@link #equals(Object)}
	 * to compare the contained elements but the `==` operation.
	 * 
	 * @param element
	 *            The element that must be contained in the collection.
	 * @return
	 */
	public ExpectCollection<T> containsIdentical(final T element) {
		return check(Util.exists(actual, e -> e == element),
				"Actual collection does not contain expected element \"{0}\", actual: \"{1}\"", element, actual);
	}

	public ExpectCollection<T> containsNot(final T fragment) {
		return check(!actual.contains(fragment), "Actual must not contain expected element \"{0}\", actual: \"{1}\"", fragment,
				actual);
	}

}
