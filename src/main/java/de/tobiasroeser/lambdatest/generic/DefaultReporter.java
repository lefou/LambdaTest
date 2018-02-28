package de.tobiasroeser.lambdatest.generic;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.tobiasroeser.lambdatest.LambdaTestCase;
import de.tobiasroeser.lambdatest.Optional;
import de.tobiasroeser.lambdatest.Reporter;
import de.tobiasroeser.lambdatest.Section;
import de.tobiasroeser.lambdatest.internal.AnsiColor;
import de.tobiasroeser.lambdatest.internal.AnsiColor.Color;

public class DefaultReporter implements Reporter {

	private final AnsiColor ansi = new AnsiColor();
	private final PrintStream out;
	private final boolean showStacktrace;
	private Map<String, Section> lastSuiteSection = new LinkedHashMap<>();

	public DefaultReporter() {
		this(System.out);
	}

	public DefaultReporter(final PrintStream printStream) {
		this(printStream, true);
	}
	
	public DefaultReporter(final PrintStream printStream, final boolean showStacktrace) {
		out = printStream;
		this.showStacktrace = showStacktrace;
	}

	@Override
	public void testStart(final LambdaTestCase test) {
		// we ignore the start for now
	}

	private String indent(final LambdaTestCase test) {
		return indent(test.getSection().orNull());
	}

	private String indent(final Section sectionOrNull) {
		if (sectionOrNull == null) {
			return "";
		} else {
			final StringBuilder sb = new StringBuilder();
			final int size = sectionOrNull.getLevel();
			for (int i = 0; i < size; ++i) {
				sb.append("  ");
			}
			return sb.toString();
		}
	}

	protected Optional<Section> findSameOrInParent(final Section s1, final Section s2) {
		Optional<Section> found = Optional.none();
		if (s1 != null) {
			found = s1.findInParents(s2);
		}
		if (found.isEmpty() && s2 != null && s2.getParent() != null) {
			found = findSameOrInParent(s1, s2.getParent());
		}
		return found;
	}

	protected void reportSectionUntilParent(final String suiteName, final Section section, final Section parent) {
		if (section == null || section.equals(parent))
			return;
		else {
			reportSectionUntilParent(suiteName, section.getParent(), parent);
			out.println(indent(section.getParent()) + ansi.fg(Color.GREEN) + "- " + section.getName() + ansi.reset());
		}
	}

	private void reportSectionOnce(final LambdaTestCase test) {
		final String suiteName = test.getSuiteName();

		final Section lastSection = lastSuiteSection.get(suiteName);
		lastSuiteSection.put(suiteName, test.getSection().orNull());

		if (test.getSection().isDefined() && (lastSection == null || !lastSection.equals(test.getSection().orNull()))) {
			final Optional<Section> sameParent = findSameOrInParent(test.getSection().get(), lastSection);
			reportSectionUntilParent(test.getSuiteName(), test.getSection().orNull(), sameParent.orNull());
		}
	}

	@Override
	public void testSkipped(final LambdaTestCase test, final String message) {
		reportSectionOnce(test);
		final String testName = test.getName();
		if (PENDING_DEFAULT_MSG.equals(message)) {
			out.println(indent(test) + ansi.fg(Color.YELLOW) + "- " + testName + " (pending)" + ansi.reset());
		} else {
			out.println(
					indent(test) + ansi.fg(Color.YELLOW) + "- " + testName + " (pending): " + message + ansi.reset());
		}
	}

	@Override
	public void testFailed(final LambdaTestCase test, final Throwable error) {
		reportSectionOnce(test);
		try {
			out.println(indent(test) + ansi.fg(Color.RED) + "- " + test.getName() + " *** FAILED ***");
			// System.out.println(e.getMessage());
			if (showStacktrace) {
				error.printStackTrace(out);
			} else {
				out.println(error.getClass().getName() + ": " + error.getMessage());
			}
			Throwable oldCause = error;
			Throwable cause = error.getCause();
			// unpack exception stack
			while (cause != null && cause != oldCause) {
				out.print("Caused by: ");
				if (showStacktrace) {
					cause.printStackTrace(out);
				} else {
					out.println(error.getClass().getName() + ": " + error.getMessage());
				}
				oldCause = cause;
				cause = cause.getCause();
			}
		} finally {
			out.print(ansi.reset());
		}
	}

	@Override
	public void testSucceeded(final LambdaTestCase test) {
		reportSectionOnce(test);
		out.println(indent(test) + ansi.fg(Color.GREEN) + "- " + test.getName() + ansi.reset());
	}

	@Override
	public void suiteStart(final String suiteName, final List<? extends LambdaTestCase> tests) {
		out.println("Running " + ansi.fg(Color.CYAN) + tests.size()
				+ ansi.reset() + " tests in " + ansi.fg(Color.CYAN)
				+ suiteName + ansi.reset() + ":");
	}

	@Override
	public void suiteWarning(final String suiteName, final String warning) {
		out.println(suiteName + ": " + warning);
	}

}
