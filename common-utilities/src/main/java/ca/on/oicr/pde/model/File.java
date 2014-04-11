package ca.on.oicr.pde.model;

import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class File implements Accessionable, Attributable {

    private String metaType;
    private String swid;
    private Map<String, String> attributes;
    private String path;
    private String md5sum;
    private String size;
    private String description;  

    public File() {
        attributes = Collections.EMPTY_MAP;
    }

    public String getMetaType() {
        return metaType;
    }

    public void setMetaType(String metaType) {
        this.metaType = metaType;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String fileDescription) {
        this.description = fileDescription;
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
