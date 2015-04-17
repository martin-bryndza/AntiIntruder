/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.bato.anyoffice.backend.dto.convert.impl;

import eu.bato.anyoffice.backend.model.StateSwitch;
import eu.bato.anyoffice.serviceapi.dto.StateSwitchDto;
import org.springframework.stereotype.Component;

/**
 *
 * @author bryndza
 */
@Component
public class StateSwitchConvert {
    
    public static StateSwitch fromDtoToEntity(StateSwitchDto dto) {
        if (dto == null) {
            return null;
        }
        StateSwitch e = new StateSwitch();
        e.setPersonId(dto.getPersonId());
        e.setState(dto.getState());
        e.setTime(dto.getTime());
        return e;
    }

    public static StateSwitchDto fromEntityToDto(StateSwitch entity) {
        if (entity == null) {
            return null;
        }
        StateSwitchDto dto = new StateSwitchDto();
        dto.setPersonId(entity.getPersonId());
        dto.setState(entity.getState());
        dto.setTime(entity.getTime());
        return dto;
    }
    
}
