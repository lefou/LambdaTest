package de.tobiasroeser.lambdatest;

import java.util.Arrays;

import de.tobiasroeser.lambdatest.testng.FreeSpec;
import org.testng.annotations.Test;
import static de.tobiasroeser.lambdatest.ExpectCollection.expectCollection;
import static org.testng.Assert.assertThrows;

public class ExpectCollectionTest extends FreeSpec {
	public ExpectCollectionTest() {
		section("ExpectCollection size tests", () -> {
			test("empty collection", () -> expectCollection(Arrays.asList()).isEmpty());
			test("test for correct size", () -> expectCollection(Arrays.asList(1)).hasSize(1));
		});

		section("ExpectCollection contains tests", () -> {
			test("empty collection contains nothing", () -> expectCollection(Arrays.asList()).containsNot(1));
			test("[1,2,3] contains 1, 2 and 3", () -> {
				expectCollection(Arrays.asList(1, 2, 3)).contains(1);
				expectCollection(Arrays.asList(1, 2, 3)).contains(2);
				expectCollection(Arrays.asList(1, 2, 3)).contains(3);
				expectCollection(Arrays.asList(1, 2, 3)).containsNot(0);
			});
			test("[a,b,c] contains a, b and c", () -> {
				expectCollection(Arrays.asList("a", "b", "c")).contains("a");
				expectCollection(Arrays.asList("a", "b", "c")).contains("b");
				expectCollection(Arrays.asList("a", "b", "c")).contains("c");
				expectCollection(Arrays.asList("a", "b", "c")).containsNot("x");
			});
		});

		section("ExpectCollection duplicates tests", () -> {
			test("empty list has no duplicates", () -> expectCollection(Arrays.asList()).hasNoDuplicates());
			test("[1,2,3] has no duplicates", () -> expectCollection(Arrays.asList(1, 2, 3)).hasNoDuplicates());
		});

	}

	@Test
	public void testThatIsEmptyFails() {
		assertThrows(AssertionError.class, () -> expectCollection(Arrays.asList(1)).isEmpty());
	}

	@Test
	public void testThatHasSizeFails() {
		assertThrows(AssertionError.class, () -> expectCollection(Arrays.asList(1)).hasSize(2));
	}

	@Test
	public void testThatContainsFails() {
		assertThrows(AssertionError.class, () -> expectCollection(Arrays.asList(1)).contains(2));
	}

	@Test
	public void testThatNotContainsFails() {
		assertThrows(AssertionError.class, () -> expectCollection(Arrays.asList(1)).containsNot(1));
	}

	@Test
	public void testThatHasNoDuplicates() {
		assertThrows(AssertionError.class, () -> expectCollection(Arrays.asList(1, 2, 1)).hasNoDuplicates());
		assertThrows(AssertionError.class, () -> expectCollection(Arrays.asList(2, 2)).hasNoDuplicates());
	}
}
