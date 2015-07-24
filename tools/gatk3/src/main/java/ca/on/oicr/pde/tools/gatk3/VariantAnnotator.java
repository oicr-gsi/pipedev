package ca.on.oicr.pde.tools.gatk3;

import ca.on.oicr.pde.tools.common.AbstractCommand;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author mlaszloffy
 */
public class VariantAnnotator extends AbstractCommand {

    private String outputFile;

    private VariantAnnotator() {
    }

    public String getOutputFile() {
        return outputFile;
    }

    public static class Builder extends AbstractGatkBuilder<Builder> {

        private String inputVcfFile;
        private String additionalParams;

        public Builder(String javaPath, String maxHeapSize, String tmpDir, String gatkJarPath, String gatkKey, String outputDir) {
            super(javaPath, maxHeapSize, tmpDir, gatkJarPath, gatkKey, outputDir);
        }

        public Builder setInputVcfFile(String inputVcfFile) {
            this.inputVcfFile = inputVcfFile;
            return this;
        }

        public Builder setAdditionalParams(String additionalParams) {
            this.additionalParams = additionalParams;
            return this;
        }

        public VariantAnnotator build() {

            String outputFilePath;
            if (outputFileName != null) {
                outputFilePath = outputDir + outputFileName + ".vcf";
            } else {
                outputFilePath = outputDir + FilenameUtils.getBaseName(inputVcfFile) + ".annotated.vcf";
            }

            List<String> c = build("VariantAnnotator");

            c.add("--variant");
            c.add(inputVcfFile);

            c.add("--out");
            c.add(outputFilePath);

            if (additionalParams != null) {
                c.add(additionalParams);
            }

            VariantAnnotator cmd = new VariantAnnotator();
            cmd.command.addAll(c);
            cmd.outputFile = outputFilePath;
            return cmd;
        }

    }
}
