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
package eu.bato.anyoffice.backend.dao;

import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.persistence.NoResultException;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bato
 */
@Transactional(noRollbackFor = NoResultException.class)
public interface PersonDao extends Dao<Person, Long> {

    /**
     * Deletes the Person entity and the referenced Entity entity.
     *
     * @param id the id of the referenced Entity entity
     * @throws IllegalArgumentException if any of the objects is not an entity
     * or is a detached entity
     */
    @Override
    void delete(Long id);

    /**
     * Finds Person entity according to id of the referenced Entity entity
     *
     * @param id the id of the referenced Entity entity
     * @return corresponding Person entity
     */
    @Override
    Person findOne(Long id) throws IllegalArgumentException, NoResultException;

    /**
     * Finds Person entity according to its username
     *
     * @param username the username
     * @return corresponding Person entity
     * @throws IllegalArgumentException if the username is null
     * @throws NoResultException if user with such username does not exist
     */
    Person findOneByUsername(String username) throws IllegalArgumentException, NoResultException;

    /**
     * Saves the Person entity and the referenced Entity entity into DB. If any
     * of PKs is null, such entity is created and assigned a PK.
     *
     * @param entity the Person entity to save
     * @return the merged Person entity
     */
    @Override
    Person save(Person entity);

    Person updateState(Long id, PersonState personState);

    Person updateState(String username, PersonState personState);

    /**
     *
     * @param username
     * @param dndStart the start of DND to set or empty to keep unchanged
     * @param dndEnd the end of DND to set or empty to keep unchanged
     * @param awayStart the start of AWAY to set or empty to delete the value
     */
    void updateTimers(String username, Optional<Date> dndStart, Optional<Date> dndEnd, Optional<Date> awayStart);

    /**
     * Adds an interaction of this person with an entity.
     *
     * @param username
     * @param id
     */
    void addInteractionEntity(String username, Long id);

    /**
     * Removes interaction with entity that this person interacts with
     *
     * @param username
     * @param id
     */
    void removeInteractionEntity(String username, Long id);

    /**
     * Removes interactions with entities that this person interacts with
     *
     * @param username
     */
    void removeAllInteractionEntities(String username);

    /**
     * Removes interactions with selected entities (IDs) that this person
     * (username) wants to interact with
     *
     * @param username
     * @param ids
     */
    void removeInteractionEntities(String username, Collection<Long> ids);

    /**
     * Returns all persons that this person (username) wants to interact with
     *
     * @param username
     * @return
     */
    List<Person> getInteractionPersons(String username);

    /**
     * Returns all persons that want to interact with this Person (username)
     *
     * @param username
     * @return
     */
    List<Person> getInteractingPersons(String username);

    /**
     * Removes interactions with all persons that interact with this Entity
     *
     * @param username
     */
    void removeAllInteractingPersons(String username);

    boolean isTaken(String username);

    void setLocation(String username, String location);

    String getLocation(String username);
    
    void setLastPing(String username, Date when);
    
    Date getLastPing(String username);

}
