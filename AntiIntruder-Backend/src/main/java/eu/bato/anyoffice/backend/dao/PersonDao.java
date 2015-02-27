package eu.bato.anyoffice.backend.dao;

import eu.bato.anyoffice.backend.model.Entity;
import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import java.util.Optional;

/**
 *
 * @author Bato
 */
public interface PersonDao extends Dao<Person, Long>{
    
        
    /**
     * Deletes the Person entity and the referenced Entity entity.
     * @param id the id of the referenced Entity entity
     * @throws IllegalArgumentException if any of the objects is not an entity
     * or is a detached entity
     */
    void delete(Long id);
    
    /**
     * Finds Person entity according to id of the referenced Entity entity
     * @param id the id of the referenced Entity entity
     * @return corresponding Person entity
     */
    Optional<Person> findOne(Long id);
    
    /**
     * Finds Person entity according to its username
     * @param username the username
     * @return corresponding Person entity
     */
    Optional<Person> findOneByUsername(String username);

    /**
     * Saves the Person entity and the referenced Entity entity into DB. If any of PKs is null, such entity is created and assigned a PK. 
     * @param entity the Person entity to save
     * @return the merged Person entity
     */
    @Override
    Person save(Person entity);
    
    Person updateState(Long id, PersonState personState);
    
}
