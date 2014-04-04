package ca.on.oicr.pde.model;

import java.util.Map;

public interface Attributable {

    public Map<String,String> getAttributes();

    public void setAttributes(String attributes);
    
    public String getAttribute(String key);
    
}
