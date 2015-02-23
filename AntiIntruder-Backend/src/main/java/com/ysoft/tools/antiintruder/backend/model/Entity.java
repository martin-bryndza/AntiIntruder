/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author Bato
 */
@javax.persistence.Entity
public class Entity implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(columnDefinition = "VARCHAR(150)", nullable = false)
    private String displayName;
    @Column(columnDefinition = "VARCHAR(250)", nullable = false)
    private String description;
    
    @ManyToOne
    private State state;
    
    @Column(nullable = false, name = "LAST_STATE_CHANGE")
    private Date lastStateChange;
    @Column(nullable = true, name = "NEXT_STATE_CHANGE")
    private Date nextPossibleStateChange;
    @Column(nullable = true, name = "STATE_EXPIRATION")
    private Date stateExpiration;

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

    public State getState() {
        return state;
    }

    public Long getId() {
        return id;
    }

    /**
     * Sets new state. If the state is different than the previously set state, 
     * fields lastStateChange, nextPossibleStateChange and stateExpiration will 
     * automatically be updated.
     * @param state 
     */
    public void setState(State state) {
        if (state!=null && !state.equals(this.state)){
            final Long currentMillis = Calendar.getInstance().getTimeInMillis();
            setLastStateChange(new Date(currentMillis));
            setNextPossibleStateChange(new Date(currentMillis + state.getMinDuration()));
            setStateExpiration((Optional<Date>) (state.getMaxDuration()==0?Optional.empty():Optional.of(new Date(currentMillis + state.getMaxDuration()))));
        }
        this.state = state;
    }

    public Date getLastStateChange() {
        return lastStateChange;
    }

    private void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    public Date getNextPossibleStateChange() {
        return nextPossibleStateChange;
    }

    private void setNextPossibleStateChange(Date nextPossibleStateChange) {
        this.nextPossibleStateChange = nextPossibleStateChange;
    }

    /**
     * 
     * @return Time when the current state expires or null, if the state never expires
     */
    public Optional<Date> getStateExpiration() {
        return Optional.ofNullable(stateExpiration);
    }

    private void setStateExpiration(Optional<Date> stateExpiration) {
        this.stateExpiration = stateExpiration.orElse(null);
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
        return "Entity{" + "id=" + id + ", displayName=" + displayName + ", description=" + description + ", state=" + state + '}';
    }

}
