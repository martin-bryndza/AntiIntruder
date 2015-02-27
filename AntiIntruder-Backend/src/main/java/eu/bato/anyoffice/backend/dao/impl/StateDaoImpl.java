/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.backend.dao.impl;

import eu.bato.anyoffice.backend.dao.StateDao;
import eu.bato.anyoffice.backend.model.State;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Bato
 */
@Repository
public class StateDaoImpl implements StateDao{

    final static Logger log = LoggerFactory.getLogger(StateDaoImpl.class);
    // injected from Spring
    @PersistenceContext(name = "entityManagerFactory")
    private EntityManager em;
    
    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid record: null or with no id.");
        }
        State state = em.find(State.class, id);
        if (state == null) {
            log.error("State with id " + id + " is not in DB");
        }
        em.remove(state);
    }

    @Override
    public List<State> findAll() {
        
        return em.createQuery("SELECT tbl FROM State tbl", State.class).getResultList();
    }

    @Override
    public Optional<State> findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        try{
            return Optional.ofNullable(em.createQuery("SELECT e FROM State e WHERE e.id = :pk", State.class).setParameter("pk", id).getSingleResult());
        } catch (NoResultException e){
            throw new IllegalArgumentException("Invalid id: nonexistent");
        }
    }

    @Override
    public State save(State entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Invalid entity (State): " + entity);
        }
        log.info("Creating " + entity.toString());
        State modelEntity = em.merge(entity);
        Long id = modelEntity.getId();
        log.debug("Created " + modelEntity.toString() + ". Assigned ID: " + id);
        return modelEntity;
    }
    
}
