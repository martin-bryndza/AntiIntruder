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
public class CurrentUser {
    
    private final Credentials credentials;
    
    private static CurrentUser instance;
    
    private CurrentUser(Credentials credentials){
        this.credentials = credentials;
    }
    
    public static CurrentUser getInstance(){
        if (instance == null){
            instance = new CurrentUser(TrayIconManager.getInstance().requestCredentials());
        }
        return instance;
    }

    public Credentials getCredentials() {
        return credentials;
    }
    
}
