package ca.on.oicr.pde.workflows;

import ca.on.oicr.pde.utilities.workflows.OicrWorkflow;
import io.seqware.pipeline.SqwKeys;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.util.configtools.ConfigTools;

import net.sourceforge.seqware.pipeline.workflowV2.model.Command;
import net.sourceforge.seqware.pipeline.workflowV2.model.Job;
import net.sourceforge.seqware.pipeline.workflowV2.model.SqwFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Workflow extends OicrWorkflow {

    private final Logger logger = LoggerFactory.getLogger(Workflow.class);

    private String javaPath;
    private String seqwareDistributionJarPath;
    private String cromwellRunScript;
    private String provisionWdlOutputScript;
    private String cromwellJarPath;
    private String cromwellHost;
    private String pollingInterval;
    private String wdlWorkflow;
    private String wdlInputs;
    private String wdlOutputs;
    private String wdlOptions;
    private String wdlDepsZip;
    private boolean manualOutput = false;
    private String jobMemory;
    private String outputDir = "output/";
    private String tmpDir = "tmp/";

    private void init() {
        javaPath = getRequiredProperty("java_path");
        seqwareDistributionJarPath = getRequiredProperty("seqware_distribution_jar_path");
        cromwellRunScript = getRequiredProperty("cromwell_run_script");
        provisionWdlOutputScript = getRequiredProperty("provision_wdl_output_script");
        cromwellJarPath = getRequiredProperty("cromwell_jar_path");
        cromwellHost = getRequiredProperty("cromwell_host");
        pollingInterval = getRequiredProperty("polling_interval");
        wdlWorkflow = getRequiredProperty("wdl_workflow");
        wdlInputs = getRequiredProperty("wdl_inputs");
        wdlOutputs = getOptionalProperty("wdl_outputs", null);
        wdlOptions = getOptionalProperty("wdl_options", null);
        wdlDepsZip = getOptionalProperty("wdl_deps_zip", null);
        manualOutput = Boolean.valueOf(getRequiredProperty("manual_output"));
        jobMemory = getRequiredProperty("job_memory");
    }

    @Override
    public void setupDirectory() {
        //since setupDirectory is the first method run, we use it to initialize variables too.
        init();

        this.addDirectory(tmpDir);
        this.addDirectory(outputDir);
    }

    @Override
    public Map<String, SqwFile> setupFiles() {
        return this.getFiles();
    }

    @Override
    public void buildWorkflow() {

        Job setup = newJob("setup");
        setup.setMaxMemory(jobMemory);
        Command setupCommand = setup.getCommand();

        // write wdl workflow to file
        String wdlWorkflowFile;
        if (wdlWorkflow.startsWith("file://")) {
            wdlWorkflowFile = wdlWorkflow;
        } else {
            wdlWorkflowFile = tmpDir + "workflow.wdl";
            setupCommand.getArguments().addAll(writeStringToFile(wdlWorkflow, wdlWorkflowFile));
        }

        // write wdl inputs to file
        String wdlInputsFile;
        if (wdlInputs.startsWith("file://")) {
            //cromwell current does not support inputs urls, remove file:// prefix
            wdlInputsFile = wdlInputs.replaceFirst("^file://", "");
        } else {
            wdlInputsFile = tmpDir + "inputs.json";
            setupCommand.getArguments().addAll(writeStringToFile(wdlInputs, wdlInputsFile));
        }

        // write wdl options to file
        String wdlOptionsFile;
        if (wdlOptions == null || wdlOptions.isEmpty()) {
            wdlOptionsFile = null;
        } else if (wdlOptions.startsWith("file://")) {
            //cromwell current does not support options urls, remove file:// prefix
            wdlOptionsFile = wdlOptions.replaceFirst("^file://", "");
        } else {
            wdlOptionsFile = tmpDir + "options.json";
            setupCommand.getArguments().addAll(writeStringToFile(wdlOptions, wdlOptionsFile));
        }

        String wdlOutputsFile;
        if (wdlOutputs == null || wdlOutputs.isEmpty()) {
            wdlOutputsFile = null;
        } else if (wdlOutputs.startsWith("file://")) {
            //cromwell current does not support options urls, remove file:// prefix
            wdlOutputsFile = wdlOutputs.replaceFirst("^file://", "");
        } else {
            wdlOutputsFile = tmpDir + "outputs.json";
            setupCommand.getArguments().addAll(writeStringToFile(wdlOutputs, wdlOutputsFile));
        }

        // job execute wdl
        String workflowIdPath = tmpDir + "workflow_id";
        Job runWdlWorkflow = newJob("run_wdl");
        runWdlWorkflow.setMaxMemory(jobMemory);
        runWdlWorkflow.addParent(setup);

        Command runWdlWorkflowCommand = runWdlWorkflow.getCommand();
        runWdlWorkflowCommand.addArgument(getRequiredProperty("setup_python3_environment_command") + ";");
        runWdlWorkflowCommand.addArgument(cromwellRunScript);
        runWdlWorkflowCommand.addArgument("--java-path");
        runWdlWorkflowCommand.addArgument(javaPath);
        runWdlWorkflowCommand.addArgument("--seqware-jar-path");
        runWdlWorkflowCommand.addArgument(seqwareDistributionJarPath);
        runWdlWorkflowCommand.addArgument("--niassa-host");
        runWdlWorkflowCommand.addArgument(ConfigTools.getSettingsValue(SqwKeys.SW_REST_URL));
        runWdlWorkflowCommand.addArgument("--workflow-run-swid");
        runWdlWorkflowCommand.addArgument("${WORKFLOW_RUN_ACCESSION}"); //exported at runtime
        runWdlWorkflowCommand.addArgument("--processing-swid");
        runWdlWorkflowCommand.addArgument("${PROCESSING_ACCESSION}"); //exported at runtime
        runWdlWorkflowCommand.addArgument("--cromwell-jar-path");
        runWdlWorkflowCommand.addArgument(cromwellJarPath);
        runWdlWorkflowCommand.addArgument("--cromwell-host");
        runWdlWorkflowCommand.addArgument(cromwellHost);
        runWdlWorkflowCommand.addArgument("--polling-interval");
        runWdlWorkflowCommand.addArgument(pollingInterval);
        runWdlWorkflowCommand.addArgument("--wdl-workflow");
        runWdlWorkflowCommand.addArgument(wdlWorkflowFile);
        runWdlWorkflowCommand.addArgument("--wdl-inputs");
        runWdlWorkflowCommand.addArgument(wdlInputsFile);
        runWdlWorkflowCommand.addArgument("--cromwell-workflow-id-path");
        runWdlWorkflowCommand.addArgument(workflowIdPath);
        if (wdlOptionsFile != null) {
            runWdlWorkflowCommand.addArgument("--wdl-options");
            runWdlWorkflowCommand.addArgument(wdlOptionsFile);
        }
        if (wdlDepsZip != null && !wdlDepsZip.isEmpty()) {
            runWdlWorkflowCommand.addArgument("--wdl-deps-zip");
            runWdlWorkflowCommand.addArgument(wdlDepsZip);
        }

        // provision out all output files produced by cromwell workflow
        Job provisionOut = newJob("provision_out");
        provisionOut.setMaxMemory(jobMemory);
        provisionOut.addParent(runWdlWorkflow);

        String outputPath;
        if (manualOutput) {
            outputPath = this.getMetadata_output_file_prefix() + getMetadata_output_dir() + "/";
        } else {
            // WORKFLOW_RUN_ACCESSION is exported and evaluated at runtime
            outputPath = this.getMetadata_output_file_prefix() + getMetadata_output_dir() + "/" + this.getName() + "_" + this.getVersion() + "/${WORKFLOW_RUN_ACCESSION}/";
        }

        Command provisionOutCommand = provisionOut.getCommand();
        provisionOutCommand.addArgument(getRequiredProperty("setup_python3_environment_command") + ";");
        provisionOutCommand.addArgument(provisionWdlOutputScript);
        provisionOutCommand.addArgument("--java-path");
        provisionOutCommand.addArgument(javaPath);
        provisionOutCommand.addArgument("--seqware-jar-path");
        provisionOutCommand.addArgument(seqwareDistributionJarPath);
        provisionOutCommand.addArgument("--workflow-run-swid");
        provisionOutCommand.addArgument("${WORKFLOW_RUN_ACCESSION}"); //exported and evaluated at runtime
        provisionOutCommand.addArgument("--processing-swid");
        provisionOutCommand.addArgument("${PROCESSING_ACCESSION}"); //exported and evaluated at runtime
        provisionOutCommand.addArgument("--cromwell-host");
        provisionOutCommand.addArgument(cromwellHost);
        provisionOutCommand.addArgument("--cromwell-workflow-id-path");
        provisionOutCommand.addArgument(workflowIdPath);
        provisionOutCommand.addArgument("--output-dir");
        provisionOutCommand.addArgument(outputPath);

        if(wdlOutputsFile != null) {
            provisionOutCommand.addArgument("--wdl-outputs-path");
            provisionOutCommand.addArgument(wdlOutputsFile);
        }
    }

    private List<String> writeStringToFile(String fileContents, String filePath) {
        return Arrays.asList(
                "cat << 'END_OF_FILE_CONTENTS' >",
                filePath + "\n" + fileContents + "\nEND_OF_FILE_CONTENTS\n");
    }

    private String getRequiredProperty(String key) {
        String value = getProperty(key);
        if (value.trim().isEmpty()) {
            logger.error("Property = [" + key + "] is empty.");
            setWorkflowInvalid();
        }
        return value;
    }
}
