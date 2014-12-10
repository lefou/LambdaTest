package de.tobiasroeser.lambdatest.testng;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import de.tobiasroeser.lambdatest.testng.FreeSpec;

@SuppressWarnings("serial")
public class FreeSpecTest extends FreeSpec {

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

	{
		test("intercept by type works", () -> {
			try {
				final InterceptTestException ex = new FreeSpec().intercept(InterceptTestException.class, () -> {
					throw new InterceptTestException("msg");
				});
				assertEquals(ex.getMessage(), "msg");
			} catch (final Throwable e) {
				assertTrue(false, "Expected no exception thrown");
			}
		});

		test("intercept by type works for sub types", () -> {
			try {
				final InterceptTestException ex = new FreeSpec().intercept(InterceptTestException.class, () -> {
					throw new InterceptTestSubException("msg");
				});
				assertEquals(ex.getMessage(), "msg");
			} catch (final Throwable e) {
				assertTrue(false, "Expected no exception thrown");
			}
		});

		test("intercept by type ignores super types",
				() -> {
					try {
						new FreeSpec().intercept(InterceptTestSubException.class, () -> {
							throw new InterceptTestException("msg");
						});
						assertFalse(true);
					} catch (final Throwable e) {
						assertTrue(e instanceof AssertionError, "Expected different exception type");
						assertEquals(
								e.getMessage().trim(),
								"Thrown exception of type [de.tobiasroeser.lambdatest.testng.FreeSpecTest$InterceptTestSubException]"
										+ " does not match expected type [de.tobiasroeser.lambdatest.testng.FreeSpecTest$InterceptTestException]");
					}
				});

		test("intercept with correct type and message",
				() -> {
					try {
						final InterceptTestException ex = new FreeSpec().intercept(InterceptTestException.class,
								"\\Qmsg\\E", () -> {
									throw new InterceptTestException("msg");
								});
						assertEquals(ex.getMessage(), "msg");
					} catch (final Throwable e) {
						assertTrue(false, "Expected no exception thrown");
					}
				});

		test("intercept with correct type but wrong message", () -> {
			try {
				new FreeSpec().intercept(InterceptTestException.class, "Msg", () -> {
					throw new InterceptTestException("msg");
				});
				assertFalse(true);
			} catch (final Throwable e) {
				assertTrue(e instanceof AssertionError, "Expected different exception type");
				assertEquals(e.getMessage().trim(),
						"Exception was thrown with the wrong message: Expected: 'Msg' but got 'msg'.");
			}
		});

		test("intercept with wrong type but correct message",
				() -> {
					try {
						new FreeSpec().intercept(InterceptTestSubException.class, "\\Qmsg\\E", () -> {
							throw new InterceptTestException("msg");
						});
						assertFalse(true);
					} catch (final Throwable e) {
						assertTrue(e instanceof AssertionError, "Expected different exception type");
						assertEquals(
								e.getMessage().trim(),
								"Thrown exception of type [de.tobiasroeser.lambdatest.testng.FreeSpecTest$InterceptTestSubException]"
										+ " does not match expected type [de.tobiasroeser.lambdatest.testng.FreeSpecTest$InterceptTestException]");
					}
				});

	}
}
