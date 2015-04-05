package eu.bato.anyoffice.frontend.web.controllers;

import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
public class PersonController {
    
    @Autowired
    protected PersonService personService;
    
    @RequestMapping(value = "/personEdit", method = RequestMethod.GET)
    public String loadItems(Model model, Authentication authentication) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PersonDto currentPerson = personService.findOneByUsername(currentUser.getUsername());
        model.addAttribute("personObject", currentPerson);
        return "personEdit";
    }

    @RequestMapping(value = "/personEdit/save", method = RequestMethod.POST)
    public String submitFormHandler(@ModelAttribute PersonDto person) {
        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PersonDto currentPerson = personService.findOneByUsername(currentUser.getUsername());
        currentPerson.setLocation(person.getLocation());
        currentPerson.setDescription(person.getDescription());
        currentPerson.setDisplayName(person.getDisplayName());
        personService.save(currentPerson);
        return "redirect:/";
    }
    
}
