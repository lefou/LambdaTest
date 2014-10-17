package de.tobiasroeser.lambdatest.testng;

/**
 * A Runnable with is permitted to throw exceptions.
 * 
 * @see Runnable
 *
 */
public interface RunnableWithException {

	void run() throws Exception;

}
