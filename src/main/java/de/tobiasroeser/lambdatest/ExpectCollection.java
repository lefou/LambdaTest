package de.tobiasroeser.lambdatest;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tobiasroeser.lambdatest.internal.Util;

public class ExpectCollection<T> extends ExpectBase<ExpectCollection<T>> {
	final private Collection<T> actual;

	public ExpectCollection(final Collection<T> actual) {
		check(actual != null, "Actual is not a String but null");
		this.actual = actual;
	}

	public static <T> ExpectCollection<T> expectCollection(Collection<T> actual){
		return new ExpectCollection<T>(actual);
	}
	
	public ExpectCollection<T> isEmpty() {
		return check(actual.isEmpty(), "Is not empty and has a size of {0}.", actual.size());
	}
	public ExpectCollection<T> hasSize(int expected) {
		return check(actual.size() == expected, "Actual has not a size of {0}, actual: {1}", expected, actual.size());
	}

	public ExpectCollection<T> hasNoDuplicates() {
		final HashSet<T> set = new HashSet<>(actual);
		final boolean cond = actual.size() == set.size();
		if(cond) return this;

		final List<String> duplicatesAsString= new LinkedList<>();
		final Set<T> seen = new HashSet<>();

		int pos = 0;
		for(T e : actual) {
			if(seen.contains(e)) {
				duplicatesAsString.add("["+pos+ "] = " + e );
			} else {
				seen.add(e);
			}
			pos++;
		}

		return check(false, "Actual has duplicates:", Util.mkString(duplicatesAsString,"{",",","}"));
	}

	public ExpectCollection<T> contains(final T fragment) {
		return check(actual.contains(fragment), "Actual does not contain element \"{0}\", actual: \"{1}\"", fragment,
				actual);
	}

	public ExpectCollection<T> containsNot(final T fragment) {
		return check(!actual.contains(fragment), "Actual must not contain element \"{0}\", actual: \"{1}\"", fragment,
				actual);
	}
	
	
	
}
