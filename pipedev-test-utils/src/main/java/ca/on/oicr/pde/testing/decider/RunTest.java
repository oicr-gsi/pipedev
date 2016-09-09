package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.gsi.provenance.ProvenanceClient;
import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.pde.reports.WorkflowReport;
import ca.on.oicr.pde.diff.ObjectDiff;
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
import net.sourceforge.seqware.common.model.Workflow;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.annotations.Test;
import ca.on.oicr.pde.client.SeqwareClient;
import ca.on.oicr.pde.dao.reader.FileProvenanceClient;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import ca.on.oicr.pde.reports.WorkflowRunReport;
import com.google.common.collect.Lists;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import static org.testng.Assert.fail;

@Listeners({ca.on.oicr.pde.testing.testng.TestCaseReporter.class})
public class RunTest extends RunTestBase {

    private final Logger log = LogManager.getLogger(RunTest.class);
    private final static List<File> reports = Collections.synchronizedList(new ArrayList<File>());

    private final File deciderJar;
    private final String deciderClass;

    private Workflow workflow;

    private final List<String> studies = new ArrayList<>();
    private final List<String> sequencerRuns = new ArrayList<>();
    private final List<String> samples = new ArrayList<>();

    private File actualReportFile;
    private File expectedReportFile;

    private WorkflowReport actual;
    private WorkflowReport expected;

    private RunTestDefinition testDefinition;

    private Timer executionTimer;

    private SeqwareClient seqwareClient;
    private ProvenanceClient provenanceClient;
    private Path provenanceSettings;

    private static final Comparator<WorkflowRunReport> WORKFLOW_RUN_REPORT_COMPARATOR = new Comparator<WorkflowRunReport>() {
        @Override
        public int compare(WorkflowRunReport o1, WorkflowRunReport o2) {
            SortedSet<String> sampleNames1 = new TreeSet();
            SortedSet<String> laneNames1 = new TreeSet();
            SortedSet<Integer> fileIds1 = new TreeSet();
            for (ReducedFileProvenanceReportRecord r : o1.getFiles()) {

                for (String sampleName : r.getSampleName()) {
                    sampleNames1.add(sampleName);
                }

                for (String laneName : r.getLaneName()) {
                    laneNames1.add(laneName);
                }

                fileIds1.add(r.getFileId());
            }
            String o1key = Joiner.on("").join(sampleNames1) + Joiner.on("").join(laneNames1) + Joiner.on("").join(fileIds1);

            SortedSet<String> sampleNames2 = new TreeSet();
            SortedSet<String> laneNames2 = new TreeSet();
            SortedSet<Integer> fileIds2 = new TreeSet();
            for (ReducedFileProvenanceReportRecord r : o2.getFiles()) {

                for (String sampleName : r.getSampleName()) {
                    sampleNames2.add(sampleName);
                }

                for (String laneName : r.getLaneName()) {
                    laneNames2.add(laneName);
                }

                fileIds2.add(r.getFileId());
            }
            String o2key = Joiner.on("").join(sampleNames2) + Joiner.on("").join(laneNames2) + Joiner.on("").join(fileIds2);

            return o1key.compareTo(o2key);
        }
    };

    public RunTest(SeqwareClient seqwareClient, ProvenanceClient provenanceClient, File seqwareDistribution, File seqwareSettings, File workingDirectory, String testName,
            File deciderJar, Workflow workflow, String deciderClass, RunTestDefinition definition) throws IOException {

        super(seqwareDistribution, seqwareSettings, workingDirectory, testName);
        this.seqwareClient = seqwareClient;
        this.provenanceClient = provenanceClient;
        this.deciderJar = deciderJar;
        this.deciderClass = deciderClass;
        this.testDefinition = definition;
        this.workflow = workflow;

        studies.addAll(testDefinition.getStudies());
        samples.addAll(testDefinition.getSamples());
        sequencerRuns.addAll(testDefinition.getSequencerRuns());

        expectedReportFile = testDefinition.getMetrics();
        if (expectedReportFile == null) {
            log.printf(Level.WARN, "[%s] Missing an expected output metrics file. Skipping comparison step", testName);
        }

    }

    public void setProvenanceSettings(Path provenanceSettings) {
        this.provenanceSettings = provenanceSettings;
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
        if (!reports.isEmpty()) {
            log.warn("Report file paths: " + reports.toString());
            log.warn("cp " + Joiner.on(" ").join(reports) + " " + "/tmp");
        }
    }

    @BeforeGroups(groups = "preExecution")
    public static void loadPreExecutionSharedData() {
    }

    @Test(groups = "preExecution")
    public void initializeEnvironment() throws IOException {

    }

    @Test(groups = "preExecution")
    public void installWorkflow() throws IOException {
        Timer timer = Timer.start();

        if (workflow.getCwd() != null) {
            workflow.setSwAccession(seqwareExecutor.installWorkflow(new File(workflow.getCwd())).getSwAccession());
        } else if (workflow.getName() != null && workflow.getVersion() != null) {
            Map<String, String> defaultWorkflowProperties = new HashMap<>();
            defaultWorkflowProperties.put("output_prefix", "./");

            workflow.setSwAccession(seqwareClient.createWorkflow(workflow.getName(), workflow.getVersion(), "", defaultWorkflowProperties).getSwAccession());
        } else {
            fail("Unable to install workflow - bundle or workflow name + version was not specified");
        }

        //replace partial workflow object with the seqware workflow object
        Assert.assertNotNull(workflow.getSwAccession(), "Installation of the workflow failed");
        workflow = metadata.getWorkflow(workflow.getSwAccession());

        log.printf(Level.INFO, "[%s] Completed installing workflow in %s", testName, timer.stop());
    }

    @Test(dependsOnGroups = "preExecution", groups = "execution")
    public void executeDecider() throws IOException, InstantiationException, ClassNotFoundException, IllegalAccessException {
        Timer timer = Timer.start();
        StringBuilder extraArgs = new StringBuilder();
        for (Entry<String, List<String>> e : testDefinition.getParameters().entrySet()) {
            String parameter = e.getKey();
            List<String> arguments = e.getValue();
            if (arguments == null) {
                //continue
            } else if (arguments.isEmpty()) {
                extraArgs.append(" ").append(parameter);
            } else {
                for (String argument : arguments) {
                    extraArgs.append(" ").append(parameter).append(" ").append(argument);
                }
            }
        }

        //run all decider run tests in verbose mode
        extraArgs.append(" ").append("--verbose");

        //configure decider to use provenance settings file
        extraArgs.append(" ").append("--provenance-settings").append(" ").append(provenanceSettings.toAbsolutePath().toString());

        if (!testDefinition.getProperties().isEmpty()) {
            extraArgs.append(" --");
            for (Entry<String, List<String>> e : testDefinition.getProperties().entrySet()) {
                extraArgs.append(" --").append(e.getKey()).append(" ");
                extraArgs.append(Joiner.on(",").join(e.getValue()));
            }
        }

        seqwareExecutor.deciderRunSchedule(deciderJar, workflow, studies, sequencerRuns, samples, extraArgs.toString());
        log.printf(Level.INFO, "[%s] Completed workflow run scheduling in %s", testName, timer.stop());
    }

    private static FileProvenanceClient postExecutionFpc;

    @AfterGroups(groups = "execution")
    public void loadPostExecutionSharedData() {
        Collection<FileProvenance> fps = provenanceClient.getFileProvenance();
        postExecutionFpc = new FileProvenanceClient(Lists.newArrayList(fps));
    }

    @Test(dependsOnGroups = "execution", groups = "postExecution")
    public void calculateWorkflowRunReport() throws JsonProcessingException, IOException {
        Timer timer = Timer.start();

        List<WorkflowRunReportRecord> wrrs = seqwareClient.getWorkflowRunRecords(workflow);
        actual = WorkflowReport.generateReport(seqwareClient, postExecutionFpc, wrrs);

        File actualUnmodifiedReportFile = new File(workingDirectory.getAbsolutePath() + "/tmp/" + testDefinition.outputName());
        if (actualUnmodifiedReportFile.exists()) {
            throw new RuntimeException("File already exists.");
        } else {
            FileUtils.write(actualUnmodifiedReportFile, actual.toJson());
        }

        Map<String, String> iniStringSubstitutions = new HashMap<>();
        if (workflow.getCwd() != null) {
            iniStringSubstitutions.put(workflow.getCwd(), "\\$\\{workflow_bundle_dir\\}");
        }
        iniStringSubstitutions.putAll(testDefinition.getIniStringSubstitutions());

        actual.applyIniExclusions(testDefinition.getIniExclusions());
        actual.applyIniStringSubstitutions(iniStringSubstitutions);
        actual.applyIniSubstitutions(testDefinition.getIniSubstitutions());

        List<WorkflowRunReport> actualWorkflowRunReports = actual.getWorkflowRuns();
        Collections.sort(actualWorkflowRunReports, WORKFLOW_RUN_REPORT_COMPARATOR);

        actualReportFile = new File(workingDirectory.getAbsolutePath() + "/" + testDefinition.outputName());
        if (actualReportFile.exists()) {
            throw new RuntimeException("File already exists.");
        } else {
            FileUtils.write(actualReportFile, actual.toJson());
        }

        reports.add(actualReportFile);

        if (expectedReportFile != null) {
            try {
                expected = WorkflowReport.buildFromJson(expectedReportFile);
                expected.applyIniExclusions(testDefinition.getIniExclusions());

                expected.applyIniStringSubstitution("\\$\\{workflow_bundle_dir\\}/Workflow_Bundle_[^/]+/[^/]+/",
                        "\\$\\{workflow_bundle_dir\\}/Workflow_Bundle_" + workflow.getName() + "/" + workflow.getVersion() + "/");
                expected.applyIniStringSubstitutions(testDefinition.getIniStringSubstitutions());
                expected.applyIniSubstitutions(testDefinition.getIniSubstitutions());

                List<WorkflowRunReport> expectedWorkflowRunReports = expected.getWorkflowRuns();
                Collections.sort(expectedWorkflowRunReports, WORKFLOW_RUN_REPORT_COMPARATOR);

                expectedReportFile = new File(workingDirectory.getAbsolutePath() + "/" + "expected_" + testDefinition.outputName());
                if (expectedReportFile.exists()) {
                    throw new RuntimeException("File already exists.");
                } else {
                    FileUtils.write(expectedReportFile, expected.toJson());
                }

            } catch (IOException ioe) {
                log.printf(Level.WARN, "[%s] There was a problem loading expected output:\n" + ioe.getMessage());
                expected = null;
            }

        }
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
            String headerSummary = WorkflowReport.diffHeader(actual, expected);
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
