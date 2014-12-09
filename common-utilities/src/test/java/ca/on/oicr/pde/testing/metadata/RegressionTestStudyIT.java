package ca.on.oicr.pde.testing.metadata;

import ca.on.oicr.pde.dao.writer.SeqwareWriteService;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.model.SeqwareAccession;
import ca.on.oicr.pde.model.SeqwareObject;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.dao.writer.SeqwareWriteService.FileInfo;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author mlaszloffy
 */
public class RegressionTestStudyIT extends RegressionTestStudyBase{

    @Test
    public void attachFilesToTestStudy() throws MalformedURLException, InterruptedException, ExecutionException {

        es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
        List<Future<String>> fs = new LinkedList<Future<String>>();
        for (int i = 1; i <= 22; i++) {
            fs.addAll(generateFiles(seqwareObjects.get("IUS" + i), "Workflow_" + i, 100));
        }
        List<SeqwareAccession> workflowRunIds = new ArrayList<SeqwareAccession>();
        for (Future<String> f : fs) {
            workflowRunIds.add(new SeqwareAccession(f.get()));
        }
        es.shutdownNow();

        //TODO: file provenance report update - the reader service should be doing this explicitly
        seqwareWriter.updateFileReport();

        //refresh local seqware file provenance report info
        seqwareReader.update();

        int skipCount = 0;
        int okayCount = 0;
        int nullCount = 0;
        List<ReducedFileProvenanceReportRecord> files = seqwareReader.getFiles(workflowRunIds);
        for (ReducedFileProvenanceReportRecord f : files) {
            if (f.getSkip() == true) {
                skipCount++;
            } else if (f.getSkip() == false) {
                okayCount++;
            } else {
                nullCount++;
            }
        }

        //sequencer 1 has 8 samples, sequencer 2 has 12 samples, sequencer 3 has 2 samples
        //each sample has 100 files... (8 + 12 + 2) * 100 = 2200 files expected
        assertEquals(files.size(), 2200);

        //ius 13 and 18 (from sequencer run 2), lane 4 of sequencer run 2, and all of sequencer run 3 are skipped
        //2200 - 6 * 100 = 1600
        assertEquals(okayCount, 1600);
        //6 * 100 = 600
        assertEquals(skipCount, 600);
        //no expected 
        assertEquals(nullCount, 0);

    }

    private List<Future<String>> generateFiles(SeqwareObject parent, String workflowName, int numberOfFiles) {
        Workflow wf = seqwareWriter.createWorkflow(workflowName, "0.0", "the description");
        List<Future<String>> fs = new ArrayList<Future<String>>();
        for (int i = 0; i < numberOfFiles; i++) {
            fs.add(es.submit(new CreateFile(seqwareWriter, wf, Arrays.asList(parent),
                    Arrays.asList(new FileInfo("TYPE", "META-TYPE", "/tmp/test_" + workflowName + "_" + i + ".gz"))
            //BUG: returns <2100 files? Arrays.asList(new SeqwareWriteService.FileInfo("TYPE", "META-TYPE", "/tmp/test_" + workflowName + ".gz"))
            )));

        }
        return fs;
    }

    private class CreateFile implements Callable {

        private final SeqwareWriteService service;
        private final Workflow wf;
        private final List<SeqwareObject> parents;
        private final List<FileInfo> files;

        public CreateFile(SeqwareWriteService ctx, Workflow wf, List<SeqwareObject> parents, List<FileInfo> files) {
            this.service = ctx;
            this.wf = wf;
            this.parents = parents;
            this.files = files;
        }

        @Override
        public String call() throws Exception {
            return service.createWorkflowRun(wf, parents, files);
        }

    }

}
