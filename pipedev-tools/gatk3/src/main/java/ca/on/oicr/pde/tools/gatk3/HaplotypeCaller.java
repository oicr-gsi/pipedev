package ca.on.oicr.pde.tools.gatk3;

import ca.on.oicr.pde.tools.common.AbstractCommand;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author mlaszloffy
 */
public class HaplotypeCaller extends AbstractCommand {

    private String outputFile;
    private String outputIndex;

    private HaplotypeCaller() {
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getOutputIndex() {
        return outputIndex;
    }

    public enum OperatingMode {

        VCF, GVCF;
    }

    public static class Builder extends AbstractGatkBuilder<Builder> {

        private final List<String> inputBamFiles = new LinkedList<>();
        private String dbsnpFilePath;
        private String standardCallConfidence;
        private String standardEmitConfidence;
        private Integer downsamplingCoverageThreshold;
        private String downsamplingType;
        private String genotypingMode;
        private String outputMode;
        private OperatingMode operatingMode = OperatingMode.GVCF;

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

        public Builder setDownsamplingCoverageThreshold(Integer downsamplingCoverageThreshold) {
            this.downsamplingCoverageThreshold = downsamplingCoverageThreshold;
            return this;
        }

        public Builder setDownsamplingType(String downsamplingType) {
            this.downsamplingType = downsamplingType;
            return this;
        }

        public Builder setGenotypingMode(String genotypingMode) {
            this.genotypingMode = genotypingMode;
            return this;
        }

        public Builder setOutputMode(String outputMode) {
            this.outputMode = outputMode;
            return this;
        }

        public Builder setOperatingMode(OperatingMode operatingMode) {
            this.operatingMode = operatingMode;
            return this;
        }

        public Multimap<String, String> validate() {
            Multimap<String, String> msgs = HashMultimap.create();
            switch (operatingMode) {
                case GVCF:
                    if (standardCallConfidence != null) {
                        msgs.put("WARNINGS", "standardCallConfidence is ignored in GVCF mode.");
                    }
                    if (standardEmitConfidence != null) {
                        msgs.put("WARNINGS", "standardEmitConfidence is ignored in GVCF mode.");
                    }
                    break;
                case VCF:
                    break;
            }

            if (inputBamFiles.isEmpty()) {
                msgs.put("ERRORS", "Expected one or more input bam files");
            }

            return msgs;
        }

        public HaplotypeCaller build() {

            Multimap<String, String> msgs = validate();
            if (!msgs.get("WARNINGS").isEmpty()) {
                System.out.println("Warnings when validating: " + msgs.get("WARNINGS").toString());
            }
            if (!msgs.get("ERRORS").isEmpty()) {
                throw new RuntimeException("Errors when validating: " + msgs.get("ERRORS").toString());
            }

            String outputFilePath;
            if (outputFileName == null) {
                outputFileName = "gatk." + RandomStringUtils.randomAlphanumeric(4);
            }
            switch (operatingMode) {
                case GVCF:
                    outputFilePath = outputDir + outputFileName + ".g.vcf.gz";
                    break;
                case VCF:
                    outputFilePath = outputDir + outputFileName + ".haplotype_caller.raw.vcf";
                    break;
                default:
                    throw new RuntimeException("Unexpected operating mode: " + operatingMode);
            }

            List<String> c = build("HaplotypeCaller");

            for (String inputBamFile : inputBamFiles) {
                c.add("--input_file");
                c.add(inputBamFile);
            }

            c.add("--dbsnp");
            c.add(dbsnpFilePath);

            if (standardCallConfidence != null) {
                c.add("--standard_min_confidence_threshold_for_calling");
                c.add(standardCallConfidence);
            }

            if (standardEmitConfidence != null) {
                c.add("--standard_min_confidence_threshold_for_emitting");
                c.add(standardEmitConfidence);
            }

            if (downsamplingCoverageThreshold != null) {
                c.add("--downsample_to_coverage");
                c.add(downsamplingCoverageThreshold.toString());
            }

            if (downsamplingType != null) {
                c.add("--downsampling_type");
                c.add(downsamplingType);
            }

            if (genotypingMode != null) {
                c.add("--genotyping_mode");
                c.add(StringUtils.upperCase(genotypingMode));
            }

            if (outputMode != null) {
                c.add("--output_mode");
                c.add(StringUtils.upperCase(outputMode));
            }

            if (OperatingMode.GVCF == operatingMode) {
                c.add("--emitRefConfidence");
                c.add("GVCF");

                c.add("--variant_index_type");
                c.add("LINEAR");

                c.add("--variant_index_parameter");
                c.add("128000");
            }

            c.add("--out");
            c.add(outputFilePath);

            HaplotypeCaller cmd = new HaplotypeCaller();
            cmd.command.addAll(c);
            cmd.outputFile = outputFilePath;
            cmd.outputIndex = outputFilePath + ".tbi";

            return cmd;
        }
    }

}
