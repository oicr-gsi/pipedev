package ca.on.oicr.pde.tools.cutadapt;

import ca.on.oicr.pde.tools.common.AbstractCommand;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author mlaszloffy
 */
public class CutadaptPairedEnd extends AbstractCommand {

    private String outputFileRead1;
    private String outputFileRead2;

    public String getOutputFileRead1() {
        return outputFileRead1;
    }

    public String getOutputFileRead2() {
        return outputFileRead2;
    }

    public class Builder extends AbstractCutadaptBuilder<Builder> {

        private String inputFileRead1;
        private String inputFileRead2;

        public Builder(String binDir, String outputDir) throws IOException {
            super(binDir, outputDir);
        }

        public Builder setPairedEndInput(String read1, String read2) {
            this.inputFileRead1 = read1;
            this.inputFileRead2 = read2;
            return this;
        }

        public CutadaptPairedEnd build() {
            List<String> c = super.getCommand();

            String outputFileRead1 = outputDir + "trimmed." + FilenameUtils.getName(inputFileRead1);
            String outputFileRead2 = outputDir + "trimmed." + FilenameUtils.getName(inputFileRead2);

            //output files
            c.add("-o");
            c.add(outputFileRead1);
            c.add("-p");
            c.add(outputFileRead2);

            //input files
            c.add(inputFileRead1);
            c.add(inputFileRead2);

            CutadaptPairedEnd cmd = new CutadaptPairedEnd();
            cmd.outputFileRead1 = outputFileRead1;
            cmd.outputFileRead2 = outputFileRead2;
            cmd.command.addAll(c);

            return cmd;
        }
    }
}
