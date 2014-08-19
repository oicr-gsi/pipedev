package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.dao.SeqwareService;
import ca.on.oicr.pde.model.Accessionable;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.model.Sample;
import ca.on.oicr.pde.model.SequencerRun;
import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.model.Study;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import ca.on.oicr.pde.testing.common.RunTestBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

public class DeciderRunTest extends RunTestBase implements org.testng.ITest {

    private final static Logger log = LogManager.getLogger(DeciderRunTest.class);
    private final static List<File> reports = Collections.synchronizedList(new ArrayList<File>());

    private final File deciderJar;
    private final String deciderClass;
    private final File bundledWorkflow;

    SeqwareService seqwareService;
    SeqwareAccession workflowSwid;

    private final List<Study> studies = new ArrayList<Study>();
    private final List<SequencerRun> sequencerRuns = new ArrayList<SequencerRun>();
    private final List<Sample> samples = new ArrayList<Sample>();

    File actualReportFile;
    File expectedReportFile;

    TestResult actual;
    TestResult expected;

    TestDefinition.Test testDefinition;

    public DeciderRunTest(SeqwareService seqwareService, File seqwareDistribution, File seqwareSettings, File workingDirectory, String testName,
            File deciderJar, File bundledWorkflow, String deciderClass, TestDefinition.Test definition) throws IOException {

        super(seqwareDistribution, seqwareSettings, workingDirectory, testName);

        this.seqwareService = seqwareService;
        this.deciderJar = deciderJar;
        this.bundledWorkflow = bundledWorkflow;
        this.deciderClass = deciderClass;
        this.testDefinition = definition;

        for (String s : testDefinition.getStudies()) {
            Study x = new Study();
            x.setTitle(s);
            studies.add(x);
        }

        for (String s : testDefinition.getSamples()) {
            Sample x = new Sample();
            x.setName(s);
            samples.add(x);
        }

        for (String s : testDefinition.getSequencerRuns()) {
            SequencerRun x = new SequencerRun();
            x.setName(s);
            sequencerRuns.add(x);
        }

        expectedReportFile = testDefinition.metrics();

        if (expectedReportFile != null) {
            log.warn("Found a metrics file: [" + expectedReportFile.getAbsolutePath() + "].");
            try {
                expected = TestResult.buildFromJson(expectedReportFile);
            } catch (IOException ioe) {
                log.error("There was a problem loading the metrics file: [" + expectedReportFile.getAbsolutePath() + "]."
                        + "\nThe exception output:\n" + ioe.toString() + "\nContinuing with test but comparision step will fail.");
                expected = null;
            }
        } else {
            log.error("Metrics does not exist, skipping comparison step");
        }

    }

    @BeforeSuite
    public void beforeAllRunTests() {
        //
    }

    @BeforeClass
    public void beforeEachRunTest() throws IOException {

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
         * TODO: Simplify this when the pde-seqware API is complete
         */
        long startTime = System.nanoTime();
        log.printf(Level.INFO, "Starting clean up of %s", testName);
        Workflow w = new Workflow();
        w.setSwid(workflowSwid.toString());
        seqwareExecutor.cancelWorkflowRuns(w);

        log.printf(Level.INFO, "Completed clean up for [%s] in %.2fs", testName, (System.nanoTime() - startTime) / 1E9);
        
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

        long startTime = System.nanoTime();

        workflowSwid = seqwareExecutor.installWorkflow(bundledWorkflow);

        Assert.assertNotNull(workflowSwid);

        log.printf(Level.INFO, "Completed installing workflow bundle for [%s] in %.2fs", testName, (System.nanoTime() - startTime) / 1E9);

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

        long startTime = System.nanoTime();
        log.printf(Level.INFO, "Starting execution of %s test", testName);

        StringBuilder extraArgs = new StringBuilder();
        for (Entry<String, String> e : testDefinition.getParameters().entrySet()) {
            extraArgs.append(" ").append(e.getKey()).append(" ").append(e.getValue());
        }

        seqwareExecutor.deciderRunSchedule(deciderJar, workflowSwid, studies, sequencerRuns, samples, extraArgs.toString());

        log.printf(Level.INFO, "Completed workflow run scheduling for [%s] in %.2fs", testName, (System.nanoTime() - startTime) / 1E9);

    }

    @Test(dependsOnGroups = "execution", groups = "postExecution")
    public void calculateWorkflowRunReport() throws JsonProcessingException, IOException {

        long startTime = System.nanoTime();

        Workflow w = new Workflow();
        w.setSwid(workflowSwid.getSwid());

        actual = getWorkflowReport(w);

        actualReportFile = new File(workingDirectory.getAbsolutePath() + "/" + testDefinition.outputName());
        Assert.assertFalse(actualReportFile.exists());

        FileUtils.write(actualReportFile, testResultToJson(actual));

        reports.add(actualReportFile);

        log.printf(Level.INFO, "Completed generating workflow run report for [%s] in %.2fs", testName, (System.nanoTime() - startTime) / 1E9);

    }

    @Test(dependsOnGroups = "execution", dependsOnMethods = "calculateWorkflowRunReport", groups = "postExecution")
    public void compareWorkflowRunReport() throws JsonProcessingException, IOException {

        long startTime = System.nanoTime();

        Assert.assertNotNull(expected, "no expected output to compare to.");

        List<String> problems = validateReport(actual);
        Assert.assertTrue(problems.isEmpty(), problems.toString());

        Assert.assertTrue(compareReports(actual, expected),
                "There are differences between reports:\nExpected: " + expectedReportFile + "\nActual: " + actualReportFile);
        //+ "\nExpected object:\n" + expected.toString() + "\nActual object:\n" + actual.toString());

        log.printf(Level.INFO, "Completed comparing workflow run reports for [%s] in %.2fs", testName, (System.nanoTime() - startTime) / 1E9);

    }

    private List<String> validateReport(TestResult t) {

        List<String> problems = new ArrayList<String>();

        if (t.getWorkflowRunCount().equals(Integer.valueOf("0"))) {
            problems.add("No workflow run were scheduled.");
        }

        return problems;

    }

    public static <T> boolean compareReports(T actual, T expected) {

        //Node root = ObjectDifferFactory.getInstance().compare(actual, expected);
        //log.warn("root categories: " + root.getCategories());
        //log.warn(root.getChildren());
        //diff is not working root.visit(new PrintingVisitor(actual, expected));
        //return (!root.hasChanges());
        return actual.equals(expected);

    }

    //TODO: move this to a separate implementation class of "Decider Report"
    private TestResult getWorkflowReport(Workflow w) {

        List<WorkflowRunReportRecord> wrrs = seqwareService.getWorkflowRunRecords(w);

        TestResult t = new TestResult();
        t.setWorkflowRunCount(wrrs.size());

        for (WorkflowRunReportRecord wrr : wrrs) {

            //TODO: get workflow run object from workflow run report record
            WorkflowRun wr = new WorkflowRun();
            wr.setSwid(wrr.getWorkflowRunSwid());

            //Get the workflow run's parent accession(s) (processing accession(s))
            List<Accessionable> parentAccessions = seqwareService.getParentAccessions(wr);

            //Get the workflow run's input file(s) (file accession(s))
            List<Accessionable> inputFileAccessions = seqwareService.getInputFileAccessions(wr);

            //TODO: 0.13.x series deciders do not use input_files, generalize parent and input file accessions
            if (inputFileAccessions.isEmpty()) {
                log.warn("overriding input file accessions with parent accessions, workflow run swid=[" + wr.getSwid() + "]");
                inputFileAccessions = parentAccessions;
            }

            t.addStudies(seqwareService.getStudy(parentAccessions));
            t.addSamples(seqwareService.getSamples(parentAccessions));
            t.addSequencerRuns(seqwareService.getSequencerRuns(parentAccessions));
            t.addLanes(seqwareService.getLanes(parentAccessions));

            t.addWorkflows(seqwareService.getWorkflows(inputFileAccessions));
            t.addProcessingAlgorithms(seqwareService.getProcessingAlgorithms(inputFileAccessions));
            t.addFileMetaTypes(seqwareService.getFileMetaTypes(inputFileAccessions));

            List<ReducedFileProvenanceReportRecord> files = seqwareService.getFiles(inputFileAccessions);
            if (files.size() > t.getMaxInputFiles()) {
                t.setMaxInputFiles(files.size());
            }

            if (files.size() < t.getMinInputFiles()) {
                t.setMinInputFiles(files.size());
            }

            Map ini = seqwareService.getWorkflowRunIni(wr);
            for (String s : testDefinition.getIniExclusions()) {
                ini.remove(s);
            }

            WorkflowRunReport x = new WorkflowRunReport();
            x.setWorkflowIni(ini);
            x.setFiles(files);

            t.addWorkflowRun(x);

        }

        return t;

    }

    public static String testResultToJson(TestResult t) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(t);

    }

}
