package ca.on.oicr.pde.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class FileProvenanceReportRecord implements Serializable {

    private final Experiment experiment;
    private final Study study;
    private final Ius ius;
    private final Lane lane;
    private final SequencerRun sequencerRun;
    private final Processing processing;
    private final Workflow workflow;
    private final WorkflowRun workflowRun;
    private final File file;
    private final Sample sample;
    //private final List<Sample> parentSamples;
    private final String parentSampleSwids;
    private final String parentSampleNames;
    private final String parentSampleAttributes;

    private final String lastModified;
    private final String skip;

    private FileProvenanceReportRecord(Builder b) {

        this.experiment = b.experiment;
        this.study = b.study;
        this.ius = b.ius;
        this.lane = b.lane;
        this.sequencerRun = b.sequencerRun;
        this.processing = b.processing;
        this.workflow = b.workflow;
        this.workflowRun = b.workflowRun;
        this.file = b.file;
        this.sample = b.sample;
        //this.parentSamples = b.parentSamples;
        this.parentSampleSwids = b.parentSampleSwids;
        this.parentSampleNames = b.parentSampleNames;
        this.parentSampleAttributes = b.parentSampleAttributes;
        this.lastModified = b.lastModified;
        this.skip = b.skip;

    }

    public String getLastModified() {
        return lastModified;
    }

    public String getStudyTitle() {
        return study.getTitle();
    }

    public String getStudySwid() {
        return study.getSwid();
    }

    public Map<String, String> getStudyAttributes() {
        return study.getAttributes();
    }

    public String getExperimentName() {
        return experiment.getName();
    }

    public String getExperimentSwid() {
        return experiment.getSwid();
    }

    public Map<String, String> getExperimentAttributes() {
        return experiment.getAttributes();
    }

    public String getParentSampleName() {
//        String result = "";
//        String delim = "";
//        for (Sample s : parentSamples) {
//            result = delim + s.getName();
//            delim = ":";
//        }
//        return result;

        return parentSampleNames;
    }

    public String getParentSampleSwid() {
//        String result = "";
//        String delim = "";
//        for (Sample s : parentSamples) {
//            result = delim + s.getSwid().toString();
//            delim = ":";
//        }
//        return result;

        return parentSampleSwids;
    }

    public String getParentSampleAttributes() {
//        String result = "";
//        String delim = "";
//        for (Sample s : parentSamples) {
//            result = delim + s.getAttributes().toString();
//            delim = ":";
//        }
//        return result;

        return parentSampleAttributes;
    }

    public String getSampleName() {
        return sample.getName();
    }

    public String getSampleSwid() {
        return sample.getSwid();
    }

    public Map<String, String> getSampleAttributes() {
        return sample.getAttributes();
    }

    public String getSequencerRunName() {
        return sequencerRun.getName();
    }

    public String getSequencerRunSwid() {
        return sequencerRun.getSwid();
    }

    public Map<String, String> getSequencerRunAttributes() {
        return sequencerRun.getAttributes();
    }

    public String getLaneName() {
        return lane.getName();
    }

    public String getLaneNumber() {
        return lane.getNumber();
    }

    public String getLaneSwid() {
        return lane.getSwid();
    }

    public Map<String, String> getLaneAttributes() {
        return lane.getAttributes();
    }

    public String getIusTag() {
        return ius.getTag();
    }

    public String getIusSwid() {
        return ius.getSwid();
    }

    public Map<String, String> getIusAttributes() {
        return ius.getAttributes();
    }

    public String getWorkflowName() {
        return workflow.getName();
    }

    public String getWorkflowVersion() {
        return workflow.getVersion();
    }

    public String getWorkflowSwid() {
        return workflow.getSwid();
    }

    public String getWorkflowRunName() {
        return workflowRun.getName();
    }

    public String getWorkflowRunStatus() {
        return workflowRun.getStatus();
    }

    public String getWorkflowRunSwid() {
        return workflowRun.getSwid();
    }

    public String getProcessingAlgorithm() {
        return processing.getAlgorithm();
    }

    public String getProcessingSwid() {
        return processing.getSwid();
    }

    public Map<String, String> getProcessingAttributes() {
        return processing.getAttributes();
    }

    public String getFileMetaType() {
        return file.getMetaType();
    }

    public String getFileSwid() {
        return file.getSwid();
    }

    public Map<String, String> getFileAttributes() {
        return file.getAttributes();
    }

    public String getFilePath() {
        return file.getPath();
    }

    public String getFileMd5sum() {
        return file.getMd5sum();
    }

    public String getFileSize() {
        return file.getSize();
    }

    public String getFileDescription() {
        return file.getDescription();
    }

    public String getSkip() {
        return skip;
    }

    public List<String> getSeqwareAccessions() {

        return Arrays.asList(study.getSwid(), experiment.getSwid(), sample.getSwid(), sequencerRun.getSwid(), lane.getSwid(), ius.getSwid(), workflow.getSwid(),
                workflowRun.getSwid(), processing.getSwid(), file.getSwid());

    }

    public boolean containsSeqwareAccession(Accessionable swid) {

        return getSeqwareAccessions().contains(swid.getSwid());

    }

    public boolean containsSeqwareAccession(Collection<? extends Accessionable> swids) {

        List<String> accessions = getSeqwareAccessions();

        for (Accessionable swid : swids) {
            if (accessions.contains(swid.getSwid())) {
                return true;
            }
        }
        return false;

    }

    public ReducedFileProvenanceReportRecord getSimpleFileProvenanceReportRecord() {

        return new ReducedFileProvenanceReportRecord(this);

    }

//    public SimpleFileProvenanceReportRecord getSimpleRecord(){
//        
//    }
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

    public static class Builder {

        private Experiment experiment;
        private Study study;
        private Ius ius;
        private Lane lane;
        private SequencerRun sequencerRun;
        private Processing processing;
        private Workflow workflow;
        private WorkflowRun workflowRun;
        private File file;
        private Sample sample;
        //private List<Sample> parentSamples;

        private String lastModified = "";
        private String studyTitle = "";
        private String studySwid = "";
        private String studyAttributes = "";
        private String experimentName = "";
        private String experimentSwid = "";
        private String experimentAttributes = "";
        private String parentSampleNames = "";
        private String parentSampleSwids = "";
        private String parentSampleAttributes = "";
        private String sampleName = "";
        private String sampleSwid = "";
        private String sampleAttributes = "";
        private String sequencerRunName = "";
        private String sequencerRunSwid = "";
        private String sequencerRunAttributes = "";
        private String laneName = "";
        private String laneNumber = "";
        private String laneSwid = "";
        private String laneAttributes = "";
        private String iusTag = "";
        private String iusSwid = "";
        private String iusAttributes = "";
        private String workflowName = "";
        private String workflowVersion = "";
        private String workflowSwid = "";
        private String workflowRunName = "";
        private String workflowRunStatus = "";
        private String workflowRunSwid = "";
        private String processingAlgorithm = "";
        private String processingSwid = "";
        private String processingAttributes = "";
        private String fileMetaType = "";
        private String fileSwid = "";
        private String fileAttributes = "";
        private String filePath = "";
        private String fileMd5sum = "";
        private String fileSize = "";
        private String fileDescription = "";
        private String skip = "";

        public Builder() {

        }

        public Builder setLastModified(String lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder setStudyTitle(String studyTitle) {
            this.studyTitle = studyTitle;
            return this;
        }

        public Builder setStudySwid(String studySwid) {
            this.studySwid = studySwid;
            return this;
        }

        public Builder setStudyAttributes(String studyAttributes) {
            this.studyAttributes = studyAttributes;
            return this;
        }

        public Builder setExperimentName(String experimentName) {
            this.experimentName = experimentName;
            return this;
        }

        public Builder setExperimentSwid(String experimentSwid) {
            this.experimentSwid = experimentSwid;
            return this;
        }

        public Builder setExperimentAttributes(String experimentAttributes) {
            this.experimentAttributes = experimentAttributes;
            return this;
        }

        public Builder setParentSampleName(String parentSampleName) {
            this.parentSampleNames = parentSampleName;
            return this;
        }

        public Builder setParentSampleSwid(String parentSampleSwid) {
            this.parentSampleSwids = parentSampleSwid;
            return this;
        }

        public Builder setParentSampleAttributes(String parentSampleAttributes) {
            this.parentSampleAttributes = parentSampleAttributes;
            return this;
        }

        public Builder setSampleName(String sampleName) {
            this.sampleName = sampleName;
            return this;
        }

        public Builder setSampleSwid(String sampleSwid) {
            this.sampleSwid = sampleSwid;
            return this;
        }

        public Builder setSampleAttributes(String sampleAttributes) {
            this.sampleAttributes = sampleAttributes;
            return this;
        }

        public Builder setSequencerRunName(String sequencerRunName) {
            this.sequencerRunName = sequencerRunName;
            return this;
        }

        public Builder setSequencerRunSwid(String sequencerRunSwid) {
            this.sequencerRunSwid = sequencerRunSwid;
            return this;
        }

        public Builder setSequencerRunAttributes(String sequencerRunAttributes) {
            this.sequencerRunAttributes = sequencerRunAttributes;
            return this;
        }

        public Builder setLaneName(String laneName) {
            this.laneName = laneName;
            return this;
        }

        public Builder setLaneNumber(String laneNumber) {
            this.laneNumber = laneNumber;
            return this;
        }

        public Builder setLaneSwid(String laneSwid) {
            this.laneSwid = laneSwid;
            return this;
        }

        public Builder setLaneAttributes(String laneAttributes) {
            this.laneAttributes = laneAttributes;
            return this;
        }

        public Builder setIusTag(String iusTag) {
            this.iusTag = iusTag;
            return this;
        }

        public Builder setIusSwid(String iusSwid) {
            this.iusSwid = iusSwid;
            return this;
        }

        public Builder setIusAttributes(String iusAttributes) {
            this.iusAttributes = iusAttributes;
            return this;
        }

        public Builder setWorkflowName(String workflowName) {
            this.workflowName = workflowName;
            return this;
        }

        public Builder setWorkflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public Builder setWorkflowSwid(String workflowSwid) {
            this.workflowSwid = workflowSwid;
            return this;
        }

        public Builder setWorkflowRunName(String workflowRunName) {
            this.workflowRunName = workflowRunName;
            return this;
        }

        public Builder setWorkflowRunStatus(String workflowRunStatus) {
            this.workflowRunStatus = workflowRunStatus;
            return this;
        }

        public Builder setWorkflowRunSwid(String workflowRunSwid) {
            this.workflowRunSwid = workflowRunSwid;
            return this;
        }

        public Builder setProcessingAlgorithm(String processingAlgorithm) {
            this.processingAlgorithm = processingAlgorithm;
            return this;
        }

        public Builder setProcessingSwid(String processingSwid) {
            this.processingSwid = processingSwid;
            return this;
        }

        public Builder setProcessingAttributes(String processingAttributes) {
            this.processingAttributes = processingAttributes;
            return this;
        }

        public Builder setFileMetaType(String fileMetaType) {
            this.fileMetaType = fileMetaType;
            return this;
        }

        public Builder setFileSwid(String fileSwid) {
            this.fileSwid = fileSwid;
            return this;
        }

        public Builder setFileAttributes(String fileAttributes) {
            this.fileAttributes = fileAttributes;
            return this;
        }

        public Builder setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder setFileMd5sum(String fileMd5sum) {
            this.fileMd5sum = fileMd5sum;
            return this;
        }

        public Builder setFileSize(String fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder setFileDescription(String fileDescription) {
            this.fileDescription = fileDescription;
            return this;
        }

        public Builder setSkip(String skip) {
            this.skip = skip;
            return this;
        }

        public FileProvenanceReportRecord build() {

            //TODO: use factory to create these objects:
            experiment = new Experiment();
            experiment.setSwid(getSwid(experimentSwid));
            experiment.setName(experimentName);
            experiment.setAttributes(experimentAttributes);

            study = new Study();
            study.setSwid(getSwid(studySwid));
            study.setTitle(studyTitle);
            study.setAttributes(studyAttributes);

            ius = new Ius();
            ius.setSwid(getSwid(iusSwid));
            ius.setTag(iusTag);
            ius.setAttributes(iusAttributes);

            lane = new Lane();
            lane.setSwid(getSwid(laneSwid));
            lane.setName(laneName);
            lane.setNumber(laneNumber);
            lane.setAttributes(laneAttributes);

            sequencerRun = new SequencerRun();
            sequencerRun.setSwid(getSwid(sequencerRunSwid));
            sequencerRun.setName(sequencerRunName);
            sequencerRun.setAttributes(sequencerRunAttributes);

            processing = new Processing();
            processing.setSwid(getSwid(processingSwid));
            processing.setAlgorithm(processingAlgorithm);
            processing.setAttributes(processingAttributes);

            workflow = new Workflow();
            workflow.setSwid(getSwid(workflowSwid));
            workflow.setName(workflowName);
            workflow.setVersion(workflowVersion);

            workflowRun = new WorkflowRun();
            workflowRun.setSwid(getSwid(workflowRunSwid));
            workflowRun.setName(workflowRunName);
            workflowRun.setStatus(workflowRunStatus);

            file = new File();
            file.setSwid(getSwid(fileSwid));
            file.setMetaType(fileMetaType);
            file.setSize(fileSize);
            file.setMd5sum(fileMd5sum);
            file.setDescription(fileDescription);
            file.setPath(filePath);
            file.setAttributes(fileAttributes);

            sample = new Sample();
            sample.setSwid(getSwid(sampleSwid));
            sample.setName(sampleName);
            sample.setAttributes(sampleAttributes);

//            parentSamples = new ArrayList<Sample>();
//            
//            List<String> swids = Arrays.asList(parentSampleSwid.split(":"));
//            List<String> names = Arrays.asList(parentSampleName.split(":"));
//            List<String> attrs = Arrays.asList(parentSampleAttributes.split(":"));
//            for (int i = 0; i < swids.size(); i++) {
//                
//                Sample parent = new Sample();
//                parent.setSwid(getSwid(swids.get(i)));
//                parent.setName(names.get(i));
//                parent.setAttributes(attrs.get(i));
//
//            }
            return new FileProvenanceReportRecord(this);

        }

        private String getSwid(String swid) {

            String result = "";

            if (swid != null && !swid.isEmpty()) {
                return swid;
            }

            return result;

        }

    }

}
