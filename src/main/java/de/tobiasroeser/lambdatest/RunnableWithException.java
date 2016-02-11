package de.tobiasroeser.lambdatest;

/**
 * A Runnable with is permitted to throw exceptions.
 * 
 * @see Runnable
 *
 */
public interface RunnableWithException {

	void run() throws Exception;

}
