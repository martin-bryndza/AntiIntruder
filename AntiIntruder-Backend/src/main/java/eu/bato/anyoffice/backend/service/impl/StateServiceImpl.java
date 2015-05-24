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

import eu.bato.anyoffice.backend.dao.StateDao;
import eu.bato.anyoffice.backend.dto.convert.impl.StateConvert;
import eu.bato.anyoffice.backend.model.State;
import eu.bato.anyoffice.backend.service.common.DataAccessExceptionNonVoidTemplate;
import eu.bato.anyoffice.backend.service.common.DataAccessExceptionVoidTemplate;
import eu.bato.anyoffice.serviceapi.dto.StateDto;
import eu.bato.anyoffice.serviceapi.service.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Bato
 */
@Service
@Transactional
public class StateServiceImpl implements StateService {

    final static Logger log = LoggerFactory.getLogger(StateServiceImpl.class);
    @Autowired
    private StateDao stateDao;
    @Autowired
    private StateConvert stateConvert;

    @Override
    @Transactional(readOnly = false)
    public Long save(StateDto dto) {
        return (Long) new DataAccessExceptionNonVoidTemplate(dto) {
            @Override
            public Long doMethod() {
                State entity = stateConvert.fromDtoToEntity((StateDto) getU());
                State savedEntity = stateDao.save(entity);
                return savedEntity.getId();
            }
        }.tryMethod();
    }

    @Override
    public StateDto findOne(Long id) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid id in parameter: null");
            log.error("StateServiceImpl.get() called on null parameter: Long id", iaex);
            throw iaex;
        }
        return (StateDto) new DataAccessExceptionNonVoidTemplate(id) {
            @Override
            public StateDto doMethod() {
                State entity = stateDao.findOne((Long) getU());
                return stateConvert.fromEntityToDto(entity);
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
                    stateDao.delete((Long) getU());
                }
            }.tryMethod();
        }
    }

    @Override
    public List<StateDto> findAll() {
        List<State> states = stateDao.findAll();
        List<StateDto> result = new LinkedList<>();
        for (State state : states) {
            result.add(stateConvert.fromEntityToDto(state));
        }
        return result;
    }
}
