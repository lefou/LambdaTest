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

	public static String mkString(final Iterable<?> source, final String separator) {
		return mkString(source, null, separator, null);
	}

	public static String mkString(final Object[] source, final String separator) {
		return mkString(Arrays.asList(source), separator);
	}

	public static String mkString(final Iterable<?> source, final String prefix, final String separator,
			final String suffix) {
		return mkString(source, prefix, separator, suffix, null);
	}

	public static <T> String mkString(final T[] source, final String prefix, final String separator, final String suffix) {
		return mkString(Arrays.asList(source), prefix, separator, suffix);
	}

	public static <T> String mkString(final Iterable<T> source, final String prefix, final String separator,
			final String suffix, final F1<? super T, String> convert) {
		final StringBuilder result = new StringBuilder();
		if (prefix != null) {
			result.append(prefix);
		}
		boolean sep = false;
		for (final T t : source) {
			if (sep && separator != null) {
				result.append(separator);
			}
			sep = true;
			if (convert != null) {
				result.append(convert.apply(t));
			} else {
				result.append(t == null ? null : t.toString());
			}
		}
		if (suffix != null) {
			result.append(suffix);
		}
		return result.toString();
	}

	public static <T> String mkString(final T[] source, final String prefix, final String separator,
			final String suffix, final F1<? super T, String> convert) {
		return mkString(Arrays.asList(source), prefix, separator, suffix, convert);
	}

}
