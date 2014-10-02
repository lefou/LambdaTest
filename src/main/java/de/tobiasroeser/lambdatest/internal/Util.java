package de.tobiasroeser.lambdatest.internal;

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
}
