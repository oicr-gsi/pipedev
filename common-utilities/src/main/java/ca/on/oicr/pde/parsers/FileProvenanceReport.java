package ca.on.oicr.pde.parsers;

import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FileProvenanceReport {

    private final static Logger log = LogManager.getLogger(FileProvenanceReport.class);

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

    private static void validateHeader(Map header) {

        // This map represents the header names and column positions that are expected.
        // If there is a change to the SeqWare FileProvenanceReport structure:
        //  1) Update the tsv "schema" (below)
        //  2) Update the sample tsv resource in the test fileprovenance resourses
        //  3) Update the "parseFileProvenanceReport" method to include/remove the attribute
        //  4) Update the FileProvenanceRecord class (don't forget the setter/getter)
        Map fileProvenanceReportExpectedHeader = new LinkedHashMap();
        int col = 0;
        fileProvenanceReportExpectedHeader.put("Last Modified", col++);
        
        fileProvenanceReportExpectedHeader.put("Study Title", col++);
        fileProvenanceReportExpectedHeader.put("Study SWID", col++);
        fileProvenanceReportExpectedHeader.put("Study Attributes", col++);
        
        fileProvenanceReportExpectedHeader.put("Experiment Name", col++);
        fileProvenanceReportExpectedHeader.put("Experiment SWID", col++);
        fileProvenanceReportExpectedHeader.put("Experiment Attributes", col++);
        
        fileProvenanceReportExpectedHeader.put("Root Sample Name", col++);
        fileProvenanceReportExpectedHeader.put("Root Sample SWID", col++);
        
        fileProvenanceReportExpectedHeader.put("Parent Sample Name", col++);
        fileProvenanceReportExpectedHeader.put("Parent Sample SWID", col++);
        fileProvenanceReportExpectedHeader.put("Parent Sample Organism IDs", col++);
        fileProvenanceReportExpectedHeader.put("Parent Sample Attributes", col++);
        
        fileProvenanceReportExpectedHeader.put("Sample Name", col++);
        fileProvenanceReportExpectedHeader.put("Sample SWID", col++);
        fileProvenanceReportExpectedHeader.put("Sample Organism ID", col++);
        fileProvenanceReportExpectedHeader.put("Sample Organism Code", col++);
        fileProvenanceReportExpectedHeader.put("Sample Attributes", col++);
        
        fileProvenanceReportExpectedHeader.put("Sequencer Run Name", col++);
        fileProvenanceReportExpectedHeader.put("Sequencer Run SWID", col++);
        fileProvenanceReportExpectedHeader.put("Sequencer Run Attributes", col++);
        fileProvenanceReportExpectedHeader.put("Sequencer Run Platform ID", col++);
        fileProvenanceReportExpectedHeader.put("Sequencer Run Platform Name", col++);
        
        fileProvenanceReportExpectedHeader.put("Lane Name", col++);
        fileProvenanceReportExpectedHeader.put("Lane Number", col++);
        fileProvenanceReportExpectedHeader.put("Lane SWID", col++);
        fileProvenanceReportExpectedHeader.put("Lane Attributes", col++);
        
        fileProvenanceReportExpectedHeader.put("IUS Tag", col++);
        fileProvenanceReportExpectedHeader.put("IUS SWID", col++);
        fileProvenanceReportExpectedHeader.put("IUS Attributes", col++);
        
        fileProvenanceReportExpectedHeader.put("Workflow Name", col++);
        fileProvenanceReportExpectedHeader.put("Workflow Version", col++);
        fileProvenanceReportExpectedHeader.put("Workflow SWID", col++);
        fileProvenanceReportExpectedHeader.put("Workflow Attributes", col++);
        
        fileProvenanceReportExpectedHeader.put("Workflow Run Name", col++);
        fileProvenanceReportExpectedHeader.put("Workflow Run Status", col++);
        fileProvenanceReportExpectedHeader.put("Workflow Run SWID", col++);
        fileProvenanceReportExpectedHeader.put("Workflow Run Attributes", col++);
        fileProvenanceReportExpectedHeader.put("Workflow Run Input File SWAs", col++);
        
        fileProvenanceReportExpectedHeader.put("Processing Algorithm", col++);
        fileProvenanceReportExpectedHeader.put("Processing SWID", col++);
        fileProvenanceReportExpectedHeader.put("Processing Attributes", col++);
        fileProvenanceReportExpectedHeader.put("Processing Status", col++);
        
        fileProvenanceReportExpectedHeader.put("File Meta-Type", col++);
        fileProvenanceReportExpectedHeader.put("File SWID", col++);
        fileProvenanceReportExpectedHeader.put("File Attributes", col++);
        fileProvenanceReportExpectedHeader.put("File Path", col++);
        fileProvenanceReportExpectedHeader.put("File Md5sum", col++);
        fileProvenanceReportExpectedHeader.put("File Size", col++);
        fileProvenanceReportExpectedHeader.put("File Description", col++);
        fileProvenanceReportExpectedHeader.put("Path Skip", col++);
        fileProvenanceReportExpectedHeader.put("Skip", col++);

        if (!fileProvenanceReportExpectedHeader.equals(header)) {

            log.printf(Level.WARN, "Expected header:\n%s\nActual header:\n%s", fileProvenanceReportExpectedHeader.toString(), header.toString());
            throw new ValidationException("FileProvenanceReport headers differ from expected format.");

        }

    }

    public enum HeaderValidationMode {

        STRICT, SKIP;
    }

    public static List<FileProvenanceReportRecord> parseFileProvenanceReport(InputStream reportStream, HeaderValidationMode mode) throws IOException {

        return parseFileProvenanceReport(new InputStreamReader(reportStream), mode);

    }

    public static List<FileProvenanceReportRecord> parseFileProvenanceReport(InputStream reportStream) throws IOException {

        return parseFileProvenanceReport(new InputStreamReader(reportStream), HeaderValidationMode.STRICT);

    }

    public static List<FileProvenanceReportRecord> parseFileProvenanceReport(Reader reportReader) throws IOException {

        return parseFileProvenanceReport(reportReader, HeaderValidationMode.STRICT);

    }

    public static List<FileProvenanceReportRecord> parseFileProvenanceReport(File reportFile) throws IOException {

        return parseFileProvenanceReport(new FileReader(reportFile), HeaderValidationMode.STRICT);

    }

    public static List<FileProvenanceReportRecord> parseFileProvenanceReport(Reader reportReader, HeaderValidationMode mode) throws IOException {

        CSVFormat csvFormat = CSVFormat.RFC4180.withHeader().withDelimiter('\t');

        CSVParser parser = new CSVParser(reportReader, csvFormat);
        // This will throw a runtime exception (ValidationException) if the headers to not match the expected header format
        try {
            validateHeader(parser.getHeaderMap());
        } catch (ValidationException ve) {
            if (HeaderValidationMode.STRICT.equals(mode)) {
                throw ve;
            } else if (HeaderValidationMode.SKIP.equals(mode)) {
                log.warn("Header validation has been skipped, continuing");
            } else {
                throw ve;
            }
        }

        // Build a list of FileProvenanceRecord from tsv stream reader
        List ls = new LinkedList<>();
        for (CSVRecord r : parser) {

            FileProvenanceReportRecord.Builder rec = new FileProvenanceReportRecord.Builder(r.getRecordNumber());

            rec.setLastModified(r.get("Last Modified"));

            rec.setStudyTitle(r.get("Study Title"));
            rec.setStudySwid(r.get("Study SWID"));
            rec.setStudyAttributes(r.get("Study Attributes"));

            rec.setExperimentName(r.get("Experiment Name"));
            rec.setExperimentSwid(r.get("Experiment SWID"));
            rec.setExperimentAttributes(r.get("Experiment Attributes"));

            rec.setRootSampleName(r.get("Root Sample Name"));
            rec.setRootSampleSwid(r.get("Root Sample SWID"));

            rec.setParentSampleName(r.get("Parent Sample Name"));
            rec.setParentSampleSwid(r.get("Parent Sample SWID"));
            rec.setParentSampleOrganismIds(r.get("Parent Sample Organism IDs"));
            rec.setParentSampleAttributes(r.get("Parent Sample Attributes"));

            rec.setSampleName(r.get("Sample Name"));
            rec.setSampleSwid(r.get("Sample SWID"));
            rec.setSampleOrganismId(r.get("Sample Organism ID"));
            rec.setSampleOrganismCode(r.get("Sample Organism Code"));
            rec.setSampleAttributes(r.get("Sample Attributes"));

            rec.setSequencerRunName(r.get("Sequencer Run Name"));
            rec.setSequencerRunSwid(r.get("Sequencer Run SWID"));
            rec.setSequencerRunAttributes(r.get("Sequencer Run Attributes"));
            rec.setSequencerRunPlatformId(r.get("Sequencer Run Platform ID"));
            rec.setSequencerRunPlatformName(r.get("Sequencer Run Platform Name"));

            rec.setLaneName(r.get("Lane Name"));
            rec.setLaneNumber(r.get("Lane Number"));
            rec.setLaneSwid(r.get("Lane SWID"));
            rec.setLaneAttributes(r.get("Lane Attributes"));

            rec.setIusTag(r.get("IUS Tag"));
            rec.setIusSwid(r.get("IUS SWID"));
            rec.setIusAttributes(r.get("IUS Attributes"));

            rec.setWorkflowName(r.get("Workflow Name"));
            rec.setWorkflowVersion(r.get("Workflow Version"));
            rec.setWorkflowSwid(r.get("Workflow SWID"));
            rec.setWorkflowAttributes(r.get("Workflow Attributes"));

            rec.setWorkflowRunName(r.get("Workflow Run Name"));
            rec.setWorkflowRunStatus(r.get("Workflow Run Status"));
            rec.setWorkflowRunSwid(r.get("Workflow Run SWID"));
            rec.setWorkflowRunAttributes(r.get("Workflow Run Attributes"));
            rec.setWorkflowRunInputFileSwids(r.get("Workflow Run Input File SWAs"));

            rec.setProcessingAlgorithm(r.get("Processing Algorithm"));
            rec.setProcessingSwid(r.get("Processing SWID"));
            rec.setProcessingAttributes(r.get("Processing Attributes"));
            rec.setProcessingStatus(r.get("Processing Status"));

            rec.setFileMetaType(r.get("File Meta-Type"));
            rec.setFileSwid(r.get("File SWID"));
            rec.setFileAttributes(r.get("File Attributes"));
            rec.setFilePath(r.get("File Path"));
            rec.setFileMd5sum(r.get("File Md5sum"));
            rec.setFileSize(r.get("File Size"));
            rec.setFileDescription(r.get("File Description"));
            rec.setPathSkip(r.get("Path Skip"));
            rec.setSkip(r.get("Skip"));

            ls.add(rec.build());

            if (r.getRecordNumber() % 10000 == 0) {
                log.printf(Level.INFO, "Processing record %s", r.getRecordNumber());
            }

        }

        return ls;

    }

}
