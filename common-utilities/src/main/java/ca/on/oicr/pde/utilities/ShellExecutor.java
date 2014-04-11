package ca.on.oicr.pde.utilities;

import ca.on.oicr.pde.model.Name;
import ca.on.oicr.pde.model.Sample;
import ca.on.oicr.pde.model.SequencerRun;
import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.model.Study;
import ca.on.oicr.pde.parsers.SeqwareOutputParser;
import static com.google.common.base.Preconditions.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShellExecutor implements SeqwareExecutor {

    private final File workingDirectory;
    private final File seqwareDistribution;
    private final File seqwareSettings;
    private final Map<String, String> environmentVariables;
    private final String id;

    public ShellExecutor(String id, File seqwareDistrubution, File seqwareSettings, File workingDirectory) {

        this.seqwareSettings = checkNotNull(seqwareSettings);
        this.environmentVariables = new HashMap<String, String>();
        environmentVariables.put("SEQWARE_SETTINGS", this.seqwareSettings.getAbsolutePath());

        this.seqwareDistribution = checkNotNull(seqwareDistrubution);

        checkNotNull(workingDirectory);
        checkArgument(workingDirectory.exists() && workingDirectory.isDirectory(), "The working directory %s is invalid", workingDirectory.getAbsolutePath());
        this.workingDirectory = workingDirectory;

        this.id = checkNotNull(id);

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
    public void deciderRunSchedule(File deciderJar, SeqwareAccession workflowSwid, List<Study> studies, List<SequencerRun> sequencerRuns, List<Sample> samples, String extraArgs) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -jar ").append(deciderJar);
        //cmd.append(" --plugin ").append(o.getClass().getCanonicalName()).append(" -- "); //need to add the decider jar to this shells classpath
        cmd.append(" --wf-accession ").append(workflowSwid.getSwid());
        cmd.append(listToParamString(" --study-name ", studies));
        cmd.append(listToParamString(" --sequencer-run-name ", sequencerRuns));
        cmd.append(listToParamString(" --sample-name ", samples));
        cmd.append(" --schedule");
        cmd.append(" --force-run-all");
        cmd.append(extraArgs);

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, true, environmentVariables);

    }

    @Override
    public SeqwareAccession workflowRunSchedule(SeqwareAccession workflowSwid, File workflowIniFile) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(seqwareDistribution);
        cmd.append(" io.seqware.cli.Main workflow schedule");
        cmd.append(" --accession ").append(workflowSwid.getSwid());
        cmd.append(" --host no");
        cmd.append(" --ini ").append(workflowIniFile);
        cmd.append(" --override manual_output=true");
        cmd.append(" --override output_prefix=").append(workingDirectory).append("/");
        cmd.append(" --override output_dir=").append("output");

        return new SeqwareAccession(SeqwareOutputParser.getSwidFromOutput(Helpers.executeCommand(id, cmd.toString(), workingDirectory, environmentVariables)));

    }

    @Override
    public void workflowRunLaunch(SeqwareAccession workflowRunSwid) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(seqwareDistribution);
        cmd.append(" io.seqware.cli.Main workflow-run launch-scheduled");
        cmd.append(" --accession ").append(workflowRunSwid.getSwid());

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, environmentVariables);

    }

    @Override
    public void workflowRunLaunch(File workflowBundle, File workflowIniFile, String workflowName, String workflowVersion) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -jar ").append(seqwareDistribution);
        cmd.append(" --plugin net.sourceforge.seqware.pipeline.plugins.WorkflowLauncher");
        cmd.append(" --");
        cmd.append(" --no-metadata");
        cmd.append(" --provisioned-bundle-dir ").append(workflowBundle);
        cmd.append(" --workflow ").append(workflowName);
        cmd.append(" --version ").append(workflowVersion);
        cmd.append(" --ini-files ").append(workflowIniFile);
        cmd.append(" --wait");
        cmd.append(" --");
        cmd.append(" --manual_output true");
        cmd.append(" --output_prefix ").append(workingDirectory).append("/");
        cmd.append(" --output_dir ").append("output");

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, environmentVariables);

    }

    @Override
    public void workflowRunUpdateStatus(SeqwareAccession workflowRunSwid) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(seqwareDistribution);
        cmd.append(" io.seqware.cli.Main workflow-run propagate-statuses");
        cmd.append(" --accession ").append(workflowRunSwid.getSwid());

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, environmentVariables);

    }

    @Override
    public String workflowRunReport(SeqwareAccession workflowRunSwid) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(seqwareDistribution);
        cmd.append(" io.seqware.cli.Main workflow-run report");
        cmd.append(" --accession ").append(workflowRunSwid.getSwid());

        return SeqwareOutputParser.getWorkflowRunStatusFromOutput(Helpers.executeCommand(id, cmd.toString(), workingDirectory, environmentVariables));

    }

    /**
     *
     * @param prefix The string to prefix each <code>objects</code> "Name" representation
     * @param objects A list of objects that implement the "Name" interface.
     * @return A string in the following format: [prefix][object 1's name][prefix][object 2's name]...
     */
    public static String listToParamString(String prefix, List<? extends Name> objects) {

        StringBuilder result = new StringBuilder();

        if (objects != null && !objects.isEmpty()) {
            for (Name o : objects) {
                result.append(prefix).append(o.getName());
            }
        }

        return result.toString();

    }

}
