package ca.on.oicr.pde.utilities;

import ca.on.oicr.pde.dao.SeqwareService;
import ca.on.oicr.pde.model.Workflow;
import ca.on.oicr.pde.model.WorkflowRun;
import ca.on.oicr.pde.model.WorkflowRunReportRecord;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadedSeqwareExecutor extends ShellExecutor {

    private final ExecutorService sharedPool;
    private final SeqwareService seqwareService;
    private final static Logger log = LogManager.getLogger(ThreadedSeqwareExecutor.class);

    public ThreadedSeqwareExecutor(String id, File seqwareDistrubution, File seqwareSettings, File workingDirectory, ExecutorService sharedPool, SeqwareService seqwareService) {

        super(id, seqwareDistrubution, seqwareSettings, workingDirectory);

        this.sharedPool = sharedPool;

        this.seqwareService = seqwareService;

    }

    @Override
    public void cancelWorkflowRun(WorkflowRun wr) throws IOException {
        executeTask(Executors.callable(new CancelTask(wr.getSwid())));
    }

    @Override
    public void cancelWorkflowRuns(Workflow w) {
        List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
        for (WorkflowRunReportRecord wr : seqwareService.getWorkflowRunRecords(w)) {
            tasks.add(Executors.callable(new CancelTask(wr.getWorkflowRunSwid())));
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
            workflowRunSwid = Integer.parseInt(wr.getSwid());
        }

        public CancelTask(String id) {
            workflowRunSwid = Integer.parseInt(id);
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