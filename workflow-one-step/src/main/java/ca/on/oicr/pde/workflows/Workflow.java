package ca.on.oicr.pde.workflows;

import ca.on.oicr.pde.utilities.workflows.OicrWorkflow;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sourceforge.seqware.pipeline.workflowV2.model.Job;

public class Workflow extends OicrWorkflow {

    /**
     * Builds a one step workflow, running one user-defined script and one
     * monitoring job for monitoring the jobs submitted to the cluster by the
     * script.
     */
    @Override
    public void buildWorkflow(){
        //Runs the script you submit to the Job
        Job job = myScriptJob();

        // You shouldn't need to modify this
        // If your script does not submit jobs to the cluster or 'blocks', meaning
        // that it pauses execution until completion, then you do not need the monitoring
        // step. Remove the following line and amend the output files so that they 
        // are defined for the job above. e.g. defineOutputFiles(job);
        Job monitor = monitorSgeJobs(job);
	try {
        defineOutputFiles(monitor);
	} catch(Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    /**
     * Creates a job that wraps a user-provided script. Modify this method if
     * necessary.
     *
     * @return the Job that describes
     */
    private Job myScriptJob() {
        //Set up the job
        Job job1 = newJob("RunScript");
        job1.setMaxMemory(getProperty("my_script_mem_mb"));

        //Create the command
        String command = String.format("%s %s >> output", 
                getProperty("my_script"), getProperty("my_script_parameters"));
        job1.getCommand().addArgument(command);
	job1.setQueue(getOptionalProperty("queue",""));
        return job1;
    }

    /**
     * Monitors the qsubs from the script in the previous job. The monitoring
     * depends on the 'unique-job-string' being a part of the name of all Jobs
     * produced in the user's script.
     *
     * You shouldn't need to modify this method.
     *
     * @param parent the Job wrapping the script that spawned the qsubs
     */
    private Job monitorSgeJobs(Job parent) {
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmm.ss");

        String logFile = getProperty("log_file");
        
        Job monitorJob = newJob("JobMonitor");
        String command = String.format("java -jar -Xmx500M %s --unique-job-string %s --output-file %s --begin-time %s >> %s", 
                getProperty("monitor_script"), getProperty("unique_string"), getProperty("log_file"), format.format(new Date()), logFile + ".extended");
        monitorJob.getCommand().addArgument(command);
        monitorJob.setQueue(getOptionalProperty("queue",""));
        monitorJob.addParent(parent);
        monitorJob.addFile(this.createOutputFile("FinalLogFile", logFile));
        return monitorJob;

    }
}
