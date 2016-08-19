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
public class CombineGVCFs extends AbstractCommand {

    private String outputFile;
    private String outputIndex;

    private CombineGVCFs() {
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getOutputIndex() {
        return outputIndex;
    }

    public static class Builder extends AbstractGatkBuilder<Builder> {

        private final List<String> inputFiles = new LinkedList<>();

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

        public CombineGVCFs build() {

            String outputFilePath;
            if (outputFileName == null) {
                outputFileName = "gatk." + RandomStringUtils.randomAlphanumeric(4);
            }
            outputFilePath = outputDir + outputFileName + ".g.vcf.gz";

            List<String> c = build("CombineGVCFs");

            for (String inputFile : inputFiles) {
                c.add("--variant");
                c.add(inputFile);
            }

            c.add("--out");
            c.add(outputFilePath);

            CombineGVCFs cmd = new CombineGVCFs();
            cmd.command.addAll(c);
            cmd.outputFile = outputFilePath;
            cmd.outputIndex = outputFilePath + ".tbi";
            return cmd;
        }

    }

}
