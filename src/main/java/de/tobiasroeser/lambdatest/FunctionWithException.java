package de.tobiasroeser.lambdatest;

/**
 * A Function with is permitted to throw exceptions.
 *
 * @param <P>
 *            The parameter type.
 * @param <R>
 *            The return type.
 */
public interface FunctionWithException<P, R> {

	R apply(P param) throws Exception;

}
