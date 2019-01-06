package de.tobiasroeser.lambdatest;

import static de.tobiasroeser.lambdatest.Expect.expectDouble;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class ExpectDoubleTest extends FreeSpec {
	public ExpectDoubleTest() {
		setExpectFailFast(true);

		section("ExpectDouble.isCloseTo", () -> {
			test("for x=0.001 ~ 0.001000001 with eps = 0.000001",
					() -> expectDouble(0.001d).isCloseTo(0.001000001d, 0.000001d));
			testFail("fail for 0.1 ~ 0.11 with eps = 0.001", () -> expectDouble(0.1d).isCloseTo(0.11d, 0.001d));
		});

    section("ExpectDouble.isNotCloseTo", () -> {
			test("for x=0.1 ~ 0.11 with eps = 0.001", () -> expectDouble(0.01d).isNotCloseTo(0.11d, 0.001d));
			testFail("fail for x=0.001 ~ 0.001000001 with eps = 0.000001",
					() -> expectDouble(0.001d).isNotCloseTo(0.001000001d, 0.000001d));
		});

    section("ExpectDouble.isBetween", () -> {
  			test("for 0.1 <= x=0.1 < 0.11",
  					() -> expectDouble(0.1d).isBetween(0.1d, 0.11d));
  			testFail("for 0.1 <= x=0.1 < 0.1", 	() -> expectDouble(0.1d).isBetween(0.1d, 0.1d));
  		});

    section("ExpectDouble.isNotBetween", () -> {
  			test("for 0.1 <= x=0.01 < 0.11",
  					() -> expectDouble(0.01d).isNotBetween(0.1d, 0.11d));
  			testFail("fail for 0.1 <= x=0.1 < 0.11", 	() -> expectDouble(0.1d).isNotBetween(0.1d, 0.11d));
  		});

    section("ExpectDouble.isNaN", () -> {
  			test("for Double.Nan",
  					() -> expectDouble(Double.NaN).isNaN());
  			testFail("fail for 0.1", 	() -> expectDouble(0.1d).isNaN());
  		});

    section("ExpectDouble.isNotNaN", () -> {
  			test("for 0.1",
  					() -> expectDouble(0.1d).isNotNaN());
  			testFail("fail for Double.NaN", 	() -> expectDouble(Double.NaN).isNotNaN());
  		});
	}

	private void testFail(final String testName, final RunnableWithException testCase) {
		test(testName, () -> intercept(AssertionError.class, testCase));
	}
}
