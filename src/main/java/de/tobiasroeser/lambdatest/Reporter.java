package de.tobiasroeser.lambdatest;

import java.util.List;

public interface Reporter {

	String PENDING_DEFAULT_MSG = "Pending";

	void testStart(LambdaTestCase test);

	void testSkipped(LambdaTestCase test, String message);

	void testFailed(LambdaTestCase test, Throwable error);

	void testSucceeded(LambdaTestCase test);

	void suiteStart(String suiteName, List<? extends LambdaTestCase> tests);

	void suiteWarning(String suiteName, String warning);

}
