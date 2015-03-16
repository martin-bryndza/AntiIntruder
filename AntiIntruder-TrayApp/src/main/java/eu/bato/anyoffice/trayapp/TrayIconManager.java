/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import java.awt.AWTException;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Bato
 */
class TrayIconManager {

    final static Logger log = LoggerFactory.getLogger(TrayIconManager.class);
    
    private static TrayIconManager instance;
    
    private TrayIcon trayIcon;
    private PopupMenu popup;
    private Map<PersonState, MenuItem> stateItems;
    private PersonState currentState;
    private final UpdateIconMouseListener updateIconMouseListener;
    
    private static final Font BOLD_FONT = Font.decode(null).deriveFont(java.awt.Font.BOLD);

    private TrayIconManager() {
        updateIconMouseListener = new UpdateIconMouseListener();
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
        PersonState state = currentState; // create a local copy
        if (!SystemTray.isSupported()) {
            log.error("SystemTray is not supported on this system.");
            return;
        }
        SystemTray tray = SystemTray.getSystemTray();
        if (trayIcon == null){
            trayIcon = createIcon(state.getIconPath(), state.getDescription());
            try {
                tray.add(trayIcon);
            } catch (AWTException ex) {
                log.error("Desktop system tray is missing", ex);
            }
        } else {
            trayIcon.setImage(getTrayIconImage(state.getIconPath()));
        }
        trayIcon.setPopupMenu(createMenu(state));
        trayIcon.addMouseListener(updateIconMouseListener);
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
            if (state.isAwayState()){
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
        TrayIcon icon = new TrayIcon(getTrayIconImage(path), description);
        
        icon.addActionListener((ActionEvent) -> {
            icon.displayMessage(icon.getToolTip(), "Remaining time: ", TrayIcon.MessageType.NONE);
        });
        
        return icon;
    }
    
    private Image getTrayIconImage(String path){
        BufferedImage trayIconImage;
        try {
            trayIconImage = ImageIO.read(new File(path));
        } catch (IOException ex) {
            log.error("Icon " + path + " not found.", ex);
            return new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        }
        if (trayIconImage == null) {
            log.error("Unable to create tray icon");
            return new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        }
        int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
        return trayIconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH);
    }
        
    /**
     * Change of state and/or refresh of tray icon. If the current and requested states are the same and state availability hasn't changed either, nothing will be changed. Do not use this to update menu before displaying it, use updateIcon instead.
     * @param state 
     */
    synchronized void updateState(PersonState state){
        boolean wasDndAvailable = stateItems.get(PersonState.DO_NOT_DISTURB).isEnabled();
        if (!wasDndAvailable && PersonStateManager.getInstance().isStateChangePossible(PersonState.DO_NOT_DISTURB)) {
            stateItems.get(PersonState.DO_NOT_DISTURB).setEnabled(true);
            int result = JOptionPane.showConfirmDialog(null, "Go to Do not disturb state now?", "Do not disturb state is possible", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                changeState(PersonState.DO_NOT_DISTURB);
                return;
            }
        }
        if (state == null || state.equals(currentState)){
            return;
        }
        log.info("Updating icon to " + state);
        boolean showAvailableBubble = state.equals(PersonState.AVAILABLE) && !currentState.isAwayState();
        updateIcon(state);
        if (showAvailableBubble){
            showBubble("You have gone Available");
        }
    }
    
    /**
     * Update of visual components to correspond to the given state.
     * @param state 
     */
    synchronized void updateIcon(PersonState state){
        currentState = state;
        initialize();
    }
    
    private void showBubble(String text){
        trayIcon.displayMessage("", text, TrayIcon.MessageType.INFO);
    }
    
    private void changeState(PersonState state){
        if (state == null || state.equals(currentState)) {
            return;
        }
        log.info("Changing state to " + state);
        if (!PersonStateManager.getInstance().isStateChangePossible(state)) {
            trayIcon.displayMessage("Unable to switch to " + state.getDisplayName(), "", TrayIcon.MessageType.ERROR);
        }
        PersonState newStateByServer = PersonStateManager.getInstance().setState(state);
        log.debug("Server returned state " + newStateByServer + " after state change to " + state);
        updateState(newStateByServer);
    }
    
    Credentials requestCredentials(){
        JTextField field1 = new JTextField(Configuration.getInstance().getProperty(Property.CURRENT_USER));
        JTextField field2 = new JTextField(Configuration.getInstance().getProperty(Property.CURRENT_PASSWORD));
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Username:"));
        panel.add(field1);
        panel.add(new JLabel("Password:"));
        panel.add(field2);
        field1.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
                field1.requestFocusInWindow();
            }
        });
        int result = JOptionPane.showConfirmDialog(null, panel, "Please log in",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Credentials c;
            try {
                c = new Credentials(field1.getText(), field2.getText().toCharArray());
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(null, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                return requestCredentials();
            }
            if (new RestClient().isCorrectCredentials(c)){
                Configuration.getInstance().setProperty(Property.CURRENT_USER, field1.getText());
                return c;
            } else {
                JOptionPane.showMessageDialog(null, "Incorrect password or unknown user.", "Authentication failed", JOptionPane.ERROR_MESSAGE);
                return requestCredentials();
            }
        } else {
            JOptionPane.showMessageDialog(null, "No credentials were provided. Application will exit now.", "Cancelled", JOptionPane.WARNING_MESSAGE);
            Main.programFinish();
            return null;
        }
    }
    
    private class UpdateIconMouseListener extends MouseAdapter {
        
        private boolean released = true;
            
        @Override
        public void mousePressed(MouseEvent e) {
            if (released){
                updateIcon(PersonStateManager.getInstance().getStateFromServer());
                released = false;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            released = true;
        }
        
        

    }
}
