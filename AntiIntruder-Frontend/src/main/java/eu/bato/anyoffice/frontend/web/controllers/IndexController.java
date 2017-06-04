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

import eu.bato.anyoffice.core.person.PersonStateManager;
import eu.bato.anyoffice.frontend.web.data.PasswordObject;
import eu.bato.anyoffice.serviceapi.dto.ConsultationState;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonRole;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.ConsultationService;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Bato
 */
@Controller
public class IndexController extends CommonController {

    @Autowired
    protected PersonStateManager personStateManager;
    
    @Autowired
    protected ConsultationService consultationService;

    private final static Logger log = LoggerFactory.getLogger(PersonStateManager.class);

    private PersonDto adminPerson = null;

    @ModelAttribute("page")
    public String module() {
        return "index";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String loadItems(Model model, Authentication authentication) {
        model.addAttribute("personObject", new PersonDto());
        model.addAttribute("password", new PasswordObject());
        model.addAttribute("states", PersonState.values());
        addCurrentAndPersons(model);
        return "index";
    }

    @RequestMapping(value = "/colleagues", method = RequestMethod.GET)
    public String loadColleagues(Model model, Authentication authentication) {
        model.addAttribute("states", PersonState.values());
        addCurrentAndPersons(model);
        model.addAttribute("now", new Date().getTime());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals("anonymousUser")) {
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority(PersonRole.ROLE_USER.name()))) {
                model.addAttribute("consultationsIds", consultationService.getTargetsIds(auth.getName(), ConsultationState.PENDING));
            }
        }
        return "fragments/colleagues :: colleagues";
    }

    private void addCurrentAndPersons(Model model) {
        List<PersonDto> otherPersons = personService.findAll();
        if (adminPerson == null) {
            adminPerson = personService.findOneByUsername("adminAnyOffice");
        }
        otherPersons.remove(adminPerson);
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
            Authentication auth = (Authentication) SecurityContextHolder.getContext().getAuthentication();
            PersonDto currentPerson = personService.findOneByUsername(auth.getName());
            model.addAttribute("currentPerson", currentPerson);
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority(PersonRole.ROLE_USER.name()))) {
                otherPersons.remove(currentPerson);
            }
        }
        model.addAttribute("persons", otherPersons);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String submitFormHandler(@ModelAttribute PersonDto person, @ModelAttribute PasswordObject password) {
        person.setRole(PersonRole.ROLE_USER);
        person.setState(PersonState.UNKNOWN);
        personService.register(person, password.getValue());
        return "redirect:";
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public String completeItem(@RequestParam Long id) {
        personService.delete(id);
        return "redirect:";
    }

    @RequestMapping(value = "/interact", method = RequestMethod.POST)
    @ResponseBody
    public void interact(@RequestParam Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals("anonymousUser")) {
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority(PersonRole.ROLE_USER.name()))) {
                consultationService.addConsultation(auth.getName(), id, "TODO");
            }
        }
    }

    @RequestMapping(value = "/cancelinteract", method = RequestMethod.POST)
    @ResponseBody
    public void cancelInteract(@RequestParam Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals("anonymousUser")) {
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority(PersonRole.ROLE_USER.name()))) {
                consultationService.cancelConsultationByRequester(auth.getName(), id);
            }
        }
    }

    @RequestMapping(value = "/changeState", method = RequestMethod.GET)
    public String changeState(@RequestParam Long id, String state) {
        personStateManager.setState(id, PersonState.valueOf(state), true);
        return "redirect:";
    }
}
