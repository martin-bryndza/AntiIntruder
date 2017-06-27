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
import eu.bato.anyoffice.serviceapi.dto.ConsultationState;
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
    @Autowired
    private ConsultationConvert consultationConvert;

    @Override
    @Transactional(readOnly = false)
    public Long save(ConsultationDto dto) {
        return (Long) new DataAccessExceptionNonVoidTemplate(dto) {
            @Override
            public Long doMethod() {
                ConsultationDto dto = (ConsultationDto) getU();
                Consultation entity = consultationConvert.fromDtoToEntity(dto);
                if (entity.getState() == null) {
                    entity.setState(ConsultationState.PENDING);
                }
                if (entity.getTime() == null) {
                    entity.setTime(new Date());
                }
                Consultation savedEntity = consultationDao.save(entity);
                return savedEntity.getId();
            }
        }.tryMethod();
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Cannot"
                    + " remove a consultation that doesn't exist.");
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
        entities.stream().forEach((Consultation entity) -> {
            result.add(ConsultationConvert.fromEntityToDto(entity));
        });
        return result;
    }

    @Override
    public ConsultationDto findOne(Long id) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid id in parameter: null");
            log.error("ConsultationServiceImpl.findOne() called on null parameter: Long id", iaex);
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
    public void setState(Long consultationId, ConsultationState state){
        if (consultationId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid consultationId parameter: null");
            log.error("consultationId is null", iaex);
            throw iaex;
        }
        if (state == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Cannot update consultation to null state.");
            log.error("ConsultationState is null", iaex);
            throw iaex;
        }
        new DataAccessExceptionVoidTemplate(consultationId, state) {
            @Override
            public void doMethod() {
                consultationDao.setState((Long) getU(), (ConsultationState) getV());
            }
        }.tryMethod();
    }
    
    @Override
    public List<ConsultationDto> getIncomingConsultations(Long targetId, ConsultationState state) {
        if (targetId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid targetId in parameter: null");
            log.error("PersonServiceImpl.getIncomingConsultations() called on null parameter: Long targetId", iaex);
            throw iaex;
        }
        return (List<ConsultationDto>) new DataAccessExceptionNonVoidTemplate(targetId, state) {
            @Override
            public List<ConsultationDto> doMethod() {
                List<Consultation> outgoingConsultations = consultationDao.getIncomingConsultations((Long) getU(), (ConsultationState) getV());
                List<ConsultationDto> result = new LinkedList<>();
                outgoingConsultations.stream().forEach((c) -> {
                    result.add(ConsultationConvert.fromEntityToDto(c));
                });
                return result;
            }
        }.tryMethod();
    }

    @Override
    public List<ConsultationDto> getOutgoingConsultations(Long requesterId, ConsultationState state) {
        if (requesterId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid requesterId in parameter: null");
            log.error("PersonServiceImpl.getOutgoingConsultations() called on null parameter: Long requesterId", iaex);
            throw iaex;
        }
        return (List<ConsultationDto>) new DataAccessExceptionNonVoidTemplate(requesterId, state) {
            @Override
            public List<ConsultationDto> doMethod() {
                List<Consultation> outgoingConsultations = consultationDao.getOutgoingConsultations((Long) getU(), (ConsultationState) getV());
                List<ConsultationDto> result = new LinkedList<>();
                outgoingConsultations.stream().forEach((c) -> {
                    result.add(ConsultationConvert.fromEntityToDto(c));
                });
                return result;
            }
        }.tryMethod();
    }

    @Override
    public List<Long> getTargetsIds(String username, ConsultationState state) {
        if (username == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid username in parameter: null");
            log.error("PersonServiceImpl.getTargetIds() called on null parameter: String username", iaex);
            throw iaex;
        }        
        return (List<Long>) new DataAccessExceptionNonVoidTemplate(username, state) {
            @Override
            public List<Long> doMethod() {
                Long requesterId = personDao.findOneByUsername((String) getU()).getId();
                return consultationDao.getTargetsIds(requesterId, (ConsultationState) getV());
            }
        }.tryMethod();
    }

    @Override
    public List<Long> getRequestersIds(Long targetId, ConsultationState state) {
        if (targetId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid targetId in parameter: null");
            log.error("PersonServiceImpl.getRequestersIds() called on null parameter: Long targetId", iaex);
            throw iaex;
        }
        return (List<Long>) new DataAccessExceptionNonVoidTemplate(targetId, state) {
            @Override
            public List<Long> doMethod() {
                return consultationDao.getRequestersIds((Long) getU(), (ConsultationState) getV());
            }
        }.tryMethod();
    }

    @Override
    public ConsultationState getState(Long consultationId) {
        if (consultationId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid consultationId parameter: null");
            log.error("consultationId is null", iaex);
            throw iaex;
        }
        return (ConsultationState) new DataAccessExceptionNonVoidTemplate(consultationId) {
            @Override
            public ConsultationState doMethod() {
                return consultationDao.getState((Long) getU());
            }
        }.tryMethod();
    }

}
