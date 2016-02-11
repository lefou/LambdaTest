package de.tobiasroeser.lambdatest.junit;

import static org.testng.Assert.assertEquals;

import org.junit.Assert;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.testng.annotations.Test;

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
			test("should fail", () -> {
				Assert.assertTrue(false);
			});
		}
	}

	public static class SimplePendingTest extends FreeSpec {
		public SimplePendingTest() {
			test("should be pending", () -> {
				pending();
			});
		}
	}

	@Test(groups = { "junit" })
	public void testSuccessTest() {
		final Result result = JUnitCore.runClasses(SimpleSuccessTest.class);
		assertEquals(result.getRunCount(), 1);
		assertEquals(result.getFailureCount(), 0);
		assertEquals(result.getIgnoreCount(), 0);
	}

	@Test(groups = { "junit" })
	public void testFailureTest() {
		final Result result = JUnitCore.runClasses(SimpleFailureTest.class);
		assertEquals(result.getRunCount(), 1);
		assertEquals(result.getFailureCount(), 1);
		assertEquals(result.getIgnoreCount(), 0);
	}

	@Test(groups = { "junit" })
	public void testPendingTest() {
		final Result result = JUnitCore.runClasses(SimplePendingTest.class);
		assertEquals(result.getRunCount(), 1);
		assertEquals(result.getFailureCount(), 0);
		assertEquals(result.getIgnoreCount(), 0);
	}

}