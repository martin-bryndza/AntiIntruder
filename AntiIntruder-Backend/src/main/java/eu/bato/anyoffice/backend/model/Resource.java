/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.backend.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author Bato
 */
@javax.persistence.Entity
@Table(name = "Resource")
public class Resource extends Entity{
    
    @ManyToOne
    private State state;
    @Column(nullable = true, name = "NEXT_STATE_CHANGE")
    private Date nextPossibleStateChange;
    @Column(nullable = true, name = "STATE_EXPIRATION")
    private Date stateExpiration;
    
    public State getState() {
        return state;
    }
    
    /**
     * Sets new state. If the state is different than the previously set state,
     * fields lastStateChange, nextPossibleStateChange and stateExpiration will
     * automatically be updated.
     *
     * @param state
     */
    public void setState(State state) {
        if (state != null && !state.equals(this.state)) {
            final Long currentMillis = Calendar.getInstance().getTimeInMillis();
            setLastStateChange(new Date(currentMillis));
            setNextPossibleStateChange(new Date(currentMillis + state.getMinDuration()));
            setStateExpiration((Optional<Date>) (state.getMaxDuration() == 0 ? Optional.empty() : Optional.of(new Date(currentMillis + state.getMaxDuration()))));
        }
        this.state = state;
    }
    
        public Date getNextPossibleStateChange() {
        return nextPossibleStateChange;
    }

    protected void setNextPossibleStateChange(Date nextPossibleStateChange) {
        this.nextPossibleStateChange = nextPossibleStateChange;
    }

    /**
     *
     * @return Time when the current state expires or null, if the state never
     * expires
     */
    public Optional<Date> getStateExpiration() {
        return Optional.ofNullable(stateExpiration);
    }

    protected void setStateExpiration(Optional<Date> stateExpiration) {
        this.stateExpiration = stateExpiration.orElse(null);
    }
}
