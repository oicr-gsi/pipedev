package ca.on.oicr.pde.model;

import java.util.Map;
import java.util.Set;

public interface Attributable {

    public Map<String,Set<String>> getAttributes();

    public void setAttributes(Map<String,Set<String>> attributes);
    
    public Set<String> getAttribute(String key);
    
}
