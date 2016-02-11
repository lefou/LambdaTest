package de.tobiasroeser.lambdatest.junit;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import de.tobiasroeser.lambdatest.Expect;
import de.tobiasroeser.lambdatest.internal.AnsiColor;
import de.tobiasroeser.lambdatest.internal.AnsiColor.Color;
import de.tobiasroeser.lambdatest.shared.LambdaTestCase;

public class FreeSpecRunner extends ParentRunner<LambdaTestCase> {

	private final List<LambdaTestCase> testCases;
	private final Class<?> freeSpecClass;
	private final boolean expectFailFast;

	private volatile boolean testNeverRun = true;

	public FreeSpecRunner(final Class<?> freeSpecClass) throws InitializationError {
		super(freeSpecClass);
		if (!FreeSpec.class.isAssignableFrom(freeSpecClass)) {
			throw new InitializationError(
					"FreeSpecRunner only supports test classes of type " + FreeSpec.class.getName());
		}
		this.freeSpecClass = freeSpecClass;

		try {
			final FreeSpec freeSpec = (FreeSpec) freeSpecClass.newInstance();
			testCases = Collections.unmodifiableList(freeSpec.getTestCases());
			expectFailFast = freeSpec.getExpectFailFast();

		} catch (final Exception e) {
			throw new InitializationError("Could not intantiate test class " + freeSpecClass.getName());
		}
	}

	@Override
	protected Description describeChild(final LambdaTestCase testCase) {
		return Description.createTestDescription(freeSpecClass, testCase.getName());
	}

	@Override
	protected List<LambdaTestCase> getChildren() {
		return testCases;
	}

	@Override
	protected void runChild(final LambdaTestCase testCase, final RunNotifier runNotifier) {
		final AnsiColor ansi = new AnsiColor();
		final PrintStream out = System.out;

		if (testNeverRun) {
			synchronized (this) {
				if (testNeverRun) {
					out.println("Running " + ansi.fg(Color.CYAN) + testCases.size()
							+ ansi.reset() + " tests in " + ansi.fg(Color.CYAN)
							+ freeSpecClass.getName() + ansi.reset() + ":");
					testNeverRun = false;
				}
			}
		}

		final Description description = getDescription();
		runNotifier.fireTestStarted(description);

		final String testName = testCase.getName();

		try {
			Expect.setup(expectFailFast);
			Throwable uncatchedTestError = null;
			Throwable delayedTestError = null;
			try {
				testCase.getTest().run();
			} catch (final Throwable t) {
				uncatchedTestError = t;
			}
			try {
				Expect.finish();
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
			out.println(ansi.fg(Color.GREEN) + "-- SUCCESS " + testName + ansi.reset());
		} catch (final AssumptionViolatedException e) {
			out.println(ansi.fg(Color.YELLOW) + "-- SKIPPED " + testName + " (pending)" + ansi.reset());
			runNotifier.fireTestAssumptionFailed(new Failure(description, e));
		} catch (final Throwable e) {
			try {
				out.println(ansi.fg(Color.RED) + "-- FAILED  " + testName);
				// System.out.println(e.getMessage());
				e.printStackTrace(out);
				Throwable oldCause = e;
				Throwable cause = e.getCause();
				// unpack exception stack
				while (cause != null && cause != oldCause) {
					out.print("Caused by: ");
					cause.printStackTrace(out);
					oldCause = cause;
					cause = cause.getCause();
				}
			} catch (final Throwable t) {
				// ignore any further errors, just in case
			} finally {
				System.out.print(ansi.reset());
			}
			runNotifier.fireTestFailure(new Failure(description, e));
		} finally {
			runNotifier.fireTestFinished(description);
		}
	}

}
