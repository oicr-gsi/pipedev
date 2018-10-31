package ca.on.oicr.pde.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Workflow implements PdeObject {

    private static final Map<String, Workflow> cache = new ConcurrentHashMap<>();

    private final String swid;
    private final String name;
    private final String version;
    private final Map<String, Set<String>> attributes;

    private Workflow(Builder b) {
        swid = b.swid;
        name = b.name;
        version = b.version;
        if (b.attributes == null) {
            attributes = Collections.emptyMap();
        } else {
            attributes = new HashMap<>(b.attributes);
        }
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
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

    @Override
    public String getTableName() {
        return "workflow";
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

        private String swid;
        private String name;
        private String version;
        private Map<String, Set<String>> attributes;

        public void setSwid(String swid) {
            this.swid = swid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public void setAttributes(Map<String, Set<String>> attributes) {
            this.attributes = attributes;
        }

        public Workflow build() {
            String key = swid;
            Workflow r = cache.get(key);
            if (r == null) {
                r = new Workflow(this);
                cache.put(key, r);
            }
            return r;
        }
    }

    public static void clearCache() {
        cache.clear();
    }

}
