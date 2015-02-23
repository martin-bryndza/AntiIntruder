package com.ysoft.tools.antiintruder.backend.dao.impl;

import com.ysoft.tools.antiintruder.backend.dao.EntityDao;
import com.ysoft.tools.antiintruder.backend.dao.StateDao;
import com.ysoft.tools.antiintruder.backend.model.Entity;
import com.ysoft.tools.antiintruder.backend.model.State;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Bato
 */
@Repository
public class EntityDaoImpl implements EntityDao{
    
    final static Logger log = LoggerFactory.getLogger(EntityDaoImpl.class);

    @Autowired
    private StateDao stateDao;
    
    @PersistenceContext(name = "entityManagerFactory")
    private EntityManager em;

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid record: null or with no id.");
        }
        Entity entity = em.find(Entity.class, id);
        if (entity == null) {
            log.error("Entity is not in DB");
        }
        em.remove(entity);
    }

    @Override
    public List<Entity> findAll() {
        return em.createQuery("SELECT tbl FROM Entity tbl", Entity.class).getResultList();
    }

    @Override
    public Optional<Entity> findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid id: " + id);
        } else if (em.createQuery("SELECT e.id FROM Entity e WHERE e.id = :pk", Long.class).setParameter("pk", id).getResultList().size() < 1) {
            throw new IllegalArgumentException("Invalid id: nonexistent");
        }
        return Optional.ofNullable(em.createQuery("SELECT e FROM Entity e WHERE e.id = :pk", Entity.class).setParameter("pk", id).getSingleResult());
    }

    @Override
    public Entity save(Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Invalid entity (Entity): " + entity);
        }
        if (entity.getState() == null){
            throw new IllegalArgumentException("Entity state cannot be null. " + entity.toString());
        }
        //Save the entity
        log.info("Saving " + entity.toString());
        Entity modelEntity = em.merge(entity);
        Long id = modelEntity.getId();
        log.info("Saved " + modelEntity.toString() + ". Assigned ID: " + id);
        return modelEntity;
    }

    @Override
    public Entity updateState(Long id, Long stateId) {
        Optional<Entity> e = findOne(id);
        if (!e.isPresent()){
            throw new IllegalArgumentException("Entity with id " + id + " does not exist.");
        }
        Optional<State> s = stateDao.findOne(stateId);
        if (!s.isPresent()) {
            throw new IllegalArgumentException("State with id " + stateId + " does not exist.");
        }
        if (e.get().getState().equals(s.get())){
            log.info("Actual and wanted states are the same. State will not be changed.");
            return e.get();
        }        
        Entity ent = e.get();
        ent.setState(s.get());
        return ent;
    }
        
}
