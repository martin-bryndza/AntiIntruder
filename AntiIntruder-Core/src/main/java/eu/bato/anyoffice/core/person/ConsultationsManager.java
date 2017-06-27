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
package eu.bato.anyoffice.core.person;

import eu.bato.anyoffice.serviceapi.dto.ConsultationDto;
import eu.bato.anyoffice.serviceapi.dto.ConsultationState;
import eu.bato.anyoffice.serviceapi.service.ConsultationService;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages interactions of persons and entities.
 *
 * @author bryndza
 */
@Service
public class ConsultationsManager {

    private final static Logger log = LoggerFactory.getLogger(ConsultationsManager.class);

    @Autowired
    private PersonService personService;
    
    @Autowired
    private ConsultationService consultationService;
    
    public void addConsultation(Long requesterId, Long targetId, String message) {
        if (requesterId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid parameter: requesterId");
            log.error("Null parameter: Long requesterId", iaex);
            throw iaex;
        }
        if (targetId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid parameter: targetId");
            log.error("Null parameter: Long targetId", iaex);
            throw iaex;
        }
        ConsultationDto dto = new ConsultationDto();
        dto.setRequester(personService.findOne(requesterId));
        dto.setState(ConsultationState.PENDING);
        dto.setTarget(personService.findOne(targetId));
        dto.setTime(new Date());
        dto.setMessage(message);
        consultationService.save(dto);
    }
    
    public void cancelConsultationByRequester(Long consultationId){
        if (consultationId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid parameter: consultationId");
            log.error("Null parameter: Long consultationId", iaex);
            throw iaex;
        }
        consultationService.setState(consultationId, ConsultationState.CANCELLED);
    }
    
    public void settleConsultation(Long consultationId){
        if (consultationId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid parameter: consultationId");
            log.error("Null parameter: Long consultationId", iaex);
            throw iaex;
        }
        consultationService.setState(consultationId, ConsultationState.DONE);
    }

    public void callRequester(Long consultationId) {
        if (consultationId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid parameter: consultationId");
            log.error("Null parameter: Long consultationId", iaex);
            throw iaex;
        }
        if (consultationService.getState(consultationId).isActiveState()){
            consultationService.setState(consultationId, ConsultationState.WAITING_FOR_REQUESTER);
        }
    }

    public void cancelCallToRequester(Long consultationId) {
        if (consultationId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid parameter: consultationId");
            log.error("Null parameter: Long consultationId", iaex);
            throw iaex;
        }
        if (consultationService.getState(consultationId).isActiveState()) {
            consultationService.setState(consultationId, ConsultationState.PENDING);
        }
    }
    
    public void acceptCallFromTarget(Long consultationId) {
        if (consultationId == null) {
            IllegalArgumentException iaex = new IllegalArgumentException("Invalid parameter: consultationId");
            log.error("Null parameter: Long consultationId", iaex);
            throw iaex;
        }
        consultationService.setState(consultationId, ConsultationState.IN_PROGRESS);
    }

    public List<ConsultationDto> getActiveIncomingConsultations(Long targetId) {
        List<ConsultationDto> result = new LinkedList<>();
        for (ConsultationState state: ConsultationState.values()){
            if (state.isActiveState()){
                result.addAll(consultationService.getIncomingConsultations(targetId, state));
            }
        }
        return result;
    }

    public List<ConsultationDto> getActiveOutgoingConsultations(Long requesterId) {
        List<ConsultationDto> result = new LinkedList<>();
        for (ConsultationState state : ConsultationState.values()) {
            if (state.isActiveState()) {
                result.addAll(consultationService.getOutgoingConsultations(requesterId, state));
            }
        }
        return result;
    }
    
}
