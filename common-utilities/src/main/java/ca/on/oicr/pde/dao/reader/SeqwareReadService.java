package ca.on.oicr.pde.dao.reader;

import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import ca.on.oicr.pde.model.Accessionable;
import ca.on.oicr.pde.model.Experiment;
import ca.on.oicr.pde.model.File;
import ca.on.oicr.pde.model.Ius;
import ca.on.oicr.pde.model.Lane;
import ca.on.oicr.pde.model.Processing;
import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.model.Sample;
import ca.on.oicr.pde.model.SequencerRun;
import ca.on.oicr.pde.model.Study;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * An interface to a seqware service that uses basic Seqware webservice based reports to build a local in-memory Seqware metadata
 * representation.
 * <p>
 * Currently the service makes use of the Seqware
 * <a href="http://seqware.github.io/docs/webservice-api/metadata/report/fileprovenance_get/">file provenance repoort</a> and the
 * <a href="http://seqware.github.io/docs/webservice-api/metadata/report/workflowrun_get/">workflow run report</a>.
 */
public abstract class SeqwareReadService {

    /**
     * The local file provenance report records.
     *
     * @see FileProvenanceReportRecord
     */
    protected List<FileProvenanceReportRecord> fileProvenanceReportRecords;

    /**
     * The lookup map of file provenance report key(s) to file provenance report record object.
     */
    protected final Multimap<String, FileProvenanceReportRecord> accessionToFileProvenanceReportRecords;

    /**
     * The lookup map for Workflow object to workflow run report record objects.
     *
     * @see WorkflowRunReportRecord
     */
    protected final Map<Workflow, List<WorkflowRunReportRecord>> workflowToWorkflowRunReportRecords;

    /**
     * Setup common
     */
    protected SeqwareReadService() {
        workflowToWorkflowRunReportRecords = new HashMap<Workflow, List<WorkflowRunReportRecord>>();
        accessionToFileProvenanceReportRecords = LinkedListMultimap.create();
    }

    /**
     * Get a list ReducedFileProvenanceReportRecord (file records) from a collection of accessions.
     *
     * Two notable operations occur in this method:
     * <ol>
     * <li>All input accessions that uniquely (determined by file accession) reference a file will be converted directly into a
     * ReducedFileProvenanceReportRecord.
     * <li>Any input accessions that reference the same file (determined by file accession) will be merged into one
     * ReducedFileProvenanceReportRecord.
     * </ol>
     * <p>
     * For example:
     * <pre>
     * Case 1:
     * Accession 1 --> File1 --> ReducedFile1
     * Accession 2 --> File2 --> ReducedFile2
     *
     * Case 2:
     * Accession 3 --> File3 \
     * Accession 4 --> File3 --> ReducedFile3
     * Accession 5 --> File3 /
     * </pre>
     *
     * @param accessions a collection of objects that implement Accessionable
     * @return A list of ReducedFileProvenanceReportRecord
     */
    public List<ReducedFileProvenanceReportRecord> getFiles(Collection<? extends Accessionable> accessions) {

        List<FileProvenanceReportRecord> filesToBeProcessed = new ArrayList<FileProvenanceReportRecord>();
        for (Accessionable s : accessions) {
            filesToBeProcessed.addAll(accessionToFileProvenanceReportRecords.get(s.getSwid()));
        }

        //Parition filesToBeProcessed by file swid
        ImmutableListMultimap<String, FileProvenanceReportRecord> filesMap = Multimaps.index(filesToBeProcessed, new Function<FileProvenanceReportRecord, String>() {
            @Override
            public String apply(FileProvenanceReportRecord f) {
                return f.getFileSwid();
            }
        });

        //Convert set of FileProvenanceReportRecord to ReducedFileProvenanceReportRecord
        //If the accession only has one file, transform it into one ReducedFileProvenanceReportRecord
        //If the accession has multiple files, merge them all into one ReducedFileProvenanceReportRecord
        List<ReducedFileProvenanceReportRecord> files = new ArrayList<ReducedFileProvenanceReportRecord>();
        for (Entry<String, Collection<FileProvenanceReportRecord>> e : filesMap.asMap().entrySet()) {
            files.add(new ReducedFileProvenanceReportRecord(e.getValue()));
        }

        return files;
    }

    /**
     * Get all studies that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     * @return A set of study names
     */
    public Set<String> getStudy(List<? extends Accessionable> swids) {
        Set studies = new HashSet<String>();
        for (Accessionable swid : swids) {
            for (FileProvenanceReportRecord fpr : accessionToFileProvenanceReportRecords.get(swid.getSwid())) {
                studies.add(fpr.getStudyTitle());
            }
        }
        return studies;
    }

    /**
     * Get all sequencer runs that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     * @return A set of sequencer run names
     */
    public Set<String> getSequencerRuns(List<? extends Accessionable> swids) {
        Set sequencerRuns = new HashSet<String>();
        for (Accessionable swid : swids) {
            for (FileProvenanceReportRecord fpr : accessionToFileProvenanceReportRecords.get(swid.getSwid())) {
                sequencerRuns.add(fpr.getSequencerRunName());
            }
        }
        return sequencerRuns;
    }

    /**
     * Get all lanes that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     * @return A set of lanes names
     */
    public Set<String> getLanes(List<? extends Accessionable> swids) {
        Set lanes = new HashSet<String>();
        for (Accessionable swid : swids) {
            for (FileProvenanceReportRecord fpr : accessionToFileProvenanceReportRecords.get(swid.getSwid())) {
                lanes.add(fpr.getLaneName());
            }
        }
        return lanes;
    }

    /**
     * Get all samples that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     * @return A set of sample names
     */
    public Set<String> getSamples(List<? extends Accessionable> swids) {
        Set samples = new HashSet<String>();
        for (Accessionable swid : swids) {
            for (FileProvenanceReportRecord fpr : accessionToFileProvenanceReportRecords.get(swid.getSwid())) {
                samples.add(fpr.getSampleName());
            }
        }
        return samples;
    }

    /**
     * Get all processing algorithms that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     * @return A set of processing algorithm names
     */
    public Set<String> getProcessingAlgorithms(List<? extends Accessionable> swids) {
        Set processingAlgorithms = new HashSet<String>();
        for (Accessionable swid : swids) {
            for (FileProvenanceReportRecord fpr : accessionToFileProvenanceReportRecords.get(swid.getSwid())) {
                processingAlgorithms.add(fpr.getProcessingAlgorithm());
            }
        }
        return processingAlgorithms;
    }

    /**
     * Get all file metatypes that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     * @return A list of file metatype names
     */
    public Set<String> getFileMetaTypes(List<? extends Accessionable> swids) {
        Set fileTypes = new HashSet<String>();
        for (Accessionable swid : swids) {
            for (FileProvenanceReportRecord fpr : accessionToFileProvenanceReportRecords.get(swid.getSwid())) {
                fileTypes.add(fpr.getFileMetaType());
            }
        }
        return fileTypes;
    }

    /**
     * Get all workflows that have some relation to the list of accessions provided.
     *
     * @param swids list of seqware accessions
     * @return A set of workflow names
     */
    public Set<String> getWorkflows(List<? extends Accessionable> swids) {
        Set workflows = new HashSet<String>();
        for (Accessionable swid : swids) {
            for (FileProvenanceReportRecord fpr : accessionToFileProvenanceReportRecords.get(swid.getSwid())) {
                workflows.add(fpr.getWorkflowName());
            }
        }
        return workflows;
    }

    /**
     * Get all workflow run records that are associated with the specified workflow.
     *
     * @param workflow the search workflow object
     * @return A list of workflow run report records
     */
    public List<WorkflowRunReportRecord> getWorkflowRunRecords(Workflow workflow) {
        if (!workflowToWorkflowRunReportRecords.containsKey(workflow)) {
            updateWorkflowRunRecords(workflow);
        }
        return new ArrayList<WorkflowRunReportRecord>(workflowToWorkflowRunReportRecords.get(workflow));
    }

    /**
     * Get all parent accessions for the specified workflow run.
     *
     * @param wr the search workflow run object
     * @return A list of accessionable objects
     */
    public List<Accessionable> getParentAccessions(WorkflowRun wr) {
        Map ini = getWorkflowRunIni(wr);
        StringTokenizer st = new StringTokenizer(ini.get("parent-accessions").toString(), ",");
        List<Accessionable> ps = new ArrayList<Accessionable>();
        while (st.hasMoreTokens()) {
            ps.add(new SeqwareAccession(st.nextToken()));
        }
        return ps;
    }

    /**
     * Get all input file accessions for the specified workflow run (using getWorkflowRunInputFiles).
     *
     * @param wr the search workflow run object
     * @return A list of accessionable objects
     * @see #getWorkflowRunInputFiles(ca.on.oicr.pde.model.WorkflowRun)
     */
    public List<Accessionable> getInputFileAccessions(WorkflowRun wr) {
        return getWorkflowRunInputFiles(wr);
    }

    /**
     * Updates the local Seqware metadate repesentation.
     *
     * Should be called after a major metadata change or to populate the local data store.
     */
    public final void update() {

        Experiment.clearCache();
        File.clearCache();
        Ius.clearCache();
        Lane.clearCache();
        Processing.clearCache();
        Sample.clearCache();
        SequencerRun.clearCache();
        Study.clearCache();
        Workflow.clearCache();
        WorkflowRun.clearCache();

        updateFileProvenanceRecords();
        //TODO: updateWorkflowRunRecords();
        
    }

    /**
     * Update the local file provenance data.
     */
    public abstract void updateFileProvenanceRecords();

    /**
     * Update the local workflow run report data for the file specified workflow.
     *
     * @param workflow a workflow object
     */
    public abstract void updateWorkflowRunRecords(Workflow workflow);

    /**
     * Get the Seqware workflow run ini for the specified workflow run.
     *
     * @param workflowRun a workflow run object
     * @return Seqware workflow run properties
     */
    public abstract Map<String, String> getWorkflowRunIni(WorkflowRun workflowRun);

    /**
     * Get all input files for the workflow run.
     *
     * @param workflowRun the search workflow run object
     * @return A list of file objects
     */
    protected abstract List<Accessionable> getWorkflowRunInputFiles(WorkflowRun workflowRun);

}
