package ca.on.oicr.pde.model;

import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SequencerRun implements Accessionable, Attributable, Name {

    private String name;
    private String swid;
    private Map<String, String> attributes;

    public SequencerRun() {
        attributes = Collections.EMPTY_MAP;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Map getAttributes() {
        return new HashMap(attributes);
    }

    @Override
    public void setAttributes(String attributes) {

        this.attributes = Splitter.on(";").omitEmptyStrings().withKeyValueSeparator("=").split(attributes);

    }

    @Override
    public String getAttribute(String key) {

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
