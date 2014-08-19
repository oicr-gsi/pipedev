package ca.on.oicr.pde.dao;

import ca.on.oicr.pde.model.Accessionable;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.parsers.FileProvenanceReport;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SeqwareLocalDataImpl extends SeqwareService {
    File fileProvenanceReport;
    File workflowRunReport;

    public SeqwareLocalDataImpl(File seqwareSettingsFile, File fileProvenanceReport, File workflowRunReport) {
        super();
        this.fileProvenanceReport = fileProvenanceReport;
        this.workflowRunReport = workflowRunReport;
    }
    
    @Override
    protected void updateFileProvenanceRecords() {
        try {
            fprs = FileProvenanceReport.parseFileProvenanceReport(new FileReader(fileProvenanceReport), FileProvenanceReport.HeaderValidationMode.SKIP);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    protected void updateWorkflowRunRecords(Workflow workflow) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, String> getWorkflowRunIni(WorkflowRun workflowRun) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected List<Accessionable> getWorkflowRunInputFiles(WorkflowRun workflowRun) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
