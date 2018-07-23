package de.tobiasroeser.lambdatest;

/**
 * A function with a parameter and a return value.
 * 
 * @param <A>
 *            The parameter type 1.
 * @param <B>
 *            The parameter type 2.
 * @param <R>
 *            The return type.
 */
public interface F2<A, B, R> {
	R apply(A a, B b);
}
