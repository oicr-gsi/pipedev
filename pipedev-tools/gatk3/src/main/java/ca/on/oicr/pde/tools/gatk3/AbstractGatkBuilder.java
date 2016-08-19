package ca.on.oicr.pde.tools.gatk3;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author mlaszloffy
 */
public abstract class AbstractGatkBuilder<T> {

    protected final String javaPath;
    protected final String maxHeapSize;
    protected final String tmpDir;
    protected final String gatkJarPath;
    protected final String gatkKey;
    protected final String outputDir;

    protected String outputFileName;
    protected String referenceSequence;
    protected List<String> intervals = new LinkedList<>();
    protected List<String> intervalFiles = new LinkedList<>();
    protected SetRule intervalSetRule;// = SetRule.UNION;
    protected Integer intervalPadding;
    protected Integer numDataThreads;
    protected Integer numCpuThreadsPerDataThread;

    protected String extraParameters;

    public AbstractGatkBuilder(String javaPath, String maxHeapSize, String tmpDir, String gatkJarPath, String gatkKey, String outputDir) {
        this.javaPath = javaPath;
        this.maxHeapSize = maxHeapSize;
        this.tmpDir = tmpDir;
        this.gatkJarPath = getGatkJar(gatkJarPath);
        this.gatkKey = gatkKey;
        this.outputDir = outputDir;
    }

    private String getGatkJar(String gatkJarPath) {
        String jarPath;
        Path p = Paths.get(gatkJarPath);
        if (Files.isDirectory(p)) {
            Properties props = new Properties();
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("tool.properties")) {
                props.load(is);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            jarPath = Paths.get(p.toString(), props.getProperty("gatk_dir"), "GenomeAnalysisTK.jar").toString();
        } else {
            jarPath = gatkJarPath;
        }
        return jarPath;
    }

    public T setReferenceSequence(String referenceSequence) {
        this.referenceSequence = referenceSequence;
        return (T) this;
    }

    public T addInterval(String interval) {
        if (interval != null) {
            this.intervals.add(interval);
        }
        return (T) this;
    }

    public T addIntervals(Collection<String> intervals) {
        this.intervals.addAll(intervals);
        return (T) this;
    }

    public T addIntervalFile(String intervalFile) {
        this.intervalFiles.add(intervalFile);
        return (T) this;
    }

    public T addIntervalFiles(Collection<String> intervalFiles) {
        this.intervalFiles.addAll(intervalFiles);
        return (T) this;
    }

    public T setIntervalSetRule(SetRule setRule) {
        this.intervalSetRule = setRule;
        return (T) this;
    }

    public T setIntervalPadding(Integer intervalPadding) {
        this.intervalPadding = intervalPadding;
        return (T) this;
    }

    public T setNumDataThreads(int numDataThreads) {
        this.numDataThreads = numDataThreads;
        return (T) this;
    }

    public T setNumCpuThreadsPerDataThread(int numCpuThreadsPerDataThread) {
        this.numCpuThreadsPerDataThread = numCpuThreadsPerDataThread;
        return (T) this;
    }

    public T setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
        return (T) this;
    }

    public T setExtraParameters(String extraParameters) {
        this.extraParameters = extraParameters;
        return (T) this;
    }

    protected List<String> build(String type) {
        List<String> c = new LinkedList<>();

        c.add(javaPath);
        c.add("-Xmx" + maxHeapSize);
        c.add("-Djava.io.tmpdir=" + tmpDir);

        c.add("-jar");
        c.add(gatkJarPath);

        c.add("--analysis_type");
        c.add(type);

        c.add("--phone_home");
        c.add("NO_ET");
        c.add("--gatk_key");
        c.add(gatkKey);

        c.add("--logging_level");
        c.add("INFO");

        c.add("--reference_sequence");
        c.add(referenceSequence);

        for (String interval : intervals) {
            c.add("--intervals"); //aka -L
            c.add(interval);
        }

        for (String intervalFile : intervalFiles) {
            c.add("--intervals"); //aka -L
            c.add(intervalFile);
        }

        if (intervalSetRule != null) {
            c.add("--interval_set_rule");
            c.add(intervalSetRule.toString());
        } else if (!intervals.isEmpty() && !intervalFiles.isEmpty()) {
            c.add("--interval_set_rule");
            c.add(SetRule.INTERSECTION.toString());
        } else {
            // do not need to set "--interval_set_rule"
        }

        if (intervalPadding != null) {
            c.add("--interval_padding");
            c.add(intervalPadding.toString());
        }

        if (numDataThreads != null) {
            c.add("--num_threads"); //-nt
            c.add(numDataThreads.toString());
        }

        if (numCpuThreadsPerDataThread != null) {
            c.add("--num_cpu_threads_per_data_thread"); //-nct
            c.add(numCpuThreadsPerDataThread.toString());
        }

        if (extraParameters != null) {
            c.add(extraParameters);
        }

        return c;
    }

    public enum SetRule {

        UNION, INTERSECTION;
    }

}
