package de.tobiasroeser.lambdatest.internal;

public interface Procedure1<P> {

	public void apply(P param);

	public static final class NoOp<P> implements Procedure1<P> {

		@SuppressWarnings("unused")
		public void apply(final P p) {
			// no op
		}

	}
}
