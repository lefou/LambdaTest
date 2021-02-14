package de.tobiasroeser.lambdatest;

/**
 * Shared interface for executable test cases.
 * 
 */
public interface LambdaTestCase {

	/**
	 * The name of the test case. This is most often also the description.
	 */
	String getName();

	/**
	 * The name of the test suite, e.g. the class name.
	 */
	String getSuiteName();

	/**
	 * The optional section, this test case belongs to.
	 */
	Optional<Section> getSection();

	String getSectionAndTestName(final String separator);

}
