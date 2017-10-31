package de.tobiasroeser.lambdatest.demo;

import static de.tobiasroeser.lambdatest.Expect.*;
//You can also use JUnit based tests with 
//import de.tobiasroeser.lambdatest.junit.FreeSpec; 
import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class SimpleTest extends FreeSpec {
	public SimpleTest() {

		test("1 + 1 = 2", () -> {
			expectEquals(1 + 1, 2);
		});

		test("a pending test", () -> pending());

		test("divide by zero", () -> {
			int a = 2;
			int b = 0;
			intercept(ArithmeticException.class, () -> {
				int c = a / b;
			});
		});

		section("A String should", () -> {
			String aString = "A string";

			test("match certain criteria", () -> {
				expectString(aString)
						.contains("string")
						.containsIgnoreCase("String")
						.startsWith("A")
						.endsWith("ng")
						.hasLength(8);
			});

			test("be not longer than 2", () -> {
				expectString(aString).isLongerThan(2);
			});

		});

		test("demo of a fail", () -> {
			pending("Disabled, as it would fail the project.");
			expectEquals("yes and no", "yes");
		});
	}
}
