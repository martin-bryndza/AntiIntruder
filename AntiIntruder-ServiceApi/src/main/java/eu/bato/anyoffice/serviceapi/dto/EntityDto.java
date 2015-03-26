/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.serviceapi.dto;

import java.util.Objects;

/**
 *
 * @author Bato
 */
public abstract class EntityDto {
    
    private String description;
    private String displayName;
    private Long id;
    private Long lastStateChange;
    private String location;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public Long getLastStateChange() {
        return lastStateChange;
    }

    public void setLastStateChange(Long lastStateChange) {
        this.lastStateChange = lastStateChange;
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
