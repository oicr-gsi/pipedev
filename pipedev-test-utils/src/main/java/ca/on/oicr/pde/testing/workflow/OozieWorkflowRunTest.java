package ca.on.oicr.pde.testing.workflow;

import ca.on.oicr.pde.dao.executor.ShellExecutor;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.model.WorkflowRun;
import net.sourceforge.seqware.common.util.maptools.MapTools;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OozieWorkflowRunTest extends WorkflowRunTest {

    Workflow workflow;
    WorkflowRun workflowRun;

    Metadata metadb;

    public OozieWorkflowRunTest(File seqwareDistribution, File seqwareSettings, File workingDirectory, String testName,
            File workflowBundlePath, String workflowName, String workflowVersion, File workflowBundleBinPath,
            List<File> workflowInis, String actualOutputFileName, File expectedOutput, File calculateMetricsScript, File compareMetricsScript,
            Map<String, String> environmentVariables, Map<String, String> parameters) throws IOException {

        super(seqwareDistribution, seqwareSettings, workingDirectory, testName, workflowBundlePath, workflowName, workflowVersion, workflowBundleBinPath,
                workflowInis, actualOutputFileName, expectedOutput, calculateMetricsScript, compareMetricsScript, environmentVariables, parameters);

    }

    @Override
    public void initializeEnvironment() throws IOException {

        super.initializeEnvironment();

        //Get a seqware metadb object for probing the metadb state
        HashMap<String, String> hm = new HashMap<>();
        MapTools.ini2Map(seqwareSettings.getAbsolutePath(), hm, true);
        metadb = MetadataFactory.get(hm);

        //This forces DFSClient to use hostnames rather than IP addresses when communicating with the hadoop datanode.  This is required 
        //when submitting to an oozie/hadoop service that is using open stack floating ip addresses.
        Configuration c = new Configuration(false);
        c.set("dfs.client.use.datanode.hostname", "true");
        File hdfsConfiguration = new File(workingDirectory + "/conf/" + "hdfs-site.xml");
        try (OutputStream os = FileUtils.openOutputStream(hdfsConfiguration)) {
            c.writeXml(os);
        }
        if (seqwareExecutor instanceof ShellExecutor) {
            ((ShellExecutor) seqwareExecutor).addToClassPath(workingDirectory + "/conf/");
        } else {
            throw new UnsupportedOperationException("Unable to add hdfs-site.xml to classpath.");
        }
    }

    @Test(groups = "preExecution")
    public void installWorkflow() throws IOException {

        workflow = seqwareExecutor.installWorkflow(workflowBundlePath);

        Assert.assertNotNull(workflow);

    }

    @Test(groups = "preExecution", dependsOnMethods = "installWorkflow")
    public void scheduleWorkflow() throws IOException {

        workflowRun = seqwareExecutor.workflowRunSchedule(workflow, workflowInis, parameters);

        Assert.assertNotNull(workflowRun);

        Reporter.getCurrentTestResult().setAttribute("workflow run swid", workflowRun.getSwAccession());
    }

    @Override
    public void launchWorkflow() throws IOException {
        seqwareExecutor.workflowRunLaunch(workflowRun);

        Assert.assertTrue(Arrays.asList("running", "pending").contains(seqwareExecutor.workflowRunStatus(workflowRun)));

    }

    @Override
    public void monitorWorkflow() throws IOException {
        String workflowRunStatus = "pending";
        while (Arrays.asList("running", "pending").contains(workflowRunStatus)) {

            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException ie) {
                System.out.println(ie.getStackTrace());
            }

            seqwareExecutor.workflowRunUpdateStatus(workflowRun);
            workflowRunStatus = seqwareExecutor.workflowRunStatus(workflowRun);
        }

        Assert.assertEquals(workflowRunStatus, "completed");
    }

    }
}
