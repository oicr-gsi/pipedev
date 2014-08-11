package ca.on.oicr.pde.testing.workflow;

import ca.on.oicr.pde.model.SeqwareAccession;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import net.sourceforge.seqware.common.util.maptools.MapTools;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OozieWorkflowRunTest extends WorkflowRunTest {

    SeqwareAccession workflowSwid;
    SeqwareAccession workflowRunSwid;

    Metadata metadb;

    public OozieWorkflowRunTest(File seqwareDistribution, File seqwareSettings, File workingDirectory, String testName,
            File workflowBundlePath, String workflowName, String workflowVersion, File workflowBundleBinPath,
            List<File> workflowInis, File expectedOutput, File calculateMetricsScript, File compareMetricsScript,
            Map<String, String> environmentVariables, Map<String,String> parameters) throws IOException {

        super(seqwareDistribution, seqwareSettings, workingDirectory, testName, workflowBundlePath, workflowName, workflowVersion, workflowBundleBinPath,
                workflowInis, expectedOutput, calculateMetricsScript, compareMetricsScript, environmentVariables, parameters);

    }

    @Override
    public void initializeEnvironment() throws IOException {

        super.initializeEnvironment();

        //Get a seqware metadb object for probing the metadb state
        HashMap<String, String> hm = new HashMap<String, String>();
        MapTools.ini2Map(seqwareSettings.getAbsolutePath(), hm, true);
        metadb = MetadataFactory.get(hm);

    }

//    @Test(groups = "preExecution")
//    public void checkEnvironment() {
//
//        //TODO: verify webservice url is accessible
//        //TODO: verify scheduling host is accessible
//        
//    }
    @Test(groups = "preExecution")
    public void installWorkflow() throws IOException {

        workflowSwid = exec.installWorkflow(workflowBundlePath);

        Assert.assertNotNull(workflowSwid);

    }

    @Test(groups = "preExecution", dependsOnMethods = "installWorkflow")
    public void scheduleWorkflow() throws IOException {

        workflowRunSwid = exec.workflowRunSchedule(workflowSwid, workflowInis, parameters);

        Assert.assertNotNull(workflowRunSwid);

    }

    @Override
    public void executeWorkflow() throws IOException {

        //TODO: integrate this process into SeqwareExecutor
        exec.workflowRunLaunch(workflowRunSwid);

        String workflowRunStatus = "pending";
        while (Arrays.asList("running", "pending").contains(workflowRunStatus)) {

            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException ie) {
                System.out.println(ie.getStackTrace());
            }

            exec.workflowRunUpdateStatus(workflowRunSwid);
            workflowRunStatus = exec.workflowRunReport(workflowRunSwid);

        }

        //TODO: what if the test/workflow run should fail?
        //TODO: print std out/err if workflow failed.
        Assert.assertEquals(workflowRunStatus, "completed");

    }

//    @Test(dependsOnGroups = "execution", groups = "postExecution")
//    public void testCommand3() {
//
//        //TODO: check db state
//
//    }
}
