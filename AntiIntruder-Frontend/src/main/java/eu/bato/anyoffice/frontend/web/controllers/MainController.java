package eu.bato.anyoffice.frontend.web.controllers;

import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Bato
 */
@Controller
public class MainController extends CommonController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout(@ModelAttribute CsrfToken csrf) {
        return "redirect:/";
    }

    @RequestMapping(value = "/downloadClient", method = RequestMethod.GET)
    public void downloadClient(HttpServletResponse response) throws FileNotFoundException, IOException {
        try (InputStream is = new FileInputStream(env.getProperty("client.path"))) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=AnyOffice_client.zip");
            try {
                org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            } catch (IOException e) {
                log.info("User cancelled request to download client.");
            } finally {
                response.flushBuffer();
                is.close();
            }
        }
    }
//
//    @RequestMapping("/error")
//    public String notFound() {
//        return "errors/404";
//    }

    @RequestMapping(value = "/faq", method = RequestMethod.GET)
    public String loadFaq(Model model) {
        model.addAttribute("page", "faq");
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
            Authentication auth = (Authentication) SecurityContextHolder.getContext().getAuthentication();
            PersonDto currentPerson = personService.findOneByUsername(auth.getName());
            model.addAttribute("currentPerson", currentPerson);
        }
        return "faq";
    }

}
