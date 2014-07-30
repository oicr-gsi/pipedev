package ca.on.oicr.pde.utilities;

import ca.on.oicr.pde.model.Sample;
import ca.on.oicr.pde.model.SequencerRun;
import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.model.Study;
import ca.on.oicr.pde.model.WorkflowRun;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SeqwareExecutor {

    public SeqwareAccession installWorkflow(File workflowPath) throws IOException;

    public void deciderRunSchedule(File deciderJar, SeqwareAccession workflowSwid, List<Study> studies, List<SequencerRun> sequencerRuns, List<Sample> samples, String extraArgs) throws IOException;

    public SeqwareAccession workflowRunSchedule(SeqwareAccession workflowSwid, File workflowIniFile, Map<String, String> parameters) throws IOException;

    public void workflowRunLaunch(SeqwareAccession workflowRunSwid) throws IOException;

    public void workflowRunLaunch(File workflowBundle, File workflowIniFile, String workflowName, String workflowVersion) throws IOException;

    public void workflowRunUpdateStatus(SeqwareAccession workflowRunSwid) throws IOException;

    public String workflowRunReport(SeqwareAccession workflowRunSwid) throws IOException;

    public void cancelWorkflowRun(WorkflowRun wr) throws IOException;

}
