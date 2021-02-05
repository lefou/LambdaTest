package de.tobiasroeser.lambdatest.generic;

import java.util.List;

import de.tobiasroeser.lambdatest.LambdaTestCase;
import de.tobiasroeser.lambdatest.Reporter;
import de.tobiasroeser.lambdatest.internal.Logger;
import de.tobiasroeser.lambdatest.internal.LoggerFactory;

/**
 * A Reporter that logs and also wraps another reporter.
 */
public class LoggingWrappingReporter implements Reporter {

	private final Logger log = LoggerFactory.getLogger(LoggingWrappingReporter.class);

	private final Reporter underlying;

	public LoggingWrappingReporter(final Reporter underlying) {
		this.underlying = underlying;
	}

	protected String formatTestCase(final LambdaTestCase test) {
		return "\"" + test.getName() + "\" (" + test.getSuiteName() + ")";
	}

	@Override
	public void testStart(final LambdaTestCase test) {
		if (log.isDebugEnabled()) {
			log.debug("Test started: {}", formatTestCase(test));
		}
		underlying.testStart(test);
	}

	@Override
	public void testSkipped(final LambdaTestCase test, final String message) {
		if (log.isDebugEnabled()) {
			if (PENDING_DEFAULT_MSG.equals(message)) {
				log.debug("Test skipped: {}", formatTestCase(test));
			} else {
				log.debug("Test skipped: {} with message: {}", formatTestCase(test), message);
			}
		}
		underlying.testSkipped(test, message);
	}

	@Override
	public void testFailed(final LambdaTestCase test, final Throwable error) {
		log.error("Test failed: {}", formatTestCase(test), error);
		underlying.testFailed(test, error);
	}

	@Override
	public void testSucceeded(final LambdaTestCase test) {
		if (log.isDebugEnabled()) {
			log.debug("Test succeeded: {}", formatTestCase(test));
		}
		underlying.testSucceeded(test);
	}

	@Override
	public void suiteStart(final String suiteName, final List<? extends LambdaTestCase> tests) {
		if (log.isDebugEnabled()) {
			log.debug("Suite started with {} tests: {}", tests.size(), suiteName);
		}
		underlying.suiteStart(suiteName, tests);
	}

	@Override
	public void suiteWarning(final String suiteName, final String warning) {
		log.warn("Test suite {} issues a warning: {}", suiteName, warning);
		underlying.suiteWarning(suiteName, warning);
	}

}
