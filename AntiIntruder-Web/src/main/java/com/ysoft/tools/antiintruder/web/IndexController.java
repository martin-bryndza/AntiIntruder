/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.web;

import com.ysoft.tools.antiintruder.serviceapi.dto.PersonDto;
import com.ysoft.tools.antiintruder.serviceapi.dto.PersonRole;
import com.ysoft.tools.antiintruder.serviceapi.service.EntityService;
import com.ysoft.tools.antiintruder.serviceapi.service.PersonService;
import com.ysoft.tools.antiintruder.serviceapi.service.StateService;
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
    protected EntityService entityService;
    @Autowired
    protected StateService stateService;
        
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String loadItems(Model model){
        model.addAttribute("personObject", new PersonDto());
        model.addAttribute("password", new PasswordObject());
        model.addAttribute("persons", personService.findAll());
        model.addAttribute("states", stateService.findAll());
        return "index";
    }
    
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String submitFormHandler(@ModelAttribute PersonDto person, @ModelAttribute PasswordObject password){
        person.setStateId(1L); //TODO: replace with default state for entity type
        person.setRole(PersonRole.USER);
        personService.register(person, password.getValue());
        return "redirect:";
    }
    
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String completeItem(@RequestParam Long entityId){
        personService.delete(entityId);
        return "redirect:";
    }
    
    @RequestMapping(value = "/changeState", method = RequestMethod.GET)
    public String changeState(@RequestParam Long entityId, Long stateId) {
        entityService.updateState(entityId, stateId);
        return "redirect:";
    }
}
