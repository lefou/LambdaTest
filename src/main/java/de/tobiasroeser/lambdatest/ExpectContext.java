package de.tobiasroeser.lambdatest;

import java.util.LinkedList;
import java.util.List;

import de.tobiasroeser.lambdatest.internal.Util;

public class ExpectContext {

	private static ThreadLocal<ExpectContext> threadContext = new ThreadLocal<ExpectContext>();

	/* package */ static ExpectContext threadContext() {
		return threadContext.get();
	}

	public static void setup(final boolean failEarly) {
		if (threadContext.get() != null) {
			System.out.println("Warning: Overriding already setup expect context");
		}
		threadContext.set(new ExpectContext(failEarly));
	}

	public static void finish() {
		final ExpectContext context = threadContext.get();
		threadContext.set(null);
		if (context != null) {
			final List<AssertionError> errors = context.getErrors();
			if (errors.isEmpty()) {
				return;
			} else if (errors.size() == 1) {
				throw errors.get(0);
			} else {
				// TODO: create a multi-Exception
				final List<String> formatted = Util.map(errors, error -> {
					final String msg = error.getClass().getName() + ": " + error.getMessage() + "\n\tat " +
							Util.mkString(error.getStackTrace(), "\n\tat ");
					return msg;
				});

				throw new AssertionError("" + errors.size()
						+ " expectations failed\n--------------------------------------------------\n" +
						Util.mkString(formatted, "\n\n") + "\n--------------------------------------------------");
			}
		}
	}

	public static void clear() {
		threadContext.set(null);
	}

	/* package */ static void handleAssertionError(AssertionError e) {
		final ExpectContext context = threadContext.get();
		if (context != null && !context.getFailEarly()) {
			context.addAssertionError(e);
		} else {
			throw e;
		}
	}

	// END OF STATIC PART

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
