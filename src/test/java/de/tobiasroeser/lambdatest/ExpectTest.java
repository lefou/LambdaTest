package de.tobiasroeser.lambdatest;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;
import static de.tobiasroeser.lambdatest.Intercept.intercept;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.regex.Pattern;

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
		Expect.expectEquals(1, 1, "ONE");
	}

	@Test
	public void testBaseFailure() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		intercept(AssertionError.class, "ONE", () -> {
			Expect.expectEquals(1, 2, "ONE");
		});
	}

	@Test
	public void testPlainSetupFinishCycle() {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(true);
		assertNotEquals(Expect.__TEST_threadContext(), null);
		Expect.finish();
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test
	public void testOneSuccessfulAssert() {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(true);
		assertNotEquals(Expect.__TEST_threadContext(), null);
		Expect.expectEquals(1, 1, "ONE");
		Expect.finish();
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test
	public void testOneFailingAssert() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(true);
		assertNotEquals(Expect.__TEST_threadContext(), null);
		Expect.expectEquals(1, 2, "ONE");
		intercept(AssertionError.class, "ONE", () -> {
			Expect.finish();
		});
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test
	public void testTwoSuccessfulAssert() {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(true);
		assertNotEquals(Expect.__TEST_threadContext(), null);
		Expect.expectEquals(1, 1, "ONE");
		Expect.expectEquals(2, 2, "TWO");
		Expect.finish();
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test
	public void testTwoFailingAssert() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(true);
		assertNotEquals(Expect.__TEST_threadContext(), null);
		Expect.expectEquals(1, 2, "ONE");
		Expect.expectEquals(2, 1, "TWO");
		intercept(AssertionError.class, () -> {
			Expect.finish();
		});
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test
	public void testTwoBoolean() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.expectEquals(true, true);
		Expect.expectEquals(false, false);
		intercept(AssertionError.class, () -> {
			Expect.expectEquals(false, true);
		});
		intercept(AssertionError.class, () -> {
			Expect.expectEquals(true, false);
		});
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test
	public void testExpectEqualsStringArrayFailEarly() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(false);
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
				"\\QArray length of 1 does not match expected length of 0. Expected [] but was [a]\\E", () -> {
					expectEquals(new String[] { "a" }, new String[] {});
				});
		intercept(AssertionError.class,
				"\\QArray length of 0 does not match expected length of 1. Expected [a] but was []\\E", () -> {
					expectEquals(new String[] {}, new String[] { "a" });
				});
		intercept(AssertionError.class,
				"\\QArray length of 1 does not match expected length of 2. Expected [a,b] but was [a]\\E", () -> {
					expectEquals(new String[] { "a" }, new String[] { "a", "b" });
				});
		intercept(AssertionError.class,
				"\\QArray length of 2 does not match expected length of 1. Expected [a] but was [a,b]\\E", () -> {
					expectEquals(new String[] { "a", "b" }, new String[] { "a" });
				});
		intercept(
				AssertionError.class,
				"\\QArrays differ at index 0. Expected [b] but was [a]. Element difference error: Actual a is not equal to b\\E",
				() -> {
					expectEquals(new String[] { "a" }, new String[] { "b" });
				});

		// intercept(AssertionError.class, () -> {
		Expect.finish();
		// });
		assertEquals(Expect.__TEST_threadContext(), null);
	}

	@Test
	public void testExpectEqualsStringArrayNotFailEarly() throws Exception {
		Expect.clear();
		assertEquals(Expect.__TEST_threadContext(), null);
		Expect.setup(true);
		assertNotEquals(Expect.__TEST_threadContext(), null);

		expectEquals(null, null);
		expectEquals(new String[] {}, new String[] {});
		expectEquals(new String[] { "a" }, new String[] { "a" });
		// intercept(AssertionError.class,
		// "\\QExpected an array, but got a java.lang.String\\E", () -> {
		expectEquals("a", new String[] { "a" });
		// });
		// intercept(AssertionError.class,
		// "\\QGot an array, but did not expected one. Expected a java.lang.String\\E",
		// () -> {
		expectEquals(new String[] { "a" }, "a");
		// });
		// intercept(AssertionError.class,
		// "\\QArray length of 1 does not match expected length of 0. Expected [] but was [a]\\E",
		// () -> {
		expectEquals(new String[] { "a" }, new String[] {});
		// });
		// intercept(AssertionError.class,
		// "\\QArray length of 0 does not match expected length of 1. Expected [a] but was []\\E",
		// () -> {
		expectEquals(new String[] {}, new String[] { "a" });
		// });
		// intercept(AssertionError.class,
		// "\\QArray length of 1 does not match expected length of 2. Expected [a,b] but was [a]\\E",
		// () -> {
		expectEquals(new String[] { "a" }, new String[] { "a", "b" });
		// });
		// intercept(AssertionError.class,
		// "\\QArray length of 2 does not match expected length of 1. Expected [a] but was [a,b]\\E",
		// () -> {
		expectEquals(new String[] { "a", "b" }, new String[] { "a" });
		// });
		// intercept(
		// AssertionError.class,
		// "\\QArrays differ at index 0. Expected [b] but was [a]. Element difference error: Actual a is not equal to b\\E",
		// () -> {
		expectEquals(new String[] { "a" }, new String[] { "b" });
		// });

		intercept(AssertionError.class, "\\Q7 expectations failed\\E[\\s\\S]*", () -> {
			Expect.finish();
		});
		assertEquals(Expect.__TEST_threadContext(), null);
	}

}
