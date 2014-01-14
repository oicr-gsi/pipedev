package ca.on.oicr.pde.common;

import ca.on.oicr.pde.common.CommandRunner.CommandResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.*;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class WorkflowRunTest implements org.testng.ITest {

    private static String bundledJava;
    private static String seqwareDistribution;
    private static String bundledWorkflowPath;
    private static String workflowName;
    private static String workflowVersion;
    private static String workingDirectory;
    private static String schedulingSystem;
    private static String seqwareWebserviceUrl;
    private static String schedulingHost;

    private int testID;
    private String calculateMetricsScriptName;
    private String compareMetricsScriptName;
    private File testWorkingDirectory;
    private File workflowIniFile;
    private File outputExpectationFile;
    private File calculateMetricsScript;
    private File compareMetricsScript;

    private Map<String, String> environmentVariables;

    static {

        //Get common test parameters from pom.xml testng system properties
        bundledJava = System.getProperty("bundledJava");
        seqwareDistribution = System.getProperty("seqwareDistribution");
        bundledWorkflowPath = System.getProperty("bundledWorkflow");
        workflowName = System.getProperty("workflowName");
        workflowVersion = System.getProperty("workflowVersion");
        workingDirectory = System.getProperty("workingDirectory");
        seqwareWebserviceUrl = System.getProperty("webserviceUrl");
        schedulingSystem = System.getProperty("schedulingSystem");
        schedulingHost = System.getProperty("schedulingHost");

    }

    public WorkflowRunTest(int testID, String description, String workflowConfigPath, String outputExpectationFilePath, String calculateMetricsScriptName,
            String compareMetricsScriptName, Map<String, String> environmentVariables) {

        this.testID = testID;
        this.workflowIniFile = new File(workflowConfigPath);
        this.calculateMetricsScriptName = calculateMetricsScriptName;
        this.compareMetricsScriptName = compareMetricsScriptName;
        this.environmentVariables = environmentVariables;
        this.outputExpectationFile = new File(outputExpectationFilePath);

    }

    @BeforeSuite
    public void workflowCommon() {

        Assert.assertNotNull(bundledJava, "Bundled java path is not set - set bundledJava in pom.xml.");
        Assert.assertNotNull(seqwareDistribution, "Seqware distribution path is not set - set seqwareDistribution in pom.xml.");
        Assert.assertNotNull(bundledWorkflowPath, "Bundled workflow path is not set - set bundledWorkflow in pom.xml.");
        Assert.assertNotNull(workflowName, "Workflow name is not set - set workflowName in pom.xml.");
        Assert.assertNotNull(workflowVersion, "Workflow version is not set - set workflowVersion in pom.xml.");
        Assert.assertNotNull(workingDirectory, "Working directory path is not set - set workingDirectory in pom.xml.");
        Assert.assertNotNull(schedulingSystem, "Scheduling system is not set - set schedulingSystem in pom.xml.");
        Assert.assertNotNull(seqwareWebserviceUrl, "Webservice url is not set - set webserviceUrl in pom.xml.");
        Assert.assertNotNull(schedulingHost, "Scheduling host is not set - set schedulingHost in pom.xml.");

        Assert.assertTrue(FileUtils.getFile(bundledJava).exists(),
                "Bundled java path does not exist - verify bundledJava is correct in pom.xml.");
        Assert.assertTrue(FileUtils.getFile(seqwareDistribution).exists(),
                "Seqware distribution does not exist - verify seqwareDistribution is correct in pom.xml.");
        Assert.assertTrue(FileUtils.getFile(bundledWorkflowPath).exists(),
                "Bundled workflow path does not exist - verify bundledWorkflow is correct in pom.xml.");

    }

    @BeforeClass
    public void workflowSpecific() throws IOException {

        Assert.assertTrue(workflowIniFile.exists() && workflowIniFile.canRead() && workflowIniFile.isFile(),
                String.format("The seqware ini file [%s] is not accessible.", workflowIniFile));

        String prefix = new SimpleDateFormat("yyMMdd_HHmm").format(new Date()) + "_";
        String suffix = ""; //"_" + UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        
        testWorkingDirectory = new File(workingDirectory + "/" + prefix + getTestName() + suffix + "/");
        testWorkingDirectory.mkdir();
        
        print(String.format("Bundled Workflow Path=[%s]\n"
                + "Seqware Distribution=[%s]\n"
                + "Seqware INI File=[%s]\n"
                + "Seqware Scheduling Host=[%s](System=[%s])\n"
                + "Seqware Webservice Host=[%s]\n"
                + "Workflow Run Test Working Directory=[%s]",
                bundledWorkflowPath, seqwareDistribution, workflowIniFile, schedulingHost,
                schedulingSystem, seqwareWebserviceUrl, testWorkingDirectory));

    }

    @Test
    public void initializeEnvironment() throws IOException {

        Assert.assertTrue(outputExpectationFile.exists() && outputExpectationFile.canRead() && outputExpectationFile.isFile(),
                String.format("The output expectation file [%s] is not accessible - please generate it using calculate script.", outputExpectationFile));

        calculateMetricsScript = getScriptFromResource(calculateMetricsScriptName);

        compareMetricsScript = getScriptFromResource(compareMetricsScriptName);

        //TODO: verify webservice url is accessible
        //TODO: verify scheduling host is accessible
        StringBuilder command = new StringBuilder();
        command.append(getScriptFromResource("generateSeqwareSettings.sh"));
        command.append(" ").append(testWorkingDirectory);
        command.append(" ").append(seqwareWebserviceUrl);
        command.append(" ").append(schedulingSystem);
        command.append(" ").append(schedulingHost);

        String seqwareSettingsPath = executeCommand(command.toString());
        Assert.assertTrue(FileUtils.getFile(seqwareSettingsPath).exists(), "Generate seqware settings failed - please verify seqwareDirectory is accessible");

        //Record seqware setting file path for execute workflow step
        environmentVariables.put("SEQWARE_SETTINGS", seqwareSettingsPath);
        Assert.assertNotNull(environmentVariables.get("SEQWARE_SETTINGS"), "");

    }

    @Test(dependsOnMethods = "initializeEnvironment", enabled = true)
    public void executeWorkflow() throws IOException {

        //TODO: refactor to use seqware 1.x commandline
        StringBuilder command = new StringBuilder();
        command.append("java -jar ").append(seqwareDistribution);
        command.append(" --plugin net.sourceforge.seqware.pipeline.plugins.WorkflowLauncher");
        command.append(" --");
        command.append(" --no-metadata");
        command.append(" --provisioned-bundle-dir ").append(bundledWorkflowPath);
        command.append(" --workflow ").append(workflowName);
        command.append(" --version ").append(workflowVersion);
        command.append(" --ini-files ").append(workflowIniFile);
        command.append(" --wait");
        command.append(" --");
        command.append(" --manual_output true");
        command.append(" --output_prefix ").append(testWorkingDirectory).append("/");
        command.append(" --output_dir ").append("output");

        executeCommand(command.toString(), environmentVariables);

    }

    @Test(dependsOnMethods = "executeWorkflow", enabled = true)
    public void checkWorkflowOutputExists() {

        File workflowOutputDirectory = new File(testWorkingDirectory + "/output/");
        Assert.assertTrue(workflowOutputDirectory.exists() && workflowOutputDirectory.isDirectory(),
                String.format("The workflow output directory [%s] is not accessible.", workflowOutputDirectory));

    }

    @Test(dependsOnMethods = "checkWorkflowOutputExists", enabled = true)
    public void compareOutputToExpected() throws IOException {

        StringBuilder command = new StringBuilder();
        command.append(compareMetricsScript + " " + "<(" + calculateMetricsScript + " " + testWorkingDirectory + "/output" + ")" + " " + outputExpectationFile);

        executeCommand(command.toString());

    }

    @AfterClass
    public void afterTest() {
        //TODO: clean up testWorkingDirectory if tests were successful?
    }

    @Override
    public String getTestName() {

        return getClass().getSimpleName() + "_" + testID + "_" + workflowName + "-" + workflowVersion;

    }

    @Override
    public String toString() {

        return this.getTestName();

    }

    private File getScriptFromResource(String scriptName) throws IOException {

        File script = File.createTempFile(scriptName, ".sh");
        script.setExecutable(true);
        script.deleteOnExit();

        InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(scriptName);
        Assert.assertNotNull(resourceStream, String.format("Script resource [%s] was not found - verify that script exists as a resource.", scriptName));

        //Write resource to temporary file that can then be executed.
        FileUtils.writeStringToFile(script, IOUtils.toString(resourceStream));

        return script;

    }

    private String executeCommand(String command, Map<String, String>... environmentVariables) throws IOException {

        CommandLine c = new CommandLine("/bin/bash");
        c.addArgument("-c");
        c.addArgument(command, false);

        CommandRunner cr = new CommandRunner();
        cr.setCommand(c);
        for (Map<String, String> e : environmentVariables) {
            cr.addEnvironmentVariable(e);
        }

        print("Executing:\n" + command.toString());

        CommandResult r = cr.runCommand();
        Assert.assertTrue(r.getExitCode() == 0,
                String.format("The following command returned a non-zero exit code [%s]:\n%s\nOutput from command:\n%s\n",
                        r.getExitCode(), command, r.getOutput()));

        return r.getOutput().trim();

    }

    private void print(String s) {

        System.out.println(getTestName() + "{\n" + s + "\n}");

    }
}
