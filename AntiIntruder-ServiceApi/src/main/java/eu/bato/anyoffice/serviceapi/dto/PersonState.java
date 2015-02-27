/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.serviceapi.dto;

/**
 *
 * @author Bato
 */
public enum PersonState {
    
    DO_NOT_DISTURB("Do not disturb", false, true), AVAILABLE("Available", false, false), AWAY_DND("Away", true, true), AWAY_AVAILABLE("Away", true, false);
    
    private final String name;
    private final Boolean awayState;
    private final Boolean dndState;

    private PersonState(String name, Boolean isAwayState, Boolean dndState) {
        this.name = name;
        this.awayState = isAwayState;
        this.dndState = dndState;
    }

    public String getName() {
        return name;
    }

    public Boolean isAwayState() {
        return awayState;
    }

    public Boolean isDndState() {
        return dndState;
    }
    
}
