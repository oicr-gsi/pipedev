/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.on.oicr.pde.utilities;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Collections;
import io.seqware.oozie.action.sge.JobStatus;

/**
 * For testing the polling functionality. JUnit does not work with Timer and
 * TimerTask, hence this class.
 *
 * @author mtaschuk
 */
public class SgeOfflinePoll extends SgeJobPoll {

    private Map<Integer, String> jobs;
    private Map<Integer, String> finjobs;
    private Map<Integer, String> additionalJobs;
    private Map<String, Queue<JobStatus>> status;
    private int iteration=0;


    public Map<String, Queue<JobStatus>> getStatus() {
        return status;
    }

    public void setStatus(Map<String, Queue<JobStatus>> status) {
        this.status = status;
    }

    public void setAdditionalJobs( Map<Integer,String> addJobs) {
	this.additionalJobs = addJobs;
    }

    public SgeOfflinePoll(String[] args) {
        super(args);
	additionalJobs = Collections.EMPTY_MAP;
        setPollInterval(50);
    }

    @Override
    protected Map<Integer, String> findRunningJobs(String jobString) {
	Map<Integer,String> j = Collections.EMPTY_MAP;
	if (iteration == 0) {
	    j = jobs;
	} else if (iteration == 1) {
	    j = additionalJobs;
	}
	iteration++;
        return j;
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
        setDone(true);
    }
}
