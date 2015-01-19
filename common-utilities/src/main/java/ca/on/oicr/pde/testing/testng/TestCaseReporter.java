package ca.on.oicr.pde.testing.testng;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
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
import org.testng.ITest;
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
 * __Test context = [Command line test 5614e690-20f6-4d42-8ba0-6ad6d97164e8]
 * ____Test case = [Fake test case 0]
 * ______group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]
 * ______group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]
 * ____Test case = [Fake test case 1]
 * ______group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]
 * ______group = [], method = [sleepTest], execution time = [1.00s], status = [SUCCESS]
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

        public int getValue() {
            return statusCode;
        }
    }

    private Map<String, Collection<ITestResult>> groupResults(IResultMap... results) {
        Multimap<String, ITestResult> groupedResults = LinkedListMultimap.create();
        for (IResultMap rm : results) {
            for (ITestResult tr : rm.getAllResults()) {
                String testName = null;
                if (tr.getTestName() != null) {
                    testName = tr.getTestName();
                } else if (tr.getMethod().getInstance() != null) {
                    testName = ((ITest) tr.getMethod().getInstance()).getTestName();
                } else {
                    throw new RuntimeException("Test name can not be null");
                }
                groupedResults.put(testName, tr);
            }
        }
        return new TreeMap(groupedResults.asMap());
    }

    public static String formatAndIndent(int level, String out, Object... args) {
        return formatAndIndent("  ", level, out, args);
    }

    public static String formatAndIndent(String padding, int level, String out, Object... args) {
        checkArgument(level >= 0);
        checkNotNull(out);
        String prefix = StringUtils.repeat(padding, level);
        return String.format(prefix + out, args).replaceAll("\n", "\n" + prefix);
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

        String separator = StringUtils.repeat("=", 80);
        System.out.println(separator);

        //Iterate over all test suites (eg, testng.xml)
        for (ISuite suite : suites) {
            System.out.println(formatAndIndent(0, "Test suite = [%s]", suite.getName()));

            //Get the results for the test suite
            for (ISuiteResult sr : suite.getResults().values()) {
                ITestContext tc = sr.getTestContext();
                System.out.println(formatAndIndent(1, "Test context = [%s]", tc.getName()));

                //Iterate over all test case results.  Aggregate failed, skipped, and succeeded tests by test name (@ITest's getName()).
                for (Entry<String, Collection<ITestResult>> e : groupResults(tc.getPassedTests(), tc.getFailedTests(), tc.getSkippedTests()).entrySet()) {
                    System.out.println(formatAndIndent(2, "Test case = [%s]", e.getKey())); //test case name
                    List<ITestResult> rs = new ArrayList<>(e.getValue()); //get all tests for the test case (ie, all tests with the same @ITest getName()
                    Collections.sort(rs, new Comparator<ITestResult>() { //sort the list of tests by their start time
                        @Override
                        public int compare(ITestResult o1, ITestResult o2) {
                            return Long.valueOf(o1.getStartMillis()).compareTo(o2.getStartMillis());
                        }
                    });

                    //Iterate over all test results for the test case
                    for (ITestResult tr : rs) {
                        System.out.println(formatAndIndent(3, "group = %s, method = [%s], execution time = [%.2fs], status = [%s]",
                                Arrays.toString(tr.getMethod().getGroups()), tr.getMethod().getMethodName(),
                                (tr.getEndMillis() - tr.getStartMillis()) / 1000D, TestNGStatuses.valueOf(tr.getStatus())));

                        //If there was an error for the test case, print the output
                        if (!tr.isSuccess() && tr.getStatus() != TestNGStatuses.SKIP.statusCode) {
                            String errorMessage = null;
                            if (tr.getThrowable() != null) {
                                errorMessage = tr.getThrowable().getMessage();
                            }
                            if (errorMessage != null) {
                                System.out.println(formatAndIndent(4, "%s", errorMessage));
                            }
                        }
                    }
                }
            }
            System.out.println(separator);
        }
    }

}
