package eu.bato.anyoffice.frontend.web.controllers;

import eu.bato.anyoffice.core.integration.hipchat.HipChatClient;
import eu.bato.anyoffice.serviceapi.dto.HipChatCredentials;
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
        }
        personService.save(currentPerson);
        return "redirect:/";
    }

}
