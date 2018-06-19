package de.tobiasroeser.lambdatest;

import static de.tobiasroeser.lambdatest.ExpectCollection.expectCollection;

import java.util.Arrays;
import java.util.Collections;

import de.tobiasroeser.lambdatest.proxy.TestProxy;
import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class ExpectCollectionTest extends FreeSpec {

	public static interface TypeA {
	}

	public ExpectCollectionTest() {
		setExpectFailFast(true);
		
		test("EcpectCollection creation", () -> expectCollection(Arrays.asList()));
		testFail("EcpectCollection creation with null should fail", 
				"\\QActual is not a Collection but null.\\E",
				() -> expectCollection(null));
		
		section("ExpectCollection size tests", () -> {
			test("empty collection", () -> expectCollection(Arrays.asList()).isEmpty());
			test("test for correct size", () -> expectCollection(Arrays.asList(1)).hasSize(1));
		});

		section("ExpectCollection.isEmpty", () -> {
			test("for empty collection", () -> expectCollection(Arrays.asList()).isEmpty());
			testFail("for non-empty collection should fail",
					"\\QActual collection is not empty but has a size of 1.\nActual: [1]\\E",
					() -> expectCollection(Collections.singletonList("1")).isEmpty());
		});

		section("ExpectCollection.hasSize", () -> {
			test("for empty collection", () -> expectCollection(Collections.emptyList()).hasSize(0));
			test("non-empty collection", () -> expectCollection(Arrays.asList(1, 2, 3)).hasSize(3));
			testFail("wrong size should fail",
					"\\QActual collection has not expected size of 2, actual size: 1.\nActual: [1]\\E",
					() -> expectCollection(Collections.singletonList("1")).hasSize(2));
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

			// Reference
			final TypeA a1 = new TypeA() {
			};
			final TypeA a2 = new TypeA() {
			};
			final TypeA a3 = new TypeA() {
			};
			test("[a1,a2] contains a1", () -> expectCollection(Arrays.asList(a1, a2)).contains(a1));
			test("[a1,a2] contains a2", () -> expectCollection(Arrays.asList(a1, a2)).contains(a2));
			testFail("[a1,a2] contains a3 should fail",
					"\\QActual collection does not contain expected element \"de.tobiasroeser.lambdatest.ExpectCollectionTest\\E.*",
					() -> expectCollection(Arrays.asList(a1, a2)).contains(a3));

			// Proxies fail equality
			final TypeA b1 = TestProxy.proxy(TypeA.class, new Object() {
				@Override
				public boolean equals(Object obj) {
					return super.equals(obj);
				}
			});
			final TypeA b2 = TestProxy.proxy(TypeA.class, new Object() {
				@Override
				public boolean equals(Object obj) {
					return super.equals(obj);
				}
			});
			testFail("[b1,b2] contains b1 fails with proxies",
					() -> expectCollection(Arrays.asList(b1, b2)).contains(b1));
			testFail("[b1,b2] contains b2 fails with proxies",
					() -> expectCollection(Arrays.asList(b1, b2)).contains(b2));
		});

		section("ExpectCollection.containsIdentical", () -> {

			// Reference
			final TypeA a1 = new TypeA() {
			};
			final TypeA a2 = new TypeA() {
			};
			final TypeA a3 = new TypeA() {
			};
			test("[a1,a2] contains a1", () -> expectCollection(Arrays.asList(a1, a2)).containsIdentical(a1));
			test("[a1,a2] contains a2", () -> expectCollection(Arrays.asList(a1, a2)).containsIdentical(a2));
			testFail("[a1,a2] contains not a3", () -> expectCollection(Arrays.asList(a1, a2)).containsIdentical(a3));

			// Proxies fail equality, but not identity
			final TypeA b1 = TestProxy.proxy(TypeA.class);
			final TypeA b2 = TestProxy.proxy(TypeA.class);
			final TypeA b3 = TestProxy.proxy(TypeA.class);
			test("[b1,b2] contains b1", () -> expectCollection(Arrays.asList(b1, b2)).containsIdentical(b1));
			test("[b1,b2] contains b2", () -> expectCollection(Arrays.asList(b1, b2)).containsIdentical(b2));
			testFail("[b1,b2] contains b3 should fail",
					() -> expectCollection(Arrays.asList(b1, b2)).containsIdentical(b3));
		});

		section("ExpectCollection.containsNot", () -> {
			test("empty collection contains nothing", () -> expectCollection(Collections.emptyList()).containsNot(1));
			testFail("should fail for contained elements",
					() -> expectCollection(Collections.singletonList(1)).containsNot(1));

			test("[1,2,3] contains 1, 2 and 3", () -> expectCollection(Arrays.asList(1, 2, 3)).containsNot(0));
			test("[a,b,c] contains a, b and c", () -> expectCollection(Arrays.asList("a", "b", "c")).containsNot("x"));
		});

		section("ExpectCollection.containsNotIdentical", () -> {
			test("empty collection contains nothing", () -> expectCollection(Arrays.asList()).containsNotIdentical(1));
			testFail("should fail for contained elements",
					() -> expectCollection(Arrays.asList(1)).containsNotIdentical(1));
			// Proxies fail equality, but not identity
			final TypeA b1 = TestProxy.proxy(TypeA.class);
			final TypeA b2 = TestProxy.proxy(TypeA.class);
			final TypeA b3 = TestProxy.proxy(TypeA.class);
			test("[b1,b2] contains not b3",
					() -> expectCollection(Arrays.asList(b1, b2)).containsNotIdentical(b3));
			testFail("[b1,b2] contains not b1 should fail",
					() -> expectCollection(Arrays.asList(b1, b2)).containsNotIdentical(b1));
			testFail("[b1,b2] contains not b2 should fail",
					() -> expectCollection(Arrays.asList(b1, b2)).containsNotIdentical(b2));
		});

		section("ExpectCollection.hasNoDuplicates", () -> {
			test("empty list has no duplicates", () -> expectCollection(Collections.emptyList()).hasNoDuplicates());
			test("[1,2,3] has no duplicates", () -> expectCollection(Arrays.asList(1, 2, 3)).hasNoDuplicates());
			testFail("for collection with duplicates should fail", () -> {
				expectCollection(Arrays.asList(1, 2, 1)).hasNoDuplicates();
				expectCollection(Arrays.asList(2, 2)).hasNoDuplicates();
			});
		});

		section("ExpectCollection.hasDuplicates", () -> {
			testFail("negative duplicate count should fail", IllegalArgumentException.class,
					() -> expectCollection(Arrays.asList(1)).hasDuplicates(-1));

			section("ExpectCollection.hasDuplicates(0)", () -> {
				test("empty list has no duplicates", () -> expectCollection(Arrays.asList()).hasNoDuplicates());
				test("[1,2,3] has no duplicates", () -> expectCollection(Arrays.asList(1, 2, 3)).hasNoDuplicates());
				testFail("for collection with dulicates should fail", () -> {
					expectCollection(Arrays.asList(1, 2, 1)).hasNoDuplicates();
					expectCollection(Arrays.asList(2, 2)).hasNoDuplicates();
				});
			});
			section("ExpectCollection.hasDuplicates(n > 0)", () -> {
				testFail("always fails for empty list", () -> expectCollection(Arrays.asList()).hasDuplicates(1));
				testFail("[1,2,3] has not 1 duplicates",
						() -> expectCollection(Arrays.asList(1, 2, 3)).hasDuplicates(1));
				test("[1,2,3,1] has 1 duplicates", () -> expectCollection(Arrays.asList(1, 2, 3, 1)).hasDuplicates(1));
				test("[1,2,2,1] has 2 duplicates", () -> expectCollection(Arrays.asList(1, 2, 2, 1)).hasDuplicates(2));
			});
		});

	}

	@Deprecated
	private void testFail(String testName, RunnableWithException testCase) {
		test(testName, () -> intercept(AssertionError.class, testCase));
	}

	private void testFail(String testName, String msgRegex, RunnableWithException testCase) {
		test(testName, () -> intercept(AssertionError.class, msgRegex, testCase));
	}

	private void testFail(String testName, Class<? extends Throwable> exType, RunnableWithException testCase) {
		test(testName, () -> intercept(exType, testCase));
	}

}
