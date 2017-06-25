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
package eu.bato.anyoffice.frontend.rest.v1_0.controllers;

import eu.bato.anyoffice.core.person.ConsultationsManager;
import eu.bato.anyoffice.core.person.PersonStateManager;
import eu.bato.anyoffice.frontend.rest.Versions;
import eu.bato.anyoffice.frontend.web.data.User;
import eu.bato.anyoffice.serviceapi.dto.ConsultationDto;
import eu.bato.anyoffice.serviceapi.dto.ConsultationState;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.ConsultationService;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Bato
 */
@Transactional
@RestController("PersonStateResourceV1")
@RequestMapping(value = "/api/v1",
        produces = Versions.V1_0,
        consumes = Versions.V1_0)
public class PersonStateController {

    private static final Logger log = LoggerFactory.getLogger(PersonStateController.class);

    @Autowired
    PersonStateManager personStateManager;

    @Autowired
    ConsultationsManager consultationsManager;

    @Autowired
    PersonService personService;
    
    @Autowired
    ConsultationService consultationService;

    @RequestMapping(value = "login", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public void validateCredentials(Authentication authentication) {
        log.info("User " + authentication.getName() + " connected.");
        ping(authentication); // temporal, until all clients are updated
    }

    @RequestMapping(value = "ping", method = PUT)
    public @ResponseBody
    String ping(Authentication authentication) {
        personService.setLastPing(authentication.getName(), new Date());
        return "pong";
    }

    @RequestMapping(value = "state", method = GET)
    public @ResponseBody
    PersonState getCurrentState(Authentication authentication) {
        return personStateManager.getCurrentState(authentication.getName());
    }

    @RequestMapping(value = "state", method = PUT)
    @ResponseBody
    public PersonState setCurrentState(@RequestBody PersonState newState, Authentication authentication) {
        log.info("Setting new state: " + newState + " to person " + authentication.getName());
        return personStateManager.setState(authentication.getName(), newState, false);
    }

    @RequestMapping(value = "statednd", method = PUT)
    @ResponseBody
    public PersonState setDndState(@RequestBody Long period, Authentication authentication) {
        log.info("Setting new state: {} to person {}", PersonState.DO_NOT_DISTURB, authentication.getName());
        return personStateManager.setDndState(authentication.getName(), false, period);
    }

    @RequestMapping(value = "adddnd", method = PUT)
    @ResponseBody
    public Long addDndStateTime(@RequestBody Long millisToAdd, Authentication authentication) {
        log.info("Adding {} DND millis to person {}", millisToAdd.toString(), authentication.getName());
        return personStateManager.addDndTime(authentication.getName(), millisToAdd);
    }

    @RequestMapping(value = "dndmax", method = GET)
    public @ResponseBody
    Long getDndMaxTime() {
        return personStateManager.getDndMaxTime();
    }

    @RequestMapping(value = "locked", method = PUT)
    @ResponseBody
    public PersonState setSessionLocked(@RequestBody Boolean locked, Authentication authentication) {
        log.info("User " + authentication.getName() + (locked ? " " : " started client or un") + "locked session");
        if (locked) {
            return personStateManager.setState(authentication.getName(), PersonState.AWAY, false);
        } else {
            return personStateManager.returnFromAwayState(authentication.getName());
        }
    }

    @RequestMapping(value = "canchange", method = GET)
    public @ResponseBody
    Boolean isStateChangePossible(@RequestParam String state, Authentication authentication) {
        return personStateManager.isStateChangePossible(authentication.getName(), PersonState.valueOf(state));
    }

    @RequestMapping(value = "location", method = GET)
    public @ResponseBody
    String getCurrentLocation(Authentication authentication) {
        return personService.getLocation(authentication.getName());
    }

    @RequestMapping(value = "location", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public void setCurrentLocation(@RequestBody String location, Authentication authentication) {
        log.info("Setting new location: " + location + " to person " + authentication.getName());
        personService.setLocation(authentication.getName(), location);
    }

    @RequestMapping(value = "requests", method = GET)
    public @ResponseBody
    Integer getNumberOfIncomingConsultations(Authentication authentication) {
        int result = consultationsManager.getActiveIncomingConsultations(getCurrentUserId(authentication)).size();
        log.debug("GET number of incoming consultations for user {}, response: {}", authentication.getName(), result);
        return result;
    }

    /**
     * Returns all active incoming consultations.
     *
     * @param authentication
     * @return List of consultations
     */
    @RequestMapping(value = "incomingConsultations", method = GET)
    public @ResponseBody
    List<ConsultationDto> getPendingIncomingConsultations(Authentication authentication) {
        List<ConsultationDto> incomingConsultations = consultationsManager.getActiveIncomingConsultations(getCurrentUserId(authentication));
        log.debug("GET incomingConsultations for user {}, response size: {}", authentication.getName(), incomingConsultations.size());
        return incomingConsultations;
    }
    
    /**
     * Returns all active outgoing consultations.
     *
     * @param authentication
     * @return List of consultations
     */
    @RequestMapping(value = "outgoingConsultations", method = GET)
    public @ResponseBody
    List<ConsultationDto> getPendingOutgoingConsultations(Authentication authentication) {
        List<ConsultationDto> outgoingConsultations = consultationsManager.getActiveOutgoingConsultations(getCurrentUserId(authentication));
        log.debug("GET outgoingConsultations for user {}, response size: {}", authentication.getName(), outgoingConsultations.size());
        return outgoingConsultations;
    }

    @RequestMapping(value = "dndStart", method = GET)
    public @ResponseBody
    Long getdndStart(Authentication authentication) {
        long result = personStateManager.getDndStart(authentication.getName());
        log.debug("GET dndStart for user {}, response: {}", authentication.getName(), result);
        return result;
    }

    @RequestMapping(value = "dndEnd", method = GET)
    public @ResponseBody
    Long getdndEnd(Authentication authentication) {
        long result = personStateManager.getDndEnd(authentication.getName());
        log.debug("GET dndEnd for user {}, response: {}", authentication.getName(), result);
        return result;
    }

    @RequestMapping(value = "disturbance", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public void noteDisturbance(@RequestBody Boolean aoUser, Authentication authentication) {
        log.info("Noting disturbance by AnyOffice user: " + aoUser + " to person " + authentication.getName());
        personService.noteDisturbance(authentication.getName(), aoUser);
    }
    
    @RequestMapping(value = "settleConsultation", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public void settleConsultation(@RequestBody Long consultationId, Authentication authentication) {
        log.info("Settling consultation with id " + consultationId +" by user: " + authentication.getName());
        consultationsManager.settleConsultation(consultationId);
    }
    
    @RequestMapping(value = "callRequester", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public void callRequester(@RequestBody Long consultationId, Authentication authentication) {
        log.info("Calling requester of consultation with id " + consultationId + " by user: " + authentication.getName());
        consultationsManager.callRequester(consultationId);
    }
    
    @RequestMapping(value = "cancelCallToRequester", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public void cancelCallToRequester(@RequestBody Long consultationId, Authentication authentication) {
        log.info("Cancelling call to requester of consultation with id " + consultationId + " by user: " + authentication.getName());
        consultationsManager.cancelCallToRequester(consultationId);
    }
    
    /**
     * Gets the ID of the currently authenticated user.
     *
     * @param authentication
     * @return The ID.
     */
    private Long getCurrentUserId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();    
    }

}
