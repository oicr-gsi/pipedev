package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class TestResult {

    Integer workflowRunCount;
    Set<String> studies;
    Set<String> sequencerRuns;
    Set<String> lanes;
    Set<String> samples;
    Set<String> workflows;
    Set<String> processingAlgorithms;
    Set<String> fileMetaTypes;
    Integer maxInputFiles;
    Integer minInputFiles;
    Collection<WorkflowRun> workflowRuns;

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
        
        workflowRuns = new ArrayList<WorkflowRun>();
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

    public Collection<WorkflowRun> getWorkflowRuns() {
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

    public void addWorkflowRun(WorkflowRun s) {
        this.workflowRuns.add(s);
    }

    public void validate() {
        //TODO: Check workflow run input file info against header
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static class WorkflowRun {
        
        Map workflowIni = new HashMap<String, String>();
        Collection<ReducedFileProvenanceReportRecord> files;

        public Map getWorkflowIni() {
            return workflowIni;
        }

        public void setWorkflowIni(Map workflowIni) {
            this.workflowIni = workflowIni;
        }

        public Collection<ReducedFileProvenanceReportRecord> getFiles() {
            return files;
        }

        public void setFiles(Collection<ReducedFileProvenanceReportRecord> files) {
            this.files = files;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            //
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            return EqualsBuilder.reflectionEquals(this, obj);
        }

    }

}
