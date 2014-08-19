package ca.on.oicr.pde.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class WorkflowRun implements Accessionable {

    private static final Map<String, WorkflowRun> cache = new ConcurrentHashMap<String, WorkflowRun>();

    private final String name;
    private final String status;
    private final String swid;

    private WorkflowRun(Builder b) {
        name = b.name;
        status = b.status;
        swid = b.swid;
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

        public void setName(String name) {
            this.name = name;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setSwid(String swid) {
            this.swid = swid;
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

}
