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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ysoft.tools.antiintruder.serviceapi.service.EntityService;
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
//        if (dto.getActivityRecordId() != null) {
//            IllegalArgumentException iaex = new IllegalArgumentException("Cannot create activity record that"
//                    + " already exists. Use update instead.");
//            log.error("ActivityRecordServiceImpl.create() called on existing entity", iaex);
//            throw iaex;
//        }
        return (Long) new DataAccessExceptionNonVoidTemplate(dto) {
            @Override
            public Long doMethod() {
                System.out.println(convert);
                Entitty entity = convert.fromDtoToEntity((EntityDto) getU());
                Long entityId = entityDao.create(entity);
                return entityId;
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
                Entitty entity = entityDao.get((Long) getU());
                EntityDto dto = convert.fromEntityToDto(entity);
                return dto;
            }
        }.tryMethod();
    }

    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    

    @Override
    public Page<EntityDto> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
