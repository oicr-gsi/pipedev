package ca.on.oicr.pde.deciders;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.*;
import net.sourceforge.seqware.common.model.*;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ca.on.oicr.gsi.provenance.FileProvenanceFilter;
import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.gsi.provenance.model.FileProvenance.Status;
import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import ca.on.oicr.pde.TestUtils;
import ca.on.oicr.pinery.api.RunPosition;
import ca.on.oicr.pinery.client.HttpResponseException;
import ca.on.oicr.pinery.lims.DefaultRunPosition;
import net.sourceforge.seqware.common.module.FileMetadata;

public class OicrDeciderBase extends DeciderBase {

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

		// create a file in seqware
		Workflow upstreamWorkflow = seqwareClient.createWorkflow("UpstreamWorkflow", "0.0", "",
				ImmutableMap.of("test_param", "test_value"));
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
		seqwareClient.createWorkflowRun(upstreamWorkflow, Sets.newHashSet(ius), Collections.emptyList(),
				Arrays.asList(file));

		EnumMap<FileProvenanceFilter, Set<String>> filters = new EnumMap<>(FileProvenanceFilter.class);
		filters.put(FileProvenanceFilter.workflow, ImmutableSet.of(upstreamWorkflow.getSwAccession().toString()));

		assertEquals(provenanceClient.getFileProvenance(filters).size(), 1);

		// setup downstream workflow and decider
		Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "",
				ImmutableMap.of("test_param", "test_value"));
		OicrDecider decider = new OicrDecider(provenanceClient);
		decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
		decider.setMetaType(Arrays.asList("text/plain"));

		filters.put(FileProvenanceFilter.workflow, ImmutableSet.of(upstreamWorkflow.getSwAccession().toString(),
				downstreamWorkflow.getSwAccession().toString()));

		// test decider operation modes
		run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(),
				"--sample-name", "TEST_SAMPLE", "--dry-run"));
		assertEquals(decider.getWorkflowRuns().size(), 1);
		assertEquals(provenanceClient.getFileProvenance(filters).size(), 1);

		run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(),
				"--sample-name", "TEST_SAMPLE", "--no-metadata"));
		assertEquals(decider.getWorkflowRuns().size(), 1);
		assertEquals(provenanceClient.getFileProvenance(filters).size(), 1);

		run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(),
				"--sample-name", "TEST_SAMPLE"));
		assertEquals(decider.getWorkflowRuns().size(), 1);
		assertEquals(provenanceClient.getFileProvenance(filters).size(), 2);

		run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(),
				"--sample-name", "TEST_SAMPLE", "--ignore-previous-runs"));
		assertEquals(decider.getWorkflowRuns().size(), 1);
		assertEquals(provenanceClient.getFileProvenance(filters).size(), 3);

		run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(), "--all",
				"--ignore-previous-runs"));
		assertEquals(decider.getWorkflowRuns().size(), 1);
		assertEquals(provenanceClient.getFileProvenance(filters).size(), 4);

		run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(), "--all",
				"--ignore-previous-runs", "--launch-max", "0"));
		assertEquals(decider.getWorkflowRuns().size(), 0);
		assertEquals(provenanceClient.getFileProvenance(filters).size(), 4);

		run(decider, Arrays.asList("--parent-wf-accession", upstreamWorkflow.getSwAccession().toString(), "--all",
				"--ignore-previous-runs", "--study-to-output-path-csv", TestUtils
						.getResourceFilePath("studyToOutputPathConfig/test-study-to-output-path.csv").getAbsolutePath(),
				"--launch-max", "999999")); // TODO: BasicDecider should reset launch-max during init()
		assertEquals(decider.getWorkflowRuns().size(), 1);
		Properties p = new Properties();
		p.load(FileUtils.openInputStream(Paths.get(Iterables.getOnlyElement(decider.getWorkflowRuns())).toFile()));
		Map<String, String> iniProperties = p.entrySet().stream()
				.collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
		assertEquals(iniProperties.get("output_prefix"), "/tmp/output/path/123/");
		assertEquals(provenanceClient.getFileProvenance(filters).size(), 5);
	}

	@Test
	public void deciderForCollectingWorkflow() {

		SampleProvenance sp = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);

		// setup files in seqware
		Workflow workflow1 = seqwareClient.createWorkflow("TestWorkflow1", "0.0", "",
				ImmutableMap.of("test_param", "test_value"));
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
		seqwareClient.createWorkflowRun(workflow1, Sets.newHashSet(ius), Collections.emptyList(), Arrays.asList(file));

		ius = seqwareClient.addLims("pinery", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
		file = new FileMetadata();
		file.setDescription("description");
		file.setMd5sum("md5sum");
		file.setFilePath("/tmp/file2.bam");
		file.setMetaType("text/plain");
		file.setType("type?");
		file.setSize(1L);
		seqwareClient.createWorkflowRun(workflow1, Sets.newHashSet(ius), Collections.emptyList(), Arrays.asList(file));

		EnumMap<FileProvenanceFilter, Set<String>> filters = new EnumMap<>(FileProvenanceFilter.class);
		filters.put(FileProvenanceFilter.workflow, ImmutableSet.of(workflow1.getSwAccession().toString()));

		assertEquals(provenanceClient.getFileProvenance(filters).size(), 2);

		// setup downstream workflow and decider
		Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "",
				ImmutableMap.of("test_param", "test_value"));
		OicrDecider decider = new OicrDecider(provenanceClient);
		decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
		decider.setMetaType(Arrays.asList("text/plain"));
		decider.setGroupBy(Group.ROOT_SAMPLE_NAME, true);

		filters.put(FileProvenanceFilter.workflow,
				ImmutableSet.of(workflow1.getSwAccession().toString(), downstreamWorkflow.getSwAccession().toString()));

		run(decider,
				Arrays.asList("--parent-wf-accessions", workflow1.getSwAccession().toString(), "--all", "--dry-run"));
		assertEquals(decider.getWorkflowRuns().size(), 1);
		assertEquals(provenanceClient.getFileProvenance(filters).size(), 2);

		run(decider, Arrays.asList("--parent-wf-accessions", workflow1.getSwAccession().toString(), "--all",
				"--no-metadata"));
		assertEquals(decider.getWorkflowRuns().size(), 1);
		assertEquals(provenanceClient.getFileProvenance(filters).size(), 2);

		run(decider, Arrays.asList("--parent-wf-accessions", workflow1.getSwAccession().toString(), "--all"));
		assertEquals(decider.getWorkflowRuns().size(), 1);

                // same sample for both bams, only one IUS-LimsKey is created so only one file provenance record is created
		assertEquals(provenanceClient.getFileProvenance(filters).size(), 3);
	}

	@Test
	public void sampleNameChangeCheckFileProvenanceStatus() {

		// setup files in seqware
		Workflow workflow1 = seqwareClient.createWorkflow("TestWorkflow1", "0.0", "",
				ImmutableMap.of("test_param", "test_value"));
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
		seqwareClient.createWorkflowRun(workflow1, Sets.newHashSet(ius), Collections.emptyList(), Arrays.asList(file));

		EnumMap<FileProvenanceFilter, Set<String>> filters = new EnumMap<>(FileProvenanceFilter.class);
		filters.put(FileProvenanceFilter.workflow, ImmutableSet.of(workflow1.getSwAccession().toString()));

		assertEquals(provenanceClient.getFileProvenance(filters).size(), 1);
		assertEquals(Iterables.getOnlyElement(provenanceClient.getFileProvenance(filters)).getStatus(), Status.OKAY);

		// modify sample provenance - version and last modified have changed
		sample.setName("TEST_SAMPLE_modified");
		assertEquals(Iterables.getOnlyElement(provenanceClient.getFileProvenance(filters)).getStatus(), Status.STALE);

		// modify sample id - sample provenance id changed, so unable to join analysis
		// provenance and sample provenance
		runSample.setId("1-modified");
		sample.setId("1-modified");
		assertEquals(Iterables.getOnlyElement(provenanceClient.getFileProvenance(filters)).getStatus(), Status.ERROR);
	}

	@Test
	public void demultiplexingWorkflow() {

		// sequence "sample" on 7 more lanes
		Set<RunPosition> lanes = new HashSet<>();
		for (int laneNumber = 2; laneNumber <= 8; laneNumber++) {
			RunPosition l = new DefaultRunPosition();
			l.setPosition(laneNumber);
			l.setRunSample(Sets.newHashSet(runSample));
			lanes.add(l);
		}
		run.getSamples().addAll(lanes);
		assertEquals(provenanceClient.getSampleProvenance().size(), 8);

		// setup a fake seqware workflow run that takes as input all samples and
		// provisions out one file per sample (so only one IusLimsKey link per file)
		String workflowName = "bcl2fastq";
		Workflow wf1 = seqwareClient.createWorkflow(workflowName, "0.0", "",
				ImmutableMap.of("test_param", "test_value"));
		Set<IUS> iuses = new HashSet<>();
		Set<Processing> processings = new HashSet<>();
		int id = 1;
		for (SampleProvenance sp : provenanceClient.getSampleProvenance()) {
			IUS ius = seqwareClient.addLims("pinery", sp.getSampleProvenanceId(), sp.getVersion(),
					sp.getLastModified());
			iuses.add(ius);

			File f2 = new File();
			f2.setFileId(id++);
			f2.setFilePath("/tmp/file1.bam");

			Processing p = new Processing();
			p.setProcessingId(id++);
			p.setFiles(Sets.newHashSet(f2)); // link file to processing
			p.setIUS(Sets.newHashSet(ius)); // link ius to processing
			processings.add(p);
		}
		seqwareClient.createWorkflowRun(wf1, iuses, Collections.emptyList(), processings);

		assertEquals(provenanceClient.getFileProvenance().size(), 8);
		for (FileProvenance fp : provenanceClient.getFileProvenance()) {
			assertEquals(fp.getIusLimsKeys().size(), 1);
			assertEquals(fp.getWorkflowName(), workflowName);
		}
	}

    @Test
    public void workflowRunAttributeFilterTest_GP_1978() {
        // setup files in seqware
        Workflow workflow1 = seqwareClient.createWorkflow("TestWorkflow1", "0.0", "",
                ImmutableMap.of("test_param", "test_value"));

        SampleProvenance sp = Iterables.getFirst(provenanceClient.getSampleProvenance(), null);
        IUS ius = seqwareClient.addLims("pinery", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
        FileMetadata file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/file1.bam");
        file.setMetaType("text/plain");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(workflow1, Sets.newHashSet(ius), Collections.emptyList(), Arrays.asList(file));

        EnumMap<FileProvenanceFilter, Set<String>> filters = new EnumMap<>(FileProvenanceFilter.class);
        filters.put(FileProvenanceFilter.skip, ImmutableSet.of("false"));
        filters.put(FileProvenanceFilter.workflow, ImmutableSet.of(workflow1.getSwAccession().toString()));
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 1);

        // setup DownstreamWorkflow workflow and decider
        Workflow downstreamWorkflow = seqwareClient.createWorkflow("DownstreamWorkflow", "0.0", "",
                ImmutableMap.of("test_param", "test_value"));
        OicrDecider decider = new OicrDecider(provenanceClient);
        decider.setWorkflowAccession(downstreamWorkflow.getSwAccession().toString());
        decider.setMetaType(Arrays.asList("text/plain"));
        decider.setGroupBy(Group.ROOT_SAMPLE_NAME, true);

        // schedule a DownstreamWorkflow run
        filters.put(FileProvenanceFilter.workflow,
                ImmutableSet.of(downstreamWorkflow.getSwAccession().toString()));
        run(decider, Arrays.asList("--parent-wf-accessions", workflow1.getSwAccession().toString(), "--all"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 1);

        // check that scheduling is blocked
        run(decider, Arrays.asList("--parent-wf-accessions", workflow1.getSwAccession().toString(), "--all"));
        assertEquals(decider.getWorkflowRuns().size(), 0);

        // annotate the above DownstreamWorkflow run as skipped
        Integer workflowRunSwid = provenanceClient.getFileProvenance(Maps.immutableEnumMap(ImmutableMap.of(
                FileProvenanceFilter.workflow, ImmutableSet.of(downstreamWorkflow.getSwAccession().toString()),
                FileProvenanceFilter.skip, ImmutableSet.of("false"))))
                .stream().collect(MoreCollectors.onlyElement()).getWorkflowRunSWID();
        WorkflowRunAttribute wra = new WorkflowRunAttribute();
        wra.setTag("skip");
        wra.setValue("test");
        metadata.annotateWorkflowRun(workflowRunSwid, wra, null);

        // override default workflow-run-attribute filters, check decider is blocked still
        run(decider, Arrays.asList("--parent-wf-accessions", workflow1.getSwAccession().toString(), "--all",
                "--workflow-run-annotation-tag-filters", ""));
        assertEquals(decider.getWorkflowRuns().size(), 0);

        //run the decider again, previous skipped workflow run is filtered so a new workflow run should be launched
        run(decider, Arrays.asList("--parent-wf-accessions", workflow1.getSwAccession().toString(), "--all",
                "--workflow-run-annotation-tag-filters", "skip"));
        assertEquals(decider.getWorkflowRuns().size(), 1);
        assertEquals(provenanceClient.getFileProvenance(filters).size(), 1);

        // another check to ensure the a new DownstreamWorkflow run was scheduled
        Integer newWorkflowRunSwid = provenanceClient.getFileProvenance(Maps.immutableEnumMap(ImmutableMap.of(
                FileProvenanceFilter.workflow, ImmutableSet.of(downstreamWorkflow.getSwAccession().toString()),
                FileProvenanceFilter.skip, ImmutableSet.of("false"))))
                .stream().collect(MoreCollectors.onlyElement()).getWorkflowRunSWID();
        assertNotEquals(workflowRunSwid, newWorkflowRunSwid);
    }

}
