/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.web;

import com.ysoft.tools.antiintruder.serviceapi.dto.EntityDto;
import com.ysoft.tools.antiintruder.serviceapi.service.EntityService;
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
    protected EntityService entityService;
    @Autowired
    protected StateService stateService;
        
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String loadItems(Model model){
        EntityDto prefillDto = new EntityDto();
        prefillDto.setDescription("aaUsername");
        prefillDto.setDisplayName("aaDisplayName");
        model.addAttribute("prefillEntity", prefillDto); // object bindovany na formular - moze byt predvyplneny
        model.addAttribute("entities", entityService.findAll());
        model.addAttribute("states", stateService.findAll());
        return "index";
    }
    
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String submitFormHandler(@ModelAttribute EntityDto entity){
        entity.setStateId(1L); //TODO: replace with default state for entity type
        entityService.save(entity);
        return "redirect:";
    }
    
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String completeItem(@RequestParam Long entityId){
        entityService.delete(entityId);
        return "redirect:";
    }
    
    @RequestMapping(value = "/changeState", method = RequestMethod.POST)
    public String changeState(@ModelAttribute EntityDto entity) {
        entityService.save(entity);
        return "redirect:";
    }
}
