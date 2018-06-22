package de.tobiasroeser.lambdatest.generic;

import static de.tobiasroeser.lambdatest.internal.Util.find;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import de.tobiasroeser.lambdatest.Intercept;
import de.tobiasroeser.lambdatest.LambdaTest;
import de.tobiasroeser.lambdatest.Optional;
import de.tobiasroeser.lambdatest.Reporter;
import de.tobiasroeser.lambdatest.RunnableWithException;
import de.tobiasroeser.lambdatest.Section;

/**
 * Common base class containing framework agnostic implementation of
 * FreeSpec-like classes.
 * 
 * @see de.tobiasroeser.lambdatest.junit.FreeSpec
 * @see de.tobiasroeser.lambdatest.testng.FreeSpec
 */
public abstract class FreeSpecBase implements LambdaTest {

	private static final ThreadLocal<Section> sectionHolder = new ThreadLocal<Section>();

	private static Reporter defaultReporter = new LoggingWrappingReporter(new DefaultReporter());

	public static Reporter getDefaultReporter() {
		return defaultReporter;
	}

	public static void setDefaultReporter(final Reporter reporter) {
		defaultReporter = reporter;
	}

	public interface F0WithException<R> {
		public R apply() throws Exception;
	}

	public static <T> T withDefaultReporter(final Reporter reporter, final F0WithException<T> f) throws Exception {
		final Reporter defRep = getDefaultReporter();
		try {
			setDefaultReporter(reporter);
			return f.apply();
		} finally {
			setDefaultReporter(defRep);
		}
	}

	private Reporter reporter = defaultReporter;
	private final List<DefaultTestCase> testCases = new LinkedList<>();
	private String suiteName = getClass().getName();
	private boolean expectFailFast;
	private boolean runInParallel = false;

	public boolean getRunInParallel() {
		return runInParallel;
	}

	@Override
	public void setRunInParallel(final boolean runInParallel) {
		this.runInParallel = runInParallel;
	}

	public boolean getExpectFailFast() {
		return expectFailFast;
	}

	@Override
	public void setExpectFailFast(final boolean failFast) {
		this.expectFailFast = failFast;
	}

	public String getSuiteName() {
		return suiteName;
	}

	@Override
	public Reporter getReporter() {
		return reporter;
	}

	@Override
	public void setReporter(final Reporter reporter) {
		this.reporter = reporter;
	}

	/**
	 * Adds a test to the test suite.
	 *
	 * @param name
	 *            The name of the new test.
	 * @param testCase
	 *            The test case. It should return when it is successful, else it
	 *            should throw an exception. Depending on the underlying test
	 *            framework/runner, there are different exceptions types, which
	 *            are recognized and handled. See in the concrete implementation
	 *            documentation for more details.
	 */
	public void test(final String name, final RunnableWithException testCase) {
		if (find(testCases, tc -> name.equals(tc.getName())).isDefined()) {
			getReporter().suiteWarning(suiteName, "Test with non-unique name added: " + name);
		}
		this.testCases.add(new DefaultTestCase(sectionHolder.get(), name, suiteName, testCase));
	}

	public List<DefaultTestCase> getTestCases() {
		return testCases;
	}

	/**
	 * Intercept exceptions of type <code>exceptionType</code> and fail if no
	 * such exception or an exception with an incompatible type was thrown.
	 *
	 * @param exceptionType
	 *            The exception type to intercept.
	 * @param throwing
	 *            The execution block which is expected to throw the exception.
	 * @return The intercepted exception.
	 * @throws Exception
	 *             If no exception was thrown or an exception with an
	 *             incompatible type was thrown.
	 */
	@Override
	public <T extends Throwable> T intercept(final Class<T> exceptionType,
			final RunnableWithException throwing) throws Exception {
		return Intercept.intercept(exceptionType, throwing);
	}

	/**
	 * Intercept exceptions of type <code>exceptionType</code> and fail if no
	 * such exception or an exception with an incompatible type was thrown or it
	 * the message does not match a given pattern.
	 *
	 * @param exceptionType
	 *            The exception type to intercept.
	 * @param messageRegex
	 *            A regular expression pattern to match the expected message.
	 *            See {@link Pattern} for details.
	 * @param throwing
	 *            The execution block which is expected to throw the exception.
	 * @return The intercepted exception.
	 * @throws Exception
	 *             If no exception was thrown or an exception with an
	 *             incompatible type was thrown or if the message of the
	 *             exception did not match the expected pattern.
	 */
	@Override
	public <T extends Throwable> T intercept(final Class<T> exceptionType,
			final String messageRegex, final RunnableWithException throwing)
			throws Exception {
		return Intercept.intercept(exceptionType, messageRegex, throwing);
	}

	public void section(final String section, final Runnable code) {
		final Section parent = sectionHolder.get();
		sectionHolder.set(new Section(section, parent));
		code.run();
		if (parent == null) {
			sectionHolder.remove();
		} else {
			sectionHolder.set(parent);
		}
	}

	public Optional<Section> getCurrentSection() {
		return Optional.lift(sectionHolder.get());
	}

	@Override
	public void pending() {
		pending(Reporter.PENDING_DEFAULT_MSG);
	}

}
