package ca.on.oicr.pde.dao;

import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import ca.on.oicr.pde.model.Accessionable;
import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    public List<ReducedFileProvenanceReportRecord> getFiles(List<? extends Accessionable> swids) {

        List files = new ArrayList<ReducedFileProvenanceReportRecord>();

        for (Accessionable s : swids) {
            for (FileProvenanceReportRecord f : swidToFpr.get(s.getSwid())) {
                files.add(f.getSimpleFileProvenanceReportRecord());
            }
        }

        Collections.sort(files);
        
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
