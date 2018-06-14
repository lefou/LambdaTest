package de.tobiasroeser.lambdatest;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Value class representing an optional value.
 *
 * This class is immutable and thus thread-safe.
 *
 * @param <T>
 *            The type of the optional value.
 */
public class Optional<T> implements Iterable<T>, Serializable {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	private static final Optional<?> NONE = new Optional();

	/**
	 * Create a defined {@link Optional} with the given value `some`.
	 */
	public static <S> Optional<S> some(final S some) {
		return new Optional<S>(some);
	}

	/**
	 * Create an {@link Optional} from the given object or a `none` in case the
	 * object was `null`.
	 */
	public static <S> Optional<S> lift(final S someOrNull) {
		if (someOrNull == null) {
			return new Optional<S>();
		} else {
			return new Optional<S>(someOrNull);
		}
	}

	private final boolean isNone;
	private final T optional;

	private Optional() {
		this.isNone = true;
		this.optional = null;
	}

	private Optional(final T optional) {
		this.isNone = false;
		this.optional = optional;
	}

	@SuppressWarnings("unchecked")
	public static <N> Optional<N> none() {
		return (Optional<N>) NONE;
	}

	public T get() {
		if (isDefined()) {
			return optional;
		} else {
			throw new NoSuchElementException("Optional value not defined.");
		}
	}

	public T getOrElse(final T t) {
		if (isDefined()) {
			return optional;
		} else {
			return t;
		}
	}

	public T getOrElseF(final F0<T> f) {
		if (isDefined()) {
			return optional;
		} else {
			return f.apply();
		}
	}

	public T orNull() {
		return isDefined() ? optional : null;
	}

	public boolean isDefined() {
		return !isNone;
	}

	public boolean isEmpty() {
		return isNone;
	}

	public Optional<?> adapt() {
		return isDefined() ? Optional.some(get()) : Optional.none();
	}

	public List<T> toList() {
		return isDefined() ? Arrays.<T> asList(optional) : Collections.<T> emptyList();
	}

	public Iterator<T> iterator() {
		return toList().iterator();
	}

	@SuppressWarnings("unchecked")
	public <R> Optional<R> map(final F1<? super T, ? extends R> f) {
		return (Optional<R>) (isEmpty() ? Optional.none() : Optional.some(f.apply(get())));
	}

	@SuppressWarnings("unchecked")
	public <R> Optional<R> flatMap(final F1<? super T, Optional<? extends R>> f) {
		return (Optional<R>) (isEmpty() ? Optional.none() : f.apply(get()));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + (isDefined() ? optional : "") + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isNone ? 1231 : 1237);
		result = prime * result + ((optional == null) ? 0 : optional.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Optional<?> other = (Optional<?>) obj;
		if (isNone != other.isNone) {
			return false;
		}
		if (optional == null) {
			if (other.optional != null) {
				return false;
			}
		} else if (!optional.equals(other.optional)) {
			return false;
		}
		return true;
	}

}
