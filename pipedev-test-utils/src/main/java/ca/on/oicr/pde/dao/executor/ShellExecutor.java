package ca.on.oicr.pde.dao.executor;

import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.parsers.SeqwareOutputParser;
import ca.on.oicr.pde.utilities.Helpers;
import com.google.common.base.Joiner;
import static com.google.common.base.Preconditions.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.model.WorkflowRun;
import org.apache.commons.io.FileUtils;

public class ShellExecutor implements SeqwareExecutor {

    protected final File workingDirectory;
    protected final File seqwareDistribution;
    protected final File seqwareSettings;
    protected final Map<String, String> environmentVariables;
    protected final String id;
    protected final File loggingDirectory;
    protected final List<String> classPath;

    public ShellExecutor(String id, File seqwareDistrubution, File seqwareSettings, File workingDirectory) {

        this.seqwareSettings = checkNotNull(seqwareSettings);
        this.seqwareDistribution = checkNotNull(seqwareDistrubution);
        this.id = checkNotNull(id);
        checkNotNull(workingDirectory);
        checkArgument(workingDirectory.exists() && workingDirectory.isDirectory(),
                "The working directory %s is invalid", workingDirectory.getAbsolutePath());
        this.workingDirectory = workingDirectory;

        this.environmentVariables = new HashMap<>();
        environmentVariables.put("SEQWARE_SETTINGS", this.seqwareSettings.getAbsolutePath());

        StringBuilder javaOptions = new StringBuilder();

        String javaOptsFromMaven = System.getProperty("java_opts");
        if (javaOptsFromMaven != null && !javaOptsFromMaven.isEmpty()) {
            javaOptions.append(javaOptsFromMaven);
            javaOptions.append(" ");
        }

        // Setup a java tmp dir that is located within the working directory
        File javaTmpDir = new File(workingDirectory + "/" + "javaTmp");
        javaTmpDir.mkdir();
        javaOptions.append("-Djava.io.tmpdir=").append(javaTmpDir.getAbsolutePath());

        environmentVariables.put("_JAVA_OPTIONS", javaOptions.toString());

        // Setup an output/logging directory for command output
        loggingDirectory = new File(workingDirectory + "/" + "logs");
        loggingDirectory.mkdir();

        classPath = new ArrayList<>();
        classPath.add(seqwareDistrubution.getAbsolutePath());
    }

    @Override
    public Workflow installWorkflow(File bundledWorkflowPath) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -jar ").append(seqwareDistribution);
        cmd.append(" --plugin net.sourceforge.seqware.pipeline.plugins.BundleManager");
        cmd.append(" -- --bundle ").append(bundledWorkflowPath);
        cmd.append(" --install-dir-only");

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "installWorkflow.out");

        Integer swid = Integer.parseInt(SeqwareOutputParser.getSwidFromOutput(
                Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables)));
        Workflow w = new Workflow();
        w.setSwAccession(swid);
        return w;
    }

    @Override
    public void deciderRunSchedule(File deciderJar, Workflow workflow, List<String> studies, List<String> sequencerRuns,
            List<String> samples, String extraArgs) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -jar ").append(deciderJar);
        cmd.append(" --wf-accession ").append(workflow.getSwAccession());
        cmd.append(listToParamString(" --study-name ", studies));
        cmd.append(listToParamString(" --sequencer-run-name ", sequencerRuns));
        cmd.append(listToParamString(" --sample-name ", samples));
        cmd.append(extraArgs);

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "deciderRunSchedule.out");

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables);
    }

    @Override
    public WorkflowRun workflowRunSchedule(Workflow workflow, List<File> workflowIniFiles,
            Map<String, String> parameters) throws IOException {

        //Temporary fix, see parameters handling section below
        if (!parameters.isEmpty()) {
            File parametersFile = File.createTempFile("extra_parameters", ".ini");
            Properties p = new Properties();
            p.putAll(parameters);
            p.store(FileUtils.openOutputStream(parametersFile), "");
            workflowIniFiles.add(parametersFile);
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(getClassPathAsString());
        cmd.append(" io.seqware.cli.Main workflow schedule");
        cmd.append(" --accession ").append(workflow.getSwAccession());
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

        Integer swid = Integer.parseInt(SeqwareOutputParser.getSwidFromOutput(
                Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables)));
        WorkflowRun wr = new WorkflowRun();
        wr.setSwAccession(swid);
        return wr;
    }

    @Override
    public void workflowRunLaunch(WorkflowRun workflowRun) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(getClassPathAsString());
        cmd.append(" io.seqware.cli.Main workflow-run launch-scheduled");
        cmd.append(" --accession ").append(workflowRun.getSwAccession());

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "workflowRunLaunch.out");

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables);
    }

    @Override
    public void workflowRunLaunch(File workflowBundle, List<File> workflowIniFiles, String workflowName,
            String workflowVersion) throws IOException {

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
    public void workflowRunUpdateStatus(WorkflowRun workflowRun) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(getClassPathAsString());
        cmd.append(" io.seqware.cli.Main workflow-run propagate-statuses");
        cmd.append(" --accession ").append(workflowRun.getSwAccession());

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "workflowRunUpdateStatus.out");

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables);
    }

    @Override
    public String workflowRunReport(WorkflowRun workflowRunSwid) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(getClassPathAsString());
        cmd.append(" io.seqware.cli.Main workflow-run report");
        cmd.append(" --accession ").append(workflowRunSwid.getSwAccession());

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "workflowRunReport.out");

        return SeqwareOutputParser.getWorkflowRunStatusFromOutput(
                Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables));
    }

    @Override
    public void cancelWorkflowRun(WorkflowRun wr) throws IOException {

        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp ").append(getClassPathAsString());
        cmd.append(" io.seqware.cli.Main workflow-run cancel");
        cmd.append(" --accession ").append(wr.getSwAccession());

        File stdOutAndErrFile = new File(loggingDirectory + "/" + "cancelWorkflowRun.out");

        Helpers.executeCommand(id, cmd.toString(), workingDirectory, stdOutAndErrFile, environmentVariables);
    }

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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deciderRunSchedule(String decider, Workflow workflow, String... deciderArgs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String getClassPathAsString() {
        return "\"" + Joiner.on(":").join(classPath) + "\"";
    }

    public void addToClassPath(String path) {
        classPath.add(path);
    }
}
