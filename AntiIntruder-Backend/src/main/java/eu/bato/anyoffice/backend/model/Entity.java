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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
public abstract class Entity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToAny(fetch = FetchType.LAZY, metaColumn = @Column(name = "ENTITY_TYPE"))
    @AnyMetaDef(
            idType = "long",
            metaType = "string",
            metaValues = {
                @MetaValue(value = "P", targetEntity = Person.class)})
    @Cascade(CascadeType.ALL)
    @JoinTable(name = "INTERACTION", joinColumns = @JoinColumn(name = "entity_id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
    private List<Person> interactingPersons;

    public void setId(Long id) {
        this.id = id;
    }





    public Long getId() {
        return id;
    }



    public List<Person> getInteractingPersons() {
        return interactingPersons;
    }

    public void setInteractingPersons(List<Person> interactingPersons) {
        this.interactingPersons = interactingPersons;
    }

    public void addInteractingPersons(Person interactingPerson) {
        if (this.interactingPersons == null) {
            this.interactingPersons = new LinkedList<>();
        }
        this.interactingPersons.add(interactingPerson);
    }

    public void removeInteractingPerson(Person interactingPerson) {
        if (this.interactingPersons != null) {
            this.interactingPersons.remove(interactingPerson);
        }
    }

    public void removeAllInteractingPersons() {
        this.interactingPersons.clear();
    }
}
