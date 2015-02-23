/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.serviceapi.dto;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Bato
 */
public class PersonDto {
    
    private String description;
    private String displayName;
    private Long stateId;
    private Long entityId;
    private String username;
    private PersonRole role;
    private Date lastStateChange;
    private Date nextPossibleStateChange;
    private Optional<Date> stateExpiration;

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PersonRole getRole() {
        return role;
    }

    public void setRole(PersonRole role) {
        this.role = role;
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
        hash = 13 * hash + Objects.hashCode(this.entityId);
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
        final PersonDto other = (PersonDto) obj;
        if (!Objects.equals(this.entityId, other.entityId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PersonDto{" + "description=" + description + ", displayName=" + displayName + ", stateId=" + stateId + ", entityId=" + entityId + ", name=" + username + ", role=" + role + '}';
    }  
    
}
