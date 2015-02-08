/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.dao.impl;

import com.ysoft.tools.antiintruder.backend.dao.EntityDao;
import com.ysoft.tools.antiintruder.backend.model.Entitty;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Bato
 */
@Repository
public class EntityDaoImpl implements EntityDao{
    
    final static Logger log = LoggerFactory.getLogger(EntityDaoImpl.class);
    // injected from Spring
    @PersistenceContext(name = "entityManagerFactory")
    private EntityManager em;

    @Override
    public Long create (Entitty entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Invalid entity (Entity): " + entity);
        }
        log.info("Creating " + entity.toString());
        Entitty modelEntity = em.merge(entity);
        Long id = modelEntity.getId();
        log.debug("Created " + modelEntity.toString() + ". Assigned ID: " + id);
        return id; 
    }

    @Override
    public Entitty get(Long pk) {
        if (pk == null) {
            throw new IllegalArgumentException("Invalid id: " + pk);
        } else if (em.createQuery("SELECT e.id FROM Entities e WHERE e.id = :pk", Long.class).setParameter("pk", pk).getResultList().size() < 1) {
            throw new IllegalArgumentException("Invalid id: nonexistent");
        }
        return em.createQuery("SELECT e FROM Entities e WHERE e.id = :pk", Entitty.class).setParameter("pk", pk).getSingleResult(); 
    }

    @Override
    public void update (Entitty entity) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void remove(Long pk) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
}
