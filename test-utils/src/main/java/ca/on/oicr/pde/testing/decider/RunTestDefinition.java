package ca.on.oicr.pde.testing.decider;

import ca.on.oicr.pde.utilities.Helpers;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author mlaszloffy
 */
public class RunTestDefinition {

    private String id;
    private String description;
    private Set<String> studies;
    private Set<String> sequencerRuns;
    private Set<String> samples;
    private String workflowName;
    private String workflowVersion;
    private final Map<String, List<String>> parameters;
    private String metricsDirectory;
    private String metricsResources;
    private String metricsFile;
    private Set<String> iniExclusions;
    private Map<String, String> iniSubstitutions;
    private Map<String, String> iniStringSubstitutions;

    public RunTestDefinition() {
        parameters = new LinkedHashMap<>();
        id = "";
        description = "";
        studies = new HashSet<>();
        sequencerRuns = new HashSet<>();
        samples = new HashSet<>();
        metricsDirectory = "";
        metricsResources = "";
        iniExclusions = new HashSet<>();
        iniSubstitutions = new HashMap<>();
        iniStringSubstitutions = new HashMap<>();
    }

    public RunTestDefinition(@JacksonInject final RunTestDefinition defaults) {
        this();
        description = defaults.getDescription();
        studies = defaults.getStudies();
        sequencerRuns = defaults.getSequencerRuns();
        samples = defaults.getSamples();
        parameters.putAll(defaults.getParameters());
        metricsDirectory = defaults.getMetricsDirectory();
        metricsResources = defaults.getMetricsResources();
        iniExclusions = defaults.getIniExclusions();
        iniSubstitutions = defaults.getIniSubstitutions();
        iniStringSubstitutions = defaults.getIniStringSubstitutions();
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
        this.studies = new TreeSet<>(studies);
    }

    public Set<String> getSequencerRuns() {
        return Collections.unmodifiableSet(sequencerRuns);
    }

    public void setSequencerRuns(Set<String> sequencerRuns) {
        this.sequencerRuns = new TreeSet<>(sequencerRuns);
    }

    public Set<String> getSamples() {
        return Collections.unmodifiableSet(samples);
    }

    public void setSamples(Set<String> samples) {
        this.samples = new TreeSet<>(samples);
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public void setWorkflowVersion(String workflowVersion) {
        this.workflowVersion = workflowVersion;
    }

    public Map<String, List<String>> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters.putAll(parseParameters(parameters));
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
        this.iniExclusions = new TreeSet<>(iniExclusions);
    }

    public Map<String, String> getIniSubstitutions() {
        return Collections.unmodifiableMap(iniSubstitutions);
    }

    public void setIniSubstitutions(Map<String, String> iniSubstitutions) {
        this.iniSubstitutions = new HashMap<>(iniSubstitutions);
    }

    public Map<String, String> getIniStringSubstitutions() {
        return Collections.unmodifiableMap(iniStringSubstitutions);
    }

    public void setIniStringSubstitutions(Map<String, String> iniStringSubstitutions) {
        this.iniStringSubstitutions = new HashMap<>(iniStringSubstitutions);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @JsonIgnore
    public File getMetrics() throws IOException {
        String path;
        URL resource;
        path = metricsFile;
        if (Helpers.isFileAccessible(path)) {
            return FileUtils.getFile(path);
        }
        resource = getClass().getResource(metricsResources + "/" + metricsFile);
        if (resource != null && Helpers.isFileAccessible(resource.getPath())) {
            return FileUtils.getFile(resource.getPath());
        }
        resource = getClass().getResource(metricsResources + "/" + WordUtils.capitalizeFully(id.trim()).replaceAll("[^A-Za-z0-9]", "") + ".json");
        if (resource != null && Helpers.isFileAccessible(resource.getPath())) {
            return FileUtils.getFile(resource.getPath());
        }
        path = metricsDirectory + "/" + metricsFile;
        if (Helpers.isFileAccessible(path)) {
            return FileUtils.getFile(path);
        }
        path = metricsDirectory + "/" + WordUtils.capitalizeFully(id.trim()).replaceAll("[^A-Za-z0-9]", "") + ".json";
        if (Helpers.isFileAccessible(path)) {
            return FileUtils.getFile(path);
        }
        return null;
    }

    @JsonIgnore
    public String outputName() throws IOException {
        if (id != null && !id.isEmpty()) {
            return WordUtils.capitalizeFully(id.trim()).replaceAll("[^A-Za-z0-9]", "") + ".json";
        } else if (metricsFile != null && !metricsFile.isEmpty()) {
            if (Helpers.isFileAccessible(metricsFile)) {
                return FileUtils.getFile(metricsFile).getName();
            } else {
                return metricsFile;
            }
        } else {
            return "output.json";
        }
    }

    private Map<String, List<String>> parseParameters(Map<String, Object> parameters) {
        Map<String, List<String>> ps = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : parameters.entrySet()) {
            String key = e.getKey();
            Object o = e.getValue();
            if (o == null) {
                ps.put(key, null);
            } else if (o instanceof List<?>) {
                ps.put(key, (List<String>) o);
            } else if (o instanceof String) {
                ps.put(key, Arrays.asList((String) o));
            } else {
                throw new RuntimeException("Unsupported object found in test definition parameters.");
            }
        }
        return ps;
    }

}
