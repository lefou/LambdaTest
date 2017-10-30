package de.tobiasroeser.lambdatest;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;
import static de.tobiasroeser.lambdatest.Intercept.intercept;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

/**
 * We assume {@link Intercept} is properly tested.
 *
 */
public class ExpectTest {

	@Test
	public void testBaseSuccess() {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		expectEquals(1, 1, "ONE");
	}

	@Test(dependsOnGroups = { "intercept" })
	public void testBaseFailure() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		intercept(AssertionError.class, "ONE", () -> {
			expectEquals(1, 2, "ONE");
		});
	}

	@Test
	public void testPlainSetupFinishCycle() {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(false);
		assertNotEquals(Expect.__TEST_threadContext(), null);
		Expect.finish();
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test
	public void testOneSuccessfulAssert() {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(false);
		assertNotEquals(Expect.__TEST_threadContext(), null);
		expectEquals(1, 1, "ONE");
		Expect.finish();
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test(dependsOnGroups = { "intercept" })
	public void testOneFailingAssert() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(false);
		assertNotEquals(Expect.__TEST_threadContext(), null);
		expectEquals(1, 2, "ONE");
		intercept(AssertionError.class, "ONE", () -> {
			Expect.finish();
		});
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test
	public void testTwoSuccessfulAssert() {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(false);
		assertNotEquals(Expect.__TEST_threadContext(), null);
		expectEquals(1, 1, "ONE");
		expectEquals(2, 2, "TWO");
		Expect.finish();
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test(dependsOnGroups = { "intercept" })
	public void testTwoFailingAssert() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(false);
		assertNotEquals(Expect.__TEST_threadContext(), null);
		expectEquals(1, 2, "ONE");
		expectEquals(2, 1, "TWO");
		intercept(AssertionError.class, () -> {
			Expect.finish();
		});
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test
	public void testTwoBoolean() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		expectEquals(true, true);
		expectEquals(false, false);
		intercept(AssertionError.class, () -> {
			expectEquals(false, true);
		});
		intercept(AssertionError.class, () -> {
			expectEquals(true, false);
		});
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test(dependsOnGroups = { "intercept" })
	public void testExpectEqualsStringArrayFailEarly() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(true);
		assertNotEquals(Expect.__TEST_threadContext(), null);

		expectEquals(null, null);
		expectEquals(new String[] {}, new String[] {});
		expectEquals(new String[] { "a" }, new String[] { "a" });
		intercept(AssertionError.class, "\\QExpected an array, but got a java.lang.String\\E", () -> {
			expectEquals("a", new String[] { "a" });
		});
		intercept(AssertionError.class, "\\QGot an array, but did not expected one. Expected a java.lang.String\\E",
				() -> {
					expectEquals(new String[] { "a" }, "a");
				});
		intercept(AssertionError.class,
				"\\QActual array length of 1 does not match expected length of 0. Expected [] but was [a]\\E", () -> {
					expectEquals(new String[] { "a" }, new String[] {});
				});
		intercept(AssertionError.class,
				"\\QActual array length of 0 does not match expected length of 1. Expected [a] but was []\\E", () -> {
					expectEquals(new String[] {}, new String[] { "a" });
				});
		intercept(AssertionError.class,
				"\\QActual array length of 1 does not match expected length of 2. Expected [a,b] but was [a]\\E",
				() -> {
					expectEquals(new String[] { "a" }, new String[] { "a", "b" });
				});
		intercept(AssertionError.class,
				"\\QActual array length of 2 does not match expected length of 1. Expected [a] but was [a,b]\\E",
				() -> {
					expectEquals(new String[] { "a", "b" }, new String[] { "a" });
				});
		intercept(
				AssertionError.class,
				"\\QArrays differ at index 0. Expected [b] but was [a]. Error for element at index 0: Strings differ at index 0 (see [*] marker). Expected \"[*]b\" but was \"[*]a\".\\E",
				() -> {
					expectEquals(new String[] { "a" }, new String[] { "b" });
				});

		Expect.finish();
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test(dependsOnGroups = { "intercept" })
	public void testExpectEqualsStringArrayNotFailEarly() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(false);
		assertNotEquals(Expect.__TEST_threadContext(), null);

		expectEquals(null, null);
		expectEquals(new String[] {}, new String[] {});
		expectEquals(new String[] { "a" }, new String[] { "a" });
		expectEquals("a", new String[] { "a" });
		expectEquals(new String[] { "a" }, "a");
		expectEquals(new String[] { "a" }, new String[] {});
		expectEquals(new String[] {}, new String[] { "a" });
		expectEquals(new String[] { "a" }, new String[] { "a", "b" });
		expectEquals(new String[] { "a", "b" }, new String[] { "a" });
		expectEquals(new String[] { "a" }, new String[] { "b" });

		intercept(AssertionError.class, "\\Q7 expectations failed\\E[\\s\\S]*", () -> {
			Expect.finish();
		});
		assertEquals(Expect.__TEST_threadContext(), null);
	}

}
