package ca.on.oicr.pde.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class WorkflowRun implements SeqwareObject {

    private static final Map<String, WorkflowRun> cache = new ConcurrentHashMap<>();

    private final String name;
    private final String status;
    private final String swid;
    private final Map<String, Set<String>> attributes;
    private final Set<String> inputFileSwids;

    private WorkflowRun(Builder b) {
        name = b.name;
        status = b.status;
        swid = b.swid;
        if (b.attributes == null) {
            attributes = Collections.EMPTY_MAP;
        } else {
            attributes = new HashMap(b.attributes);
        }
        inputFileSwids = b.inputFileSwids;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
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

    public Set<String> getInputFileSwids() {
        return new HashSet(inputFileSwids);
    }

    @Override
    public String getTableName() {
        return "workflow-run";
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
        private String status;
        private String swid;
        private Map<String, Set<String>> attributes;
        private Set<String> inputFileSwids;

        public void setName(String name) {
            this.name = name;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setSwid(String swid) {
            this.swid = swid;
        }

        public void setAttributes(Map<String, Set<String>> attributes) {
            this.attributes = attributes;
        }

        public void setInputFileSwids(Set<String> inputFileSwids) {
            this.inputFileSwids = inputFileSwids;
        }

        public WorkflowRun build() {
            String key = swid;
            WorkflowRun r = cache.get(key);
            if (r == null) {
                r = new WorkflowRun(this);
                cache.put(key, r);
            }
            return r;
        }
    }

    public static void clearCache() {
        cache.clear();
    }

}
