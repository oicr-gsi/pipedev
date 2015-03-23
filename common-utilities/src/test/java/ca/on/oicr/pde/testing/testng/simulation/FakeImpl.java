package ca.on.oicr.pde.testing.testng.simulation;

import java.util.LinkedList;
import java.util.List;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This is a fake test case that is used by TestCaseReporterTest.
 *
 * This class tests:
 *
 * 1) TestCaseReporter reporter class
 *
 * This class also demonstrates the how to use: TestNG factories, data providers, parallelization, and the ITest interface.
 */
@Listeners({ca.on.oicr.pde.testing.testng.TestCaseReporter.class})
public class FakeImpl {

    @Factory
    @Parameters({"numberOfTests", "numberOfTestMethods"})
    public Object[] testCaseGenerator(int testCount, int testMethods) {
        List<Object> tests = new LinkedList<>();
        for (int i = 0; i < testCount; i++) {
            tests.add(new TestClass(i, testMethods));
        }
        return tests.toArray();
    }

    public static class TestClass implements ITest {

        private final int testId;
        private int numberOfTestMethods = 1;

        @DataProvider(name = "testCaseGenerator")
        public Object[][] testMethodGenerator() {
            int size = numberOfTestMethods;
            int argc = 2;
            Object[][] methodParams = new Object[size][argc];
            for (int i = 0; i < size; i++) {
                methodParams[i][0] = i;
                methodParams[i][1] = Integer.toString(i);
            }
            return methodParams;
        }

        public TestClass(int id, int numberOfTestMethods) {
            this.testId = id;
            this.numberOfTestMethods = numberOfTestMethods;
        }

        @Test(dataProvider = "testCaseGenerator")
        public void sleepTest(int arg1, String arg2) throws InterruptedException {
            //Simulate computation time
            Thread.sleep(1000L);
            
            Assert.assertEquals(Integer.toString(arg1), arg2);
        }

        @Override
        public String getTestName() {
            return "Fake test case " + testId;
        }

    }

}
