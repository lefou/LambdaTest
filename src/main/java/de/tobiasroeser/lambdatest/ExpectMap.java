package de.tobiasroeser.lambdatest;

import java.util.Map;

public class ExpectMap<K,V> extends ExpectBase<ExpectMap<K,V>> {
	final private Map<K,V> actual;

	public ExpectMap(final Map<K,V> actual) {
		check(actual != null, "Actual is not a Map but null");
		this.actual = actual;
	}

	public static <K,V> ExpectMap<K,V> expectMap(Map<K,V> actual) {
		return new ExpectMap<>(actual);
	}

	public ExpectMap<K,V> isEmpty() {
		return check(actual.isEmpty(), "Actual map is not empty but has a size of {0}.", actual.size());
	}

	public ExpectMap<K,V> hasSize(int expected) {
		return check(actual.size() == expected, "Actual map has not expected size of {0}, actual: {1}", expected,
				actual.size());
	}


	public ExpectMap<K,V> containsKey(final K key) {
		return check(actual.containsKey(key), "Actual collection does not contain expected element \"{0}\", actual: \"{1}\"", key,
				actual);
	}

	public ExpectMap<K,V> containsNotKey(final K key) {
		return check(!actual.containsKey(key), "Actual must not contain expected element \"{0}\", actual: \"{1}\"", key,
				actual);
	}

}
