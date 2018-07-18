package de.tobiasroeser.lambdatest;

import static de.tobiasroeser.lambdatest.Expect.expectMap;

import java.util.LinkedHashMap;
import java.util.Map;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class ExpectMapTest extends FreeSpec {

	public ExpectMapTest() {
		setExpectFailFast(true);

		section("ExpectMap.isEmpty", () -> {
			test("for empty map", () -> expectMap(mapOf()).isEmpty());
			testFail("for non-empty map should fail",
					() -> expectMap(mapOf(1, 1)).isEmpty());
		});

		section("ExpectMap.hasSize", () -> {
			test("for empty map", () -> expectMap(mapOf()).hasSize(0));
			test("non-empty map", () -> expectMap(mapOf(1, 1, 2, 2, 3, 3)).hasSize(3));
			testFail("wrong size should fail", () -> expectMap(mapOf(1, 1)).hasSize(2));
		});

		section("ExpectMap.contains", () -> {
			test("for existing key value pairs", () -> {
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).contains(1, 1);
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).contains(2, 2);
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).contains(3, 3);
			});
			testFail("for non-contained key should fail", () -> expectMap(mapOf(1, 1, 2, 2, 3, 3)).contains(4, 1));
			testFail("for non-contained value should fail", () -> expectMap(mapOf(1, 1, 2, 2, 3, 3)).contains(1, 4));
			testFail("for null value should fail", () -> expectMap(mapOf(1, 1, 2, 2, 3, 3)).contains(1, null));
			test("for null-values", () -> {
				expectMap(mapOf(1, 1, 2, null)).contains(1, 1);
				expectMap(mapOf(1, 1, 2, null)).contains(2, null);
			});
		});

		section("ExpectMap.containsKey", () -> {
			test("[1->1,2->2,3->3] containsKeys 1, 2 and 3", () -> {
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).containsKey(1);
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).containsKey(2);
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).containsKey(3);
			});

			testFail("for non-contained element should fail",
					() -> expectMap(mapOf(1, 1, 2, 2, 3, 3)).containsKey(4));
		});

		section("ExpectMap.containsNotKey", () -> {
			test("[1->1,2->2,3->3] containsNotKeys 4", () -> expectMap(mapOf(1, 1, 2, 2, 3, 3)).containsNotKey(4));

			testFail("for contained element should fail",
					() -> expectMap(mapOf(1, 1, 2, 2, 3, 3)).containsNotKey(3));
		});

		section("ExpectMap.values", () -> {
			test("should delegate to ExpectCollection", () -> {
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).values().contains(1);
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).values().contains(2);
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).values().contains(3);
			});
		});

		section("ExpectMap.keys", () -> {
			test("should delegate to ExpectCollection", () -> {
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).keys().contains(1);
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).keys().contains(2);
				expectMap(mapOf(1, 1, 2, 2, 3, 3)).keys().contains(3);
			});
		});

		section("Expect.expectMap", () -> {
			test("should delegate to ExpectMap", () -> {
				Expect.expectMap(mapOf(1, 1)).containsKey(1);
			});
		});
	}

	private void testFail(final String testName, final RunnableWithException testCase) {
		test(testName, () -> intercept(AssertionError.class, testCase));
	}

	private static <K> Map<K, K> mapOf(final K... ks) {
		if (ks.length % 2 != 0) {
			new AssertionError("parameter count must be even");
		}
		final LinkedHashMap<K, K> map = new LinkedHashMap<>();
		for (int i = 0; i < ks.length; i = i + 2) {
			map.put(ks[i], ks[i + 1]);
		}
		return map;
	}

}
