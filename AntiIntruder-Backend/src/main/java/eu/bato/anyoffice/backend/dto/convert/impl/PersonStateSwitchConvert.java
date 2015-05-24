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

import eu.bato.anyoffice.backend.model.PersonStateSwitch;
import eu.bato.anyoffice.serviceapi.dto.PersonStateSwitchDto;
import org.springframework.stereotype.Component;

/**
 *
 * @author bryndza
 */
@Component
public class PersonStateSwitchConvert {

    public static PersonStateSwitch fromDtoToEntity(PersonStateSwitchDto dto) {
        if (dto == null) {
            return null;
        }
        PersonStateSwitch e = new PersonStateSwitch();
        e.setPersonId(dto.getPersonId());
        e.setState(dto.getState());
        e.setTime(dto.getTime());
        return e;
    }

    public static PersonStateSwitchDto fromEntityToDto(PersonStateSwitch entity) {
        if (entity == null) {
            return null;
        }
        PersonStateSwitchDto dto = new PersonStateSwitchDto();
        dto.setPersonId(entity.getPersonId());
        dto.setState(entity.getState());
        dto.setTime(entity.getTime());
        return dto;
    }

}
