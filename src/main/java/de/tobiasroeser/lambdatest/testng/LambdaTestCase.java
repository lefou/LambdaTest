package de.tobiasroeser.lambdatest.testng;

/**
 * A single test case, meant to be internally used by {@link FreeSpec}.
 *
 */
public class LambdaTestCase {

	private final String name;
	private final RunnableWithException test;

	public LambdaTestCase(String name, RunnableWithException test) {
		this.name = name;
		this.test = test;
	}

	/**
	 * Return the test name. Thus, when the test parameter of the generic test
	 * method is shown in the TestNG report, the user sees the actual test case
	 * name.
	 */
	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		return name;
	}

	public RunnableWithException getTest() {
		return test;
	}

}
