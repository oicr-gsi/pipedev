package ca.on.oicr.pde.testing.decider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.danielbechler.diff.ObjectDifferFactory;
import de.danielbechler.diff.node.Node;
import de.danielbechler.diff.path.PropertyPath;
import de.danielbechler.diff.visitor.PrintingVisitor;
import de.danielbechler.diff.visitor.ToMapPrintingVisitor;
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

    public static Map<PropertyPath, String> diffReports(DeciderRunTestReport actual, DeciderRunTestReport expected) {
        Node root = ObjectDifferFactory.getInstance().compare(actual, expected);
        MinimalToMapPrintingVisitor v = new MinimalToMapPrintingVisitor(actual, expected);
        root.visit(v);
        return v.getMessages();
    }

    public static String diffReportSummary(DeciderRunTestReport actual, DeciderRunTestReport expected, int maxChangesToPrint) {

        //Get changes
        Map<PropertyPath, String> differences = diffReports(actual, expected);

        //Build summary
        StringBuilder sb = new StringBuilder();
        switch (differences.size()) {
            case 0:
                sb.append("There are no changes");
                break;
            case 1:
                sb.append("There is 1 change:");
                break;
            default:
                sb.append(String.format("There are %s changes:", differences.size()));
                break;
        }
        int count = 0;
        for (Entry<PropertyPath, String> e : differences.entrySet()) {
            if (++count > maxChangesToPrint) {
                sb.append("\n... ").append(differences.size() - maxChangesToPrint).append(" more");
                break;
            }
            sb.append("\n").append(e.getValue());
        }
        return sb.toString();
    }

    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(this);
    }

    private static class MinimalToMapPrintingVisitor extends ToMapPrintingVisitor {

        public MinimalToMapPrintingVisitor(final Object working, final Object base) {
            super(working, base);
        }

        @Override
        protected String differenceToString(final Node node, final Object base, final Object modified) {
            String text = super.differenceToString(node, base, modified);
            text = text.replaceAll("Property at path ", "");
            getMessages().put(node.getPropertyPath(), text);
            return text;
        }

    }

}
