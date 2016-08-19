package ca.on.oicr.pde.tools.cutadapt;

import ca.on.oicr.pde.tools.common.AbstractCommand;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author mlaszloffy
 */
public class Cutadapt extends AbstractCommand {

    private String outputFile;

    private Cutadapt() {
    }

    public String getOutoutFile() {
        return outputFile;
    }

    public static class Builder extends AbstractCutadaptBuilder<Builder> {

        private String inputFile;
        private Integer nFirstBasesToRemove;
        private Integer nLastBasesToRemove;

        public Builder(String binDir, String outputDir) throws IOException {
            super(binDir, outputDir);
        }

        public Builder setInputFile(String inputFile) {
            this.inputFile = inputFile;
            return this;
        }

        public Builder removeStartBases(Integer count) {
            nFirstBasesToRemove = count;
            return this;
        }

        public Builder removeEndBases(Integer count) {
            nLastBasesToRemove = count;
            return this;
        }

        public Cutadapt build() {

            List<String> c = super.getCommand();

            //https://cutadapt.readthedocs.org/en/stable/guide.html#removing-a-fixed-number-of-bases
            if (nFirstBasesToRemove != null && nFirstBasesToRemove != 0) {
                c.add("--cut");
                c.add(nFirstBasesToRemove.toString());
            }
            if (nLastBasesToRemove != null && nLastBasesToRemove != 0) {
                c.add("--cut");
                c.add(Integer.toString(-1 * nLastBasesToRemove));
            }

            String outputFile = outputDir + "trimmed." + FilenameUtils.getName(inputFile);

            //output file
            c.add("-o");
            c.add(outputFile);

            //input file
            c.add(inputFile);

            Cutadapt cmd = new Cutadapt();
            cmd.command.addAll(c);
            cmd.outputFile = outputFile;
            return cmd;
        }
    }
}
