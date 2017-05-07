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

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
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
public abstract class Entity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(columnDefinition = "VARCHAR(150)", nullable = false)
    private String displayName;
    @Column(columnDefinition = "VARCHAR(250)", nullable = true)
    private String description;
    @Column(columnDefinition = "VARCHAR(125)", nullable = true)
    private String location;
    @Column(nullable = false, name = "LAST_STATE_CHANGE")
    private Date lastStateChange;
//    @ManyToAny(fetch = FetchType.LAZY, metaColumn = @Column(name = "ENTITY_TYPE"))
//    @AnyMetaDef(
//            idType = "long",
//            metaType = "string",
//            metaValues = {
//                @MetaValue(value = "P", targetEntity = Person.class),
//                @MetaValue(value = "R", targetEntity = Resource.class)})
//    @Cascade(CascadeType.ALL)
//    @JoinTable(name = "INTERACTION", joinColumns = @JoinColumn(name = "entity_id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
//    private List<Person> interactingPersons;

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

//    public List<Person> getInteractingPersons() {
//        return interactingPersons;
//    }
//
//    public void setInteractingPersons(List<Person> interactingPersons) {
//        this.interactingPersons = interactingPersons;
//    }
//
//    public void addInteractingPersons(Person interactingPerson) {
//        if (this.interactingPersons == null) {
//            this.interactingPersons = new LinkedList<>();
//        }
//        this.interactingPersons.add(interactingPerson);
//    }
//
//    public void removeInteractingPerson(Person interactingPerson) {
//        if (this.interactingPersons != null) {
//            this.interactingPersons.remove(interactingPerson);
//        }
//    }
//
//    public void removeAllInteractingPersons() {
//        this.interactingPersons.clear();
//    }

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
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Entity{" + "id=" + id + ", displayName=" + displayName + ", description=" + description + ", location=" + location + '}';
    }

}
