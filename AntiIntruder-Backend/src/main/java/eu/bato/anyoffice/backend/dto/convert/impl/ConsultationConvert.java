/* 
 * Copyright (c) 2015, Martin Bryndza
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package eu.bato.anyoffice.backend.dto.convert.impl;

import eu.bato.anyoffice.backend.model.Consultation;
import eu.bato.anyoffice.serviceapi.dto.ConsultationDto;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Bato
 */
public class ConsultationConvert {

    @Autowired
    PersonService personService;
    
    public static ConsultationDto fromEntityToDto(Consultation entity) {
        if (entity == null) {
            return null;
        }
        ConsultationDto dto = new ConsultationDto();
        dto.setId(entity.getId());
        dto.setPurpose(entity.getPurpose());
        dto.setRequesterDisplayName(entity.getRequester().getDisplayName());
        dto.setRequesterState(entity.getRequester().getState());
        dto.setRequesterUsername(entity.getRequester().getUsername());
        dto.setTargetDisplayName(entity.getTarget().getDisplayName());
        dto.setTargetState(entity.getTarget().getState());
        dto.setTargetUsername(entity.getTarget().getUsername());
        dto.setTime(entity.getTime());
        return dto;
    }

//    public Consultation fromDtoToEntity(ConsultationDto dto) {
//        if (dto == null) {
//            return null;
//        }
//        Consultation entity = new Consultation();
//        
//        PersonDto requester = personService.findOneByUsername(dto.getRequesterUsername());
//        PersonDto target = personService.findOneByUsername(dto.getTargetUsername());
//        entity.setRequester(requester);
//        entity.setTarget(target);
//        entity.setTargetState(target.getState());
//        
//        entity.setPurpose(dto.getPurpose());
//        entity.setTime(new Date());
//        return entity;
//    }

}
