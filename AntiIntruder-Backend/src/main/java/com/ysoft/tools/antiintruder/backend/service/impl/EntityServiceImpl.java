/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.service.impl;

import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;
import com.ysoft.tools.antiintruder.backend.dao.EntityDao;
import com.ysoft.tools.antiintruder.backend.dto.convert.impl.EntityConvert;
import com.ysoft.tools.antiintruder.backend.model.Entitty;
import com.ysoft.tools.antiintruder.backend.model.State;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author Bato
 */
@Service
@Transactional
public class EntityServiceImpl implements EntityService{
    
    final static Logger log = LoggerFactory.getLogger(EntityServiceImpl.class);
    @Autowired
    private EntityConvert convert;
    @Autowired
    private EntityDao entityDao;
    
    @Override
    @Transactional(readOnly = false)
    public Long save(EntityDto dto) {
        return (Long) new DataAccessExceptionNonVoidTemplate(dto) {
            @Override
            public Long doMethod() {
                System.out.println(convert);
                Entitty entity = convert.fromDtoToEntity((EntityDto) getU());
                Entitty savedEntity = entityDao.save(entity);
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
                Optional<Entitty> entity = entityDao.findOne((Long) getU());
                if (entity.isPresent()){
                    return convert.fromEntityToDto(entity.get());
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
        List<Entitty> entities = entityDao.findAll();
        List<EntityDto> result = new LinkedList<>();
        for (Entitty entity : entities) {
            result.add(EntityConvert.fromEntityToDto(entity));
        }
        return result;
    }
}
