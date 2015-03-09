package eu.bato.anyoffice.backend.dao;

import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import java.util.Optional;
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
    
    Person updateState(String username, PersonState personState);
    
}
