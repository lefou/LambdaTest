package de.tobiasroeser.lambdatest.shared;

import java.util.Collections;
import java.util.List;

import de.tobiasroeser.lambdatest.RunnableWithException;

/**
 * A single test case, meant to be internally used by {@link FreeSpec}.
 *
 */
public class LambdaTestCase {

	private final List<String> sections;
	private final String name;
	private final RunnableWithException test;

	public LambdaTestCase(final List<String> sections, final String name, final RunnableWithException test) {
		this.sections = Collections.unmodifiableList(sections);
		this.name = name;
		this.test = test;
	}

	public LambdaTestCase(final String name, final RunnableWithException test) {
		this(Collections.emptyList(), name, test);
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

	public List<String> getSections() {
		return sections;
	}

	public String getName() {
		return name;
	}

	public RunnableWithException getTest() {
		return test;
	}

}
