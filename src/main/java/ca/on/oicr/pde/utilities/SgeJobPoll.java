package ca.on.oicr.pde.utilities;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.exec.CommandLine;
import org.apache.oozie.action.sge.Invoker;
import org.apache.oozie.action.sge.JobStatus;
import org.apache.oozie.action.sge.StatusChecker;

/**
 *
 */
public class SgeJobPoll extends TimerTask {

    protected StringBuilder stderr = new StringBuilder();
    protected StringBuilder stdout = new StringBuilder();
    private Map<Integer, String> jobs;
    private OptionSet options;
    private String[] parameters;
    private Collection<String> jobIds;
    private Map<String, String[]> mappedJobs;
    public Boolean isSuccessful = null;

    public static void main(String[] args) throws Exception {
        try {
	    
            SgeJobPoll app = new SgeJobPoll(args);
            app.runMe();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public SgeJobPoll(String[] args) {
        this.parameters = args;
        this.jobIds = new HashSet<String>();
        this.mappedJobs = new HashMap<String, String[]>();
    }

    private void init() throws OptionException {
        try {
            OptionParser parser = getOptionParser();
            options = parser.parse(this.getParameters());
        } catch (OptionException e) {
            stderr.append(e.getMessage());
            stderr.append(get_syntax());
            throw e;
        }
    }

    /**
     * Verifies that the parameters make sense
     *
     * @return a ReturnValue object
     */
    private void verifyParameters() throws SgePollException {

        // now look at the options and make sure they make sense
        for (String option : new String[]{
                    "unique-job-string"
                }) {
            if (!options.has(option)) {
                stderr.append(get_syntax());
                stderr.append("Must include parameter: --").append(option).append("\n");
                throw new SgePollException("Must include parameter: --" + option);
            }
        }
    }

    private void verifyInput() throws SgePollException {
        String string = (String) options.valueOf("unique-job-string");
	System.out.println("Starting polling on jobs with extension "+string);
        jobs = findJobs(string);
        for (Integer i : jobs.keySet()) {
            String id = String.valueOf(i);
            this.jobIds.add(id);
            this.mappedJobs.put(id, new String[]{jobs.get(i), ""});
        }
        if (jobs.isEmpty()) {
            stderr.append("No running jobs were found with string ").append(string);
            throw new SgePollException("No running jobs were found with string " + string);
        }
	//System.out.println(printJobs());
    }

    public void runMe() throws SgePollException {
        try {
            init();
            verifyParameters();
            verifyInput();
            try {
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(this, 0, 5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            printLogsToStd();
        }
    }

    protected void printLogsToStd() {
        System.err.println(stderr.toString());
        System.out.println(stdout.toString());
    }

    protected void finish() {
        printLogsToStd();
        if (!isSuccessful) {
            new SgePollException("SGE jobs failed or are in an inconsistent "
                    + "state. See the extended log for details.").printStackTrace();
            System.exit(15);
        }
        System.exit(0);
    }

    private OptionParser getOptionParser() {
        OptionParser parser = new OptionParser();
        parser.accepts("unique-job-string", "A unique string that is attached to all jobs of interest.").withRequiredArg().isRequired();
        return (parser);
    }

    private String get_syntax() {
        OptionParser parser = getOptionParser();
        StringWriter output = new StringWriter();
        try {
            parser.printHelpOn(output);
            return (output.toString());
        } catch (IOException e) {
            stderr.append(e.getMessage());
            return (e.getMessage());
        }
    }

    /**
     * Finds the jobs running as the current user named with the given string.
     *
     * There is a command line string in this method that is used to identify
     * those jobs. Qstat (at OICR at least) seems to have a bug where if you run
     * it with -j, it does not respect the -u flag. To avoid pulling back jobs
     * from other users, we follow this series: <ol><li>Find the job ids with
     * qstat</li> <li>Query that job id with -j and grep out the job number and
     * name onto one line</li> <li>Grep this output for the given identifier
     * </li> </ol>
     *
     * There is a potential bug here if
     *
     * @param jobString
     * @return
     */
    protected Map<Integer, String> findJobs(String jobString) throws SgePollException {
        Map<Integer, String> jobToName = new HashMap<Integer, String>();

        Pattern pat = Pattern.compile(".*job_name:(.*" + jobString + ")");
        String listOfJobs = runACommand("qstat");
        for (String s : listOfJobs.split("\\n")) {
            if (s.substring(0, 1).matches("^[0-9]")) {
                //System.out.println("s:"+s);
                String id = s.split("\\s")[0];
                //System.out.println("id:"+id);
                String jobInfo = runACommand("qstat -j " + id);
                Matcher mat = pat.matcher(jobInfo);
                if (mat.find() == true) {
                    String name = mat.group(1).trim();
                    //System.out.println(name);
                    jobToName.put(Integer.parseInt(id), name);
                } else {
                    System.err.println("No match found in " + id);
                }
            }
        }

        return jobToName;
    }

    private String runACommand(String st) throws SgePollException {
        CommandLine command = CommandLine.parse(st);
        Invoker.Result result = Invoker.invoke(command);
        
        if (result.exit != 0) {
            stderr.append("Exit value from ").append(st).append(":").append(result.exit);
            stderr.append("Result:").append(result.output);
            throw new SgePollException("Command failed: "+st);
        }
        return result.output;

    }

    public String[] getParameters() {
        return parameters;
    }

    /**
     * Monitors the status of jobs submitted to the cluster. If the job is
     * running, ignore it and move on. If the job is successful, log its changed
     * status and remove it from the pool. If the job is completed in any other
     * way, log its status, remove it from the pool, and set the class to failed
     */
    @Override
    public void run() {
        Collection<String> tempJobIds = new HashSet<String>(jobIds);
        for (String jobId : tempJobIds) {
            JobStatus status = checkStatus(jobId);

            if (status == JobStatus.RUNNING) {
                continue;
            } else if (status == JobStatus.LOST) {
		System.out.println("Job "+jobId+" is temporarily unavailable. Continuing.");
		continue;
	    }else if (status == JobStatus.SUCCESSFUL) {
	    } else {
                isSuccessful = false;
            }
            stdout.append(new Date().toString()).append(": Job ").append(jobId).append(" status ").append(status.name()).append("\n");
            jobIds.remove(jobId);

        }
        if (jobIds.isEmpty()) {
            cancel();
            finish();
        }
    }

    @Override
    public boolean cancel() {
        if (isSuccessful == null) {
            isSuccessful = Boolean.TRUE;
        }
        return super.cancel();
    }

    public JobStatus checkStatus(String jobId) {
        StatusChecker.Result result = StatusChecker.check(jobId);
        mappedJobs.get(jobId)[1] = result.status.name();
        return result.status;
    }

    public String printJobs() {
	StringBuffer stdout = new StringBuffer();
        stdout.append("\nJob ID\tJob name\tStatus\n");
        for (String job : mappedJobs.keySet()) {
            stdout.append(job).append("\t").append(mappedJobs.get(job)[0]).append("\t").append(mappedJobs.get(job)[1]);
        }
        stdout.append("Failures so far? ").append(!isSuccessful);
	return stdout.toString();
    }
}
