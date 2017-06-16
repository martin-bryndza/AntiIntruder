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

import eu.bato.anyoffice.core.integration.hipchat.HipChatClient;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author bryndza
 */
@Controller
public class PersonController extends CommonController {

    @Autowired
    HipChatClient hipChatClient;

    @ModelAttribute("page")
    public String module() {
        return "personEdit";
    }

    @RequestMapping(value = "/personEdit", method = RequestMethod.GET)
    public String loadItems(Model model, Authentication authentication) {
        Authentication auth = (Authentication) SecurityContextHolder.getContext().getAuthentication();
        PersonDto currentPerson = personService.findOneByUsername(auth.getName());
        model.addAttribute("currentPerson", currentPerson);
        return "personEdit";
    }

    @RequestMapping(value = "/personEdit/save", method = RequestMethod.POST)
    public String submitFormHandler(Model model, @ModelAttribute PersonDto person) {
        Authentication auth = (Authentication) SecurityContextHolder.getContext().getAuthentication();
        PersonDto currentPerson = personService.findOneByUsername(auth.getName());
        currentPerson.setLocation(person.getLocation());
        currentPerson.setDescription(person.getDescription());
        if (!person.getDisplayName().isEmpty()) {
            currentPerson.setDisplayName(person.getDisplayName());
        }
        if (!person.getHipChatEmail().isEmpty() && !person.getHipChatToken().isEmpty()) {
            currentPerson.setHipChatEmail(person.getHipChatEmail());
            currentPerson.setHipChatToken(person.getHipChatToken());
            if (hipChatClient.getPerson(currentPerson.getHipChatToken(), currentPerson.getHipChatEmail()) == null) {
                model.addAttribute("currentPerson", currentPerson);
                model.addAttribute("hcError", true);
                return "personEdit";
            }
        } else {
            currentPerson.setHipChatEmail(null);
            currentPerson.setHipChatToken(null);
        }
        personService.save(currentPerson);
        return "redirect:/";
    }

}
