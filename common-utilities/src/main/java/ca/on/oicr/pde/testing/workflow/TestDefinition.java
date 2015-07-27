package ca.on.oicr.pde.testing.workflow;

import static ca.on.oicr.pde.utilities.Helpers.isFileAccessible;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class TestDefinition {

    private static Defaults defaults;
    private Collection<ca.on.oicr.pde.testing.workflow.TestDefinition.Test> tests;

    public static ca.on.oicr.pde.testing.workflow.TestDefinition buildFromJson(String json) throws IOException {
        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return m.readValue(json, ca.on.oicr.pde.testing.workflow.TestDefinition.class);
    }

    @JsonProperty("defaults")
    public void setDefaults(Defaults defaults) {
        TestDefinition.defaults = defaults;
    }

    @JsonProperty("tests")
    public void setTests(Collection<ca.on.oicr.pde.testing.workflow.TestDefinition.Test> tests) {
        this.tests = tests;
    }

    public Collection<TestDefinition.Test> getTests() {
        return Collections.unmodifiableCollection(tests);
    }

    public static class Defaults {

        private String description = "";
        private String metricsFilePath = "";
        private String metricsDirectoryPath = "";
        private String metricsCalculateScript = "";
        private String metricsCompareScript = "";
        private String iniDirectory = "";
        private int iterations = 1;
        private Map<String, String> parameters = new LinkedHashMap<>();
        private Map<String, String> enviromentVariables = new LinkedHashMap<>();

        public void setDescription(String description) {
            this.description = description;
        }

        @JsonProperty("metrics_file")
        public void setMetricsFilePath(String metricsFilePath) {
            this.metricsFilePath = metricsFilePath;
        }

        @JsonProperty("output_metrics_dir")
        public void setMetricsDirectoryPath(String metricsDirectoryPath) {
            this.metricsDirectoryPath = metricsDirectoryPath;
        }

        //@JsonProperty("defaultMetricsCalculateScript, metrics_calculate")
        @JsonProperty("metrics_calculate")
        public void setMetricsCalculateScript(String metricsCalculateScript) {
            this.metricsCalculateScript = metricsCalculateScript;
        }

        //@JsonProperty("defaultMetricsCompareScript, metrics_compare")
        @JsonProperty("metrics_compare")
        public void setMetricsCompareScript(String metricsCompareScript) {
            this.metricsCompareScript = metricsCompareScript;
        }

        @JsonProperty("input_config_dir")
        public void setIniDirectory(String iniDirectory) {
            this.iniDirectory = iniDirectory;
        }

        @JsonProperty("iterations")
        public void setIterations(int iterations) {
            this.iterations = iterations;
        }

        @JsonProperty("parameters")
        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }

        @JsonProperty("environment_variables")
        public void setEnvironmentVariables(Map<String, String> environmentVariables) {
            this.enviromentVariables = environmentVariables;
        }

        @Override
        public String toString() {
            //return "TestDefinition{" + "tests=" + tests + '}';
            return ToStringBuilder.reflectionToString(this);
        }

    }

    public static class Test {

        private String id;
        private String description = defaults.description;
        private String metricsFilePath = defaults.metricsFilePath;
        private String metricsDirectoryPath = defaults.metricsDirectoryPath;
        private String metricsCalculateScript = defaults.metricsCalculateScript;
        private String metricsCompareScript = defaults.metricsCompareScript;
        private String iniDirectoryPath = defaults.iniDirectory;
        private int iterations = defaults.iterations;
        private String iniFilePath = "";
        private final Map<String, String> parameters;
        private final Map<String, String> environmentVariables;

        public Test() {
            parameters = new LinkedHashMap<>(defaults.parameters);
            environmentVariables = new LinkedHashMap<>(defaults.enviromentVariables);
        }

        public String getId() {
            return id;
        }

        @JsonProperty("id")
        public void setId(String id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        @JsonProperty("description")
        public void setDescription(String description) {
            this.description = description;
        }

        public String getMetricsFilePath() {
            return metricsFilePath;
        }

        @JsonProperty("metrics_file")
        public void setMetricsFilePath(String metricsFilePath) {
            this.metricsFilePath = metricsFilePath;
        }

        public String getMetricsDirectoryPath() {
            return metricsDirectoryPath;
        }

        @JsonProperty("output_metrics_dir")
        public void setMetricsDirectoryPath(String metricsDirectoryPath) {
            this.metricsDirectoryPath = metricsDirectoryPath;
        }

        public String getMetricsCalculateScript() {
            return metricsCalculateScript;
        }

        @JsonProperty("metrics_calculate")
        public void setMetricsCalculateScript(String metricsCalculateScript) {
            this.metricsCalculateScript = metricsCalculateScript;
        }

        public String getMetricsCompareScript() {
            return metricsCompareScript;
        }

        @JsonProperty("metrics_compare")
        public void setMetricsCompareScript(String metricsCompareScript) {
            this.metricsCompareScript = metricsCompareScript;
        }

        public String getIniFilePath() {
            return iniFilePath;
        }

        @JsonProperty("input_config")
        public void setIniFilePath(String iniFilePath) {
            this.iniFilePath = iniFilePath;
        }

        public String getIniDirectoryPath() {
            return iniDirectoryPath;
        }

        @JsonProperty("input_config_dir")
        public void setIniDirectory(String iniDirectoryPath) {
            this.iniDirectoryPath = iniDirectoryPath;
        }

        public int getIterations() {
            return iterations;
        }

        @JsonProperty("iterations")
        public void setIterations(int iterations) {
            this.iterations = iterations;
        }

        public Map<String, String> getParameters() {
            return Collections.unmodifiableMap(parameters);
        }

        @JsonProperty("parameters")
        public void setParameters(Map<String, String> parameters) {
            this.parameters.putAll(parameters);
        }

        public Map<String, String> getEnvironmentVariables() {
            return Collections.unmodifiableMap(environmentVariables);
        }

        @JsonProperty("environment_variables")
        public void setEnvironmentVariables(Map<String, String> environmentVariables) {
            this.environmentVariables.putAll(environmentVariables);
        }

        @JsonIgnore
        public File getIniFile() {
            File iniFile = null;
            if ("".equals(iniFilePath)) {
                //"input_config" is not set, assuming the workflow parameters are set in the "parameters" field
                return null;
            } else if (isFileAccessible(iniFilePath)) {
                iniFile = new File(iniFilePath);
            } else if (isFileAccessible(iniDirectoryPath + "/" + iniFilePath)) {
                iniFile = new File(iniDirectoryPath + "/" + iniFilePath);
            } else {
                //"input_config" is set but the ini file is not accessible
                throw new RuntimeException(String.format("The ini file [%s] is not accessible", iniFilePath));
            }
            return iniFile;
        }

        @JsonIgnore
        public File getMetricsFile() {
            File metricsFile = null;
            if (isFileAccessible(metricsFilePath)) {
                metricsFile = new File(metricsFilePath);
            } else if (isFileAccessible(metricsDirectoryPath + "/" + metricsFilePath)) {
                metricsFile = new File(metricsDirectoryPath + "/" + metricsFilePath);
            } else if (getId() != null && isFileAccessible(metricsDirectoryPath + "/" + getId() + ".metrics")) {
                metricsFile = new File(metricsDirectoryPath + "/" + getId() + ".metrics");
            } else if (getIniFile() != null && isFileAccessible(metricsDirectoryPath + "/" + getIniFile().getName() + ".metrics")) {
                metricsFile = new File(metricsDirectoryPath + "/" + getIniFile().getName() + ".metrics");
            } else {
                //no valid metrics file path
                metricsFile = null;
            }
            return metricsFile;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

    }

    @Override
    public String toString() {
        //return "TestDefinition{" + "tests=" + tests + '}';
        return ToStringBuilder.reflectionToString(this);
    }

}
