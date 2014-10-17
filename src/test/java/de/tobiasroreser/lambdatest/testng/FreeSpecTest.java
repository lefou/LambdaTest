package de.tobiasroreser.lambdatest.testng;

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
				assertTrue(false, "Expected thrown exception");
			} catch (Exception e) {
				assertTrue(e instanceof TestException, "Expected thrown TestException");
			}
		});

		test("intercept by type works for sub types", () -> {
			try {
				new FreeSpec().intercept(InterceptTestException.class, () -> {
					throw new InterceptTestSubException("msg");
				});
				assertTrue(false, "Expected thrown exception");
			} catch (Exception e) {
				assertTrue(e instanceof TestException, "Expected thrown TestException");
			}
		});

		test("intercept by type ignores super types",
				() -> {
					try {
						new FreeSpec().intercept(InterceptTestSubException.class, () -> {
							throw new InterceptTestException("msg");
						});
					} catch (Exception e) {
						assertTrue(e instanceof TestException, "Expected thrown TestException");
						assertEquals(
								e.getMessage().trim(),
								"Thrown exception of type [de.tobiasroreser.lambdatest.testng.FreeSpecTest$InterceptTestSubException]"
										+ " does not match expected type [de.tobiasroreser.lambdatest.testng.FreeSpecTest$InterceptTestException]");
					}
				});

		test("intercept with correct type and message regex", () -> {
			try {
				new FreeSpec().intercept(InterceptTestException.class, "\\Qmsg\\E", () -> {
					throw new InterceptTestSubException("msg");
				});
				assertTrue(false, "Expected thrown exception");
			} catch (Exception e) {
				assertTrue(e instanceof TestException, "Expected thrown TestException");
			}
		});

	}
}
