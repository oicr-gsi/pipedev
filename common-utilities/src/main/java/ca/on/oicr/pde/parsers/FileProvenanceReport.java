package ca.on.oicr.pde.parsers;

import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

public class FileProvenanceReport {

    private final static Logger log = Logger.getLogger(FileProvenanceReport.class);

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

    public static List<FileProvenanceReportRecord> parseFileProvenanceReport(Reader reportReader, HeaderValidationMode mode) throws IOException {

        CSVFormat csvFormat = CSVFormat.RFC4180.withHeader().withDelimiter('\t');

        CSVParser p = new CSVParser(reportReader, csvFormat);

        // This will throw a runtime exception (ValidationException) if the headers to not match the expected header format
        try {
            validateHeader(p.getHeaderMap());
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
        List ls = new ArrayList<FileProvenanceReportRecord>();
        for (CSVRecord r : p) {

            FileProvenanceReportRecord.Builder rec = new FileProvenanceReportRecord.Builder();

            rec.setLastModified(r.get("Last Modified"));
            rec.setStudyTitle(r.get("Study Title"));
            rec.setStudySwid(r.get("Study SWID"));
            rec.setStudyAttributes(r.get("Study Attributes"));
            rec.setExperimentName(r.get("Experiment Name"));
            rec.setExperimentSwid(r.get("Experiment SWID"));
            rec.setExperimentAttributes(r.get("Experiment Attributes"));
            rec.setParentSampleName(r.get("Parent Sample Name"));
            rec.setParentSampleSwid(r.get("Parent Sample SWID"));
            rec.setParentSampleAttributes(r.get("Parent Sample Attributes"));
            rec.setSampleName(r.get("Sample Name"));
            rec.setSampleSwid(r.get("Sample SWID"));
            rec.setSampleAttributes(r.get("Sample Attributes"));
            rec.setSequencerRunName(r.get("Sequencer Run Name"));
            rec.setSequencerRunSwid(r.get("Sequencer Run SWID"));
            rec.setSequencerRunAttributes(r.get("Sequencer Run Attributes"));
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

        }
        log.debug("Completed transform of file provenance csv to FileProvenanceRecord objects");

        return ls;

    }

}
