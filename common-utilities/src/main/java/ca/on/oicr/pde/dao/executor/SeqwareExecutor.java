package ca.on.oicr.pde.dao.executor;

import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SeqwareExecutor {

    public SeqwareAccession installWorkflow(File workflowPath) throws IOException;

    public void deciderRunSchedule(File deciderJar, Workflow workflow, List<String> studies, List<String> sequencerRuns, List<String> samples, String extraArgs) throws IOException;

    public SeqwareAccession workflowRunSchedule(SeqwareAccession workflowSwid, List<File> workflowIniFiles, Map<String, String> parameters) throws IOException;

    public void workflowRunLaunch(SeqwareAccession workflowRunSwid) throws IOException;

    public void workflowRunLaunch(File workflowBundle, List<File> workflowIniFiles, String workflowName, String workflowVersion) throws IOException;

    public void workflowRunUpdateStatus(SeqwareAccession workflowRunSwid) throws IOException;

    public String workflowRunReport(SeqwareAccession workflowRunSwid) throws IOException;

    public void cancelWorkflowRun(WorkflowRun wr) throws IOException;

    public void cancelWorkflowRuns(Workflow w) throws IOException;
    
    public void deciderRunSchedule(String decider, Workflow workflow, String... deciderArgs) throws IOException;

}