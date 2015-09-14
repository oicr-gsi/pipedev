package ca.on.oicr.pde.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import junit.framework.Assert;
import junit.framework.TestCase;
import io.seqware.oozie.action.sge.JobStatus;

/**
 *
 * @author mtaschuk
 */
public class SgeJobPollTest extends TestCase {

    public SgeJobPollTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testErrorInRunACommand() throws Exception {
        System.out.println("testErrorInRunACommand()");
        SgeJobPoll errorPoller = new SgeJobPoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"}) {

            @Override
            protected void finish() {
                printLogsToStd();
                setDone(true);
            }

            @Override
            protected String runACommand(String st) throws SgePollException {
                SgePollException e = new SgePollException("Testing error states");
                throw e;
            }
        };
        
        Assert.assertTrue("Finished jobs not empty?", errorPoller.findFinishedJobs("1234").isEmpty());
        Assert.assertTrue("Running jobs not empty?", errorPoller.findRunningJobs("1234").isEmpty());
    }

    public void testParseQstatJobs() throws Exception {
        System.out.println("testParseQstatJobs");

        String[] qstatFiles = {"qstat9608118", "qstat9791754"};
        final StringBuilder[] qstat = new StringBuilder[qstatFiles.length];
        Arrays.fill(qstat, new StringBuilder());

        try {
            int i = 0;
            for (String st : qstatFiles) {
                BufferedReader reader = new BufferedReader(new FileReader(this.getClass().getResource(st).getPath()));
                String line = reader.readLine();
                while (line != null) {
                    //ugh this makes me cringe but it is only done once (twice!)
                    qstat[i].append(line).append("\n");
                    line = reader.readLine();
                }
                i++;
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        SgeJobPoll errorPoller = new SgeJobPoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"}) {

            @Override
            protected void finish() {
                printLogsToStd();
                setDone(true);
            }

            @Override
            protected String runACommand(String st) throws SgePollException {
                if (st.contains("*")) {
                    return qstat[0].toString() + qstat[1].toString();
                } else if (st.contains("9608118")) {
                    return qstat[0].toString();
                } else if (st.contains("9791754")) {
                    return qstat[1].toString();
                } else {
                    Assert.fail("");
                    throw new SgePollException("No idea what you're asking for!");
                }
            }
        };
        
        Map<Integer, String> jobs = errorPoller.findRunningJobs("1234");
        Assert.assertEquals("Wrong number of jobs!", 2, jobs.size());
        Assert.assertNotNull("Job doesn't exist", jobs.get(9608118));
        Assert.assertNotNull("Job doesn't exist", jobs.get(9791754));
    }

    public void testParseQacctJobs() throws Exception {
        System.out.println("testParseQacctJobs");


        String[] qacctFiles = {"qacct9615552", "qacct9616062", "qacct76949030"};
        final StringBuilder[] qacct = new StringBuilder[qacctFiles.length];
        Arrays.fill(qacct, new StringBuilder());

        try {
            int i = 0;
            for (String st : qacctFiles) {
                BufferedReader reader = new BufferedReader(new FileReader(this.getClass().getResource(st).getPath()));
                String line = reader.readLine();
                while (line != null) {
                    //ugh this makes me cringe but it is only done once (twice!)
                    qacct[i].append(line).append("\n");
                    line = reader.readLine();
                }
                i++;
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        SgeJobPoll errorPoller = new SgeJobPoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"}) {

            @Override
            protected void finish() {
                printLogsToStd();
                setDone(true);
            }

            @Override
            protected String runACommand(String st) throws SgePollException {
                if (st.contains("*")) {
                    return qacct[0].toString() + qacct[1].toString() + qacct[2].toString();
                } else if (st.contains("9615552")) {
                    return qacct[0].toString();
                } else if (st.contains("9616062")) {
                    return qacct[1].toString();
                } else if (st.contains("76949030")) {
		    return qacct[2].toString();
		} else {
                    Assert.fail("");
                    throw new SgePollException("No idea what you're asking for!");
                }
            }
        };
        
        Map<Integer, String> jobs = errorPoller.findFinishedJobs("1234");
        Assert.assertEquals("Wrong number of jobs!", 3, jobs.size());
        Assert.assertNotNull("Job doesn't exist", jobs.get(9615552));
        Assert.assertNotNull("Job doesn't exist", jobs.get(9616062));
	Assert.assertNotNull("Job doesn't exist", jobs.get(76949030));

    }

    public void testNoJobs() throws Exception {
        System.out.println("testNoJobs");
        SgeOfflinePoll poller = new SgeOfflinePoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"});
        Map<Integer, String> jobs = new HashMap<Integer, String>();
        poller.setJobs(jobs);

        Map<Integer, String> finjobs = new HashMap<Integer, String>();
        poller.setFinjobs(finjobs);

        Map<String, Queue<JobStatus>> status = new HashMap<String, Queue<JobStatus>>();
        poller.setStatus(status);
        try {
            poller.runMe();
            Assert.fail("Sge poller should not succeed when it can't find any jobs");
        } catch (Exception ignore) {
        }
    }

    public void testSuccessfulJobs() throws Exception {
        System.out.println("testSuccessfulJobs");
        SgeOfflinePoll poller = new SgeOfflinePoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"});
        Map<Integer, String> jobs = new HashMap<Integer, String>();
        jobs.put(1357, "Job1-1234");
        jobs.put(85950395, "Job2-1234");
        poller.setJobs(jobs);

        Map<Integer, String> finjobs = new HashMap<Integer, String>();
        finjobs.put(46745678, "Job0-1234");
        poller.setFinjobs(finjobs);

        Map<String, Queue<JobStatus>> status = new HashMap<String, Queue<JobStatus>>();
        poller.addEntry(status, "1357", JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.SUCCESSFUL);
        poller.addEntry(status, "85950395", JobStatus.RUNNING, JobStatus.SUCCESSFUL);
        poller.addEntry(status, "46745678", JobStatus.SUCCESSFUL);
        poller.setStatus(status);
        poller.runMe();
        Assert.assertTrue("One of the jobs failed when they should have succeeded", poller.isSuccessful());
    }

    public void testFailedRunningJob() throws Exception {
        System.out.println("testFailedRunningJobs");
        SgeOfflinePoll poller = new SgeOfflinePoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"});
        Map<Integer, String> jobs = new HashMap<Integer, String>();
        jobs.put(1357, "Job1-1234");
        jobs.put(85950395, "Job2-1234");
        poller.setJobs(jobs);

        Map<Integer, String> finjobs = new HashMap<Integer, String>();
        finjobs.put(46745678, "Job0-1234");
        poller.setFinjobs(finjobs);

        Map<String, Queue<JobStatus>> status = new HashMap<String, Queue<JobStatus>>();
        poller.addEntry(status, "1357", JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.FAILED);
        poller.addEntry(status, "85950395", JobStatus.RUNNING, JobStatus.SUCCESSFUL);
        poller.addEntry(status, "46745678", JobStatus.SUCCESSFUL);
        poller.setStatus(status);

        poller.runMe();
        Assert.assertFalse("Poller didn't find failed running job", poller.isSuccessful());
    }

    public void testFailedFinishedJob() throws Exception {
        System.out.println("testFailedFinishedJob");
        SgeOfflinePoll poller = new SgeOfflinePoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"});
        Map<Integer, String> jobs = new HashMap<Integer, String>();
        jobs.put(1357, "Job1-1234");
        jobs.put(85950395, "Job2-1234");
        poller.setJobs(jobs);

        Map<Integer, String> finjobs = new HashMap<Integer, String>();
        finjobs.put(46745678, "Job0-1234");
        poller.setFinjobs(finjobs);

        Map<String, Queue<JobStatus>> status = new HashMap<String, Queue<JobStatus>>();
        poller.addEntry(status, "1357", JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.SUCCESSFUL);
        poller.addEntry(status, "85950395", JobStatus.RUNNING, JobStatus.SUCCESSFUL);
        poller.addEntry(status, "46745678", JobStatus.EXIT_ERROR);
        poller.setStatus(status);

        poller.runMe();
        Assert.assertFalse("Poller did not find failed finished job", poller.isSuccessful());
    }

    public void testFailedRunningAndFinishedJobs() throws Exception {
        System.out.println("testFailedRunningAndFinishedJobs");
        SgeOfflinePoll poller = new SgeOfflinePoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"});
        Map<Integer, String> jobs = new HashMap<Integer, String>();
        jobs.put(1357, "Job1-1234");
        jobs.put(85950395, "Job2-1234");
        poller.setJobs(jobs);

        Map<Integer, String> finjobs = new HashMap<Integer, String>();
        finjobs.put(46745678, "Job0-1234");
        poller.setFinjobs(finjobs);

        Map<String, Queue<JobStatus>> status = new HashMap<String, Queue<JobStatus>>();
        poller.addEntry(status, "1357", JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.FAILED);
        poller.addEntry(status, "85950395", JobStatus.RUNNING, JobStatus.SUCCESSFUL);
        poller.addEntry(status, "46745678", JobStatus.EXIT_ERROR);
        poller.setStatus(status);

        poller.runMe();
        Assert.assertFalse("Poller did not find failed running and/or finished jobs", poller.isSuccessful());
    }

    public void testAdditionalJobsSuccess() throws Exception {
        System.out.println("testAdditionalJobsSuccess");
        SgeOfflinePoll poller = new SgeOfflinePoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"});
        Map<Integer, String> jobs = new HashMap<Integer, String>();
        jobs.put(1357, "Job1-1234");
        jobs.put(85950395, "Job2-1234");
        poller.setJobs(jobs);

        Map<Integer, String> finjobs = new HashMap<Integer, String>();
        finjobs.put(46745678, "Job0-1234");
        poller.setFinjobs(finjobs);


	Map<Integer,String> addJobs = new HashMap<Integer, String>();
	addJobs.put(1098765, "Job4-1234");
	poller.setAdditionalJobs(addJobs);


        Map<String, Queue<JobStatus>> status = new HashMap<String, Queue<JobStatus>>();
        poller.addEntry(status, "1357", JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.SUCCESSFUL);
        poller.addEntry(status, "85950395", JobStatus.RUNNING, JobStatus.SUCCESSFUL);
        poller.addEntry(status, "46745678", JobStatus.SUCCESSFUL);
	poller.addEntry(status, "1098765", JobStatus.SUCCESSFUL);
        poller.setStatus(status);

        poller.runMe();
        Assert.assertTrue("Poller did not find finished jobs", poller.isSuccessful());
    }


    public void testAdditionalJobsFail() throws Exception {
        System.out.println("testAdditionalJobsFail");
        SgeOfflinePoll poller = new SgeOfflinePoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"});
        Map<Integer, String> jobs = new HashMap<Integer, String>();
        jobs.put(1357, "Job1-1234");
        jobs.put(85950395, "Job2-1234");
        poller.setJobs(jobs);

        Map<Integer, String> finjobs = new HashMap<Integer, String>();
        finjobs.put(46745678, "Job0-1234");
        poller.setFinjobs(finjobs);


        Map<Integer,String> addJobs = new HashMap<Integer, String>();
        addJobs.put(1098765, "Job4-1234");
        poller.setAdditionalJobs(addJobs);


        Map<String, Queue<JobStatus>> status = new HashMap<String, Queue<JobStatus>>();
        poller.addEntry(status, "1357", JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.SUCCESSFUL);
        poller.addEntry(status, "85950395", JobStatus.RUNNING, JobStatus.SUCCESSFUL);
        poller.addEntry(status, "46745678", JobStatus.SUCCESSFUL);
        poller.addEntry(status, "1098765", JobStatus.RUNNING, JobStatus.EXIT_ERROR);
        poller.setStatus(status);

        poller.runMe();
        Assert.assertFalse("Poller did not find failed jobs", poller.isSuccessful());
    }

    public void testLostJobsSuccess() throws Exception {
        System.out.println("testLostJobsSuccess");
        SgeOfflinePoll poller = new SgeOfflinePoll(new String[]{"--unique-job-string", "1234", "--output-file", "/tmp/log.txt"});
        Map<Integer, String> jobs = new HashMap<Integer, String>();
        jobs.put(1357, "Job1-1234");
        jobs.put(85950395, "Job2-1234");
        poller.setJobs(jobs);

        Map<Integer, String> finjobs = new HashMap<Integer, String>();
        finjobs.put(46745678, "Job0-1234");
        poller.setFinjobs(finjobs);



        Map<String, Queue<JobStatus>> status = new HashMap<String, Queue<JobStatus>>();
        poller.addEntry(status, "1357", JobStatus.RUNNING, JobStatus.RUNNING, JobStatus.SUCCESSFUL);
        poller.addEntry(status, "85950395", JobStatus.RUNNING, JobStatus.LOST, JobStatus.LOST,JobStatus.LOST,JobStatus.LOST,JobStatus.SUCCESSFUL);
        poller.addEntry(status, "46745678", JobStatus.SUCCESSFUL);
        poller.setStatus(status);

        poller.runMe();
        Assert.assertTrue("Poller did not treat lost job properly", poller.isSuccessful());
    }


}
