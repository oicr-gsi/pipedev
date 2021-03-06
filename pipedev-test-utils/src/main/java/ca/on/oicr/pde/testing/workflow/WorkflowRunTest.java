package ca.on.oicr.pde.testing.workflow;

import ca.on.oicr.pde.testing.common.RunTestBase;
import ca.on.oicr.pde.utilities.Helpers;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Listeners({ca.on.oicr.pde.testing.testng.TestCaseReporter.class})
public abstract class WorkflowRunTest extends RunTestBase {

    private final static Logger log = LogManager.getLogger(WorkflowRunTest.class);

    //protected final String bundledJava;
    protected final File workflowBundleBinPath;
    protected final List<File> workflowInis;
    protected final File expectedOutput;
    protected final File calculateMetricsScript;
    protected final File compareMetricsScript;
    protected final Map<String, String> environmentVariables;
    protected final File workflowOutputDirectory;
    protected final File workflowBundlePath;
    protected final String workflowName;
    protected final String workflowVersion;
    protected final Map<String, String> parameters;

    protected File actualOutput;

    public WorkflowRunTest(File seqwareDistribution, File seqwareSettings, File workingDirectory, String testName,
            File workflowBundlePath, String workflowName, String workflowVersion, File workflowBundleBinPath,
            List<File> workflowInis, String actualOutputFileName, File expectedOutput, File calculateMetricsScript, File compareMetricsScript,
            Map<String, String> environmentVariables, Map<String, String> parameters) throws IOException {

        super(seqwareDistribution, seqwareSettings, workingDirectory, testName);

        this.workflowBundlePath = workflowBundlePath;
        this.workflowName = workflowName;
        this.workflowVersion = workflowVersion;
        this.workflowBundleBinPath = workflowBundleBinPath;
        this.workflowInis = workflowInis;
        this.calculateMetricsScript = calculateMetricsScript;
        this.compareMetricsScript = compareMetricsScript;
        this.environmentVariables = new LinkedHashMap<>(environmentVariables);
        this.expectedOutput = expectedOutput;
        this.workflowOutputDirectory = new File(workingDirectory + "/output/");
        this.parameters = new LinkedHashMap<>(parameters);
        this.actualOutput = new File(workingDirectory + "/" + actualOutputFileName);

        // Add all directories located within "workflowBundleBinPath" to the PATH
        this.environmentVariables.put("PATH", Helpers.buildPathFromDirectory(System.getenv("PATH"), workflowBundleBinPath));

    }

    @BeforeClass
    public void beforeAllTests() throws IOException {

        Assert.assertNotNull(seqwareDistribution,
                "Seqware distribution path is not set - set seqwareDistribution in pom.xml.");
        Assert.assertNotNull(workflowBundlePath,
                "Bundled workflow path is not set - set bundledWorkflow in pom.xml.");
        Assert.assertNotNull(workflowName,
                "Workflow name is not set - set workflowName in pom.xml.");
        Assert.assertNotNull(workflowVersion,
                "Workflow version is not set - set workflowVersion in pom.xml.");
        Assert.assertNotNull(workingDirectory,
                "Working directory path is not set - set workingDirectory in pom.xml.");
        Assert.assertTrue(FileUtils.getFile(seqwareDistribution).exists(),
                "Seqware distribution does not exist - verify seqwareDistribution is correct in pom.xml.");
        Assert.assertTrue(FileUtils.getFile(workflowBundlePath).exists(),
                "Bundled workflow path does not exist - verify bundledWorkflow is correct in pom.xml.");
//        Assert.assertTrue(expectedOutput.exists() && expectedOutput.canRead() && expectedOutput.isFile(),
//                String.format("The output expectation file [%s] is not accessible - please generate it using calculate script.", expectedOutput));

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
    public void afterAllTests() {
        //
    }

    @Test(groups = "preExecution")
    public void initializeEnvironment() throws IOException {
        //
    }

    @Test(dependsOnGroups = "preExecution", groups = "execution")
    public abstract void launchWorkflow() throws IOException;

    @Test(dependsOnMethods = "launchWorkflow", groups = "execution")
    public abstract void monitorWorkflow() throws IOException;

    @Test(dependsOnGroups = "execution", groups = "postExecution")
    public void checkWorkflowOutputExists() {

        Assert.assertTrue(workflowOutputDirectory.exists() && workflowOutputDirectory.isDirectory(),
                String.format("The workflow output directory [%s] is not accessible.", workflowOutputDirectory));

    }

    @Test(dependsOnGroups = "execution", dependsOnMethods = "checkWorkflowOutputExists", groups = "postExecution")
    public void calculateOutputMetrics() throws IOException {

        Assert.assertTrue(!actualOutput.exists(),
                String.format("The actual output metrics file [%s] already exists", actualOutput));

        StringBuilder command = new StringBuilder();
        command.append(calculateMetricsScript + " " + workflowOutputDirectory + " > " + actualOutput);

        Helpers.executeCommand(testName, command.toString(), workflowOutputDirectory, environmentVariables);

    }

    @Test(dependsOnGroups = "execution", dependsOnMethods = "calculateOutputMetrics", groups = "postExecution")
    public void compareOutputToExpected() throws IOException {

        Assert.assertTrue(expectedOutput != null && expectedOutput.exists() && expectedOutput.canRead() && expectedOutput.isFile(),
                String.format("The output expectation file [%s] is not accessible.", expectedOutput));

        StringBuilder command = new StringBuilder();
        command.append(compareMetricsScript + " " + actualOutput + " " + expectedOutput);

        Helpers.executeCommand(testName, command.toString(), workingDirectory, environmentVariables);

    }

}
