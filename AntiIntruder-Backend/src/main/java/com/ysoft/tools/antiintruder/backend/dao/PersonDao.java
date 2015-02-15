package com.ysoft.tools.antiintruder.backend.dao;

import com.ysoft.tools.antiintruder.backend.model.Entity;
import com.ysoft.tools.antiintruder.backend.model.Person;
import java.util.Optional;

/**
 *
 * @author Bato
 */
public interface PersonDao extends Dao<Person, Entity>{
    
    /**
     * Deletes the Person entity and the referenced Entity entity.
     * @param id 
     * @throws IllegalArgumentException if any of the objects is not an entity or is a detached entity
     */
    @Override
    void delete(Entity id);
    
    /**
     * Deletes the Person entity and the referenced Entity entity.
     * @param entityId the id of the referenced Entity entity
     * @throws IllegalArgumentException if any of the objects is not an entity
     * or is a detached entity
     */
    void delete(Long entityId);
    
    /**
     * Finds Person entity according to id of the referenced Entity entity
     * @param entityId the id of the referenced Entity entity
     * @return corresponding Person entity
     */
    Optional<Person> findOne(Long entityId);
    
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
    
    Person login(String username, String password);
    
}
