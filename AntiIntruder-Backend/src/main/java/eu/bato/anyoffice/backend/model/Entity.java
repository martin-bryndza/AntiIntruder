/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.backend.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author Bato
 */
@MappedSuperclass
public abstract class Entity implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(columnDefinition = "VARCHAR(150)", nullable = false)
    private String displayName;
    @Column(columnDefinition = "VARCHAR(250)", nullable = false)
    private String description;    
    @Column(nullable = false, name = "LAST_STATE_CHANGE")
    private Date lastStateChange;

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

    public Long getId() {
        return id;
    }

    public Date getLastStateChange() {
        return lastStateChange;
    }

    protected void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.id);
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
        final Entity other = (Entity) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entity{" + "id=" + id + ", displayName=" + displayName + ", description=" + description + "}";
    }

}
