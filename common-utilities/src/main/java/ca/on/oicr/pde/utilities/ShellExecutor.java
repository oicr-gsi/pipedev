package ca.on.oicr.pde.utilities;

import ca.on.oicr.pde.model.Name;
import ca.on.oicr.pde.model.Sample;
import ca.on.oicr.pde.model.SequencerRun;
import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.model.Study;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.parsers.SeqwareOutputParser;
import com.google.common.base.Joiner;
import static com.google.common.base.Preconditions.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FileUtils;

public class ShellExecutor implements SeqwareExecutor {

    protected final File workingDirectory;
    protected final File seqwareDistribution;
    protected final File seqwareSettings;
    protected final Map<String, String> environmentVariables;
    protected final String id;
    protected final File loggingDirectory;

    public ShellExecutor(String id, File seqwareDistrubution, File seqwareSettings, File workingDirectory) {

        this.seqwareSettings = checkNotNull(seqwareSettings);
        this.seqwareDistribution = checkNotNull(seqwareDistrubution);
        this.id = checkNotNull(id);
        checkNotNull(workingDirectory);
        checkArgument(workingDirectory.exists() && workingDirectory.isDirectory(), "The working directory %s is invalid", workingDirectory.getAbsolutePath());
        this.workingDirectory = workingDirectory;

        this.environmentVariables = new HashMap<String, String>();
        environmentVariables.put("SEQWARE_SETTINGS", this.seqwareSettings.getAbsolutePath());

        // Setup a java tmp dir that is located within the working directory
        File javaTmpDir = new File(workingDirectory + "/" + "javaTmp");
        javaTmpDir.mkdir();
        environmentVariables.put("_JAVA_OPTIONS", System.getenv("_JAVA_OPTIONS") == null
                ? "-Djava.io.tmpdir=" + javaTmpDir.getAbsolutePath() : System.getenv("_JAVA_OPTIONS") + " " + "-Djava.io.tmpdir=" + javaTmpDir.getAbsolutePath());

        // Setup an output/logging directory for command output
        loggingDirectory = new File(workingDirectory + "/" + "logs");
        loggingDirectory.mkdir();

    }

    @Override
    public SeqwareAccession installWorkflow(File bundledWorkflowPath) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -jar ").append(seqwareDistribution);
        cmd.append(" --plugin net.sourceforge.seqware.pipeline.plugins.BundleManager");
        cmd.append(" -- --bundle ").append(bundledWorkflowPath);
        cmd.append(" --install-dir-only");

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "installWorkflow.out");

        return new SeqwareAccession(SeqwareOutputParser.getSwidFromOutput(Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables)));

    }

    @Override
    public void deciderRunSchedule(File deciderJar, SeqwareAccession workflowSwid, List<String> studies, List<String> sequencerRuns, List<String> samples, String extraArgs) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -jar ").append(deciderJar);
        //cmd.append(" --plugin ").append(o.getClass().getCanonicalName()).append(" -- "); //need to add the decider jar to this shells classpath
        cmd.append(" --wf-accession ").append(workflowSwid.getSwid());
        cmd.append(listToParamString(" --study-name ", studies));
        cmd.append(listToParamString(" --sequencer-run-name ", sequencerRuns));
        cmd.append(listToParamString(" --sample-name ", samples));
        cmd.append(" --schedule");
        cmd.append(extraArgs);

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "deciderRunSchedule.out");

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables);

    }

    @Override
    public SeqwareAccession workflowRunSchedule(SeqwareAccession workflowSwid, List<File> workflowIniFiles, Map<String, String> parameters) throws IOException {

        //Temporary fix, see parameters handling section below
        if (!parameters.isEmpty()) {
            File parametersFile = File.createTempFile("extra_parameters", ".ini");
            Properties p = new Properties();
            p.putAll(parameters);
            p.store(FileUtils.openOutputStream(parametersFile), "");
            workflowIniFiles.add(parametersFile);
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(seqwareDistribution);
        cmd.append(" io.seqware.cli.Main workflow schedule");
        cmd.append(" --accession ").append(workflowSwid.getSwid());
        cmd.append(" --host no");
        cmd.append(" --ini ").append(Joiner.on(",").join(workflowIniFiles));
        cmd.append(" --override manual_output=true");
        cmd.append(" --override output_prefix=").append(workingDirectory).append("/");
        cmd.append(" --override output_dir=").append("output");

//      FIXME: seqware command line cannot handle special characters.  For example, --override some_param=--the_param_to_pass_to_workflow will fail
//      ... so parameters are treated currently as an iniFile        
//        //Add additional parameters from map.  Note, these paramters override entries in ini file
//        for (Entry<String, String> e : parameters.entrySet()) {
//            cmd.append(" --override ").append(e.getKey()).append("=").append(escapeForSeqware(e.getValue()));
//        }
        File stdOutAndErrFile = new File(loggingDirectory + "/" + "workflowRunSchedule.out");

        return new SeqwareAccession(SeqwareOutputParser.getSwidFromOutput(Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables)));

    }

    @Override
    public void workflowRunLaunch(SeqwareAccession workflowRunSwid) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(seqwareDistribution);
        cmd.append(" io.seqware.cli.Main workflow-run launch-scheduled");
        cmd.append(" --accession ").append(workflowRunSwid.getSwid());

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "workflowRunLaunch.out");

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables);

    }

    @Override
    public void workflowRunLaunch(File workflowBundle, List<File> workflowIniFiles, String workflowName, String workflowVersion) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -jar ").append(seqwareDistribution);
        cmd.append(" --plugin net.sourceforge.seqware.pipeline.plugins.WorkflowLauncher");
        cmd.append(" --");
        cmd.append(" --no-metadata");
        cmd.append(" --provisioned-bundle-dir ").append(workflowBundle);
        cmd.append(" --workflow ").append(workflowName);
        cmd.append(" --version ").append(workflowVersion);
        cmd.append(" --ini-files ").append(Joiner.on(",").join(workflowIniFiles));
        cmd.append(" --wait");
        cmd.append(" --");
        cmd.append(" --manual_output true");
        cmd.append(" --output_prefix ").append(workingDirectory).append("/");
        cmd.append(" --output_dir ").append("output");

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "workflowRunLaunch.out");

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables);

    }

    @Override
    public void workflowRunUpdateStatus(SeqwareAccession workflowRunSwid) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(seqwareDistribution);
        cmd.append(" io.seqware.cli.Main workflow-run propagate-statuses");
        cmd.append(" --accession ").append(workflowRunSwid.getSwid());

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "workflowRunUpdateStatus.out");

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables);

    }

    @Override
    public String workflowRunReport(SeqwareAccession workflowRunSwid) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(seqwareDistribution);
        cmd.append(" io.seqware.cli.Main workflow-run report");
        cmd.append(" --accession ").append(workflowRunSwid.getSwid());

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "workflowRunReport.out");

        return SeqwareOutputParser.getWorkflowRunStatusFromOutput(Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables));

    }

    @Override
    public void cancelWorkflowRun(WorkflowRun wr) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(seqwareDistribution);
        cmd.append(" io.seqware.cli.Main workflow-run cancel");
        cmd.append(" --accession ").append(wr.getSwid());

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "cancelWorkflowRun.out");

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables);

    }

    /**
     *
     * @param prefix The string to prefix each <code>objects</code> "Name" representation
     * @param objects A list of objects that implement the "Name" interface.
     * @return A string in the following format: [prefix][object 1's name][prefix][object 2's name]...
     */
    public static String listToParamString(String prefix, List<? extends String> objects) {

        StringBuilder result = new StringBuilder();

        if (objects != null && !objects.isEmpty()) {
            for (String o : objects) {
                result.append(prefix).append(o.toString());
            }
        }

        return result.toString();

    }

    //method in development to escape strings for seqware command line
    private String escapeForSeqware(String s) {
        String result = s;

        // spaces need to be escaped from " " to "\ "
        result = result.replaceAll(" ", "\\\\ ");

        return result;
    }

    @Override
    public void cancelWorkflowRuns(Workflow w) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
