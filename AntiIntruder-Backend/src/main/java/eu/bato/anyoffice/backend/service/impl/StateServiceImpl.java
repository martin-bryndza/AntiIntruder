/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
