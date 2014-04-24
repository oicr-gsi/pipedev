package ca.on.oicr.pde.dao;

import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import ca.on.oicr.pde.model.Accessionable;
import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
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

public abstract class SeqwareInterface {

    //HashMap<String, String> seqwareSettings;
    List<FileProvenanceReportRecord> fprs;
    Multimap<String, FileProvenanceReportRecord> swidToFpr;
    protected final Map<Workflow, List<WorkflowRunReportRecord>> wrrs;

    public SeqwareInterface() {
//        seqwareSettings = new HashMap<String, String>();
//        MapTools.ini2Map(seqwareSettingsFile.toString(), seqwareSettings, true);
        wrrs = new HashMap<Workflow, List<WorkflowRunReportRecord>>();

    }

    /**
     * Get a list ReducedFileProvenanceReportRecord (file records) from a collection of accessions.
     * Two notable operations occur in this method:
     * 1) All input accessions that uniquely (determined by file accession) reference a file will be converted directly into a ReducedFileProvenanceReportRecord.
     * 2) Any input accessions that reference the same file (determined by file accession) will be merged into one ReducedFileProvenanceReportRecord.
     * 
     * For example:
     * Case 1)
     *      Accession 1 --> File1 --> ReducedFile1
     *      Accession 2 --> File2 --> ReducedFile2
     * Case 2)
     *      Accession 3 --> File3 \ 
     *      Accession 4 --> File3 --> ReducedFile3
     *      Accession 5 --> File3 /
     * 
     * @param accessions a collection of objects that implement Accessionable
     * @return a list of ReducedFileProvenanceReportRecord
     */
    public List<ReducedFileProvenanceReportRecord> getFiles(Collection<? extends Accessionable> accessions) {

        List<FileProvenanceReportRecord> filesToBeProcessed = new ArrayList<FileProvenanceReportRecord>();
        for (Accessionable s : accessions) {
            filesToBeProcessed.addAll(swidToFpr.get(s.getSwid()));
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
        for(Entry<String, Collection<FileProvenanceReportRecord>> e : filesMap.asMap().entrySet()){
            files.add(new ReducedFileProvenanceReportRecord(e.getValue()));
        }

        return files;
    }

    public Set<String> getStudy(List<? extends Accessionable> swids) {
        Set studies = new HashSet<String>();
        //TODO: don't scan, lookup table...
        //TODO: retrieve only the records needed (don't get the whole file provenance report)
        for (FileProvenanceReportRecord fpr : fprs) {
            if (fpr.containsSeqwareAccession(swids)) {
                studies.add(fpr.getStudyTitle());
            }
        }
        return studies;
    }

    public Set<String> getSequencerRuns(List<? extends Accessionable> swids) {
        Set sequencerRuns = new HashSet<String>();
        //TODO: don't scan, lookup table...
        //TODO: retrieve only the records needed (don't get the whole file provenance report)
        for (FileProvenanceReportRecord fpr : fprs) {
            if (fpr.containsSeqwareAccession(swids)) {
                sequencerRuns.add(fpr.getSequencerRunName());
            }
        }
        return sequencerRuns;
    }

    public Set<String> getLanes(List<? extends Accessionable> swids) {
        Set lanes = new HashSet<String>();
        //TODO: don't scan, lookup table...
        //TODO: retrieve only the records needed (don't get the whole file provenance report)
        for (FileProvenanceReportRecord fpr : fprs) {
            if (fpr.containsSeqwareAccession(swids)) {
                lanes.add(fpr.getLaneName());
            }
        }
        return lanes;
    }

    public Set<String> getSamples(List<? extends Accessionable> swids) {
        Set samples = new HashSet<String>();
        //TODO: don't scan, lookup table...
        //TODO: retrieve only the records needed (don't get the whole file provenance report)
        for (FileProvenanceReportRecord fpr : fprs) {
            if (fpr.containsSeqwareAccession(swids)) {
                samples.add(fpr.getSampleName());
            }
        }
        return samples;
    }

    public Set<String> getProcessingAlgorithms(List<? extends Accessionable> swids) {
        Set processingAlgorithms = new HashSet<String>();
        //TODO: don't scan, lookup table...
        //TODO: retrieve only the records needed (don't get the whole file provenance report)
        for (FileProvenanceReportRecord fpr : fprs) {
            if (fpr.containsSeqwareAccession(swids)) {
                processingAlgorithms.add(fpr.getProcessingAlgorithm());
            }
        }
        return processingAlgorithms;
    }

    public Set<String> getFileMetaTypes(List<? extends Accessionable> swids) {
        Set fileTypes = new HashSet<String>();
        //TODO: don't scan, lookup table...
        //TODO: retrieve only the records needed (don't get the whole file provenance report)
        for (FileProvenanceReportRecord fpr : fprs) {
            if (fpr.containsSeqwareAccession(swids)) {
                fileTypes.add(fpr.getFileMetaType());
            }
        }
        return fileTypes;
    }

    public Set<String> getStudy(Accessionable swid) {
        Set studies = new HashSet<String>();
        //TODO: don't scan, lookup table...
        //TODO: retrieve only the records needed (don't get the whole file provenance report)
        for (FileProvenanceReportRecord fpr : fprs) {
            if (fpr.containsSeqwareAccession(swid)) {
                studies.add(fpr.getStudyTitle()); //TODO: get study object?
            }
        }
        return studies;
    }

    public Set<String> getWorkflows(List<? extends Accessionable> swids) {
        Set workflows = new HashSet<String>();
        for (FileProvenanceReportRecord fpr : fprs) {
            if (fpr.containsSeqwareAccession(swids)) {
                workflows.add(fpr.getWorkflowName()); //TODO: get study object?
            }
        }
        return workflows;
    }

    public List<WorkflowRunReportRecord> getWorkflowRunRecords(Workflow workflow) {
        if (!wrrs.containsKey(workflow)) {
            updateWorkflowRunRecords(workflow);
        }
        return new ArrayList<WorkflowRunReportRecord>(wrrs.get(workflow));
    }

    public List<Accessionable> getParentAccessions(WorkflowRun wr) {
        Map ini = getWorkflowRunIni(wr);
        StringTokenizer st = new StringTokenizer(ini.get("parent-accessions").toString(), ",");
        List<Accessionable> ps = new ArrayList<Accessionable>();
        while (st.hasMoreTokens()) {
            ps.add(new SeqwareAccession(st.nextToken()));
        }
        return ps;
    }

    public List<Accessionable> getInputFileAccessions(WorkflowRun wr) {
        return getWorkflowRunInputFiles(wr);
    }

    public abstract void update();

    protected abstract void updateFileProvenanceRecords();

    protected abstract void updateWorkflowRunRecords(Workflow workflow);

    public abstract Map<String, String> getWorkflowRunIni(WorkflowRun workflowRun);

    protected abstract List<Accessionable> getWorkflowRunInputFiles(WorkflowRun workflowRun);

}
