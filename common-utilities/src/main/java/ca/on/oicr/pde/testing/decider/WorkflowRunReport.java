package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class WorkflowRunReport {

    Map workflowIni = new HashMap<String,String>();

    List<ReducedFileProvenanceReportRecord> files;

    public Map getWorkflowIni() {
        return workflowIni;
    }

    public void setWorkflowIni(Map<String, String> workflowIni) {
        this.workflowIni.putAll(workflowIni);
    }

    public List<ReducedFileProvenanceReportRecord> getFiles() {
        return files;
    }

    public void setFiles(List<ReducedFileProvenanceReportRecord> files) {
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
