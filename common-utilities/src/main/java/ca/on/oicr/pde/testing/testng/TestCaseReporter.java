package ca.on.oicr.pde.testing.testng;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

/**
 * A TestNG Reporter that groups test results by test case name (ie, ITest getName()).
 * 
 * Test results are grouped in this order: test suite --> test context --> test case
 * Within the test case results, the tests are order by their start time.
 * 
 * Add this to your TestNG test suite either by add the annotation: @Listeners({ca.on.oicr.pde.testing.testng.TestCaseReporter.class})
 * For more information, check out http://testng.org/doc/documentation-main.html#testng-listeners
 * 
 * Example report:
 * 
 * Test suite = [Default Suite]
 *   Test context = [Command line test 5614e690-20f6-4d42-8ba0-6ad6d97164e8]
 *      Test case = [Fake test case 0]
 *         group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]
 *         group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]
 *      Test case = [Fake test case 1]
 *         group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]
 *         group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]
 */
public class TestCaseReporter implements IReporter {

    public enum TestNGStatuses {

        SUCCESS(ITestResult.SUCCESS),
        FAILURE(ITestResult.FAILURE),
        SKIP(ITestResult.SKIP),
        SUCCESS_PERCENTAGE_FAILURE(ITestResult.SUCCESS_PERCENTAGE_FAILURE),
        STARTED(ITestResult.STARTED);

        private final int statusCode;

        private TestNGStatuses(int statusCode) {
            this.statusCode = statusCode;
        }

        public static TestNGStatuses valueOf(int code) {
            for (TestNGStatuses s : TestNGStatuses.values()) {
                if (s.statusCode == code) {
                    return s;
                }
            }
            throw new IllegalArgumentException("The status code = [" + code + "] is not a valid TestNGStatus");
        }
        
        public int getValue(){
            return statusCode;
        }
    }

    private Map<String, Collection<ITestResult>> groupResults(IResultMap... results) {
        Multimap<String, ITestResult> groupedResults = LinkedListMultimap.create();
        for (IResultMap rm : results) {
            for (ITestResult tr : rm.getAllResults()) {
                groupedResults.put(tr.getTestName(), tr);
            }
        }
        return new TreeMap(groupedResults.asMap());
    }

    String padding = "   ";
    private void print(int level, String out, Object... args) {
        String prefix = StringUtils.repeat(padding, level);
        List<Object> argsList = new LinkedList<Object>(Arrays.asList(args));
        argsList.add(0, prefix);
        System.out.format("%s" + out + "%n", argsList.toArray());
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

        String separator = StringUtils.repeat("=", 80);
        System.out.println(separator);
        for (ISuite suite : suites) {
            print(0, "Test suite = [%s]", suite.getName());
            for (ISuiteResult sr : suite.getResults().values()) {
                ITestContext tc = sr.getTestContext();
                print(1, "Test context = [%s]", tc.getName());

                for (Entry<String, Collection<ITestResult>> e : groupResults(tc.getPassedTests(), tc.getFailedTests(), tc.getSkippedTests()).entrySet()) {
                    print(2, "Test case = [%s]", e.getKey());
                    List<ITestResult> rs = new ArrayList<ITestResult>(e.getValue());
                    Collections.sort(rs, new Comparator<ITestResult>() {
                        @Override
                        public int compare(ITestResult o1, ITestResult o2) {
                            return Long.valueOf(o1.getStartMillis()).compareTo(o2.getStartMillis());
                        }
                    });
                    for (ITestResult tr : rs) {
                        print(3, "group = %s, method = [%s], execution time = [%.2fs], status = [%s]",
                                Arrays.toString(tr.getMethod().getGroups()), tr.getMethod().getMethodName(),
                                (tr.getEndMillis() - tr.getStartMillis()) / 1000D, TestNGStatuses.valueOf(tr.getStatus()));

                        if (!tr.isSuccess()) {
                            print(4, tr.getThrowable().getMessage());
                        }
                    }
                }

//                System.out.println("Passed tests for suite '" + suite.getName()
//                        + "' is:" + tc.getPassedTests().getAllResults().size());
//                System.out.println("Failed tests for suite '" + suite.getName()
//                        + "' is:"
//                        + tc.getFailedTests().getAllResults().size());
//                System.out.println("Skipped tests for suite '" + suite.getName()
//                        + "' is:"
//                        + tc.getSkippedTests().getAllResults().size());
            }
            System.out.println(separator);
        }
    }

}
