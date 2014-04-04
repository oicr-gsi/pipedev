package ca.on.oicr.pde.testing.decider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

public class TestDefinition {

    private static String defaultDescription = "";
    private static String defaultMetricsDirectory = "";
    private static Map<String, Object> defaultParameters = new LinkedHashMap<String, Object>();
    private static Set<String> defaultIniExclusions = new HashSet<String>();
    private static Set<String> defaultStudies = new HashSet<String>();
    private static Set<String> defaultSequencerRuns = new HashSet<String>();
    private static Set<String> defaultSamples = new HashSet<String>();

    public Collection<TestDefinition.Test> tests;

    public static TestDefinition buildFromJson(String json) throws IOException {
        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return m.readValue(json, TestDefinition.class);
    }

    public void setDefaultDescription(String defaultDescription) {
        TestDefinition.defaultDescription = defaultDescription;
    }

    public void setDefaultMetricsDirectory(String defaultMetricsDirectory) {
        TestDefinition.defaultMetricsDirectory = defaultMetricsDirectory;
    }

    public void setDefaultParameters(Map<String, Object> defaultParameters) {
        TestDefinition.defaultParameters = defaultParameters;
    }

    public void setDefaultStudies(Set<String> defaultStudies) {
        TestDefinition.defaultStudies = defaultStudies;
    }

    public void setDefaultSequencerRuns(Set<String> defaultSequencerRuns) {
        TestDefinition.defaultSequencerRuns = defaultSequencerRuns;
    }

    public void setDefaultSamples(Set<String> defaultSamples) {
        TestDefinition.defaultSamples = defaultSamples;
    }

    public void setDefaultIniExclusions(Set<String> defaultIniExclusions) {
        TestDefinition.defaultIniExclusions = defaultIniExclusions;
    }

    public static class Test {

        public String id = "";
        public String description = defaultDescription;
        public Set<String> studies = defaultStudies;
        public Set<String> sequencerRuns = defaultSequencerRuns;
        public Set<String> samples = defaultSamples;
        public Map<String, Object> parameters = defaultParameters;
        public String metricsDirectory = defaultMetricsDirectory;
        public String metricsFile;

        public Set<String> iniExclusions = defaultIniExclusions;

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        private boolean isAccessible(String s) {

            if (s == null || s.isEmpty()) {
                return false;
            }

            File f = FileUtils.getFile(s);

            if (f == null) {
                return false;
            }

            return f.exists() && f.isFile() && f.canRead();

        }

        @JsonIgnore
        //TODO: @JsonIgnore is broken...?: public String getMetrics() throws IOException {
        public File metrics() throws IOException {

            if (isAccessible(metricsFile)) {
                return FileUtils.getFile(metricsFile);
            } else if (isAccessible(defaultMetricsDirectory + "/" + metricsFile)) {
                return FileUtils.getFile(defaultMetricsDirectory + "/" + metricsFile);
            } else if (isAccessible(defaultMetricsDirectory + "/" + WordUtils.capitalizeFully(id.trim()).replaceAll("[^A-Za-z0-9]", "") + ".json")) {
                return FileUtils.getFile(defaultMetricsDirectory + "/" + WordUtils.capitalizeFully(id.trim()).replaceAll("[^A-Za-z0-9]", "") + ".json");
            } //                else if (isAccessible(defaultMetricsDirectory + "/" + testName + ".json")) {
            //                return FileUtils.getFile(defaultMetricsDirectory + "/" + testName + ".json");
            //            } 
            else {
                return null;
            }

        }

        @JsonIgnore
        //TODO: @JsonIgnore is broken...?: public String getMetrics() throws IOException {
        public String outputName() throws IOException {

            if (id != null && !id.isEmpty()) {
                return WordUtils.capitalizeFully(id.trim()).replaceAll("[^A-Za-z0-9]", "") + ".json";
            } else if (metricsFile != null && !metricsFile.isEmpty()) {
                if (isAccessible(metricsFile)) {
                    return FileUtils.getFile(metricsFile).getName();
                } else {
                    return metricsFile;
                }
            } else {
                return "output.json";
            }

        }
    }

    @Override
    public String toString() {
        //return "TestDefinition{" + "tests=" + tests + '}';
        return ToStringBuilder.reflectionToString(this);
    }

}
