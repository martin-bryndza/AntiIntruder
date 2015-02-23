/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.dto.convert.impl;

import com.ysoft.tools.antiintruder.backend.dao.StateDao;
import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;
import com.ysoft.tools.antiintruder.backend.model.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class EntityConvert{
    
    @Autowired
    private StateDao stateDao;

    public Entity fromDtoToEntity(EntityDto dto) {
        if (dto == null){
            return null;
        } 
        Entity e = new Entity();
        e.setId(dto.getId());
        e.setDescription(dto.getDescription());
        e.setDisplayName(dto.getDisplayName());
        e.setState(stateDao.findOne(dto.getStateId()).get());
        return e;
    }

    public static EntityDto fromEntityToDto (Entity entity) {
        if (entity == null){
            return null;
        }
        EntityDto dto = new EntityDto();
        dto.setId(entity.getId());
        dto.setDisplayName(entity.getDisplayName());
        dto.setDescription(entity.getDescription());
        dto.setStateId(entity.getState().getId());
        dto.setLastStateChange(entity.getLastStateChange());
        dto.setNextPossibleStateChange(entity.getNextPossibleStateChange());
        dto.setStateExpiration(entity.getStateExpiration());
        return dto;
    }
    
}
