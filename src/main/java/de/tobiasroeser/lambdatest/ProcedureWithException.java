package de.tobiasroeser.lambdatest;

/**
 * A Function which is permitted to throw exceptions.
 *
 */
public interface ProcedureWithException<P> {

	void apply(P param) throws Exception;

}
