package ca.on.oicr.pde.testing.testng.simulation;

import java.util.LinkedList;
import java.util.List;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This is a fake test case that is used by TestCaseReporterTest to test test failures.
 *
 * This class tests:
 *
 * 1) TestCaseReporter reporter class and test failure
 *
 */
@Listeners({ca.on.oicr.pde.testing.testng.TestCaseReporter.class})
public class MultipleFakeFailsImpl {

    @Factory
    @Parameters({"numberOfTests"})
    public Object[] testCaseGenerator(int testCount) {
        List<Object> tests = new LinkedList<>();
        for (int i = 0; i < testCount; i++) {
            tests.add(new MultipleFakeFailsImpl.TestClass(i));
        }
        return tests.toArray();
    }

    public static class TestClass implements ITest {

        private final int testId;

        public TestClass(int id) {
            this.testId = id;
        }

        @Test(groups = {"level1"})
        public void successTestLevel1_1() throws InterruptedException {
            Thread.sleep(100L);
            Assert.assertTrue(true);
        }

        @Test(groups = {"level2"}, dependsOnGroups = "level1")
        public void successTestLevel2_1() throws InterruptedException {
            Thread.sleep(100L);
            Assert.assertTrue(true);
        }

        @Test(groups = {"level3"}, dependsOnGroups = "level2")
        public void failureTestLevel3_1() throws InterruptedException {
            Thread.sleep(100L);
            Assert.assertTrue(false, "There was a fake failure.\nExtra information regarding the failure.");
        }

        //should be skipped
        @Test(groups = {"level4"}, dependsOnGroups = "level3")
        public void successTestLevel4_1() throws InterruptedException {
            Thread.sleep(100L);
            Assert.assertTrue(true);
        }

        @Override
        public String getTestName() {
            return "Test name " + testId;
        }
    }

}
