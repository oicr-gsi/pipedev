package ca.on.oicr.pde.common.utilities;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class FileProvenanceReport {

    public static class FileProvenanceRecord implements Serializable {

        private String lastModified;
        private String studyTitle;
        private String studySWID;
        private String studyAttributes;
        private String experimentName;
        private String experimentSWID;
        private String experimentAttributes;
        private String parentSampleName;
        private String parentSampleSWID;
        private String parentSampleAttributes;
        private String sampleName;
        private String sampleSWID;
        private String sampleAttributes;
        private String sequencerRunName;
        private String sequencerRunSWID;
        private String sequencerRunAttributes;
        private String laneName;
        private String laneNumber;
        private String laneSWID;
        private String laneAttributes;
        private String iusTag;
        private String iusSWID;
        private String iusAttributes;
        private String workflowName;
        private String workflowVersion;
        private String workflowSWID;
        private String workflowRunName;
        private String workflowRunStatus;
        private String workflowRunSWID;
        private String processingAlgorithm;
        private String processingSWID;
        private String processingAttributes;
        private String fileMetaType;
        private String fileSWID;
        private String fileAttributes;
        private String filePath;
        private String fileMd5sum;
        private String fileSize;
        private String fileDescription;
        private String skip;

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        public String getStudyTitle() {
            return studyTitle;
        }

        public void setStudyTitle(String studyTitle) {
            this.studyTitle = studyTitle;
        }

        public String getStudySWID() {
            return studySWID;
        }

        public void setStudySWID(String studySWID) {
            this.studySWID = studySWID;
        }

        public String getStudyAttributes() {
            return studyAttributes;
        }

        public void setStudyAttributes(String studyAttributes) {
            this.studyAttributes = studyAttributes;
        }

        public String getExperimentName() {
            return experimentName;
        }

        public void setExperimentName(String experimentName) {
            this.experimentName = experimentName;
        }

        public String getExperimentSWID() {
            return experimentSWID;
        }

        public void setExperimentSWID(String experimentSWID) {
            this.experimentSWID = experimentSWID;
        }

        public String getExperimentAttributes() {
            return experimentAttributes;
        }

        public void setExperimentAttributes(String experimentAttributes) {
            this.experimentAttributes = experimentAttributes;
        }

        public String getParentSampleName() {
            return parentSampleName;
        }

        public void setParentSampleName(String parentSampleName) {
            this.parentSampleName = parentSampleName;
        }

        public String getParentSampleSWID() {
            return parentSampleSWID;
        }

        public void setParentSampleSWID(String parentSampleSWID) {
            this.parentSampleSWID = parentSampleSWID;
        }

        public String getParentSampleAttributes() {
            return parentSampleAttributes;
        }

        public void setParentSampleAttributes(String parentSampleAttributes) {
            this.parentSampleAttributes = parentSampleAttributes;
        }

        public String getSampleName() {
            return sampleName;
        }

        public void setSampleName(String sampleName) {
            this.sampleName = sampleName;
        }

        public String getSampleSWID() {
            return sampleSWID;
        }

        public void setSampleSWID(String sampleSWID) {
            this.sampleSWID = sampleSWID;
        }

        public String getSampleAttributes() {
            return sampleAttributes;
        }

        public void setSampleAttributes(String sampleAttributes) {
            this.sampleAttributes = sampleAttributes;
        }

        public String getSequencerRunName() {
            return sequencerRunName;
        }

        public void setSequencerRunName(String sequencerRunName) {
            this.sequencerRunName = sequencerRunName;
        }

        public String getSequencerRunSWID() {
            return sequencerRunSWID;
        }

        public void setSequencerRunSWID(String sequencerRunSWID) {
            this.sequencerRunSWID = sequencerRunSWID;
        }

        public String getSequencerRunAttributes() {
            return sequencerRunAttributes;
        }

        public void setSequencerRunAttributes(String sequencerRunAttributes) {
            this.sequencerRunAttributes = sequencerRunAttributes;
        }

        public String getLaneName() {
            return laneName;
        }

        public void setLaneName(String laneName) {
            this.laneName = laneName;
        }

        public String getLaneNumber() {
            return laneNumber;
        }

        public void setLaneNumber(String laneNumber) {
            this.laneNumber = laneNumber;
        }

        public String getLaneSWID() {
            return laneSWID;
        }

        public void setLaneSWID(String laneSWID) {
            this.laneSWID = laneSWID;
        }

        public String getLaneAttributes() {
            return laneAttributes;
        }

        public void setLaneAttributes(String laneAttributes) {
            this.laneAttributes = laneAttributes;
        }

        public String getIusTag() {
            return iusTag;
        }

        public void setIusTag(String iusTag) {
            this.iusTag = iusTag;
        }

        public String getIusSWID() {
            return iusSWID;
        }

        public void setIusSWID(String iusSWID) {
            this.iusSWID = iusSWID;
        }

        public String getIusAttributes() {
            return iusAttributes;
        }

        public void setIusAttributes(String iusAttributes) {
            this.iusAttributes = iusAttributes;
        }

        public String getWorkflowName() {
            return workflowName;
        }

        public void setWorkflowName(String workflowName) {
            this.workflowName = workflowName;
        }

        public String getWorkflowVersion() {
            return workflowVersion;
        }

        public void setWorkflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
        }

        public String getWorkflowSWID() {
            return workflowSWID;
        }

        public void setWorkflowSWID(String workflowSWID) {
            this.workflowSWID = workflowSWID;
        }

        public String getWorkflowRunName() {
            return workflowRunName;
        }

        public void setWorkflowRunName(String workflowRunName) {
            this.workflowRunName = workflowRunName;
        }

        public String getWorkflowRunStatus() {
            return workflowRunStatus;
        }

        public void setWorkflowRunStatus(String workflowRunStatus) {
            this.workflowRunStatus = workflowRunStatus;
        }

        public String getWorkflowRunSWID() {
            return workflowRunSWID;
        }

        public void setWorkflowRunSWID(String workflowRunSWID) {
            this.workflowRunSWID = workflowRunSWID;
        }

        public String getProcessingAlgorithm() {
            return processingAlgorithm;
        }

        public void setProcessingAlgorithm(String processingAlgorithm) {
            this.processingAlgorithm = processingAlgorithm;
        }

        public String getProcessingSWID() {
            return processingSWID;
        }

        public void setProcessingSWID(String processingSWID) {
            this.processingSWID = processingSWID;
        }

        public String getProcessingAttributes() {
            return processingAttributes;
        }

        public void setProcessingAttributes(String processingAttributes) {
            this.processingAttributes = processingAttributes;
        }

        public String getFileMetaType() {
            return fileMetaType;
        }

        public void setFileMetaType(String fileMetaType) {
            this.fileMetaType = fileMetaType;
        }

        public String getFileSWID() {
            return fileSWID;
        }

        public void setFileSWID(String fileSWID) {
            this.fileSWID = fileSWID;
        }

        public String getFileAttributes() {
            return fileAttributes;
        }

        public void setFileAttributes(String fileAttributes) {
            this.fileAttributes = fileAttributes;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getFileMd5sum() {
            return fileMd5sum;
        }

        public void setFileMd5sum(String fileMd5sum) {
            this.fileMd5sum = fileMd5sum;
        }

        public String getFileSize() {
            return fileSize;
        }

        public void setFileSize(String fileSize) {
            this.fileSize = fileSize;
        }

        public String getFileDescription() {
            return fileDescription;
        }

        public void setFileDescription(String fileDescription) {
            this.fileDescription = fileDescription;
        }

        public String getSkip() {
            return skip;
        }

        public void setSkip(String skip) {
            this.skip = skip;
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

    public static class ValidationException extends RuntimeException {

        public ValidationException() {
            super();
        }

        public ValidationException(String message) {
            super(message);
        }

        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }

        public ValidationException(Throwable cause) {
            super(cause);
        }

    }
//
//    public static void compareFileProvenanceReport() {
//
//    }

    private static void validateHeader(Map header) {

        // This map represents the header names and column positions that are expected.
        // If there is a change to the SeqWare FileProvenanceReport structure:
        //  1) Update the tsv "schema" (below)
        //  2) Update the sample tsv resource in the test fileprovenance resourses
        //  3) Update the "parseFileProvenanceReport" method to include/remove the attribute
        //  4) Update the FileProvenanceRecord class (don't forget the setter/getter)
        Map fileProvenanceReportExpectedHeader = new HashMap();
        fileProvenanceReportExpectedHeader.put("Last Modified", 0);
        fileProvenanceReportExpectedHeader.put("Study Title", 1);
        fileProvenanceReportExpectedHeader.put("Study SWID", 2);
        fileProvenanceReportExpectedHeader.put("Study Attributes", 3);
        fileProvenanceReportExpectedHeader.put("Experiment Name", 4);
        fileProvenanceReportExpectedHeader.put("Experiment SWID", 5);
        fileProvenanceReportExpectedHeader.put("Experiment Attributes", 6);
        fileProvenanceReportExpectedHeader.put("Parent Sample Name", 7);
        fileProvenanceReportExpectedHeader.put("Parent Sample SWID", 8);
        fileProvenanceReportExpectedHeader.put("Parent Sample Attributes", 9);
        fileProvenanceReportExpectedHeader.put("Sample Name", 10);
        fileProvenanceReportExpectedHeader.put("Sample SWID", 11);
        fileProvenanceReportExpectedHeader.put("Sample Attributes", 12);
        fileProvenanceReportExpectedHeader.put("Sequencer Run Name", 13);
        fileProvenanceReportExpectedHeader.put("Sequencer Run SWID", 14);
        fileProvenanceReportExpectedHeader.put("Sequencer Run Attributes", 15);
        fileProvenanceReportExpectedHeader.put("Lane Name", 16);
        fileProvenanceReportExpectedHeader.put("Lane Number", 17);
        fileProvenanceReportExpectedHeader.put("Lane SWID", 18);
        fileProvenanceReportExpectedHeader.put("Lane Attributes", 19);
        fileProvenanceReportExpectedHeader.put("IUS Tag", 20);
        fileProvenanceReportExpectedHeader.put("IUS SWID", 21);
        fileProvenanceReportExpectedHeader.put("IUS Attributes", 22);
        fileProvenanceReportExpectedHeader.put("Workflow Name", 23);
        fileProvenanceReportExpectedHeader.put("Workflow Version", 24);
        fileProvenanceReportExpectedHeader.put("Workflow SWID", 25);
        fileProvenanceReportExpectedHeader.put("Workflow Run Name", 26);
        fileProvenanceReportExpectedHeader.put("Workflow Run Status", 27);
        fileProvenanceReportExpectedHeader.put("Workflow Run SWID", 28);
        fileProvenanceReportExpectedHeader.put("Processing Algorithm", 29);
        fileProvenanceReportExpectedHeader.put("Processing SWID", 30);
        fileProvenanceReportExpectedHeader.put("Processing Attributes", 31);
        fileProvenanceReportExpectedHeader.put("File Meta-Type", 32);
        fileProvenanceReportExpectedHeader.put("File SWID", 33);
        fileProvenanceReportExpectedHeader.put("File Attributes", 34);
        fileProvenanceReportExpectedHeader.put("File Path", 35);
        fileProvenanceReportExpectedHeader.put("File Md5sum", 36);
        fileProvenanceReportExpectedHeader.put("File Size", 37);
        fileProvenanceReportExpectedHeader.put("File Description", 38);
        fileProvenanceReportExpectedHeader.put("Skip", 39);

        if (!fileProvenanceReportExpectedHeader.equals(header)) {

            throw new ValidationException("FileProvenanceReport headers differ from expected format.");

        }

    }

    public static List<FileProvenanceRecord> parseFileProvenanceReport(Reader reportReader) throws IOException {

        CSVFormat csvFormat = CSVFormat.RFC4180.withHeader().withDelimiter('\t');

        CSVParser p = new CSVParser(reportReader, csvFormat);

        // This will throw a runtime exception (ValidationException) if the headers to not match the expected header format
        validateHeader(p.getHeaderMap());

        // Build a list of FileProvenanceRecord from tsv stream reader
        List ls = new ArrayList<FileProvenanceRecord>();
        for (CSVRecord r : p) {

            FileProvenanceRecord rec = new FileProvenanceRecord();
            rec.setLastModified(r.get("Last Modified"));
            rec.setStudyTitle(r.get("Study Title"));
            rec.setStudySWID(r.get("Study SWID"));
            rec.setStudyAttributes(r.get("Study Attributes"));
            rec.setExperimentName(r.get("Experiment Name"));
            rec.setExperimentSWID(r.get("Experiment SWID"));
            rec.setExperimentAttributes(r.get("Experiment Attributes"));
            rec.setParentSampleName(r.get("Parent Sample Name"));
            rec.setParentSampleSWID(r.get("Parent Sample SWID"));
            rec.setParentSampleAttributes(r.get("Parent Sample Attributes"));
            rec.setSampleName(r.get("Sample Name"));
            rec.setSampleSWID(r.get("Sample SWID"));
            rec.setSampleAttributes(r.get("Sample Attributes"));
            rec.setSequencerRunName(r.get("Sequencer Run Name"));
            rec.setSequencerRunSWID(r.get("Sequencer Run SWID"));
            rec.setSequencerRunAttributes(r.get("Sequencer Run Attributes"));
            rec.setLaneName(r.get("Lane Name"));
            rec.setLaneNumber(r.get("Lane Number"));
            rec.setLaneSWID(r.get("Lane SWID"));
            rec.setLaneAttributes(r.get("Lane Attributes"));
            rec.setIusTag(r.get("IUS Tag"));
            rec.setIusSWID(r.get("IUS SWID"));
            rec.setIusAttributes(r.get("IUS Attributes"));
            rec.setWorkflowName(r.get("Workflow Name"));
            rec.setWorkflowVersion(r.get("Workflow Version"));
            rec.setWorkflowSWID(r.get("Workflow SWID"));
            rec.setWorkflowRunName(r.get("Workflow Run Name"));
            rec.setWorkflowRunStatus(r.get("Workflow Run Status"));
            rec.setWorkflowRunSWID(r.get("Workflow Run SWID"));
            rec.setProcessingAlgorithm(r.get("Processing Algorithm"));
            rec.setProcessingSWID(r.get("Processing SWID"));
            rec.setProcessingAttributes(r.get("Processing Attributes"));
            rec.setFileMetaType(r.get("File Meta-Type"));
            rec.setFileSWID(r.get("File SWID"));
            rec.setFileAttributes(r.get("File Attributes"));
            rec.setFilePath(r.get("File Path"));
            rec.setFileMd5sum(r.get("File Md5sum"));
            rec.setFileSize(r.get("File Size"));
            rec.setFileDescription(r.get("File Description"));
            rec.setSkip(r.get("Skip"));

            ls.add(rec);

        }

        return ls;

    }

}