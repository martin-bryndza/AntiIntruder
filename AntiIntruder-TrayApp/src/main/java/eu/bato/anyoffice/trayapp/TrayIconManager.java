/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Bato
 */
class TrayIconManager {

    final static org.slf4j.Logger log = LoggerFactory.getLogger(TrayIconManager.class);
    
    private static TrayIconManager instance;
    
    private TrayIcon trayIcon;
    private PopupMenu popup;
    private Map<PersonState, MenuItem> stateItems;
    private PersonState currentState;
    
    private static final Font BOLD_FONT = Font.decode(null).deriveFont(java.awt.Font.BOLD);

    private TrayIconManager() {
        
    }
    
    static TrayIconManager getInstance(){
        if (instance == null) {
            instance = new TrayIconManager();
        }
        return instance;
    }
    
    synchronized void initialize(){
        popup = new PopupMenu("Any Office");
        stateItems = new HashMap<>();
        if (currentState == null){
            currentState = PersonStateManager.getInstance().getStateFromServer();
        }
        PersonState state = currentState;
        if (!SystemTray.isSupported()) {
            log.error("SystemTray is not supported on this system.");
            return;
        }
        SystemTray tray = SystemTray.getSystemTray();
        trayIcon = createIcon(state.getIconPath(), state.getDescription());
        trayIcon.setPopupMenu(createMenu(state));
        try {
            tray.add(trayIcon);
        } catch (AWTException ex) {
            log.error("Desktop system tray is missing", ex);
        }
    }
    
    private PopupMenu createMenu(PersonState currentState){
//        MenuItem aboutItem = new MenuItem("About");
//        CheckboxMenuItem cb1 = new CheckboxMenuItem("Set auto size");
//        CheckboxMenuItem cb2 = new CheckboxMenuItem("Set tooltip");
//        Menu displayMenu = new Menu("Display");
//        MenuItem errorItem = new MenuItem("Error");
//        MenuItem warningItem = new MenuItem("Warning");
//        MenuItem infoItem = new MenuItem("Info");
//        MenuItem noneItem = new MenuItem("None");
        
        for (PersonState state: PersonState.values()){
            if (state.equals(PersonState.UNKNOWN)){
                continue;
            }
            MenuItem item = new MenuItem(state.getDisplayName());
            if (state.equals(currentState)){
                item.setFont(BOLD_FONT);
                item.setLabel("-" + item.getLabel() + "-");
            } else if (!PersonStateManager.getInstance().isStateChangePossible(state)){
                item.setEnabled(false);
            } else {               
                item.addActionListener((ActionEvent) -> {
                    log.info("State change by user -> " + state);
                    changeState(state);
                });
            }
            stateItems.put(state, item);
            popup.add(item);
        }
        
        popup.addSeparator();
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener((ActionEvent) -> {
            Main.programFinish();
        });
        popup.add(exitItem);

        return popup;
    }

    private TrayIcon createIcon(String path, String description) {
        BufferedImage trayIconImage;
        try {
            trayIconImage = ImageIO.read(new File(path));
        } catch (IOException ex) {
            log.error("Icon " + path + " not found.", ex);
            return new TrayIcon(new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB), description);
        }
        if (trayIconImage == null) {
            log.error("Unable to create tray icon");
            return new TrayIcon(new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB), description);
        }
        int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
        TrayIcon icon = new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH), description);
        
        icon.addActionListener((ActionEvent) -> {
            icon.displayMessage(icon.getToolTip(), "Remaining time: ", TrayIcon.MessageType.NONE);
        });
        
        return icon;
    }
        
    /**
     * Change of state and refresh of tray icon.
     * @param state 
     */
    synchronized void changeState(PersonState state){
        if (state == null || state.equals(currentState)){
            return;
        }
        log.info("Changing state to " + state);
        if (!PersonStateManager.getInstance().isStateChangePossible(state)){
            trayIcon.displayMessage("Unable to switch to " + state.getDisplayName(), "Remaining time: ", TrayIcon.MessageType.ERROR);
        }
        PersonStateManager.getInstance().setState(state);
        currentState = state;
        SystemTray.getSystemTray().remove(trayIcon);
        initialize();
    }
}
