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

import eu.bato.anyoffice.backend.dao.ConsultationDao;
import eu.bato.anyoffice.backend.model.Consultation;
import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.serviceapi.dto.ConsultationState;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author bryndza
 */
@Repository
@Transactional
public class ConsultationDaoImpl implements ConsultationDao {

    @PersistenceContext
    private EntityManager em;

    final static Logger log = LoggerFactory.getLogger(DisturbanceDaoImpl.class);

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid record: null or with no id.");
        }
        Person person = em.find(Person.class, id);
        if (person == null) {
            log.error("Consultation request with id " + id + " is not in DB");
        }
        em.remove(person);
    }

    @Override
    public List<Consultation> findAll() {
        return em.createQuery("SELECT tbl FROM ConsultationRequest tbl", Consultation.class).getResultList();
    }

    @Override
    public Consultation findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        return em.createQuery("SELECT e FROM ConsultationRequest e WHERE e.id = :pk", Consultation.class).setParameter("pk", id).getSingleResult();

    }

    @Override
    public Consultation save(Consultation consultationRequest) {
        if (consultationRequest == null) {
            throw new IllegalArgumentException("Invalid entity (ConsultationRequest): " + consultationRequest);
        }

        log.debug("Saving " + consultationRequest.toString());
        Consultation modelConsultationRequest = em.merge(consultationRequest);
        log.info("Saved " + modelConsultationRequest.toString() + ".");
        log.debug(" Assigned entity id: " + modelConsultationRequest.getId());
        return modelConsultationRequest;
    }

    @Override
    public Consultation updateState(Long id, ConsultationState state) {
        Consultation e = findOne(id);
        if (e.getState().equals(state)) {
            log.info("Actual {} and wanted {} states are the same. State of consultation {} will not be changed.", e.getState(), state, e.getId());
            return e;
        }
        e.setState(state);
        return e;
    }

}