package de.tobiasroeser.lambdatest.generic;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import de.tobiasroeser.lambdatest.Optional;
import de.tobiasroeser.lambdatest.Section;
import de.tobiasroeser.lambdatest.generic.DefaultReporter;

public class DefaultReporterTest {

	@DataProvider
	public Object[][] dataFindSameOrInParent() {
		final Section s1 = new Section("s1", null);
		final Section s1a = new Section("s1a", s1);
		final Section s1a1 = new Section("s1a1", s1a);
		final Section s1a2 = new Section("s1a2", s1a);
		final Section s1b = new Section("s1b", s1);
		final Section s1b1 = new Section("s1b1", s1b);
		final Section s2 = new Section("s2", null);
		final Section s2a = new Section("s2a", s2);
		final Section s2a1 = new Section("s2a1", s2a);

		return new Object[][] {
				new Object[] { null, null, null },
				new Object[] { s1, s1, s1 },
				new Object[] { s1, null, null },
				new Object[] { null, s1, null },
				new Object[] { s1, s2, null },
				new Object[] { s1a, s2a, null },
				new Object[] { s1a, s1b, s1 },
				new Object[] { s1a1, s1a2, s1a },
				new Object[] { s1a1, s1b1, s1 },
				new Object[] { s1a1, s2a1, null }
		};
	}

	@Test(dataProvider = "dataFindSameOrInParent")
	public void testFindSameOrInParent(final Section s1, final Section s2, final Section expected) {
		final DefaultReporter reporter = new DefaultReporter();
		Assert.assertEquals(reporter.findSameOrInParent(s1, s2), Optional.lift(expected));
		Assert.assertEquals(reporter.findSameOrInParent(s2, s1), Optional.lift(expected));
	}

}
