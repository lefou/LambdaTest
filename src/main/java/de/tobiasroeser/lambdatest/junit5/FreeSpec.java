package de.tobiasroeser.lambdatest.junit5;

import de.tobiasroeser.lambdatest.Optional;
import de.tobiasroeser.lambdatest.*;
import de.tobiasroeser.lambdatest.generic.DefaultTestCase;
import de.tobiasroeser.lambdatest.generic.FreeSpecBase;
import de.tobiasroeser.lambdatest.internal.Util;
import org.junit.jupiter.api.*;
import org.opentest4j.TestSkippedException;

import java.util.*;

public class FreeSpec extends FreeSpecBase implements LambdaTest {

    private volatile boolean testNeverRun = true;

    @Override
    public void pending(final String reason) {
        throw new TestSkippedException(reason);
    }

    private List<Section> sectionsOf(LambdaTestCase testCase) {
        final LinkedList<Section> sections = new LinkedList<>();
        if (testCase.getSection().isDefined()) {
            Section section = testCase.getSection().get();
            while (section != null) {
                sections.add(0, section);
                section = section.getParent();
            }
        }
        return sections;
    }

    private DynamicTest testFor(DefaultTestCase testCase) {
        return DynamicTest.dynamicTest(testCase.getName(), () -> {
            runTestCase(testCase);
        });
    }

    @TestFactory
    @DisplayName("FreeSpec")
    public Iterable<DynamicNode> testFactory() {
        final List<DefaultTestCase> testCases = getTestCases();
        List<Section> sections = Util.distinct(Util.flatMap(testCases, tc -> sectionsOf(tc)));

        final LinkedHashMap<Section, DynamicContainer> containers = new LinkedHashMap<>();
        final Map<Optional<Section>, List<DefaultTestCase>> testsBySection = Util.groupBy(
                testCases,
                tc -> tc.getSection()
        );

        class CreateContainer {
            DynamicContainer create(Section section) {
                if (!containers.containsKey(section)) {
                    // completely populate this section and recurse into subsections
                    final List<Section> children = Util.filter(sections, sub -> sub.getParent() != null && sub.getParent().equals(section));
                    final List<DynamicContainer> subContainer = Util.map(children, c -> create(c));

                    final List<DefaultTestCase> sectionTests = testsBySection.getOrDefault(Optional.lift(section), Collections.emptyList());
                    final List<DynamicTest> subTests = Util.map(sectionTests, t -> testFor(t));

                    DynamicContainer cont = DynamicContainer.dynamicContainer(section.getName(), Util.concat(subContainer, subTests));
                    containers.put(section, cont);
                    return cont;
                } else {
                    return containers.get(section);
                }
            }
        }
        final CreateContainer createContainer = new CreateContainer();

        // create all containers
        Util.foreach(sections, s -> {
            createContainer.create(s);
        });


        final List<DynamicNode> topLevelContainers = Util.map(
                Util.filter(sections, s -> s.getParent() == null),
                s -> containers.get(s)
        );
        final List<DynamicNode> topLevelTests = Util.map(
                Util.filter(testCases, tc -> !tc.getSection().isDefined()),
                t -> testFor(t)
        );

        List<DynamicNode> res = Util.concat(topLevelContainers, topLevelTests);
        return res;
    }

    private void runTestCase(DefaultTestCase testCase) throws Throwable {
        if (testNeverRun) {
            synchronized (this) {
                if (testNeverRun) {
                    getReporter().suiteStart(getSuiteName(), getTestCases());
                    testNeverRun = false;
                }
            }
        }

        try {
            ExpectContext.setup(getExpectFailFast());
            Throwable uncaughtTestError = null;
            Throwable delayedTestError = null;
            try {
                getReporter().testStart(testCase);
                testCase.getTest().run();
            } catch (final Throwable t) {
                uncaughtTestError = t;
            }
            try {
                ExpectContext.finish();
            } catch (final Throwable t) {
                delayedTestError = t;
            }
            if (uncaughtTestError != null && delayedTestError != null) {
                throw new AssertionError(
                        "An error occurred (see root cause) after some expectations failed. Failed Expectations:\n"
                                + delayedTestError.getMessage(),
                        uncaughtTestError
                );
            } else if (uncaughtTestError != null) {
                // if this was a SkipException, we still detect it, else some
                // other errors occurred before
                throw uncaughtTestError;
            } else if (delayedTestError != null) {
                throw delayedTestError;
            }
            getReporter().testSucceeded(testCase);
        } catch (final TestSkippedException e) {
            getReporter().testSkipped(testCase, e.getMessage());
            throw e;
        } catch (final Throwable e) {
            getReporter().testFailed(testCase, e);
            throw e;
        }
    }
}
