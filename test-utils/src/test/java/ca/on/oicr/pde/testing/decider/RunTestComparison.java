package ca.on.oicr.pde.testing.decider;

import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class RunTestComparison {

    RunTestReport testReport;

    @BeforeTest
    public void buildDeciderRunTestReport() {
        Map<String, String> ini = new HashMap<>();
        ini.put("java", "${workflow_bundle_dir}/Workflow_Bundle_WORKFLOW_NAME/WORKFLOW_VERSION/bin/java");
        ini.put("cat", "${workflow_bundle_dir}/Workflow_Bundle_WORKFLOW_NAME/WORKFLOW_VERSION/bin/cat");

        WorkflowRunReport runReport = new WorkflowRunReport();
        runReport.setWorkflowIni(ini);

        testReport = new RunTestReport();
        testReport.addWorkflowRun(runReport);
    }

    @Test
    public void name() {
        RunTestSuiteDefinition defn = new RunTestSuiteDefinition();
        
        RunTestDefinition defaults = new RunTestDefinition();
        defaults.setDescription("Default test description");
        defn.setDefaults(defaults);

        String workflowName = "ExpectedWorkflow";
        String workflowVersion = "1.0";

        Map<String, String> iniStringSubstitutions = new HashMap<>();
        iniStringSubstitutions.put("\\$\\{workflow_bundle_dir\\}/Workflow_Bundle_[^/]+/[^/]+/", "\\$\\{workflow_bundle_dir\\}/Workflow_Bundle_" + workflowName + "/" + workflowVersion + "/");

        RunTestDefinition testDefinition = defn.getDeciderRunTestDefinition();
        testDefinition.setId("test1");
        testDefinition.setIniStringSubstitutions(iniStringSubstitutions);

        testReport.applyIniStringSubstitutions(testDefinition.getIniStringSubstitutions());

        for (WorkflowRunReport wrr : testReport.getWorkflowRuns()) {
            Assert.assertEquals(wrr.getWorkflowIni().get("java"), "${workflow_bundle_dir}/Workflow_Bundle_" + workflowName + "/" + workflowVersion + "/bin/java");
            Assert.assertEquals(wrr.getWorkflowIni().get("cat"), "${workflow_bundle_dir}/Workflow_Bundle_" + workflowName + "/" + workflowVersion + "/bin/cat");
        }
    }
}
