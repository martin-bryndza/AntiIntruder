/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.serviceapi.dto;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Bato
 */
public abstract class EntityDto {
    
    private String description;
    private String displayName;
    private Long id;
    private Date lastStateChange;
    private Date nextPossibleStateChange;
    private Optional<Date> stateExpiration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
  
    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Date getLastStateChange() {
        return lastStateChange;
    }

    public void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
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
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.id);
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
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EntityDto{" + "description=" + description + ", displayName=" + displayName + ", id=" + id + '}';
    }    
    
}
