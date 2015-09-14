package ca.on.oicr.pde.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.exec.CommandLine;
import io.seqware.oozie.action.sge.Invoker;
import io.seqware.oozie.action.sge.JobStatus;
import io.seqware.oozie.action.sge.StatusChecker;

/**
 * <p>Monitors the status of jobs submitted to the SGE cluster. Searches in both
 * qstat and qacct for jobs with the given identifier, provided on the command
 * line. It then monitors those jobs, polling every few seconds until they
 * complete.</p>
 *
 * <p>In any particular round of polling, if the job is running, it is ignored.
 * If the job is successful, log its changed status and remove it from the pool
 * of polled jobs. If the job is completed in any other way, log its status,
 * remove it from the pool, and log its failed status.</p>
 *
 * <p>Jobs may occasionally have transient errors, which usually occur when they
 * transition from running to finished (move from qstat to qacct). This abnormal
 * only when the state persists for longer than a few minutes.</p>
 *
 * @author Morgan Taschuk
 */
public class SgeJobPoll {

    private StringBuilder stderr = new StringBuilder();
    private StringBuilder stdout = new StringBuilder();
    private OptionSet options;
//    private String[] parameters;
    private Collection<String> jobIds;
    private Map<String, String[]> mappedJobs;
    private Boolean isSuccessful = null;
    private boolean done = false;
    private int pollInterval = 5000;

    /**
     * Get the polling interval in milliseconds.
     *
     * @return the polling interval. Default is 5000.
     */
    public int getPollInterval() {
        return pollInterval;
    }

    /**
     * Set the polling interval in milliseconds.
     *
     * @param pollInterval new polling interval
     */
    protected void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }

    /**
     * Returns if the polling is completed.
     *
     * @return true if completed, false if still polling
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Set whether the polling is completed. Setting this value to true will
     * cause the polling to stop.
     *
     * @param done whether the polling is completed
     */
    protected void setDone(boolean done) {
        this.done = done;
    }

    /**
     * Returns whether the jobs completed successfully or not. This value is
     * null if polling is not completed.
     *
     * @return true if the jobs all succeeded, false if one or more jobs failed,
     * or null if the jobs are not complete.
     */
    public Boolean isSuccessful() {
        return isSuccessful;
    }

    /**
     * Set whether the jobs completed successfully or not. Can be used in
     * conjunction with setDone to modify the behaviour of the poller.
     *
     * @param isSuccessful true if the jobs all succeeded, false if one or more
     * jobs failed, and null if the jobs are still running
     */
    protected void setSuccessful(Boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }

    /**
     * Creates a new SgeJobPoll object and executes the runMe() method. Catches
     * any exceptions thrown and exits with error code 15.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            SgeJobPoll app = new SgeJobPoll(args);
            app.runMe();
        } catch (Exception e) {
            System.err.println("Erred out with status: " + e.getMessage());
            System.exit(15);
        }
        System.err.println("Program finished unexpectedly");
        System.exit(-1);
    }

    /**
     * Constructor for SgeJobPoll. Initializes variables and parses the command
     * line arguments.
     *
     * @param args the command line arguments
     */
    public SgeJobPoll(String[] args) {
        this.jobIds = new HashSet<String>();
        this.mappedJobs = new HashMap<String, String[]>();
        try {
            OptionParser parser = getOptionParser();
            options = parser.parse(args);
        } catch (OptionException e) {
            stderr.append(e.getMessage()).append("\n");
            stderr.append(get_syntax()).append("\n");
            throw e;
        }
    }

    /**
     * Verifies that the arguments given on the command line make sense.
     * Specifies that 'unique-job-string' and 'output-file' are required
     * arguments.
     *
     * @throws SgePollException when the required arguments are not provided
     */
    private void verifyParameters() throws SgePollException {
        System.out.println("Verifying parameters");
        // now look at the options and make sure they make sense
        for (String option : new String[]{
                    "unique-job-string", "output-file"
                }) {
            if (!options.has(option)) {
                errPrintln(get_syntax());
                errPrintln("Must include parameter: --", option);
                throw new SgePollException("Must include parameter: --" + option);
            }
        }
    }

    private void verifyInput() throws SgePollException {
        System.out.println("Finding jobs");
        String string = (String) options.valueOf("unique-job-string");
        outPrintln("Starting polling on jobs with extension ", string);
	Map<Integer, String> jobs = findRunningJobs(string);
        outPrintln("Number of running jobs:" + jobs.keySet().size());
        jobs.putAll(findFinishedJobs(string));
        outPrintln("Number of running + finished jobs:", String.valueOf(jobs.keySet().size()));
	for (Integer i : jobs.keySet()) {
            String id = String.valueOf(i);
	    this.jobIds.add(id);
	    this.mappedJobs.put(id, new String[]{jobs.get(i), ""});
        }
        if (jobs.isEmpty()) {
            errPrintln("No jobs were found with string ", string);
            throw new SgePollException("No running jobs were found with string " + string);
        }
    }

    private void addRunningJobs() throws SgePollException {
	System.out.println("Finding running jobs");
	String string = (String) options.valueOf("unique-job-string");
	Map<Integer, String> jobs = findRunningJobs(string);
        outPrintln("Number of still running jobs:" + jobs.keySet().size());
	for (Integer i : jobs.keySet()) {
            String id = String.valueOf(i);
	    this.jobIds.add(id);
	    this.mappedJobs.put(id, new String[]{jobs.get(i), ""});
        }
    }

    /**
     * Monitors the status of jobs submitted to the cluster. If the job is
     * running, ignore it and move on. If the job is successful, log its changed
     * status and remove it from the pool. If the job is completed in any other
     * way, log its status, remove it from the pool, and set the class to failed
     *
     * @throws Exception if the command line arguments are not correct, if no
     * jobs can be found, or if there is some other error while polling.
     */
    public void runMe() throws Exception {
        try {
            verifyParameters();
            verifyInput();
            while (!done) {
                try {
                    this.run();
                    Thread.sleep(pollInterval);
                } catch (Exception e) {
                    e.printStackTrace();
                    done = true;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            finish();
        }
    }

    /**
     * Prints the stdout and stderr that have been captured thus far to the
     * command line.
     */
    protected void printLogsToStd() {
        System.out.println(stdout.toString());
        System.err.println(stderr.toString());
    }

    private void printLogsToOutput() {
        try {
            if (options.has("output-file")) {
                File file = new File(options.valueOf("output-file").toString());
                FileWriter writer = new FileWriter(file);
                writer.append(new Date() + "\n");
                writer.append(printJobs());
                writer.flush();
                writer.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Prints the logs to standard out, prints to the log file, and exits the
     * poller with an exit status if any jobs failed. Exits with 15 if any jobs
     * failed, with 1 if the success was not determined, and 0 otherwise.
     */
    protected void finish() {
        printLogsToStd();
        printLogsToOutput();
        if (isSuccessful != null && !isSuccessful) {
            System.err.println("SGE jobs failed or are in an inconsistent "
                    + "state. See the extended log for details.");
	    System.err.println(printJobs());
            System.exit(15);
        } else if (isSuccessful == null) {
            System.err.println("Polling was not completed or isSuccessful was "
                    + "not set");
            System.err.println(printJobs());
	    System.exit(1);
        }
        System.exit(0);
    }

    private OptionParser getOptionParser() {
        OptionParser parser = new OptionParser();
        parser.accepts("unique-job-string", "A unique string that is attached to all jobs of interest.").withRequiredArg().isRequired();
        parser.acceptsAll(Arrays.asList("output-file", "o"), "A location for an output file describing the finished jobs").withRequiredArg();
        parser.acceptsAll(Arrays.asList("begin-time", "b"), "The earliest start time for jobs to be summarized, in the format [[CC]YY]MMDDhhmm[.SS]").withRequiredArg();
        return (parser);
    }

    private String get_syntax() {
        OptionParser parser = getOptionParser();
        StringWriter output = new StringWriter();
        try {
            parser.printHelpOn(output);
            return (output.toString());
        } catch (IOException e) {
            errPrintln(e.getMessage());
            return (e.getMessage());
        }
    }

    /**
     * Finds the jobs running as the current user named with the given string.
     *
     * There is a command line string in this method that is used to identify
     * those jobs. Qstat (at OICR at least) seems to have a bug where if you run
     * it with -j, it does not respect the -u flag. To avoid pulling back jobs
     * from other users, we follow this series:
     *
     * <ol><li>Find the job ids with qstat</li>
     *
     * <li>Query that job id with -j and grep out the job name</li>
     *
     * <li>Grep this output for the given identifier</li></ol>
     *
     * @param jobString
     * @return
     */
    protected Map<Integer, String> findRunningJobs(String jobString) throws SgePollException {
        Pattern pat = Pattern.compile(".*job_number:(.*)");
        Pattern pat2 = Pattern.compile(".*job_name:(.*)");
        return findJobs(jobString, "qstat ", pat, pat2);
    }

    /**
     * Finds the jobs finished recently with the given string.
     *
     *
     * @param jobString
     * @return
     */
    protected Map<Integer, String> findFinishedJobs(String jobString) throws SgePollException {

        Pattern pat = Pattern.compile(".*jobnumber(.*)");
        Pattern pat2 = Pattern.compile(".*jobname(.*)");

        StringBuilder st = new StringBuilder();
        st.append("qacct ");
        if (options.has("b")) {
            st.append(" -b ").append(options.valueOf("b")).append(" ");
        }

        return findJobs(jobString, st.toString(), pat, pat2);
    }

    private Map<Integer, String> findJobs(String jobString, String jobFinder, Pattern jobIdPattern, Pattern jobNamePattern) {
        Map<Integer, String> jobToName = new HashMap<Integer, String>();
        String listOfJobs;
        try {
            listOfJobs = runACommand(jobFinder + " -j *" + jobString+"*");
	    System.out.println(listOfJobs);
        } catch (SgePollException e) {
            return jobToName;
        }

        Matcher mat = jobIdPattern.matcher(listOfJobs);

        while (mat.find()) {
            String id = mat.group(1).trim();
            try {
                String jobInfo = runACommand(jobFinder + " -j " + id);
                Matcher mat2 = jobNamePattern.matcher(jobInfo);
                if (mat2.find() == true) {
                    String name = mat2.group(1).trim();
                    jobToName.put(Integer.parseInt(id), name);
                } else {
                    outPrintln("No match found in ", id);
                }
            } catch (SgePollException e) {
                continue;
            }

        }
	System.out.println("New jobs: "+jobToName.keySet().size());
        return jobToName;
    }

    /**
     * Runs a command on the command line. Ignores exit code 1 as an acceptable error.
     * @param st the command to run
     * @return the text result from the executed command, or empty string if it exited with a 1.
     * @throws SgePollException if the command execution returns an exit code 
     * other than 0 or 1.
     */
    protected String runACommand(String st) throws SgePollException {
        CommandLine command = CommandLine.parse(st);
        Invoker.Result result = Invoker.invoke(command);
        String output = result.output;
        if (result.exit == 1) {
            output = "";
            errPrintln(st, " command returned ", String.valueOf(result.exit), ":", result.output);
        } else if (result.exit != 0) {
            errPrintln("Exit value from ", st, ":", String.valueOf(result.exit));
            throw new SgePollException("Command failed: " + st);
        }
        return output;

    }

    private void run() throws SgePollException {
        System.out.println(new java.util.Date().toString() + ": Running");
	
        while (!jobIds.isEmpty()) {
	    monitorCurrentJobs();
	    addRunningJobs();
        }
	outPrintln("No more jobs!");
        cancel();
        finish();

    }

    private void monitorCurrentJobs() {
        Collection<String> tempJobIds = new HashSet<String>(jobIds);
        for (String jobId : tempJobIds) {
            JobStatus status = checkStatus(jobId);
            if (status == JobStatus.RUNNING) {
                continue;
            } else if (status == JobStatus.LOST) {
                System.out.println("Job " + jobId + " is temporarily unavailable. Continuing.");
                continue;
            } else if (status == JobStatus.SUCCESSFUL) {
            } else {
                isSuccessful = false;
            }
            outPrintln(new Date().toString(), ": Job ", jobId, " status ", status.name());
            jobIds.remove(jobId);
        }  
    }

    private void cancel() {
        if (isSuccessful == null) {
            isSuccessful = Boolean.TRUE;
        }
        done = true;
    }

    /**
     * Check the JobStatus of the particular job ID.
     *
     * @param jobId the job number
     * @return the Job Status
     */
    protected JobStatus checkStatus(String jobId) {
        StatusChecker.Result result = StatusChecker.check(jobId);
        mappedJobs.get(jobId)[1] = result.status.name();
        return result.status;
    }

    private String printJobs() {
        StringBuilder out = new StringBuilder();
        println(out, "\nJob ID\tJob name\tStatus\n");
        for (String job : mappedJobs.keySet()) {
            println(out, job, "\t", mappedJobs.get(job)[0], "\t", mappedJobs.get(job)[1]);
        }
        return out.toString();
    }

    /**
     * Append a line with the given arguments to the standard error log. This will only be printed when
     * execution is completed.
     *
     * @param details Strings to append in a single line to the stderr log.
     */
    protected void errPrintln(String... details) {
        println(stderr, details);
    }
    /**
     * Append a line with the given arguments to the standard out log. This will only be printed when
     * execution is completed.
     *
     * @param details Strings to append in a single line to the stdout log.
     */
    protected void outPrintln(String... details) {
        println(stdout, details);
    }

    private void println(StringBuilder builder, String... details) {
        for (String detail : details) {
            builder.append(detail);
        }
        builder.append("\n");
    }
}
