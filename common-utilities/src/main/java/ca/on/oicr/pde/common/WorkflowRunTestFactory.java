package ca.on.oicr.pde.common;

import static ca.on.oicr.pde.utilities.Helpers.generateSeqwareSettings;
import static ca.on.oicr.pde.utilities.Helpers.generateTestWorkingDirectory;
import static ca.on.oicr.pde.utilities.Helpers.getRequiredSystemPropertyAsFile;
import static ca.on.oicr.pde.utilities.Helpers.getRequiredSystemPropertyAsString;
import static ca.on.oicr.pde.utilities.Helpers.getScriptFromResource;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
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
        schedulingSystem = getRequiredSystemPropertyAsString("schedulingSystem");
        schedulingHost = getRequiredSystemPropertyAsString("schedulingHost");

    }

    @Parameters({"testDefinition"})
    @Factory
    public Object[] createTests(String testDefinition) throws IOException {

        //Get the test configuration json document
        JsonNode testConfig = new ObjectMapper().readTree(FileUtils.readFileToString(FileUtils.toFile(getClass().getResource(testDefinition))));

        //Get default and common settings
        String defaultDescription = testConfig.get("defaults").get("description").getTextValue();
        String defaultMetricsCalculateCommand = testConfig.get("defaults").get("metrics_calculate").getTextValue();
        String defaultMetricsCompareCommand = testConfig.get("defaults").get("metrics_compare").getTextValue();
        String defaultInputConfigDir = testConfig.get("defaults").get("input_config_dir").getTextValue();
        String defaultOutputMetricsDir = testConfig.get("defaults").get("output_metrics_dir").getTextValue();

        //build a list of tests
        tests = new ArrayList<WorkflowRunTest>();

        int count = 0;
        //Generate a new test for each test defined in json file
        for (JsonNode test : testConfig.get("tests")) {
            String description = test.get("description") == null ? defaultDescription : test.get("description").getTextValue();
            String inputConfigDir = test.get("input_config_dir") == null ? defaultInputConfigDir : test.get("input_config_dir").getTextValue();
            String inputConfig = test.get("input_config").getTextValue();
            String outputMetricsDir = test.get("output_metrics_dir") == null ? defaultOutputMetricsDir : test.get("output_metrics_dir").getTextValue();
            String outputMetrics = test.get("output_metrics") == null ? inputConfig + ".metrics" : test.get("output_metrics").getTextValue();
            String metricsCalculateCommand = test.get("metrics_calculate") == null ? defaultMetricsCalculateCommand : test.get("metrics_calculate").getTextValue();
            String metricsCompareCommand = test.get("metrics_compare") == null ? defaultMetricsCompareCommand : test.get("metrics_compare").getTextValue();

            Map<String, String> environmentVariables = new HashMap<String, String>();
            if (test.get("environment_variables") != null) {
                Iterator<Entry<String, JsonNode>> ji = test.get("environment_variables").getFields();
                while (ji.hasNext()) {
                    Entry<String, JsonNode> e = ji.next();
                    environmentVariables.put(e.getKey(), e.getValue().getTextValue());
                }
            }

            File workflowIni = new File(inputConfigDir + "/" + inputConfig);
            File expectedOutput = new File(outputMetricsDir + "/" + outputMetrics);
            File calculateMetricsScript = getScriptFromResource(metricsCalculateCommand);
            File compareMetricsScript = getScriptFromResource(metricsCompareCommand);

            String testName = "WorkflowRunTest_" + (count++) + "_" + workflowName + "-" + workflowVersion;
            String prefix = new SimpleDateFormat("yyMMdd_HHmm").format(new Date());
            String testId = UUID.randomUUID().toString().substring(0, 7);
            File testWorkingDir = generateTestWorkingDirectory(workingDirectory, prefix, testName, testId);

            File seqwareSettings = generateSeqwareSettings(testWorkingDir, seqwareWebserviceUrl, schedulingSystem, schedulingHost);

            if ("oozie".equals(schedulingSystem)) {
                tests.add(new OozieWorkflowRunTest(seqwareDistribution, seqwareSettings, testWorkingDir, testName, workflowBundlePath, workflowName, workflowVersion, workflowBundleBinPath, workflowIni, expectedOutput, calculateMetricsScript, compareMetricsScript, environmentVariables));
            } else if ("pegasus".equals(schedulingSystem)) {
                tests.add(new WorkflowRunTest(seqwareDistribution, seqwareSettings, testWorkingDir, testName, workflowBundlePath, workflowName, workflowVersion, workflowBundleBinPath, workflowIni, expectedOutput, calculateMetricsScript, compareMetricsScript, environmentVariables));
            } else {
                throw new RuntimeException();
            }

        }

        return tests.toArray();

    }
}
