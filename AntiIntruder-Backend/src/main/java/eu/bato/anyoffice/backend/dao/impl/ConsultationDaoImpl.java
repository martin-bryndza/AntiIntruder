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
import eu.bato.anyoffice.serviceapi.dto.ConsultationState;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
    public void delete(Consultation.ConsultationPK pk) {
        Consultation consultation = findOne(pk);
        if (consultation == null) {
            log.error("Consultation " + pk.toString() + " is not in DB");
        }
        em.remove(consultation);
    }
    
    @Override
    public void delete(Long id) {
        Consultation consultation = findOne(id);
        if (consultation == null) {
            log.error("Consultation with ID " + id + " is not in DB");
        }
        em.remove(consultation);
    }

    @Override
    public List<Consultation> findAll() {
        return em.createQuery("SELECT tbl FROM Consultation tbl", Consultation.class).getResultList();
    }

    @Override
    public Consultation findOne(Consultation.ConsultationPK pk) {
        if (pk == null) {
            throw new IllegalArgumentException("Invalid pk: " + pk);
        }
        return em.find(Consultation.class, pk);

    }
    
    @Override
    public Consultation findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Invalid ID: " + id);
        }
        return em.createQuery("SELECT e FROM Consultation e WHERE e.id = :pk", Consultation.class).setParameter("pk", id).getSingleResult();

    }

    @Override
    public Consultation save(Consultation consultationRequest) {
        if (consultationRequest == null) {
            throw new IllegalArgumentException("Invalid entity (Consultation): " + consultationRequest);
        }

        log.debug("Saving " + consultationRequest.toString());
        Consultation modelConsultationRequest = em.merge(consultationRequest);
        log.info("Saved " + modelConsultationRequest.toString() + ".");
        return modelConsultationRequest;
    }

    @Override
    public Consultation setState(Consultation.ConsultationPK pk, ConsultationState state) {
        Consultation e = findOne(pk);
        if (e.getState().equals(state)) {
            log.info("Actual {} and wanted {} states are the same. State of consultation {} will not be changed.", e.getState(), state, e.getPk().toString());
            return e;
        }
        e.setState(state);
        return e;
    }
    
    @Override
    public List<Consultation> getIncomingConsultations(Long targetId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Consultation> getOutgoingConsultations(Long requesterId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public List<Long> getTargetsIds(Long requesterId, ConsultationState state) {
        if (requesterId == null) {
            throw new IllegalArgumentException("Invalid requesterId: " + requesterId);
        }
//        return em.createQuery("SELECT p.id FROM (SELECT e.target FROM Consultation e WHERE e.requester = :id AND e.state = :state) p", Long.class)
//                .query("id", requesterId).query("state", state).getResultList();
        log.info("em" + em);
        TypedQuery<Long> query = em.createQuery("SELECT e.target.id FROM Consultation e WHERE e.requester.id = :id AND e.state = :state", Long.class);
        query = query.setParameter("id", requesterId);
        query = query.setParameter("state", state);
        List<Long> resultList = query.getResultList();
        return resultList;
    }

}
