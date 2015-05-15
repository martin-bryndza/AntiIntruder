package eu.bato.anyoffice.frontend.web.controllers;

import eu.bato.anyoffice.core.person.PersonStateManager;
import eu.bato.anyoffice.frontend.web.data.PasswordObject;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonRole;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.service.ResourceService;
import java.util.Date;
import java.util.List;
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
    protected ResourceService entityService;
    @Autowired
    protected PersonStateManager personStateManager;

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
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority(PersonRole.USER.name()))) {
                otherPersons.remove(currentPerson);
            }
        }
        model.addAttribute("persons", otherPersons);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String submitFormHandler(@ModelAttribute PersonDto person, @ModelAttribute PasswordObject password) {
        person.setRole(PersonRole.USER);
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
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority(PersonRole.USER.name()))) {
                personService.addInteractionEntity(auth.getName(), id);
            }
        }
    }

    @RequestMapping(value = "/cancelinteract", method = RequestMethod.POST)
    @ResponseBody
    public void cancelInteract(@RequestParam Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals("anonymousUser")) {
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority(PersonRole.USER.name()))) {
                personService.removeInteractionEntity(auth.getName(), id);
            }
        }
    }

    @RequestMapping(value = "/changeState", method = RequestMethod.GET)
    public String changeState(@RequestParam Long id, String state) {
        personStateManager.setState(id, PersonState.valueOf(state), true);
        return "redirect:";
    }
}
