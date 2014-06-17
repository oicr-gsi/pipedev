package ca.on.oicr.pde.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Ius implements Accessionable, Attributable {

    private String tag;
    private String swid;
    private Map<String, Set<String>> attributes;

    public Ius() {
        attributes = Collections.EMPTY_MAP;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String iusTag) {
        this.tag = iusTag;
    }

    @Override
    public Map<String, Set<String>> getAttributes() {
        return new HashMap(attributes);
    }

    @Override
    public void setAttributes(Map<String, Set<String>> attributes) {

        this.attributes = attributes;

    }

    @Override
    public Set<String> getAttribute(String key) {

        return this.attributes.get(key);

    }

    @Override
    public String getSwid() {
        return swid;
    }

    public void setSwid(String swid) {
        this.swid = swid;
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
        //
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
