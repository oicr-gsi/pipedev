/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.oicr.pde.utilities;

import java.util.*;
import org.apache.oozie.action.sge.JobStatus;

/**
 * For testing the polling functionality. JUnit does not work with Timer and
 * TimerTask, hence this class.
 *
 * @author mtaschuk
 */
public class SgeOfflinePoll extends SgeJobPoll {

    private Map<Integer, String> jobs;
    private Map<String, Queue<JobStatus>> status;

    public static void main(String[] args) throws Exception {
        testSuccess();
        testFailure();
    }

    public Map<String, Queue<JobStatus>> getStatus() {
        return status;
    }

    public void setStatus(Map<String, Queue<JobStatus>> status) {
        this.status = status;
    }

    public SgeOfflinePoll(String[] args) {
        super(args);
    }

    @Override
    protected Map<Integer, String> findJobs(String jobString) {
        return jobs;
    }

    public Map<Integer, String> getJobs() {
        return jobs;
    }

    public void setJobs(Map<Integer, String> jobs) {
        this.jobs = jobs;
    }

    @Override
    public JobStatus checkStatus(String jobId) {
        return status.get(jobId).poll();
    }

    public static void testSuccess() throws Exception {
        try {
            System.out.println("");
            SgeOfflinePoll poller = new SgeOfflinePoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"}) {

                @Override
                protected void finish() {
                    printLogsToStd();
                    if (isSuccessful == Boolean.TRUE) {
                        System.err.println("testSuccess\tSUCCESS");
                    } else if (isSuccessful == Boolean.FALSE) {
                        System.err.println("testSuccess\tFAIL");
                    }
                    done = true;
                }
            };

            Map<Integer, String> jobs = new HashMap<Integer, String>();
            jobs.put(1357, "Job1-1234");
            jobs.put(85950395, "Job2-1234");
            poller.setJobs(jobs);

            Map<String, Queue<JobStatus>> status = new HashMap<String, Queue<JobStatus>>();
            addEntry(status, "1357", JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.SUCCESSFUL);
            addEntry(status, "85950395", JobStatus.RUNNING, JobStatus.SUCCESSFUL);
            poller.setStatus(status);

            poller.runMe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testFailure() throws Exception {
        try {
            System.out.println("");
            SgeOfflinePoll poller = new SgeOfflinePoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"}) {

                @Override
                protected void finish() {
                    printLogsToStd();
                    if (isSuccessful == Boolean.FALSE) {
                        System.err.println("testFailure\tSUCCESS");
                    } else if (isSuccessful == Boolean.TRUE) {
                        System.err.println("testFailure\tFAIL");
                    }
                    done = true;
                }
            };

            Map<Integer, String> jobs = new HashMap<Integer, String>();
            jobs.put(1357, "Job1-1234");
            jobs.put(85950395, "Job2-1234");
            poller.setJobs(jobs);

            Map<String, Queue<JobStatus>> status = new HashMap<String, Queue<JobStatus>>();
            addEntry(status, "1357", JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.FAILED);
            addEntry(status, "85950395", JobStatus.RUNNING, JobStatus.SUCCESSFUL);
            poller.setStatus(status);

            poller.runMe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addEntry(Map<String, Queue<JobStatus>> status, String jobId, JobStatus... jobStatuses) {
        Queue<JobStatus> queue = new LinkedList<JobStatus>();
        queue.addAll(Arrays.asList(jobStatuses));
        status.put(jobId, queue);
    }
}
