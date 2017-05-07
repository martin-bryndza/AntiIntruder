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
package eu.bato.anyoffice.core.person;

import eu.bato.anyoffice.serviceapi.dto.ConsultationDto;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages interactions of persons and entities.
 *
 * @author bryndza
 */
@Service
public class PersonInteractionsManager {

    private final static Logger log = LoggerFactory.getLogger(PersonInteractionsManager.class);

    @Autowired
    private PersonService personService;

    /**
     * Returns all persons that this person (username) wants to interact with and are in the requested state.
     *
     * @param username
     * @param state
     * @return
     */
    public Set<ConsultationDto> getAvailableConsultations(String username, PersonState state) {
        return null;
    }

    /**
     * Returns all persons, that want to interact with this person (username).
     *
     * @param username
     * @return
     */
    public Set<ConsultationDto> getRequestedConsultations(String username) {
        return null;
    }
    
    /**
     * Cancels the consultation.
     * @param id 
     */
    public void cancelConsultation(Long id){
        
    }
    
    /**
     * Marks consultation as resolved. The username identifies the party, that marked the consultation as resolved.
     * @param id
     * @param username 
     */
    public void resolveConsultation(Long id, String username){
        
    }

    

//    private class PersonIdentificator {
//
//        private final Long id;
//        private final String username;
//
//        public PersonIdentificator(Long id, String username) {
//            this.id = id;
//            this.username = username;
//        }
//
//        public PersonIdentificator(Long id) {
//            this.id = id;
//            this.username = personService.getUsername(id);
//        }
//
//        public PersonIdentificator(String username) {
//            this.username = username;
//            this.id = personService.getId(username);
//        }
//
//        public Long getId() {
//            return id;
//        }
//
//        public String getUsername() {
//            return username;
//        }
//
//        public ConsultationDto getInteractionPerson() {
//            return personService.findOneByUsernameAsInteractionPerson(username);
//        }
//
//        @Override
//        public int hashCode() {
//            int hash = 5;
//            hash = 97 * hash + Objects.hashCode(this.id);
//            hash = 97 * hash + Objects.hashCode(this.username);
//            return hash;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == null) {
//                return false;
//            }
//            if (getClass() != obj.getClass()) {
//                return false;
//            }
//            final PersonIdentificator other = (PersonIdentificator) obj;
//            if (!Objects.equals(this.id, other.id)) {
//                return false;
//            }
//            return Objects.equals(this.username, other.username);
//        }
//
//    }

//    /**
//     * Object containing information about partially seen interactions. Two
//     * interactions are the same if they have the same entities in the same
//     * roles.
//     */
//    private class Interaction {
//
//        //Interacting = who wants to interact
//        private final PersonIdentificator interactingPersonIdentificator;
//        //Interaction = who is interacted with
//        private final PersonIdentificator interactionPersonIdentificator;
//
//        public Interaction(PersonIdentificator interactingPersonIdentificator, PersonIdentificator interactionPersonIdentificator) {
//            this.interactingPersonIdentificator = interactingPersonIdentificator;
//            this.interactionPersonIdentificator = interactionPersonIdentificator;
//        }
//
//        public PersonIdentificator getInteractingPersonIdentificator() {
//            return interactingPersonIdentificator;
//        }
//
//        public PersonIdentificator getInteractionPersonIdentificator() {
//            return interactionPersonIdentificator;
//        }
//
//        @Override
//        public int hashCode() {
//            int hash = 7;
//            hash = 89 * hash + Objects.hashCode(this.interactingPersonIdentificator);
//            hash = 89 * hash + Objects.hashCode(this.interactionPersonIdentificator);
//            return hash;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == null) {
//                return false;
//            }
//            if (getClass() != obj.getClass()) {
//                return false;
//            }
//            final Interaction other = (Interaction) obj;
//            if (!Objects.equals(this.interactingPersonIdentificator, other.interactingPersonIdentificator)) {
//                return false;
//            }
//            return Objects.equals(this.interactionPersonIdentificator, other.interactionPersonIdentificator);
//        }
//
//    }

}
