/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

/**
 *
 * @author Bato
 */
class PersonStateManager {
    
    private static PersonStateManager instance;
    
    private PersonState state;
    
    private PersonStateManager(){
        state = PersonState.UNKNOWN; //TODO:ask server
    }
    
    static PersonStateManager getInstance(){
        if (instance == null){
            instance = new PersonStateManager();
        }
        return instance;
    }

    PersonState getState() {
        return state;
    }

    void setState(PersonState state) {
        if (isStateChangePossible(state)){
            this.state = state;
        } else {
            throw new IllegalArgumentException("Not possible to switch to state " + state);
        }
    }
    
    boolean isStateChangePossible(PersonState newState){
        return true; //TODO: ask server
    }
    
}
