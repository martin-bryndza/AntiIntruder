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

import eu.bato.anyoffice.backend.model.Resource;
import eu.bato.anyoffice.serviceapi.dto.ResourceDto;
import eu.bato.anyoffice.serviceapi.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class ResourceConvert {

    @Autowired
    private StateService stateService;

    @Autowired
    private StateConvert stateConvert;

    public Resource fromDtoToEntity(ResourceDto dto) {
        if (dto == null) {
            return null;
        }
        Resource e = new Resource();
        e.setId(dto.getId());
        e.setDescription(dto.getDescription());
        e.setDisplayName(dto.getDisplayName());
        e.setState(stateConvert.fromDtoToEntity(stateService.findOne(dto.getStateId())));
        e.setLocation(dto.getLocation());
        //interaction entities are added one after another
        //it is not possible to change lastStateChange from outside Backend module
        return e;
    }

    public ResourceDto fromEntityToDto(Resource entity) {
        if (entity == null) {
            return null;
        }
        ResourceDto dto = new ResourceDto();
        dto.setId(entity.getId());
        dto.setDisplayName(entity.getDisplayName());
        dto.setDescription(entity.getDescription());
        dto.setStateId(entity.getState().getId());
        dto.setLastStateChange(entity.getLastStateChange().getTime());
        dto.setNextPossibleStateChange(entity.getNextPossibleStateChange());
        dto.setStateExpiration(entity.getStateExpiration());
        dto.setLocation(entity.getLocation());
        return dto;
    }

}
