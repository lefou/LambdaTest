package de.tobiasroeser.lambdatest;

import java.util.List;

import de.tobiasroeser.lambdatest.generic.DefaultReporter;
import de.tobiasroeser.lambdatest.generic.LoggingWrappingReporter;

/**
 * A Reporter is used to report (log,output) the progress of a running test
 * suite.
 * 
 * @see DefaultReporter
 * @see LoggingWrappingReporter
 *
 */
public interface Reporter {

	String PENDING_DEFAULT_MSG = "Pending";

	void testStart(LambdaTestCase test);

	void testSkipped(LambdaTestCase test, String message);

	void testFailed(LambdaTestCase test, Throwable error);

	void testSucceeded(LambdaTestCase test);

	void suiteStart(String suiteName, List<? extends LambdaTestCase> tests);

	void suiteWarning(String suiteName, String warning);

}
