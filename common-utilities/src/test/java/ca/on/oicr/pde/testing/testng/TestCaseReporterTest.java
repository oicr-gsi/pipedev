package ca.on.oicr.pde.testing.testng;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class TestCaseReporterTest {

    @Test
    public void test(ITestContext context) {

        int numberOfTestCases = 10;
        int numberOfTestMethods = 2;

        //Setup test runner
        TestNG t = new TestNG();
        t.setOutputDirectory(context.getOutputDirectory());
        TestListenerAdapter tla = new TestListenerAdapter();
        t.addListener(tla);

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
        testCase.setXmlClasses(Arrays.asList(new XmlClass(ca.on.oicr.pde.testing.testng.FakeImpl.class)));

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

}
