package ca.on.oicr.pde.workflows;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ca.on.oicr.pde.testing.workflow.DryRun;
import ca.on.oicr.pde.testing.workflow.TestDefinition;
import net.sourceforge.seqware.common.util.maptools.MapTools;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WorkflowTest {

    public WorkflowTest() {

    }

    private Workflow getWorkflow() throws IOException {
        File defaultIniFile = new File(System.getProperty("bundleDirectory") + "/config/defaults.ini");
        String defaultIniFileContents = FileUtils.readFileToString(defaultIniFile);

        Workflow wf = new Workflow();
        wf.setConfigs(MapTools.iniString2Map(defaultIniFileContents));

        return wf;
    }

    @Test
    public void validateDevelopmentTestCases() throws IllegalAccessException, InstantiationException, IOException, Exception {
        File testDefinitionFile = new File(System.getProperty("testDefinitionFile"));
        if (!testDefinitionFile.exists() || !testDefinitionFile.canRead()) {
            Assert.fail("Can not access test definition file = [" + testDefinitionFile + "]");
        }

        TestDefinition td = TestDefinition.buildFromJson(FileUtils.readFileToString(testDefinitionFile));
        for (TestDefinition.Test t : td.getTests()) {
            Map<String, String> parameters = new HashMap<>(t.getParameters());
            parameters.put("cromwell_host", "???");

            DryRun d = new DryRun(System.getProperty("bundleDirectory"), parameters, Workflow.class);
            d.buildWorkflowModel();
            d.validateWorkflow();
        }
    }

    @Test
    public void testInit() throws IOException {

        Map<String, String> config = new HashMap<>();
        config.put("wdl_inputs", "{\"key\":\"value\"}");
        config.put("cromwell_host", "???");

        Workflow wf = getWorkflow();
        wf.getConfigs().putAll(config);
        wf.setupDirectory();

        Assert.assertEquals(wf.getProperty("output_prefix"), "./");
        Assert.assertEquals(wf.getProperty("manual_output"), "false");
    }
}
