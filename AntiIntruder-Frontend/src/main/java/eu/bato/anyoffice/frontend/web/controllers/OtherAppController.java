package eu.bato.anyoffice.frontend.web.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class OtherAppController extends CommonController {

    private static final Logger log = LoggerFactory.getLogger(OtherAppController.class);

    @ModelAttribute("app")
    @Override
    public String app() {
        return "other"; //will be defined in loadOtherApp
    }

    @RequestMapping(value = "/otherApp", method = RequestMethod.GET)
    public String loadOtherApp(Model model, @RequestParam String url, @RequestParam String app, @RequestParam boolean inframe) {
        if (inframe) {
            model.addAttribute("app", app);
            model.addAttribute("appUrl", url);
            return "otherApp";
        } else {
            return "redirect:" + url;
        }
    }

}
