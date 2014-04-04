package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.dao.SeqwareInterface;
import ca.on.oicr.pde.model.Accessionable;
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
import de.danielbechler.diff.ObjectDifferFactory;
import de.danielbechler.diff.node.Node;
import de.danielbechler.diff.visitor.PrintingVisitor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    TestResult actual;
    TestResult expected;

    TestDefinition.Test td;

    public DeciderRunTest(SeqwareInterface seq, File seqwareDistribution, File seqwareSettings, File workingDirectory, String testName,
            File deciderJar, File bundledWorkflow, String deciderClass, TestDefinition.Test td) {

        super(seqwareDistribution, seqwareSettings, workingDirectory, testName);

        this.seq = seq;
        this.deciderJar = deciderJar;
        this.bundledWorkflow = bundledWorkflow;
        this.deciderClass = deciderClass;

        for (String s : td.studies) {
            Study x = new Study();
            x.setTitle(s);
            studies.add(x);
        }

        for (String s : td.samples) {
            Sample x = new Sample();
            x.setName(s);
            samples.add(x);
        }

        for (String s : td.sequencerRuns) {
            SequencerRun x = new SequencerRun();
            x.setName(s);
            sequencerRuns.add(x);
        }

        try {
            File metricsFile = td.metrics();
            if (metricsFile != null) {
                log.error("found a metrics file: " + metricsFile.getAbsolutePath());
                expected = TestResult.buildFromJson(metricsFile);
            } else {
                log.error("metrics does not exist, will skip comparison step");
            }
        } catch (IOException ioe) {
            log.error(ioe);
            throw new RuntimeException(ioe);
        }

        this.td = td;
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

        exec.deciderRunSchedule(deciderJar, workflowSwid, studies, sequencerRuns, samples, "");

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
        File output = new File(workingDirectory.getAbsolutePath() + "/" + td.outputName());
        Assert.assertFalse(output.exists());

        FileUtils.write(output, x);
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

        reports.add(output);

    }

    @Test(dependsOnGroups = "execution", dependsOnMethods = "calculateWorkflowRunReport", groups = "postExecution")
    public void compareWorkflowRunReport() throws JsonProcessingException, IOException {

        if (expected == null) {
            Assert.fail("no expected output to compare to.");
        }
        //TODO: option to provide sw accession of successful run?

        Assert.assertTrue(compareReports(actual, expected));

    }

    public static <T> boolean compareReports(T actual, T expected) {

        Node root = ObjectDifferFactory.getInstance().compare(actual, expected);

        log.warn("root categories: " + root.getCategories());
        log.warn(root.getChildren());
        //diff is not working root.visit(new PrintingVisitor(actual, expected));

        return (!root.hasChanges());

    }

    private TestResult getWorkflowReport(Workflow w) {

        List<WorkflowRunReportRecord> wrrs = seq.getWorkflowRunRecords(w);

        TestResult t = new TestResult();
        t.setWorkflowRunCount(wrrs.size());

        for (WorkflowRunReportRecord wrr : wrrs) {

            //TODO: get workflow run object from workflow run report record
            WorkflowRun wr = new WorkflowRun();
            wr.setSwid(wrr.getWorkflowRunSwid());

            //Get the workflow run's parent accession (aka, the input files)
            List<Accessionable> parentAccessions = seq.getParentAccessions(wr);
            List<Accessionable> inputFileAccessions = seq.getInputFileAccessions(wr);

            t.addStudies(seq.getStudy(parentAccessions));
            t.addSamples(seq.getSamples(parentAccessions));
            t.addSequencerRuns(seq.getSequencerRuns(parentAccessions));
            t.addLanes(seq.getLanes(parentAccessions));

            t.addWorkflows(seq.getWorkflows(inputFileAccessions));
            t.addProcessingAlgorithms(seq.getProcessingAlgorithms(inputFileAccessions));
            t.addFileMetaTypes(seq.getFileMetaTypes(inputFileAccessions));

            if (parentAccessions.size() > t.getMaxInputFiles()) {
                t.setMaxInputFiles(inputFileAccessions.size());
            }

            if (parentAccessions.size() < t.getMinInputFiles()) {
                t.setMinInputFiles(inputFileAccessions.size());
            }

            Map ini = seq.getWorkflowRunIni(wr);
            for (String s : td.iniExclusions) {
                ini.remove(s);
            }

            TestResult.WorkflowRun x = new TestResult.WorkflowRun();
            x.setWorkflowIni(ini);
            x.setFiles(seq.getFiles(inputFileAccessions));

            t.addWorkflowRun(x);

        }

        return t;

    }

}
