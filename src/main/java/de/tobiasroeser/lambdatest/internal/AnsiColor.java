package de.tobiasroeser.lambdatest.internal;

public class AnsiColor {

	private static final char ESC = 27;
	private static final String CSI = ESC + "[";

	public enum Color {
		BLACK(0),
		RED(1),
		GREEN(2),
		YELLOW(3),
		BLUE(4),
		MAGENTA(5),
		CYAN(6),
		WHITE(7);

		public int code;

		private Color(int code) {
			this.code = code;
		}
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

	public String fg(Color color) {
		if (isEnabled()) {
			return CSI + (30 + color.code) + "m";
		} else {
			return "";
		}
	}

	public String fgBright(Color color) {
		if (isEnabled()) {
			return CSI + (30 + color.code) + ";1m";
		} else {
			return "";
		}
	}

	public String reset() {
		if (isEnabled()) {
			return CSI + "0m";
		} else {
			return "";
		}
	}

}
