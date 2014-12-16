package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * @author mlaszloffy
 */
public class WorkflowRunReport implements Comparable<WorkflowRunReport> {
    Map workflowIni = new TreeMap<>();
    List<ReducedFileProvenanceReportRecord> files = new ArrayList<>();

    public Map getWorkflowIni() {
        return workflowIni;
    }

    public void setWorkflowIni(Map<String, String> workflowIni) {
        this.workflowIni = new TreeMap<>(workflowIni);
    }

    public List<ReducedFileProvenanceReportRecord> getFiles() {
        Collections.sort(files);
        return files;
    }

    public void setFiles(List<ReducedFileProvenanceReportRecord> files) {
        this.files.addAll(files);
        Collections.sort(this.files);
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

    @Override
    public int compareTo(WorkflowRunReport o) {
        //TODO: optimize
        return this.toString().compareTo(o.toString());
    }
    
}
