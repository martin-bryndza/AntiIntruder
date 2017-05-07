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
package eu.bato.anyoffice.backend.service.impl;

import eu.bato.anyoffice.backend.dao.ConsultationDao;
import eu.bato.anyoffice.backend.dao.PersonDao;
import eu.bato.anyoffice.backend.dto.convert.impl.ConsultationConvert;
import eu.bato.anyoffice.backend.model.Consultation;
import eu.bato.anyoffice.backend.service.common.DataAccessExceptionNonVoidTemplate;
import eu.bato.anyoffice.backend.service.common.DataAccessExceptionVoidTemplate;
import eu.bato.anyoffice.serviceapi.dto.ConsultationDto;
import eu.bato.anyoffice.serviceapi.service.ConsultationService;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bato
 */
@Service
@Transactional(noRollbackFor = NoResultException.class)
public class ConsultationServiceImpl implements ConsultationService {

    final static Logger log = LoggerFactory.getLogger(ConsultationServiceImpl.class);
    @Autowired
    private ConsultationDao consultationDao;
    @Autowired
    private PersonDao personDao;
    
    @Override
    @Transactional(readOnly = false)
    public Long save(ConsultationDto dto) {
        return (Long) new DataAccessExceptionNonVoidTemplate(dto) {
            @Override
            public Long doMethod() {
                ConsultationDto dto = (ConsultationDto) getU();
                Consultation entity = new Consultation();
                entity.setRequester(personDao.findOneByUsername(dto.getRequesterUsername()));
                entity.setTarget(personDao.findOneByUsername(dto.getTargetUsername()));
                entity.setPurpose(dto.getPurpose());
                entity.setTime(dto.getTime());
                if (entity.getRequester() == null) {
                    throw new IllegalArgumentException("Error while saving consultation " + dto.toString() + ". Requester does not exist in DB.");
                }
                if (entity.getTarget() == null) {
                    throw new IllegalArgumentException("Error while saving consultation " + dto.toString() + ". Target does not exist in DB.");
                }
                Consultation savedEntity = consultationDao.save(entity);
                return savedEntity.getId();
            }
        }.tryMethod();
    }

    @Override
    public ConsultationDto findOne(Long id) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid id in parameter: null");
            log.error("ConsultationServiceImpl.get() called on null parameter: Long id", iaex);
            throw iaex;
        }
        return (ConsultationDto) new DataAccessExceptionNonVoidTemplate(id) {
            @Override
            public ConsultationDto doMethod() {
                Consultation entity = consultationDao.findOne((Long) getU());
                return ConsultationConvert.fromEntityToDto(entity);
            }
        }.tryMethod();
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Cannot remove entity that"
                    + " doesn't exist.");
            log.error("ID is null", iaex);
            throw iaex;
        } else {
            new DataAccessExceptionVoidTemplate(id) {
                @Override
                public void doMethod() {
                    consultationDao.delete((Long) getU());
                }
            }.tryMethod();
        }
    }

    //TODO: add paging
    @Override
    public List<ConsultationDto> findAll() {
        List<Consultation> entities = consultationDao.findAll();
        List<ConsultationDto> result = new LinkedList<>();
        entities.stream().forEach((entity) -> {
            result.add(ConsultationConvert.fromEntityToDto(entity));
        });
        return result;
    }

    @Override
    public Long addConsultation(String requesterUsername, Long targetId, String purpose) {
        ConsultationDto dto = new ConsultationDto();
        dto.setTime(new Date());
        dto.setRequesterUsername(requesterUsername);
        dto.setTargetUsername(personDao.findOne(targetId).getUsername());
        dto.setPurpose(purpose);
        return save(dto);
    }

    @Override
    public void cancelConsultation(String requesterUsername, Long targetId) {
        Consultation entity = consultationDao.findOne(requesterUsername, targetId);
        entity.cancel();
    }

    

}
