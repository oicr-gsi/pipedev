package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.dao.SeqwareInterface;
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
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.annotations.Test;

public class DeciderRunTest extends RunTestBase implements org.testng.ITest {

    private final static Logger log = Logger.getLogger(DeciderRunTest.class);
    private final static List<File> reports = Collections.synchronizedList(new ArrayList<File>());

    private final File deciderJar;
    private final String deciderClass;
    private final File bundledWorkflow;

    SeqwareInterface seq;
    SeqwareAccession workflowSwid;

    private final List<Study> studies = new ArrayList<Study>();
    private final List<SequencerRun> sequencerRuns = new ArrayList<SequencerRun>();
    private final List<Sample> samples = new ArrayList<Sample>();

    File actualReportFile;
    File expectedReportFile;

    TestResult actual;
    TestResult expected;

    TestDefinition.Test testDefinition;

    public DeciderRunTest(SeqwareInterface seq, File seqwareDistribution, File seqwareSettings, File workingDirectory, String testName,
            File deciderJar, File bundledWorkflow, String deciderClass, TestDefinition.Test testDefinition) {

        super(seqwareDistribution, seqwareSettings, workingDirectory, testName);

        this.seq = seq;
        this.deciderJar = deciderJar;
        this.bundledWorkflow = bundledWorkflow;
        this.deciderClass = deciderClass;

        for (String s : testDefinition.studies) {
            Study x = new Study();
            x.setTitle(s);
            studies.add(x);
        }

        for (String s : testDefinition.samples) {
            Sample x = new Sample();
            x.setName(s);
            samples.add(x);
        }

        for (String s : testDefinition.sequencerRuns) {
            SequencerRun x = new SequencerRun();
            x.setName(s);
            sequencerRuns.add(x);
        }

        try {
            expectedReportFile = testDefinition.metrics();
            if (expectedReportFile != null) {
                log.warn("found a metrics file: " + expectedReportFile.getAbsolutePath());
                expected = TestResult.buildFromJson(expectedReportFile);
            } else {
                log.error("metrics does not exist, skipping comparison step");
            }
        } catch (IOException ioe) {
            log.error(ioe);
            throw new RuntimeException(ioe);
        }

        this.testDefinition = testDefinition;
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
    public void afterEachRunTest() {
        //
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

        workflowSwid = exec.installWorkflow(bundledWorkflow);

        Assert.assertNotNull(workflowSwid);

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

        log.warn(testName + " starting execution");

        StringBuilder extraArgs = new StringBuilder();
        for(Entry<String, Object> e : testDefinition.parameters.entrySet()){
            extraArgs.append(" ").append(e.getKey()).append(" ").append(e.getValue().toString());
        }

        exec.deciderRunSchedule(deciderJar, workflowSwid, studies, sequencerRuns, samples, extraArgs.toString());

        log.warn(testName + " execution complete");

    }

    @Test(dependsOnGroups = "execution", groups = "postExecution")
    public void calculateWorkflowRunReport() throws JsonProcessingException, IOException {

        Workflow w = new Workflow();
        w.setSwid(workflowSwid.getSwid());

        actual = getWorkflowReport(w);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String x = mapper.writeValueAsString(actual);
        actualReportFile = new File(workingDirectory.getAbsolutePath() + "/" + testDefinition.outputName());
        Assert.assertFalse(actualReportFile.exists());

        FileUtils.write(actualReportFile, x);
//        
//        boolean populateMetricsDirectory = true;        
//        if(populateMetricsDirectory && expected == null){
//            File outputDir = new File(t.metricsDirectory);
//            Assert.assertTrue(outputDir.exists() && outputDir.isDirectory() && outputDir.canWrite());
//            
//            File outputFile = outputDir.
//            
//        }

        log.debug(x);

        Assert.assertNotNull(actual);
        //Assert.assertTrue(actual.isConsistent);

        reports.add(actualReportFile);

    }

    @Test(dependsOnGroups = "execution", dependsOnMethods = "calculateWorkflowRunReport", groups = "postExecution")
    public void compareWorkflowRunReport() throws JsonProcessingException, IOException {

        if (expected == null) {
            Assert.fail("no expected output to compare to.");
        }
        //TODO: option to provide sw accession of successful run?

        Assert.assertTrue(compareReports(actual, expected),
                "There are differences between reports:\nExpected: " + expectedReportFile + "\nActual: " + actualReportFile);
                //+ "\nExpected object:\n" + expected.toString() + "\nActual object:\n" + actual.toString());

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

        List<WorkflowRunReportRecord> wrrs = seq.getWorkflowRunRecords(w);

        TestResult t = new TestResult();
        t.setWorkflowRunCount(wrrs.size());

        for (WorkflowRunReportRecord wrr : wrrs) {

            //TODO: get workflow run object from workflow run report record
            WorkflowRun wr = new WorkflowRun();
            wr.setSwid(wrr.getWorkflowRunSwid());

            //Get the workflow run's parent accession(s) (processing accession(s))
            List<Accessionable> parentAccessions = seq.getParentAccessions(wr);
            
            //Get the workflow run's input file(s) (file accession(s))
            List<Accessionable> inputFileAccessions = seq.getInputFileAccessions(wr);

            //TODO: 0.13.x series deciders do not use input_files, generalize parent and input file accessions
            if (inputFileAccessions.isEmpty()) {
                log.warn("overriding input file accessions with parent accessions, workflow run swid=[" + wr.getSwid() + "]");
                inputFileAccessions = parentAccessions;
            }

            t.addStudies(seq.getStudy(parentAccessions));
            t.addSamples(seq.getSamples(parentAccessions));
            t.addSequencerRuns(seq.getSequencerRuns(parentAccessions));
            t.addLanes(seq.getLanes(parentAccessions));

            t.addWorkflows(seq.getWorkflows(inputFileAccessions));
            t.addProcessingAlgorithms(seq.getProcessingAlgorithms(inputFileAccessions));
            t.addFileMetaTypes(seq.getFileMetaTypes(inputFileAccessions));

            List<ReducedFileProvenanceReportRecord> files = seq.getFiles(inputFileAccessions);
            if (files.size() > t.getMaxInputFiles()) {
                t.setMaxInputFiles(files.size());
            }

            if (files.size() < t.getMinInputFiles()) {
                t.setMinInputFiles(files.size());
            }

            Map ini = seq.getWorkflowRunIni(wr);
            for (String s : testDefinition.iniExclusions) {
                ini.remove(s);
            }

            WorkflowRunReport x = new WorkflowRunReport();
            x.setWorkflowIni(ini);
            x.setFiles(files);

            t.addWorkflowRun(x);

        }

        return t;

    }

}
