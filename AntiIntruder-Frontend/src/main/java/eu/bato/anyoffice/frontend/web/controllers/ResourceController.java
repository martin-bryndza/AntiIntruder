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
        person.setRole(PersonRole.USER);
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
