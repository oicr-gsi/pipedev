package ca.on.oicr.pde.deciders.readgroups;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds a ReadGroupsGenerator from a configuration map.
 *
 * @author mlaszloffy
 */
public abstract class ReadGroupsGeneratorBuilder {

    protected final Map<String, String> configuration = new HashMap<>();

    /**
     * Get the required configuration for the ReadGroupsGenerator.
     *
     * @return configuration as key-value pairs
     */
    public Map<String, String> getConfiguration() {
        return configuration;
    }

    /**
     * Setup the configuration for the ReadGroupsGenerator.
     *
     * @param configuration configuration as key-value pairs
     */
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration.putAll(configuration);
    }

    /**
     * Configures and builds the ReadGroupsGenerator.
     *
     * @return configured ReadGroupsGenerator
     */
    public abstract ReadGroupsGenerator build();

    /**
     * Get the ReadGroupsGenerator name.
     *
     * @return ReadGroupsGenerator name
     */
    public abstract String getName();

}
