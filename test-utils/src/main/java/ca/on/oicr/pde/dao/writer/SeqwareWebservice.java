package ca.on.oicr.pde.dao.writer;

import ca.on.oicr.pde.experimental.PDEPluginRunner;
import ca.on.oicr.pde.model.Experiment;
import ca.on.oicr.pde.model.Ius;
import ca.on.oicr.pde.model.Lane;
import ca.on.oicr.pde.model.Sample;
import ca.on.oicr.pde.model.SequencerRun;
import ca.on.oicr.pde.model.SeqwareObject;
import ca.on.oicr.pde.model.Study;
import ca.on.oicr.pde.model.Workflow;
import static com.google.common.base.Preconditions.*;
import com.google.common.primitives.Ints;
import io.seqware.common.model.ProcessingStatus;
import io.seqware.common.model.SequencerRunStatus;
import io.seqware.common.model.WorkflowRunStatus;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataFactory;
import net.sourceforge.seqware.common.model.WorkflowRun;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author mlaszloffy
 */
public class SeqwareWebservice implements SeqwareWriteService {

    private final Metadata metadata;
    private final PDEPluginRunner runner;

    public SeqwareWebservice(Map config) {
        metadata = MetadataFactory.getWS(config);
        runner = new PDEPluginRunner(config);
    }

    @Override
    public Experiment createExperiment(String description, String platformId, Study study, String title) {

        checkNotNull(platformId);
        checkNotNull(study);
        checkNotNull(title);

        ReturnValue rv = metadata.addExperiment(Integer.parseInt(study.getSwid()), Integer.parseInt(platformId), sanitize(description), title, null, null);

        if (!isReturnValueValid(rv)) {
            return null;
        }

        Experiment.Builder b = new Experiment.Builder();
        b.setSwid(rv.getAttribute("sw_accession"));
        return b.build();
    }

    @Override
    public Ius createIus(String barcode, String description, Lane lane, String name, Sample sample, boolean skip) {

        checkNotNull(barcode);
        checkNotNull(lane);
        checkNotNull(sample);
        checkNotNull(skip);

        ReturnValue rv = metadata.addIUS(Integer.parseInt(lane.getSwid()), Integer.parseInt(sample.getSwid()), sanitize(name), sanitize(description), barcode, skip);

        if (!isReturnValueValid(rv)) {
            return null;
        }

        Ius.Builder b = new Ius.Builder();
        b.setSwid(rv.getAttribute("sw_accession"));
        return b.build();
    }

    @Override
    public Lane createLane(String cycleDescriptor, String description, String laneNumber, String librarySelectionId, String librarySourceId, String libraryStrategyId, String name, SequencerRun sequencerRun, boolean skip, String studyTypeId) {

        checkNotNull(laneNumber);
        checkNotNull(sequencerRun);
        checkNotNull(studyTypeId);
        checkNotNull(skip);
        checkNotNull(librarySourceId);
        checkNotNull(librarySelectionId);
        checkNotNull(libraryStrategyId);

        ReturnValue rv = metadata.addLane(Integer.parseInt(sequencerRun.getSwid()), Integer.parseInt(studyTypeId), Integer.parseInt(libraryStrategyId), Integer.parseInt(librarySelectionId), Integer.parseInt(librarySourceId), sanitize(name), sanitize(description), sanitize(cycleDescriptor), skip, Integer.parseInt(laneNumber));

        if (!isReturnValueValid(rv)) {
            return null;
        }

        Lane.Builder b = new Lane.Builder();
        b.setSwid(rv.getAttribute("sw_accession"));
        return b.build();
    }

    @Override
    public Sample createSample(String description, Experiment experiment, String organismId, String title, Sample parentSample) {

        checkNotNull(experiment);
        checkNotNull(organismId);
        checkNotNull(title);

        Integer parentSampleId = parentSample != null ? Integer.parseInt(parentSample.getSwid()) : 0;

        ReturnValue rv = metadata.addSample(Integer.parseInt(experiment.getSwid()), parentSampleId, Integer.parseInt(organismId), sanitize(description), title);

        if (!isReturnValueValid(rv)) {
            return null;
        }

        Sample.Builder b = new Sample.Builder();
        b.setSwid(rv.getAttribute("sw_accession"));
        return b.build();
    }

    @Override
    public SequencerRun createSequencerRun(String description, String filePath, String name, boolean isPairedEnd, String platformId, boolean skip) {

        checkNotNull(filePath);
        checkNotNull(name);
        checkNotNull(isPairedEnd);
        checkNotNull(platformId);
        checkNotNull(skip);

        ReturnValue rv = metadata.addSequencerRun(Integer.parseInt(platformId), name, sanitize(description), isPairedEnd, skip, filePath, SequencerRunStatus.Complete);

        if (!isReturnValueValid(rv)) {
            return null;
        }

        SequencerRun.Builder b = new SequencerRun.Builder();
        b.setSwid(rv.getAttribute("sw_accession"));
        return b.build();
    }

    @Override
    public Study createStudy(String id, String centerName, String centerProjectName, String description, String studyType, String title) {
        //TODO: id is not used - how to set it in seqware?
        checkNotNull(centerName);
        checkNotNull(centerProjectName);
        checkNotNull(studyType);
        checkNotNull(title);

        ReturnValue rv = metadata.addStudy(title, sanitize(description), centerName, centerProjectName, Integer.parseInt(studyType));

        if (!isReturnValueValid(rv)) {
            return null;
        }

        Study.Builder b = new Study.Builder();
        b.setSwid(rv.getAttribute("sw_accession"));
        return b.build();
    }

    @Override
    public String createWorkflowRun(Workflow workflow, Collection<? extends SeqwareObject> parents, List<FileInfo> files) {

        checkNotNull(workflow);
        checkNotNull(parents);
        checkNotNull(files);

        ArrayList<FileMetadata> files2 = new ArrayList<>();
        for (FileInfo f : files) {
            FileMetadata fm = new FileMetadata(f.getFilePath(), f.getType(), f.getMetaType(), null);
            files2.add(fm);
        }

        List<Integer> parentSwids = new ArrayList<>();
        for (SeqwareObject so : parents) {
            parentSwids.add(Integer.parseInt(so.getSwid()));
        }

        Integer workflowRunId = metadata.add_workflow_run(Integer.parseInt(workflow.getSwid()));
        Integer workflowRunSwid = metadata.get_workflow_run_accession(workflowRunId);
        ReturnValue processingRv = metadata.add_empty_processing_event_by_parent_accession(Ints.toArray(parentSwids));
        Integer processingId = processingRv.getReturnValue();
        ReturnValue newRet = new ReturnValue();
        newRet.setFiles(files2);
        metadata.update_processing_event(processingId, newRet);
        //Integer mapProcessingIdToSwid = metadata.mapProcessingIdToAccession(processingId);
        metadata.update_processing_workflow_run(processingId, workflowRunSwid);
        metadata.update_processing_status(processingId, ProcessingStatus.success);
        WorkflowRun wr = metadata.getWorkflowRun(workflowRunSwid);
        wr.setStatus(WorkflowRunStatus.completed);
        //wr.setStdOut("");
        //wr.setStdErr("");
        ReturnValue rv = metadata.update_workflow_run(wr.getWorkflowRunId(), wr.getCommand(), wr.getTemplate(), wr.getStatus(), wr.getStatusCmd(), wr.getCurrentWorkingDir(), wr.getDax(), wr.getIniFile(), wr.getHost(), wr.getStdErr(), wr.getStdOut(), wr.getWorkflowEngine(), wr.getInputFileAccessions());
        //return rv.getAttribute("sw_accession");
        return Integer.toString(rv.getReturnValue());
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

        File defaultParametersFile = null;
        try {
            defaultParametersFile = File.createTempFile("defaults", ".ini");
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

        Workflow.Builder b = new Workflow.Builder();
        b.setName(name);
        b.setVersion(version);
        b.setSwid(rv.getAttribute("sw_accession"));
//        Workflow w = b.build();

//        net.sourceforge.seqware.common.model.Workflow wf = metadata.getWorkflow(Integer.parseInt(w.getSwid()));
//        wf.setWorkflowParams(null);
        return b.build();
    }

    @Override
    public void annotate(SeqwareObject o, String key, String value) {

        checkNotNull(o);
        checkNotNull(key);
        checkNotNull(value);

        List<String> params = new ArrayList(Arrays.asList(
                "--plugin",
                "net.sourceforge.seqware.pipeline.plugins.AttributeAnnotator",
                "--",
                "--" + o.getTableName() + "-accession", o.getSwid(),
                "--key", key,
                "--val", value
        ));

        //io.seqware.cli.Main.annotate
        ReturnValue rv = runner.runPlugin(params.toArray(new String[params.size()]));

        //System.out.println("annotation rv = " + rv.toString());
    }

    @Override
    public void annotate(SeqwareObject o, boolean skip, String value) {

        checkNotNull(o);
        checkNotNull(skip);

        List<String> params = new ArrayList(Arrays.asList(
                "--plugin",
                "net.sourceforge.seqware.pipeline.plugins.AttributeAnnotator",
                "--",
                "--" + o.getTableName() + "-accession", o.getSwid(),
                "--skip", Boolean.toString(skip)
        ));

        if (value != null && !value.isEmpty()) {
            params.add("--value");
            params.add(value);
        }

        //io.seqware.cli.Main.annotate
        System.out.println(params);
        ReturnValue rv = runner.runPlugin(params.toArray(new String[params.size()]));

        //System.out.println("annotation rv = " + rv.toString());
    }

    @Override
    public void updateFileReport() {
        try {
            metadata.fileProvenanceReportTrigger();
        } catch (NullPointerException npe) {
            //TODO: when running the seqware webservice using jetty, the following returns an NPE
        }
    }

    private boolean isReturnValueValid(ReturnValue rv) {

        if (rv == null) {
            return false;
        }

        if (rv.getReturnValue() != ReturnValue.SUCCESS) {
            return false;
        }

        if (rv.getAttribute("sw_accession") == null) {
            return false;
        }

        //rv is valid
        return true;
    }

    private String sanitize(String input) {
        if (input == null) {
            return " ";
        }
        return input;
    }

}
