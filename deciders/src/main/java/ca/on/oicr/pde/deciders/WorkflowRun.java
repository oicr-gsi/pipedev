package ca.on.oicr.pde.deciders;

import java.util.HashMap;
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

    public WorkflowRun(Map<String, String> iniFile, FileAttributes[] files) {
        if (iniFile == null) {
            this.iniFile = new HashMap<String, String>();
        } else {
            this.iniFile = iniFile;
        }
        this.files = files;
        //Create a unique string for the job identifier
        String uuid = String.valueOf(UUID.randomUUID());
        this.iniFile.put("unique_string", uuid);
        this.iniFile.put("manual_output", "false");
    }

    public void addProperty(String property, String value) {
        addProperty(property, value, null);
    }

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
    
    public void addProperty(Map<String,String> properties){
        iniFile.putAll(properties);
    }

    public StringBuilder addOutputFile(String workingFilepath, String metatype) {
        String s = "%s::%s";
        if (outputFiles.length() > 0) {
            outputFiles.append(",");
        }
        outputFiles.append(String.format(s, workingFilepath, metatype));
        addProperty("output_files", outputFiles.toString());
        return outputFiles;
    }

    public FileAttributes[] getFiles() {
        return files;
    }

    public Map<String, String> getIniFile() {
        return iniFile;
    }

    public StringBuilder getOutputFiles() {
        return outputFiles;
    }

    public void setScript(String script) {
        addProperty("my_script", script);
    }

    public String getScript(String script) {
        return iniFile.get("my_script");
    }
    
    public void setScriptParameters(String parameters) {
        addProperty("my_script_parameters", parameters);
    }
    
    public String getScriptParameters() {
        return iniFile.get("my_script_parameters");
    }

    public void setScriptMemory(String memoryInMb) {
        addProperty("my_script_mem_mb", String.valueOf(memoryInMb), "2000");
    }
    public String getScriptMemory() {
        return iniFile.get("my_script_mem_mb");
    }

    public String getUniqueString() {
        return iniFile.get("unique_string");
    }
    
    public void setManualOutput(boolean useManualOutput) {
        iniFile.put("manual_output", String.valueOf(useManualOutput));
    }
    
    public String getManualOutput(){
        return iniFile.get("manual_output");
    }
    
}