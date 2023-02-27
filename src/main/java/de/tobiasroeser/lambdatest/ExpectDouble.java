package de.tobiasroeser.lambdatest;

@SuppressWarnings({ "UnusedReturnValue", "WeakerAccess" })
public class ExpectDouble extends ExpectBase<ExpectDouble> {
	private final Double actual;

	/**
	 * Creates and instance for the given non-null map.
	 */
	public ExpectDouble(Double actual) {
		check(actual != null, "Actual is not a Double but null");
		this.actual = actual;
	}

	/**
	 * Check for non-null {@link Double} and provided further checks on the actual
	 * double in a fluent API.
	 *
	 * @param actual
	 *            The double to check.
	 * @return A {@link ExpectDouble} to express further expectations on the actual
	 *         double.
	 */
	public static ExpectDouble expectDouble(Double actual) {
		return new ExpectDouble(actual);
	}

	/**
	 * Checks, if actual is close to expected regarding eps.
	 *
	 * Mathematically |actual - expected| &lt; eps
	 *
	 * @param expected
	 *            The expected double near of actual.
	 * @param eps
	 *            Epsilon value.
	 */
	public ExpectDouble isCloseTo(final Double expected, Double eps) {
		return check(Math.abs(actual - expected) < eps,
				"Actual double {0} is not close to expected double {1} regarding epsilon {2}.", actual, expected, eps);
	}

	/**
	 * Checks, if actual is not close to expected regarding eps.
	 *
	 * Mathematically |actual - expected| &gt;= eps
	 *
	 * @param expected
	 *            The expected double near of actual.
	 * @param eps
	 *            Epsilon value.
	 */
	public ExpectDouble isNotCloseTo(final Double expected, Double eps) {
		return check(Math.abs(actual - expected) >= eps,
				"Actual double {0} is close to expected double {1} regarding epsilon {2}.", actual, expected, eps);
	}

	/**
	 * Checks, if actual is not a number (NaN).
	 */
	public ExpectDouble isNaN() {
		return check(Double.isNaN(actual),
				"Actual double {0} is not NaN.", actual);
	}

  /**
	 * Checks, if actual is not not a number (NaN).
	 */
	public ExpectDouble isNotNaN() {
		return check(!Double.isNaN(actual),
				"Actual double {0} is NaN.", actual);
	}

  /**
 	 * Checks, if actual is between start (inclusive) and end (exclusive).
 	 *
 	 * Mathematically start &lt;= actual &lt; end.
 	 *
 	 * @param start
 	 *            Interval start (inclusive).
 	 * @param end
 	 *            Interval end (exclusive).
 	 */
	public ExpectDouble isBetween(Double start, Double end) {
		return check(start <= actual && actual < end,
				"Actual double {0} is not between {1} and {2}, but should be {1} <= {0} < {2}.", actual, start, end);
	}

  /**
 	 * Checks, if actual is not between start (inclusive) and end (exclusive).
 	 *
 	 * @param start
 	 *            Interval start (inclusive).
 	 * @param end
 	 *            Interval end (exclusive).
 	 */
	public ExpectDouble isNotBetween(Double start, Double end) {
		return check(!(start <= actual && actual < end),
				"Actual double {0} is between {1} and {2}, but not should be {1} <= {0} < {2}.", actual, start,
				end);
	}
}
