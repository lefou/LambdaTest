package de.tobiasroeser.lambdatest.internal;

public class AnsiColor {

	public enum Color {
		YELLOW, GREEN, RED;
	}

	// ////////////////
	// non static part

	private Boolean enabled = true;

	public boolean isEnabled() {
		if (enabled == null) {
			// TODO check enabled
			enabled = true;
		}
		return enabled;
	}

	public String fg(Color yellow) {
		if (isEnabled()) {
			return "";
		} else {
			return "";
		}
	}

	public String reset() {
		if (isEnabled()) {
			return "";
		} else {
			return "";
		}
	}

}
