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
package eu.bato.anyoffice.serviceapi.service;

import eu.bato.anyoffice.serviceapi.dto.ConsultationDto;
import eu.bato.anyoffice.serviceapi.dto.ConsultationState;
import eu.bato.anyoffice.serviceapi.dto.HipChatCredentials;
import eu.bato.anyoffice.serviceapi.dto.LoginDetailsDto;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.dto.PersonStateSwitchDto;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Bato
 */
public interface PersonService extends Service<PersonDto> {

    /**
     * Gets hashed password of the Person with username.
     *
     * @param username
     * @return Hashed password for the username or null, if such password does
     * not exist.
     */
    Optional<LoginDetailsDto> getLoginDetails(String username);

    /**
     * Set a new password to person.
     *
     * @param username
     * @param password
     */
    void setPassword(String username, String password);

    /**
     * Create new Person.
     *
     * @param person
     * @param password
     * @return Person's id if registration was successful, null otherwise
     */
    Long register(PersonDto person, String password);

    String getUsername(Long id);

    Long getId(String username);

    List<String> findAllUsernames();

    /**
     * Sets state of Person with id to personState, if possible. Otherwise
     * throws IllegalArgumentException
     *
     * @param id
     * @param personState
     */
    void setState(Long id, PersonState personState);

    /**
     * Gets the current state of Person with id.
     *
     * @param id
     * @return
     */
    PersonState getState(Long id);

    /**
     * Sets state of Person with username to personState, if possible. Otherwise
     * throws IllegalArgumentException
     *
     * @param username
     * @param personState
     */
    void setState(String username, PersonState personState);

    /**
     * Gets the current state of Person with username.
     *
     * @param username
     * @return
     */
    PersonState getState(String username);
    
    /**
     * Sets State of the Person identified by id to the State identified by
     * stateId.
     *
     * @param id Id of the Person
     * @param personState Id of the State
     */
    void updateState(Long id, PersonState personState);

    PersonDto findOneByUsername(String username);

    /**
     *
     * @param username
     * @param dndStart the start of DND or empty to keep unchanged
     * @param dndEnd the end of DND or empty to keep unchanged
     * @param awayStart the start of AWAY or empty to delete the value
     */
    void setTimers(String username, Optional<Date> dndStart, Optional<Date> dndEnd, Optional<Date> awayStart);

    boolean isPresent(String username);

//    /**
//     * Adds a request for interaction of this person (username) with an entity
//     * (id).
//     *
//     * @param username
//     * @param id
//     */
//    void addInteractionEntity(String username, Long id);
//
//    /**
//     * Removes interaction with entity (id) that this person (username) wants to
//     * interact with
//     *
//     * @param username
//     * @param id
//     */
//    void removeInteractionEntity(String username, Long id);
//
//    /**
//     * Removes all interactions with entities that this person (username) wants
//     * to interact with
//     *
//     * @param username
//     */
//    void removeAllInteractionEntities(String username);
//
//    /**
//     * Removes interactions with selected entities (IDs) that this person
//     * (username) wants to interact with
//     *
//     * @param username
//     * @param ids
//     */
//    void removeInteractionEntities(String username, Collection<Long> ids);
//
//    /**
//     * Returns persons that person (username) wants to interact with.
//     *
//     * @param username
//     * @return
//     */
//    List<InteractionPersonDto> getInteractionPersons(String username);
//
//    /**
//     * Returns persons that person (username) wants to interact with and are
//     * currently in the requested state.
//     *
//     * @param username
//     * @param state
//     * @return
//     */
//    List<InteractionPersonDto> getInteractionPersons(String username, PersonState state);
//
//    /**
//     * Returns all persons that want to interact with this person (username)
//     *
//     * @param username
//     * @return
//     */
//    List<InteractionPersonDto> getInteractingPersons(String username);
//
//    InteractionPersonDto findOneByUsernameAsInteractionPerson(String username);
//
//    /**
//     * Removes interactions with all persons that want to interact with this
//     * person (username)
//     *
//     * @param username
//     */
//    void removeAllInteractingPersons(String username);

    void setLocation(String username, String location);

    String getLocation(String username);

    void setLastPing(String username, Date lastPing);

    List<PersonStateSwitchDto> getStateSwitches(String username, Date from, Date to);

    void noteDisturbance(String username, Boolean aoUser);

    HipChatCredentials getHipChatCredentials(String username);

}
