/* 
 * Copyright (c) 2015, Martin Bryndza
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package eu.bato.anyoffice.backend.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity prepared to represent any other entity than person (e.g. printers,
 * public notebooks, books etc.)
 *
 * @author Bato
 * @deprecated Kept only for historical reasons.
 */
@Deprecated
@javax.persistence.Entity
@Table(name = "Resource")
public class Resource extends Entity {

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
