/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.service.impl;

import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;
import com.ysoft.tools.antiintruder.backend.dao.EntityDao;
import com.ysoft.tools.antiintruder.backend.dto.convert.impl.EntityConvert;
import com.ysoft.tools.antiintruder.backend.model.Entity;
import com.ysoft.tools.antiintruder.backend.service.common.DataAccessExceptionNonVoidTemplate;
import com.ysoft.tools.antiintruder.backend.service.common.DataAccessExceptionVoidTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ysoft.tools.antiintruder.serviceapi.service.EntityService;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Bato
 */
@Service
@Transactional
public class EntityServiceImpl implements EntityService{
    
    final static Logger log = LoggerFactory.getLogger(EntityServiceImpl.class);
    @Autowired
    private EntityDao entityDao;
    @Autowired
    private EntityConvert entityConvert;
    
    @Override
    @Transactional(readOnly = false)
    public Long save(EntityDto dto) {
        return (Long) new DataAccessExceptionNonVoidTemplate(dto) {
            @Override
            public Long doMethod() {
                EntityDto dto = (EntityDto) getU();
                if (dto.getStateId() == null){
                    dto.setStateId(1L); // TODO: Replace with default state for entity type
                }
                Entity entity = entityConvert.fromDtoToEntity((EntityDto) getU());
                Entity savedEntity = entityDao.save(entity);
                return savedEntity.getId();
            }
        }.tryMethod();
    }

    @Override
    public EntityDto findOne(Long id) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid id in parameter: null");
            log.error("EntityServiceImpl.get() called on null parameter: Long id", iaex);
            throw iaex;
        }
        return (EntityDto) new DataAccessExceptionNonVoidTemplate(id) {
            @Override
            public EntityDto doMethod() {
                Optional<Entity> entity = entityDao.findOne((Long) getU());
                if (entity.isPresent()){
                    return EntityConvert.fromEntityToDto(entity.get());
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
                    entityDao.delete((Long) getU());
                }
            }.tryMethod();
        }
    }    

    //TODO: add paging
    @Override
    public List<EntityDto> findAll() {
        List<Entity> entities = entityDao.findAll();
        List<EntityDto> result = new LinkedList<>();
        for (Entity entity : entities) {
            result.add(EntityConvert.fromEntityToDto(entity));
        }
        return result;
    }

    @Override
    public void updateState(Long id, Long stateId) {
        if (id == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Cannot update entity that"
                    + " doesn't exist.");
            log.error("ID is null", iaex);
            throw iaex;
        } else if (stateId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Cannot update entity to state that"
                    + " doesn't exist.");
            log.error("stateId is null", iaex);
            throw iaex;
        } else {
            new DataAccessExceptionVoidTemplate(id, stateId) {
                @Override
                public void doMethod() {
                    entityDao.updateState((Long) getU(), (Long) getV());
                }
            }.tryMethod();
        }
    }

}
