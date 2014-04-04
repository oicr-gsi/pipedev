package ca.on.oicr.pde.parsers;

import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class WorkflowRunReport {

    private static void validateHeader(Map header) {

        Map workflowRunReportExpectedHeader = new HashMap<String, Integer>();
        workflowRunReportExpectedHeader.put("Workflow", 0);
        workflowRunReportExpectedHeader.put("Workflow Run SWID", 1);
        workflowRunReportExpectedHeader.put("Workflow Run Status", 2);
        workflowRunReportExpectedHeader.put("Workflow Run Create Timestamp", 3);
        workflowRunReportExpectedHeader.put("Workflow Run Host", 4);
        workflowRunReportExpectedHeader.put("Workflow Run Working Dir", 5);
        workflowRunReportExpectedHeader.put("Workflow Run Engine ID", 6);
        workflowRunReportExpectedHeader.put("Library Sample Names", 7);
        workflowRunReportExpectedHeader.put("Library Sample SWIDs", 8);
        workflowRunReportExpectedHeader.put("Identity Sample Names", 9);
        workflowRunReportExpectedHeader.put("Identity Sample SWIDs", 10);
        workflowRunReportExpectedHeader.put("Input File Meta-Types", 11);
        workflowRunReportExpectedHeader.put("Input File SWIDs", 12);
        workflowRunReportExpectedHeader.put("Input File Paths", 13);
        workflowRunReportExpectedHeader.put("Immediate Input File Meta-Types", 14);
        workflowRunReportExpectedHeader.put("Immediate Input File SWIDs", 15);
        workflowRunReportExpectedHeader.put("Immediate Input File Paths", 16);
        workflowRunReportExpectedHeader.put("Output File Meta-Types", 17);
        workflowRunReportExpectedHeader.put("Output File SWIDs", 18);
        workflowRunReportExpectedHeader.put("Output File Paths", 19);
        workflowRunReportExpectedHeader.put("Workflow Run Time", 20);

        if (!workflowRunReportExpectedHeader.equals(header)) {
            System.out.println("Expected header:\n" + workflowRunReportExpectedHeader.toString() + "\nActual header:\n" + header.toString());
            throw new RuntimeException("Header is not valid.");
        }

    }
    
    public static List<WorkflowRunReportRecord> parseWorkflowRunReport(InputStream tsvReportStream) throws IOException {
        
        return parseWorkflowRunReport(new InputStreamReader(tsvReportStream));
        
    }

    public static List<WorkflowRunReportRecord> parseWorkflowRunReport(Reader tsvReport) throws IOException {

        //CSVFormat csvFormat = CSVFormat.RFC4180.withHeader().withDelimiter('\t');
        //FIX: there is an extra tab in the current seqware 
        CSVFormat csvFormat = CSVFormat.RFC4180.withHeader("Workflow","Workflow Run SWID","Workflow Run Status","Workflow Run Create Timestamp",
                "Workflow Run Host","Workflow Run Working Dir","Workflow Run Engine ID","Library Sample Names","Library Sample SWIDs","Identity Sample Names",
                "Identity Sample SWIDs","Input File Meta-Types","Input File SWIDs","Input File Paths","Immediate Input File Meta-Types",
                "Immediate Input File SWIDs","Immediate Input File Paths","Output File Meta-Types","Output File SWIDs","Output File Paths",
                "Workflow Run Time").withDelimiter('\t').withSkipHeaderRecord(true);
        CSVParser p = new CSVParser(tsvReport, csvFormat);

        validateHeader(p.getHeaderMap());

        List ls = new ArrayList<WorkflowRunReportRecord>();
        for (CSVRecord r : p) {

            if (!r.isConsistent()) {
                System.out.println(r.toString());
                throw new RuntimeException("Record " + r.getRecordNumber() + " does not conform to header format.");
            }

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

            ls.add(w);

        }

        return ls;

    }

   

}
