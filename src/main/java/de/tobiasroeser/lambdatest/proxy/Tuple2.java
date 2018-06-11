package de.tobiasroeser.lambdatest.proxy;

import java.io.Serializable;

/**
 * Value class representing a 2-tuple.
 *
 * This class is immutable and thus thread-safe.
 *
 * @param <A>
 *            The type of the first element.
 * @param <B>
 *            The type of the second element.
 */
public class Tuple2<A, B> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static <A, B> Tuple2<A, B> of(final A a, final B b) {
		return new Tuple2<A, B>(a, b);
	}

	private final A a;
	private final B b;

	protected Tuple2(final A a, final B b) {
		this.a = a;
		this.b = b;
	}

	public A a() {
		return a;
	}

	public B b() {
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a() == null) ? 0 : a().hashCode());
		result = prime * result + ((b() == null) ? 0 : b().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof Tuple2)) {
			return false;
		}

		final Tuple2<?, ?> that = (Tuple2<?, ?>) other;
		if (!that.canEqual(this)) {
			return false;
		}

		if (this.a() == null) {
			if (that.a() != null) {
				return false;
			}
		} else if (!this.a().equals(that.a())) {
			return false;
		}
		if (this.b() == null) {
			if (that.b() != null) {
				return false;
			}
		} else if (!this.b().equals(that.b())) {
			return false;
		}
		return true;
	}

	public boolean canEqual(final Object other) {
		return other instanceof Tuple2;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + a() + "," + b() + ")";
	}
}
