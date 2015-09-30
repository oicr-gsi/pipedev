package ca.on.oicr.pde.tools.gatk3;

import ca.on.oicr.pde.tools.common.AbstractCommand;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author mlaszloffy
 */
public class CatVariants extends AbstractCommand {

    private String outputFile;
    private String outputIndex;

    public String getOutputFile() {
        return outputFile;
    }

    public String getOutputIndex() {
        return outputIndex;
    }

    public static class Builder extends AbstractGatkBuilder<Builder> {

        private final List<String> inputFiles = new LinkedList<>();
        private boolean doSorting = false;

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

        public Builder disableSorting() {
            this.doSorting = false;
            return this;
        }

        public Builder enableSorting() {
            this.doSorting = true;
            return this;
        }

        public CatVariants build() {

            String outputFileType = null;
            for (String inputFile : inputFiles) {
                String currentFileType;
                if (inputFile.endsWith(".g.vcf.gz")) {
                    currentFileType = ".g.vcf.gz";
                } else if (inputFile.endsWith(".vcf.gz")) {
                    currentFileType = ".vcf.gz";
                } else if (inputFile.endsWith(".g.vcf")) {
                    currentFileType = ".g.vcf";
                } else if (inputFile.endsWith(".vcf")) {
                    currentFileType = ".vcf";
                } else {
                    throw new RuntimeException("Unsupported file type = [" + inputFile + "]");
                }

                if (outputFileType == null) {
                    outputFileType = currentFileType;
                } else if (!currentFileType.equals(outputFileType)) {
                    throw new RuntimeException("Expected all input files to be of the same type:\n" + Arrays.toString(inputFiles.toArray()));
                } else {
                    //outputFileType == currentFileType
                }
            }

            String outputFilePath;
            if (outputFileName == null) {
                outputFileName = "gatk." + RandomStringUtils.randomAlphanumeric(4);
            }
            outputFilePath = outputDir + outputFileName + outputFileType;

            //CatVariants does not extend CommandLineGatk
            List<String> c = new LinkedList<>();
            c.add(javaPath);
            c.add("-Xmx" + maxHeapSize);
            c.add("-Djava.io.tmpdir=" + tmpDir);
            c.add("-cp");
            c.add(gatkJarPath);
            c.add("org.broadinstitute.gatk.tools.CatVariants");

            c.add("--reference");
            c.add(referenceSequence);

            if (!doSorting) {
                c.add("--assumeSorted");
            }

            for (String inputFile : inputFiles) {
                c.add("--variant");
                c.add(inputFile);
            }

            c.add("--outputFile");
            c.add(outputFilePath);

            CatVariants cmd = new CatVariants();
            cmd.command.addAll(c);
            cmd.outputFile = outputFilePath;
            cmd.outputIndex = outputFilePath + ".tbi";
            return cmd;
        }
    }
}
