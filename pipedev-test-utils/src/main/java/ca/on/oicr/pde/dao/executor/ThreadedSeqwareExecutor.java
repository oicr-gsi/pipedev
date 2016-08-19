package ca.on.oicr.pde.dao.executor;

import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.sourceforge.seqware.common.model.Workflow;
import net.sourceforge.seqware.common.model.WorkflowRun;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ca.on.oicr.pde.client.SeqwareClient;

public class ThreadedSeqwareExecutor extends ShellExecutor {

    private final ExecutorService sharedPool;
    private final SeqwareClient seqwareClient;
    private final static Logger log = LogManager.getLogger(ThreadedSeqwareExecutor.class);

    public ThreadedSeqwareExecutor(String id, File seqwareDistrubution, File seqwareSettings, File workingDirectory, 
            ExecutorService sharedPool, SeqwareClient seqwareClient) {
        super(id, seqwareDistrubution, seqwareSettings, workingDirectory);
        this.sharedPool = sharedPool;
        this.seqwareClient = seqwareClient;
    }

    @Override
    public void cancelWorkflowRun(WorkflowRun wr) throws IOException {
        executeTask(Executors.callable(new CancelTask(wr.getSwAccession())));
    }

    @Override
    public void cancelWorkflowRuns(Workflow w) {
        List<Callable<Object>> tasks = new ArrayList<>();
        for (WorkflowRunReportRecord wr : seqwareClient.getWorkflowRunRecords(w)) {
            tasks.add(Executors.callable(new CancelTask(Integer.parseInt(wr.getWorkflowRunSwid()))));
        }
        executeTasks(tasks);
    }

    private void executeTask(Callable<Object> task) {
        executeTasks(Arrays.asList(task));
    }

    private void executeTasks(List<Callable<Object>> tasks) {
        try {
            sharedPool.invokeAll(tasks);
        } catch (InterruptedException ie) {
            log.warn("Interupted while executing tasks.");
        }
    }

    private class CancelTask implements Runnable {

        Integer workflowRunSwid;

        public CancelTask(WorkflowRun wr) {
            workflowRunSwid = wr.getSwAccession();
        }

        public CancelTask(Integer id) {
            workflowRunSwid = id;
        }

        @Override
        public void run() {
            //Set the path to the seqware settings for this executor instance.
            //Currently (2014-08-13), the only way to configure io.seqware is through "SEQWARE_SETTINGS"
            System.setProperty("SEQWARE_SETTINGS", seqwareSettings.getAbsolutePath());

            //Execute the cancel task (blocks until complete)
            io.seqware.WorkflowRuns.submitCancel(workflowRunSwid);

            log.printf(Level.DEBUG, "Workflow run [%s] has been cancelled.", workflowRunSwid);
        }
    }

}