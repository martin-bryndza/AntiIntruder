package eu.bato.anyoffice.serviceapi.dto;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;


public class ResourceDto extends EntityDto {
    
    private Long stateId;
    private Date nextPossibleStateChange;
    private Optional<Date> stateExpiration;

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }
    
    public Date getNextPossibleStateChange() {
        return nextPossibleStateChange;
    }

    public void setNextPossibleStateChange(Date nextPossibleStateChange) {
        this.nextPossibleStateChange = nextPossibleStateChange;
    }

    /**
     *
     * @return Time when the current state expires or null, if the state never
     * expires
     */
    public Date getStateExpiration() {
        return stateExpiration.orElse(null);
    }

    public void setStateExpiration(Optional<Date> stateExpiration) {
        this.stateExpiration = stateExpiration;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(super.getId());
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
        final EntityDto other = (EntityDto) obj;
        return Objects.equals(super.getId(), other.getId());
    }

    @Override
    public String toString() {
        return "ResourceDto{" + "stateId=" + stateId + '}';
    }
    
}
