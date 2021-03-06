package ca.on.oicr.pde.testing;

import ca.on.oicr.pde.testing.workflow.TestDefinition;
import ca.on.oicr.pde.testing.workflow.OozieWorkflowRunTest;
import ca.on.oicr.pde.testing.workflow.WorkflowRunTest;
import static ca.on.oicr.pde.utilities.Helpers.generateSeqwareSettings;
import static ca.on.oicr.pde.utilities.Helpers.generateTestWorkingDirectory;
import static ca.on.oicr.pde.utilities.Helpers.getRequiredSystemPropertyAsFile;
import static ca.on.oicr.pde.utilities.Helpers.getRequiredSystemPropertyAsString;
import static ca.on.oicr.pde.utilities.Helpers.getScriptFromResource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;

public class WorkflowRunTestFactory {

    private List<WorkflowRunTest> tests;

    //private final File bundledJava;
    private final File workflowBundleBinPath;
    private final File seqwareDistribution;
    private final File workflowBundlePath;
    private final String workflowName;
    private final String workflowVersion;
    private final File workingDirectory;
    private final String seqwareWebserviceUrl;
    private final String seqwareWebserviceUser;
    private final String seqwareWebservicePassword;
    private final String schedulingSystem;
    private final String schedulingHost;

    public WorkflowRunTestFactory() {

        //Get common test parameters from pom.xml testng system properties
        //bundledJava = getRequiredSystemPropertyAsFile("bundledJava");
        seqwareDistribution = getRequiredSystemPropertyAsFile("seqwareDistribution");
        workflowBundlePath = getRequiredSystemPropertyAsFile("bundledWorkflow");
        workflowBundleBinPath = getRequiredSystemPropertyAsFile("bundledBinPath");
        workflowName = getRequiredSystemPropertyAsString("workflowName");
        workflowVersion = getRequiredSystemPropertyAsString("workflowVersion");
        workingDirectory = getRequiredSystemPropertyAsFile("workingDirectory");
        seqwareWebserviceUrl = getRequiredSystemPropertyAsString("webserviceUrl");
        seqwareWebserviceUser = getRequiredSystemPropertyAsString("webserviceUser");
        seqwareWebservicePassword = getRequiredSystemPropertyAsString("webservicePassword");
        schedulingSystem = getRequiredSystemPropertyAsString("schedulingSystem");
        schedulingHost = getRequiredSystemPropertyAsString("schedulingHost");

    }

    @Parameters({"testDefinition"})
    @Factory
    public Object[] createTests(String testDefinition) throws IOException {
        TestDefinition td = null;
        if (getClass().getResource(testDefinition) != null) {
            td = TestDefinition.buildFromJson(FileUtils.readFileToString(FileUtils.toFile(getClass().getResource(testDefinition))));
        } else if (Paths.get(testDefinition).toFile().exists()) {
            td = TestDefinition.buildFromJson(FileUtils.readFileToString(Paths.get(testDefinition).toFile()));
        } else {
            throw new RuntimeException("Unable to load tests from test definition = [" + testDefinition + "]");
        }

        tests = new ArrayList();
        int count = 0;
        for (TestDefinition.Test t : td.getTests()) {
            for (int i = 0; i < t.getIterations(); i++) {
                String testName = "WorkflowRunTest_" + (count++) + "_" + workflowName + "-" + workflowVersion;
                String prefix = new SimpleDateFormat("yyMMdd_HHmm").format(new Date());
                String testId = UUID.randomUUID().toString().substring(0, 7);
                File testWorkingDir = generateTestWorkingDirectory(workingDirectory, prefix, testName, testId);
                File seqwareSettings = generateSeqwareSettings(testWorkingDir, seqwareWebserviceUrl, seqwareWebserviceUser, seqwareWebservicePassword, schedulingSystem, schedulingHost);

                Path scriptDirectory = Files.createDirectory(Paths.get(testWorkingDir.getAbsolutePath()).resolve("scripts"));
                File calculateMetricsScript = getScriptFromResource(t.getMetricsCalculateScript(), scriptDirectory);
                File compareMetricsScript = getScriptFromResource(t.getMetricsCompareScript(), scriptDirectory);

                List<File> iniFiles = new ArrayList<>();

                //Add a blank ini file to list (need at least one ini file for seqware command line)
                iniFiles.add(File.createTempFile("blank", "ini"));

                //Add user specified ini file if it is accessible
                if (t.getIniFile() != null) {
                    iniFiles.add(t.getIniFile());
                }

                String actualOutputFileName = "";
                if (StringUtils.isNotBlank(t.getId())) {
                    actualOutputFileName = StringUtils.trim(t.getId()) + ".metrics";
                } else if (t.getIniFile() != null && StringUtils.isNotBlank(t.getIniFile().getName())) {
                    actualOutputFileName = t.getIniFile().getName() + ".metrics";
                } else {
                    throw new RuntimeException();
                }

                if ("oozie".equals(schedulingSystem)) {
                    tests.add(new OozieWorkflowRunTest(seqwareDistribution, seqwareSettings, testWorkingDir, testName,
                            workflowBundlePath, workflowName, workflowVersion, workflowBundleBinPath, iniFiles, actualOutputFileName, t.getMetricsFile(),
                            calculateMetricsScript, compareMetricsScript, t.getEnvironmentVariables(), t.getParameters()));
                } else {
                    throw new RuntimeException("Unsupported schedulingSystem type.");
                }
            }
        }

        return tests.toArray();

    }
}
