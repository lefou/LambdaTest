package de.tobiasroeser.lambdatest.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Util {

	public static <T> Optional<T> find(final Iterable<T> source,
			final F1<? super T, Boolean> accept) {
		for (final T t : source) {
			if (accept.apply(t)) {
				return Optional.some(t);
			}
		}
		return Optional.none();
	}

	public static <R, T> List<R> map(final Iterable<T> source, final F1<? super T, ? extends R> convert) {
		final List<R> result = (source instanceof Collection<?>) ? new ArrayList<R>(((Collection<?>) source).size())
				: new LinkedList<R>();
		for (final T t : source) {
			result.add(convert.apply(t));
		}
		return result;
	}

	public static <T, R> List<R> map(final T[] source, final F1<? super T, ? extends R> convert) {
		return map(Arrays.asList(source), convert);
	}

	public static <T> List<T> filter(final Iterable<T> source, final F1<? super T, Boolean> accept) {
		final List<T> result = new LinkedList<T>();
		for (final T t : source) {
			if (accept.apply(t)) {
				result.add(t);
			}
		}
		return result;
	}
}
