package de.tobiasroeser.lambdatest.junit5;

import de.tobiasroeser.lambdatest.generic.DefaultReporter;
import de.tobiasroeser.lambdatest.generic.LoggingWrappingReporter;
import de.tobiasroeser.lambdatest.internal.Util;
import org.junit.Assert;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.testng.annotations.Test;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.testng.Assert.assertEquals;

public class RuntimeTest {

    public static class SimpleSuccessTest extends FreeSpec {
        public SimpleSuccessTest() {
            test("should succeed", () -> {
                Assert.assertTrue(true);
            });
        }
    }

    public static class SimpleFailureTest extends FreeSpec {
        public SimpleFailureTest() {
            // We don't want the stacktrace to fool us in the test suite
            setReporter(new LoggingWrappingReporter(new DefaultReporter(System.out, false)));
            test("should fail", () -> {
                Assert.assertTrue(false);
            });
        }
    }

    public static class SimplePendingTest extends FreeSpec {
        public SimplePendingTest() {
            test("should be pending", () -> {
                pending();
                Assert.fail("should not be reached");
            });
        }
    }

    public static class SimplePendingWithReasonTest extends FreeSpec {
        public SimplePendingWithReasonTest() {
            test("should be pending with reason", () -> {
                pending("With Reason");
                Assert.fail("should not be reached");
            });
        }
    }

    public static class SimpleLazyInitTest extends FreeSpec {
        @Override
        protected void initTests() {
            test("should succeed (lazy init)", () -> {
                Assert.assertTrue(true);
            });
        }
    }

    public static class SimpleWithSectionsTest extends FreeSpec {
        protected void initTests() {
            section("section 1", () -> {
                section("section 1.1", () -> {
                    test("test 1.1.1", () -> {
                        Assert.assertTrue(true);
                    });
                });
                section("section 1.2", () -> {
                    test("test 1.2.1 (fail)", () -> {
                        Assert.assertTrue(false);
                    });
                });
            });
            section("section 2", () -> {
                test("test 2.1 (pending)", () -> {
                    pending("pending test");
                });
            });
        }
    }

    private TestExecutionSummary runTestClasses(Class<?>... classes) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(Util.map(classes, c -> selectClass(c)))
                .build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(testPlan);
        return listener.getSummary();
    }

    @Test(groups = {"jupiter"})
    public void testSuccess() {
        TestExecutionSummary summary = runTestClasses(SimpleSuccessTest.class);
        assertEquals(summary.getTestsFoundCount(), 1);
        assertEquals(summary.getTestsSucceededCount(), 1);
        assertEquals(summary.getTestsFailedCount(), 0);
        assertEquals(summary.getTestsSkippedCount(), 0);
    }

    @Test(groups = {"jupiter"})
    public void testFailure() {
        TestExecutionSummary summary = runTestClasses(SimpleFailureTest.class);
        assertEquals(summary.getTestsFoundCount(), 1);
        assertEquals(summary.getTestsSucceededCount(), 0);
        assertEquals(summary.getTestsFailedCount(), 1);
        assertEquals(summary.getTestsSkippedCount(), 0);
    }

    @Test(groups = {"jupiter"})
    public void testPending() {
        TestExecutionSummary summary = runTestClasses(SimplePendingTest.class);
        assertEquals(summary.getTestsFoundCount(), 1);
        assertEquals(summary.getTestsSucceededCount(), 0);
        assertEquals(summary.getTestsFailedCount(), 1);
        assertEquals(summary.getTestsSkippedCount(), 0);
    }

    @Test(groups = {"jupiter"})
    public void testPendingWithReason() {
        TestExecutionSummary summary = runTestClasses(SimplePendingWithReasonTest.class);
        assertEquals(summary.getTestsFoundCount(), 1);
        assertEquals(summary.getTestsSucceededCount(), 0);
        assertEquals(summary.getTestsFailedCount(), 1);
        assertEquals(summary.getTestsSkippedCount(), 0);
    }

    @Test(groups = {"jupiter"})
    public void testLazyInit() {
        TestExecutionSummary summary = runTestClasses(SimpleLazyInitTest.class);
        assertEquals(summary.getTestsFoundCount(), 1);
        assertEquals(summary.getTestsSucceededCount(), 1);
        assertEquals(summary.getTestsFailedCount(), 0);
        assertEquals(summary.getTestsSkippedCount(), 0);
    }

    @Test(groups = {"jupiter"})
    public void testWithSections() {
        TestExecutionSummary summary = runTestClasses(SimpleWithSectionsTest.class);
        assertEquals(summary.getTestsFoundCount(), 3);
        assertEquals(summary.getTestsSucceededCount(), 1);
        assertEquals(summary.getTestsFailedCount(), 2);
        assertEquals(summary.getTestsSkippedCount(), 0);
    }

}