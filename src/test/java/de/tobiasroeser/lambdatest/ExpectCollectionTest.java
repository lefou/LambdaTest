package de.tobiasroeser.lambdatest;

import static de.tobiasroeser.lambdatest.ExpectCollection.expectCollection;

import java.util.Arrays;
import java.util.Collections;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class ExpectCollectionTest extends FreeSpec {

	public ExpectCollectionTest() {
		setExpectFailFast(true);

		section("ExpectCollection size tests", () -> {
			test("empty collection", () -> expectCollection(Arrays.asList()).isEmpty());
			test("test for correct size", () -> expectCollection(Arrays.asList(1)).hasSize(1));
		});

		section("ExpectCollection.isEmpty", () -> {
			test("for empty collection", () -> expectCollection(Arrays.asList()).isEmpty());
			testFail("for non-empty collection should fail", () -> expectCollection(Arrays.asList("1")).isEmpty());
		});

		section("ExpectCollection.hasSize", () -> {
			test("for empty collection", () -> expectCollection(Collections.emptyList()).hasSize(0));
			test("non-empty collection", () -> expectCollection(Arrays.asList(1, 2, 3)).hasSize(3));
			testFail("wrong size should fail", () -> expectCollection(Arrays.asList("1")).hasSize(2));
			testFail("negative size should fail with IllegalArgumentException", IllegalArgumentException.class,
					() -> expectCollection(Arrays.asList("1")).hasSize(-1));
		});

		section("ExpectCollection.contains", () -> {
			test("[1,2,3] contains 1, 2 and 3", () -> {
				expectCollection(Arrays.asList(1, 2, 3)).contains(1);
				expectCollection(Arrays.asList(1, 2, 3)).contains(2);
				expectCollection(Arrays.asList(1, 2, 3)).contains(3);
			});
			test("[a,b,c] contains a, b and c", () -> {
				expectCollection(Arrays.asList("a", "b", "c")).contains("a");
				expectCollection(Arrays.asList("a", "b", "c")).contains("b");
				expectCollection(Arrays.asList("a", "b", "c")).contains("c");
			});
			testFail("for non-contained element should fail",
					() -> expectCollection(Arrays.asList("1", "2")).contains("3"));
		});

		section("ExpectCollection.containsNot", () -> {
			test("empty collection contains nothing", () -> expectCollection(Arrays.asList()).containsNot(1));
			testFail("should fail for contained elements", () -> expectCollection(Arrays.asList(1)).containsNot(1));

			test("[1,2,3] contains 1, 2 and 3", () -> {
				expectCollection(Arrays.asList(1, 2, 3)).containsNot(0);
			});
			test("[a,b,c] contains a, b and c", () -> expectCollection(Arrays.asList("a", "b", "c")).containsNot("x"));
		});

		section("ExpectCollection.hasNoDuplicates", () -> {
			test("empty list has no duplicates", () -> expectCollection(Arrays.asList()).hasNoDuplicates());
			test("[1,2,3] has no duplicates", () -> expectCollection(Arrays.asList(1, 2, 3)).hasNoDuplicates());
			testFail("for collection with dulicates should fail", () -> {
				expectCollection(Arrays.asList(1, 2, 1)).hasNoDuplicates();
				expectCollection(Arrays.asList(2, 2)).hasNoDuplicates();
			});
		});

	}

	private void testFail(String testName, RunnableWithException testCase) {
		testFail(testName, AssertionError.class, testCase);
	}

	private void testFail(String testName, Class<? extends Throwable> exType, RunnableWithException testCase) {
		test(testName, () -> intercept(exType, testCase));
	}

}
