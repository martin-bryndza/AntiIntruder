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

    DO_NOT_DISTURB, AVAILABLE, AWAY,
    /**
     * If the TrayApp has not contacted the server for some time.
     */
    UNKNOWN;

    public String getName() {
        return name();
    }
}
