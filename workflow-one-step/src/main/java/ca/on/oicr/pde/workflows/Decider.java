package ca.on.oicr.pde.workflows;

import ca.on.oicr.pde.deciders.*;
import java.util.Arrays;
import java.util.UUID;
import net.sourceforge.seqware.common.module.ReturnValue;

/**
 *
 * @author mtaschuk
 */
public class Decider extends OicrDecider {

    public Decider() {
        super();
        defineArgument("script", "The absolute path of your script in this workflow", true);
        defineArgument("parameters", "Static parameters needed for your script. These would not vary with different samples.", false);
        defineArgument("memory", "Amount of memory needed to run your script, in MB. Default: 2000", false);
        defineArgument("commit-version", "The code that describes the current version of the script", false);
    }

    /**
     * Initialize the type of file(s) you want to read in.
     *
     * <p>For a full list of metatypes, see
     * http://seqware.github.io/docs/6-pipeline/file-types/ (follow the link on
     * that page to the Google Doc).</p>
     *
     * @return a ReturnValue describing the success or failure of initialization
     */
    @Override
    public ReturnValue init() {
        //FastQ files
        //group by IUS identifier (1 library = 1 IUS = 1 barcode = 2 fastq files)
        this.setMetaType(Arrays.asList("chemical/seq-na-fastq-gzip"));
        this.setGroupBy(Group.BARCODE, false);
        this.setNumberOfFilesPerGroup(2);

        return super.init();
    }

    /**
     * Customize the execution of each workflow that will be run by this decider.
     * This method runs once per group of files to be launched together.
     * The WorkflowRun passed in contains the files that will be used in this 
     * particular run, as well as their attributes (e.g. sequencer run, donor, any
     * Lims attributes. Here, you can set the script, parameters, and memory 
     * as well as the output files.
     * @param run an object that describes one complete workflow run (including
     * files and all parameters)
     * @return 
     */
    @Override
    public ReturnValue customizeRun(WorkflowRun run) {
        ret = super.customizeRun(run);

        
        run.setScript(getArgument("script"));
        run.setScriptMemory(getArgument("memory"));
        run.setManualOutput(false);
        run.addProperty("commit_version", getArgument("commit-version"), "No version supplied");

        FileAttributes[] files = run.getFiles();
	if (files.length != 2)
	 	return ret;
        //Pull the library sample name from file-associated metadata
        String sampleName = files[0].getLibrarySample();
        String sequencerRun = files[0].getSequencerRun();
        String info = files[0].getLimsValue(Lims.TUBE_ID);

        /*
         * Modify the section below with your own parameters for your script
         */
        String parameters = getArgument("script_parameters");
        parameters = String.format("%s --unique-string %s --file %s -file %s --sample %s",
                parameters, run.getUniqueString(), files[0], files[1], sampleName);
        run.setScriptParameters(parameters);

        /*
         * Define the final files of the workflow. If these are unknown at
         * decider time, then remove these lines. Otherwise, pass the final
         * paths of the files at the end of the workflow along with their
         * metatype.
         */

        run.addOutputFile(files[0].basename() + ".hello", "txt/plain");
        run.addOutputFile(files[1].basename() + ".hello2", "txt/plain");



        return ret;
    }
}
