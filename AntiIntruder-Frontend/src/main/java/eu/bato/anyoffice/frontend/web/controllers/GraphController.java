/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.frontend.web.controllers;

import eu.bato.anyoffice.serviceapi.dto.PersonDto;
import eu.bato.anyoffice.serviceapi.dto.PersonRole;
import eu.bato.anyoffice.serviceapi.dto.PersonState;
import eu.bato.anyoffice.serviceapi.dto.StateSwitchDto;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author bryndza
 */
@Controller
public class GraphController extends CommonController {

    @ModelAttribute("page")
    public String module() {
        return "graph";
    }

    @RequestMapping(value = "/graph", method = RequestMethod.GET)
    public String loadItems(Model model, Authentication authentication, @RequestParam(required = false) String usernameGraph, @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm") Date from, @RequestParam(required = false) Integer next) {
        PersonDto currentPerson = personService.findOneByUsername(authentication.getName());
        model.addAttribute("currentPerson", currentPerson);

        //check input and prepare attributes
        if ((usernameGraph == null || usernameGraph.isEmpty()) || (!usernameGraph.equals(currentPerson.getUsername()) && !authentication.getAuthorities().contains(new SimpleGrantedAuthority(PersonRole.ADMIN.name())))) {
            usernameGraph = currentPerson.getUsername();
        }
        model.addAttribute("usernameGraph", usernameGraph);
        if (from == null) {
            from = getStartDate();
        }
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy H:mm");
        model.addAttribute("from", format.format(from));
        format = new SimpleDateFormat("dd.MM. H:mm");
        model.addAttribute("fromTime", format.format(from));
        if (next == null || (next != 12 && next != 24)) {
            next = 12;
        }
        model.addAttribute("next", next);

        Date to = new Date(from.getTime() + (next == 12 ? 43200000 : 86400000));
        Date now = new Date();
        if (to.after(now)) {
            to = now;
        }
        Calendar cFrom = new GregorianCalendar();
        cFrom.setTime(from);
        Calendar cTo = new GregorianCalendar();
        cTo.setTime(to);

        //get switches
        State[] computeStates = computeStates(usernameGraph, from, to);
        model.addAttribute("dailyStates", computeStates);

        //set start angle
        int startAngle = next == 12 ? 30 * cFrom.get(Calendar.HOUR) - 1 /*the start line*/ : 15 * cFrom.get(Calendar.HOUR_OF_DAY);
        model.addAttribute("startAngle", startAngle);

        model.addAttribute("total", next == 24 ? 86400000 : 43200000);

        return "graph";
    }

    /**
     * Computes series for chartist. Adds 1 minute long black field to the front
     * of the series and the time from beginning until first switch is grey.
     *
     * @param username
     * @param from
     * @param to
     * @return
     */
    private State[] computeStates(String username, Date from, Date to) {
        List<StateSwitchDto> stateSwitches = personService.getStateSwitches(username, from, to);
        if (stateSwitches.isEmpty()) {
            return new State[0];
        }
        State[] states = new State[stateSwitches.size() + 2];
        states[0] = new State();
        states[0].className = "graphblack";
        states[0].data = 60000L;
        Long previous = from.getTime() + 60000;
        states[1] = new State(PersonState.UNKNOWN);
        int i = 2;
        for (StateSwitchDto swich : stateSwitches) {
            states[i - 1].data = (swich.getTime().getTime() - previous);
            states[i++] = new State(swich.getState());
            previous = swich.getTime().getTime();
        }
        states[i - 1].data = ((to.getTime() - previous));
        return states;
    }

    private static Date getStartDate() {
        Calendar now = new GregorianCalendar();
        if (now.get(Calendar.HOUR_OF_DAY) > 19 || now.get(Calendar.HOUR_OF_DAY) < 7) { //if it's after 7PM or before 7AM, start 11 hours before now
            now.add(Calendar.HOUR, -11);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
        } else { //else start at 7AM
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.HOUR_OF_DAY, 7);
        }
        return now.getTime();
    }

    public class State {

        private Long data;
        private String className;

        public State() {
        }

        public State(PersonState state) {
            setState(state);
        }

        final void setDuration(Long duration) {
            this.data = duration;
        }

        final void setState(PersonState state) {
            switch (state) {
                case AVAILABLE:
                    this.className = "graphgreen";
                    break;
                case AWAY:
                    this.className = "graphyellow";
                    break;
                case DO_NOT_DISTURB:
                    this.className = "graphred";
                    break;
                default:
                    this.className = "graphgrey";
            }
        }

        public Long getData() {
            return data;
        }

        public String getClassName() {
            return className;
        }

        @Override
        public String toString() {
            return "State{" + "data=" + data + ", className=" + className + '}';
        }

    }
}
