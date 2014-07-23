package ca.on.oicr.pde.utilities.workflows;

import java.io.File;
import java.util.*;
import net.sourceforge.seqware.common.module.ReturnValue;
import net.sourceforge.seqware.common.util.Log;
import net.sourceforge.seqware.pipeline.workflowV2.AbstractWorkflowDataModel;
import net.sourceforge.seqware.pipeline.workflowV2.model.Job;
import net.sourceforge.seqware.pipeline.workflowV2.model.SqwFile;

/**
 * An extension to SeqWare's workflow class that adds additional OICR-specific functionality.
 *
 * @author mtaschuk
 */
public abstract class OicrWorkflow extends AbstractWorkflowDataModel {

    protected ReturnValue ret = new ReturnValue();
    protected Map<String, Job> jobs = new HashMap<String, Job>();

    /**
     * <p>
     * Returns a list of files from an INI property that is comma-separated.</p>
     *
     * <p>
     * For example in the INI file</p>
     *
     * <code>input_files=/u/me/in1.txt,/u/me/in2.txt</code>
     *
     * <p>
     * If you call this method with the string "input_files", it would return a String array with the contents {"/u/me/in1.txt","u/me/in2.txt"}.</p>
     *
     *
     * @param identifier the name of the property in the INI file that has the comma-separated input files
     * @return an array containing the absolute paths of the input files parsed from "identifier"
     */
    protected String[] getInputFiles(String identifier) {
        List<String> files = new ArrayList<String>();
        try {
            String input = super.getProperty(identifier);
            files.addAll(Arrays.asList(input.split(",")));
        } catch (Exception e) {
            e.printStackTrace();
            ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
        }
        return files.toArray(new String[0]);
    }

    /**
     * <p>
     * Provisions input files to the current working directory in the workflow. Takes a property name from the INI file, splits it on commas, and creates a
     * SqwFile object for each absolute path. Creating a SqwFile object means that the file will be provisioned, or symlinked into the current working directory
     * when the workflow executes. Also saves each file to a map that can be retrieved with this.getFiles().</p>
     *
     * @param identifier the name of the property in the INI file that has the comma-separated input files
     * @return an array of SqwFile objects
     */
    protected SqwFile[] provisionInputFiles(String identifier) {
        String[] files = getInputFiles(identifier);
        Random random = new Random(System.currentTimeMillis());
	int start = random.nextInt(10000);
        SqwFile[] pFiles = new SqwFile[files.length];
        for (int i = 0; i < files.length; i++) {
            SqwFile file = this.createFile("file_in_" + start++);
            file.setSourcePath(files[i]);
            file.setIsInput(true);
            pFiles[i] = file;
        }
        return pFiles;
    }

    /**
     * Retrieves the value of a property from the INI file. Implemented in OicrWorkflow to catch any Exceptions thrown, set the ReturnValue to
     * ReturnValue.INVALIDPARAMETERS, and print the stacktrace.
     *
     * @param key the name of the INI property
     * @return The value of the property named by "key"
     */
    @Override
    public String getProperty(String key) {
        String property = null;
        try {
            property = super.getProperty(key);

        } catch (Exception e) {
            Log.error("Error retrieving property: " + key);
            e.printStackTrace();
            ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
        }
        return property;
    }

    /**
     * Retrieves the value of a property from the INI file or returns the user specified default value. Implemented in OicrWorkflow to catch any Exceptions
     * thrown, set the ReturnValue to ReturnValue.INVALIDPARAMETERS, and print the stacktrace.
     *
     * @param key the name of the INI property to retrieve
     * @param defaultValue the property value that will be returned if the property key/value is not set in the INI
     * @return The value of the property value named by "key"
     */
    public String getOptionalProperty(String key, String defaultValue) {
        String property = defaultValue;
        if (super.hasPropertyAndNotNull(key)) {
            try {
                property = super.getProperty(key);

            } catch (Exception e) {
                property = null;
                Log.error("Error retrieving property that should exist: " + key);
                e.printStackTrace();
                ret.setExitStatus(ReturnValue.INVALIDPARAMETERS);
            }
        }
        return property;
    }

    /**
     * Creates a new BashJob with the given title plus an incremented integer and adds the title and Job to a map of jobs that can be retrieved by
     * this.getJobs().
     *
     * @see #getJobs()
     * @param jobTitle the name of the job in the workflow
     * @return the Job with the given title plus an incremented integer to avoid duplicate names
     */
    protected synchronized Job newJob(String jobTitle) {
        Job job = this.getWorkflow().createBashJob(jobTitle);
        boolean allGood = false;
        int index = 0;
        while (!allGood) {
            String title = jobTitle + "_" + index++;
            if (jobs.get(title) == null) {
                jobs.put(title, job);
                allGood = true;
            }
        }
        return job;
    }

    /**
     * Returns a map of Job titles -> Jobs. Jobs can be added to this map through this.newJob(title);
     *
     * @return Returns a map of Job titles -> Jobs.
     */
    protected Map<String, Job> getJobs() {
        return jobs;
    }

    /**
     * <p>
     * Adds the output files from the INI property named "output_files" to the given job as outputs. The format of the output_files property is the
     * following:</p>
     *
     * <code>output_files=/u/me/out1.txt::txt/plain,/u/me/out2.txt::txt/plain</code>
     *
     * <p>
     * In this example there are two output files both of meta-type txt/plain.</p>
     *
     * <p>
     * Alternatively you can create each output file individually using this.createOutputFile(String,String) and assign them manually to a job.</p>
     *
     * @param endJob the job that produces the output files.
     * @param directory the directory where the file is located
     */
    protected void defineOutputFiles(Job endJob, String directory) throws Exception {
        if (directory == null) {
            directory = "";
        }
        if (!directory.equals("") && !directory.endsWith("/")) {
            directory = directory + "/";
        }
        String outputFiles = getProperty("output_files");
        Boolean doIt = false;
        if (getProperty("manual_output") != null) {
            doIt = Boolean.parseBoolean(getProperty("manual_output").toString());
        }
        if (outputFiles != null && !outputFiles.trim().isEmpty()) {
            int i = 0;
            for (String s : outputFiles.split(",")) {
                String[] outputFile = s.split("::");
                if (outputFile.length != 2) {
                    throw new Exception("The output_files INI property is incorrectly defined at token:" + s);
                };
                SqwFile file = this.createOutputFile(directory + outputFile[0].trim(), outputFile[1].trim(), doIt);
                file.setType(outputFile[1].trim());
                endJob.addFile(file);
            }
        }
    }
    
    protected void defineOutputFiles(Job endJob) throws Exception {
        defineOutputFiles(endJob, "");
    }

    /**
     * Defines an output file with the filetype "plain/txt" located at the given path in the working directory. The manual_output INI property is checked in
     * order to determine whether to set the final path with a random integer or not.
     *
     * @deprecated Use createOutputFile(String, String, boolean) instead
     * @param name Not used
     * @param workingPath the location where the final file will be in the working directory
     * @return a SqwFile that has the source path set and the provisioned path for the final file location.
     */
    protected SqwFile createOutputFile(String name, String workingPath) {
        Boolean doIt = false;
        if (getProperty("manual_output") != null) {
            doIt = Boolean.parseBoolean(getProperty("manual_output").toString());
        }
        return createOutputFile(workingPath, "txt/plain", doIt);
    }

    /**
     * Defines an output file with the given meta-type located at the given path in the working directory. The final location of the file is either in the
     * directory defined by output_prefix and output_dir in the INI file for manualOutput, or at
     * output_prefix/output_dir/WorkflowName_WorkflowVersion/[randomNumber].
     *
     * @param workingPath
     * @param metatype
     * @param manualOutput
     * @return a SqwFile with the source path of the file in the working directory and the provisioned path of the file in the permanent location
     */
    protected SqwFile createOutputFile(String workingPath, String metatype, boolean manualOutput) {
        SqwFile file = new SqwFile();
        file.setForceCopy(true);
        file.setIsOutput(true);
        file.setSourcePath(workingPath);
        file.setType(metatype);
        java.io.File filePath = new java.io.File(workingPath);
        String basename = filePath.getName();
        if (manualOutput) {
            file.setOutputPath(this.getMetadata_output_file_prefix() + getMetadata_output_dir() + "/" + basename);
        } else {
            file.setOutputPath(this.getMetadata_output_file_prefix()
                    + getMetadata_output_dir() + "/" + this.getName() + "_" + this.getVersion() + "/" + this.getRandom() + "/" + basename);
        }

        return file;
    }
}
