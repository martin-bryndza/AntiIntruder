/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.dto.convert.impl;

import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;
import com.ysoft.tools.antiintruder.backend.model.Entitty;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class EntityConvert{

    public static Entitty fromDtoToEntity(EntityDto dto) {
     Entitty e = new Entitty();
        e.setId(dto.getId());
        e.setUsername(dto.getUsername());
        e.setDisplayName(dto.getDisplayName());
//        e.setState(State.AVAILABLE);
        return e;
    }

    public static EntityDto fromEntityToDto (Entitty entity) {
        EntityDto dto = new EntityDto();
        dto.setId(entity.getId());
        dto.setDisplayName(entity.getDisplayName());
        dto.setUsername(entity.getUsername());
        return dto;
    }
    
}
