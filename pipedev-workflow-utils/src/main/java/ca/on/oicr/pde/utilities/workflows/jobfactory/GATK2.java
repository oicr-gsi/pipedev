package ca.on.oicr.pde.utilities.workflows.jobfactory;

import ca.on.oicr.pde.utilities.workflows.jobfactory.GATK2.Resource;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.seqware.pipeline.workflowV2.model.Job;
import net.sourceforge.seqware.pipeline.workflowV2.model.Workflow;

/**
 * This is a factory class that will create GATK jobs for your workflows.
 *
 * @author mtaschuk
 */
public class GATK2 {

    private Workflow workflow;

    public GATK2(Workflow workflow) {
        this.workflow = workflow;
    }

    /**
     * Creates the interval file to use for realignment in the indelRealigner step using GATK's RealignerTargetCreator.
     *
     * @param java the path to java
     * @param gatk the location of the GenomeAnalysisTK.jar
     * @param memoryMb the amount of memory to give the job in MB
     * @param tmpDir the path of the temp dir where the processing will occur
     * @param refFasta the human genome reference
     * @param outputFile the path of the output file
     * @param inputFile the path of the input BAM file
     * @param goldIndelsVcf the path of the dbSNP VCF file
     * @param chromosome the chromosome to realign
     * @param otherParams any other params to apply to RealignerTargetCreator
     * @return the Job with the command and max memory set
     */
    public Job realignerTargetCreator(String java, String gatk, int memoryMb, String tmpDir,
            String refFasta, String chromosome, List<String> goldIndelsVcf,
            String inputFile, String outputFile, String otherParams) {
        String command = String.format("%s -Xmx%dM -Djava.io.tmpdir=%s -jar %s "
                + "-T RealignerTargetCreator "
                + "-R %s "
                + "-I %s "
                + "-o %s "
                + "-l INFO",
                java, memoryMb - 2000, tmpDir, gatk, refFasta, inputFile, outputFile);
        Job job = workflow.createBashJob("RealignerTargetCreator");
        job.getCommand().addArgument(command);
        for (String vcf : goldIndelsVcf) {
            job.getCommand().addArgument(String.format("-known %s ", vcf));
        }
        if (chromosome != null) {
            job.getCommand().addArgument(String.format("-L %s", chromosome));
        }
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb));
        return job;
    }

    /**
     * Realigns a BAM file around the given intervals generated in the realignerTargetCreator method using GATK's IndelRealigner.
     *
     * @param chromosome the chromosome to realign
     * @param java the path to java
     * @param memoryMb the amount of memory to give the job in MB
     * @param gatk the location of the GenomeAnalysisTK.jar
     * @param refFasta the human genome reference
     * @param intervalFile the interval file with the locations to be realigned (generated with realignerTargetCreator)
     * @param inputFile the input BAM file
     * @param goldIndelsVcf the path of the dbSNP VCF file
     * @param outputFile the output BAM file
     * @param tmpDir the path of the temp dir where the processing will occur
     * @param otherParams any other params to apply to IndelRealigner
     * @return the Job with the command and the max memory set
     */
    public Job indelRealigner(String java, String gatk, int memoryMb, String tmpDir,
            String refFasta, String chromosome, String intervalFile, String goldIndelsVcf,
            String inputFile, String outputFile, String otherParams) {
        Job job = workflow.createBashJob("GATKIndelRealigner");
        String command = String.format("%s -Xmx%dM -Djava.io.tmpdir=%s -jar %s "
                + "-T IndelRealigner "
                + "-R %s "
                + "-targetIntervals %s "
                + "-known %s "
                + "-I %s "
                + "-o %s "
                + "-l INFO "
                + "-compress 0",
                java, memoryMb - 2000, tmpDir, gatk, refFasta, intervalFile, goldIndelsVcf, inputFile, outputFile);
        job.getCommand().addArgument(command);
        if (chromosome != null) {
            job.getCommand().addArgument(String.format("-L %s", chromosome));
        }
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb));
        return job;
    }

    /**
     * Calculates the base quality recalibration scores using GATK's BaseRecalibrator. This file can subsequently be applied with
     * applyBaseRecalibration (PrintReads).
     *
     * @param java the path to java
     * @param gatk the location of the GenomeAnalysisTK.jar
     * @param memoryMb the amount of memory to give the job in MB
     * @param tmpDir the path of the temp dir where the processing will occur
     * @param refFasta the human genome reference
     * @param knownSitesVcfs Gold standard VCF files (e.g. dbSNP, 1000G, Mills Devine indels)
     * @param inputFiles the input BAM files
     * @param recalFile the output recalibration file
     * @param otherParams any other params to apply to BaseRecalibrator
     * @return the Job with the command and the max memory set
     */
    public Job baseRecalibrator(String java, String gatk, int memoryMb, String tmpDir,
            String refFasta, List<String> knownSitesVcfs,
            List<String> inputFiles, String recalFile, String otherParams) {
        Job job = workflow.createBashJob("BaseRecalibrator");

        String command = String.format("%s -Xmx%dM -Djava.io.tmpdir=%s -jar %s "
                + "-T BaseRecalibrator "
                + "-R %s "
                + "-o %s "
                + "-l INFO",
                java, memoryMb - 2000, tmpDir, gatk, refFasta, recalFile);
        job.getCommand().addArgument(command);
        for (String knownSiteVcf : knownSitesVcfs) {
            job.getCommand().addArgument(String.format("-knownSites %s", knownSiteVcf));
        }
        for (String input : inputFiles) {
            job.getCommand().addArgument(String.format("-I %s", input));
        }
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb));
        return job;
    }

    /**
     * Applies the recalibration file calculated by BaseRecalibrator to the given chromosome using GATK PrintReads.
     *
     * @param java the path to java
     * @param memoryMb the amount of memory to give the job in MB
     * @param gatk the location of the GenomeAnalysisTK.jar
     * @param tmpDir the path of the temp dir where the processing will occur
     * @param refFasta the human genome reference
     * @param chromosome the chromosome to recalibrate
     * @param recalFile the recalibration file from BaseRecalibrator
     * @param inputFile the input BAM file
     * @param outputFile the output BAM file
     * @param otherParams any other params to apply to Printreads
     * @return the Job with the command and max memory set
     */
    public Job applyBaseRecalibration(String java, String gatk, int memoryMb, String tmpDir,
            String refFasta, String chromosome, String recalFile, String inputFile,
            String outputFile, String otherParams) {
        Job job = workflow.createBashJob("BaseRecalibration");

        String command = String.format("%s -Xmx%dM -Djava.io.tmpdir=%s -jar %s "
                + "-T PrintReads "
                + "-R %s "
                + "-I %s "
                + "-BQSR %s "
                + "-o %s "
                + "-l INFO",
                java, (memoryMb - 2000), tmpDir, gatk, refFasta, inputFile, recalFile, outputFile);
        job.getCommand().addArgument(command);
        if (chromosome != null) {
            job.getCommand().addArgument(String.format("-L %s", chromosome));
        }
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb));
        return job;
    }

    public Job reduceReads(String java, String gatk, int memoryMb, String tmpDir,
            String refFasta, String chromosome,
            String inputFile, String outputFile, String otherParams) {
        Job job = workflow.createBashJob("ReduceReads");

        String command = String.format("%s -Xmx%dM -Djava.io.tmpdir=%s -jar %s "
                + "-T ReduceReads "
                + "-R %s "
                + "-I %s "
                + "-o %s",
                java, memoryMb - 2000, tmpDir, gatk, refFasta, inputFile, outputFile);
        job.getCommand().addArgument(command);
        if (chromosome != null) {
            job.getCommand().addArgument(String.format("-L %s", chromosome));
        }
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb));
        return job;
    }

    public Job haplotypeCaller(String java, String gatk, int memoryMb, String tmpDir,
            String refFasta, String chromosome, String genotypingMode, String outputMode,
            int standEmitConf, int standCallConf, String gatkKey, String dbSnp,
            String inputFile, String outputFile, String otherParams) {
        Job job = workflow.createBashJob("HaplotypeCaller");

        String command = String.format("%s -Xmx%dM -Djava.io.tmpdir=%s -jar %s "
                + "-T HaplotypeCaller "
                + "-R %s "
                + "-I %s "
                + "--genotyping_mode %s "
                + "-stand_emit_conf %d "
                + "-stand_call_conf %d "
                + "-o %s "
                + "--dbsnp %s",
                java, memoryMb - 2000, tmpDir, gatk, refFasta, inputFile,
                genotypingMode, standEmitConf, standCallConf, outputFile, dbSnp);

        job.getCommand().addArgument(command);
	if (outputMode !=null) {
            job.getCommand().addArgument(String.format("--output_mode %s", outputMode));
        }
        if (chromosome != null) {
            job.getCommand().addArgument(String.format("-L %s", chromosome));
        }
        if (gatkKey != null) {
            job.getCommand().addArgument(String.format("-et NO_ET -K %s", gatkKey));
        }
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb));
        return job;
    }

    /**
     * DBsnp is provided to annotate variants during calling: http://www.broadinstitute.org/gatk/guide/article?id=1247
     *
     * @param java the path to java
     * @param gatk the location of the GenomeAnalysisTK.jar
     * @param memoryMb the amount of memory to give the job in MB
     * @param tmpDir the path of the temp dir where the processing will occur
     * @param refFasta the human genome reference
     * @param chromosome the chromosome to realign
     * @param ploidy
     * @param genotypeLikelihoodModel
     * @param standEmitConf
     * @param standCallConf
     * @param gatkKey
     * @param dbSnp
     * @param inputFile the path of the input BAM file
     * @param outputFile the path of the output file
     * @param otherParams any other parameters to send to UnifiedGenotyper
     * @return the Job with the command and the max memory set
     */
    public Job unifiedGenotyper(String java, String gatk, int memoryMb, String tmpDir,
            String refFasta, String chromosome, int ploidy, String genotypeLikelihoodModel,
            int standEmitConf, int standCallConf, String gatkKey, String dbSnp,
            String inputFile, String outputFile, String otherParams) {
        Job job = workflow.createBashJob("UnifiedGenotyper");
        String command = String.format("%s -Xmx%dM -Djava.io.tmpdir=%s -jar %s "
                + "-T UnifiedGenotyper "
                + "-R %s "
                + "-I %s "
                + "-ploidy %s "
                + "-glm %s "
                + "-stand_emit_conf %d "
                + "-stand_call_conf %d "
                + "--computeSLOD "
                + "-o %s "
                + "--dbsnp %s ",
                java, memoryMb - 2000, tmpDir, gatk, refFasta, inputFile, ploidy,
                genotypeLikelihoodModel, standEmitConf, standCallConf, outputFile, dbSnp);

        job.getCommand().addArgument(command);
        if (chromosome != null) {
            job.getCommand().addArgument(String.format("-L %s", chromosome));
        }
        if (gatkKey != null) {
            job.getCommand().addArgument(String.format("-et NO_ET -K %s", gatkKey));
        }
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb));
        return job;
    }

    public Job variantRecalibrator(String java, String gatk, int memoryMb, String tmpDir,
            String refFasta, String chromosome, String mode, double tranche, String gatkKey,
            double percentBad, int minNumBad, List<Resource> resources, List<String> annotations,
            String inputFile, String outputFile, String tranchesFile, String otherParams) {
        Job job = workflow.createBashJob("VariantRecalibrator");

        String command = String.format("%s -Xmx%dM -Djava.io.tmpdir=%s -jar %s "
                + "-T VariantRecalibrator "
                + "-R %s "
                + "-mode %s "
                + "-tranche %.1f "
                + "-percentBad %.2f "
                + "-minNumBad %d "
                + "-input %s "
                + "-recalFile %s "
                + "-tranchesFile %s ",
                java, memoryMb - 2000, tmpDir, gatk, refFasta, mode, tranche,
                percentBad, minNumBad, inputFile, outputFile, tranchesFile);
        job.getCommand().addArgument(command);
        for (Resource resource : resources) {
            job.getCommand().addArgument(String.format("-resource:%s,known=%s,training=%s,truth=%s,prior=%.1f %s",
                    resource.name, resource.known, resource.training, resource.truth, resource.prior, resource.path));
        }
        for (String annotation : annotations) {
            job.getCommand().addArgument(String.format("-an %s", annotation));
        }
        if (chromosome != null) {
            job.getCommand().addArgument(String.format("-L %s", chromosome));
        }
        if (gatkKey != null) {
            job.getCommand().addArgument(String.format("-et NO_ET -K %s", gatkKey));
        }
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb));
        return job;
    }

    /**
     * Recommendations from
     * http://www.broadinstitute.org/gatk/guide/article?id=1259
     */
    public Job snpVariantRecalibrator(String java, String gatk, int memoryMb, String tmpDir,
            String refFasta, String chromosome, double tranche, String gatkKey,
            String hapMapPath, String omniPath, String thousandGPath, String dbSnpPath,
            String inputFile, String outputFile, String tranchesFile, String otherParams) {
        String mode = "SNP";
        List<String> annotations = Arrays.asList("DP", "QD", "FS", "MQRankSum", "ReadPosRankSum");
        List<Resource> resources = Arrays.asList(
                new Resource("hapmap", false, true, true, 15.0, hapMapPath),
                new Resource("omni", false, true, false, 12.0, omniPath),
                new Resource("1000G", false, true, false, 10.0, thousandGPath),
                new Resource("dbsnp", true, false, false, 2.0, dbSnpPath));

        return variantRecalibrator(java, gatk, memoryMb, tmpDir, refFasta, chromosome,
                mode, tranche, gatkKey, 0.01, 1000, resources,
                annotations, inputFile, outputFile, tranchesFile, otherParams);
    }

    /**
     * Recommendations from
     * http://www.broadinstitute.org/gatk/guide/article?id=1259
     */
    public Job indelVariantRecalibrator(String java, String gatk, int memoryMb, String tmpDir,
            String refFasta, String chromosome, double tranche, String gatkKey,
            String millsDevinePath, String thousandGIndelPath, String dbSnpPath,
            String inputFile, String outputFile, String tranchesFile, String otherParams) {
        String mode = "INDEL";
        List<String> annotations = Arrays.asList("DP", "FS", "MQRankSum", "ReadPosRankSum");
        List<Resource> resources = Arrays.asList(new Resource("mills", true, true, true, 12.0, millsDevinePath),
                new Resource("1000G", false, true, false, 10.0, thousandGIndelPath),
                new Resource("dbsnp", true, false, false, 2.0, dbSnpPath));
        String params = String.format("--maxGaussians %d %s", 4, (otherParams == null ? "" : otherParams));

        return variantRecalibrator(java, gatk, memoryMb, tmpDir, refFasta, chromosome,
                mode, tranche, gatkKey, 0.01, 1000, resources,
                annotations, inputFile, outputFile, tranchesFile, params);
    }

    public Job applyVariantRecalibration(String java, String gatk, int memoryMb, String tmpDir,
            String refFasta, String chromosome, String mode, double tranche,
            String recalFile, String inputFile, String outputFile, String tranchesFile, String otherParams) {
        Job job = workflow.createBashJob("ApplyRecalibration");
        String command = String.format("%s -Xmx%dM -Djava.io.tmpdir=%s -jar %s "
                + "-T ApplyRecalibration "
                + "-R %s "
                + "-mode %s "
                + "--ts_filter_level %.1f "
                + "-recalFile %s "
                + "-tranchesFile %s "
                + "-input %s "
                + "-o %s",
                java, memoryMb - 2000, tmpDir, gatk, refFasta, mode, tranche, recalFile, tranchesFile, inputFile, outputFile);
        job.getCommand().addArgument(command);
        if (chromosome != null) {
            job.getCommand().addArgument(String.format("-L %s", chromosome));
        }
        if (otherParams != null) {
            job.getCommand().addArgument(otherParams);
        }
        job.setMaxMemory(String.valueOf(memoryMb));
        return job;
    }

    public class Resource {

        private String name;
        private boolean known;
        private boolean training;
        private boolean truth;
        private double prior;
        private String path;

        public Resource(String name, boolean known, boolean training, boolean truth,
                double prior, String path) {
            this.name = name;
            this.known = known;
            this.training = training;
            this.truth = truth;
            this.prior = prior;
            this.path = path;
        }
    }
}
