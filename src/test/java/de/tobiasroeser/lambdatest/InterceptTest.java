package de.tobiasroeser.lambdatest;

import static de.tobiasroeser.lambdatest.Intercept.intercept;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

@SuppressWarnings("serial")
public class InterceptTest {

	public static class InterceptTestException extends RuntimeException {
		public InterceptTestException(final String msg) {
			super(msg);
		}
	}

	public static class InterceptTestSubException extends InterceptTestException {
		public InterceptTestSubException(final String msg) {
			super(msg);
		}
	}

	@Test(groups = { "intercept" })
	public void testInterceptByTypeWorks() {
		try {
			final InterceptTestException ex = intercept(InterceptTestException.class, () -> {
				throw new InterceptTestException("msg");
			});
			assertEquals(ex.getMessage(), "msg");
		} catch (final Throwable e) {
			assertTrue(false, "Expected no exception thrown");
		}
	}

	@Test(groups = { "intercept" })
	public void testInterceptByTypeWorksForSubTypes() {
		try {
			final InterceptTestException ex = intercept(InterceptTestException.class, () -> {
				throw new InterceptTestSubException("msg");
			});
			assertEquals(ex.getMessage(), "msg");
		} catch (final Throwable e) {
			assertTrue(false, "Expected no exception thrown");
		}
	}

	@Test(groups = { "intercept" })
	public void testInterceptByTypeIgnoresSuperTypes() {
		try {
			intercept(InterceptTestSubException.class, () -> {
				throw new InterceptTestException("msg");
			});
			assertFalse(true);
		} catch (final Throwable e) {
			assertTrue(e instanceof AssertionError, "Expected different exception type");
			assertEquals(
					e.getMessage().trim(),
					"Thrown exception of type [de.tobiasroeser.lambdatest.InterceptTest$InterceptTestSubException]"
							+ " does not match expected type [de.tobiasroeser.lambdatest.InterceptTest$InterceptTestException]");
		}
	}

	@Test(groups = { "intercept" })
	public void testInterceptWithCorrectTypeAndMessage() {
		try {
			final InterceptTestException ex = intercept(InterceptTestException.class, "\\Qmsg\\E", () -> {
				throw new InterceptTestException("msg");
			});
			assertEquals(ex.getMessage(), "msg");
		} catch (final Throwable e) {
			assertTrue(false, "Expected no exception thrown");
		}
	}

	@Test(groups = { "intercept" })
	public void testInterceptWithCorrectTypeButWrongMessage() {
		try {
			intercept(InterceptTestException.class, "Msg", () -> {
				throw new InterceptTestException("msg");
			});
			assertFalse(true);
		} catch (final Throwable e) {
			assertTrue(e instanceof AssertionError, "Expected different exception type");
			assertEquals(e.getMessage().trim(),
					"Exception was thrown with the wrong message: Expected: 'Msg' but got 'msg'.");
		}
	}

	@Test(groups = { "intercept" })
	public void testInterceptWithWrongTypeButCorrectMessage() {
		try {
			intercept(InterceptTestSubException.class, "\\Qmsg\\E", () -> {
				throw new InterceptTestException("msg");
			});
			assertFalse(true);
		} catch (final Throwable e) {
			assertTrue(e instanceof AssertionError, "Expected different exception type");
			assertEquals(
					e.getMessage().trim(),
					"Thrown exception of type [de.tobiasroeser.lambdatest.InterceptTest$InterceptTestSubException]"
							+ " does not match expected type [de.tobiasroeser.lambdatest.InterceptTest$InterceptTestException]");
		}
	}

}
