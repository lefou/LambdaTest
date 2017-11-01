package de.tobiasroeser.lambdatest;

import static de.tobiasroeser.lambdatest.Intercept.intercept;

import org.testng.annotations.Test;

/**
 * We assume {@link Intercept} is properly tested.
 *
 */
public class AssertTest {

	@Test(dependsOnGroups = { "intercept" })
	public void testAssertEqualsBoolean() throws Exception {
		Assert.assertEquals(true, true);
		Assert.assertEquals(false, false);
		intercept(AssertionError.class, "\\QActual false is not equal to true\\E", () -> {
			Assert.assertEquals(false, true);
		});
		intercept(AssertionError.class, "\\QActual true is not equal to false\\E", () -> {
			Assert.assertEquals(true, false);
		});
		intercept(AssertionError.class, "\\QActual was null but expected: true\\E", () -> {
			Assert.assertEquals(null, true);
		});
		intercept(AssertionError.class, "\\QExpected null but was: false\\E", () -> {
			Assert.assertEquals(false, null);
		});
	}

	@Test(dependsOnGroups = { "intercept" })
	public void testAssertEqualsLong() throws Exception {
		// standard cases
		Assert.assertEquals(0L, 0L);
		Assert.assertEquals(Long.valueOf(0L), 0L);
		Assert.assertEquals(0L, Long.valueOf(0L));
		intercept(AssertionError.class, "\\QActual 0 is not equal to 1.\\E", () -> {
			Assert.assertEquals(0L, 1L);
		});
		intercept(AssertionError.class, "\\QActual 0 is not equal to 1.\\E", () -> {
			Assert.assertEquals(Long.valueOf(0L), 1L);
		});
		intercept(AssertionError.class, "\\QActual 0 is not equal to 1.\\E", () -> {
			Assert.assertEquals(0L, Long.valueOf(1L));
		});
	}

	@Test(dependsOnGroups = { "intercept" })
	public void testAssertEqualsInteger() throws Exception {
		// standard cases
		Assert.assertEquals(0, 0);
		Assert.assertEquals(Integer.valueOf(0), 0);
		Assert.assertEquals(0, Integer.valueOf(0));
		intercept(AssertionError.class, "\\QActual 0 is not equal to 1.\\E", () -> {
			Assert.assertEquals(0, 1);
		});
		intercept(AssertionError.class, "\\QActual 0 is not equal to 1.\\E", () -> {
			Assert.assertEquals(Integer.valueOf(0), 1);
		});
		intercept(AssertionError.class, "\\QActual 0 is not equal to 1.\\E", () -> {
			Assert.assertEquals(0, Integer.valueOf(1));
		});
	}

	@Test(dependsOnGroups = { "intercept" })
	public void testAssertEqualsDiffTypes() throws Exception {
		intercept(AssertionError.class,
				"\\QActual 0 of type java.lang.Integer is not equal to 0 of type java.lang.Long, but their long values are equal.\\E",
				() -> {
					Assert.assertEquals(0, 0L);
				});
		intercept(AssertionError.class,
				"\\QActual 0 of type java.lang.Long is not equal to 0 of type java.lang.Integer, but their long values are equal.\\E",
				() -> {
					Assert.assertEquals(0L, 0);
				});
		intercept(AssertionError.class,
				"\\QActual " + Float.valueOf(1F)
						+ " of type java.lang.Float is not equal to " + Double.valueOf(1D)
						+ " of type java.lang.Double, but their double values are equal.\\E",
				() -> {
					Assert.assertEquals(1F, 1D);
				});
		intercept(AssertionError.class,
				"\\QActual " + Double.valueOf(1D)
						+ " of type java.lang.Double is not equal to " + Float.valueOf(1F)
						+ " of type java.lang.Float, but their double values are equal.\\E",
				() -> {
					Assert.assertEquals(1D, 1F);
				});

	}

	@Test(dependsOnGroups = { "intercept" })
	public void testAssertEqualsString() throws Exception {
		Assert.assertEquals(null, null);
		Assert.assertEquals("a", "a");
		Assert.assertEquals("  a a", "  a a");
		intercept(AssertionError.class, "\\QExpected null but was: a\\E", () -> {
			Assert.assertEquals("a", null);
		});
		intercept(AssertionError.class, "\\QActual was null but expected: b\\E", () -> {
			Assert.assertEquals(null, "b");
		});
		intercept(AssertionError.class,
				"\\QStrings differ at index 0 (see [*] marker). Expected \"[*]b\" but was \"[*]a\".\\E", () -> {
					Assert.assertEquals("a", "b");
				});
		// This triggered a StackOverflowError in 0.2.1
		intercept(AssertionError.class, () -> {
			Assert.assertEquals("TEST", "");
		});
	}

	@Test(dependsOnGroups = { "intercept" })
	public void testAssertEqualsStringArray() throws Exception {
		Assert.assertEquals(null, null);
		Assert.assertEquals(new String[] {}, new String[] {});
		Assert.assertEquals(new String[] { "a" }, new String[] { "a" });
		intercept(AssertionError.class, "\\QExpected an array, but got a java.lang.String\\E", () -> {
			Assert.assertEquals("a", new String[] { "a" });
		});
		intercept(AssertionError.class, "\\QGot an array, but did not expected one. Expected a java.lang.String\\E",
				() -> {
					Assert.assertEquals(new String[] { "a" }, "a");
				});
		intercept(AssertionError.class,
				"\\QActual array length of 1 does not match expected length of 0. Expected [] but was [a]\\E", () -> {
					Assert.assertEquals(new String[] { "a" }, new String[] {});
				});
		intercept(AssertionError.class,
				"\\QActual array length of 0 does not match expected length of 1. Expected [a] but was []\\E", () -> {
					Assert.assertEquals(new String[] {}, new String[] { "a" });
				});
		intercept(AssertionError.class,
				"\\QActual array length of 1 does not match expected length of 2. Expected [a,b] but was [a]\\E",
				() -> {
					Assert.assertEquals(new String[] { "a" }, new String[] { "a", "b" });
				});
		intercept(AssertionError.class,
				"\\QActual array length of 2 does not match expected length of 1. Expected [a] but was [a,b]\\E",
				() -> {
					Assert.assertEquals(new String[] { "a", "b" }, new String[] { "a" });
				});
		intercept(
				AssertionError.class,
				"\\QArrays differ at index 0. Expected [b] but was [a]. Error for element at index 0: Strings differ at index 0 (see [*] marker). Expected \"[*]b\" but was \"[*]a\".\\E",
				() -> {
					Assert.assertEquals(new String[] { "a" }, new String[] { "b" });
				});
	}

	@Test(dependsOnGroups = { "intercept" })
	public void testAssertNull() throws Exception {
		Assert.assertNull(null);
		Assert.assertNull(null, "null should be ok");
		intercept(AssertionError.class, "\\QActual [Some] should be null\\E", () -> {
			Assert.assertNull("Some");
		});
		intercept(AssertionError.class, "\\QSome should be not ok\\E", () -> {
			Assert.assertNull("Some", "Some should be not ok");
		});
	}

	@Test(dependsOnGroups = { "intercept" })
	public void testAssertNotNull() throws Exception {
		Assert.assertNotNull("Some");
		Assert.assertNotNull("Some", "[Some] should be ok");
		intercept(AssertionError.class, "\\QActual should be not null\\E", () -> {
			Assert.assertNotNull(null);
		});
		intercept(AssertionError.class, "\\Qnull should be not ok\\E", () -> {
			Assert.assertNotNull(null, "null should be not ok");
		});
	}

}
