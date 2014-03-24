package ca.on.oicr.pde.common;

import ca.on.oicr.pde.common.OozieWorkflowRunTest;
import ca.on.oicr.pde.common.WorkflowRunTest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;

public class WorkflowRunTestFactory {

    private static int count = 0;
    private List<WorkflowRunTest> tests;

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

        String schedulingSystem = System.getProperty("schedulingSystem");

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

            if ("oozie".equals(schedulingSystem)) {
                tests.add(new OozieWorkflowRunTest(count++, description, inputConfigDir + inputConfig, outputMetricsDir + outputMetrics, metricsCalculateCommand, metricsCompareCommand, environmentVariables));
            } else if ("pegasus".equals(schedulingSystem)) {
                tests.add(new WorkflowRunTest(count++, description, inputConfigDir + inputConfig, outputMetricsDir + outputMetrics, metricsCalculateCommand, metricsCompareCommand, environmentVariables));
            } else {
                throw new RuntimeException();
            }

        }

        return tests.toArray();

    }
}
