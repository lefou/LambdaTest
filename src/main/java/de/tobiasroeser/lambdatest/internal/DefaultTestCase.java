package de.tobiasroeser.lambdatest.internal;

import de.tobiasroeser.lambdatest.LambdaTestCase;
import de.tobiasroeser.lambdatest.Optional;
import de.tobiasroeser.lambdatest.RunnableWithException;
import de.tobiasroeser.lambdatest.Section;

/**
 * A single test case, meant to be internally used by
 * {@link de.tobiasroeser.lambdatest.junit.FreeSpec} and
 * {@link de.tobiasroeser.lambdatest.testng.FreeSpec}
 *
 */
public class DefaultTestCase implements LambdaTestCase {

	private final Section section;
	private final String name;
	private final RunnableWithException test;
	private final String suiteName;

	public DefaultTestCase(
			final Section section,
			final String name,
			final String suiteName,
			final RunnableWithException test) {
		this.section = section;
		this.name = name;
		this.suiteName = suiteName;
		this.test = test;
	}

	public DefaultTestCase(
			final String name,
			final String suiteName,
			final RunnableWithException test) {
		this(null, name, suiteName, test);
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

	public Optional<Section> getSection() {
		return Optional.lift(section);
	}

	public String getName() {
		return name;
	}

	public RunnableWithException getTest() {
		return test;
	}

	public String getSuiteName() {
		return suiteName;
	}

}
