/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.backend.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 *
 * @author Bato
 * @param <T> Generic type of entity
 * @param <U> Generic type of primary key of the entity
 */
@NoRepositoryBean
public interface Dao<T, U extends Serializable> extends Repository<T, U>{
    
    /**
     * Deletes a given entity.
     * @param id - id of the entity to delete.
     */
    void delete(U id);
 
    /**
     * Returns all instances of the type.
     * @return list of all instances
     */
    List<T> findAll();
 
    /**
     * Retrieves an entity by its id.
     * @param id - must not be null.
     * @return the entity with the given id or null if none found
     * @throws IllegalArgumentException - if id is null 
     */
    Optional<T> findOne(U id);
 
    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the entity instance completely.
     * @param entity - the entity to save
     * @return copy of the saved entity
     */
    T save(T entity);
    
}
