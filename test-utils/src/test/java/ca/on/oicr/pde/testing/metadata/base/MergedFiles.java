package ca.on.oicr.pde.testing.metadata.base;

import ca.on.oicr.gsi.provenance.model.FileProvenance;
import ca.on.oicr.gsi.provenance.model.LimsKey;
import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import ca.on.oicr.pde.dao.reader.FileProvenanceClient;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.model.SeqwareObject;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.seqware.common.model.FileProvenanceParam;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Processing;
import net.sourceforge.seqware.common.model.WorkflowRun;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.module.FileMetadata;
import org.testng.Assert;
import org.testng.annotations.Test;
import ca.on.oicr.gsi.provenance.model.IusLimsKey;
import java.util.Collection;

/**
 * Test case for SEQWARE-1994: For files with multiple paths, skip the file when any of paths are skipped
 *
 * @author mlaszloffy
 */
public class MergedFiles extends Base {

    List<WorkflowRun> inputFileWorkflowRuns = new ArrayList<>();
    Workflow mergeBams;

    public MergedFiles(TestContext ctx) {
        super(ctx);
    }
    
    @Test
    public void setupFiles() {

        Workflow inputFileProducer = seqwareClient.createWorkflow("SomeAlignerWorkflow", "0.0", "the description");

        WorkflowRun wr;
        //create first bam (sequencer run 1, lane 1, IUS1)

        SampleProvenance sp;
        Map<String, Set<String>> filters = new HashMap<>();
        IUS i;
        FileMetadata file;

        filters.clear();
        filters.put(FileProvenanceParam.sample.toString(), Sets.newHashSet(seqwareObjects.get("IUS1").getSwAccession().toString()));
        sp = Iterables.getOnlyElement(provenanceClient.getSampleProvenance(filters));
        i = seqwareClient.addLims("seqware", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/ius1.bam");
        file.setMetaType("application/bam");
        file.setType("type?");
        file.setSize(1L);
        wr = seqwareClient.createWorkflowRun(inputFileProducer, Sets.newHashSet(i), Collections.EMPTY_LIST, Arrays.asList(file));
        inputFileWorkflowRuns.add(wr);

        //create second bam (sequencer run 1, lane 2, IUS2)
        filters.clear();
        filters.put(FileProvenanceParam.sample.toString(), Sets.newHashSet(seqwareObjects.get("IUS2").getSwAccession().toString()));
        sp = Iterables.getOnlyElement(provenanceClient.getSampleProvenance(filters));
        i = seqwareClient.addLims("seqware", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/ius2.bam");
        file.setMetaType("application/bam");
        file.setType("type?");
        file.setSize(1L);
        wr = seqwareClient.createWorkflowRun(inputFileProducer, Sets.newHashSet(i), Collections.EMPTY_LIST, Arrays.asList(file));
        inputFileWorkflowRuns.add(wr);

        //create third bam (sequencer run 2, lane 1, IUS9)
        filters.clear();
        filters.put(FileProvenanceParam.sample.toString(), Sets.newHashSet(seqwareObjects.get("IUS9").getSwAccession().toString()));
        sp = Iterables.getOnlyElement(provenanceClient.getSampleProvenance(filters));
        i = seqwareClient.addLims("seqware", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/ius9.bam");
        file.setMetaType("application/bam");
        file.setType("type?");
        file.setSize(1L);
        wr = seqwareClient.createWorkflowRun(inputFileProducer, Sets.newHashSet(i), Collections.EMPTY_LIST, Arrays.asList(file));
        inputFileWorkflowRuns.add(wr);

        List<ReducedFileProvenanceReportRecord> files = getAllFiles();
        Assert.assertEquals(files.size(), 3);
        for (ReducedFileProvenanceReportRecord f : files) {
            Assert.assertEquals(f.getSkip(), Boolean.FALSE);
            Assert.assertEquals(Iterables.getOnlyElement(f.getFileMetaType()), "application/bam");
        }
    }

    @Test(dependsOnMethods = "setupFiles")
    public void mergeFiles() {

        //Get workflow run's file processing records and IUS/LimsKeys
        Set<SeqwareObject> ps = new HashSet<>();
        Set<IusLimsKey> iks = new HashSet<>();
        for (WorkflowRun wr : inputFileWorkflowRuns) {
            Map<String, Set<String>> filters = new HashMap<>();
            filters.put(FileProvenanceParam.workflow_run.toString(), Sets.newHashSet(wr.getSwAccession().toString()));

            for (FileProvenance f : provenanceClient.getFileProvenance(filters)) {
                iks.addAll(f.getIusLimsKeys());
                Processing p = new Processing();
                p.setSwAccession(f.getProcessingSWID());
                ps.add(new SeqwareObject(p));
            }
        }
        FileMetadata file;
        Set<IUS> newIus = new HashSet<>();
        for (IusLimsKey ik : iks) {
            LimsKey lk = ik.getLimsKey();
            newIus.add(seqwareClient.addLims(lk.getProvider(), lk.getId(), lk.getVersion(), lk.getLastModified()));
        }

        mergeBams = seqwareClient.createWorkflow("SomeMergeWorkflow", "0.0", "the description");
        file = new FileMetadata();
        file.setDescription("description");
        file.setMd5sum("md5sum");
        file.setFilePath("/tmp/merged.bam");
        file.setMetaType("application/bam");
        file.setType("type?");
        file.setSize(1L);
        seqwareClient.createWorkflowRun(mergeBams, newIus, ps, Arrays.asList(file));

        Collection<FileProvenance> fps = provenanceClient.getFileProvenance();
        
        Assert.assertEquals(fps.size(), 6);

        List<ReducedFileProvenanceReportRecord> files = getAllFiles();
        Assert.assertEquals(files.size(), 4);
        for (ReducedFileProvenanceReportRecord f : files) {
            Assert.assertEquals(f.getSkip(), Boolean.FALSE);
            Assert.assertEquals(Iterables.getOnlyElement(f.getFileMetaType()), "application/bam");
        }
    }

    @Test(dependsOnMethods = "mergeFiles")
    public void skipSequencerRun() {
        seqwareLimsClient.annotate(seqwareObjects.getSequencerRun("TEST_SEQUENCER_RUN_002"), true, "sequencer run skip test");

        Map<String, Set<String>> filters = new HashMap<>();
        filters.put(FileProvenanceParam.sequencer_run.toString(), Sets.newHashSet("TEST_SEQUENCER_RUN_002"));

        List<FileProvenance> fps = new ArrayList<>(provenanceClient.getFileProvenance(filters));

        //check file provenance records
        for (FileProvenance f : fps) {
            Assert.assertEquals(f.getSkip(), "true", "File with swid = " + f.getFileSWID() + " should have been skipped.");
        }

        //check reduced file provenance records
        for (ReducedFileProvenanceReportRecord rf : new FileProvenanceClient(fps).getAllFiles()) {
            Assert.assertEquals(rf.getSkip(), Boolean.TRUE, "The file [" + rf.toString() + "] should have been skipped.");
        }
    }

    @Test(dependsOnMethods = "skipSequencerRun")
    public void checkFileProvenanceRecordsAreSkipped() {
        Map<String, Set<String>> filters = new HashMap<>();
        filters.put(FileProvenanceParam.workflow.toString(), Sets.newHashSet(mergeBams.getSwAccession().toString()));
//        filters.put(FileProvenanceParam.skip.toString(), Sets.newHashSet("false"));
        List<FileProvenance> fps = new ArrayList<>(provenanceClient.getFileProvenance(filters));

        Assert.assertEquals(fps.size(), 3);
        for (FileProvenance fp : fps) {
            Assert.assertEquals(fp.getSkip(), "true", "File with swid = " + fp.getFileSWID() + " should have been skipped.");
        }
    }

    @Test(dependsOnMethods = "skipSequencerRun")
    public void checkReducedFileProvenanceRecordsAreSkipped() {
        Map<String, Set<String>> filters = new HashMap<>();
        filters.put(FileProvenanceParam.workflow.toString(), Sets.newHashSet(mergeBams.getSwAccession().toString()));
        List<FileProvenance> fps = new ArrayList<>(provenanceClient.getFileProvenance(filters));

        List<ReducedFileProvenanceReportRecord> rfs = new FileProvenanceClient(fps).getAllFiles();
        Assert.assertEquals(rfs.size(), 1);
        for (ReducedFileProvenanceReportRecord rf : rfs) {
            Assert.assertEquals(rf.getSkip(), Boolean.TRUE, "The file [" + rf.toString() + "] should have been skipped.");
        }
    }

    private List<ReducedFileProvenanceReportRecord> getAllFiles() {
        List<FileProvenance> fps = new ArrayList<>(provenanceClient.getFileProvenance());
        return new FileProvenanceClient(fps).getAllFiles();
    }

}
