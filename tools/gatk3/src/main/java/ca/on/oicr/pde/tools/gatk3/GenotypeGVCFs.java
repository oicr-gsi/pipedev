package ca.on.oicr.pde.tools.gatk3;

import ca.on.oicr.pde.tools.common.AbstractCommand;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author mlaszloffy
 */
public class GenotypeGVCFs extends AbstractCommand {

    private String outputFile;
    private String outputIndex;

    private GenotypeGVCFs() {
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getOutputIndex() {
        return outputIndex;
    }

    public static class Builder extends AbstractGatkBuilder<Builder> {

        private final List<String> inputFiles = new LinkedList<>();
        private Double standardCallConfidence;
        private Double standardEmitConfidence;
        private String dbsnpFilePath;

        public Builder(String javaPath, String maxHeapSize, String tmpDir, String gatkJarPath, String gatkKey, String outputDir) {
            super(javaPath, maxHeapSize, tmpDir, gatkJarPath, gatkKey, outputDir);
        }

        public Builder addInputFile(String inputFile) {
            this.inputFiles.add(inputFile);
            return this;
        }

        public Builder setInputFiles(Collection<String> inputFiles) {
            this.inputFiles.clear();
            this.inputFiles.addAll(inputFiles);
            return this;
        }

        public Builder setStandardCallConfidence(Double standardCallConfidence) {
            this.standardCallConfidence = standardCallConfidence;
            return this;
        }

        public Builder setStandardEmitConfidence(Double standardEmitConfidence) {
            this.standardEmitConfidence = standardEmitConfidence;
            return this;
        }

        public Builder setDbsnpFilePath(String dbsnpFilePath) {
            this.dbsnpFilePath = dbsnpFilePath;
            return this;
        }

        public GenotypeGVCFs build() {

            String outputFilePath;
            if (outputFileName == null) {
                outputFileName = "gatk." + RandomStringUtils.randomAlphanumeric(4);
            }
            outputFilePath = outputDir + outputFileName + ".vcf.gz";

            List<String> c = build("GenotypeGVCFs");

            if (standardCallConfidence != null) {
                c.add("--standard_min_confidence_threshold_for_calling");
                c.add(standardCallConfidence.toString());
            }

            if (standardEmitConfidence != null) {
                c.add("--standard_min_confidence_threshold_for_emitting");
                c.add(standardEmitConfidence.toString());
            }

            if (dbsnpFilePath != null) {
                c.add("--dbsnp");
                c.add(dbsnpFilePath);
            }

            for (String inputFile : inputFiles) {
                c.add("--variant");
                c.add(inputFile);
            }

            c.add("--out");
            c.add(outputFilePath);

            GenotypeGVCFs cmd = new GenotypeGVCFs();
            cmd.command.addAll(c);
            cmd.outputFile = outputFilePath;
            cmd.outputIndex = outputFilePath + ".tbi";
            return cmd;
        }

    }

}
