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

import eu.bato.anyoffice.backend.dao.StateDao;
import eu.bato.anyoffice.backend.model.State;
import eu.bato.anyoffice.serviceapi.dto.StateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class StateConvert {

    @Autowired
    private StateDao stateDao;

    public State fromDtoToEntity(StateDto dto) {
        if (dto == null) {
            return null;
        }
        State s = new State();
        s.setId(dto.getId());
        s.setName(dto.getName());
        s.setMinDuration(dto.getMinDuration());
        s.setMaxDuration(dto.getMaxDuration());
        s.setDefaultSuccessor(stateDao.findOne(dto.getDefaultSuccessorId()));
        return s;
    }

    public StateDto fromEntityToDto(State entity) {
        if (entity == null) {
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
