package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.dao.writer.SeqwareWriteService.FileInfo;
import ca.on.oicr.pde.model.FileProvenanceReportRecord;
import ca.on.oicr.pde.model.Processing;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * IT for SEQWARE-1994: For files with multiple paths, skip the file when any of paths are skipped
 *
 * @author mlaszloffy
 */
public class MergedFilesIT extends RegressionTestStudyBase {

    List<WorkflowRun> inputFileWorkflowRuns;
    Workflow mergeBams;

    public MergedFilesIT() {
        inputFileWorkflowRuns = new ArrayList<>();
    }

    @Test
    public void setupFiles() {

        Workflow inputFileProducer = seqwareWriter.createWorkflow("SomeAlignerWorkflow", "0.0", "the description");
        WorkflowRun.Builder wrb;

        //create first bam (sequencer run 1, lane 1, IUS1)
        wrb = new WorkflowRun.Builder();
        wrb.setSwid(seqwareWriter.createWorkflowRun(inputFileProducer, Arrays.asList(seqwareObjects.get("IUS1")), Arrays.asList(new FileInfo("type", "application/bam", "/tmp/ius1.bam"))));
        inputFileWorkflowRuns.add(wrb.build());

        //create second bam (sequencer run 1, lane 2, IUS2)
        wrb = new WorkflowRun.Builder();
        wrb.setSwid(seqwareWriter.createWorkflowRun(inputFileProducer, Arrays.asList(seqwareObjects.get("IUS2")), Arrays.asList(new FileInfo("type", "application/bam", "/tmp/ius2.bam"))));
        inputFileWorkflowRuns.add(wrb.build());

        //create third bam (sequencer run 2, lane 1, IUS9)
        wrb = new WorkflowRun.Builder();
        wrb.setSwid(seqwareWriter.createWorkflowRun(inputFileProducer, Arrays.asList(seqwareObjects.get("IUS9")), Arrays.asList(new FileInfo("type", "application/bam", "/tmp/ius9.bam"))));
        inputFileWorkflowRuns.add(wrb.build());

        List<ReducedFileProvenanceReportRecord> files = getAllFiles();
        Assert.assertEquals(files.size(), 3);
        for (ReducedFileProvenanceReportRecord f : files) {
            Assert.assertEquals(f.getSkip(), Boolean.FALSE);
            Assert.assertEquals(Iterables.getOnlyElement(f.getFileMetaType()), "application/bam");
        }
    }

    @Test(dependsOnMethods = "setupFiles")
    public void mergeFiles() {

        //Get workflow run's file processing records
        seqwareWriter.updateFileReport();
        seqwareReader.update();
        Set<Processing> ps = new HashSet<>();
        for (WorkflowRun wr : inputFileWorkflowRuns) {
            for (FileProvenanceReportRecord f : seqwareReader.getFileRecords(wr)) {
                ps.add(f.getProcessing());
            }
        }

        mergeBams = seqwareWriter.createWorkflow("SomeMergeWorkflow", "0.0", "the description");
        seqwareWriter.createWorkflowRun(mergeBams, ps, Arrays.asList(new FileInfo("type", "application/bam", "/tmp/merged.bam")));
        List<ReducedFileProvenanceReportRecord> files = getAllFiles();
        Assert.assertEquals(files.size(), 4);
        for (ReducedFileProvenanceReportRecord f : files) {
            Assert.assertEquals(f.getSkip(), Boolean.FALSE);
            Assert.assertEquals(Iterables.getOnlyElement(f.getFileMetaType()), "application/bam");
        }
    }

    @Test(dependsOnMethods = "mergeFiles")
    public void skipSequencerRun() {
        seqwareWriter.annotate(seqwareObjects.get("TEST_SEQUENCER_RUN_002"), true, "sequencer run skip test");

        seqwareWriter.updateFileReport();
        seqwareReader.update();

        //check file provenance records
        for (FileProvenanceReportRecord f : seqwareReader.getFileRecords(seqwareObjects.get("TEST_SEQUENCER_RUN_002"))) {
            Assert.assertEquals(f.getSkip(), "true", "File with swid = " + f.getFileSwid() + " should have been skipped.");
        }

        //check reduced file provenance records
        for (ReducedFileProvenanceReportRecord rf : seqwareReader.getFiles(Arrays.asList(seqwareObjects.get("TEST_SEQUENCER_RUN_002")))) {
            Assert.assertEquals(rf.getSkip(), Boolean.TRUE, "The file [" + rf.toString() + "] should have been skipped.");
        }
    }

    @Test(dependsOnMethods = "skipSequencerRun")
    public void checkFileProvenanceRecordsAreSkipped() {
        Collection<FileProvenanceReportRecord> fs = seqwareReader.getFileRecords(mergeBams);
        Assert.assertEquals(fs.size(), 3);
        for (FileProvenanceReportRecord f : fs) {
            Assert.assertEquals(f.getSkip(), "true", "File with swid = " + f.getFileSwid() + " should have been skipped.");
        }
    }

    @Test(dependsOnMethods = "skipSequencerRun")
    public void checkReducedFileProvenanceRecordsAreSkipped() {
        List<ReducedFileProvenanceReportRecord> rfs = seqwareReader.getFiles(Arrays.asList(mergeBams));
        Assert.assertEquals(rfs.size(), 1);
        for (ReducedFileProvenanceReportRecord rf : rfs) {
            Assert.assertEquals(rf.getSkip(), Boolean.TRUE, "The file [" + rf.toString() + "] should have been skipped.");
        }
    }

    private List<ReducedFileProvenanceReportRecord> getAllFiles() {
        seqwareWriter.updateFileReport();
        seqwareReader.update();
        return seqwareReader.getAllFiles();
    }

}
