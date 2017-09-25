package ca.on.oicr.pde.dao.executor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.model.WorkflowRun;

public interface SeqwareExecutor {

    public Workflow installWorkflow(File workflowPath) throws IOException;

    public void deciderRunSchedule(File deciderJar, Workflow workflow, List<String> studies, List<String> sequencerRuns, List<String> samples, String extraArgs) throws IOException;

    public WorkflowRun workflowRunSchedule(Workflow workflow, List<File> workflowIniFiles, Map<String, String> parameters) throws IOException;

    public void workflowRunLaunch(WorkflowRun workflowRun) throws IOException;

    public void workflowRunLaunch(File workflowBundle, List<File> workflowIniFiles, String workflowName, String workflowVersion) throws IOException;

    public void workflowRunUpdateStatus(WorkflowRun workflowRun) throws IOException;

    public String workflowRunStatus(WorkflowRun workflowRun) throws IOException;

    public String workflowRunReport(WorkflowRun workflowRun) throws IOException;

    public void cancelWorkflowRun(WorkflowRun workflowRun) throws IOException;

    public void cancelWorkflowRuns(Workflow workflow) throws IOException;

    public void deciderRunSchedule(String decider, Workflow workflow, String... deciderArgs) throws IOException;

}
