package ca.on.oicr.pde.deciders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author mtaschuk
 */
public class WorkflowRun {

    private StringBuilder outputFiles = new StringBuilder();
    private Map<String, String> iniFile;
    private FileAttributes[] files;
    private List<Integer> iusSwidsToLinkWorkflowRunTo = new ArrayList<>();
    private Map<String,String> inputIusToOutputIus = new HashMap<>();

    /**
     * Create a local WorkflowRun object to locally store ini file properties and workflow run input files.
     * @param iniFile ini file key value pairs
     * @param files input files
     */
    public WorkflowRun(Map<String, String> iniFile, FileAttributes[] files) {
        if (iniFile == null) {
            this.iniFile = new HashMap<>();
        } else {
            this.iniFile = iniFile;
        }
        this.files = files;
        //Create a unique string for the job identifier
        String uuid = String.valueOf(UUID.randomUUID());
        this.iniFile.put("unique_string", uuid);
        this.iniFile.put("manual_output", "false");
    }

    /**
     * Add an ini file property.
     * @param property ini file property key
     * @param value ini file property value
     */
    public void addProperty(String property, String value) {
        addProperty(property, value, null);
    }

    /**
     * Add an ini file property.
     * @param property ini file property key
     * @param value ini file property value
     * @param dflt ini file property value if {@code value} is null or empty
     */
    public void addProperty(String property, String value, String dflt) {
        if (value == null || value.isEmpty()) {
            if (dflt != null && !dflt.isEmpty()) {
                iniFile.put(property, dflt);
            } else {
                (new Exception("Trying to add null or empty property " + property + ", using value:" + value + ", default:" + dflt)).printStackTrace();
            }
        } else {
            iniFile.put(property, value);
        }
    }
    
    /**
     * Add a map of ini file properties
     * @param properties a map of key value pairs to add to the ini file
     */
    public void addProperty(Map<String,String> properties){
        iniFile.putAll(properties);
    }

    /**
     * Add an output file to the workflow run.
     * @param workingFilepath the relative path to the output file that a workflow run will produce
     * @param metatype the metatype of the future output file
     * @return a StringBuilder object with the output file added
     */
    public StringBuilder addOutputFile(String workingFilepath, String metatype) {
        String s = "%s::%s";
        if (outputFiles.length() > 0) {
            outputFiles.append(",");
        }
        outputFiles.append(String.format(s, workingFilepath, metatype));
        addProperty("output_files", outputFiles.toString());
        return outputFiles;
    }

    /**
     * Get input files.
     * @return Array of input files in FileAttribute object format
     */
    public FileAttributes[] getFiles() {
        return files;
    }

    /**
     * Get the ini file map representation.
     * @return map of key value ini file property pairs
     */
    public Map<String, String> getIniFile() {
        return iniFile;
    }

    /**
     * Get the output file object
     * @return StringBuilder of output file paths + metatypes
     */
    public StringBuilder getOutputFiles() {
        return outputFiles;
    }

    /**
     * Set the "my_script" ini file property
     * @param script my_script value
     */
    public void setScript(String script) {
        addProperty("my_script", script);
    }

    /**
     * Get the "my_script" ini file property
     * @return my_script value
     */
    public String getScript() {
        return iniFile.get("my_script");
    }
    
    /**
     * Set the "my_script_parameters" ini file property
     * @param parameters my_script_parameters value
     */
    public void setScriptParameters(String parameters) {
        addProperty("my_script_parameters", parameters);
    }
    
    /**
     * Get the "my_script_parameters" ini file property
     * @return my_script_parameters value
     */
    public String getScriptParameters() {
        return iniFile.get("my_script_parameters");
    }

    /**
     * Set the "my_script_mem_mb" ini file property
     * @param memoryInMb my_script_mem_mb value
     */
    public void setScriptMemory(String memoryInMb) {
        addProperty("my_script_mem_mb", String.valueOf(memoryInMb), "2000");
    }

    /**
     * Get the "my_script_mem_mb" ini file property
     * @return my_script_mem_mb value
     */
    public String getScriptMemory() {
        return iniFile.get("my_script_mem_mb");
    }

    /**
     * Get the "unique_string" ini file property
     * @return unique_string value
     */
    public String getUniqueString() {
        return iniFile.get("unique_string");
    }
    
    /**
     * Set the "manual_output" ini file property
     * @param useManualOutput manual_output value
     */
    public void setManualOutput(boolean useManualOutput) {
        iniFile.put("manual_output", String.valueOf(useManualOutput));
    }
    
    /**
     * Get the "manual_output" ini file property
     * @return manual_output value
     */
    public String getManualOutput(){
        return iniFile.get("manual_output");
    }

    /**
     * Get the list of IUS swids that the workflow run should be linked to
     *
     * @return list of IUS swids
     */
    public List<Integer> getIusSwidsToLinkWorkflowRunTo() {
        return iusSwidsToLinkWorkflowRunTo;
    }

    /**
     * Set the list of IUS swids that the workflow run should be linked to
     *
     * @param iusSwidsToLinkWorkflowRunTo list of IUS swids
     */
    public void setIusSwidsToLinkWorkflowRunTo(List<Integer> iusSwidsToLinkWorkflowRunTo) {
        this.iusSwidsToLinkWorkflowRunTo = iusSwidsToLinkWorkflowRunTo;
    }

    /**
     * Get the mapping of input IUS swids to output IUS swids that the workflow run should be linked to
     *
     * @return map of input IUS swids to output IUS swids
     */
    public Map<String, String> getInputIusToOutputIus() {
        return Collections.unmodifiableMap(inputIusToOutputIus);
    }

    /**
     * Set the mapping of input IUS swids to output IUS swids that the workflow run should be linked to
     *
     * @param inputIusToOutputIus
     */
    public void setInputIusToOutputIus(Map<String, String> inputIusToOutputIus) {
        this.inputIusToOutputIus.putAll(inputIusToOutputIus);
    }

}
