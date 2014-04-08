package ca.on.oicr.pde.testing.decider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class TestResult {

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
    private final List<WorkflowRunReport> workflowRuns;

    public TestResult() {
        studies = new HashSet<String>();
        sequencerRuns = new HashSet<String>();
        lanes = new HashSet<String>();
        samples = new HashSet<String>();
        workflows = new HashSet<String>();
        processingAlgorithms = new HashSet<String>();
        fileMetaTypes = new HashSet<String>();
        maxInputFiles = Integer.MIN_VALUE;
        minInputFiles = Integer.MAX_VALUE;
        
        workflowRuns = new ArrayList<WorkflowRunReport>();
    }

    public static TestResult buildFromJson(java.io.File jsonPath) throws IOException {

        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return m.readValue(jsonPath, TestResult.class);

    }

    public static TestResult buildFromJson(String jsonPath) throws IOException {

        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return m.readValue(jsonPath, TestResult.class);

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

    public List<WorkflowRunReport> getWorkflowRuns() {
        Collections.sort(workflowRuns, new Comparator<WorkflowRunReport>() {

            @Override
            public int compare(WorkflowRunReport o1, WorkflowRunReport o2) {
               return o1.toString().compareTo(o2.toString());
            }
        });
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
    }

    public void validate() {
        //TODO: Check workflow run input file info against header
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
