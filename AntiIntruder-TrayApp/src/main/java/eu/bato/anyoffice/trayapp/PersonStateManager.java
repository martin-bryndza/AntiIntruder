/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import org.slf4j.LoggerFactory;

/**
 *
 * @author Bato
 */
class PersonStateManager {
    
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(PersonStateManager.class);

    private static PersonStateManager instance;

    private PersonState state;
    private final RestClient client;

    private PersonStateManager() {
        client = new RestClient();
        state = client.returnFromAway();
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

    PersonState setState(PersonState state) {
        if (this.state.equals(state)){
            return state;
        }
        log.info("State " + state + " sent to server.");
        this.state = client.setState(state);
        return this.state;
    }
    
    PersonState workstationLock(){
        this.state = client.goAway();
        return state;
    }
    
    PersonState workstationUnlock(){
        this.state = client.returnFromAway();
        return state;
    }

    boolean isStateChangePossible(PersonState newState) {
        return client.isStateChangePossible(newState);
    }

}
