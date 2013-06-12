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
import org.apache.oozie.action.sge.Invoker;
import org.apache.oozie.action.sge.JobStatus;
import org.apache.oozie.action.sge.StatusChecker;

/**
 *
 */
public class SgeJobPoll {

    protected StringBuilder stderr = new StringBuilder();
    protected StringBuilder stdout = new StringBuilder();
    private Map<Integer, String> jobs;
    private OptionSet options;
    private String[] parameters;
    private Collection<String> jobIds;
    private Map<String, String[]> mappedJobs;
    public Boolean isSuccessful = null;
    public boolean done = false;
    private int pollInterval = 5000;
    
    /**
     * Get the value of pollInterval
     *
     * @return the value of pollInterval
     */
    public int getPollInterval() {
        return pollInterval;
    }

    /**
     * Set the value of pollInterval
     *
     * @param pollInterval new value of pollInterval
     */
    protected void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }

    public static void main(String[] args) throws Exception {
        SgeJobPoll app = new SgeJobPoll(args);
        app.runMe();
    }

    public SgeJobPoll(String[] args) {
        this.parameters = args;
        this.jobIds = new HashSet<String>();
        this.mappedJobs = new HashMap<String, String[]>();
    }

    protected void init() throws OptionException {
        try {
            OptionParser parser = getOptionParser();
            options = parser.parse(this.getParameters());
        } catch (OptionException e) {
            errPrintln(e.getMessage());
            errPrintln(get_syntax());
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
        String string = (String) options.valueOf("unique-job-string");
        outPrintln("Starting polling on jobs with extension ", string);
        jobs = findRunningJobs(string);
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

    public void runMe() throws Exception {
        try {
	    System.out.println("Initializing");
            init();
	    System.out.println("Verifying parameters");
            verifyParameters();
	    System.out.println("Finding jobs");
            verifyInput();
            while (!done) {
                try {
		    System.out.println(new java.util.Date().toString() + ": Running");
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

    protected void printLogsToStd() {
        System.out.println(stdout.toString());
        System.err.println(stderr.toString());
    }

    protected void printLogsToOutput() {
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

    protected void finish() {
        printLogsToStd();
        printLogsToOutput();
        if (isSuccessful != null && !isSuccessful) {
            new SgePollException("SGE jobs failed or are in an inconsistent "
                    + "state. See the extended log for details.").printStackTrace();
            System.exit(15);
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
        st.append("qacct").append(jobString);
        if (options.has("b")) {
            st.append(" -b ").append(options.valueOf("b")).append(" ");
        }

        return findJobs(jobString, st.toString(), pat, pat2);
    }

    private Map<Integer, String> findJobs(String jobString, String jobFinder, Pattern jobIdPattern, Pattern jobNamePattern) {
        Map<Integer, String> jobToName = new HashMap<Integer, String>();
        String listOfJobs;
        try {
            listOfJobs = runACommand(jobFinder + " -j *" + jobString);
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
                    String name = mat.group(1).trim();
                    jobToName.put(Integer.parseInt(id), name);
                } else {
                    outPrintln("No match found in ", id);
                }
            } catch (SgePollException e) {
                continue;
            }

        }

        return jobToName;
    }

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

    public String[] getParameters() {
        return parameters;
    }

    /**
     * Monitors the status of jobs submitted to the cluster. If the job is
     * running, ignore it and move on. If the job is successful, log its changed
     * status and remove it from the pool. If the job is completed in any other
     * way, log its status, remove it from the pool, and set the class to failed
     */
    public void run() {
        Collection<String> tempJobIds = new HashSet<String>(jobIds);
        for (String jobId : tempJobIds) {
            JobStatus status = checkStatus(jobId);

            if (status == JobStatus.RUNNING) {
                continue;
            } else if (status == JobStatus.LOST) {
                errPrintln("Job ", jobId, " is temporarily unavailable. Continuing.");
                continue;
            } else if (status == JobStatus.SUCCESSFUL) {
            } else {
                isSuccessful = false;
            }
            outPrintln(new Date().toString(), ": Job ", jobId, " status ", status.name());
            jobIds.remove(jobId);

        }
        if (jobIds.isEmpty()) {
            outPrintln("No more jobs!");
            cancel();
            finish();
        }
    }

    public void cancel() {
        if (isSuccessful == null) {
            isSuccessful = Boolean.TRUE;
        }
        done = true;
    }

    public JobStatus checkStatus(String jobId) {
        StatusChecker.Result result = StatusChecker.check(jobId);
        mappedJobs.get(jobId)[1] = result.status.name();
        return result.status;
    }

    public String printJobs() {
        StringBuilder out = new StringBuilder();
        println(out, "\nJob ID\tJob name\tStatus\n");
        for (String job : mappedJobs.keySet()) {
            println(out, job, "\t", mappedJobs.get(job)[0], "\t", mappedJobs.get(job)[1]);
        }
        return out.toString();
    }

    private void errPrintln(String... details) {
        println(stderr, details);
    }

    private void outPrintln(String... details) {
        println(stdout, details);
    }

    private void println(StringBuilder builder, String... details) {
        for (String detail : details) {
            builder.append(detail);
        }
        builder.append("\n");
    }
}
