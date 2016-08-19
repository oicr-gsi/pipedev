package ca.on.oicr.pde.tools.cutadapt;

import ca.on.oicr.pde.tools.utilities.Configuration;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author mlaszloffy
 */
public class AbstractCutadaptBuilder<T> {

    protected final String cutadapt;
    protected final String outputDir;
    protected final List<String> forwardAdapters = new LinkedList<>();
    protected final List<String> reverseAdapters = new LinkedList<>();
    protected Integer minimumQualityScore;

    public AbstractCutadaptBuilder(String toolPath, String outputDir) throws IOException {
        cutadapt = Configuration.getToolPath(toolPath, "cutadapt", "bin/cutadapt").toString();
        this.outputDir = outputDir;
    }

    public T addForwardAdapter(String adapter) {
        forwardAdapters.add(adapter);
        return (T) this;
    }

    public T addReverseAdapter(String adapter) {
        reverseAdapters.add(adapter);
        return (T) this;
    }

    public T setMinimumQualityScore(int score) {
        minimumQualityScore = score;
        return (T) this;
    }

    protected List<String> getCommand() {

        List<String> c = new LinkedList<>();
        c.add(cutadapt);

        //https://cutadapt.readthedocs.org/en/stable/guide.html#quality-trimming
        if (minimumQualityScore != null) {
            c.add("-q");
            c.add(minimumQualityScore.toString());
        }

        for (String adapter : forwardAdapters) {
            c.add("-a");
            c.add(adapter);
        }

        for (String adapter : reverseAdapters) {
            c.add("-A");
            c.add(adapter);
        }

        return c;
    }

}
