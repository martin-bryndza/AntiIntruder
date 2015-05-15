package eu.bato.anyoffice.serviceapi.dto;

import java.util.Objects;
import java.util.Set;

/**
 * DTO prepared for use with as a state of a resource.
 * @author Bato
 */
public class StateDto {

    private long id;
    private String name;
    private long maxDuration;
    private long minDuration;
    private long defaultSuccessorId;
    private Set<Long> successorsIds;

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public long getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(long minDuration) {
        this.minDuration = minDuration;
    }

    public long getDefaultSuccessorId() {
        return defaultSuccessorId;
    }

    public void setDefaultSuccessorId(Long defaultSuccessorId) {
        this.defaultSuccessorId = defaultSuccessorId;
    }

    public Set<Long> getSuccessorsIds() {
        return successorsIds;
    }

    public void setSuccessorsIds(Set<Long> successorsIds) {
        this.successorsIds = successorsIds;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StateDto other = (StateDto) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StateDto{" + "id=" + id + ", name=" + name + ", maxDuration=" + maxDuration + ", minDuration=" + minDuration + '}';
    }

}
