package eu.bato.anyoffice.frontend.web.controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
    
    @Autowired
    Environment env;

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout(@ModelAttribute CsrfToken csrf) {
        return "redirect:/";
    }

    @RequestMapping(value = "/downloadClient", method = RequestMethod.GET)
    public void downloadClient(HttpServletResponse response) throws FileNotFoundException, IOException {
            try (InputStream is = new FileInputStream(env.getProperty("client.path"))) {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=AnyOffice_client.zip");
                org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
                response.flushBuffer();
                is.close();
            }
    }
//
//    @RequestMapping("/error")
//    public String notFound() {
//        return "errors/404";
//    }

}
