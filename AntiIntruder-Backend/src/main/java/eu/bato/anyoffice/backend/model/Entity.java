/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.backend.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.MetaValue;

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
    @Column(columnDefinition = "VARCHAR(125)", nullable = true)
    private String location;    
    @Column(nullable = false, name = "LAST_STATE_CHANGE")
    private Date lastStateChange;
//    @ManyToAny(fetch = FetchType.LAZY, metaColumn = @Column(name = "ENTITY_TYPE"))
//    @AnyMetaDef(
//        idType = "integer",
//        metaType = "string",
//        metaValues = {
//            @MetaValue(value = "P", targetEntity = Person.class),
//            @MetaValue(value = "R", targetEntity = Resource.class)})
//    @Cascade(CascadeType.ALL)
//    @JoinTable(name = "INTERACTIONS")
//    private List<Entity> interactionEntities;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public List<Entity> getInteractionEntities() {
//        return interactionEntities;
        return new LinkedList<>();
    }

    public void setInteractionEntities(List<Entity> interactionEntities) {
//        this.interactionEntities = interactionEntities;
    }
    
    public void addInteractionEntity(Entity interactionEntity){
//        if (this.interactionEntities == null){
//            this.interactionEntities = new LinkedList<>();
//        }
//        this.interactionEntities.add(interactionEntity);
    }
    
    public void removeInteractionEntity(Entity interactionEntity) {
//        if (this.interactionEntities != null){
//            this.interactionEntities.remove(interactionEntity);
//        }
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
