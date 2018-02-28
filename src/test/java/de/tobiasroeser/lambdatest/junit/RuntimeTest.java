package de.tobiasroeser.lambdatest.junit;

import static org.testng.Assert.assertEquals;

import org.junit.Assert;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.testng.annotations.Test;

import de.tobiasroeser.lambdatest.generic.DefaultReporter;
import de.tobiasroeser.lambdatest.generic.LoggingWrappingReporter;

public class RuntimeTest {

	public static class SimpleSuccessTest extends FreeSpec {
		public SimpleSuccessTest() {
			test("should succeed", () -> {
				Assert.assertTrue(true);
			});
		}
	}

	public static class SimpleFailureTest extends FreeSpec {
		public SimpleFailureTest() {
			// We don't want the stacktrace to fool us in the test suite
			setReporter(new LoggingWrappingReporter(new DefaultReporter(System.out, false)));
			test("should fail", () -> {
				Assert.assertTrue(false);
			});
		}
	}

	public static class SimplePendingTest extends FreeSpec {
		public SimplePendingTest() {
			test("should be pending", () -> {
				pending();
				Assert.fail("should not be reached");
			});
		}
	}

	public static class SimplePendingWithReasonTest extends FreeSpec {
		public SimplePendingWithReasonTest() {
			test("should be pending with reason", () -> {
				pending("With Reason");
				Assert.fail("should not be reached");
			});
		}
	}

	@Test(groups = { "junit" })
	public void testSuccess() {
		final Result result = JUnitCore.runClasses(SimpleSuccessTest.class);
		assertEquals(result.getRunCount(), 1);
		assertEquals(result.getFailureCount(), 0);
		assertEquals(result.getIgnoreCount(), 0);
	}

	@Test(groups = { "junit" })
	public void testFailure() {
		final Result result = JUnitCore.runClasses(SimpleFailureTest.class);
		assertEquals(result.getRunCount(), 1);
		assertEquals(result.getFailureCount(), 1);
		assertEquals(result.getIgnoreCount(), 0);
	}

	@Test(groups = { "junit" })
	public void testPending() {
		final Result result = JUnitCore.runClasses(SimplePendingTest.class);
		assertEquals(result.getRunCount(), 1);
		assertEquals(result.getFailureCount(), 0);
		assertEquals(result.getIgnoreCount(), 0);
	}

	@Test(groups = { "junit" })
	public void testPendingWithReason() {
		final Result result = JUnitCore.runClasses(SimplePendingWithReasonTest.class);
		assertEquals(result.getRunCount(), 1);
		assertEquals(result.getFailureCount(), 0);
		assertEquals(result.getIgnoreCount(), 0);
	}

}