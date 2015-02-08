/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.dto.convert.impl;

import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;
import com.ysoft.tools.antiintruder.serviceapi.dto.convert.Convert;
import com.ysoft.tools.antiintruder.backend.model.Entitty;
import com.ysoft.tools.antiintruder.backend.model.State;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class EntityConvert implements Convert<Entitty, EntityDto>{

    @Override
    public Entitty fromDtoToEntity(EntityDto dto) {
     Entitty e = new Entitty();
        e.setUsername(dto.getUsername());
        e.setDisplayName(dto.getDisplayName());
//        e.setState(State.AVAILABLE);
        return e;
    }

    @Override
    public EntityDto fromEntityToDto (Entitty entity) {
        EntityDto dto = new EntityDto();
        dto.setDisplayName(entity.getDisplayName());
        dto.setUsername(entity.getUsername());
        return dto;
    }
    
}
