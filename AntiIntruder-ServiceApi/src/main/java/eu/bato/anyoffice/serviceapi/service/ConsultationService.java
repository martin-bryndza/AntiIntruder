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
package eu.bato.anyoffice.serviceapi.service;

import eu.bato.anyoffice.serviceapi.dto.ConsultationDto;
import eu.bato.anyoffice.serviceapi.dto.ConsultationState;
import java.util.List;


/**
 *
 * @author Bato
 */
public interface ConsultationService extends Service<ConsultationDto> {
    
    void setState(Long consultationId, ConsultationState state);
    
    List<ConsultationDto> getIncomingConsultations(Long targetId, ConsultationState state);

    List<ConsultationDto> getOutgoingConsultations(Long requesterId, ConsultationState state);
    
    /**
     * Filters consultations in the "state" requested by the "username".
     * @param username The requester's username
     * @param state The state of the consultations to filter
     * @return List of IDs of target persons
     */
    List<Long> getTargetsIds(String username, ConsultationState state);
    
    /**
     * Filters consultations in the "state" where the "targetId" is the target.
     *
     * @param targetId The target's ID
     * @param state The state of the consultations to filter
     * @return List of IDs of requesters
     */
    List<Long> getRequestersIds(Long targetId, ConsultationState state);
    

}
