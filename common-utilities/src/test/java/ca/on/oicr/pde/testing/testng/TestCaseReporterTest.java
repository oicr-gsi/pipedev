package ca.on.oicr.pde.testing.testng;

import ca.on.oicr.pde.diff.ObjectDiff;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class TestCaseReporterTest {

    TestNG t;
    TestListenerAdapter tla;

    @BeforeMethod
    public void setupTestNg(ITestContext context) {
        t = new TestNG();
        t.setOutputDirectory(context.getOutputDirectory());

        tla = new TestListenerAdapter();
        t.addListener(tla);
    }

    @Test
    public void testCaseReporterTestOnTestSuccesses() {

        int numberOfTestCases = 10;
        int numberOfTestMethods = 2;

        //Setup test suite
        XmlSuite testSuite = new XmlSuite();
        testSuite.setThreadCount(numberOfTestCases * numberOfTestMethods);
        //testSuite.setParallel(XmlSuite.PARALLEL_CLASSES); // cases * methods = seconds
        //testSuite.setParallel(XmlSuite.PARALLEL_INSTANCES); // methods = seconds
        //testSuite.setParallel(XmlSuite.PARALLEL_METHODS); // methods = seconds ????
        //testSuite.setParallel(XmlSuite.PARALLEL_TESTS); // cases * methods = seconds
        //testSuite.setParallel(XmlSuite.PARALLEL_NONE); // cases * methods = seconds
        testSuite.setParallel(XmlSuite.PARALLEL_METHODS); //run time should be about ~2s (2 methods * 1s)
        XmlTest testCase = new XmlTest(testSuite);
        Map<String, String> params = new HashMap<String, String>();
        params.put("numberOfTests", Integer.toString(numberOfTestCases));
        params.put("numberOfTestMethods", Integer.toString(numberOfTestMethods));
        testCase.setParameters(params);
        testCase.setXmlClasses(Arrays.asList(new XmlClass(ca.on.oicr.pde.testing.testng.simulation.FakeImpl.class)));

        //Execute test suite
        t.setXmlSuites(Arrays.asList(testSuite));
        t.run();

        ITestContext tc = tla.getTestContexts().get(0);
        Assert.assertEquals(tla.getTestContexts().size(), 1, "The assumption that there is only one test listener no longer holds");

        Assert.assertEquals(tc.getFailedTests().size(), 0);
        Assert.assertEquals(tc.getSkippedTests().size(), 0);
        Assert.assertEquals(tc.getPassedTests().size(), numberOfTestCases * numberOfTestMethods);

        Assert.assertEquals(t.getStatus(), 0);
    }

    @Test
    public void testCaseReporterTestOnTestFailures() {

        XmlSuite testSuite = new XmlSuite();
        XmlTest testCase = new XmlTest(testSuite);
        testCase.setXmlClasses(Arrays.asList(new XmlClass(ca.on.oicr.pde.testing.testng.simulation.FakeFailImpl.class)));

        //Execute test suite
        t.setXmlSuites(Arrays.asList(testSuite));
        t.run();

        ITestContext tc = tla.getTestContexts().get(0);
        Assert.assertEquals(tla.getTestContexts().size(), 1, "The assumption that there is only one test listener no longer holds");

        Assert.assertEquals(tc.getFailedTests().size(), 1);
        Assert.assertEquals(tc.getSkippedTests().size(), 0);
        Assert.assertEquals(tc.getPassedTests().size(), 0);

        Assert.assertEquals(t.getStatus(), 1);

    }

    @Test
    public void testCaseReporterGetNameNPE_PDE813() {

        XmlSuite testSuite = new XmlSuite();
        testSuite.setName("Fake test suite name");

        XmlTest testCase = new XmlTest(testSuite);
        testCase.setName("Fake test context name");
        testCase.setXmlClasses(Arrays.asList(new XmlClass(ca.on.oicr.pde.testing.testng.simulation.MultipleFakeFailsImpl.class)));

        //capture std out/err
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream out = System.out;
        PrintStream err = System.err;
        System.setOut(ps);
        System.setErr(ps);

        //Execute test suite
        t.setXmlSuites(Arrays.asList(testSuite));
        t.run();

        //reset std out/err
        System.out.flush();
        System.err.flush();
        System.setOut(out);
        System.setErr(err);

        String actualOutput = baos.toString();

        ITestContext tc = tla.getTestContexts().get(0);
        Assert.assertEquals(tla.getTestContexts().size(), 1, "The assumption that there is only one test listener no longer holds");

        Assert.assertEquals(tc.getFailedTests().size(), 1);
        Assert.assertEquals(tc.getSkippedTests().size(), 1);
        Assert.assertEquals(tc.getPassedTests().size(), 2);

        Assert.assertEquals(t.getStatus(), 3, "Test status should be \"has failed\" (1) and \"has skipped\" (2).");

        String expectedOutput = "[TestNG] Running:\n"
                + "  Command line suite\n"
                + "\n"
                + "\n"
                + "===============================================\n"
                + "Fake test suite name\n"
                + "Total tests run: 4, Failures: 1, Skips: 1\n"
                + "===============================================\n"
                + "\n"
                + "================================================================================\n"
                + "Test suite = [Fake test suite name]\n"
                + "  Test context = [Fake test context name]\n"
                + "    Test case = [Test name]\n"
                + "      group = [level1], method = [successlTestLevel1_1], execution time = [0.00s], status = [SUCCESS]\n"
                + "      group = [level2], method = [successTestLevel2_1], execution time = [0.00s], status = [SUCCESS]\n"
                + "      group = [level3], method = [failureTestLevel3_1], execution time = [0.00s], status = [FAILURE]\n"
                + "        There was a fake failure.\n"
                + "        Extra information regarding the failure. expected [true] but found [false]\n"
                + "      group = [level4], method = [successTestLevel4_1], execution time = [0.00s], status = [SKIP]\n"
                + "================================================================================";

        Assert.assertEquals(actualOutput.trim(), expectedOutput.trim(), "The test report is different from what was expected.");
    }

}
