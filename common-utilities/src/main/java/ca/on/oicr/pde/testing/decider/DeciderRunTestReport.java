package ca.on.oicr.pde.testing.decider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    private List<WorkflowRunReport> workflowRuns;

    public DeciderRunTestReport() {
        studies = new TreeSet<String>();
        sequencerRuns = new TreeSet<String>();
        lanes = new TreeSet<String>();
        samples = new TreeSet<String>();
        workflows = new TreeSet<String>();
        processingAlgorithms = new TreeSet<String>();
        fileMetaTypes = new TreeSet<String>();
        maxInputFiles = Integer.MIN_VALUE;
        minInputFiles = Integer.MAX_VALUE;

        workflowRuns = new ArrayList<WorkflowRunReport>();
    }

    public static DeciderRunTestReport buildFromJson(java.io.File jsonPath) throws IOException {

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

    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(this);
    }

}
