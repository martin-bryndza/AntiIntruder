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

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Entity prepared for use with as a state of a resource.
 *
 * @author Bato
 */
@Entity
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String name;

    private long maxDuration;
    private long minDuration;

    @ManyToOne(optional = true)
    private State defaultSuccessor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return Maximum state duration in milliseconds or 0 for not specified
     */
    public long getMaxDuration() {
        return maxDuration;
    }

    /**
     *
     * @param maxDuration Maximum state duration in milliseconds or 0 for not
     * specified
     */
    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    /**
     *
     * @return Minimum state duration in milliseconds or 0 for not specified
     */
    public long getMinDuration() {
        return minDuration;
    }

    /**
     *
     * @param minDuration Minimum state duration in milliseconds or 0 for not
     * specified
     */
    public void setMinDuration(long minDuration) {
        this.minDuration = minDuration;
    }

    /**
     * Gets the default successor of this state. If the given successor is null,
     * this object will returned as its successor.
     *
     * @return Default successor state of this state.
     */
    public State getDefaultSuccessor() {
        return defaultSuccessor == null ? this : defaultSuccessor;
    }

    public void setDefaultSuccessor(State defaultSuccessor) {
        this.defaultSuccessor = defaultSuccessor;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final State other = (State) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "State{" + "id=" + id + ", name=" + name + ", maxDuration=" + maxDuration + ", minDuration=" + minDuration + '}';
    }

}
