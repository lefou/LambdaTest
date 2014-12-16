package de.tobiasroeser.lambdatest;

import java.util.LinkedList;
import java.util.List;

class ExpectContext {

	private final boolean failEarly;
	private final List<AssertionError> errors = new LinkedList<>();

	public ExpectContext(final boolean failEarly) {
		this.failEarly = failEarly;
	}

	public boolean getFailEarly() {
		return failEarly;
	}

	public void addAssertionError(final AssertionError error) {
		errors.add(error);
	}

	public List<AssertionError> getErrors() {
		return errors;
	}
}
