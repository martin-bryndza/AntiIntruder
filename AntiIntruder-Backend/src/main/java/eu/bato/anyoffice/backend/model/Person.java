/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.backend.model;

import eu.bato.anyoffice.serviceapi.dto.PersonRole;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.MetaValue;

/**
 *
 * @author Bato
 */
@javax.persistence.Entity
@Table(name = "Person")
public class Person extends Entity{
    
    @Column(columnDefinition = "VARCHAR(100)", nullable = false, unique = true)
    private String username;
    @Column(columnDefinition = "VARCHAR(250)", nullable = false)
    private String password;
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private PersonState state = PersonState.UNKNOWN;
    @Enumerated(EnumType.STRING)
    private PersonRole role;
    @Column(nullable = false, name = "DND_START")
    private Date dndStart = new Date();
    @Column(nullable = false, name = "DND_END")
    private Date dndEnd = new Date();
    @Column(nullable = true, name = "AWAY_START")
    private Date awayStart;
    @ManyToAny(fetch = FetchType.LAZY, metaColumn = @Column(name = "ENTITY_TYPE"))
    @AnyMetaDef(
            idType = "long",
            metaType = "string",
            metaValues = {
                @MetaValue(value = "P", targetEntity = Person.class),
                @MetaValue(value = "R", targetEntity = Resource.class)})
    @Cascade(CascadeType.ALL)
    @JoinTable(name = "INTERACTION", joinColumns = @JoinColumn(name = "person_id"), inverseJoinColumns = @JoinColumn(name = "entity_id"))
    private List<Entity> interactionEntities;
 
    public PersonRole getRole() {
        return role;
    }

    public void setRole(PersonRole role) {
        this.role = role;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public PersonState getState() {
        return state;
    }

    public void setState(PersonState state) {
        final Long currentMillis = Calendar.getInstance().getTimeInMillis();
        super.setLastStateChange(new Date(currentMillis));
        this.state = state;
    }

    public Date getDndStart() {
        return dndStart;
    }

    public void setDndStart(Date dndStart) {
        this.dndStart = dndStart==null?this.dndStart:dndStart;
    }

    public Date getDndEnd() {
        return dndEnd;
    }

    public void setDndEnd(Date dndEnd) {
        this.dndEnd = dndEnd==null?this.dndEnd:dndEnd;
    }

    public Optional<Date> getAwayStart() {
        return Optional.ofNullable(awayStart);
    }

    public void setAwayStart(Optional<Date> awayStart) {
        this.awayStart = awayStart.orElse(null);
    }
    
    public List<Entity> getInteractionEntities() {
        return interactionEntities;
    }

    public void setInteractionEntities(List<Entity> interactionEntities) {
        this.interactionEntities = interactionEntities;
    }

    public void addInteractionEntity(Entity interactionEntity) {
        if (this.interactionEntities == null) {
            this.interactionEntities = new LinkedList<>();
        }
        this.interactionEntities.add(interactionEntity);
    }

    public void removeInteractionEntity(Entity interactionEntity) {
        if (this.interactionEntities != null) {
            this.interactionEntities.remove(interactionEntity);
        }
    }
    
    public void removeAllInteractionEntities() {
        this.interactionEntities.clear();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.username);
        hash = 47 * hash + Objects.hashCode(this.password);
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
        final Person other = (Person) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        return Objects.equals(this.password, other.password);
    }

    @Override
    public String toString() {
        return "Person{username=" + username + ", password=" + password + ", role=" + role + '}';
    }

}
