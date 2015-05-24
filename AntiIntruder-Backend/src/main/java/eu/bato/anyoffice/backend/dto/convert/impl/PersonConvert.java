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

import eu.bato.anyoffice.backend.model.Person;
import eu.bato.anyoffice.serviceapi.dto.HipChatCredentials;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class PersonConvert {

    public Person fromDtoToEntity(PersonDto dto, String password) {
        if (dto == null) {
            return null;
        }
        Person e = new Person();
        e.setDescription(dto.getDescription());
        e.setDisplayName(dto.getDisplayName());
        e.setId(dto.getId());
        e.setState(dto.getState());
        e.setUsername(dto.getUsername());
        e.setRole(dto.getRole());
        e.setPassword(password); // the check for emptiness is done in DAO
        e.setLocation(dto.getLocation());
        e.setLastPing(new Date(dto.getLastPing().orElse(0L)));
        e.setHipChatEmail(dto.getHipChatEmail());
        e.setHipChatToken(dto.getHipChatToken());
        //awayStart, dndStart and dndEnd are set by separate method in PersonService
        //interaction entities are added one after another
        //it is not possible to change lastStateChange from outside Backend module
        return e;
    }

    public static PersonDto fromEntityToDto(Person entity) {
        if (entity == null) {
            return null;
        }
        PersonDto dto = new PersonDto();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setRole(entity.getRole());
        dto.setState(entity.getState());
        dto.setDisplayName(entity.getDisplayName());
        dto.setDescription(entity.getDescription());
        dto.setLastStateChange(entity.getLastStateChange().getTime());
        dto.setLocation(entity.getLocation());
        dto.setAwayStart(entity.getAwayStart().isPresent() ? Optional.of(entity.getAwayStart().get().getTime()) : Optional.empty());
        dto.setDndEnd(entity.getDndEnd().getTime());
        dto.setDndStart(entity.getDndStart().getTime());
        Date ping = entity.getLastPing();
        dto.setLastPing(Optional.ofNullable(ping == null ? null : entity.getLastPing().getTime()));
        dto.setInteractionEntitiesIds(entity.getInteractionEntities().stream().map(p -> p.getId()).collect(Collectors.toList()));
        dto.setHipChatEmail(entity.getHipChatEmail());
        dto.setHipChatToken(entity.getHipChatToken());
        return dto;
    }

}
