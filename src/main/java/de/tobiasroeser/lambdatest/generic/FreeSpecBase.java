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

	/**
	 * The ThreadLocal used to hold the current section.
	 *
	 * @see #section(String, Runnable)
	 */
	private static final ThreadLocal<Section> sectionHolder = new ThreadLocal<Section>();

	private static Reporter defaultReporter = new LoggingWrappingReporter(new DefaultReporter());

	public static Reporter getDefaultReporter() {
		return defaultReporter;
	}

	/**
	 * Set the default reporter.
	 *
	 * @see #withDefaultReporter(Reporter, F0WithException)
	 */
	public static void setDefaultReporter(final Reporter reporter) {
		defaultReporter = reporter;
	}

	public interface F0WithException<R> {
		public R apply() throws Exception;
	}

	/**
	 * Executes a given function `f` with the default reporter set to
	 * `reporter`, and restored the previous default reporter afterwards.
	 *
	 * @param reporter
	 *            The default reporter to be used while executing `f`.
	 * @param f
	 *            The function to apply.
	 * @return The return value of the function `f`.
	 * @throws Exception
	 *             All expeceptions thrown by `f`.
	 */
	public static <T> T withDefaultReporter(final Reporter reporter, final F0WithException<T> f) throws Exception {
		final Reporter defRep = getDefaultReporter();
		try {
			setDefaultReporter(reporter);
			return f.apply();
		} finally {
			setDefaultReporter(defRep);
		}
	}

	// END OF STATIC PART

	private Reporter reporter = defaultReporter;
	private final List<DefaultTestCase> testCases = new LinkedList<>();
	private String suiteName = getClass().getName();
	private boolean expectFailFast;
	private boolean runInParallel = false;
	private volatile boolean lazyInitPending = true;

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
	 * Override this method to initialize test cases after class construction.
	 *
	 * This method should only be called by LambdaTest.
	 */
	protected void initTests() {
		// override this to add test cases after class construction
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
		final DefaultTestCase newTestCase = new DefaultTestCase(sectionHolder.get(), name, suiteName, testCase);
		final String sectionAndTestName = newTestCase.getSectionAndTestName();
		if (find(testCases, tc -> tc.getSectionAndTestName().equals(sectionAndTestName)).isDefined()) {
			if (newTestCase.getSection().isDefined()) {
				getReporter().suiteWarning(suiteName, "Test name is not unique in this section: " + sectionAndTestName);
			} else {
				getReporter().suiteWarning(suiteName, "Test name is not unique: " + name);
			}
		}
		this.testCases.add(newTestCase);
	}

	public List<DefaultTestCase> getTestCases() {
		if(lazyInitPending) {
			synchronized (this) {
				if(lazyInitPending) {
					initTests();
					lazyInitPending = false;
				}
			}
		}
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
		try {
			sectionHolder.set(new Section(section, parent));
			code.run();
		} finally {
			if (parent == null) {
				sectionHolder.remove();
			} else {
				sectionHolder.set(parent);
			}
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
