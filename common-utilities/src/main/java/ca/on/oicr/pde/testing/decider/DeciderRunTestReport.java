package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.dao.reader.SeqwareReadService;
import ca.on.oicr.pde.model.Accessionable;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DeciderRunTestReport {

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

    public DeciderRunTestReport() {
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

    public static DeciderRunTestReport buildFromJson(File jsonPath) throws IOException {

        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return m.readValue(jsonPath, DeciderRunTestReport.class);

    }

    public static DeciderRunTestReport buildFromJson(String jsonPath) throws IOException {

        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return m.readValue(jsonPath, DeciderRunTestReport.class);

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

    public void addStudies(Collection studies) {
        this.studies.addAll(studies);
    }

    public void addSequencerRuns(Collection sequencerRuns) {
        this.sequencerRuns.addAll(sequencerRuns);
    }

    public void addLanes(Collection lanes) {
        this.lanes.addAll(lanes);
    }

    public void addSamples(Collection samples) {
        this.samples.addAll(samples);
    }

    public void addWorkflows(Collection workflows) {
        this.workflows.addAll(workflows);
    }

    public void addProcessingAlgorithms(Collection processingAlgorithms) {
        this.processingAlgorithms.addAll(processingAlgorithms);
    }

    public void addFileMetaTypes(Collection fileMetaTypes) {
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

    public static String diffHeader(DeciderRunTestReport actual, DeciderRunTestReport expected) {

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

    public static DeciderRunTestReport generateReport(SeqwareReadService srs, Workflow workflow, Collection<String> iniExclusions,
            Map<String, String> iniSubstitutions) {
        List<WorkflowRunReportRecord> wrrs = srs.getWorkflowRunRecords(workflow);

        DeciderRunTestReport t = new DeciderRunTestReport();
        t.setWorkflowRunCount(wrrs.size());

        for (WorkflowRunReportRecord wrr : wrrs) {

            //TODO: get workflow run object from workflow run report record
            WorkflowRun.Builder workflowRunBuilder = new WorkflowRun.Builder();
            workflowRunBuilder.setSwid(wrr.getWorkflowRunSwid());
            WorkflowRun wr = workflowRunBuilder.build();

            //Get the workflow run's parent accession(s) (processing accession(s))
            List<Accessionable> parentAccessions = srs.getParentAccessions(wr);

            //Get the workflow run's input file(s) (file accession(s))
            List<Accessionable> inputFileAccessions = srs.getInputFileAccessions(wr);

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
            Map<String, String> ini = srs.getWorkflowRunIni(wr);

            //apply ini exclusions
            for (String s : iniExclusions) {
                ini.remove(s);
            }

            //apply ini substitutions
            for (Entry<String, String> iniEntry : ini.entrySet()) {
                for (Entry<String, String> substitutionEntry : iniSubstitutions.entrySet()) {
                    String modifiedValue = iniEntry.getValue().replace(substitutionEntry.getKey(), substitutionEntry.getValue());
                    iniEntry.setValue(modifiedValue);
                }
            }

            WorkflowRunReport x = new WorkflowRunReport();
            x.setWorkflowIni(ini);
            x.setFiles(files);

            t.addWorkflowRun(x);

        }

        return t;
    }
}
