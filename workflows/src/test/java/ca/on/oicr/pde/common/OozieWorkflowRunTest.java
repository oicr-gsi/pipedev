package ca.on.oicr.pde.common;

import ca.on.oicr.pde.common.utilities.SeqwareOutputParser;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import net.sourceforge.seqware.common.util.maptools.MapTools;
//import net.sourceforge.seqware.common.model.WorkflowRunStatus;
import org.testng.Assert;
import org.apache.commons.io.FileUtils;

public class OozieWorkflowRunTest extends WorkflowRunTest {

    String workflowRunSwid;

    public OozieWorkflowRunTest(int testID, String description, String workflowConfigPath, String outputExpectationFilePath, String calculateMetricsScriptName,
            String compareMetricsScriptName, Map<String, String> environmentVariables) throws IOException {

        super(testID, description, workflowConfigPath, outputExpectationFilePath, calculateMetricsScriptName, compareMetricsScriptName, environmentVariables);

    }

    @Override
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
        command.append(" ").append(UUID.randomUUID());

        String seqwareSettingsPath = executeCommand(command.toString(), testWorkingDirectory);
        Assert.assertTrue(FileUtils.getFile(seqwareSettingsPath).exists(), "Generate seqware settings failed - please verify seqwareDirectory is accessible");

        //Record seqware setting file path for execute workflow step
        environmentVariables.put("SEQWARE_SETTINGS", seqwareSettingsPath);
        Assert.assertNotNull(environmentVariables.get("SEQWARE_SETTINGS"), "");

        //Get a seqware metadb object for probing the metadb state
        HashMap<String, String> hm = new HashMap<String, String>();
        MapTools.ini2Map(seqwareSettingsPath, hm, true);
        Metadata metadb = MetadataFactory.get(hm);
        //TODO: verify metadb is accessible

        //Install the workflow and get its swid
        StringBuilder installCommand = new StringBuilder();
        installCommand.append("java -jar ").append(seqwareDistribution);
        installCommand.append(" --plugin net.sourceforge.seqware.pipeline.plugins.BundleManager --");
        installCommand.append(" --bundle ").append(bundledWorkflowPath);
        installCommand.append(" --install-dir-only");

        String workflowSwid = SeqwareOutputParser.getSwidFromOutput(executeCommand(installCommand.toString(), testWorkingDirectory, environmentVariables));
        print("Workflow name:" + metadb.getWorkflow(Integer.parseInt(workflowSwid)).getFullName().toString());

        //Schedule a workflow run
        StringBuilder scheduleCommand = new StringBuilder();
        scheduleCommand.append("java -cp ").append(seqwareDistribution);
        scheduleCommand.append(" io.seqware.cli.Main workflow schedule");
        scheduleCommand.append(" --accession ").append(workflowSwid);
        scheduleCommand.append(" --host no");
        scheduleCommand.append(" --ini ").append(workflowIniFile);
        scheduleCommand.append(" --override manual_output=true");
        scheduleCommand.append(" --override output_prefix=").append(testWorkingDirectory).append("/");
        scheduleCommand.append(" --override output_dir=").append("output");

        workflowRunSwid = SeqwareOutputParser.getSwidFromOutput(executeCommand(scheduleCommand.toString(), testWorkingDirectory, environmentVariables));

    }

    @Override
    public void executeWorkflow() throws IOException {

        StringBuilder launchWorkflowCommand = new StringBuilder();
        launchWorkflowCommand.append("java -cp ").append(seqwareDistribution);
        launchWorkflowCommand.append(" io.seqware.cli.Main workflow-run launch-scheduled");
        launchWorkflowCommand.append(" --accession ").append(workflowRunSwid);
        executeCommand(launchWorkflowCommand.toString(), testWorkingDirectory, environmentVariables);

        //String workflowRunStatus = WorkflowRunStatus.pending.toString();
        String workflowRunStatus = "pending";

        while (Arrays.asList("running", "pending").contains(workflowRunStatus)) {

            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException ie) {
                System.out.println(ie.getStackTrace());
            }

            StringBuilder updateWorkflowRunStatusCommand = new StringBuilder();
            updateWorkflowRunStatusCommand.append("java -cp ").append(seqwareDistribution);
            updateWorkflowRunStatusCommand.append(" io.seqware.cli.Main workflow-run propagate-statuses");
            updateWorkflowRunStatusCommand.append(" --accession ").append(workflowRunSwid);
            executeCommand(updateWorkflowRunStatusCommand.toString(), testWorkingDirectory, environmentVariables);

            StringBuilder checkWorkflowRunStatusCommand = new StringBuilder();
            checkWorkflowRunStatusCommand.append("java -cp ").append(seqwareDistribution);
            checkWorkflowRunStatusCommand.append(" io.seqware.cli.Main workflow-run report");
            checkWorkflowRunStatusCommand.append(" --accession ").append(workflowRunSwid);
            workflowRunStatus = SeqwareOutputParser.getWorkflowRunStatusFromOutput(executeCommand(checkWorkflowRunStatusCommand.toString(), testWorkingDirectory, environmentVariables));
        }

        Assert.assertTrue(Arrays.asList("completed").contains(workflowRunStatus));

    }

}
