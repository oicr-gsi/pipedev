package ca.on.oicr.pde.dao.reader;

import ca.on.oicr.pde.model.Accessionable;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.parsers.FileProvenanceReport;
import ca.on.oicr.pde.parsers.WorkflowRunReport;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.util.maptools.MapTools;

/**
 *
 * @author mlaszloffy
 */
public final class SeqwareMetadbImpl extends SeqwareReadService {
    
    private Metadata seqwareMetadb;

    /**
     *
     * @param seqwareSettingsFile
     */
    public SeqwareMetadbImpl(File seqwareSettingsFile) {
        updateFileProvenanceRecords();
    }

    @Override
    public void updateFileProvenanceRecords() {
        try {
            Writer w = new StringWriter();
            seqwareMetadb.fileProvenanceReport(Collections.EMPTY_MAP, w);
            fileProvenanceReportRecords = FileProvenanceReport.parseFileProvenanceReport(new StringReader(w.toString()), FileProvenanceReport.HeaderValidationMode.SKIP);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     *
     * @param workflow
     */
    @Override
    public void updateWorkflowRunRecords(Workflow workflow) {
        try {
            String report = seqwareMetadb.getWorkflowRunReport(Integer.parseInt(workflow.getSwid()), null, null);
            workflowToWorkflowRunReportRecords.put(workflow, WorkflowRunReport.parseWorkflowRunReport(new StringReader(report)));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     *
     * @param workflowRun
     * @return
     */
    @Override
    public Map<String, String> getWorkflowRunIni(WorkflowRun workflowRun) {
        net.sourceforge.seqware.common.model.WorkflowRun wr = seqwareMetadb.getWorkflowRun(Integer.parseInt(workflowRun.getSwid()));
        return MapTools.iniString2Map(wr.getIniFile());
    }

    /**
     *
     * @param workflowRun
     * @return
     */
    @Override
    protected List<Accessionable> getWorkflowRunInputFiles(WorkflowRun workflowRun) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
