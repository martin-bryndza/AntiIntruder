/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.dto.convert.impl;

import com.ysoft.tools.antiintruder.backend.dao.StateDao;
import com.ysoft.tools.antiintruder.backend.model.State;
import com.ysoft.tools.antiintruder.serviceapi.dto.StateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class StateConvert{
    
    @Autowired
    private StateDao stateDao;

    public State fromDtoToEntity(StateDto dto) {
        if (dto == null){
            return null;
        }
        State s = new State();
        s.setId(dto.getId());
        s.setName(dto.getName());
        s.setMinDuration(dto.getMinDuration());
        s.setMaxDuration(dto.getMaxDuration());
        s.setDefaultSuccessor(stateDao.findOne(dto.getDefaultSuccessorId()).get());
        return s;
    }

    public static StateDto fromEntityToDto (State entity) {
        if (entity == null){
            return null;
        }
        StateDto dto = new StateDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setMinDuration(entity.getMinDuration());
        dto.setMaxDuration(entity.getMaxDuration());
        dto.setDefaultSuccessorId(entity.getDefaultSuccessor().getId());
        return dto;
    }
    
}
