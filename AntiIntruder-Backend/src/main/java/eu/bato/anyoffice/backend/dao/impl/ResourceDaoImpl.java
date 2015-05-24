/* 
 * Copyright (c) 2015, Martin Bryndza
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package eu.bato.anyoffice.backend.dao.impl;

import eu.bato.anyoffice.backend.dao.ResourceDao;
import eu.bato.anyoffice.backend.dao.StateDao;
import eu.bato.anyoffice.backend.model.Entity;
import eu.bato.anyoffice.backend.model.Resource;
import eu.bato.anyoffice.backend.model.State;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bato
 */
@Repository
@Transactional
public class ResourceDaoImpl implements ResourceDao {

    final static Logger log = LoggerFactory.getLogger(ResourceDaoImpl.class);

    @Autowired
    private StateDao stateDao;

    @PersistenceContext
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
    public List<Resource> findAll() {
        return em.createQuery("SELECT tbl FROM Resource tbl", Resource.class).getResultList();
    }

    @Override
    public Resource findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid id: " + id);
        } else if (em.createQuery("SELECT e.id FROM Resource e WHERE e.id = :pk", Long.class).setParameter("pk", id).getResultList().size() < 1) {
            throw new IllegalArgumentException("Invalid id: nonexistent");
        }
        return em.createQuery("SELECT e FROM Resource e WHERE e.id = :pk", Resource.class).setParameter("pk", id).getSingleResult();
    }

    @Override
    public Resource save(Resource entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Invalid entity (Resource): " + entity);
        }
        if (entity.getState() == null) {
            throw new IllegalArgumentException("Resource state cannot be null. " + entity.toString());
        }
        //Save the entity
        log.info("Saving " + entity.toString());
        Resource modelEntity = em.merge(entity);
        Long id = modelEntity.getId();
        log.info("Saved " + modelEntity.toString() + ". Assigned ID: " + id);
        return modelEntity;
    }

    @Override
    public Resource updateState(Long id, Long stateId) {
        Resource e = findOne(id);
        State s = stateDao.findOne(stateId);
        if (e.getState().equals(s)) {
            log.info("Actual and wanted states are the same. State will not be changed.");
            return e;
        }
        e.setState(s);
        return e;
    }

}
