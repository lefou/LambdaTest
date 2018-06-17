package de.tobiasroeser.lambdatest;

import java.util.Arrays;
import java.util.Collections;

import de.tobiasroeser.lambdatest.testng.FreeSpec;
import static de.tobiasroeser.lambdatest.ExpectCollection.expectCollection;

public class ExpectCollectionTest extends FreeSpec {

	public ExpectCollectionTest() {
		setExpectFailFast(true);

		section("ExpectCollection.isEmpty", () -> {
			test("for empty collection", () -> expectCollection(Collections.emptyList()).isEmpty());
			testFail("for non-empty collection should fail",
					() -> expectCollection(Collections.singletonList("1")).isEmpty());
		});

		section("ExpectCollection.hasSize", () -> {
			test("for empty collection", () -> expectCollection(Collections.emptyList()).hasSize(0));
			test("non-empty collection", () -> expectCollection(Arrays.asList(1, 2, 3)).hasSize(3));
			testFail("wrong size should fail", () -> expectCollection(Collections.singletonList("1")).hasSize(2));
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
			test("empty collection contains nothing", () -> expectCollection(Collections.emptyList()).containsNot(1));
			testFail("should fail for contained elements",
					() -> expectCollection(Collections.singletonList(1)).containsNot(1));

			test("[1,2,3] contains 1, 2 and 3", () -> expectCollection(Arrays.asList(1, 2, 3)).containsNot(0));
			test("[a,b,c] contains a, b and c", () -> expectCollection(Arrays.asList("a", "b", "c")).containsNot("x"));
		});

		section("ExpectCollection.hasNoDuplicates", () -> {
			test("empty list has no duplicates", () -> expectCollection(Collections.emptyList()).hasNoDuplicates());
			test("[1,2,3] has no duplicates", () -> expectCollection(Arrays.asList(1, 2, 3)).hasNoDuplicates());
			testFail("for collection with duplicates should fail", () -> {
				expectCollection(Arrays.asList(1, 2, 1)).hasNoDuplicates();
				expectCollection(Arrays.asList(2, 2)).hasNoDuplicates();
			});
		});

	}

	private void testFail(String testName, RunnableWithException testCase) {
		test(testName, () -> intercept(AssertionError.class, testCase));
	}

}
