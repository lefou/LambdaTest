package de.tobiasroeser.lambdatest.junit;

import java.util.Collections;
import java.util.List;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import de.tobiasroeser.lambdatest.ExpectContext;
import de.tobiasroeser.lambdatest.Reporter;
import de.tobiasroeser.lambdatest.generic.DefaultTestCase;

public class FreeSpecRunner extends ParentRunner<DefaultTestCase> {

	private final List<DefaultTestCase> testCases;
	private final Class<?> freeSpecClass;
	private final boolean expectFailFast;
	private final FreeSpec freeSpec;

	private volatile boolean testNeverRun = true;

	public FreeSpecRunner(final Class<?> freeSpecClass) throws InitializationError {
		super(freeSpecClass);
		if (!FreeSpec.class.isAssignableFrom(freeSpecClass)) {
			throw new InitializationError(
					"FreeSpecRunner only supports test classes of type " + FreeSpec.class.getName());
		}
		this.freeSpecClass = freeSpecClass;

		try {
			freeSpec = (FreeSpec) freeSpecClass.newInstance();
			testCases = Collections.unmodifiableList(freeSpec.getTestCases());
			expectFailFast = freeSpec.getExpectFailFast();

		} catch (final Exception e) {
			throw new InitializationError("Could not instantiate test class " + freeSpecClass.getName());
		}
	}

	@Override
	protected Description describeChild(final DefaultTestCase testCase) {
		return Description.createTestDescription(freeSpecClass, testCase.getSectionAndTestName());
	}

	@Override
	protected List<DefaultTestCase> getChildren() {
		return testCases;
	}

	private Reporter reporter() {
		return freeSpec.getReporter();
	}

	@Override
	protected void runChild(final DefaultTestCase testCase, final RunNotifier runNotifier) {
		if (testNeverRun) {
			synchronized (this) {
				if (testNeverRun) {
					reporter().suiteStart(freeSpec.getSuiteName(), testCases);
					testNeverRun = false;
				}
			}
		}

		final Description description = getDescription();
		runNotifier.fireTestStarted(description);

		try {
			ExpectContext.setup(expectFailFast);
			Throwable uncatchedTestError = null;
			Throwable delayedTestError = null;
			try {
				reporter().testStart(testCase);
				testCase.getTest().run();
			} catch (final Throwable t) {
				uncatchedTestError = t;
			}
			try {
				ExpectContext.finish();
			} catch (final Throwable t) {
				delayedTestError = t;
			}
			if (uncatchedTestError != null && delayedTestError != null) {
				throw new AssertionError(
						"An error occured (see root cause) after some expectations failed. Failed Expectations:\n"
								+ delayedTestError.getMessage(),
						uncatchedTestError);
			} else if (uncatchedTestError != null) {
				// if this was a SkipException, we still detect it, else some
				// other errors occurred before
				throw uncatchedTestError;
			} else if (delayedTestError != null) {
				throw delayedTestError;
			}
			reporter().testSucceeded(testCase);
		} catch (final AssumptionViolatedException e) {
			reporter().testSkipped(testCase, e.getMessage());
			runNotifier.fireTestAssumptionFailed(new Failure(description, e));
		} catch (final Throwable e) {
			reporter().testFailed(testCase, e);
			runNotifier.fireTestFailure(new Failure(description, e));
		} finally {
			runNotifier.fireTestFinished(description);
		}
	}

}
