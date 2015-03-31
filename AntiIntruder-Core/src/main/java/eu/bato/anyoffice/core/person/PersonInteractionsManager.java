/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.bato.anyoffice.core.person;

import eu.bato.anyoffice.serviceapi.dto.InteractionPersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bryndza
 */
@Service
public class PersonInteractionsManager {
    
    private final static Logger log = LoggerFactory.getLogger(PersonInteractionsManager.class);

    @Autowired
    private PersonService personService;
    
    private RemovingSet<Interaction> partiallySeenInteractions = new RemovingSet<Interaction>();
       
    /**
     * Returns all persons that want to interact with this person (username)
     *
     * @param username
     * @return
     */
    public Set<InteractionPersonDto> getInteractingPersons(String username){
        Set<InteractionPersonDto> interactingPersons = new HashSet<>(personService.getInteractingPersons(username));
        partiallySeenInteractions.forEach((k) -> {
            if (k.getInteractionPersonIdentificator().getUsername().equals(username)){
                interactingPersons.add(k.getInteractingPersonIdentificator().getInteractionPerson());
            }
        });
        return interactingPersons;
    }
    
    /**
     * Returns all persons, that this person (username) wants to interact with and are in the requested state.
     * The interaction request is canceled automatically after performing this operation and thus will be reported only once.
     * @param username
     * @param state
     * @return 
     */
    public Set<InteractionPersonDto> getInteractionPersons(String username, PersonState state){
        Set<InteractionPersonDto> interactionPersons = new HashSet<>(personService.getInteractionPersons(username, state));
        personService.removeInteractionEntities(username, interactionPersons.stream().map(p -> p.getId()).collect(Collectors.toSet()));
        partiallySeenInteractions.forEach((k) -> {
            if (k.getInteractingPersonIdentificator().getUsername().equals(username)){
                interactionPersons.add(k.getInteractionPersonIdentificator().getInteractionPerson());
            }
        });
        return interactionPersons;
    }
    
    /**
     * Marks the interaction entities (ids) as have been seen by the person (username).
     * These are the entities, that the person wants to interact with and he has 
     * been notified about their availability.
     * The interaction is removed from DB. If the interaction has not been seen 
     * by the interaction Person, it is kept in memory and removed afterwards by 
     * the seenInteractingEntities method.
     * @param username interacting Person
     * @param ids list of interaction entities
     */
    public void seenInteractionEntities(String username, Collection<Long> ids){
        personService.removeInteractionEntities(username, ids);
        ids.forEach((id -> partiallySeenInteractions.add(new Interaction(new PersonIdentificator(username), new PersonIdentificator(id)))));
    }
    
    /**
     * Marks the interacting persons (ids) as have been seen by the person (username) that they want to interact with
     * If the interaction is marked as seen by interaction Person or removed 
     * from the memory if it has already been marked as seen by the interacting Person.
     * @param username
     * @param ids 
     */
    public void seenInteractingEntities(String username, Collection<Long> ids){
        ids.forEach((id -> partiallySeenInteractions.add(new Interaction(new PersonIdentificator(id), new PersonIdentificator(username)))));
    }
    
    private class PersonIdentificator {
        
        private final Long id;
        private final String username;

        public PersonIdentificator(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        public PersonIdentificator(Long id) {
            this.id = id;
            this.username = personService.getUsername(id);
        }

        public PersonIdentificator(String username) {
            this.username = username;
            this.id = personService.getId(username);
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }
        
        public InteractionPersonDto getInteractionPerson(){
            return personService.findOneByUsernameAsInteractionPerson(username);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.id);
            hash = 97 * hash + Objects.hashCode(this.username);
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
            final PersonIdentificator other = (PersonIdentificator) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return Objects.equals(this.username, other.username);
        }
        
    }
    
    private class Interaction {
        
        //Interacting = who wants to interact
        private final PersonIdentificator interactingPersonIdentificator;
        //Interaction = who is interacted with
        private final PersonIdentificator interactionPersonIdentificator;

        public Interaction(PersonIdentificator interactingPerson, PersonIdentificator interactionPerson) {
            this.interactingPersonIdentificator = interactingPerson;
            this.interactionPersonIdentificator = interactionPerson;
        }

        public PersonIdentificator getInteractingPersonIdentificator() {
            return interactingPersonIdentificator;
        }

        public PersonIdentificator getInteractionPersonIdentificator() {
            return interactionPersonIdentificator;
        }
        
    }
    
    private class RemovingSet<K> extends HashSet<K>{

        @Override
        public boolean add(K e) {
            boolean isFirstTime = super.add(e);
            if (!isFirstTime) {
                super.remove(e);
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            c.stream().forEach((k) -> {
                add(k);
            });
            return true;
        }
        
    }
    
}
