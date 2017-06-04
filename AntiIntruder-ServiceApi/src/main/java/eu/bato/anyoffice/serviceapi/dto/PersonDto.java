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
package eu.bato.anyoffice.serviceapi.dto;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Bato
 */
public class PersonDto {

    private String description;
    private String displayName;
    private Long id;
    private Long lastStateChange;
    private String location;
    private PersonState state;
    private String username;
    private PersonRole role;
    private Long dndStart;
    private Long dndEnd;
    private Optional<Long> awayStart = Optional.empty();
    private Optional<Long> lastPing = Optional.empty();
    private String hipChatToken;
    private String hipChatEmail;
    
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

    public String getHipChatToken() {
        return hipChatToken;
    }

    public void setHipChatToken(String hipChatToken) {
        this.hipChatToken = hipChatToken;
    }

    public String getHipChatEmail() {
        return hipChatEmail;
    }

    public void setHipChatEmail(String hipChatEmail) {
        this.hipChatEmail = hipChatEmail;
    }

    public PersonState getState() {
        return state;
    }

    public void setState(PersonState state) {
        this.state = state;
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

    public Long getDndStart() {
        return dndStart;
    }

    public void setDndStart(Long dndStart) {
        this.dndStart = dndStart;
    }

    public Long getDndEnd() {
        return dndEnd;
    }

    public void setDndEnd(Long dndEnd) {
        this.dndEnd = dndEnd;
    }

    public Optional<Long> getAwayStart() {
        return awayStart;
    }

    public void setAwayStart(Optional<Long> awayStart) {
        this.awayStart = awayStart;
    }

    public Optional<Long> getLastPing() {
        return lastPing;
    }

    public void setLastPing(Optional<Long> lastPing) {
        this.lastPing = lastPing;
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
        final PersonDto other = (PersonDto) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "PersonDto{" + "description=" + description + ", displayName=" + displayName + ", id=" + id + ", username=" + username + ", role=" + role + '}';
    }

}
