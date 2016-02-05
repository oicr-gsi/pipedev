package ca.on.oicr.pde.tools.gatk3;

import ca.on.oicr.pde.tools.common.AbstractCommand;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author mlaszloffy
 */
public class PrintReads extends AbstractCommand {

    private String outputFile;

    private PrintReads() {
    }

    public String getOutputFile() {
        return outputFile;
    }

    public static class Builder extends AbstractGatkBuilder<Builder> {

        private String covariatesTablesFile;
        private Integer preserveQscoresLessThan;
        List<String> inputFiles = new LinkedList<>();

        public Builder(String javaPath, String maxHeapSize, String tmpDir, String gatkJarPath, String gatkKey, String outputDir) {
            super(javaPath, maxHeapSize, tmpDir, gatkJarPath, gatkKey, outputDir);
        }

        public Builder setCovariatesTablesFile(String covariatesTablesFile) {
            this.covariatesTablesFile = covariatesTablesFile;
            return this;
        }

        public Builder setPreserveQscoresLessThan(Integer preserveQscoresLessThan) {
            this.preserveQscoresLessThan = preserveQscoresLessThan;
            return this;
        }

        @Deprecated
        public Builder setInputFile(String inputFile) {
            this.inputFiles.clear();
            this.inputFiles.add(inputFile);
            return this;
        }
        
        public Builder addInputFile(String inputFile) {
            inputFiles.add(inputFile);
            return this;
        }

        public Builder addInputFiles(Collection<String> inputFiles) {
            this.inputFiles.addAll(inputFiles);
            return this;
        }

        public PrintReads build() {
            //GP-604: When merging, use generic name
            String outputFilePath = "merged.recal.bam";
            if (inputFiles.size() == 1 ) {
                outputFilePath = FilenameUtils.getBaseName(inputFiles.get(0)) + ".recal.bam";
            }
            
            List<String> c = build("PrintReads");

            c.add("--BQSR");
            c.add(covariatesTablesFile);

            //GP-604: Support multiple inputs
            for (String inFile : inputFiles) {
             c.add("-I");
             c.add(inFile);
            }

            if (preserveQscoresLessThan != null) {
                c.add("--preserve_qscores_less_than");
                c.add(preserveQscoresLessThan.toString());
            }

            c.add("--out");
            c.add(outputFilePath);

            PrintReads cmd = new PrintReads();
            cmd.command.addAll(c);
            cmd.outputFile = outputFilePath;
            return cmd;
        }
        
        

    }
}
