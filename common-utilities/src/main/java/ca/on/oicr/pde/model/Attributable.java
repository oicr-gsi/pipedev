package ca.on.oicr.pde.model;

import java.util.Map;
import java.util.Set;

public interface Attributable {

    public Map<String,Set<String>> getAttributes();

    public Set<String> getAttribute(String key);
    
}
