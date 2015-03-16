/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.frontend.web.controllers;

import eu.bato.anyoffice.core.state.person.PersonStateManager;
import eu.bato.anyoffice.frontend.web.data.PasswordObject;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonRole;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.ResourceService;
import eu.bato.anyoffice.serviceapi.service.PersonService;
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
public class IndexController {
    
    @Autowired
    protected PersonService personService;
    @Autowired
    protected ResourceService entityService;
    @Autowired
    protected PersonStateManager personStateManager;
        
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String loadItems(Model model){
        model.addAttribute("personObject", new PersonDto());
        model.addAttribute("password", new PasswordObject());
        model.addAttribute("persons", personService.findAll());
        model.addAttribute("states", PersonState.values());
        model.addAttribute("id");
        return "index";
    }
    
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String submitFormHandler(@ModelAttribute PersonDto person, @ModelAttribute PasswordObject password){
        person.setRole(PersonRole.USER);
        person.setState(PersonState.UNKNOWN);
        personService.register(person, password.getValue());
        return "redirect:";
    }
    
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String completeItem(@RequestParam Long id){       
        personService.delete(id);
        return "redirect:";
    }
    
    @RequestMapping(value = "/changeState", method = RequestMethod.GET)
    public String changeState(@RequestParam Long id, String state) {
        personStateManager.setState(id, PersonState.valueOf(state), true);
        return "redirect:";
    }
}
