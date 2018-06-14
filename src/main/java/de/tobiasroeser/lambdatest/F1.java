package de.tobiasroeser.lambdatest;

/**
 * A function with a parameter and a return value.
 * 
 * @param <P>
 *            The parameter type.
 * @param <R>
 *            The return type.
 */
public interface F1<P, R> {
	public R apply(P param);

	public class Identity<I> implements F1<I, I> {

		public I apply(I param) {
			return param;
		}

	}

}
