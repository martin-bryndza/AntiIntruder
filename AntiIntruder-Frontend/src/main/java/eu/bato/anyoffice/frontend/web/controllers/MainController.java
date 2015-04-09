package eu.bato.anyoffice.frontend.web.controllers;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Bato
 */
@Controller
public class MainController {

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout(@ModelAttribute CsrfToken csrf) {
        return "redirect:/";
    }

    @RequestMapping("/**")
    public String notFound() {
        return "errors/404";
    }

}
