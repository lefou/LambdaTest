package de.tobiasroeser.lambdatest.testng;

public class LambdaTestCase {

	private final String name;
	private final RunnableWithException test;

	public LambdaTestCase(String name, RunnableWithException test) {
		this.name = name;
		this.test = test;
	}

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
