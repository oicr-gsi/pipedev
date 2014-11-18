package ca.on.oicr.pde.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Experiment implements SeqwareObject {

    private static final Map<String, Experiment> cache = new ConcurrentHashMap<String, Experiment>();

    private final String name;
    private final String swid;
    private final Map<String, Set<String>> attributes;

    private Experiment(Builder b) {
        name = b.name;
        swid = b.swid;
        if (b.attributes == null) {
            attributes = Collections.EMPTY_MAP;
        } else {
            attributes = new HashMap(b.attributes);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Set<String>> getAttributes() {
        return new HashMap<String, Set<String>>(attributes);
    }

    @Override
    public Set<String> getAttribute(String key) {
        return this.attributes.get(key);
    }

    @Override
    public String getSwid() {
        return swid;
    }

    @Override
    public String getTableName() {
        return "experiment";
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

        private String name;
        private String swid;
        private Map<String, Set<String>> attributes;

        public void setName(String name) {
            this.name = name;
        }

        public void setSwid(String swid) {
            this.swid = swid;
        }

        public void setAttributes(Map<String, Set<String>> attributes) {
            this.attributes = attributes;
        }

        public Experiment build() {
            String key = swid;
            Experiment r = cache.get(key);
            if (r == null) {
                r = new Experiment(this);
                cache.put(key, r);
            }
            return r;
        }

    }

    public static void clearCache() {
        cache.clear();
    }

}
