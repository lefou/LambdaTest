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
