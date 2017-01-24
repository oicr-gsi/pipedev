package ca.on.oicr.pde.client;

import ca.on.oicr.pde.experimental.PDEPluginRunner;
import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import ca.on.oicr.pde.parsers.WorkflowRunReport;
import com.google.common.primitives.Ints;
import io.seqware.common.model.ProcessingStatus;
import io.seqware.common.model.WorkflowRunStatus;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.model.FirstTierModel;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.model.WorkflowRun;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.common.util.maptools.MapTools;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.Sets;
import java.util.HashSet;
import net.sourceforge.seqware.common.model.File;
import net.sourceforge.seqware.common.model.Processing;

/**
 *
 * @author mlaszloffy
 */
public class MetadataBackedSeqwareClient implements SeqwareClient {

    private final Metadata metadata;
    private final PDEPluginRunner runner;

    public MetadataBackedSeqwareClient(Metadata metadata, Map config) {
        this.metadata = metadata;
        this.runner = new PDEPluginRunner(config, metadata);
    }

    @Override
    public <T extends FirstTierModel> WorkflowRun createWorkflowRun(Workflow workflow, Set<IUS> limsKeys, Collection<T> parents, Set<Processing> processings) {
        checkNotNull(workflow);
        checkNotNull(limsKeys);
        checkNotNull(parents);
        checkNotNull(processings);

        List<Integer> parentSwids = new ArrayList<>();
        for (T so : parents) {
            parentSwids.add(so.getSwAccession());
        }

        Integer workflowRunId = metadata.add_workflow_run(workflow.getSwAccession());
        Integer workflowRunSwid = metadata.get_workflow_run_accession(workflowRunId);

        //Link workflow run (and all associated files to IUS/LimsKey
        for (IUS ius : limsKeys) {
            try {
                metadata.linkWorkflowRunAndParent(workflowRunId, ius.getSwAccession());
            } catch (SQLException se) {
                throw new RuntimeException(se);
            }
        }

        //Create "start" processing, link it to parents and the workflow run       
        Integer startProcessingId = metadata.add_empty_processing_event_by_parent_accession(Ints.toArray(parentSwids)).getReturnValue();
        Integer startProcessingSwid = metadata.mapProcessingIdToAccession(startProcessingId);
        metadata.update_processing_status(startProcessingId, ProcessingStatus.success);
        metadata.update_processing_workflow_run(startProcessingId, workflowRunSwid);

        for (Processing processing : processings) {
            Set<FileMetadata> files = new HashSet<>();
            for (File f : processing.getFiles()) {
                FileMetadata fileMetadata = new FileMetadata();
                fileMetadata.setDescription(f.getDescription() == null ? "empty" : f.getDescription());
                fileMetadata.setFilePath(f.getFilePath() == null ? "empty" : f.getFilePath());
                fileMetadata.setMd5sum(f.getMd5sum() == null ? "empty" : f.getMd5sum());
                fileMetadata.setMetaType(f.getMetaType() == null ? "empty" : f.getMetaType());
                fileMetadata.setSize(f.getSize() == null ? -1L : f.getSize());
                fileMetadata.setType(f.getType() == null ? "empty" : f.getType());
                files.add(fileMetadata);
            }

            Set<Integer> processingParentSwids = new HashSet<>();
            processingParentSwids.add(startProcessingSwid);
            for (FirstTierModel i : processing.getIUS()) {
                processingParentSwids.add(i.getSwAccession());
            }

            Integer processingId = metadata.add_empty_processing_event_by_parent_accession(Ints.toArray(processingParentSwids)).getReturnValue();

            if (!files.isEmpty()) {
                ReturnValue newRet = new ReturnValue();
                newRet.setFiles(new ArrayList<>(files));
                metadata.update_processing_event(processingId, newRet);
            }

            metadata.add_workflow_run_ancestor(workflowRunSwid, processingId);
            metadata.update_processing_status(processingId, ProcessingStatus.success);
        }

        WorkflowRun wr = metadata.getWorkflowRun(workflowRunSwid);
        wr.setStatus(WorkflowRunStatus.completed);
        ReturnValue rv = metadata.update_workflow_run(wr.getWorkflowRunId(), wr.getCommand(), wr.getTemplate(), wr.getStatus(), wr.getStatusCmd(), wr.getCurrentWorkingDir(), wr.getDax(), wr.getIniFile(), wr.getHost(), wr.getStdErr(), wr.getStdOut(), wr.getWorkflowEngine(), wr.getInputFileAccessions());

        return metadata.getWorkflowRun(rv.getReturnValue());
    }

    @Override
    public <T extends FirstTierModel> WorkflowRun createWorkflowRun(Workflow workflow, Set<IUS> limsKeys, Collection<T> parents, List<FileMetadata> files) {

        checkNotNull(workflow);
        checkNotNull(limsKeys);
        checkNotNull(parents);
        checkNotNull(files);

        int id = 1;
        List<File> newFiles = new ArrayList<>();
        for (FileMetadata fileMetadata : files) {
            File file = new File();
            file.setFileId(id++);
            file.setDescription(fileMetadata.getDescription());
            file.setFilePath(fileMetadata.getFilePath());
            file.setMd5sum(fileMetadata.getMd5sum());
            file.setMetaType(fileMetadata.getMetaType());
            file.setSize(fileMetadata.getSize());
            file.setType(fileMetadata.getType());
            newFiles.add(file);
        }

        Processing p = new Processing();
        p.setProcessingId(id++);
        p.setAlgorithm("provision_file_out");
        p.setFiles(Sets.newHashSet(newFiles)); //link file to processing

        return createWorkflowRun(workflow, limsKeys, parents, Sets.newHashSet(p));
    }

    @Override
    public Workflow createWorkflow(String name, String version, String description) {
        return createWorkflow(name, version, description, Collections.EMPTY_MAP);
    }

    @Override
    public Workflow createWorkflow(String name, String version, String description, Map<String, String> defaultParameters) {

        checkNotNull(name);
        checkNotNull(version);
        checkNotNull(description);
        checkNotNull(defaultParameters);

        Properties p = new Properties();
        p.putAll(defaultParameters);

        java.io.File defaultParametersFile = null;
        try {
            defaultParametersFile = java.io.File.createTempFile("defaults", ".ini");
            p.store(FileUtils.openOutputStream(defaultParametersFile), "");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        ReturnValue rv = metadata.addWorkflow(name, version, description,
                null, defaultParametersFile.getAbsolutePath(),
                "", "/tmp", true, null, false, "/tmp", "java", "Oozie", "1.x");

        //TODO: rv.getReturnValue() does not equal "SUCCESS"
        if (rv == null || rv.getAttribute("sw_accession") == null) {
            throw new RuntimeException("Could not create workflow");
        }

        return metadata.getWorkflow(Integer.parseInt(rv.getAttribute("sw_accession")));
    }

    @Override
    public Map<String, String> getWorkflowRunIni(WorkflowRun workflowRun) {
        WorkflowRun wr = metadata.getWorkflowRun(workflowRun.getSwAccession());
        return MapTools.iniString2Map(wr.getIniFile());
    }

    @Override
    public List<Integer> getWorkflowRunInputFiles(WorkflowRun workflowRun) {
        List<Integer> fileAccessions = new ArrayList<>();
        WorkflowRun wr = metadata.getWorkflowRun(workflowRun.getSwAccession());
        fileAccessions.addAll(wr.getInputFileAccessions());
        return fileAccessions;
    }

    @Override
    public List<Integer> getParentAccession(WorkflowRun workflowRun) {
        Map<String, String> ini = getWorkflowRunIni(workflowRun);
        StringTokenizer st = new StringTokenizer(ini.get("parent-accessions"), ",");
        List<Integer> ps = new ArrayList<>();
        while (st.hasMoreTokens()) {
            ps.add(Integer.parseInt(st.nextToken()));
        }
        return ps;
    }

    @Override
    public List<WorkflowRunReportRecord> getWorkflowRunRecords(Workflow workflow) {
        List<WorkflowRunReportRecord> workflowRunReportRecords = null;
        try {
            InputStream is = IOUtils.toInputStream(metadata.getWorkflowRunReport(workflow.getSwAccession(), null, null));
            workflowRunReportRecords = WorkflowRunReport.parseWorkflowRunReport(is);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return workflowRunReportRecords;
    }

    @Override
    public IUS addLims(String provider, String id, String version, DateTime lastModified) {
        Integer limsKeySwid = metadata.addLimsKey(provider, id, version, lastModified);
        Integer iusSwid = metadata.addIUS(limsKeySwid, false);
        return metadata.getIUS(iusSwid);
    }

}
