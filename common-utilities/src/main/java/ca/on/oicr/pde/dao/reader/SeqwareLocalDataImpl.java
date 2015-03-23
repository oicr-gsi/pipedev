package ca.on.oicr.pde.dao.reader;

import ca.on.oicr.pde.model.Accessionable;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.parsers.FileProvenanceReport;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A Seqware service (in development) that does not require a Seqware webservice or database.
 * 
 * {@inheritDoc}
 *
 * The service is backed by a file provenance report and workflow run report CSV file.
 */
public class SeqwareLocalDataImpl extends SeqwareReadService {

    private final File fileProvenanceReport;
    private final File workflowRunReport;

    /**
     * Creates a seqware service that is 
     * @param seqwareSettingsFile the File with the seqware settings.
     * @param fileProvenanceReport the file provenance report CSV file.
     * @param workflowRunReport the workflow run report CSV file.
     */
    public SeqwareLocalDataImpl(File seqwareSettingsFile, File fileProvenanceReport, File workflowRunReport) {
        this.fileProvenanceReport = fileProvenanceReport;
        this.workflowRunReport = workflowRunReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateFileProvenanceRecords() {
        try {
            fileProvenanceReportRecords = FileProvenanceReport.parseFileProvenanceReport(new FileReader(fileProvenanceReport), FileProvenanceReport.HeaderValidationMode.SKIP);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateWorkflowRunRecords(Workflow workflow) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param workflowRun
     * @return
     */
    @Override
    public Map<String, String> getWorkflowRunIni(WorkflowRun workflowRun) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param workflowRun
     * @return
     */
    @Override
    protected List<Accessionable> getWorkflowRunInputFiles(WorkflowRun workflowRun) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
