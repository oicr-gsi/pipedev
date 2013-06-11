/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.oicr.pde.utilities;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.apache.oozie.action.sge.JobStatus;

/**
 * For testing the polling functionality. JUnit does not work with Timer and
 * TimerTask, hence this class.
 *
 * @author mtaschuk
 */
public class SgeOfflinePoll extends SgeJobPoll {

    private Map<Integer, String> jobs;
    private Map<Integer, String> finjobs;
    private Map<String, Queue<JobStatus>> status;

    public Map<String, Queue<JobStatus>> getStatus() {
        return status;
    }

    public void setStatus(Map<String, Queue<JobStatus>> status) {
        this.status = status;
    }

    public SgeOfflinePoll(String[] args) {
        super(args);
        setPollInterval(50);
    }

    @Override
    protected Map<Integer, String> findRunningJobs(String jobString) {
        return jobs;
    }

    @Override
    protected Map<Integer, String> findFinishedJobs(String jobString) throws SgePollException {
        return finjobs;
    }

    public Map<Integer, String> getFinjobs() {
        return finjobs;
    }

    public void setFinjobs(Map<Integer, String> finjobs) {
        this.finjobs = finjobs;
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

    public void addEntry(Map<String, Queue<JobStatus>> status, String jobId, JobStatus... jobStatuses) {
        Queue<JobStatus> queue = new LinkedList<JobStatus>();
        queue.addAll(Arrays.asList(jobStatuses));
        status.put(jobId, queue);
    }

    @Override
    protected void finish() {
        printLogsToStd();
        done = true;
    }
}
