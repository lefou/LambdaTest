package de.tobiasroreser.lambdatest.testng;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.TestException;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

@SuppressWarnings("serial")
public class FreeSpecTest extends FreeSpec {

	public static class InterceptTestException extends RuntimeException {
		public InterceptTestException(String msg) {
			super(msg);
		}
	}

	public static class InterceptTestSubException extends InterceptTestException {
		public InterceptTestSubException(String msg) {
			super(msg);
		}
	}

	{
		test("intercept by type works", () -> {
			try {
				new FreeSpec().intercept(InterceptTestException.class, () -> {
					throw new InterceptTestException("msg");
				});
			} catch (Exception e) {
				assertTrue(false, "Expected no exception thrown");
			}
		});

		test("intercept by type works for sub types", () -> {
			try {
				new FreeSpec().intercept(InterceptTestException.class, () -> {
					throw new InterceptTestSubException("msg");
				});
			} catch (Exception e) {
				assertTrue(false, "Expected no exception thrown");
			}
		});

		test("intercept by type ignores super types",
				() -> {
					try {
						new FreeSpec().intercept(InterceptTestSubException.class, () -> {
							throw new InterceptTestException("msg");
						});
					} catch (Exception e) {
						assertTrue(e instanceof TestException, "Expected different exception type");
						assertEquals(
								e.getMessage().trim(),
								"Thrown exception of type [de.tobiasroreser.lambdatest.testng.FreeSpecTest$InterceptTestSubException]"
										+ " does not match expected type [de.tobiasroreser.lambdatest.testng.FreeSpecTest$InterceptTestException]");
					}
				});

		test("intercept with correct type and message", () -> {
			try {
				new FreeSpec().intercept(InterceptTestException.class, "\\Qmsg\\E", () -> {
					throw new InterceptTestException("msg");
				});
			} catch (Exception e) {
				assertTrue(false, "Expected no exception thrown");
			}
		});

		test("intercept with correct type but wrong message", () -> {
			try {
				new FreeSpec().intercept(InterceptTestException.class, "Msg", () -> {
					throw new InterceptTestException("msg");
				});
			} catch (Exception e) {
				assertTrue(e instanceof TestException, "Expected different exception type");
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
					} catch (Exception e) {
						assertTrue(e instanceof TestException, "Expected different exception type");
						assertEquals(
								e.getMessage().trim(),
								"Thrown exception of type [de.tobiasroreser.lambdatest.testng.FreeSpecTest$InterceptTestSubException]"
										+ " does not match expected type [de.tobiasroreser.lambdatest.testng.FreeSpecTest$InterceptTestException]");
					}
				});

	}
}
