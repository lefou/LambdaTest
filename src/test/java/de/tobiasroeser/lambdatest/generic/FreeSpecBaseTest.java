package de.tobiasroeser.lambdatest.generic;

import static de.tobiasroeser.lambdatest.Expect.intercept;

import org.testng.annotations.Test;

import de.tobiasroeser.lambdatest.Reporter;
import de.tobiasroeser.lambdatest.proxy.TestProxy;

public class FreeSpecBaseTest {

	class SameNameTests extends FreeSpecBase {

		@Override
		public void pending(final String reason) {
			// no-op for test
		}

		public SameNameTests() {
			test("name", () -> {
			});
			test("name", () -> {
			});
		}

	}

	class SameNameInOtherSectionTests extends FreeSpecBase {

		@Override
		public void pending(final String reason) {
			// no-op for test
		}

		public SameNameInOtherSectionTests() {
			section("a", () -> {
				test("name", () -> {
				});
			});
			section("b", () -> {
				test("name", () -> {
				});
			});
		}

	}

	class SameNameInOtherSectionWithSameNameTests extends FreeSpecBase {

		@Override
		public void pending(final String reason) {
			// no-op for test
		}

		public SameNameInOtherSectionWithSameNameTests() {
			section("a", () -> {
				test("name", () -> {
				});
			});
			section("a", () -> {
				test("name", () -> {
				});
			});
		}

	}

	class MarkerException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public MarkerException(final String msg) {
			super(msg);
		}
	}

	private final Reporter failOnSuiteWarningReporter = TestProxy.proxy(Reporter.class, new Object() {
		@SuppressWarnings("unused")
		public void suiteWarning(final String suiteName, final String warning) {
			throw new MarkerException(warning);
		}
	});

	@Test(dependsOnGroups = { "intercept" })
	public void testSameNameShouldFailTests() throws Exception {
		FreeSpecBase.withDefaultReporter(failOnSuiteWarningReporter, () -> {
			intercept(MarkerException.class,
					"\\QTest name is not unique: name\\E",
					() -> new SameNameTests());
			return null;
		});
	}

	@Test
	public void testSameNameInOtherSectionTests() throws Exception {
		FreeSpecBase.withDefaultReporter(failOnSuiteWarningReporter, () -> {
			new SameNameInOtherSectionTests();
			return null;
		});
	}

	@Test
	public void testSameNameInOtherSectionWithSameNameTests() throws Exception {
		FreeSpecBase.withDefaultReporter(failOnSuiteWarningReporter, () -> {
			intercept(MarkerException.class,
					"\\QTest name is not unique in this section: a / name\\E",
					() -> new SameNameInOtherSectionWithSameNameTests());
			return null;
		});
	}

}
