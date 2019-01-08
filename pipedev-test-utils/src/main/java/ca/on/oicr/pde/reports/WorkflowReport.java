package ca.on.oicr.pde.reports;

import ca.on.oicr.gsi.provenance.FileProvenanceFilter;
import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.gsi.provenance.ProvenanceClient;
import ca.on.oicr.pde.dao.reader.FileProvenanceClient;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
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
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.model.WorkflowRun;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ca.on.oicr.pde.client.SeqwareClient;
import com.google.common.base.Functions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;

public class WorkflowReport {

    private Integer workflowRunCount;
    private final SortedSet<String> studies;
    private final SortedSet<String> sequencerRuns;
    private final SortedSet<String> lanes;
    private final SortedSet<String> samples;
    private final SortedSet<String> workflows;
    private final SortedSet<String> processingAlgorithms;
    private final SortedSet<String> fileMetaTypes;
    private Integer maxInputFiles;
    private Integer minInputFiles;
    private Integer totalInputFiles;
    private Integer maxOutputFileProvenanceRecords;
    private Integer minOutputFileProvenanceRecords;
    private Integer totalOutputFileProvenanceRecords;
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
        maxOutputFileProvenanceRecords = Integer.MIN_VALUE;
        minOutputFileProvenanceRecords = Integer.MAX_VALUE;
        totalOutputFileProvenanceRecords = Integer.valueOf("0");

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

    public Integer getMaxOutputFileProvenanceRecords() {
        return maxOutputFileProvenanceRecords;
    }

    public void setMaxOutputFileProvenanceRecords(Integer maxOutputFileProvenanceRecords) {
        this.maxOutputFileProvenanceRecords = maxOutputFileProvenanceRecords;
    }

    public Integer getMinOutputFileProvenanceRecords() {
        return minOutputFileProvenanceRecords;
    }

    public void setMinOutputFileProvenanceRecords(Integer minOutputFileProvenanceRecords) {
        this.minOutputFileProvenanceRecords = minOutputFileProvenanceRecords;
    }

    public Integer getTotalOutputFileProvenanceRecords() {
        return totalOutputFileProvenanceRecords;
    }

    public void setTotalOutputFileProvenanceRecords(Integer totalOutputFileProvenanceRecords) {
        this.totalOutputFileProvenanceRecords = totalOutputFileProvenanceRecords;
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
        if (expected.maxOutputFileProvenanceRecords.intValue() != actual.maxOutputFileProvenanceRecords.intValue()) {
            sb.append(String.format("Max output file provenance record count changed from %s to %s%n", expected.maxOutputFileProvenanceRecords, actual.maxOutputFileProvenanceRecords));
        }
        if (expected.minOutputFileProvenanceRecords.intValue() != actual.minOutputFileProvenanceRecords.intValue()) {
            sb.append(String.format("Min output file provenance record count changed from %s to %s%n", expected.minOutputFileProvenanceRecords, actual.minOutputFileProvenanceRecords));
        }
        if (expected.totalOutputFileProvenanceRecords.intValue() != actual.totalOutputFileProvenanceRecords.intValue()) {
            sb.append(String.format("Total output file provenance record count changed from %s to %s%n", expected.totalOutputFileProvenanceRecords, actual.totalOutputFileProvenanceRecords));
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

    public static WorkflowReport generateReport(SeqwareClient seqwareClient, FileProvenanceClient fpc, List<WorkflowRunReportRecord> wrrs) {

        WorkflowReport workflowReport = new WorkflowReport();
        workflowReport.setWorkflowRunCount(wrrs.size());

        for (WorkflowRunReportRecord wrr : wrrs) {

            Integer workflowRunSwid = Integer.parseInt(wrr.getWorkflowRunSwid());

            //TODO: get workflow run object from workflow run report record
            WorkflowRun workflowRun = new WorkflowRun();
            workflowRun.setSwAccession(workflowRunSwid);

            //Get the workflow run's parent accession(s) (processing accession(s))
//            List<Integer> parentAccessions = seqwareClient.getParentAccession(workflowRun);
            //Get the workflow run's input file(s) (file accession(s))
            List<Integer> inputFileAccessions = seqwareClient.getWorkflowRunInputFiles(workflowRun);

            workflowReport.addStudies(fpc.getStudy(Arrays.asList(workflowRunSwid)));
            workflowReport.addSamples(fpc.getSamples(Arrays.asList(workflowRunSwid)));
            workflowReport.addSequencerRuns(fpc.getSequencerRuns(Arrays.asList(workflowRunSwid)));
            workflowReport.addLanes(fpc.getLanes(Arrays.asList(workflowRunSwid)));
            workflowReport.addWorkflows(fpc.getWorkflows(inputFileAccessions));
            workflowReport.addProcessingAlgorithms(fpc.getProcessingAlgorithms(inputFileAccessions));
            workflowReport.addFileMetaTypes(fpc.getFileMetaTypes(inputFileAccessions));

            List<ReducedFileProvenanceReportRecord> files = fpc.getFiles(inputFileAccessions);
            if (inputFileAccessions.size() > workflowReport.getMaxInputFiles()) {
                workflowReport.setMaxInputFiles(inputFileAccessions.size());
            }

            if (inputFileAccessions.size() < workflowReport.getMinInputFiles()) {
                workflowReport.setMinInputFiles(inputFileAccessions.size());
            }

            workflowReport.setTotalInputFiles(workflowReport.getTotalInputFiles() + inputFileAccessions.size());

            //get the ini that the decider scheduled
            Map<String, String> ini = seqwareClient.getWorkflowRunIni(workflowRun);

            WorkflowRunReport workflowRunReport = new WorkflowRunReport();
            workflowRunReport.setWorkflowIni(ini);
            workflowRunReport.setFiles(files);
            workflowRunReport.setOutputFileProvenanceRecords(fpc.getFileRecords(workflowRunSwid).size());

            //update WorkflowReport fileProvenanceCount
            if (workflowRunReport.getOutputFileProvenanceRecords() > workflowReport.getMaxOutputFileProvenanceRecords()) {
                workflowReport.setMaxOutputFileProvenanceRecords(workflowRunReport.getOutputFileProvenanceRecords());
            }
            if (workflowRunReport.getOutputFileProvenanceRecords() < workflowReport.getMinOutputFileProvenanceRecords()) {
                workflowReport.setMinOutputFileProvenanceRecords(workflowRunReport.getOutputFileProvenanceRecords());
            }
            workflowReport.setTotalOutputFileProvenanceRecords(workflowReport.getTotalOutputFileProvenanceRecords() + workflowRunReport.getOutputFileProvenanceRecords());

            workflowReport.addWorkflowRun(workflowRunReport);
        }

        return workflowReport;
    }

    public static WorkflowReport generateReport(SeqwareClient seqwareClient, ProvenanceClient provenanceClient, Workflow workflow) {
        //get all file provenance records for the workflow
        Map<FileProvenanceFilter, Set<String>> workflowFilter = new HashMap<>();
        workflowFilter.put(FileProvenanceFilter.workflow, ImmutableSet.of(workflow.getSwAccession().toString()));
        Collection<? extends FileProvenance> workflowRunFileProvenanceRecords = provenanceClient.getFileProvenance(workflowFilter);

        //additionally, get all input file file provenance records for the above workflow
        Set<String> inputFiles = new HashSet<>();
        for (FileProvenance fp : workflowRunFileProvenanceRecords) {
            inputFiles.addAll(Collections2.transform(fp.getWorkflowRunInputFileSWIDs(), Functions.toStringFunction()));
        }
        Collection<? extends FileProvenance> inputFileProvenanceRecords;
        if (inputFiles.isEmpty()) {
            inputFileProvenanceRecords = Collections.emptyList();
        } else {
            Map<FileProvenanceFilter, Set<String>> inputFileFilter = new HashMap<>();
            inputFileFilter.put(FileProvenanceFilter.file, inputFiles);
            inputFileProvenanceRecords = provenanceClient.getFileProvenance(inputFileFilter);
        }

        //aggregate file provenance records
        List<FileProvenance> fps = new ArrayList<>();
        fps.addAll(workflowRunFileProvenanceRecords);
        fps.addAll(inputFileProvenanceRecords);
        FileProvenanceClient fpc = new FileProvenanceClient(fps);

        List<WorkflowRunReportRecord> wrrs = seqwareClient.getWorkflowRunRecords(workflow);
        return generateReport(seqwareClient, fpc, wrrs);
    }
}
