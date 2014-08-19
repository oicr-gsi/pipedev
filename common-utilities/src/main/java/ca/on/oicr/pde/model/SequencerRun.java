package ca.on.oicr.pde.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SequencerRun implements Accessionable, Attributable, Name {

    private static final Map<String, SequencerRun> cache = new ConcurrentHashMap<String, SequencerRun>();

    private final String name;
    private final String swid;
    private final String platformId;
    private final String platformName;
    private final Map<String, Set<String>> attributes;

    private SequencerRun(Builder b) {
        name = b.name;
        swid = b.swid;
        platformId = b.platformId;
        platformName = b.platformName;
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

    public String getPlatformId() {
        return platformId;
    }

    public String getPlatformName() {
        return platformName;
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
        private String platformId;
        private String platformName;
        private Map<String, Set<String>> attributes;

        public void setName(String name) {
            this.name = name;
        }

        public void setSwid(String swid) {
            this.swid = swid;
        }

        public void setPlatformId(String platformId) {
            this.platformId = platformId;
        }

        public void setPlatformName(String platformName) {
            this.platformName = platformName;
        }

        public void setAttributes(Map<String, Set<String>> attributes) {
            this.attributes = attributes;
        }

        public SequencerRun build() {
            String key = swid;
            SequencerRun r = cache.get(key);
            if (r == null) {
                r = new SequencerRun(this);
                cache.put(key, r);
            }
            return r;
        }

    }

}
