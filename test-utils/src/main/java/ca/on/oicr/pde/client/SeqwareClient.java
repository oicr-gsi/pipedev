package ca.on.oicr.pde.client;

import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.seqware.common.model.FirstTierModel;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Processing;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.model.WorkflowRun;
import net.sourceforge.seqware.common.module.FileMetadata;
import org.joda.time.DateTime;

/**
 *
 * @author mlaszloffy
 */
public interface SeqwareClient {

    public <T extends FirstTierModel> WorkflowRun createWorkflowRun(Workflow workflow, Set<IUS> limsKeys, Collection<T> parents, Set<Processing> processings);
    
    public <T extends FirstTierModel> WorkflowRun createWorkflowRun(Workflow workflow, Set<IUS> limsKeys, Collection<T> parents, List<FileMetadata> files);

    public Workflow createWorkflow(String name, String version, String description);

    public Workflow createWorkflow(String name, String version, String description, Map<String, String> defaultParameters);

    public Map<String, String> getWorkflowRunIni(WorkflowRun workflowRun);

    public List<Integer> getWorkflowRunInputFiles(WorkflowRun workflowRun);

    public List<Integer> getParentAccession(WorkflowRun workflowRun);

    public List<WorkflowRunReportRecord> getWorkflowRunRecords(Workflow workflow);
    
    public IUS addLims(String provider, String id, String version, DateTime lastModified);

}
