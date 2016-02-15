package de.tobiasroeser.lambdatest;

/**
 * A Function with is permitted to throw exceptions.
 *
 */
public interface ProcedureWithException<P> {

	void apply(P param) throws Exception;

}
