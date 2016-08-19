package ca.on.oicr.pde.dao.executor;

import ca.on.oicr.pde.experimental.PDEPluginRunner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.model.Workflow;
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
    public Workflow installWorkflow(File bundledWorkflowPath) throws IOException {

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

        Workflow workflow = new Workflow();
        workflow.setSwAccession(Integer.parseInt(rv.getAttribute("sw_accession")));
        return workflow;
    }

    @Override
    public void deciderRunSchedule(String decider, Workflow workflow, String... deciderArgs) throws IOException {

        List<String> params = new ArrayList<>();
        params.add("--plugin");
        params.add(decider);
        params.add("--");
        params.add("--wf-accession");
        params.add(workflow.getSwAccession().toString());
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
    public void deciderRunSchedule(File deciderJar, net.sourceforge.seqware.common.model.Workflow workflow, List<String> studies, List<String> sequencerRuns, List<String> samples, String extraArgs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public net.sourceforge.seqware.common.model.WorkflowRun workflowRunSchedule(net.sourceforge.seqware.common.model.Workflow workflow, List<File> workflowIniFiles, Map<String, String> parameters) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void workflowRunLaunch(net.sourceforge.seqware.common.model.WorkflowRun workflowRun) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void workflowRunLaunch(File workflowBundle, List<File> workflowIniFiles, String workflowName, String workflowVersion) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void workflowRunUpdateStatus(net.sourceforge.seqware.common.model.WorkflowRun workflowRun) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String workflowRunReport(net.sourceforge.seqware.common.model.WorkflowRun workflowRun) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cancelWorkflowRun(net.sourceforge.seqware.common.model.WorkflowRun workflowRun) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cancelWorkflowRuns(net.sourceforge.seqware.common.model.Workflow workflow) throws IOException {
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

}
