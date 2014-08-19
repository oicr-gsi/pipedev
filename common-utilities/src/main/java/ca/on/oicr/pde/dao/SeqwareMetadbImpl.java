package ca.on.oicr.pde.dao;

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
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import net.sourceforge.seqware.common.util.maptools.MapTools;

public final class SeqwareMetadbImpl extends SeqwareService {
    Metadata seqwareMetadb;

    public SeqwareMetadbImpl(File seqwareSettingsFile) {
        super();
        //seqwareMetadb = MetadataFactory.get(seqwareSettings);
        updateFileProvenanceRecords();
    }

    @Override
    protected void updateFileProvenanceRecords() {
        try {
            Writer w = new StringWriter();
            seqwareMetadb.fileProvenanceReport(Collections.EMPTY_MAP, w);
            fprs = FileProvenanceReport.parseFileProvenanceReport(new StringReader(w.toString()), FileProvenanceReport.HeaderValidationMode.SKIP);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    protected void updateWorkflowRunRecords(Workflow workflow) {
        try {
            String report = seqwareMetadb.getWorkflowRunReport(Integer.parseInt(workflow.getSwid()), null, null);
            wrrs.put(workflow, WorkflowRunReport.parseWorkflowRunReport(new StringReader(report)));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public Map<String, String> getWorkflowRunIni(WorkflowRun workflowRun) {
        net.sourceforge.seqware.common.model.WorkflowRun wr = seqwareMetadb.getWorkflowRun(Integer.parseInt(workflowRun.getSwid()));
        return MapTools.iniString2Map(wr.getIniFile());
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
