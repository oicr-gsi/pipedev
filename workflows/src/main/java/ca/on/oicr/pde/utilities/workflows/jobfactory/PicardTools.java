package ca.on.oicr.pde.utilities.workflows.jobfactory;

import net.sourceforge.seqware.pipeline.workflowV2.model.Job;
import net.sourceforge.seqware.pipeline.workflowV2.model.Workflow;

/**
 * This is a factory class that will create Picard jobs for your workflows.
 *
 * @author mtaschuk
 */
public class PicardTools {

    private Workflow workflow;

    public PicardTools(Workflow workflow) {
        this.workflow = workflow;
    }

    /**
     * Marks duplicates in a BAM or SAM file. Validation stringency is set to
     * SILENT.
     *
     * @param markDuplicatesJar the location of the MarkDuplicates.jar
     * @param memoryMb the amount of memory to give to the process in MB
     * @param input the path of the SAM/BAM file to analyze
     * @param output the full path of the output BAM file location
     * @param metricsFile the path where duplication metrics will be written
     * @param tmpDir the temporary directory where the processing will take
     * place
     * @param sortOrder the order to sort the resulting bam file
     * @return the Job with the command
     */
    public Job markDuplicates(String java, String markDuplicatesJar, int memoryMb, String tmpDir,
            String input, String output, String metricsFile, String otherParams) {
        String command = String.format("%s -Xmx%dM -jar %s INPUT=%s OUTPUT=%s "
                + "VALIDATION_STRINGENCY=SILENT TMP_DIR=%s METRICS_FILE=%s",
                java, memoryMb, markDuplicatesJar, input, output, tmpDir, metricsFile, otherParams);
        Job job = workflow.createBashJob("PicardMarkDuplicates");
        job.getCommand().addArgument(command);
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb * 2));
        return job;
    }

    /**
     * Merges SAM or BAM files together. Also sorts the final BAM in the given
     * order and creates an index. Validation stringency is set to SILENT.
     *
     * @param mergeBamFilesJar the location of the MergeSamFiles.jar
     * @param memoryMb the amount of memory to give to the process in MB
     * @param output the full path of the output BAM file location
     * @param sortOrder the order to sort the BAM file, e.g. coordinate
     * @param assumeSorted whether the input files are sorted or not
     * @param tmpDir the temporary directory where the processing will take
     * place
     * @param input the path(s) of the SAM/BAM files to process
     * @return the Job with the command
     */
    public Job mergeSamFiles(String java, String mergeBamFilesJar, int memoryMb, String tmpDir,
            String sortOrder, boolean assumeSorted, boolean useThreading, String otherParams,
            String output, String... input) {
        String command = String.format("java -Xmx%dM -jar %s "
                + "OUTPUT=%s "
                + "VALIDATION_STRINGENCY=SILENT "
                + "TMP_DIR=%s "
                + "SORT_ORDER=%s "
                + "CREATE_INDEX=true",
                memoryMb, mergeBamFilesJar, output, tmpDir, sortOrder);
        Job job = workflow.createBashJob("PicardMergeBam");
        job.getCommand().addArgument(command);
        for (String in : input) {
            job.getCommand().addArgument(String.format("INPUT=%s", in));
        }
        if (assumeSorted) {
            job.getCommand().addArgument(String.format("ASSUME_SORTED=true"));
        }
        if (useThreading) {
            job.getCommand().addArgument(String.format("USE_THREADING=true"));
        }
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb * 2));
        return job;
    }

    /**
     * Merges SAM or BAM files together. Also: sorts in coordinate order,
     * creates a BAM index, uses threading to speed up execution, and assumes
     * the input files are also sorted.
     *
     * @param mergeBamFilesJar the location of the MergeSamFiles.jar
     * @param memoryMb the amount of memory to give to the process in MB
     * @param tmpDir the temporary directory where the processing will take
     * place
     * @param output the full path of the output BAM file location
     * @param input the path(s) of the SAM/BAM files to process
     * @return
     */
    public Job mergeSamFiles(String java, String mergeBamFilesJar, int memoryMb,
            String tmpDir, String output, String... input) {
        return mergeSamFiles(java, mergeBamFilesJar, memoryMb, tmpDir, "coordinate",
                false, true, null, output, input);
    }

    public Job sortSamFile(String java, String sortSamFileJar, int memoryMb,
            String tmpDir, String sortOrder, String output, String input, String otherParams) {
        String command = String.format("java -Xmx%dM -jar %s "
                + "OUTPUT=%s "
                + "INPUT=%s "
                + "VALIDATION_STRINGENCY=SILENT "
                + "TMP_DIR=%s "
                + "SORT_ORDER=%s "
                + "CREATE_INDEX=true",
                memoryMb, sortSamFileJar, output, input, tmpDir, sortOrder);
        Job job = workflow.createBashJob("PicardSortBam");
        job.getCommand().addArgument(command);
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb * 2));
        return job;
    }

    public Job fixMate(String java, String fixMateJar, int memoryMb,
            String tmpDir, String sortOrder, String output, String input, String otherParams) {
        String command = String.format("java -Xmx%1$dM -jar %2$s "
                + "OUTPUT=%3$s "
                + "INPUT=%4$s "
                + "VALIDATION_STRINGENCY=SILENT "
                + "TMP_DIR=%5$s "
                + "SORT_ORDER=%6$s "
                + "CREATE_INDEX=true",
                memoryMb, fixMateJar, output, input, tmpDir, sortOrder);
        Job job = workflow.createBashJob("PicardFixMate");
        job.getCommand().addArgument(command);
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb * 2));
        return job;
    }
}
