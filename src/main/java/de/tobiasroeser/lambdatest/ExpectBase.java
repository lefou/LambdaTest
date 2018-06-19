package de.tobiasroeser.lambdatest;

class ExpectBase<T extends ExpectBase<T>> {

	protected T check(final boolean cond, final String msg, final Object... args) {
		if (!cond) {
			try {
				Assert.fail(null, msg, args);
			} catch (final AssertionError e) {
				ExpectContext.handleAssertionError(e);
			}
		}
		@SuppressWarnings("unchecked")
		final T t = (T) this;
		return t;
	}

}
