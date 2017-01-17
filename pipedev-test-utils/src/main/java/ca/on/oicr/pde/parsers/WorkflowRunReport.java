package ca.on.oicr.pde.parsers;

import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class WorkflowRunReport {

    public static List<WorkflowRunReportRecord> parseWorkflowRunReport(InputStream tsvReportStream) throws IOException {
        return parseWorkflowRunReport(new InputStreamReader(tsvReportStream));
    }

    public static List<WorkflowRunReportRecord> parseWorkflowRunReport(Reader tsvReport) throws IOException {

        CSVFormat csvFormat = CSVFormat.RFC4180.withFirstRecordAsHeader().withDelimiter('\t');
        CSVParser p = new CSVParser(tsvReport, csvFormat);

        List ls = new ArrayList<>();
        for (CSVRecord r : p) {
            WorkflowRunReportRecord w = new WorkflowRunReportRecord();
            w.setWorkflowName(r.get("Workflow"));
            w.setWorkflowRunSwid(r.get("Workflow Run SWID"));
            w.setWorkflowRunStatus(r.get("Workflow Run Status"));
            w.setWorkflowRunCreateTime(r.get("Workflow Run Create Timestamp"));
            w.setWorkflowRunHost(r.get("Workflow Run Host"));
            w.setWorkflowRunWorkingDir(r.get("Workflow Run Working Dir"));
            w.setWorkflowRunEngineId(r.get("Workflow Run Engine ID"));
            w.setLibrarySampleNames(r.get("Library Sample Names"));
            w.setLibrarySampleSwids(r.get("Library Sample SWIDs"));
            w.setIdentitySampleNames(r.get("Identity Sample Names"));
            w.setIdentitySampleSwids(r.get("Identity Sample SWIDs"));
            w.setInputFileMetaTypes(r.get("Input File Meta-Types"));
            w.setInputFileSwids(r.get("Input File SWIDs"));
            w.setInputFilePaths(r.get("Input File Paths"));
            w.setImmediateInputFileMetaTypes(r.get("Immediate Input File Meta-Types"));
            w.setImmediateInputFileSwids(r.get("Immediate Input File SWIDs"));
            w.setImmediateInputFilePaths(r.get("Immediate Input File Paths"));
            w.setOutputFileMetaTypes(r.get("Output File Meta-Types"));
            w.setOutputFileSwids(r.get("Output File SWIDs"));
            w.setOutputFilePaths(r.get("Output File Paths"));
            w.setWorkflowRunTime(r.get("Workflow Run Time"));

            if (r.isMapped("IUS-LimsKeys")) {
                w.setIusLimsKeys(r.get("IUS-LimsKeys"));
            }

            ls.add(w);
        }

        return ls;

    }

}
