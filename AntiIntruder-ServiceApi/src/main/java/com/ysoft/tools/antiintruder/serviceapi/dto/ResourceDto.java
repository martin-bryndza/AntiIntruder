package com.ysoft.tools.antiintruder.serviceapi.dto;

import java.util.Objects;


public class ResourceDto extends EntityDto {
    
    private Long stateId;

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
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
