package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.ExtendedProvenanceClient;
import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import ca.on.oicr.pinery.api.RunPosition;
import ca.on.oicr.pinery.lims.DefaultRun;
import ca.on.oicr.pinery.lims.DefaultRunPosition;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.module.FileMetadata;
import net.sourceforge.seqware.common.module.ReturnValue;
import org.apache.logging.log4j.LogManager;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author mlaszloffy
 */
public class MergingDeciderBase extends DeciderBase {

    @BeforeMethod(dependsOnGroups = "setup", dependsOnMethods = "setupData")
    public void setupIus() {

        RunPosition lane2 = new DefaultRunPosition();
        lane2.setPosition(2);
        lane2.setRunSample(Sets.newHashSet(runSample));

        run = new DefaultRun();
        run.setId(1);
        run.setName("ABC_123");
        run.setSample(Sets.newHashSet(lane, lane2));

        when(limsMock.getSampleProjects()).thenReturn(Lists.newArrayList(project));
        when(limsMock.getSamples(null, null, null, null, null)).thenReturn(samples);
        when(limsMock.getRuns()).thenReturn(Lists.newArrayList(run));

        //check preconditions
        assertEquals(provenanceClient.getSampleProvenance().size(), 2);
        assertEquals(provenanceClient.getFileProvenance().size(), 0);
    }

    @BeforeMethod(dependsOnGroups = "setup", dependsOnMethods = "setupIus")
    public void setupWorkflows() {
        List<SampleProvenance> sps = Lists.newArrayList(provenanceClient.getSampleProvenance());

        //setup files in seqware
        Workflow workflow1 = seqwareClient.createWorkflow("TestWorkflow1", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        SampleProvenance sp;
        IUS ius;
        FileMetadata file;

        sp = sps.get(0);
        ius = seqwareClient.addLims("pinery", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/file1.bam");
        file.setMetaType("text/plain");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(workflow1, Sets.newHashSet(ius), Collections.EMPTY_LIST, Arrays.asList(file));

        sp = sps.get(1);
        ius = seqwareClient.addLims("pinery", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/file2.bam");
        file.setMetaType("text/plain");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(workflow1, Sets.newHashSet(ius), Collections.EMPTY_LIST, Arrays.asList(file));
    }

    @Test
    public void defaultMergingBehaviourTest() {
        assertEquals(provenanceClient.getSampleProvenance().size(), 2);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        //setup downstream workflow and decider
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        MergingDecider decider = new MergingDeciderImpl(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        run(decider, Arrays.asList("--all", "--dry-run", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        run(decider, Arrays.asList("--all", "--no-metadata", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        System.out.println(Joiner.on("\n").join(provenanceClient.getFileProvenance()));

        run(decider, Arrays.asList("--all", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        System.out.println(Joiner.on("\n").join(provenanceClient.getFileProvenance()));
        assertEquals(provenanceClient.getFileProvenance().size(), 4);
    }

    @Test
    public void noGroupingTest() {
        assertEquals(provenanceClient.getSampleProvenance().size(), 2);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        //setup downstream workflow and decider
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        MergingDecider decider = new MergingDeciderImpl(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        run(decider, Arrays.asList("--all", "--dry-run", "--group-by", "FILE_SWA", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 2);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        run(decider, Arrays.asList("--all", "--no-metadata", "--group-by", "FILE_SWA", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 2);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        run(decider, Arrays.asList("--all", "--group-by", "FILE_SWA", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 2);
        assertEquals(provenanceClient.getFileProvenance().size(), 4);
    }

    @Test
    public void noGroupingTest2() {
        assertEquals(provenanceClient.getSampleProvenance().size(), 2);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        //setup downstream workflow and decider
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        MergingDecider decider = new MergingDeciderImpl(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        run(decider, Arrays.asList("--all", "--dry-run", "--group-by", "IUS_SWA", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 2);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        run(decider, Arrays.asList("--all", "--no-metadata", "--group-by", "IUS_SWA", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 2);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        run(decider, Arrays.asList("--all", "--group-by", "IUS_SWA", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 2);
        assertEquals(provenanceClient.getFileProvenance().size(), 4);
    }

    private class MergingDeciderImpl extends MergingDecider {

        public MergingDeciderImpl(ExtendedProvenanceClient provenanceClient) {
            super(LogManager.getLogger(MergingDeciderBase.MergingDeciderImpl.class), provenanceClient);
        }

        @Override
        protected boolean checkFilePassesFilterBeforeGrouping(FileAttributes fileAttributes) {
            return true;
        }

        @Override
        protected boolean checkFilePassesFilterAfterGrouping(FileAttributes fileAttributes) {
            return true;
        }

        @Override
        protected ReturnValue customizeWorkflowRun(WorkflowRun run) {
            return new ReturnValue();
        }

    }

}
