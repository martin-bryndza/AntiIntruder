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
public enum PersonState {
        
    DO_NOT_DISTURB("Do not disturb", "dnd.png"), AVAILABLE("Available", "available.png"), UNKNOWN("Unknown", "unknown.png");
    
    private static final String PREPOSITION = "Any Office - ";
    private static final String IMAGE_FOLDER = "images/";
    private final String name;
    private final String icon;

    private PersonState(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return PREPOSITION + name;
    }

    public String getIconPath() {
        return IMAGE_FOLDER + icon;
    }
    
}
