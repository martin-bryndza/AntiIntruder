/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.dao.impl;

import com.ysoft.tools.antiintruder.backend.dao.EntityDao;
import com.ysoft.tools.antiintruder.backend.model.Entitty;
import java.util.List;
import java.util.Optional;
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
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid record: null or with no id.");
        }
        Entitty entity = em.find(Entitty.class, id);
        if (entity == null) {
            log.error("Entity is not in DB");
        }
        em.remove(entity);
    }

    @Override
    public List<Entitty> findAll() {
        return em.createQuery("SELECT tbl FROM Entitty tbl", Entitty.class).getResultList();
    }

    @Override
    public Optional<Entitty> findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid id: " + id);
        } else if (em.createQuery("SELECT e.id FROM Entities e WHERE e.id = :pk", Long.class).setParameter("pk", id).getResultList().size() < 1) {
            throw new IllegalArgumentException("Invalid id: nonexistent");
        }
        return Optional.ofNullable(em.createQuery("SELECT e FROM Entities e WHERE e.id = :pk", Entitty.class).setParameter("pk", id).getSingleResult());
    }

    @Override
    public Entitty save(Entitty entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Invalid entity (Entity): " + entity);
        }
        log.info("Creating " + entity.toString());
        Entitty modelEntity = em.merge(entity);
        Long id = modelEntity.getId();
        log.debug("Created " + modelEntity.toString() + ". Assigned ID: " + id);
        return modelEntity;
    }
    
}
