package eu.bato.anyoffice.backend.dao;

import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import java.util.Date;
import java.util.Optional;
import javax.persistence.NoResultException;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bato
 */
@Transactional
public interface PersonDao extends Dao<Person, Long>{
    
        
    /**
     * Deletes the Person entity and the referenced Entity entity.
     * @param id the id of the referenced Entity entity
     * @throws IllegalArgumentException if any of the objects is not an entity
     * or is a detached entity
     */
    @Override
    void delete(Long id);
    
    /**
     * Finds Person entity according to id of the referenced Entity entity
     * @param id the id of the referenced Entity entity
     * @return corresponding Person entity
     */
    @Override
    Person findOne(Long id) throws IllegalArgumentException, NoResultException;
    
    /**
     * Finds Person entity according to its username
     * @param username the username
     * @return corresponding Person entity
     * @throws IllegalArgumentException if the username is null
     * @throws NoResultException if user with such username does not exist
     */
    Person findOneByUsername(String username) throws IllegalArgumentException, NoResultException;

    /**
     * Saves the Person entity and the referenced Entity entity into DB. If any of PKs is null, such entity is created and assigned a PK. 
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
    
    void addInteractionPerson(Long id, Long interactionPersonId);
    
    void removeInteractionPerson(Long id, Long interactionPersonId);
    
    boolean isTaken(String username);
    
}
