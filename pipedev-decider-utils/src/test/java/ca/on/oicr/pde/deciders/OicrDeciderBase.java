package ca.on.oicr.pde.deciders;

import ca.on.oicr.gsi.provenance.ExtendedProvenanceClient;
import ca.on.oicr.gsi.provenance.FileProvenanceFilter;
import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.gsi.provenance.model.FileProvenance.Status;
import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import ca.on.oicr.pde.TestUtils;
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
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import net.sourceforge.seqware.common.model.File;
import net.sourceforge.seqware.common.model.Processing;
import org.apache.commons.io.FileUtils;

public class OicrDeciderBase {

    protected List<Sample> samples;
    protected SampleProject project;
    protected Sample sample;
    protected RunSample runSample;
    protected RunPosition lane;
    protected Run run;

    protected ExtendedProvenanceClient provenanceClient;
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
        sample.setId("1");
        sample.setModified(DateTime.parse("2015-01-01T00:00:00.000Z").toDate());
        samples.add(sample);

        a = new DefaultAttribute();
        a.setName("Tissue Type");
        a.setValue("R");

        runSample = new DefaultRunSample();
        runSample.setId("1");
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

        //check preconditions
        assertEquals(provenanceClient.getSampleProvenance().size(), 1);
        assertEquals(provenanceClient.getFileProvenance().size(), 0);
        assertEquals(Iterables.getOnlyElement(provenanceClient.getSampleProvenance()).getSampleName(), "TEST_SAMPLE");
    }

    @Test
    public void noMetadataChange() {
        SampleProvenance before = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);
        SampleProvenance after = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);
        assertEquals(before.getVersion(), after.getVersion());
    }

    @Test
    public void sampleNameChange() {
        SampleProvenance before = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);
        sample.setName("TEST_SAMPLE_mod");
        SampleProvenance after = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);
        assertNotEquals(before.getVersion(), after.getVersion());
    }

    @Test
    public void deciderOperationModes() throws HttpResponseException, IOException {

        SampleProvenance sp = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);

        //create a file in seqware
        Workflow upstreamWorkflow = seqwareClient.createWorkflow("UpstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        IUS ius;
        FileMetadata file;
        ius = seqwareClient.addLims("pinery", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/file.bam");
        file.setMetaType("text/plain");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(upstreamWorkflow, Sets.newHashSet(ius), Collections.EMPTY_LIST, Arrays.asList(file));

        EnumMap<FileProvenanceFilter, Set<String>> filters = new EnumMap<>(FileProvenanceFilter.class);
        filters.put(FileProvenanceFilter.workflow, ImmutableSet.of(upstreamWorkflow.getSwAccession().toString()));

        assertEquals(provenanceClient.getFileProvenance(filters).size(), 1);

        //setup downstream workflow and decider
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));

        filters.put(FileProvenanceFilter.workflow, ImmutableSet.of(upstreamWorkflow.getSwAccession().toString(), downstreamWorkflow.getSwAccession().toString()));

        //test decider operation modes
        run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(), "--sample-name", "TEST_SAMPLE", "--dry-run"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 1);

        run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(), "--sample-name", "TEST_SAMPLE", "--no-metadata"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 1);

        run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(), "--sample-name", "TEST_SAMPLE"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 2);

        run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(), "--sample-name", "TEST_SAMPLE", "--ignore-previous-runs"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 3);

        run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(), "--all", "--ignore-previous-runs"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 4);

        run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(), "--all", "--ignore-previous-runs", "--launch-max", "0"));
        assertEquals(decider.getWorkflowRuns().size(), 0);
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 4);

        run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(), "--all", "--ignore-previous-runs",
                "--study-to-output-path-csv", TestUtils.getResourceFilePath("studyToOutputPathConfig/test-study-to-output-path.csv").getAbsolutePath(),
                "--launch-max", "999999")); //TODO: BasicDecider should reset launch-max during init()
        assertEquals(decider.getWorkflowRuns().size(), 1);
        Properties p = new Properties();
        p.load(FileUtils.openInputStream(Paths.get(Iterables.getOnlyElement(decider.getWorkflowRuns())).toFile()));
        Map<String, String> iniProperties = new HashMap(p);
        assertEquals(iniProperties.get("output_prefix"), "/tmp/output/path/123/");
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 5);
    }

    @Test
    public void deciderForMergingWorkflow() {

        SampleProvenance sp = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);

        //setup files in seqware
        Workflow workflow1 = seqwareClient.createWorkflow("TestWorkflow1", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        IUS ius;
        FileMetadata file;

        ius = seqwareClient.addLims("pinery", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/file1.bam");
        file.setMetaType("text/plain");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(workflow1, Sets.newHashSet(ius), Collections.EMPTY_LIST, Arrays.asList(file));

        ius = seqwareClient.addLims("pinery", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/file2.bam");
        file.setMetaType("text/plain");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(workflow1, Sets.newHashSet(ius), Collections.EMPTY_LIST, Arrays.asList(file));

        EnumMap<FileProvenanceFilter, Set<String>> filters = new EnumMap<>(FileProvenanceFilter.class);
        filters.put(FileProvenanceFilter.workflow, ImmutableSet.of(workflow1.getSwAccession().toString()));

        assertEquals(provenanceClient.getFileProvenance(filters).size(), 2);

        //setup downstream workflow and decider
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));
        decider.setGroupBy(Group.ROOT_SAMPLE_NAME, true);

        filters.put(FileProvenanceFilter.workflow, ImmutableSet.of(workflow1.getSwAccession().toString(), downstreamWorkflow.getSwAccession().toString()));

        run(decider, Arrays.asList("--parent-wf-accessions", workflow1.getSwAccession().toString(), "--all", "--dry-run"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 2);

        run(decider, Arrays.asList("--parent-wf-accessions", workflow1.getSwAccession().toString(), "--all", "--no-metadata"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 2);

        run(decider, Arrays.asList("--parent-wf-accessions", workflow1.getSwAccession().toString(), "--all"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 4);
    }

    @Test
    public void sampleNameChangeCheckFileProvenanceStatus() {

        //setup files in seqware
        Workflow workflow1 = seqwareClient.createWorkflow("TestWorkflow1", "0.0", "", ImmutableMap.of("test_param", "test_value"));
        IUS ius;
        FileMetadata file;

        SampleProvenance sp = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);
        assertNotNull(sp);

        ius = seqwareClient.addLims("pinery", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/file1.bam");
        file.setMetaType("text/plain");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(workflow1, Sets.newHashSet(ius), Collections.EMPTY_LIST, Arrays.asList(file));

        EnumMap<FileProvenanceFilter, Set<String>> filters = new EnumMap<>(FileProvenanceFilter.class);
        filters.put(FileProvenanceFilter.workflow, ImmutableSet.of(workflow1.getSwAccession().toString()));

        assertEquals(provenanceClient.getFileProvenance(filters).size(), 1);
        assertEquals(Iterables.getOnlyElement(provenanceClient.getFileProvenance(filters)).getStatus(), Status.OKAY);

        //modify sample provenance - version and last modified have changed
        sample.setName("TEST_SAMPLE_modified");
        assertEquals(Iterables.getOnlyElement(provenanceClient.getFileProvenance(filters)).getStatus(), Status.STALE);

        //modify sample id - sample provenance id changed, so unable to join analysis provenance and sample provenance
        runSample.setId("1-modified");
        sample.setId("1-modified");
        assertEquals(Iterables.getOnlyElement(provenanceClient.getFileProvenance(filters)).getStatus(), Status.ERROR);
    }

    @Test
    public void demultiplexingWorkflow() {

        //sequence "sample" on 7 more lanes
        Set<RunPosition> lanes = new HashSet<>();
        for (int laneNumber = 2; laneNumber <= 8; laneNumber++) {
            RunPosition l = new DefaultRunPosition();
            l.setPosition(laneNumber);
            l.setRunSample(Sets.newHashSet(runSample));
            lanes.add(l);
        }
        run.getSamples().addAll(lanes);
        assertEquals(provenanceClient.getSampleProvenance().size(), 8);

        //setup a fake seqware workflow run that takes as input all samples and provisions out one file per sample (so only one IusLimsKey link per file)
        String workflowName = "bcl2fastq";
        Workflow wf1 = seqwareClient.createWorkflow(workflowName, "0.0", "", ImmutableMap.of("test_param", "test_value"));
        Set<IUS> iuses = new HashSet<>();
        Set<Processing> processings = new HashSet<>();
        int id = 1;
        for (SampleProvenance sp : provenanceClient.getSampleProvenance()) {
            IUS ius = seqwareClient.addLims("pinery", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
            iuses.add(ius);

            File f2 = new File();
            f2.setFileId(id++);
            f2.setFilePath("/tmp/file1.bam");

            Processing p = new Processing();
            p.setProcessingId(id++);
            p.setFiles(Sets.newHashSet(f2)); //link file to processing
            p.setIUS(Sets.newHashSet(ius)); //link ius to processing
            processings.add(p);
        }
        seqwareClient.createWorkflowRun(wf1, iuses, Collections.EMPTY_LIST, processings);

        assertEquals(provenanceClient.getFileProvenance().size(), 8);
        for (FileProvenance fp : provenanceClient.getFileProvenance()) {
            assertEquals(fp.getIusLimsKeys().size(), 1);
            assertEquals(fp.getWorkflowName(), workflowName);
        }
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
