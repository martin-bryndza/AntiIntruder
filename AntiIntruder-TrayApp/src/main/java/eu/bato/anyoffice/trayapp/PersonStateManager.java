/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Bato
 */
class PersonStateManager {
    
    final static org.slf4j.Logger log = LoggerFactory.getLogger(PersonStateManager.class);

    private static PersonStateManager instance;

    private PersonState state;
    private final RestClient client;

    private PersonStateManager() {
        client = new RestClient();
        client.login();
        getStateFromServer();
    }

    static PersonStateManager getInstance() {
        if (instance == null) {
            instance = new PersonStateManager();
        }
        return instance;
    }

    PersonState getStateFromServer() {
        PersonState newState = client.getState();
        if (!this.state.equals(newState)){
            log.info("New state from server: " + newState);
            this.state = newState;
        }
        return this.state;
    }

    void setState(PersonState state) {
        if (this.state.equals(state)){
            return;
        }
        this.state = state;
        log.info("State " + state + " sent to server.");
        // TODO: tell server
    }
    
    void workstationLock(){
        //TODO: tell server about lock
    }
    
    PersonState workstationUnlock(){
        //TODO: tell server about unlock and get current newState
        return PersonState.AVAILABLE;
    }

    boolean isStateChangePossible(PersonState newState) {
        return true; //TODO: ask server
    }

}
