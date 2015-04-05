/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.frontend.web.controllers;

import eu.bato.anyoffice.core.person.PersonStateManager;
import eu.bato.anyoffice.frontend.web.data.PasswordObject;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonRole;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.ResourceService;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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
public class IndexController {

    @Autowired
    protected PersonService personService;
    @Autowired
    protected ResourceService entityService;
    @Autowired
    protected PersonStateManager personStateManager;
    
    private PersonDto adminPerson = null;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String loadItems(Model model, Authentication authentication) {
        model.addAttribute("personObject", new PersonDto());
        model.addAttribute("password", new PasswordObject());
        model.addAttribute("states", PersonState.values());
        List<PersonDto> otherPersons = personService.findAll();
        if (adminPerson == null){
            adminPerson = personService.findOneByUsername("adminn");
        }
        otherPersons.remove(adminPerson);
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
            UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (currentUser.getAuthorities().contains(new SimpleGrantedAuthority(PersonRole.USER.name()))) {
                PersonDto currentPerson = personService.findOneByUsername(currentUser.getUsername());
                model.addAttribute("currentPerson", currentPerson);
                otherPersons.remove(currentPerson);
            }
        }
        model.addAttribute("persons", otherPersons);
        return "index";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String submitFormHandler(@ModelAttribute PersonDto person, @ModelAttribute PasswordObject password) {
        person.setRole(PersonRole.USER);
        person.setState(PersonState.UNKNOWN);
        personService.register(person, password.getValue());
        return "redirect:";
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String completeItem(@RequestParam Long id) {
        personService.delete(id);
        return "redirect:";
    }
    
    @RequestMapping(value = "/interact", method = RequestMethod.GET)
    public String interact(@RequestParam Long id) {
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (currentUser.getAuthorities().contains(new SimpleGrantedAuthority(PersonRole.USER.name()))) {
                personService.addInteractionEntity(currentUser.getUsername(), id);
            }
        }
        return "redirect:";
    }
    
    @RequestMapping(value = "/cancelinteract", method = RequestMethod.GET)
    public String cancelInteract(@RequestParam Long id) {
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (currentUser.getAuthorities().contains(new SimpleGrantedAuthority(PersonRole.USER.name()))) {
                personService.removeInteractionEntity(currentUser.getUsername(), id);
            }
        }
        return "redirect:";
    }

    @RequestMapping(value = "/changeState", method = RequestMethod.GET)
    public String changeState(@RequestParam Long id, String state) {
        personStateManager.setState(id, PersonState.valueOf(state), true);
        return "redirect:";
    }
}
