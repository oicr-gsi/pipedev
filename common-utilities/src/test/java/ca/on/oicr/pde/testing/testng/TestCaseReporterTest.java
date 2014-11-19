package ca.on.oicr.pde.testing.testng;

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
        testSuite.setName("Fake test suite name");
        testSuite.setThreadCount(numberOfTestCases * numberOfTestMethods);
        //testSuite.setParallel(XmlSuite.PARALLEL_CLASSES); // cases * methods = seconds
        //testSuite.setParallel(XmlSuite.PARALLEL_INSTANCES); // methods = seconds
        //testSuite.setParallel(XmlSuite.PARALLEL_METHODS); // methods = seconds ????
        //testSuite.setParallel(XmlSuite.PARALLEL_TESTS); // cases * methods = seconds
        //testSuite.setParallel(XmlSuite.PARALLEL_NONE); // cases * methods = seconds
        testSuite.setParallel(XmlSuite.PARALLEL_METHODS); //run time should be about ~2s (2 methods * 1s)

        XmlTest testCase = new XmlTest(testSuite);
        testCase.setName("Fake test context name");
        Map<String, String> params = new HashMap<String, String>();
        params.put("numberOfTests", Integer.toString(numberOfTestCases));
        params.put("numberOfTestMethods", Integer.toString(numberOfTestMethods));
        testCase.setParameters(params);
        testCase.setXmlClasses(Arrays.asList(new XmlClass(ca.on.oicr.pde.testing.testng.simulation.FakeImpl.class)));

        String actualReportOutput = executeTestSuite(testSuite);

        ITestContext tc = tla.getTestContexts().get(0);
        Assert.assertEquals(tla.getTestContexts().size(), 1, "The assumption that there is only one test listener no longer holds");

        Assert.assertEquals(tc.getFailedTests().size(), 0);
        Assert.assertEquals(tc.getSkippedTests().size(), 0);
        Assert.assertEquals(tc.getPassedTests().size(), numberOfTestCases * numberOfTestMethods);

        Assert.assertEquals(t.getStatus(), 0);

        String expectedReportOutput = "[TestNG] Running:\n"
                + "  Command line suite\n"
                + "\n"
                + "\n"
                + "===============================================\n"
                + "Fake test suite name\n"
                + "Total tests run: 20, Failures: 0, Skips: 0\n"
                + "===============================================\n"
                + "\n"
                + "================================================================================\n"
                + "Test suite = [Fake test suite name]\n"
                + "  Test context = [Fake test context name]\n"
                + "    Test case = [Fake test case 0]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "    Test case = [Fake test case 1]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "    Test case = [Fake test case 2]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "    Test case = [Fake test case 3]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "    Test case = [Fake test case 4]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "    Test case = [Fake test case 5]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "    Test case = [Fake test case 6]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "    Test case = [Fake test case 7]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "    Test case = [Fake test case 8]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "    Test case = [Fake test case 9]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "      group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]\n"
                + "================================================================================\n"
                + "";

        Assert.assertEquals(normalizeOutput(actualReportOutput), normalizeOutput(expectedReportOutput), "The test report is different from what was expected.");
    }

    @Test
    public void testCaseReporterTestOnTestFailures() {

        XmlSuite testSuite = new XmlSuite();
        testSuite.setName("Fake test suite name");

        XmlTest testCase = new XmlTest(testSuite);
        testCase.setName("Fake test context name");
        testCase.setXmlClasses(Arrays.asList(new XmlClass(ca.on.oicr.pde.testing.testng.simulation.FakeFailImpl.class)));

        String actualReportOutput = executeTestSuite(testSuite);

        ITestContext tc = tla.getTestContexts().get(0);
        Assert.assertEquals(tla.getTestContexts().size(), 1, "The assumption that there is only one test listener no longer holds");

        Assert.assertEquals(tc.getFailedTests().size(), 1);
        Assert.assertEquals(tc.getSkippedTests().size(), 0);
        Assert.assertEquals(tc.getPassedTests().size(), 0);

        Assert.assertEquals(t.getStatus(), 1);

        String expectedReportOutput = "[TestNG] Running:\n"
                + "  Command line suite\n"
                + "\n"
                + "\n"
                + "===============================================\n"
                + "Fake test suite name\n"
                + "Total tests run: 1, Failures: 1, Skips: 0\n"
                + "===============================================\n"
                + "\n"
                + "================================================================================\n"
                + "Test suite = [Fake test suite name]\n"
                + "  Test context = [Fake test context name]\n"
                + "    Test case = [Test name]\n"
                + "      group = [fake], method = [fakeFailure], execution time = [0.00s], status = [FAILURE]\n"
                + "        There was a fake failure.\n"
                + "        Extra information regarding the failure. expected [true] but found [false]\n"
                + "================================================================================\n"
                + "";

        Assert.assertEquals(normalizeOutput(actualReportOutput), normalizeOutput(expectedReportOutput), "The test report is different from what was expected.");
    }

    @Test
    public void testCaseReporterGetNameNPE_PDE813() {

        XmlSuite testSuite = new XmlSuite();
        testSuite.setName("Fake test suite name");

        Map<String, String> params = new HashMap<String, String>();
        params.put("numberOfTests", Integer.toString(2));

        XmlTest testCase = new XmlTest(testSuite);
        testCase.setName("Fake test context name");
        testCase.setXmlClasses(Arrays.asList(new XmlClass(ca.on.oicr.pde.testing.testng.simulation.MultipleFakeFailsImpl.class)));
        testCase.setParameters(params);

        String actualReportOutput = executeTestSuite(testSuite);

        ITestContext tc = tla.getTestContexts().get(0);
        Assert.assertEquals(tla.getTestContexts().size(), 1, "The assumption that there is only one test listener no longer holds");

        Assert.assertEquals(tc.getFailedTests().size(), 2);
        Assert.assertEquals(tc.getSkippedTests().size(), 2);
        Assert.assertEquals(tc.getPassedTests().size(), 4);

        Assert.assertEquals(t.getStatus(), 3, "Test status should be \"has failed\" (1) and \"has skipped\" (2).");

        String expectedReportOutput = "[TestNG] Running:\n"
                + "  Command line suite\n"
                + "\n"
                + "\n"
                + "===============================================\n"
                + "Fake test suite name\n"
                + "Total tests run: 8, Failures: 2, Skips: 2\n"
                + "===============================================\n"
                + "\n"
                + "================================================================================\n"
                + "Test suite = [Fake test suite name]\n"
                + "  Test context = [Fake test context name]\n"
                + "    Test case = [Test name 0]\n"
                + "      group = [level1], method = [successlTestLevel1_1], execution time = [0.00s], status = [SUCCESS]\n"
                + "      group = [level2], method = [successTestLevel2_1], execution time = [0.00s], status = [SUCCESS]\n"
                + "      group = [level3], method = [failureTestLevel3_1], execution time = [0.00s], status = [FAILURE]\n"
                + "        There was a fake failure.\n"
                + "        Extra information regarding the failure. expected [true] but found [false]\n"
                + "      group = [level4], method = [successTestLevel4_1], execution time = [0.00s], status = [SKIP]\n"
                + "    Test case = [Test name 1]\n"
                + "      group = [level1], method = [successlTestLevel1_1], execution time = [0.00s], status = [SUCCESS]\n"
                + "      group = [level2], method = [successTestLevel2_1], execution time = [0.00s], status = [SUCCESS]\n"
                + "      group = [level3], method = [failureTestLevel3_1], execution time = [0.00s], status = [FAILURE]\n"
                + "        There was a fake failure.\n"
                + "        Extra information regarding the failure. expected [true] but found [false]\n"
                + "      group = [level4], method = [successTestLevel4_1], execution time = [0.00s], status = [SKIP]\n"
                + "================================================================================\n"
                + "";

        Assert.assertEquals(normalizeOutput(actualReportOutput), normalizeOutput(expectedReportOutput), "The test report is different from what was expected.");
    }

    private String normalizeOutput(final String output) {
        String newOutput = output.replaceAll("(.*execution time = \\[)([^\\]]*)(\\].*)", "$1X.XXs$3");
        return newOutput;
    }

    private String executeTestSuite(XmlSuite testSuite) {
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

        return baos.toString();
    }

}
