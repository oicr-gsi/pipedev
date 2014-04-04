package ca.on.oicr.pde.model;

import java.util.Collections;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ReducedFileProvenanceReportRecord {
    private String studyTitle = "";
    private Map<String, String> studyAttributes = Collections.EMPTY_MAP;
    private String experimentName = "";
    private Map<String, String> experimentAttributes = Collections.EMPTY_MAP;
    private String sampleName = "";
    private Map<String, String> sampleAttributes = Collections.EMPTY_MAP;
    private String sequencerRunName = "";
    private Map<String, String> sequencerRunAttributes = Collections.EMPTY_MAP;
    private String laneName = "";
    private Map<String, String> laneAttributes = Collections.EMPTY_MAP;
    private String iusTag = "";
    private Map<String, String> iusAttributes = Collections.EMPTY_MAP;
    private String workflowName = "";
    private String processingAlgorithm = "";
    private Map<String, String> processingAttributes = Collections.EMPTY_MAP;
    private String fileMetaType = "";
    private Map<String, String> fileAttributes = Collections.EMPTY_MAP;
    private int fileId = 0;

    public ReducedFileProvenanceReportRecord() {
    }

    public ReducedFileProvenanceReportRecord(FileProvenanceReportRecord f) {
        this.studyTitle = f.getStudyTitle();
        this.studyAttributes = f.getStudyAttributes();
        this.experimentName = f.getExperimentName();
        this.experimentAttributes = f.getExperimentAttributes();
        this.sampleName = f.getSampleName();
        this.sampleAttributes = f.getSampleAttributes();
        this.sequencerRunName = f.getSequencerRunName();
        this.sequencerRunAttributes = f.getSequencerRunAttributes();
        this.laneName = f.getLaneName();
        this.laneAttributes = f.getLaneAttributes();
        this.iusTag = f.getIusTag();
        this.iusAttributes = f.getIusAttributes();
        this.workflowName = f.getWorkflowName();
        this.processingAlgorithm = f.getProcessingAlgorithm();
        this.processingAttributes = f.getProcessingAttributes();
        this.fileMetaType = f.getFileMetaType();
        this.fileAttributes = f.getFileAttributes();
        this.fileId = f.getFilePath().hashCode();
        
    }

    public String getStudyTitle() {
        return studyTitle;
    }

    public void setStudyTitle(String studyTitle) {
        this.studyTitle = studyTitle;
    }

    public Map<String, String> getStudyAttributes() {
        return studyAttributes;
    }

    public void setStudyAttributes(Map<String, String> studyAttributes) {
        this.studyAttributes = studyAttributes;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public Map<String, String> getExperimentAttributes() {
        return experimentAttributes;
    }

    public void setExperimentAttributes(Map<String, String> experimentAttributes) {
        this.experimentAttributes = experimentAttributes;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public Map<String, String> getSampleAttributes() {
        return sampleAttributes;
    }

    public void setSampleAttributes(Map<String, String> sampleAttributes) {
        this.sampleAttributes = sampleAttributes;
    }

    public String getSequencerRunName() {
        return sequencerRunName;
    }

    public void setSequencerRunName(String sequencerRunName) {
        this.sequencerRunName = sequencerRunName;
    }

    public Map<String, String> getSequencerRunAttributes() {
        return sequencerRunAttributes;
    }

    public void setSequencerRunAttributes(Map<String, String> sequencerRunAttributes) {
        this.sequencerRunAttributes = sequencerRunAttributes;
    }

    public String getLaneName() {
        return laneName;
    }

    public void setLaneName(String laneName) {
        this.laneName = laneName;
    }

    public Map<String, String> getLaneAttributes() {
        return laneAttributes;
    }

    public void setLaneAttributes(Map<String, String> laneAttributes) {
        this.laneAttributes = laneAttributes;
    }

    public String getIusTag() {
        return iusTag;
    }

    public void setIusTag(String iusTag) {
        this.iusTag = iusTag;
    }

    public Map<String, String> getIusAttributes() {
        return iusAttributes;
    }

    public void setIusAttributes(Map<String, String> iusAttributes) {
        this.iusAttributes = iusAttributes;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getProcessingAlgorithm() {
        return processingAlgorithm;
    }

    public void setProcessingAlgorithm(String processingAlgorithm) {
        this.processingAlgorithm = processingAlgorithm;
    }

    public Map<String, String> getProcessingAttributes() {
        return processingAttributes;
    }

    public void setProcessingAttributes(Map<String, String> processingAttributes) {
        this.processingAttributes = processingAttributes;
    }

    public String getFileMetaType() {
        return fileMetaType;
    }

    public void setFileMetaType(String fileMetaType) {
        this.fileMetaType = fileMetaType;
    }

    public Map<String, String> getFileAttributes() {
        return fileAttributes;
    }

    public void setFileAttributes(Map<String, String> fileAttributes) {
        this.fileAttributes = fileAttributes;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
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
