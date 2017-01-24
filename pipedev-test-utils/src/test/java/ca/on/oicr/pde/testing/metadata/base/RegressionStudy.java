package ca.on.oicr.pde.testing.metadata.base;

import ca.on.oicr.gsi.provenance.FileProvenanceFilter;
import ca.on.oicr.gsi.provenance.model.SampleProvenance;
import ca.on.oicr.pde.dao.reader.FileProvenanceClient;
import ca.on.oicr.pde.model.ReducedFileProvenanceReportRecord;
import ca.on.oicr.pde.model.SeqwareObject;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.module.FileMetadata;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import ca.on.oicr.pde.client.SeqwareClient;

/**
 *
 * @author mlaszloffy
 */
public class RegressionStudy extends Base {

    protected int numberOfThreads = 1;

    public RegressionStudy(TestContext ctx) {
        super(ctx);
    }

    @Test
    public void attachFilesToTestStudy() throws MalformedURLException, InterruptedException, ExecutionException {

        ExecutorService es = Executors.newFixedThreadPool(numberOfThreads);//Runtime.getRuntime().availableProcessors() * 4);

        //create tasks
        List<Callable<Integer>> tasks = new LinkedList<>();
        for (int i = 1; i <= 22; i++) {
            tasks.addAll(generateFiles(seqwareObjects.get("IUS" + i), "Workflow_" + i, 100));
        }

        //execute tasks
        List<Future> fs = new LinkedList<>();
        for (Callable<Integer> task : tasks) {
            fs.add(es.submit(task));
        }

        //get task results
        List<Integer> workflowRunIds = new ArrayList<>();
        for (Future<Integer> f : fs) {
            workflowRunIds.add(f.get());
        }

        if (!es.shutdownNow().isEmpty()) {
            throw new RuntimeException("Executor service stopped before tasks completed.");
        }

        int skipCount = 0;
        int okayCount = 0;
        int nullCount = 0;

        Map<FileProvenanceFilter, Set<String>> filters = new HashMap<>();
        filters.put(FileProvenanceFilter.workflow_run, Sets.newHashSet(Lists.transform(workflowRunIds, Functions.toStringFunction())));

        List<ReducedFileProvenanceReportRecord> files = new FileProvenanceClient(Lists.newArrayList(provenanceClient.getFileProvenance(filters))).getAllFiles();
        for (ReducedFileProvenanceReportRecord f : files) {
            if (f.getSkip() == true) {
                skipCount++;
            } else if (f.getSkip() == false) {
                okayCount++;
            } else {
                nullCount++;
            }
        }

        for (ReducedFileProvenanceReportRecord f : files) {
            assertEquals(f.getStudyTitle(), Sets.newHashSet("PDE_TEST"));
        }

        //sequencer 1 has 8 samples, sequencer 2 has 12 samples, sequencer 3 has 2 samples
        //each sample has 100 files... (8 + 12 + 2) * 100 = 2200 files expected
        assertEquals(files.size(), 2200);

        //ius 13 and 19 (from sequencer run 2), lane 4 of sequencer run 2, and all of sequencer run 3 are skipped
        //2200 - 6 * 100 = 1600
        assertEquals(okayCount, 1600);
        //6 * 100 = 600
        assertEquals(skipCount, 600);
        //no expected 
        assertEquals(nullCount, 0);

    }

    private List<Callable<Integer>> generateFiles(SeqwareObject parent, String workflowName, int numberOfFiles) {
        Map<FileProvenanceFilter, Set<String>> filters = new HashMap<>();
        filters.put(FileProvenanceFilter.sample, Sets.newHashSet(parent.getSwAccession().toString()));
        SampleProvenance sp = Iterables.getOnlyElement(provenanceClient.getSampleProvenance(filters));
        IUS ius = seqwareClient.addLims("seqware", sp.getSampleProvenanceId(), sp.getVersion(), sp.getLastModified());

        Workflow workflow = seqwareClient.createWorkflow(workflowName, "0.0", "the description");
        List<Callable<Integer>> fs = new ArrayList<>();
        for (int i = 0; i < numberOfFiles; i++) {
            FileMetadata file = new FileMetadata();
            file.setDescription("description");
            file.setMd5sum("md5sum");
            file.setFilePath("/tmp/test_" + workflowName + "_" + i + ".gz");
            file.setMetaType("META-TYPE");
            file.setType("TYPE");
            file.setSize(1L);

            //BUG: returns <2100 files? Arrays.asList(new SeqwareWriteService.FileInfo("TYPE", "META-TYPE", "/tmp/test_" + workflowName + ".gz"))
            fs.add(new CreateFile(seqwareClient, workflow, Sets.newHashSet(ius), Collections.EMPTY_LIST, Arrays.asList(file)));
        }
        return fs;
    }

    private class CreateFile implements Callable<Integer> {

        private final SeqwareClient seqwareClient;
        private final Workflow workflow;
        private final Set<IUS> limsKeys;
        private final List<SeqwareObject> parents;
        private final List<FileMetadata> files;

        public CreateFile(SeqwareClient seqwareClient, Workflow workflow, Set<IUS> limsKeys, List<SeqwareObject> parents, List<FileMetadata> files) {
            this.seqwareClient = seqwareClient;
            this.workflow = workflow;
            this.limsKeys = limsKeys;
            this.parents = parents;
            this.files = files;
        }

        @Override
        public Integer call() throws Exception {
            return seqwareClient.createWorkflowRun(workflow, limsKeys, parents, files).getSwAccession();
        }

    }

}
