package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.dao.reader.SeqwareReadService;
import ca.on.oicr.pde.diff.ObjectDiff;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.testing.common.RunTestBase;
import ca.on.oicr.pde.utilities.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.annotations.Test;

@Listeners({ca.on.oicr.pde.testing.testng.TestCaseReporter.class})
public class DeciderRunTest extends RunTestBase {

    private final Logger log = LogManager.getLogger(DeciderRunTest.class);
    private final static List<File> reports = Collections.synchronizedList(new ArrayList<File>());

    private final File deciderJar;
    private final String deciderClass;
    private final File bundledWorkflow;

    private SeqwareReadService seqwareService;
    private Workflow workflow;

    private final List<String> studies = new ArrayList<>();
    private final List<String> sequencerRuns = new ArrayList<>();
    private final List<String> samples = new ArrayList<>();

    File actualReportFile;
    File expectedReportFile;

    DeciderRunTestReport actual;
    DeciderRunTestReport expected;

    DeciderRunTestDefinition.Test testDefinition;

    private Timer executionTimer;

    public DeciderRunTest(SeqwareReadService seqwareService, File seqwareDistribution, File seqwareSettings, File workingDirectory, String testName,
            File deciderJar, File bundledWorkflow, String deciderClass, DeciderRunTestDefinition.Test definition) throws IOException {

        super(seqwareDistribution, seqwareSettings, workingDirectory, testName);

        this.seqwareService = seqwareService;
        this.deciderJar = deciderJar;
        this.bundledWorkflow = bundledWorkflow;
        this.deciderClass = deciderClass;
        this.testDefinition = definition;

        studies.addAll(testDefinition.getStudies());
        samples.addAll(testDefinition.getSamples());
        sequencerRuns.addAll(testDefinition.getSequencerRuns());

        expectedReportFile = testDefinition.getMetrics();

        if (expectedReportFile != null) {
            try {
                expected = DeciderRunTestReport.buildFromJson(expectedReportFile);
            } catch (IOException ioe) {
                log.printf(Level.WARN, "[%s] There was a problem loading the metrics file: [%s].\n"
                        + "The exception output:\n%s\nContinuing with test but comparision step will fail.",
                        testName, expectedReportFile.getAbsolutePath(), ioe.toString());
                expected = null;
            }
        } else {
            log.printf(Level.WARN, "[%s] Missing an expected output metrics file. Skipping comparison step", testName);
        }

    }

    @BeforeSuite
    public void beforeAllRunTests() {
        //
    }

    @BeforeClass
    public void beforeEachRunTest() throws IOException {

        log.printf(Level.INFO, "[%s] Starting run test", testName);
        executionTimer = Timer.start();
        Assert.assertNotNull(seqwareDistribution,
                "Seqware distribution path is not set - set seqwareDistribution in pom.xml.");
        Assert.assertNotNull(workingDirectory,
                "Working directory path is not set - set workingDirectory in pom.xml.");
        Assert.assertTrue(FileUtils.getFile(seqwareDistribution).exists(),
                "Seqware distribution does not exist - verify seqwareDistribution is correct in pom.xml.");
        Assert.assertTrue(seqwareSettings.exists(),
                "Generate seqware settings failed - please verify seqwareDirectory is accessible");

    }

    @BeforeMethod
    public void beforeEachTestMethod() throws IOException {
        //
    }

    @AfterMethod
    public void afterEachTestMethod() throws IOException {
        //
    }

    @AfterClass
    public void afterEachRunTest() throws IOException {

        /* Cancel all submitted workflow runs
         * Each decider test run installs a separate instance of its associated
         * workflow bundle. So each decider run test has a unique workflow swid.
         */
        Timer timer = Timer.start();
        seqwareExecutor.cancelWorkflowRuns(workflow);
        log.printf(Level.INFO, "[%s] Completed clean up in %s", testName, timer.stop());

        //Test case summary info
        log.printf(Level.INFO, "[%s] Total run time: %s", testName, executionTimer.stop());
        log.printf(Level.INFO, "[%s] Working directory: %s", testName, workingDirectory);

    }

    @AfterSuite
    public void afterAllRunTests() {

        log.warn("Report file paths: " + reports.toString());
        log.warn("cp " + Joiner.on(" ").join(reports) + " " + "/tmp");

    }

    @Test(groups = "preExecution")
    public void initializeEnvironment() throws IOException {

    }

    @Test(groups = "preExecution")
    public void installWorkflow() throws IOException {
        Timer timer = Timer.start();
        Workflow.Builder b = new Workflow.Builder();
        b.setSwid(seqwareExecutor.installWorkflow(bundledWorkflow).getSwid());
        workflow = b.build();

        Assert.assertNotNull(workflow.getSwid(), "Installation of the workflow bundle failed");
        log.printf(Level.INFO, "[%s] Completed installing workflow bundle in %s", testName, timer.stop());
    }

//    @Test(groups = "preExecution", expectedExceptions = Exception.class)
//    public void getDeciderObject() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
//        //get decider object + all parameters
//        System.out.println(deciderClass);
//        Class c = Class.forName(deciderClass);
//        BasicDecider b = (BasicDecider) c.newInstance();
//        System.out.println("Metatype: " + b.getMetaType());
//        System.out.println("Syntax" + b.get_syntax());
//
//        b.setParams(Arrays.asList("--study", "AshPC"));
//        b.parse_parameters();
//        ReturnValue rv = b.init();
//
//        System.out.println("rv: " + rv.getParameters());
//
//        System.out.println("Metatype: " + b.getMetaType());
//        System.out.println("Syntax" + b.get_syntax());
//
//    }
    @Test(dependsOnGroups = "preExecution", groups = "execution")
    public void executeDecider() throws IOException, InstantiationException, ClassNotFoundException, IllegalAccessException {
        Timer timer = Timer.start();
        StringBuilder extraArgs = new StringBuilder();
        for (Entry<String, List<String>> e : testDefinition.getParameters().entrySet()) {
            String parameter = e.getKey();
            List<String> arguments = e.getValue();
            if (arguments.isEmpty()) {
                extraArgs.append(" ").append(parameter);
            } else {
                for (String argument : arguments) {
                    extraArgs.append(" ").append(parameter).append(" ").append(argument);
                }
            }
        }
        seqwareExecutor.deciderRunSchedule(deciderJar, workflow, studies, sequencerRuns, samples, extraArgs.toString());
        log.printf(Level.INFO, "[%s] Completed workflow run scheduling in %s", testName, timer.stop());
    }

    @Test(dependsOnGroups = "execution", groups = "postExecution")
    public void calculateWorkflowRunReport() throws JsonProcessingException, IOException {
        Timer timer = Timer.start();

        Map<String, String> iniSubstitutions = new HashMap<>();
        iniSubstitutions.put(bundledWorkflow.getAbsolutePath(), "${workflow_bundle_dir}");

        actual = DeciderRunTestReport.generateReport(seqwareService, workflow, testDefinition.getIniExclusions(), iniSubstitutions);

        actualReportFile = new File(workingDirectory.getAbsolutePath() + "/" + testDefinition.outputName());
        Assert.assertFalse(actualReportFile.exists());

        FileUtils.write(actualReportFile, actual.toJson());

        reports.add(actualReportFile);

        log.printf(Level.INFO, "[%s] Completed generating workflow run report in %s", testName, timer.stop());
    }

    @Test(dependsOnGroups = "execution", dependsOnMethods = "calculateWorkflowRunReport", groups = "postExecution")
    public void compareWorkflowRunReport() throws JsonProcessingException, IOException {
        Timer timer = Timer.start();

        Assert.assertNotNull(expected, "There is no expected output to compare to");

        if (!actual.equals(expected)) {
            StringBuilder sb = new StringBuilder();
            sb.append("There are differences between decider runs:\n");
            sb.append("Expected run report: ").append(expectedReportFile.getAbsolutePath()).append("\n");
            sb.append("Actual run report: ").append(actualReportFile.getAbsolutePath()).append("\n");

            //Build the summary report
            String headerSummary = DeciderRunTestReport.diffHeader(actual, expected);
            if (!headerSummary.isEmpty()) {
                sb.append("Change summary:\n");
                sb.append(headerSummary);
            } else {
                sb.append(ObjectDiff.diffReportSummary(actual, expected, 3));
            }

            //Don't print a testng message, only print our string
            Assert.fail(sb.toString());
        }

        log.printf(Level.INFO, "[%s] Completed comparing workflow run reports in %s", testName, timer.stop());
    }

}
