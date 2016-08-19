package ca.on.oicr.pde.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Study implements PdeObject, Name {

    private static final Map<String, Study> cache = new ConcurrentHashMap<>();

    private final String title;
    private final String swid;
    private final Map<String, Set<String>> attributes;

    private Study(Builder b) {
        title = b.title;
        swid = b.swid;
        if (b.attributes == null) {
            attributes = Collections.EMPTY_MAP;
        } else {
            attributes = new HashMap(b.attributes);
        }
    }

    public String getTitle() {
        return title;
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
    public String getName() {
        return title;
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

    @Override
    public String getTableName() {
        return "study";
    }

    public static class Builder {

        private String title;
        private String swid;
        private Map<String, Set<String>> attributes;

        public void setTitle(String title) {
            this.title = title;
        }

        public void setSwid(String swid) {
            this.swid = swid;
        }

        public void setAttributes(Map<String, Set<String>> attributes) {
            this.attributes = attributes;
        }

        public Study build() {
            String key = swid;
            Study r = cache.get(key);
            if (r == null) {
                r = new Study(this);
                cache.put(key, r);
            }
            return r;
        }

    }
    
    public static void clearCache(){
        cache.clear();
    }

}
