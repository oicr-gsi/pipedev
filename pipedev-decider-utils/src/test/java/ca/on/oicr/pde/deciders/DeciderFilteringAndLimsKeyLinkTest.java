package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.AnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.DefaultProvenanceClient;
import ca.on.oicr.gsi.provenance.ExtendedProvenanceClient;
import ca.on.oicr.gsi.provenance.LaneProvenanceProvider;
import ca.on.oicr.gsi.provenance.SampleProvenanceProvider;
import ca.on.oicr.gsi.provenance.SeqwareMetadataAnalysisProvenanceProvider;
import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import ca.on.oicr.pde.client.MetadataBackedSeqwareClient;
import ca.on.oicr.pde.client.SeqwareClient;
import ca.on.oicr.pinery.api.Run;
import ca.on.oicr.pinery.api.RunPosition;
import ca.on.oicr.pinery.api.RunSample;
import ca.on.oicr.pinery.api.Sample;
import ca.on.oicr.pinery.api.SampleProject;
import ca.on.oicr.pinery.client.PineryClient;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.metadata.MetadataInMemory;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.model.WorkflowRun;
import net.sourceforge.seqware.common.module.FileMetadata;
import org.joda.time.DateTime;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.powermock.reflect.Whitebox;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author mlaszloffy
 */
public class DeciderFilteringAndLimsKeyLinkTest {

    List<WorkflowRun> inputFileWorkflowRuns = new ArrayList<>();
    Workflow mergeBams;

    private ExtendedProvenanceClient provenanceClient;
    private AnalysisProvenanceProvider app;
    private SampleProvenanceProvider spp;
    private LaneProvenanceProvider lpp;

    private SampleProvenance sp1;
    private SampleProvenance sp2;
    private SampleProvenance sp3;

    private final String limsProvider = "mock-lims";

    protected List<Sample> samples;
    protected SampleProject project;
    protected Sample sample;
    protected RunSample runSample;
    protected RunPosition lane;
    protected Run run;

    protected Metadata metadata;
    protected Map<String, String> config;
    protected SeqwareClient seqwareClient;
    protected ca.on.oicr.pinery.api.Lims limsMock;
    protected PineryClient pineryClient;

    @BeforeClass
    public void setup() {

        //setup seqware
        metadata = new MetadataInMemory(); //new MetadataNoConnection();
        config = new HashMap<>();
        config.put("SW_METADATA_METHOD", "inmemory");
        seqwareClient = new MetadataBackedSeqwareClient(metadata, config);

        Map<String, SampleProvenanceProvider> spps = new HashMap<>();
        spp = Mockito.mock(SampleProvenanceProvider.class);
        spps.put(limsProvider, spp);

        Map<String, LaneProvenanceProvider> lpps = new HashMap<>();
        lpp = Mockito.mock(LaneProvenanceProvider.class);
        lpps.put(limsProvider, lpp);

        DefaultProvenanceClient dpc = new DefaultProvenanceClient();
        dpc.registerAnalysisProvenanceProvider("seqware", new SeqwareMetadataAnalysisProvenanceProvider(metadata));
        dpc.registerLaneProvenanceProvider(limsProvider, lpp);
        dpc.registerSampleProvenanceProvider(limsProvider, spp);

        provenanceClient = dpc;
    }

    @BeforeMethod
    public void setupLimsData() {
        DateTime limsLastModified = DateTime.now();

        sp1 = Mockito.mock(SampleProvenance.class);
        when(sp1.getProvenanceId()).thenReturn("1");
        when(sp1.getSampleProvenanceId()).thenReturn("1");
        when(sp1.getVersion()).thenReturn("v1");
        when(sp1.getLastModified()).thenReturn(limsLastModified);
        when(sp1.getSequencerRunName()).thenReturn("RUN1");

        sp2 = Mockito.mock(SampleProvenance.class);
        when(sp2.getProvenanceId()).thenReturn("2");
        when(sp2.getSampleProvenanceId()).thenReturn("2");
        when(sp2.getVersion()).thenReturn("v1");
        when(sp2.getLastModified()).thenReturn(limsLastModified);
        when(sp2.getSequencerRunName()).thenReturn("RUN2");

        sp3 = Mockito.mock(SampleProvenance.class);
        when(sp3.getProvenanceId()).thenReturn("3");
        when(sp3.getSampleProvenanceId()).thenReturn("3");
        when(sp3.getVersion()).thenReturn("v1");
        when(sp3.getLastModified()).thenReturn(limsLastModified);
        when(sp3.getSequencerRunName()).thenReturn("RUN3");

        when(spp.getSampleProvenance()).thenReturn(Arrays.asList(sp1, sp2, sp3));
    }

    @BeforeMethod(dependsOnMethods = {"setupLimsData"})
    public void setupAnalysis() {
        Workflow inputFileProducer = seqwareClient.createWorkflow("BamMergeWorkflow", "0.0", "the description");

        //create workflow run that has one BAM as output and is linked to IUS1
        FileMetadata file1 = new FileMetadata();
        file1.setDescription("description");
        file1.setMd5sum("md5sum");
        file1.setFilePath("/tmp/ius1.bam");
        file1.setMetaType("application/bam");
        file1.setType("type?");
        file1.setSize(1L);
        Set<IUS> limsKeys1 = Sets.newHashSet(
                seqwareClient.addLims(limsProvider, sp1.getSampleProvenanceId(), sp1.getVersion(), sp1.getLastModified()));
        WorkflowRun wr1 = seqwareClient.createWorkflowRun(inputFileProducer, limsKeys1, Collections.EMPTY_LIST, Arrays.asList(file1));
        inputFileWorkflowRuns.add(wr1);

        //create workflow run that has one BAM as output and is linked to IUS1 and IUS2
        FileMetadata file2 = new FileMetadata();
        file2.setDescription("description");
        file2.setMd5sum("md5sum");
        file2.setFilePath("/tmp/ius2.bam");
        file2.setMetaType("application/bam");
        file2.setType("type?");
        file2.setSize(1L);
        Set<IUS> limsKeys2 = Sets.newHashSet(
                seqwareClient.addLims(limsProvider, sp1.getSampleProvenanceId(), sp1.getVersion(), sp1.getLastModified()),
                seqwareClient.addLims(limsProvider, sp2.getSampleProvenanceId(), sp2.getVersion(), sp2.getLastModified()));
        WorkflowRun wr2 = seqwareClient.createWorkflowRun(inputFileProducer, limsKeys2, Collections.EMPTY_LIST, Arrays.asList(file2));
        inputFileWorkflowRuns.add(wr2);

        //create workflow run that has one BAM as output and is linked to IUS3
        FileMetadata file3 = new FileMetadata();
        file3.setDescription("description");
        file3.setMd5sum("md5sum");
        file3.setFilePath("/tmp/ius3.bam");
        file3.setMetaType("application/bam");
        file3.setType("type?");
        file3.setSize(1L);
        Set<IUS> limsKeys3 = Sets.newHashSet(
                seqwareClient.addLims(limsProvider, sp3.getSampleProvenanceId(), sp3.getVersion(), sp3.getLastModified()));
        WorkflowRun wr3 = seqwareClient.createWorkflowRun(inputFileProducer, limsKeys3, Collections.EMPTY_LIST, Arrays.asList(file3));
        inputFileWorkflowRuns.add(wr3);
    }

    @Test
    public void checkInitialState() {
        assertEquals(provenanceClient.getSampleProvenance().size(), 3);
        assertEquals(provenanceClient.getFileProvenance().size(), 4);
        for (FileProvenance fp : provenanceClient.getFileProvenance()) {
            assertEquals(fp.getStatus(), FileProvenance.Status.OKAY);
        }
    }

    @Test
    public void normalDeciderRun() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));

        OicrDecider decider;

        decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 3);

        decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all"));
        assertEquals(decider.getWorkflowRuns().size(), 0);

        //4 initial records + 4 new records
        assertEquals(provenanceClient.getFileProvenance().size(), 8);
    }

    @Test
    public void includeFilter_RUN1() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--include-sequencer-run", "RUN1"));
        assertEquals(decider.getWorkflowRuns().size(), 2);

        //4 initial records + 3 new records
        assertEquals(provenanceClient.getFileProvenance().size(), 7);
    }

    @Test
    public void includeFilter_RUN2() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--include-sequencer-run", "RUN2"));
        assertEquals(decider.getWorkflowRuns().size(), 1);

        //4 initial records + 2 new records
        assertEquals(provenanceClient.getFileProvenance().size(), 6);
    }

    @Test
    public void includeFilter_RUN3() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--include-sequencer-run", "RUN3"));
        assertEquals(decider.getWorkflowRuns().size(), 1);

        //4 initial records + 1 new record
        assertEquals(provenanceClient.getFileProvenance().size(), 5);
    }

    @Test
    public void includeFilter_RUN2_RUN3() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--include-sequencer-run", "RUN2", "--include-sequencer-run", "RUN3", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 2);

        //4 initial records + 3 new records
        assertEquals(provenanceClient.getFileProvenance().size(), 7);
    }

    @Test
    public void includeFilter_RUN2_RUN3_csv() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--include-sequencer-run", "RUN2,RUN3", "--verbose"));
        assertEquals(decider.getWorkflowRuns().size(), 2);

        //4 initial records + 3 new records
        assertEquals(provenanceClient.getFileProvenance().size(), 7);
    }

    @Test
    public void excludeFilter_RUN1() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--exclude-sequencer-run", "RUN1"));
        assertEquals(decider.getWorkflowRuns().size(), 2);

        //4 initial records + 3 new records
        assertEquals(provenanceClient.getFileProvenance().size(), 7);
    }

    @Test
    public void excludeFilter_RUN2() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--exclude-sequencer-run", "RUN2"));
        assertEquals(decider.getWorkflowRuns().size(), 3);

        //4 initial records + 4 new records
        assertEquals(provenanceClient.getFileProvenance().size(), 8);
    }

    @Test
    public void excludeFilter_RUN3() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--exclude-sequencer-run", "RUN3"));
        assertEquals(decider.getWorkflowRuns().size(), 2);

        //4 initial records + 3 new records
        assertEquals(provenanceClient.getFileProvenance().size(), 7);
    }

    @Test
    public void includeAndExcludeFilter_RUN1() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--include-sequencer-run", "RUN1", "--exclude-sequencer-run", "RUN1"));
        assertEquals(decider.getWorkflowRuns().size(), 0);

        //4 initial records + 0 new records
        assertEquals(provenanceClient.getFileProvenance().size(), 4);
    }

    @Test
    public void includeAndExcludeFilter_RUN2() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--include-sequencer-run", "RUN2", "--exclude-sequencer-run", "RUN2"));
        assertEquals(decider.getWorkflowRuns().size(), 0);

        //4 initial records + 0 new records
        assertEquals(provenanceClient.getFileProvenance().size(), 4);
    }

    @Test
    public void includeAndExcludeFilter_RUN3() {
        Workflow wf = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = getDeciderFor(wf);
        run(decider, Arrays.asList("--all", "--include-sequencer-run", "RUN3", "--exclude-sequencer-run", "RUN3"));
        assertEquals(decider.getWorkflowRuns().size(), 0);

        //4 initial records + 0 new records
        assertEquals(provenanceClient.getFileProvenance().size(), 4);
    }

    private OicrDecider getDeciderFor(Workflow workflow) {
        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(workflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("application/bam"));
        decider.setGroupBy(Group.FILE, true);
        return decider;
    }

    @AfterMethod
    public void destroySeqware() {
        Whitebox.<Table>getInternalState(MetadataInMemory.class, "STORE").clear();
    }

    protected void run(OicrDecider decider, List<String> params) {
        decider.setMetadata(metadata);
        decider.setConfig(config);
        decider.setParams(params);
        decider.parse_parameters();
        decider.init();
        decider.do_test();
        decider.do_run();
        decider.clean_up();
    }
}
