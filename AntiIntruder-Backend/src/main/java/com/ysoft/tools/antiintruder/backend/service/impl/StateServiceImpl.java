/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.service.impl;

import com.ysoft.tools.antiintruder.backend.dao.StateDao;
import com.ysoft.tools.antiintruder.backend.dto.convert.impl.StateConvert;
import com.ysoft.tools.antiintruder.backend.model.State;
import com.ysoft.tools.antiintruder.backend.service.common.DataAccessExceptionNonVoidTemplate;
import com.ysoft.tools.antiintruder.backend.service.common.DataAccessExceptionVoidTemplate;
import com.ysoft.tools.antiintruder.serviceapi.dto.StateDto;
import com.ysoft.tools.antiintruder.serviceapi.service.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Bato
 */
@Service
@Transactional
public class StateServiceImpl implements StateService{
    
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
                Optional<State> entity = stateDao.findOne((Long) getU());
                if (entity.isPresent()){
                    return StateConvert.fromEntityToDto(entity.get());
                } else {
                    return null;
                }
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
            result.add(StateConvert.fromEntityToDto(state));
        }
        return result;
    }
}
