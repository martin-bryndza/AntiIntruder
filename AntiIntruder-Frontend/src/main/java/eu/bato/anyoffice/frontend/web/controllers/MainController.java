/* 
 * Copyright (c) 2015, Martin Bryndza
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package eu.bato.anyoffice.frontend.web.controllers;

import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            response.setContentType("application/jar");
            response.setHeader("Content-disposition", "attachment;filename=anyoffice-client.jar");
            int read;
            byte[] bytes = new byte[1024];
            try (OutputStream os = response.getOutputStream()) {
                while ((read = is.read(bytes)) != -1) {
                    os.write(bytes, 0, read);
                }
                os.flush();
            }
        }
    }

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
