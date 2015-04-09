/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.backend.config;

import eu.bato.anyoffice.backend.dao.StateDao;
import eu.bato.anyoffice.backend.model.State;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Bato
 */
@Component
public class InitialImport {

    @Autowired
    StateDao stateDao;

    @PostConstruct
    public void run() {
        State state = new State();
        state.setName("AVAILABLE");
        state.setMinDuration(0L);
        state.setMaxDuration(0L);
        state.setDefaultSuccessor(state);
        stateDao.save(state);
    }

}
