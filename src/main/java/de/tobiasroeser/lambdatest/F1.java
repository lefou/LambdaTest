package de.tobiasroeser.lambdatest;

public interface F1<P, R> {
	public R apply(P param);

	public class Identity<I> implements F1<I, I> {

		public I apply(I param) {
			return param;
		}

	}

}
