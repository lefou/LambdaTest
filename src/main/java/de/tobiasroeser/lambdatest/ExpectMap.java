package de.tobiasroeser.lambdatest;

import java.util.Map;
import java.util.Map.Entry;

import de.tobiasroeser.lambdatest.internal.Util;

/**
 * Check for non-null {@link Map} and provides methods on the actual map in a
 * fluent API.
 *
 * @param <K> The key type of the enclosing map.
 * @param <V> The value type of the enclosing map.
 */
public class ExpectMap<K, V> extends ExpectBase<ExpectMap<K, V>> {
    /**
     * Check for non-null {@link Map} and provides further checks on the actual
     * map in a fluent API.
     *
     * @param actual The Map to check.
     * @return A {@link ExpectMap} to express further expectations on the actual
     * map.
     * @see ExpectMap
     */
    public static <K, V> ExpectMap<K, V> expectMap(final Map<K, V> actual) {
        return new ExpectMap<>(actual);
    }

    private final Map<K, V> actual;

    /**
     * Creates and instance for the given non-null map.
     *
     * @param actual The non-null map.
     */
    public ExpectMap(final Map<K, V> actual) {
        check(actual != null, "Actual is not a Map but null");
        this.actual = actual;
    }

    /**
     * Checks, if the map is empty.
     */
    public ExpectMap<K, V> isEmpty() {
        return check(actual.isEmpty(), "Actual map is not empty but has a size of {0}.", actual.size());
    }

    /**
     * Checks, if the map has the expected size.
     *
     * @param expected The expected size of the map.
     */
    public ExpectMap<K, V> hasSize(final int expected) {
        return check(actual.size() == expected, "Actual map has not expected size of {0}, actual: {1}", expected,
                actual.size());
    }

    /**
     * Checks, if the map contains an entry with the given `key` and `value`.
     *
     * @param key   The key that must be contained in the map.
     * @param value The values that must be belong to the key.
     */
    public ExpectMap<K, V> contains(final K key, final V value) {
        return check(Util.find(actual.entrySet(), e -> ((key != null && key.equals(e.getKey())) || key == e.getKey()) &&
                        ((value != null && value.equals(e.getValue())) || value == e.getValue())).isDefined(),
                "Actual collection does not contain expected entry [{0} -> {1}], actual: {2}",
                key, value, actual);
    }

    /**
     * Checks, that the map contains an entry for the given key.
     *
     * @param key The key that must be contained in the map.
     */
    public ExpectMap<K, V> containsKey(final K key) {
        return check(actual.containsKey(key),
                "Actual collection does not contain expected element \"{0}\", actual: \"{1}\"", key,
                actual);
    }

    /**
     * Checks, that the map does not contain an entry for the given key.
     *
     * @param key The key that must be not contained in the map.
     */
    public ExpectMap<K, V> containsNotKey(final K key) {
        return check(!actual.containsKey(key), "Actual must not contain expected element \"{0}\", actual: \"{1}\"", key,
                actual);
    }

    /**
     * Returns an {@link ExpectCollection} for the values of this map for
     * further checks.
     */
    public ExpectCollection<V> values() {
        return ExpectCollection.expectCollection(actual.values());
    }

    /**
     * Returns an {@link ExpectCollection} for the keys of this map for further
     * checks.
     */
    public ExpectCollection<K> keys() {
        return ExpectCollection.expectCollection(actual.keySet());
    }

    /**
     * Returns an {@link ExpectCollection} to futher check the map's entry set.
     */
    public ExpectCollection<Entry<K, V>> entrySet() {
        return ExpectCollection.expectCollection(actual.entrySet());
    }

}
