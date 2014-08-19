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
        fileProvenanceReportExpectedHeader.put("Last Modified", 0);
        fileProvenanceReportExpectedHeader.put("Study Title", 1);
        fileProvenanceReportExpectedHeader.put("Study SWID", 2);
        fileProvenanceReportExpectedHeader.put("Study Attributes", 3);
        fileProvenanceReportExpectedHeader.put("Experiment Name", 4);
        fileProvenanceReportExpectedHeader.put("Experiment SWID", 5);
        fileProvenanceReportExpectedHeader.put("Experiment Attributes", 6);
        fileProvenanceReportExpectedHeader.put("Root Sample Name", 7);
        fileProvenanceReportExpectedHeader.put("Root Sample SWID", 8);
        fileProvenanceReportExpectedHeader.put("Parent Sample Name", 9);
        fileProvenanceReportExpectedHeader.put("Parent Sample SWID", 10);
        fileProvenanceReportExpectedHeader.put("Parent Sample Organism IDs", 11);
        fileProvenanceReportExpectedHeader.put("Parent Sample Attributes", 12);
        fileProvenanceReportExpectedHeader.put("Sample Name", 13);
        fileProvenanceReportExpectedHeader.put("Sample SWID", 14);
        fileProvenanceReportExpectedHeader.put("Sample Organism ID", 15);
        fileProvenanceReportExpectedHeader.put("Sample Organism Code", 16);
        fileProvenanceReportExpectedHeader.put("Sample Attributes", 17);
        fileProvenanceReportExpectedHeader.put("Sequencer Run Name", 18);
        fileProvenanceReportExpectedHeader.put("Sequencer Run SWID", 19);
        fileProvenanceReportExpectedHeader.put("Sequencer Run Attributes", 20);
        fileProvenanceReportExpectedHeader.put("Sequencer Run Platform ID", 21);
        fileProvenanceReportExpectedHeader.put("Sequencer Run Platform Name", 22);
        fileProvenanceReportExpectedHeader.put("Lane Name", 23);
        fileProvenanceReportExpectedHeader.put("Lane Number", 24);
        fileProvenanceReportExpectedHeader.put("Lane SWID", 25);
        fileProvenanceReportExpectedHeader.put("Lane Attributes", 26);
        fileProvenanceReportExpectedHeader.put("IUS Tag", 27);
        fileProvenanceReportExpectedHeader.put("IUS SWID", 28);
        fileProvenanceReportExpectedHeader.put("IUS Attributes", 29);
        fileProvenanceReportExpectedHeader.put("Workflow Name", 30);
        fileProvenanceReportExpectedHeader.put("Workflow Version", 31);
        fileProvenanceReportExpectedHeader.put("Workflow SWID", 32);
        fileProvenanceReportExpectedHeader.put("Workflow Run Name", 33);
        fileProvenanceReportExpectedHeader.put("Workflow Run Status", 34);
        fileProvenanceReportExpectedHeader.put("Workflow Run SWID", 35);
        fileProvenanceReportExpectedHeader.put("Processing Algorithm", 36);
        fileProvenanceReportExpectedHeader.put("Processing SWID", 37);
        fileProvenanceReportExpectedHeader.put("Processing Attributes", 38);
        fileProvenanceReportExpectedHeader.put("File Meta-Type", 39);
        fileProvenanceReportExpectedHeader.put("File SWID", 40);
        fileProvenanceReportExpectedHeader.put("File Attributes", 41);
        fileProvenanceReportExpectedHeader.put("File Path", 42);
        fileProvenanceReportExpectedHeader.put("File Md5sum", 43);
        fileProvenanceReportExpectedHeader.put("File Size", 44);
        fileProvenanceReportExpectedHeader.put("File Description", 45);
        fileProvenanceReportExpectedHeader.put("Skip", 46);

        if (!fileProvenanceReportExpectedHeader.equals(header)) {

            log.warn("Expected header:\n" + fileProvenanceReportExpectedHeader.toString() + "\nActual header:\n" + header.toString());
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
        log.debug("Starting transform of file provenance csv to FileProvenanceRecord objects");
        List ls = new LinkedList<FileProvenanceReportRecord>();
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
            rec.setWorkflowRunName(r.get("Workflow Run Name"));
            rec.setWorkflowRunStatus(r.get("Workflow Run Status"));
            rec.setWorkflowRunSwid(r.get("Workflow Run SWID"));
            rec.setProcessingAlgorithm(r.get("Processing Algorithm"));
            rec.setProcessingSwid(r.get("Processing SWID"));
            rec.setProcessingAttributes(r.get("Processing Attributes"));
            rec.setFileMetaType(r.get("File Meta-Type"));
            rec.setFileSwid(r.get("File SWID"));
            rec.setFileAttributes(r.get("File Attributes"));
            rec.setFilePath(r.get("File Path"));
            rec.setFileMd5sum(r.get("File Md5sum"));
            rec.setFileSize(r.get("File Size"));
            rec.setFileDescription(r.get("File Description"));
            rec.setSkip(r.get("Skip"));

            ls.add(rec.build());

            if (r.getRecordNumber() % 10000 == 0) {
                log.printf(Level.INFO, "Processing record %s", r.getRecordNumber());
            }

        }
        log.debug("Completed transform of file provenance csv to FileProvenanceRecord objects");

        return ls;

    }

}
