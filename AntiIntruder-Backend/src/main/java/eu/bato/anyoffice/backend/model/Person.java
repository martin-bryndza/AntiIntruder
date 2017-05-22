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

import eu.bato.anyoffice.serviceapi.dto.PersonRole;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import java.io.Serializable;
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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public class Person implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(columnDefinition = "VARCHAR(100)", nullable = false, unique = true)
    private String username;
    @Column(columnDefinition = "VARCHAR(250)", nullable = false)
    private String password;
    @Column(columnDefinition = "VARCHAR(150)", nullable = false)
    private String displayName;    
    @Column(columnDefinition = "VARCHAR(250)", nullable = true)
    private String description;
    @Column(columnDefinition = "VARCHAR(125)", nullable = true)
    private String location;
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private PersonState state = PersonState.UNKNOWN; 
    @Column(nullable = false, name = "LAST_STATE_CHANGE")
    private Date lastStateChange;
    @Enumerated(EnumType.STRING)
    private PersonRole role;
    @Column(nullable = false, name = "DND_START")
    private Date dndStart;
    @Column(nullable = false, name = "DND_END")
    private Date dndEnd;
    @Column(nullable = true, name = "AWAY_START")
    private Date awayStart;
    @Column(nullable = true, name = "LAST_PING")
    private Date lastPing;
    
//    @ManyToAny(fetch = FetchType.LAZY, metaColumn = @Column(name = "ENTITY_TYPE"))
//    @AnyMetaDef(
//            idType = "long",
//            metaType = "string",
//            metaValues = {
//                @MetaValue(value = "P", targetEntity = Person.class)})
//    @Cascade(CascadeType.ALL)
//    @JoinTable(name = "INTERACTION", joinColumns = @JoinColumn(name = "person_id"), inverseJoinColumns = @JoinColumn(name = "entity_id"))
//    private List<Entity> outgoingInteractionRequests;
//    
//    @ManyToAny(fetch = FetchType.LAZY, metaColumn = @Column(name = "ENTITY_TYPE"))
//    @AnyMetaDef(
//            idType = "long",
//            metaType = "string",
//            metaValues = {
//                @MetaValue(value = "P", targetEntity = Person.class)})
//    @Cascade(CascadeType.ALL)
//    @JoinTable(name = "INTERACTION", joinColumns = @JoinColumn(name = "entity_id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
//    private List<Person> incomingInteractionRequests;
    
    @Column(columnDefinition = "VARCHAR(100)", nullable = true)
    private String hipChatToken;
    @Column(columnDefinition = "VARCHAR(100)", nullable = true)
    private String hipChatEmail;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
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

    public Date getLastPing() {
        return lastPing;
    }

    public void setLastPing(Date lastPing) {
        this.lastPing = lastPing;
    }

    public PersonRole getRole() {
        return role;
    }

    public void setRole(PersonRole role) {
        this.role = role;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
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
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public Date getLastStateChange() {
        return lastStateChange;
    }

    protected void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    public PersonState getState() {
        return state;
    }

    public void setState(PersonState state) {
        final Long currentMillis = Calendar.getInstance().getTimeInMillis();
        this.setLastStateChange(new Date(currentMillis));
        this.state = state;
    }

    public Date getDndStart() {
        return dndStart;
    }

    public void setDndStart(Date dndStart) {
        this.dndStart = dndStart == null ? this.dndStart : dndStart;
    }

    public Date getDndEnd() {
        return dndEnd;
    }

    public void setDndEnd(Date dndEnd) {
        this.dndEnd = dndEnd == null ? this.dndEnd : dndEnd;
    }

    public Optional<Date> getAwayStart() {
        return Optional.ofNullable(awayStart);
    }

    public void setAwayStart(Optional<Date> awayStart) {
        this.awayStart = awayStart.orElse(null);
    }

//    public List<Entity> getOutgoingInteractionRequests() {
//        return outgoingInteractionRequests;
//    }
//
//    public void setOutgoingInteractionRequests(List<Entity> outgoingInteractionRequests) {
//        this.outgoingInteractionRequests = outgoingInteractionRequests;
//    }
//
//    public void addOutgoingInteractionRequest(Entity person) {
//        if (this.outgoingInteractionRequests == null) {
//            this.outgoingInteractionRequests = new LinkedList<>();
//        }
//        this.outgoingInteractionRequests.add(person);
//    }
//
//    public void removeOutgoingInteractionRequest(Entity person) {
//        if (this.outgoingInteractionRequests != null) {
//            this.outgoingInteractionRequests.remove(person);
//        }
//    }
//
//    public void removeAllOutgoingInteractionRequests() {
//        this.outgoingInteractionRequests.clear();
//    }  
//    
//    public List<Person> getIncomingInteractionRequests() {
//        return incomingInteractionRequests;
//    }
//
//    public void setIncomingInteractionRequests(List<Person> persons) {
//        this.incomingInteractionRequests = persons;
//    }
//
//    public void addIncomingInteractionRequests(Person persons) {
//        if (this.incomingInteractionRequests == null) {
//            this.incomingInteractionRequests = new LinkedList<>();
//        }
//        this.incomingInteractionRequests.add(persons);
//    }
//
//    public void removeIncomingInteractionRequests(Person interactingPerson) {
//        if (this.incomingInteractionRequests != null) {
//            this.incomingInteractionRequests.remove(interactingPerson);
//        }
//    }
//
//    public void removeAllIncomingInteractionRequests() {
//        this.incomingInteractionRequests.clear();
//    }

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
        return "Person{" + "username=" + username + ", password=" + password + ", state=" + state + ", role=" + role + ", dndStart=" + dndStart + ", dndEnd=" + dndEnd + ", awayStart=" + awayStart + ", lastPing=" + lastPing + '}' + super.toString();
    }

}
