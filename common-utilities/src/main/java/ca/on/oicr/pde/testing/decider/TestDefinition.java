package ca.on.oicr.pde.testing.decider;

import static ca.on.oicr.pde.utilities.Helpers.isAccessible;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

public class TestDefinition {

    private static String defaultDescription = "";
    private static String defaultMetricsDirectory = "";
    private static Map<String, String> defaultParameters = new LinkedHashMap<String, String>();
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

    public void setDefaultParameters(Map<String, String> defaultParameters) {
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

        private String id = "";
        private String description = defaultDescription;
        private Set<String> studies = defaultStudies;
        private Set<String> sequencerRuns = defaultSequencerRuns;
        private Set<String> samples = defaultSamples;
        private final Map<String, String> parameters;
        private String metricsDirectory = defaultMetricsDirectory;
        private String metricsFile;
        private Set<String> iniExclusions = defaultIniExclusions;

        public Test(){
            parameters = new LinkedHashMap<String,String>();
            parameters.putAll(defaultParameters);
        }
        
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Set<String> getStudies() {
            return Collections.unmodifiableSet(studies);
        }

        public void setStudies(Set<String> studies) {
            this.studies = studies;
        }

        public Set<String> getSequencerRuns() {
            return Collections.unmodifiableSet(sequencerRuns);
        }

        public void setSequencerRuns(Set<String> sequencerRuns) {
            this.sequencerRuns = sequencerRuns;
        }

        public Set<String> getSamples() {
            return Collections.unmodifiableSet(samples);
        }

        public void setSamples(Set<String> samples) {
            this.samples = samples;
        }

        public Map<String, String> getParameters() {
            return Collections.unmodifiableMap(parameters);
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters.putAll(parameters);
        }

        public String getMetricsDirectory() {
            return metricsDirectory;
        }

        public void setMetricsDirectory(String metricsDirectory) {
            this.metricsDirectory = metricsDirectory;
        }

        public String getMetricsFile() {
            return metricsFile;
        }

        public void setMetricsFile(String metricsFile) {
            this.metricsFile = metricsFile;
        }

        public Set<String> getIniExclusions() {
            return Collections.unmodifiableSet(iniExclusions);
        }

        public void setIniExclusions(Set<String> iniExclusions) {
            this.iniExclusions = iniExclusions;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
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
