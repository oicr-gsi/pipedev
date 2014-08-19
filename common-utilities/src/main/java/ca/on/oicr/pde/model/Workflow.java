package ca.on.oicr.pde.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Workflow implements Accessionable {

    private static final Map<String, Workflow> cache = new ConcurrentHashMap<String, Workflow>();

    private final String swid;
    private final String name;
    private final String version;

    private Workflow(Builder b) {
        swid = b.swid;
        name = b.name;
        version = b.version;
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

        public void setSwid(String swid) {
            this.swid = swid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setVersion(String version) {
            this.version = version;
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

}
