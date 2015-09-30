package ca.on.oicr.pde.tools.gatk3;

import ca.on.oicr.pde.tools.common.AbstractCommand;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author mlaszloffy
 */
public class UnifiedGenotyper extends AbstractCommand {

    private String outputFile;

    private UnifiedGenotyper() {
    }

    public String getOutputFile() {
        return outputFile;
    }

    public static class Builder extends AbstractGatkBuilder<Builder> {

        private final List<String> inputBamFiles = new LinkedList<>();
        private String dbsnpFilePath;
        private String standardCallConfidence;
        private String standardEmitConfidence;
        private String genotypeLikelihoodsModel;
        private String group;
        private Integer downsamplingCoverageThreshold;
        private String downsamplingType;

        public Builder(String javaPath, String maxHeapSize, String tmpDir, String gatkJarPath, String gatkKey, String outputDir) {
            super(javaPath, maxHeapSize, tmpDir, gatkJarPath, gatkKey, outputDir);
        }

        public Builder setInputBamFile(String inputBamFile) {
            this.inputBamFiles.clear();
            this.inputBamFiles.add(inputBamFile);
            return this;
        }

        public Builder setInputBamFiles(Collection<String> inputBamFiles) {
            this.inputBamFiles.clear();
            this.inputBamFiles.addAll(inputBamFiles);
            return this;
        }

        public Builder setDbsnpFilePath(String dbsnpFilePath) {
            this.dbsnpFilePath = dbsnpFilePath;
            return this;
        }

        public Builder setStandardCallConfidence(String standardCallConfidence) {
            this.standardCallConfidence = standardCallConfidence;
            return this;
        }

        public Builder setStandardEmitConfidence(String standardEmitConfidence) {
            this.standardEmitConfidence = standardEmitConfidence;
            return this;
        }

        public Builder setGenotypeLikelihoodsModel(String genotypeLikelihoodsModel) {
            this.genotypeLikelihoodsModel = genotypeLikelihoodsModel;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder setDownsamplingCoverageThreshold(Integer downsamplingCoverageThreshold) {
            this.downsamplingCoverageThreshold = downsamplingCoverageThreshold;
            return this;
        }

        public Builder setDownsamplingType(String downsamplingType) {
            this.downsamplingType = downsamplingType;
            return this;
        }

        public UnifiedGenotyper build() {

            String outputFilePath;
            if (outputFileName == null) {
                outputFileName = "gatk." + RandomStringUtils.randomAlphanumeric(4);
            }
            outputFilePath = outputDir + outputFileName + ".unified_genotyper." + StringUtils.lowerCase(genotypeLikelihoodsModel) + ".raw.vcf";

            List<String> c = build("UnifiedGenotyper");

            for (String inputBamFile : inputBamFiles) {
                c.add("--input_file");
                c.add(inputBamFile);
            }

            c.add("--dbsnp");
            c.add(dbsnpFilePath);

            c.add("--computeSLOD");

            if (standardCallConfidence != null) {
                c.add("--standard_min_confidence_threshold_for_calling");
                c.add(standardCallConfidence);
            }

            if (standardEmitConfidence != null) {
                c.add("--standard_min_confidence_threshold_for_emitting");
                c.add(standardEmitConfidence);
            }

            if (genotypeLikelihoodsModel != null) {
                c.add("--genotype_likelihoods_model");
                c.add(genotypeLikelihoodsModel);
            }

            if (group != null) {
                c.add("--group");
                c.add(group);
            }

            if (downsamplingCoverageThreshold != null) {
                c.add("--downsample_to_coverage");
                c.add(downsamplingCoverageThreshold.toString());
            }

            if (downsamplingType != null) {
                c.add("--downsampling_type");
                c.add(downsamplingType);
            }

            c.add("--out");
            c.add(outputFilePath);

            UnifiedGenotyper cmd = new UnifiedGenotyper();
            cmd.command.addAll(c);
            cmd.outputFile = outputFilePath;

            return cmd;
        }
    }

}
