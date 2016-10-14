package ca.on.oicr.pde.experimental;

import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.gsi.provenance.ProvenanceClient;
import ca.on.oicr.gsi.provenance.FileProvenanceFilter;
import ca.on.oicr.pde.reports.WorkflowReport;
import ca.on.oicr.pde.reports.WorkflowRunReport;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.model.WorkflowRun;
import ca.on.oicr.pde.client.SeqwareClient;

/**
 *
 * @author mlaszloffy
 */
public class MetadataClient {

    private final ProvenanceClient provenanceClient;
    private final SeqwareClient seqwareClient;

    public MetadataClient(ProvenanceClient provenanceClient, SeqwareClient seqwareClient) {
        this.provenanceClient = provenanceClient;
        this.seqwareClient = seqwareClient;
    }

    public WorkflowReport getWorkflowReport(Workflow workflow) {
        Map<FileProvenanceFilter, Set<String>> filters = new HashMap<>();
        filters.put(FileProvenanceFilter.workflow, Sets.newHashSet(workflow.getSwAccession().toString()));

        Collection<FileProvenance> fps = provenanceClient.getFileProvenance(filters);
        WorkflowReport t = new WorkflowReport();
        t.setWorkflowRunCount(fps.size()); //wrrs.size());

        for (FileProvenance fp : fps) {

            //Get the workflow run's input file(s)
            Set<String> inputFileAccessions = new HashSet<>();
            for (Integer i : fp.getWorkflowRunInputFileSWIDs()) {
                inputFileAccessions.add(i.toString());
            }
            Map<FileProvenanceFilter, Set<String>> filters2 = new HashMap<>();
            filters2.put(FileProvenanceFilter.file, inputFileAccessions);
            Collection<FileProvenance> inputFiles = provenanceClient.getFileProvenance(filters2);

            t.addStudies(fp.getStudyTitles());
            t.addSamples(fp.getSampleNames());
            t.addSequencerRuns(fp.getSequencerRunNames());
            t.addLanes(fp.getLaneNames());
            for (FileProvenance inputFile : inputFiles) {
                if (inputFile.getWorkflowName() != null) {
                    t.addWorkflows(Arrays.asList(inputFile.getWorkflowName()));
                }
                if (inputFile.getProcessingAlgorithm() != null) {
                    t.addProcessingAlgorithms(Arrays.asList(inputFile.getProcessingAlgorithm()));
                }
                if (inputFile.getFileMetaType() != null) {
                    t.addFileMetaTypes(Arrays.asList(inputFile.getFileMetaType()));
                }
            }

            if (inputFileAccessions.size() > t.getMaxInputFiles()) {
                t.setMaxInputFiles(inputFileAccessions.size());
            }

            if (inputFileAccessions.size() < t.getMinInputFiles()) {
                t.setMinInputFiles(inputFileAccessions.size());
            }

            t.setTotalInputFiles(t.getTotalInputFiles() + inputFileAccessions.size());

            //get the ini that the decider scheduled
            WorkflowRun workflowRun = new WorkflowRun();
            workflowRun.setSwAccession(fp.getWorkflowRunSWID());
            Map<String, String> ini = seqwareClient.getWorkflowRunIni(workflowRun);

            WorkflowRunReport workflowRunReport = new WorkflowRunReport();
            workflowRunReport.setWorkflowIni(ini);
            //x.setFiles(files);

            t.addWorkflowRun(workflowRunReport);
        }

        return t;
    }

}
