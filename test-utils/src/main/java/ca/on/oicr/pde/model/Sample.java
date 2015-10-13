package ca.on.oicr.pde.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Sample implements SeqwareObject, Name {

    private static final Map<String, Sample> cache = new ConcurrentHashMap<>();

    private final String name;
    private final String swid;
    private final String organismId;
    private final String organismCode;
    private final Map<String, Set<String>> attributes;

    private Sample(Builder b) {
        name = b.name;
        swid = b.swid;
        organismId = b.organismId;
        organismCode = b.organismCode;
        if (b.attributes == null) {
            attributes = Collections.EMPTY_MAP;
        } else {
            attributes = new HashMap(b.attributes);
        }
    }

    @Override
    public String getName() {
        return name;
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
    public String getSwid() {
        return swid;
    }

    public String getOrganismId() {
        return organismId;
    }

    public String getOrganismCode() {
        return organismCode;
    }

    @Override
    public String getTableName() {
        return "sample";
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
        private String organismId;
        private String organismCode;
        private Map<String, Set<String>> attributes;

        public void setName(String name) {
            this.name = name;
        }

        public void setSwid(String swid) {
            this.swid = swid;
        }

        public void setOrganismId(String organismId) {
            this.organismId = organismId;
        }

        public void setOrganismCode(String organismCode) {
            this.organismCode = organismCode;
        }

        public void setAttributes(Map<String, Set<String>> attributes) {
            this.attributes = attributes;
        }

        public Sample build() {
            String key = swid;
            Sample r = cache.get(key);
            if (r == null) {
                r = new Sample(this);
                cache.put(key, r);
            }
            return r;
        }

    }

    public static void clearCache() {
        cache.clear();
    }

}
