package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.ProvenanceClient;
import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sourceforge.seqware.common.metadata.Metadata;
import net.sourceforge.seqware.common.module.FileMetadata;
import org.testng.annotations.*;
import static org.testng.Assert.*;
import ca.on.oicr.pinery.api.Attribute;
import ca.on.oicr.pinery.api.Run;
import ca.on.oicr.pinery.api.RunPosition;
import ca.on.oicr.pinery.api.RunSample;
import ca.on.oicr.pinery.api.Sample;
import ca.on.oicr.pinery.api.SampleProject;
import ca.on.oicr.pinery.client.HttpResponseException;
import ca.on.oicr.pinery.client.PineryClient;
import ca.on.oicr.pinery.lims.DefaultAttribute;
import ca.on.oicr.pinery.lims.DefaultRun;
import ca.on.oicr.pinery.lims.DefaultRunPosition;
import ca.on.oicr.pinery.lims.DefaultRunSample;
import ca.on.oicr.pinery.lims.DefaultSample;
import ca.on.oicr.pinery.lims.DefaultSampleProject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Workflow;
import org.joda.time.DateTime;
import static org.mockito.Mockito.when;
import ca.on.oicr.pde.client.SeqwareClient;

public class OicrDeciderBase {

    protected List<Sample> samples;
    protected SampleProject project;
    protected Sample sample;
    protected RunSample runSample;
    protected RunPosition lane;
    protected Run run;

    protected ProvenanceClient provenanceClient;
    protected Metadata metadata;
    protected Map<String, String> config;
    protected SeqwareClient seqwareClient;
    protected ca.on.oicr.pinery.api.Lims limsMock;
    protected PineryClient pineryClient;

    public OicrDeciderBase() {
    }

    @BeforeMethod(dependsOnGroups = "setup")
    public void setupData() {
        // Populate the LIMS mock with some data
        samples = new ArrayList<>();

        project = new DefaultSampleProject();
        project.setName("TEST_PROJECT");

        Attribute a;
        a = new DefaultAttribute();
        a.setName("Tissue Type");
        a.setValue("R");

        sample = new DefaultSample();
        sample.setName("TEST_SAMPLE");
        sample.setProject("TEST_PROJECT");
        sample.setAttributes(Sets.newHashSet(a));
        sample.setId(1);
        sample.setModified(DateTime.parse("2015-01-01T00:00:00.000Z").toDate());
        samples.add(sample);

        a = new DefaultAttribute();
        a.setName("Tissue Type");
        a.setValue("R");

        runSample = new DefaultRunSample();
        runSample.setId(1);
        runSample.setAttributes(Sets.newHashSet(a));

        lane = new DefaultRunPosition();
        lane.setPosition(1);
        lane.setRunSample(Sets.newHashSet(runSample));

        run = new DefaultRun();
        run.setId(1);
        run.setName("ABC_123");
        run.setSample(Sets.newHashSet(lane));

        when(limsMock.getSampleProjects()).thenReturn(Lists.newArrayList(project));
        when(limsMock.getSamples(null, null, null, null, null)).thenReturn(samples);
        when(limsMock.getRuns()).thenReturn(Lists.newArrayList(run));
    }

    @Test(enabled = true)
    public void pineryTest1() {
        SampleProvenance before = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);
        SampleProvenance after = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);
        assertEquals(before.getVersion(), after.getVersion());
    }

    @Test(enabled = true)
    public void pineryTest() {
        SampleProvenance before = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);
        sample.setName("TEST_SAMPLE_mod");
        SampleProvenance after = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);
        assertNotEquals(before.getVersion(), after.getVersion());
    }

    @Test(enabled = true)
    public void pineryTest3() {
        SampleProvenance before = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);
        assertEquals(before.getSampleName(), "TEST_SAMPLE");
    }

    @Test(enabled = true)
    public void deciderOperationModes() throws HttpResponseException {

        //check preconditions
        assertEquals(provenanceClient.getSampleProvenance().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 0);

        //create a file in seqware
        Workflow upstreamWorkflow = seqwareClient.createWorkflow("UpstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        IUS ius;
        FileMetadata file;
        ius = seqwareClient.addLims("seqware", "1_1_1", "1", new DateTime());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/file.bam");
        file.setMetaType("text/plain");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(upstreamWorkflow, Sets.newHashSet(ius), Collections.EMPTY_LIST, Arrays.asList(file));
        assertEquals(provenanceClient.getSampleProvenance().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 1);

        //setup downstream workflow and decider
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        //test decider operation modes
        run(decider, Arrays.asList("--sample-name", "TEST_SAMPLE", "--dry-run"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 1);

        run(decider, Arrays.asList("--sample-name", "TEST_SAMPLE", "--no-metadata"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 1);

        run(decider, Arrays.asList("--sample-name", "TEST_SAMPLE"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        run(decider, Arrays.asList("--sample-name", "TEST_SAMPLE", "--ignore-previous-runs"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 3);

        run(decider, Arrays.asList("--all", "--ignore-previous-runs"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 4);

        run(decider, Arrays.asList("--all", "--ignore-previous-runs", "--launch-max", "0"));
        assertEquals(decider.getWorkflowRuns().size(), 0);
        assertEquals(provenanceClient.getFileProvenance().size(), 4);
    }

    @Test
    public void deciderForMergingWorkflow() {

        //check preconditions
        assertEquals(provenanceClient.getSampleProvenance().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 0);

        //setup files in seqware
        Workflow workflow1 = seqwareClient.createWorkflow("TestWorkflow1", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        IUS ius;
        FileMetadata file;

        ius = seqwareClient.addLims("seqware", "1_1_1", "1", new DateTime());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/file1.bam");
        file.setMetaType("text/plain");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(workflow1, Sets.newHashSet(ius), Collections.EMPTY_LIST, Arrays.asList(file));

        ius = seqwareClient.addLims("seqware", "1_1_1", "1", new DateTime());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/file2.bam");
        file.setMetaType("text/plain");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(workflow1, Sets.newHashSet(ius), Collections.EMPTY_LIST, Arrays.asList(file));

        assertEquals(provenanceClient.getSampleProvenance().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        //setup downstream workflow and decider
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));
        decider.setGroupBy(Group.ROOT_SAMPLE_NAME, true);

        run(decider, Arrays.asList("--all", "--dry-run"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        run(decider, Arrays.asList("--all", "--no-metadata"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 2);

        run(decider, Arrays.asList("--all"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 4);
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
