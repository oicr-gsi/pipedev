package ca.on.oicr.pde.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Processing implements Accessionable, Attributable {

    private static final Map<String, Processing> cache = new ConcurrentHashMap<String, Processing>();

    private final String algorithm;
    private final String swid;
    private final Map<String, Set<String>> attributes;

    public Processing(Builder b) {
        algorithm = b.algorithm;
        swid = b.swid;
        if (b.attributes == null) {
            attributes = Collections.EMPTY_MAP;
        } else {
            attributes = new HashMap(b.attributes);
        }
    }

    public String getAlgorithm() {
        return algorithm;
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

        private String algorithm;
        private String swid;
        private Map<String, Set<String>> attributes;

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public void setSwid(String swid) {
            this.swid = swid;
        }

        public void setAttributes(Map<String, Set<String>> attributes) {
            this.attributes = attributes;
        }

        public Processing build() {
            String key = swid;
            Processing r = cache.get(key);
            if (r == null) {
                r = new Processing(this);
                cache.put(key, r);
            }
            return r;
        }

    }

}
