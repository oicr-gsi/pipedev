package ca.on.oicr.pde.reports;

import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.gsi.provenance.ProvenanceClient;
import ca.on.oicr.pde.dao.reader.FileProvenanceClient;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.seqware.common.model.FileProvenanceParam;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.model.WorkflowRun;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ca.on.oicr.pde.client.SeqwareClient;

public class WorkflowReport {

    private Integer workflowRunCount;
    private final Set<String> studies;
    private final Set<String> sequencerRuns;
    private final Set<String> lanes;
    private final Set<String> samples;
    private final Set<String> workflows;
    private final Set<String> processingAlgorithms;
    private final Set<String> fileMetaTypes;
    private Integer maxInputFiles;
    private Integer minInputFiles;
    private Integer totalInputFiles;
    private List<WorkflowRunReport> workflowRuns;

    public WorkflowReport() {
        studies = new TreeSet<>();
        sequencerRuns = new TreeSet<>();
        lanes = new TreeSet<>();
        samples = new TreeSet<>();
        workflows = new TreeSet<>();
        processingAlgorithms = new TreeSet<>();
        fileMetaTypes = new TreeSet<>();
        maxInputFiles = Integer.MIN_VALUE;
        minInputFiles = Integer.MAX_VALUE;
        totalInputFiles = Integer.valueOf("0");

        workflowRuns = new ArrayList<>();
    }

    public static WorkflowReport buildFromJson(File jsonPath) throws IOException {

        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return m.readValue(jsonPath, WorkflowReport.class);

    }

    public static WorkflowReport buildFromJson(String jsonPath) throws IOException {

        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return m.readValue(jsonPath, WorkflowReport.class);

    }

    public Set<String> getStudies() {
        return studies;
    }

    public Set<String> getSequencerRuns() {
        return sequencerRuns;
    }

    public Set<String> getLanes() {
        return lanes;
    }

    public Set<String> getSamples() {
        return samples;
    }

    public Set<String> getWorkflows() {
        return workflows;
    }

    public Set<String> getProcessingAlgorithms() {
        return processingAlgorithms;
    }

    public Set<String> getFileMetaTypes() {
        return fileMetaTypes;
    }

    public Integer getMaxInputFiles() {
        return maxInputFiles;
    }

    public void setMaxInputFiles(Integer maxInputFiles) {
        this.maxInputFiles = maxInputFiles;
    }

    public Integer getMinInputFiles() {
        return minInputFiles;
    }

    public void setMinInputFiles(Integer minInputFiles) {
        this.minInputFiles = minInputFiles;
    }

    public Integer getTotalInputFiles() {
        return totalInputFiles;
    }

    public void setTotalInputFiles(Integer totalInputFiles) {
        this.totalInputFiles = totalInputFiles;
    }

    public void setWorkflowRunCount(Integer workflowRunCount) {
        this.workflowRunCount = workflowRunCount;
    }

    public Integer getWorkflowRunCount() {
        return workflowRunCount;
    }

    public void setWorkflowRuns(List<WorkflowRunReport> workflowRuns) {
        Collections.sort(workflowRuns);
        this.workflowRuns = workflowRuns;
    }

    public List<WorkflowRunReport> getWorkflowRuns() {
        return workflowRuns;
    }

    public void addStudies(Collection<String> studies) {
        this.studies.addAll(studies);
    }

    public void addSequencerRuns(Collection<String> sequencerRuns) {
        this.sequencerRuns.addAll(sequencerRuns);
    }

    public void addLanes(Collection<String> lanes) {
        this.lanes.addAll(lanes);
    }

    public void addSamples(Collection<String> samples) {
        this.samples.addAll(samples);
    }

    public void addWorkflows(Collection<String> workflows) {
        this.workflows.addAll(workflows);
    }

    public void addProcessingAlgorithms(Collection<String> processingAlgorithms) {
        this.processingAlgorithms.addAll(processingAlgorithms);
    }

    public void addFileMetaTypes(Collection<String> fileMetaTypes) {
        this.fileMetaTypes.addAll(fileMetaTypes);
    }

    public void addWorkflowRun(WorkflowRunReport s) {
        this.workflowRuns.add(s);
        Collections.sort(workflowRuns);
    }

    public void validate() {
        //TODO: Check workflow run input file info against header
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public static String diffHeader(WorkflowReport actual, WorkflowReport expected) {

        StringBuilder sb = new StringBuilder();

        if (expected.getWorkflowRunCount().intValue() != actual.getWorkflowRunCount().intValue()) {
            sb.append(String.format("Workflow run count changed from %s to %s%n", expected.getWorkflowRunCount(), actual.getWorkflowRunCount()));
        }
        if (expected.studies.size() != actual.studies.size()) {
            sb.append(String.format("Study set size changed from %s to %s%n", expected.studies.size(), actual.studies.size()));
        }
        if (expected.sequencerRuns.size() != actual.sequencerRuns.size()) {
            sb.append(String.format("Sequencer run set size changed from %s to %s%n", expected.sequencerRuns.size(), actual.sequencerRuns.size()));
        }
        if (expected.lanes.size() != actual.lanes.size()) {
            sb.append(String.format("Lane set size changed from %s to %s%n", expected.lanes.size(), actual.lanes.size()));
        }
        if (expected.samples.size() != actual.samples.size()) {
            sb.append(String.format("Sample set size changed from %s to %s%n", expected.samples.size(), actual.samples.size()));
        }
        if (expected.workflows.size() != actual.workflows.size()) {
            sb.append(String.format("Workflow set size changed from %s to %s%n", expected.workflows.size(), actual.workflows.size()));
        }
        if (expected.processingAlgorithms.size() != actual.processingAlgorithms.size()) {
            sb.append(String.format("Processing algorithm set size changed from %s to %s%n", expected.processingAlgorithms.size(), actual.processingAlgorithms.size()));
        }
        if (expected.fileMetaTypes.size() != actual.fileMetaTypes.size()) {
            sb.append(String.format("File metatype set size changed from %s to %s%n", expected.fileMetaTypes.size(), actual.fileMetaTypes.size()));
        }
        if (expected.maxInputFiles.intValue() != actual.maxInputFiles.intValue()) {
            sb.append(String.format("Max input files changed from %s to %s%n", expected.maxInputFiles, actual.maxInputFiles));
        }
        if (expected.minInputFiles.intValue() != actual.minInputFiles.intValue()) {
            sb.append(String.format("Min input files changed from %s to %s%n", expected.minInputFiles, actual.minInputFiles));
        }
        if (expected.totalInputFiles.intValue() != actual.totalInputFiles.intValue()) {
            sb.append(String.format("Total input file count changed from %s to %s%n", expected.totalInputFiles, actual.totalInputFiles));
        }

        //Remove last new line character
        if (sb.length() != 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(this);
    }

    public void applyIniExclusions(Collection<String> iniExclusions) {
        for (WorkflowRunReport wrr : getWorkflowRuns()) {
            Map<String, String> ini = wrr.getWorkflowIni();
            for (String s : iniExclusions) {
                ini.remove(s);
            }
        }
    }

    public void applyIniStringSubstitution(String searchString, String replacementString) {
        for (WorkflowRunReport wrr : getWorkflowRuns()) {
            Map<String, String> ini = wrr.getWorkflowIni();
            for (Entry<String, String> iniEntry : ini.entrySet()) {
                String modifiedValue = iniEntry.getValue().replaceAll(searchString, replacementString);
                iniEntry.setValue(modifiedValue);
            }
        }
    }

    public void applyIniStringSubstitutions(Map<String, String> iniStringSubstitutions) {
        for (Entry<String, String> substitutionEntry : iniStringSubstitutions.entrySet()) {
            String searchString = substitutionEntry.getKey();
            String replacementString = substitutionEntry.getValue();
            applyIniStringSubstitution(searchString, replacementString);
        }
    }

    public void applyIniSubstitutions(Map<String, String> iniSubstitutions) {
        for (WorkflowRunReport wrr : getWorkflowRuns()) {
            Map<String, String> ini = wrr.getWorkflowIni();
            for (Entry<String, String> substitutionEntry : iniSubstitutions.entrySet()) {
                if (ini.containsKey(substitutionEntry.getKey())) {
                    ini.put(substitutionEntry.getKey(), substitutionEntry.getValue());
                }
            }
        }
    }

    public static WorkflowReport generateReport(SeqwareClient seqwareClient, ProvenanceClient provenanceClient, Workflow workflow) {
        List<WorkflowRunReportRecord> wrrs = seqwareClient.getWorkflowRunRecords(workflow);

        WorkflowReport t = new WorkflowReport();
        t.setWorkflowRunCount(wrrs.size());
        
                Map<String, Set<String>> filters = new HashMap<>();
        filters.put(FileProvenanceParam.workflow.toString(), Sets.newHashSet(workflow.getSwAccession().toString()));

        Collection<FileProvenance> fps = provenanceClient.getFileProvenance(filters);
        
        FileProvenanceClient srs = new FileProvenanceClient(Lists.newArrayList(fps));

        for (WorkflowRunReportRecord wrr : wrrs) {

            //TODO: get workflow run object from workflow run report record
            WorkflowRun workflowRun = new WorkflowRun();
            workflowRun.setSwAccession(Integer.parseInt(wrr.getWorkflowRunSwid()));

            //Get the workflow run's parent accession(s) (processing accession(s))
            List<Integer> parentAccessions = seqwareClient.getParentAccession(workflowRun);

            //Get the workflow run's input file(s) (file accession(s))
            List<Integer> inputFileAccessions = seqwareClient.getWorkflowRunInputFiles(workflowRun);

            t.addStudies(srs.getStudy(parentAccessions));
            t.addSamples(srs.getSamples(parentAccessions));
            t.addSequencerRuns(srs.getSequencerRuns(parentAccessions));
            t.addLanes(srs.getLanes(parentAccessions));
            t.addWorkflows(srs.getWorkflows(inputFileAccessions));
            t.addProcessingAlgorithms(srs.getProcessingAlgorithms(inputFileAccessions));
            t.addFileMetaTypes(srs.getFileMetaTypes(inputFileAccessions));

            List<ReducedFileProvenanceReportRecord> files = srs.getFiles(inputFileAccessions);
            if (files.size() > t.getMaxInputFiles()) {
                t.setMaxInputFiles(files.size());
            }

            if (files.size() < t.getMinInputFiles()) {
                t.setMinInputFiles(files.size());
            }

            t.setTotalInputFiles(t.getTotalInputFiles() + files.size());

            //get the ini that the decider scheduled
            Map<String, String> ini = seqwareClient.getWorkflowRunIni(workflowRun);

            WorkflowRunReport x = new WorkflowRunReport();
            x.setWorkflowIni(ini);
            x.setFiles(files);

            t.addWorkflowRun(x);

        }

        return t;
    }
}
