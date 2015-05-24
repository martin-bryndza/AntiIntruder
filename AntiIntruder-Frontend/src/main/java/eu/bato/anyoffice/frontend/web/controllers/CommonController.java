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

import eu.bato.anyoffice.frontend.web.data.OtherPageObject;
import eu.bato.anyoffice.serviceapi.service.PersonService;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

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
        apps.add(new OtherPageObject("Licence", "https://license.ysoft.local/", true));
        apps.add(new OtherPageObject("VMware", "https://vcac.ysoft.local/vcac/", false));
        apps.add(new OtherPageObject("Bamboo", "http://bamboo.ysoft.local/allPlans.action", false));
        apps.add(new OtherPageObject("Jira", "http://jira.ysoft.local", false));
        apps.add(new OtherPageObject("Intranet", "http://intranet.ysoft.local", false));
        apps.add(new OtherPageObject("SQ5 (150)", "http://10.0.10.150", false));
        apps.add(new OtherPageObject("SQ6 (120)", "http://10.0.10.150", false));
        return apps;
    }

    @ModelAttribute("app")
    public String app() {
        return "anyoffice";
    }

}
