package ca.on.oicr.pde.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class File implements SeqwareObject {

    private static final Map<String, File> cache = new ConcurrentHashMap<>();

    private final String metaType;
    private final String swid;
    private final Map<String, Set<String>> attributes;
    private final String path;
    private final String md5sum;
    private final String size;
    private final String description;

    private File(Builder b) {
        metaType = b.metaType;
        swid = b.swid;
        path = b.path;
        md5sum = b.md5sum;
        size = b.size;
        description = b.description;
        if (b.attributes == null) {
            attributes = Collections.EMPTY_MAP;
        } else {
            attributes = new HashMap(b.attributes);
        }
    }

    public String getMetaType() {
        return metaType;
    }

    @Override
    public String getSwid() {
        return swid;
    }

    @Override
    public Map<String, Set<String>> getAttributes() {
        return new HashMap(attributes);
    }

    @Override
    public Set<String> getAttribute(String key) {
        return this.attributes.get(key);
    }

    public String getPath() {
        return path;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public String getSize() {
        return size;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getTableName() {
        return "file";
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

    public static class Builder {

        private String metaType;
        private String swid;
        private Map<String, Set<String>> attributes;
        private String path;
        private String md5sum;
        private String size;
        private String description;

        public void setMetaType(String metaType) {
            this.metaType = metaType;
        }

        public void setSwid(String swid) {
            this.swid = swid;
        }

        public void setAttributes(Map<String, Set<String>> attributes) {
            this.attributes = attributes;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setMd5sum(String md5sum) {
            this.md5sum = md5sum;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public File build() {
            String key = swid;
            File r = cache.get(key);
            if (r == null) {
                r = new File(this);
                cache.put(key, r);
            }
            return r;
        }

    }

    public static void clearCache() {
        cache.clear();
    }

}
