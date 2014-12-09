package ca.on.oicr.pde.testing.decider;

import static ca.on.oicr.pde.utilities.Helpers.isFileAccessible;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class DeciderRunTestDefinition {

    private static String defaultDescription = "";
    private static String defaultMetricsDirectory = "";
    private static String defaultMetricsResources = "";
    private static Map<String, String> defaultParameters = new LinkedHashMap<>();
    private static Set<String> defaultIniExclusions = new HashSet<>();
    private static Set<String> defaultStudies = new HashSet<>();
    private static Set<String> defaultSequencerRuns = new HashSet<>();
    private static Set<String> defaultSamples = new HashSet<>();

    public Collection<DeciderRunTestDefinition.Test> tests;

    public static DeciderRunTestDefinition buildFromJson(String json) throws IOException {
        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return m.readValue(json, DeciderRunTestDefinition.class);
    }

    public void setDefaultDescription(String defaultDescription) {
        DeciderRunTestDefinition.defaultDescription = defaultDescription;
    }

    public void setDefaultMetricsDirectory(String defaultMetricsDirectory) {
        DeciderRunTestDefinition.defaultMetricsDirectory = defaultMetricsDirectory;
    }

    public void setDefaultMetricsResources(String defaultMetricsResources) {
        DeciderRunTestDefinition.defaultMetricsResources = defaultMetricsResources;
    }

    public void setDefaultParameters(Map<String, String> defaultParameters) {
        DeciderRunTestDefinition.defaultParameters = defaultParameters;
    }

    public void setDefaultStudies(Set<String> defaultStudies) {
        DeciderRunTestDefinition.defaultStudies = defaultStudies;
    }

    public void setDefaultSequencerRuns(Set<String> defaultSequencerRuns) {
        DeciderRunTestDefinition.defaultSequencerRuns = defaultSequencerRuns;
    }

    public void setDefaultSamples(Set<String> defaultSamples) {
        DeciderRunTestDefinition.defaultSamples = defaultSamples;
    }

    public void setDefaultIniExclusions(Set<String> defaultIniExclusions) {
        DeciderRunTestDefinition.defaultIniExclusions = defaultIniExclusions;
    }

    public static class Test {

        private String id = "";
        private String description = defaultDescription;
        private Set<String> studies = defaultStudies;
        private Set<String> sequencerRuns = defaultSequencerRuns;
        private Set<String> samples = defaultSamples;
        private final Map<String, String> parameters;
        private String metricsDirectory = defaultMetricsDirectory;
        private String metricsResources = defaultMetricsResources;
        private String metricsFile;
        private Set<String> iniExclusions = defaultIniExclusions;

        public Test() {
            parameters = new LinkedHashMap<String, String>();
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

        public String getMetricsResources() {
            return metricsResources;
        }

        public void setMetricsResources(String metricsResources) {
            this.metricsResources = metricsResources;
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
        public File getMetrics() throws IOException {

            String path;
            URL resource;

            path = metricsFile;
            if (isFileAccessible(path)) {
                return FileUtils.getFile(path);
            }

            resource = getClass().getResource(metricsResources + "/" + metricsFile);
            if (resource != null && isFileAccessible(resource.getPath())) {
                return FileUtils.getFile(resource.getPath());
            }

            resource = getClass().getResource(metricsResources + "/" + WordUtils.capitalizeFully(id.trim()).replaceAll("[^A-Za-z0-9]", "") + ".json");
            if (resource != null && isFileAccessible(resource.getPath())) {
                return FileUtils.getFile(resource.getPath());
            }

            path = metricsDirectory + "/" + metricsFile;
            if (isFileAccessible(path)) {
                return FileUtils.getFile(path);
            }

            path = metricsDirectory + "/" + WordUtils.capitalizeFully(id.trim()).replaceAll("[^A-Za-z0-9]", "") + ".json";
            if (isFileAccessible(path)) {
                return FileUtils.getFile(path);
            }

            return null;
        }

        @JsonIgnore
        //TODO: @JsonIgnore is broken...?: public String getMetrics() throws IOException {
        public String outputName() throws IOException {

            if (id != null && !id.isEmpty()) {
                return WordUtils.capitalizeFully(id.trim()).replaceAll("[^A-Za-z0-9]", "") + ".json";
            } else if (metricsFile != null && !metricsFile.isEmpty()) {
                if (isFileAccessible(metricsFile)) {
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
