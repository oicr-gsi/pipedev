package ca.on.oicr.pde.utilities;

import ca.on.oicr.pde.model.Name;
import ca.on.oicr.pde.model.Sample;
import ca.on.oicr.pde.model.SequencerRun;
import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.model.Study;
import ca.on.oicr.pde.parsers.SeqwareOutputParser;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShellExecutor implements SeqwareExecutor {

    private final File workingDirectory;
    private final File seqwareDistribution;
    private final Map<String, String> environmentVariables;
    private final String id;

    public ShellExecutor(String id, File seqwareDistrubution, File seqwareSettings, File workingDirectory) {

        this.environmentVariables = new HashMap<String, String>();
        environmentVariables.put("SEQWARE_SETTINGS", seqwareSettings.getAbsolutePath());
        this.seqwareDistribution = seqwareDistrubution;
        this.workingDirectory = workingDirectory;
        this.id = id;

    }

    @Override
    public SeqwareAccession installWorkflow(File bundledWorkflowPath) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -jar ").append(seqwareDistribution);
        cmd.append(" --plugin net.sourceforge.seqware.pipeline.plugins.BundleManager");
        cmd.append(" -- --bundle ").append(bundledWorkflowPath);
        cmd.append(" --install-dir-only");

        return new SeqwareAccession(SeqwareOutputParser.getSwidFromOutput(Helpers.executeCommand(id, cmd.toString(), workingDirectory, environmentVariables)));

    }

    @Override
    public void executeDecider(File deciderJar, SeqwareAccession workflowSwid, List<Study> studies, List<SequencerRun> sequencerRuns, List<Sample> samples, String extraArgs) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -jar ").append(deciderJar);
        //cmd.append(" --plugin ").append(o.getClass().getCanonicalName()).append(" -- "); //need to add the decider jar to this shells classpath
        cmd.append(" --wf-accession ").append(workflowSwid.getSwid());
        cmd.append(listToParamString(" --study-name ", studies));
        cmd.append(listToParamString(" --sequencer-run-name ", sequencerRuns));
        cmd.append(listToParamString(" --sample-name ", samples));
        cmd.append(" --schedule");
        cmd.append(extraArgs);

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, environmentVariables);

    }

    private String listToParamString(String prefix, List<? extends Name> os) {

        StringBuilder result = new StringBuilder();

        if (os != null && !os.isEmpty()) {
            for (Name o : os) {
                result.append(prefix).append(o.getName());
            }
        }

        return result.toString();

    }

    @Override
    public SeqwareAccession workflowRunSchedule(SeqwareAccession workflowSwid, File workflowIniFile) throws IOException {

        StringBuilder scheduleCommand = new StringBuilder();
        scheduleCommand.append("java -cp ").append(seqwareDistribution);
        scheduleCommand.append(" io.seqware.cli.Main workflow schedule");
        scheduleCommand.append(" --accession ").append(workflowSwid.getSwid());
        scheduleCommand.append(" --host no");
        scheduleCommand.append(" --ini ").append(workflowIniFile);
        scheduleCommand.append(" --override manual_output=true");
        scheduleCommand.append(" --override output_prefix=").append(workingDirectory).append("/");
        scheduleCommand.append(" --override output_dir=").append("output");

        return new SeqwareAccession(SeqwareOutputParser.getSwidFromOutput(Helpers.executeCommand(id, scheduleCommand.toString(), workingDirectory, environmentVariables)));

    }

    @Override
    public void workflowRunLaunch(SeqwareAccession workflowRunSwid) throws IOException {

        StringBuilder launchWorkflowCommand = new StringBuilder();
        launchWorkflowCommand.append("java -cp ").append(seqwareDistribution);
        launchWorkflowCommand.append(" io.seqware.cli.Main workflow-run launch-scheduled");
        launchWorkflowCommand.append(" --accession ").append(workflowRunSwid.getSwid());

        Helpers.executeCommand(id, launchWorkflowCommand.toString(), workingDirectory, environmentVariables);

    }

    @Override
    public void workflowRunLaunch(File workflowBundle, File workflowIniFile, String workflowName, String workflowVersion) throws IOException {

        StringBuilder command = new StringBuilder();
        command.append("java -jar ").append(seqwareDistribution);
        command.append(" --plugin net.sourceforge.seqware.pipeline.plugins.WorkflowLauncher");
        command.append(" --");
        command.append(" --no-metadata");
        command.append(" --provisioned-bundle-dir ").append(workflowBundle);
        command.append(" --workflow ").append(workflowName);
        command.append(" --version ").append(workflowVersion);
        command.append(" --ini-files ").append(workflowIniFile);
        command.append(" --wait");
        command.append(" --");
        command.append(" --manual_output true");
        command.append(" --output_prefix ").append(workingDirectory).append("/");
        command.append(" --output_dir ").append("output");

        Helpers.executeCommand(id, command.toString(), workingDirectory, environmentVariables);

    }

    @Override
    public void workflowRunUpdateStatus(SeqwareAccession workflowRunSwid) throws IOException {

        StringBuilder updateWorkflowRunStatusCommand = new StringBuilder();
        updateWorkflowRunStatusCommand.append("java -cp ").append(seqwareDistribution);
        updateWorkflowRunStatusCommand.append(" io.seqware.cli.Main workflow-run propagate-statuses");
        updateWorkflowRunStatusCommand.append(" --accession ").append(workflowRunSwid.getSwid());
        
        Helpers.executeCommand(id, updateWorkflowRunStatusCommand.toString(), workingDirectory, environmentVariables);

    }

    @Override
    public String workflowRunReport(SeqwareAccession workflowRunSwid) throws IOException {

        StringBuilder checkWorkflowRunStatusCommand = new StringBuilder();
        checkWorkflowRunStatusCommand.append("java -cp ").append(seqwareDistribution);
        checkWorkflowRunStatusCommand.append(" io.seqware.cli.Main workflow-run report");
        checkWorkflowRunStatusCommand.append(" --accession ").append(workflowRunSwid.getSwid());
        
        return SeqwareOutputParser.getWorkflowRunStatusFromOutput(Helpers.executeCommand(id, checkWorkflowRunStatusCommand.toString(), workingDirectory, environmentVariables));

    }

}
