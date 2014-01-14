package ca.on.oicr.pde.utilities.workflows.jobfactory;

import java.util.Random;
import net.sourceforge.seqware.pipeline.workflowV2.model.Job;
import net.sourceforge.seqware.pipeline.workflowV2.model.Workflow;

/**
 *
 * @author mtaschuk
 */
public class VCF4 {

    private Workflow workflow;

    public VCF4(Workflow workflow) {
        this.workflow = workflow;
    }

    /**
     * Creates a Bash job that concatenates the input VCF files together and sorts 
     * them in UCSC chromosome order. (chr1, chr2, ... chrX, chrY, chrM).
     * Requires shell commands cat, tr, head, grep, sort, sed.
     * 
     * @param outputFile the final merged sorted file name
     * @param inputFiles the input VCF files to be merged and sorted
     * @return the Job that contains the command line to sort.
     */
    public Job mergeSort(String outputFile, String... inputFiles) {
        Job job = workflow.createBashJob("MergeSortVcf");
        StringBuilder sb = new StringBuilder();
        String mergedFile = String.format("merged%d.vcf", new Random(outputFile.hashCode()).nextInt());

	//in case the job failed the first time, empty out the files for a fresh try
	sb.append(String.format("cp /dev/null %s; ", mergedFile));
        //we pass the files through tr to remove any binary characters that were
        //accidentally introduced by GATK
        for (String input : inputFiles) {
            //sb.append(String.format("cat %s | tr -cd '\\11\\12\\15\\40-\\176' >> %s;", input, mergedFile));
        sb.append(String.format("cat %s >> %s;", input, mergedFile));
	}
        sb.append(String.format("head -10000 %1$s | grep ^# > %2$s ; "
                + "grep -v ^# %1$s | sed 's/chrX/chr23/' | sed 's/chrY/chr24/' "
                + "| sed 's/chrM/chr25/' | sort -k1.4,1.5n -k2,2n "
                + "| sed 's/chr23/chrX/' | sed 's/chr24/chrY/' "
                + "| sed 's/chr25/chrM/' >> %2$s;",
                mergedFile, outputFile));
        
        job.getCommand().addArgument(sb.toString());
        return job;
    }
}
