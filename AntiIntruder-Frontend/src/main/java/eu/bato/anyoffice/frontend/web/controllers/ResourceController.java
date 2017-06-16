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
package eu.bato.anyoffice.frontend.web.controllers;

import eu.bato.anyoffice.frontend.web.data.PasswordObject;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonRole;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.ResourceService;
import eu.bato.anyoffice.serviceapi.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Bato
 */
@Controller
public class ResourceController extends CommonController {

    @Autowired
    protected ResourceService entityService;
    @Autowired
    protected StateService stateService;

    @RequestMapping(value = "resource/", method = RequestMethod.GET)
    public String loadItems(Model model) {
        model.addAttribute("personObject", new PersonDto());
        model.addAttribute("password", new PasswordObject());
        model.addAttribute("persons", personService.findAll());
        model.addAttribute("states", stateService.findAll());
        return "index";
    }

    @RequestMapping(value = "resource/add", method = RequestMethod.POST)
    public String submitFormHandler(@ModelAttribute PersonDto person, @ModelAttribute PasswordObject password) {
        person.setState(PersonState.AVAILABLE); //TODO: replace with default state for entity type
        person.setRole(PersonRole.ROLE_USER);
        personService.register(person, password.getValue());
        return "redirect:";
    }

    @RequestMapping(value = "resource/delete", method = RequestMethod.GET)
    public String completeItem(@RequestParam Long entityId) {
        personService.delete(entityId);
        return "redirect:";
    }

    @RequestMapping(value = "resource/changeState", method = RequestMethod.GET)
    public String changeState(@RequestParam Long entityId, Long stateId) {
        entityService.updateState(entityId, stateId);
        return "redirect:";
    }
}
