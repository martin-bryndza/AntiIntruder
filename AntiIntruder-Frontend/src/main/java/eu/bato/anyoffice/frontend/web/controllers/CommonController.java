package eu.bato.anyoffice.frontend.web.controllers;

import eu.bato.anyoffice.frontend.web.data.OtherPageObject;
import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
public class CommonController {

    @Autowired
    Environment env;

    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    protected PersonService personService;

    @ModelAttribute("otherApps")
    public List<OtherPageObject> otherApps() {
        List<OtherPageObject> apps = new LinkedList<>();
        apps.add(new OtherPageObject("Booking", "https://booking.ysoft.local/", false));
        apps.add(new OtherPageObject("Download Manager", "http://bamboo.ysoft.local/dm", false));
        apps.add(new OtherPageObject("Licence Tool", "https://license.ysoft.local/", true));
        apps.add(new OtherPageObject("Bamboo", "http://bamboo.ysoft.local/allPlans.action", false));
        apps.add(new OtherPageObject("Jira", "http://jira.ysoft.local", false));
        apps.add(new OtherPageObject("Intranet", "http://intranet.ysoft.local", false));
        apps.add(new OtherPageObject("10.0.10.150", "http://10.0.10.150", false));
        return apps;
    }

    @ModelAttribute("app")
    public String app() {
        return "anyoffice";
    }

}
