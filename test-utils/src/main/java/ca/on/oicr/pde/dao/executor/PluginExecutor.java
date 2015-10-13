package ca.on.oicr.pde.dao.executor;

import ca.on.oicr.pde.experimental.PDEPluginRunner;
import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.pipeline.runner.PluginRunner;

/**
 *
 * @author mlaszloffy
 */
public class PluginExecutor implements SeqwareExecutor {

    private final PDEPluginRunner runner;

    public PluginExecutor(Map config) {
        runner = new PDEPluginRunner(config);
    }

    @Override
    public SeqwareAccession installWorkflow(File bundledWorkflowPath) throws IOException {

        List<String> params = new ArrayList<>();
        params.add("--plugin");
        params.add("net.sourceforge.seqware.pipeline.plugins.BundleManager");
        params.add("--");
        params.add("--bundle");
        params.add(bundledWorkflowPath.getAbsolutePath());
        params.add("--install-dir-only");

        ReturnValue rv = runner.runPlugin(params);
        if (rv.getExitStatus() != ReturnValue.SUCCESS) {
            throw new RuntimeException("Failed to install bundled workflow");
        }

        return new SeqwareAccession(rv.getAttribute("sw_accession"));

    }

    @Override
    public void deciderRunSchedule(String decider, Workflow workflow, String... deciderArgs) throws IOException {

        List<String> params = new ArrayList<>();
        params.add("--plugin");
        params.add(decider);
        params.add("--");
        params.add("--wf-accession");
        params.add(workflow.getSwid());
        for (String args : deciderArgs) {
            //no spaces in args allowed
            params.addAll(Arrays.asList(args.split(" ")));
        }

        //pre-seqware 1.1, --schedule was required
        if (!PluginRunner.class.getPackage().getImplementationVersion().startsWith("1.1")) {
            throw new RuntimeException("Only Seqware 1.1 is supported");
        }

        ReturnValue rv = runner.runPlugin(params);

    }

    @Override
    public SeqwareAccession workflowRunSchedule(SeqwareAccession workflowSwid, List<File> workflowIniFiles,
            Map<String, String> parameters) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void workflowRunLaunch(SeqwareAccession workflowRunSwid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void workflowRunLaunch(File workflowBundle, List<File> workflowIniFiles, String workflowName,
            String workflowVersion) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void workflowRunUpdateStatus(SeqwareAccession workflowRunSwid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String workflowRunReport(SeqwareAccession workflowRunSwid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cancelWorkflowRun(WorkflowRun wr) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public void deciderRunSchedule(File deciderJar, Workflow workflowSwid, List<String> studies, List<String> sequencerRuns, List<String> samples, String extraArgs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
