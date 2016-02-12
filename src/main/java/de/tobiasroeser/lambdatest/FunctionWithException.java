package de.tobiasroeser.lambdatest;

/**
 * A Function with is permitted to throw exceptions.
 *
 */
public interface FunctionWithException<P, R> {

	R apply(P param) throws Exception;

}
