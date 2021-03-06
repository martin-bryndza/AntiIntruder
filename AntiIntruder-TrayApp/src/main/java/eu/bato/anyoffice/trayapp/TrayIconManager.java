/* 
 * Copyright (c) 2015, Martin Bryndza
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import eu.bato.anyoffice.trayapp.entities.InteractionPerson;
import eu.bato.anyoffice.trayapp.entities.PersonLocation;
import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;

/**
 *
 * @author Bato
 */
class TrayIconManager {

    final static Logger log = LoggerFactory.getLogger(TrayIconManager.class);

    private static TrayIconManager instance;

    private TrayIcon trayIcon;
    private final Map<PersonState, MenuItem> statesMenuItems;
    CheckboxMenuItem dndSwitchOnceChkboxMenuItem;
    private final UpdateIconMouseListener updateIconMouseListener;

    private final SwitchToStateFrame switchToDndFrame;
    private final AvailableConsultersMessageFrame availableConsultersMessageFrame;
    private final DndCustomPeriodFrame dndCustomPeriodFrame;

    private final RestClient client;

    private PersonState currentState;
    private String currentLocation;
    private boolean locked;

    private static final Font BOLD_FONT = Font.decode(null).deriveFont(java.awt.Font.BOLD);
    private Image icon = null;

    private TrayIconManager() {
        icon = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("images/logo.png"));
        updateIconMouseListener = new UpdateIconMouseListener();
        switchToDndFrame = new SwitchToStateFrame();
        availableConsultersMessageFrame = new AvailableConsultersMessageFrame();
        dndCustomPeriodFrame = new DndCustomPeriodFrame();
        statesMenuItems = new HashMap<>();
        String authString = Configuration.getInstance().getProperty(Property.GUID);
        if (authString.isEmpty() || !RestClient.isCorrectCredentials(new Credentials(authString))) {
            client = new RestClient(requestCredentials(false));
        } else {
            client = new RestClient(new Credentials(authString));
        }
        client.ping(); //initial ping to server
        currentState = client.returnFromAway();
        currentLocation = client.getLocation();
        if (currentLocation == null || currentLocation.isEmpty()) {
            currentLocation = "-"; // to avoid displaying null or empty string in menu
        }
        initializeTrayIcon(currentState, currentLocation);
        //if auto switch is turned on, DND is not active and it is possible to switch to DND
        if (Configuration.getInstance().getBooleanProperty(Property.STATE_AUTO_SWITCH)
                && !currentState.equals(PersonState.DO_NOT_DISTURB)
                && client.isStateChangePossible(PersonState.DO_NOT_DISTURB)) {
            startDndAutoSwitchTimeoutProcedure();
        }
    }

    /**
     * The same as getInstance. Method for semantical purposes.
     *
     * @return initialized TrayIconManager
     */
    static TrayIconManager initialize() {
        return getInstance();
    }

    /**
     * Initializes the GUI. Asks for credentials, send unlock message to server
     * and displays tray icon according to state returned by server.
     *
     * @return initialized TrayIconManager
     */
    static TrayIconManager getInstance() {
        if (instance == null) {
            instance = new TrayIconManager();
        }
        return instance;
    }

    /**
     * Gets the current person state set in the GUI.
     *
     * @return the currently set person state
     */
    public PersonState getCurrentState() {
        return currentState;
    }

    /**
     * Gets the current location set in the GUI.
     *
     * @return the currently set location
     */
    private String getCurrentLocation() {
        return currentLocation;
    }

    private void setCurrentLocation(String location) {
        this.currentLocation = location;
    }

    /**
     * Initializes or reinitializes tray icon and its menu.
     *
     * @param currentState the person state, according to which the components
     * will be initialized
     * @param currentLocation the location to display in the menu
     */
    private synchronized void initializeTrayIcon(PersonState currentState, String currentLocation) {
        log.debug("Initializing visual components with state {} and location {}", currentState, currentLocation);
        if (!SystemTray.isSupported()) {
            log.error("SystemTray is not supported on this system.");
            showInfoMessage("Error", "This operating system is not supported.");
            close();
            return;
        }
        SystemTray tray = SystemTray.getSystemTray();
        if (trayIcon == null) {
            // initialize icon ...
            trayIcon = createIcon(currentState.getIcon());
            try {
                tray.add(trayIcon);
            } catch (AWTException ex) {
                log.error("Desktop system tray is missing", ex);
            }
            if (Configuration.getInstance().getBooleanProperty(Property.FIRST_RUN)) {
                showInfoBubble("Welcome!\nRight-click this icon to change your current state.");
                Configuration.getInstance().setProperty(Property.FIRST_RUN, "false");
            }
        } else {
            // reinitialize icon
            trayIcon.setImage(getTrayIconImage(currentState.getIcon()));
            trayIcon.setToolTip(currentState.getDescription());
            ActionListener[] actionListeners = trayIcon.getActionListeners();
            for (ActionListener a : actionListeners) {
                trayIcon.removeActionListener(a);
            }
        }
        trayIcon.setPopupMenu(createMenu(currentState, currentLocation));
        trayIcon.addMouseListener(updateIconMouseListener);

        //on double click display state switch message or information about remaining time
        trayIcon.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (getCurrentState().equals(PersonState.AVAILABLE)) {
                    if (statesMenuItems.get(PersonState.DO_NOT_DISTURB).isEnabled()) {
                        switchToDndFrame.display(PersonState.DO_NOT_DISTURB);
                    } else {
                        Long current = new Date().getTime();
                        Long start = client.getDndStart();
                        showInfoMessage("AnyOffice", "Do Not Disturb will be available in " + millisToMins(start - current) + " minutes.");
                    }
                } else if (getCurrentState().equals(PersonState.DO_NOT_DISTURB)) {
                    switchToDndFrame.display(PersonState.AVAILABLE);
                }
            }
        });
    }

    /**
     * Send ping message to server.
     */
    void pingServer() {
        client.ping();
    }

    /**
     * Checks server for updates of person state and location, and updates the
     * GUI accordingly.
     */
    synchronized void updateFromServer() {
        // to prevent several requests to unreachable server
        if (!RestClient.isServerOnline()) {
            currentState = PersonState.UNKNOWN;
            initializeTrayIcon(currentState, currentLocation);
            return;
        }

        MenuItem dnd = statesMenuItems.get(PersonState.DO_NOT_DISTURB);
        boolean wasDndAvailable = dnd != null && dnd.isEnabled();
        //if DND is newly available
        if (!wasDndAvailable && client.isStateChangePossible(PersonState.DO_NOT_DISTURB) && currentState.equals(PersonState.AVAILABLE)) {
            initializeTrayIcon(currentState, currentLocation);
            log.debug("DND is now enabled");
            if (Configuration.getInstance().getBooleanProperty(Property.STATE_AUTO_SWITCH)) { // if DND autoswitch is selected
                startDndAutoSwitchTimeoutProcedure();
            } else if (dndSwitchOnceChkboxMenuItem != null && dndSwitchOnceChkboxMenuItem.getState()) { //if autoswitch to DND once is selected
                changeState(PersonState.DO_NOT_DISTURB);
                showInfoBubble("You are in Do Not Disturb state now.");
                dndSwitchOnceChkboxMenuItem.setState(false);
                return;
            } else {
                showInfoBubble(PersonState.DO_NOT_DISTURB.getDisplayName() + " state is possible now.");
            }
        }

        PersonState newState = client.getState();

        //if for some reason server thinks that client is offline or locked
        if (newState.equals(PersonState.UNKNOWN) || (newState.equals(PersonState.AWAY) && !locked)) {
            log.warn("Server returned {} state and the machine is {}locked. Sending machine unlock message...", newState, locked ? "" : "not ");
            pingServer();
            lock(locked);
            return; // updateFromServer is called again in the lock method
        }

        String newLocation = client.getLocation();

        showPendingConsultationsPopup(newState);

        Long current = new Date().getTime();

        //change tool tip message of tray icon and states menu items accordingly (due to elapsed time)
        if (newState.equals(PersonState.DO_NOT_DISTURB)) {
            Long end = client.getDndEnd() - current;
            trayIcon.setToolTip("Do not disturb will end in " + millisToMins(end) + " minutes.");
            statesMenuItems.get(PersonState.DO_NOT_DISTURB).setLabel(PersonState.DO_NOT_DISTURB.getDisplayName() + " (ends in " + millisToMins(end) + " min.)");
        } else if (newState.equals(PersonState.AVAILABLE)) {
            Long start = client.getDndStart();
            if (start > current) {
                trayIcon.setToolTip("Do not disturb will be available in " + millisToMins(start - current) + " minutes.");
                statesMenuItems.get(PersonState.DO_NOT_DISTURB).setLabel(PersonState.DO_NOT_DISTURB.getDisplayName() + " (in " + millisToMins(start - current) + " min.)");
            } else {
                trayIcon.setToolTip(PersonState.AVAILABLE.getDescription());
            }
        } else {
            trayIcon.setToolTip(newState.getDescription());
        }

        //check if any change of visual components is necessary
        if (newState.equals(currentState) && newLocation.equals(currentLocation)) {
            return;
        }

        log.debug("Updating icon to state {}, location {}", newState, newLocation);
        boolean showAvailableBubble = newState.equals(PersonState.AVAILABLE) && currentState.equals(PersonState.DO_NOT_DISTURB);
        currentState = newState;
        currentLocation = newLocation;
        initializeTrayIcon(currentState, currentLocation);
        //show bubble after the icon has been reinitialized, otherwise it would be closed by the reinitialization
        if (showAvailableBubble) {
            int requests = client.getNumberOfRequests();
            String requestsMsg = requests != 0
                    ? (" You have " + requests + " pending " + toPlural("request", requests) + " for consultation.")
                    : "";
            showInfoBubble("You have gone Available." + requestsMsg);
        }
    }

    private static String toPlural(String string, int count) {
        return string + (count > 1 ? "s" : "");
    }

    private static long millisToMins(Long end) {
        return end / 60000;
    }

    private void showPendingConsultationsPopup(PersonState newState) {
        if (!newState.isAwayState()) {
            List<InteractionPerson> availableConsulters = client.getCurrentIncomingConsultations();
            if (!availableConsulters.isEmpty()) {
                availableConsultersMessageFrame.showAvailableConsultersMessage(availableConsulters);
            }
            // if I can see a consultation request means, that I am or I've just recently been AVAILABLE
            int requests = client.getNumberOfRequests();
            if (requests > 0) {
                showInfoBubble(" You have " + requests + " pending request" + (requests > 1 ? "s" : "") + " for consultation.");
            }
        }
    }

    private void startDndAutoSwitchTimeoutProcedure() {
        //show bubble, add action to dismiss, start timer to autoswitch
        showInfoBubble("Do Not Disturb will be set in 20 seconds. Double-click this icon to dismiss.");
        final AutoDndSwitchWaitThread r = new AutoDndSwitchWaitThread();
        new Thread(r).start();
        final ActionListener[] actionListeners = trayIcon.getActionListeners();
        for (ActionListener l : actionListeners) {
            trayIcon.removeActionListener(l);
        }
        trayIcon.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                r.setWait(true);
                int result = JOptionPane.showConfirmDialog(null, "Turn off automatic switching of states?", "AnyOffice - Dismiss autoswitch",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    Configuration.getInstance().setProperty(Property.STATE_AUTO_SWITCH, "false");
                    trayIcon.removeActionListener(this);
                    for (ActionListener l : actionListeners) {
                        trayIcon.addActionListener(l);
                    }
                }
                r.setWait(false);
            }
        });
    }

    private class AutoDndSwitchWaitThread implements Runnable {

        private boolean wait = false;

        public void setWait(boolean wait) {
            this.wait = wait;
        }

        @Override
        public void run() {
            Configuration conf = Configuration.getInstance();
            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    //does not matter
                }
                if (wait) {
                    i--;
                }
                if (!conf.getBooleanProperty(Property.STATE_AUTO_SWITCH)) {
                    return;
                }
            }
            changeState(PersonState.DO_NOT_DISTURB);
        }
    }

    /**
     * Sends info about machine lock/unlock and updates the current state.
     *
     * @param lock true to lock, false to unlock
     */
    synchronized void lock(boolean lock) {
        if (lock) {
            client.goAway();
            currentState = PersonState.AWAY;
            locked = true;
        } else {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    // try to send the (un)lock message until successful (due to limited connectivity of notebooks after unlock)
                    while (true) {
                        try {
                            client.returnFromAway(true);
                            break;
                        } catch (RestClientException e) {
                            log.warn("Server is offline. Send machine unlock notification failed.");
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ex) {
                                //OK
                            }
                            log.info("Retry send machine unlock notification.");
                        }
                    }
                }
            };
            Thread t = new Thread(r);
            t.start();
            locked = false;
            updateFromServer();
        }
        log.debug("Session {}locked", lock ? "" : "un");
    }

    /**
     * Hides the GUI, switches to UNKNOWN state
     */
    void close() {
        if (!RestClient.isServerOnline()) {
            System.exit(0);
        }
        SystemTray.getSystemTray().remove(trayIcon);
        client.setState(PersonState.UNKNOWN);
    }

    /**
     * Creates new menu for given state and location. <br />
     * The menu contains: <br />
     * - Exit button <br />
     * - Settings... button <br />
     * - State autoswitch menu <br />
     * - "Disturbed by" menu <br />
     * - Set location... button <br />
     * - Go to web page ... button <br />
     * - Buttons to switch to all states from PersonState enum <br />
     * The button for current state is in bold. The buttons for currently
     * unavailable states are disabled. For each disabled button there is a
     * check box item to switch to the state as soon as possible. These switches
     * have to be done elsewhere in code. States buttons are saved in map
     * statesMenuItems. Check box items are saved in map chkBoxStateItems.
     *
     * @param currentState
     * @param currentLocation
     * @return
     */
    private PopupMenu createMenu(PersonState currentState, final String currentLocation) {
        PopupMenu popup = new PopupMenu("Any Office");

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Main.programFinish();
            }
        });
        popup.add(exitItem);

        popup.addSeparator();

        MenuItem settingsItem = new MenuItem("Settings...");
        settingsItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showSettings();
            }
        });
        popup.add(settingsItem);

        Menu dndSwitchMenu = new Menu("Automatically switch to " + PersonState.DO_NOT_DISTURB.getDisplayName());
        boolean chkBoxDndChecked = dndSwitchOnceChkboxMenuItem != null && dndSwitchOnceChkboxMenuItem.getState() && !Configuration.getInstance().getBooleanProperty(Property.STATE_AUTO_SWITCH);
        dndSwitchOnceChkboxMenuItem = new CheckboxMenuItem("next time when possible", chkBoxDndChecked);
        dndSwitchMenu.add(dndSwitchOnceChkboxMenuItem);
        final CheckboxMenuItem dndAlwaysChkboxMenuItem = new CheckboxMenuItem("always when possible", Configuration.getInstance().getBooleanProperty(Property.STATE_AUTO_SWITCH));
        if (dndAlwaysChkboxMenuItem.getState()) {
            dndSwitchOnceChkboxMenuItem.setState(false);
        }
        dndSwitchOnceChkboxMenuItem.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                if (selected) {
                    dndAlwaysChkboxMenuItem.setState(false);
                    Configuration.getInstance().setProperty(Property.STATE_AUTO_SWITCH, "false");
                }
                if (selected
                        && getCurrentState().equals(PersonState.AVAILABLE)
                        && client.isStateChangePossible(PersonState.DO_NOT_DISTURB)) {
                    changeState(PersonState.DO_NOT_DISTURB);
                }
            }
        });
        dndAlwaysChkboxMenuItem.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                if (selected) {
                    dndSwitchOnceChkboxMenuItem.setState(false);
                }
                Configuration.getInstance().setProperty(Property.STATE_AUTO_SWITCH, String.valueOf(selected));
                //switch right away if possible
                if (selected && getCurrentState().equals(PersonState.AVAILABLE) && client.isStateChangePossible(PersonState.DO_NOT_DISTURB)) {
                    changeState(PersonState.DO_NOT_DISTURB);
                }
            }
        });
        dndSwitchMenu.add(dndAlwaysChkboxMenuItem);
        popup.add(dndSwitchMenu);

        popup.addSeparator();

        if (RestClient.isServerOnline()) {
            Menu disturbanceMenu = new Menu("Disturbed by");
            MenuItem aoUserMenuItem = new MenuItem("AnyOffice user");
            aoUserMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    client.noteDisturbance(true);
                }
            });
            disturbanceMenu.add(aoUserMenuItem);
            MenuItem notAoUserMenuItem = new MenuItem("Not a user");
            notAoUserMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    client.noteDisturbance(false);
                }
            });
            disturbanceMenu.add(notAoUserMenuItem);
            MenuItem dkMenuItem = new MenuItem("Somebody (do not know)");
            dkMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    client.noteDisturbance(null);
                }
            });
            disturbanceMenu.add(dkMenuItem);
            popup.add(disturbanceMenu);

            MenuItem browserMenuItem = new MenuItem("Go to web page...");
            browserMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        openWebpage(new URL(Configuration.getInstance().getProperty(Property.WEB_ADDRESS)));
                    } catch (MalformedURLException ex) {
                        log.error("Unable to open broser.", ex);
                        showErrorBubble("Unable to open browser due to incorrect URL. Check your settings.");
                    }
                }
            });
            popup.add(browserMenuItem);

            popup.addSeparator();

            // Location menu items
            int i = 1;
            boolean checked = false;
            for (final PersonLocation location : PersonLocation.values()) {
                CheckboxMenuItem loc = new CheckboxMenuItem(location.getName(), location.getName().equals(currentLocation));
                checked = location.getName().equals(currentLocation) ? true : checked;
                popup.add(loc);
                loc.addItemListener(new ItemListener() {

                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (client.setLocation(location.getName())) {
                            setCurrentLocation(location.getName());
                            trayIcon.setPopupMenu(createMenu(getCurrentState(), location.getName()));
                        }
                    }
                });
                if (i++ > 4) {
                    break;
                }
            }
            String locationStr = currentLocation != null && !currentLocation.isEmpty()
                    ? " (" + currentLocation.substring(0, currentLocation.length() > 15 ? 15 : currentLocation.length()) + ")"
                    : "";
            final CheckboxMenuItem locationMenuItem = new CheckboxMenuItem("Other location " + (checked ? "" : locationStr) + "...", !checked);
            locationMenuItem.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    locationMenuItem.setState(e.getStateChange() == ItemEvent.DESELECTED);
                    requestNewLocation(currentLocation);
                }
            });
            popup.add(locationMenuItem);

            popup.addSeparator();

            // States menu items
            Configuration conf = Configuration.getInstance();
            final CheckboxMenuItem availableItem = new CheckboxMenuItem(PersonState.AVAILABLE.getDisplayName());
            final CheckboxMenuItem dndDefaultItem = new CheckboxMenuItem(PersonState.DO_NOT_DISTURB.getDisplayName());
            if (currentState.equals(PersonState.AVAILABLE)) {
                initMenuItemsAvailable(availableItem, popup, dndDefaultItem);
            } else if (currentState.equals(PersonState.DO_NOT_DISTURB)) {
                initMenuItemsDnd(dndDefaultItem, popup, availableItem);
            }
            statesMenuItems.put(PersonState.DO_NOT_DISTURB, dndDefaultItem);
            statesMenuItems.put(PersonState.AVAILABLE, availableItem);
            popup.add(dndDefaultItem);
            popup.add(availableItem);
        } else {
            MenuItem item = new MenuItem("Server is unreachable");
            item.setEnabled(false);
            popup.add(item);
        }
        return popup;
    }

    private void initMenuItemsDnd(final CheckboxMenuItem dndDefaultItem, PopupMenu popup, final CheckboxMenuItem availableItem) throws HeadlessException {
        final Long dndLastTime = Configuration.getInstance().getLongProperty(Property.DND_LAST_PERIOD);
        final Long dndMaxTime = client.getDndMax();
        makeStateItemChosen(dndDefaultItem);
        Long current = new Date().getTime();
        Long end = client.getDndEnd();
        dndDefaultItem.setLabel(PersonState.DO_NOT_DISTURB.getDisplayName() + " (ends in " + millisToMins(end - current) + " min.)");
        Long diff = millisToMins(dndMaxTime - dndLastTime); //difference between current and maximal time spent in DND state

        // if it is possible to prolong the DND period
        if (diff > 0) {
            final MenuItem addDndTimeMenuItem = new MenuItem("Add " + (diff > 10 ? "10" : diff) + " min. to " + PersonState.DO_NOT_DISTURB.getDisplayName());
            addDndTimeMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    long dndLastTime = Configuration.getInstance().getLongProperty(Property.DND_LAST_PERIOD);
                    long extraTime = dndMaxTime - dndLastTime;
                    long addTime = extraTime > 600000 ? 600000 : extraTime;
                    Configuration.getInstance().setProperty(Property.DND_LAST_PERIOD, String.valueOf(dndLastTime + addTime));
                    client.addDndTime(addTime);
                    extraTime = extraTime - addTime;
                    if (extraTime < 600000) {
                        // we need to refresh the menu to get the new period into the label and ActionEvent
                        trayIcon.setPopupMenu(createMenu(getCurrentState(), getCurrentLocation()));
                    }
                }
            });
            popup.add(addDndTimeMenuItem);
        }
        availableItem.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                changeState(PersonState.AVAILABLE);
            }
        });
    }

    private void initMenuItemsAvailable(final CheckboxMenuItem availableItem, PopupMenu popup, final CheckboxMenuItem dndDefaultItem) throws HeadlessException {
        final Configuration conf = Configuration.getInstance();
        final Long dndDefaultTime = conf.getLongProperty(Property.DND_DEFAULT_PERIOD);
        final Long dndLastTime = conf.getLongProperty(Property.DND_LAST_PERIOD);
        final Long dndMaxTime = client.getDndMax();
        makeStateItemChosen(availableItem);
        if (client.isStateChangePossible(PersonState.DO_NOT_DISTURB)) {
            MenuItem dndCustomItem = new MenuItem(PersonState.DO_NOT_DISTURB.getDisplayName() + " for...");
            dndCustomItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    dndCustomPeriodFrame.requestPeriodAndSwitchToDnd(dndDefaultTime, dndMaxTime);
                }
            });
            popup.add(dndCustomItem);
            dndDefaultItem.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        dndDefaultItem.setState(true);
                        return;
                    }
                    // the order is important here
                    conf.setProperty(Property.DND_LAST_PERIOD, dndDefaultTime.toString());
                    changeToDndState(dndDefaultTime);
                }
            });
            if (!Objects.equals(dndDefaultTime, dndLastTime)) {
                MenuItem dndLastItem = new MenuItem(PersonState.DO_NOT_DISTURB.getDisplayName() + " for " + millisToMins(dndLastTime) + " min.");

                dndLastItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // the order is important here
                        changeToDndState(dndLastTime);
                    }
                });
                popup.add(dndLastItem);
            }
            dndDefaultItem.setLabel(PersonState.DO_NOT_DISTURB.getDisplayName() + " for " + dndDefaultTime / 60000 + " min.");
        } else {
            dndDefaultItem.setEnabled(false);
            Long current = new Date().getTime();
            Long start = client.getDndStart();
            dndDefaultItem.setLabel(dndDefaultItem.getLabel() + " (in " + millisToMins(start - current) + " min.)");
        }
    }

    private void makeStateItemChosen(final CheckboxMenuItem item) {
        item.setState(true);
        item.setFont(BOLD_FONT);
        item.setLabel("-" + item.getLabel() + "-");
        item.addItemListener(new ItemListener() { // to prevent the item to be unchecked

            @Override
            public void itemStateChanged(ItemEvent e) {
                item.setState(true);
            }
        });
    }

    private void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                log.error("Unable to open browser.", e);
                showErrorBubble("Unable to open browser.");
            }
        }
    }

    private void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            log.error("Unable to open webpage.", e);
            showErrorBubble("Unable to open browser due to incorrect URL. Check your settings.");
        }
    }

    /**
     * This method should be called when change of state is initiated by client
     * application (not server).
     *
     * @param state
     */
    private synchronized void changeState(PersonState state) {
        if (beforeStateChangeActions(state, "-")) {
            return;
        }
        PersonState newStateByServer = client.setState(state);
        afterStateChangeActions(newStateByServer, state);
    }

    /**
     * This method should be called when change of state to DND is initiated by
     * client application (not server).
     *
     * @param period
     */
    private synchronized void changeToDndState(Long period) {
        if (beforeStateChangeActions(PersonState.DO_NOT_DISTURB, String.valueOf(period)));
        PersonState newStateByServer = client.setDndState(period);
        afterStateChangeActions(newStateByServer, PersonState.DO_NOT_DISTURB);
    }

    private boolean beforeStateChangeActions(PersonState state, String period) {
        log.info("State change request by user -> " + state);
        if (state.equals(currentState)) {
            return true;
        }
        log.debug("User switched state to {} for {} ms", state, period);
        if (!client.isStateChangePossible(state)) {
            showErrorBubble("Unable to switch to " + state.getDisplayName());
            log.warn("Attempt to switch to state {} unsuccessfull.", state.getDisplayName());
        }
        return false;
    }

    private void afterStateChangeActions(PersonState newStateByServer, PersonState state) {
        log.info("Server returned state " + newStateByServer + " after user switched state to " + state);
        currentState = newStateByServer;
        initializeTrayIcon(currentState, currentLocation);
    }

    private TrayIcon createIcon(Image image) {
        TrayIcon trayIconn = new TrayIcon(getTrayIconImage(image));
        return trayIconn;
    }

    private Image getTrayIconImage(Image image) {
        int trayIconWidth = new TrayIcon(image).getSize().width;
        return image.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH);
    }

    private void showInfoBubble(String text) {
        if (Configuration.getInstance().getBooleanProperty(Property.POPUPS_ENABLED)) {
            trayIcon.displayMessage("Any Office", text, TrayIcon.MessageType.INFO);
        }
    }

    private void showErrorBubble(String text) {
        trayIcon.displayMessage("Any Office", text, TrayIcon.MessageType.ERROR);
    }

    private void showInfoMessage(final String title, final String text) {
        final JFrame f = new JFrame();
        f.setAlwaysOnTop(true);
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                JOptionPane.showMessageDialog(f, text, title, JOptionPane.PLAIN_MESSAGE, new ImageIcon("images/logo.ico"));
            }
        });
        t.start();
    }

    /**
     * Displays a window with a field to input a new location. If user confirms
     * the location, sends the updateFromServer to server.
     */
    private void requestNewLocation(String currentLocation) {
        JComboBox combo = new JComboBox();
        combo.setEditable(true);
        for (PersonLocation location : PersonLocation.values()) {
            combo.addItem(location.getName());
        }
        combo.setSelectedItem(currentLocation);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("What is your current location?"));
        panel.add(combo);
        JFrame frame = new JFrame();
        frame.setIconImage(icon);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        int result = JOptionPane.showConfirmDialog(frame, panel, "Location",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String location = combo.getSelectedItem().toString();
            if (client.setLocation(location)) {
                this.currentLocation = location;
                trayIcon.setPopupMenu(createMenu(currentState, location));
            }
        }
    }

    /**
     * Displays a window with a field to input a new newLocation. If user
     * confirms the location, sends the updateFromServer to server.
     */
    private void showSettings() {
        Configuration conf = Configuration.getInstance();
        JTextField webAddressField = new JTextField(conf.getProperty(Property.WEB_ADDRESS));
        JCheckBox rememberMeCheckBox = new JCheckBox("Remember me", !conf.getProperty(Property.GUID).isEmpty());
        JCheckBox runAtStratupCheckBox = new JCheckBox("Run at Windows startup", conf.getBooleanProperty(Property.RUN_AT_STARTUP));
        JCheckBox popupMessagesCheckBox = new JCheckBox("Show popup messages", conf.getBooleanProperty(Property.POPUPS_ENABLED));
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Web page address"));
        panel.add(webAddressField);
        if (rememberMeCheckBox.isSelected()) { //this property can only be turned off in settings
            panel.add(rememberMeCheckBox);
            panel.add(new JLabel());
        }
        panel.add(runAtStratupCheckBox);
        panel.add(new JLabel());
        panel.add(popupMessagesCheckBox);
        panel.add(new JLabel());
        JFrame frame = new JFrame();
        frame.setIconImage(icon);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        int result = JOptionPane.showConfirmDialog(frame, panel, "Settings",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            conf.setProperty(Property.WEB_ADDRESS, webAddressField.getText());
            if (!rememberMeCheckBox.isSelected()) {
                conf.setProperty(Property.GUID, "");
            }
            if (!(runAtStratupCheckBox.isSelected() == conf.getBooleanProperty(Property.RUN_AT_STARTUP))) {
                conf.setProperty(Property.RUN_AT_STARTUP, String.valueOf(runAtStratupCheckBox.isSelected()));
                try {
                    shortcutInStartupFolder(runAtStratupCheckBox.isSelected());
                } catch (IOException ex) {
                    log.error("Unable to turn on/off RUN_AT_STARTUP", ex);
                    conf.setProperty(Property.RUN_AT_STARTUP, String.valueOf(!runAtStratupCheckBox.isSelected()));
                }
            }
            conf.setProperty(Property.POPUPS_ENABLED, String.valueOf(popupMessagesCheckBox.isSelected()));
        }
    }

    /**
     * Shows a popup window with request for credentials.
     *
     * @return New credentials
     */
    private Credentials requestCredentials(boolean serverField) {
        Configuration config = Configuration.getInstance();
        final JTextField field1 = new JTextField(config.getProperty(Property.CURRENT_USER));
        JPasswordField field2 = new JPasswordField();
        JTextField field0 = new JTextField(config.getProperty(Property.SERVER_ADDRESS));
        JCheckBox rememberCheckBox = new JCheckBox("Remember me");
        JCheckBox runAtStartupCheckBox = new JCheckBox("Run at Windows startup");
        JPanel panel = new JPanel(new GridLayout(0, 2));
        if (serverField) {
            panel.add(new JLabel("Server:"));
            panel.add(field0);
        }
        panel.add(new JLabel("Username:"));
        panel.add(field1);
        panel.add(new JLabel("Password:"));
        panel.add(field2);
        panel.add(rememberCheckBox);
        panel.add(runAtStartupCheckBox);
        runAtStartupCheckBox.setSelected(Configuration.getInstance().getBooleanProperty(Property.RUN_AT_STARTUP));
        field1.setSelectionStart(0);
        field1.setSelectionEnd(field1.getText().length() - 1);
        field1.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
                field1.requestFocusInWindow();
            }
        });
        JFrame frame = new JFrame();
        frame.setIconImage(icon);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        int result = JOptionPane.showConfirmDialog(frame, panel, "Please log in",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Credentials c;
            if (Configuration.getInstance().getBooleanProperty(Property.RUN_AT_STARTUP) != runAtStartupCheckBox.isSelected()) {
                Configuration.getInstance().setProperty(Property.RUN_AT_STARTUP, String.valueOf(runAtStartupCheckBox.isSelected()));
                try {
                    shortcutInStartupFolder(runAtStartupCheckBox.isSelected());
                } catch (IOException ex) {
                    log.error("Unable to turn on/off RUN_AT_STARTUP", ex);
                    Configuration.getInstance().setProperty(Property.RUN_AT_STARTUP, String.valueOf(!runAtStartupCheckBox.isSelected()));
                }
            }
            try {
                c = new Credentials(field1.getText(), field2.getPassword());
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(frame, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                return requestCredentials(false);
            }
            if (field0.isVisible()) {
                config.setProperty(Property.SERVER_ADDRESS, field0.getText());
                RestClient.setServerAddress(field0.getText());
            }
            if (RestClient.isCorrectCredentials(c)) {
                Configuration.getInstance().setProperty(Property.CURRENT_USER, field1.getText());
                if (rememberCheckBox.isSelected()) {
                    log.debug("Saving authentication string.");
                    config.setProperty(Property.GUID, c.getEncodedAuthenticationString());
                }
                return c;
            } else {
                JOptionPane.showMessageDialog(frame, "Incorrect credentials or server is unavailable. If the problem persists, try updating your client.", "Authentication failed", JOptionPane.ERROR_MESSAGE);
                return requestCredentials(true);
            }
        } else {
            frame.dispose();
            JOptionPane.showMessageDialog(null, "No credentials were provided. Application will exit now.", "Cancelled", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
            return null;
        }
    }

    private void shortcutInStartupFolder(boolean create) throws IOException {
        File f = new File("tmp.cmd");
        f.createNewFile();
        f.deleteOnExit();
        try (PrintWriter writer = new PrintWriter("tmp.cmd", "UTF-8")) {
            StringBuilder sb = new StringBuilder();
            String currentPath = TrayIconManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if (currentPath.startsWith("/")) {
                currentPath = currentPath.substring(1);
            } else if (currentPath.startsWith("file")) {
                currentPath = currentPath.substring(6);
            }
            System.out.println(currentPath);
            if (create) {
                sb.append("@echo off \n");
                sb.append("echo Set oWS = WScript.CreateObject(\"WScript.Shell\") > CreateShortcut.vbs\n");
                sb.append("echo IF EXISTS \"%USERPROFILE%\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\\" GOTO WIN7\n");
                sb.append("echo sLinkFile = \"%USERPROFILE%\\Start Menu\\Programs\\Startup\\AnyOffice-client.lnk\" >> CreateShortcut.vbs\n");
                sb.append("echo GOTO CONT\n");
                sb.append("echo :WIN7\n");
                sb.append("echo sLinkFile = \"%USERPROFILE%\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\AnyOffice-client.lnk\" >> CreateShortcut.vbs\n");
                sb.append("echo :CONT\n");
                sb.append("echo Set oLink = oWS.CreateShortcut(sLinkFile) >> CreateShortcut.vbs\n");
                sb.append("echo oLink.TargetPath = \"");
                sb.append(currentPath);
                sb.append("\" >> CreateShortcut.vbs\n");
                sb.append("echo oLink.Save >> CreateShortcut.vbs\n");
                sb.append("cscript CreateShortcut.vbs\n");
                sb.append("del CreateShortcut.vbs");
            } else {
                sb.append("@echo off\n");
                sb.append("IF EXIST \"%USERPROFILE%\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\" GOTO WIN7\n");
                sb.append("del \"%USERPROFILE%\\Start Menu\\Programs\\Startup\\AnyOffice-client.lnk\"\n");
                sb.append("GOTO END\n");
                sb.append(":WIN7\n");
                sb.append("del \"%USERPROFILE%\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\AnyOffice-client.lnk\"\n");
                sb.append(":END");
            }
            writer.write(sb.toString());
            writer.close();
        }
        runScript("tmp.cmd");
    }

    private void runScript(String script) {
        String commandString = "cmd /c " + script;
        Runtime run = Runtime.getRuntime();
        log.debug("Executing: " + commandString);
        Process p;
        try {
            p = run.exec(commandString);
            log.info("Command {} finished with value {}", commandString, p.waitFor());
        } catch (IOException | InterruptedException e1) {
            String msg = "Unable to execute command " + commandString;
            log.error(msg, e1);
        }
    }

    private class DndCustomPeriodFrame extends javax.swing.JFrame {

        private Long maxDndPeriod;
        private Long maxDndPeriodInMillis;
        private Long currentValue;
        private Long currentValueInMillis;

        private Long getMaxDndPeriod() {
            return maxDndPeriod;
        }

        private Long getMaxDndPeriodInMillis() {
            return maxDndPeriodInMillis;
        }

        private void setMaxDndPeriodInMillis(Long maxDndPeriodInMillis) {
            this.maxDndPeriodInMillis = maxDndPeriodInMillis;
            this.maxDndPeriod = maxDndPeriodInMillis / 60000;
        }

        private Long getCurrentValue() {
            return currentValue;
        }

        private void setCurrentValue(Long currentValue) {
            this.currentValue = currentValue;
            this.currentValueInMillis = currentValue * 60000;
            periodTextField.setText(String.valueOf(currentValue));
            minusButton.setEnabled(true);
            plusButton.setEnabled(true);
            if (currentValue >= getMaxDndPeriod()) {
                plusButton.setEnabled(false);
            } else if (currentValue <= 1) {
                minusButton.setEnabled(false);
            }
        }

        private Long getCurrentValueInMillis() {
            return currentValueInMillis;
        }

        /**
         * Creates new form NewJFrame
         */
        public DndCustomPeriodFrame() {
            initComponents();
        }

        public void requestPeriodAndSwitchToDnd(Long defaultPeriod, Long maxDndPeriod) {
            this.setMaxDndPeriodInMillis(maxDndPeriod);
            setCurrentValue(defaultPeriod / 60000);
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        }

        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="initComponents">
        private void initComponents() {

            jLabel1 = new javax.swing.JLabel();
            periodTextField = new javax.swing.JTextField();
            jLabel2 = new javax.swing.JLabel();
            minusButton = new javax.swing.JButton();
            plusButton = new javax.swing.JButton();
            saveAsDefaultCheckBox = new javax.swing.JCheckBox();
            okButton = new javax.swing.JButton();
            cancelButton = new javax.swing.JButton();

            setTitle("Go to " + PersonState.DO_NOT_DISTURB.getDisplayName());
            setAlwaysOnTop(true);
            setResizable(false);

            jLabel1.setText("How long would you like to be in " + PersonState.DO_NOT_DISTURB.getDisplayName() + " for?");

            periodTextField.setInputVerifier(new InputVerifier() {

                @Override
                public boolean verify(JComponent input) {
                    Long value = getCurrentValue();
                    try {
                        value = Long.parseUnsignedLong(((JTextField) input).getText());
                        boolean result = value <= getMaxDndPeriod() && value > 0;
                        if (!result) {
                            System.out.println(value);
                            periodTextField.setText(getCurrentValue().toString());
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(e.getMessage());
                        periodTextField.setText(getCurrentValue().toString());
                    }
                    setCurrentValue(value);
                    return true;
                }
            });

            jLabel2.setText("minutes");

            minusButton.setText("-");

            plusButton.setText("+");

            plusButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (getMaxDndPeriod() > getCurrentValue()) {
                        setCurrentValue(getCurrentValue() + 1);
                    }
                    if (Objects.equals(getMaxDndPeriod(), getCurrentValue())) {
                        plusButton.setEnabled(false);
                    }
                    minusButton.setEnabled(true);
                }
            });
            minusButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (1 < getCurrentValue()) {
                        setCurrentValue(getCurrentValue() - 1);
                    }
                    if (1 == getCurrentValue()) {
                        minusButton.setEnabled(false);
                    }
                    plusButton.setEnabled(true);
                }
            });

            saveAsDefaultCheckBox.setText("Save as default period");

            okButton.setText("OK");

            okButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (getCurrentValueInMillis() > getMaxDndPeriodInMillis()) {
                        showInfoMessage("Invalid period", "Maximum possible period is " + getMaxDndPeriod());
                        return;
                    }
                    // the order is important here
                    Configuration.getInstance().setProperty(Property.DND_LAST_PERIOD, getCurrentValueInMillis().toString());
                    changeToDndState(getCurrentValueInMillis());
                    if (saveAsDefaultCheckBox.isSelected()) {
                        Configuration.getInstance().setProperty(Property.DND_DEFAULT_PERIOD, getCurrentValueInMillis().toString());
                    }
                    setVisible(false);
                }
            });

            cancelButton.setText("Cancel");
            cancelButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(layout.createSequentialGroup()
                                                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel1)
                                                    .addGroup(layout.createSequentialGroup()
                                                            .addComponent(periodTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(jLabel2)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(minusButton)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(plusButton))))
                                    .addComponent(saveAsDefaultCheckBox))
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(periodTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)
                                    .addComponent(minusButton)
                                    .addComponent(plusButton))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(saveAsDefaultCheckBox)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(okButton)
                                    .addComponent(cancelButton))
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            pack();
        }// </editor-fold>

        private javax.swing.JButton cancelButton;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JButton minusButton;
        private javax.swing.JButton okButton;
        private javax.swing.JTextField periodTextField;
        private javax.swing.JButton plusButton;
        private javax.swing.JCheckBox saveAsDefaultCheckBox;
    }

    private class UpdateIconMouseListener extends MouseAdapter {

        private boolean released = true;

        @Override
        public void mousePressed(MouseEvent e) {
            if (released) {
                updateFromServer();
                released = false;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            released = true;
        }

    }

    /**
     * Frame for displaying people available to be contacted.
     */
    private class AvailableConsultersMessageFrame extends javax.swing.JFrame {

        private JLabel mainLabel;
        private javax.swing.JScrollPane jScrollPane1;
        private JButton dismissButton;
        private JTable jTable1;

        public AvailableConsultersMessageFrame() {
            initComponents();
            showOnTop(false);
            this.setLocationRelativeTo(null);
            setIconImage(icon);
        }

        private void initComponents() {
            mainLabel = new javax.swing.JLabel("Following people are now available:\n");
            jScrollPane1 = new javax.swing.JScrollPane();
            dismissButton = new JButton("Dismiss all");

            dismissButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    showOnTop(false);
                }
            });
//            dismissButton.addActionListener((ActionEvent) -> {
//                showOnTop(false);
//            });

            setTitle("Any Office - New consultations possible.");
            setResizable(false);
            setType(java.awt.Window.Type.POPUP);
            addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    formKeyReleased(evt);
                }
            });

            jTable1 = new javax.swing.JTable();
            jTable1.setModel(new javax.swing.table.DefaultTableModel(
                    new Object[][]{},
                    new String[]{
                        "Who", "At least how long", "Where"//, " "
                    }
            ) {
                Class[] types = new Class[]{
                    java.lang.String.class, java.lang.String.class, java.lang.String.class//, java.lang.Object.class
                };

                @Override
                public Class getColumnClass(int columnIndex) {
                    return types[columnIndex];
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return false;
                }
            });
            jTable1.setEnabled(false);
            jTable1.setRowSelectionAllowed(false);
            jTable1.setSelectionBackground(new java.awt.Color(255, 255, 255));
            jTable1.setShowHorizontalLines(false);
            jTable1.setShowVerticalLines(false);
            jTable1.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mainLabel)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addContainerGap(12, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(dismissButton)
                            .addContainerGap())
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(mainLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(dismissButton)
                            .addContainerGap())
            );

            pack();

        }

        private void showOnTop(boolean show) {
            this.setAlwaysOnTop(show);
            this.setVisible(show);
            if (!show) {
                DefaultTableModel dm = (DefaultTableModel) jTable1.getModel();
                int rowCount = dm.getRowCount();
                for (int i = rowCount - 1; i >= 0; i--) {
                    dm.removeRow(i);
                }
            }
        }

        void showAvailableConsultersMessage(List<InteractionPerson> availableConsulters) {
            if (availableConsulters.isEmpty()) {
                return;
            }
            List<String[]> consulters = new LinkedList<>();
            for (InteractionPerson p : availableConsulters) {
                Long millis = p.getDndStart() - new Date().getTime();
                Integer minutes = 0;
                Integer seconds = 0;
                if (millis > 0) {
                    minutes = millis.intValue() / 60000;
                    seconds = millis.intValue() % 60;
                }
                String name = p.getDisplayName();
                String until = minutes + "m " + seconds + "s";
                String where = p.getLocation();
                String[] labels = {name, until, where};
//                JButton b = new JButton("Postpone");
//                b.setToolTipText("Remind again in 10 minutes or when available again.");
//                b.addActionListener((ActionEvent) -> {
//                    JOptionPane.showMessageDialog(this, "Sorry, not supported yet.");
//                });
                consulters.add(labels);
            }

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            for (String[] s : consulters) {
                model.addRow(new Object[]{s[0], s[1], s[2]/*, "Postpone"*/});
            }

            jScrollPane1.setViewportView(jTable1);

//            jTable1.getColumn(" ").setCellRenderer(new ButtonRenderer());
//            jTable1.getColumn(" ").setCellEditor(new ButtonEditor(new JCheckBox()));
            pack();
            showOnTop(true);
        }

        private void formKeyReleased(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode() == (KeyEvent.VK_ENTER)) {
                showOnTop(false);
            }
        }

        class ButtonRenderer extends JButton implements TableCellRenderer {

            public ButtonRenderer() {
                setOpaque(true);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (isSelected) {
                    setForeground(table.getSelectionForeground());
                    setBackground(table.getSelectionBackground());
                } else {
                    setForeground(table.getForeground());
                    setBackground(UIManager.getColor("Button.background"));
                }
                setText((value == null) ? "" : value.toString());
                return this;
            }
        }

        private final class ButtonEditor extends DefaultCellEditor {

            protected JButton button;

            private String label;

            private boolean isPushed;

            public ButtonEditor(JCheckBox checkBox) {
                super(checkBox);
                log.debug("ButtonEditor initialized");
                button = new JButton();
                button.setOpaque(true);
                button.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fireEditingStopped();
                    }
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                log.debug("getTableCellEditorComponent({}, {}, {}, {}, {}) called", table, value, isSelected, row, column);
                if (isSelected) {
                    button.setForeground(table.getSelectionForeground());
                    button.setBackground(table.getSelectionBackground());
                } else {
                    button.setForeground(table.getForeground());
                    button.setBackground(table.getBackground());
                }
                label = (value == null) ? "" : value.toString();
                button.setText(label);
                button.setToolTipText("Remind again in 10 minutes or when available again.");
                isPushed = true;
                return button;
            }

            @Override
            public Object getCellEditorValue() {
                log.debug("getCellEditorValue called; isPushed={}", isPushed);
                if (isPushed) {
                    JOptionPane.showMessageDialog(button, "Sorry, not supported yet.");
                }
                isPushed = false;
                return label;
            }

            @Override
            public boolean stopCellEditing() {
                log.debug("stopCellEditing called");
                isPushed = false;
                return super.stopCellEditing();
            }

            @Override
            protected void fireEditingStopped() {
                log.debug("fireEditingStopped() called");
                super.fireEditingStopped();
            }
        }
    }

    private class SwitchToStateFrame extends javax.swing.JFrame {

        private PersonState state;

        /**
         * Creates new form SwitchToDndFrame
         */
        public SwitchToStateFrame() {
            initComponents();
            showOnTop(false);
            this.setLocationRelativeTo(null);
            setIconImage(icon);
        }

        void display(PersonState state) {
            this.state = state;
            this.jLabel1.setText("Would you like to switch to " + state.getDisplayName() + " now?");
            getRootPane().setDefaultButton(jButtonYes);
            jPanel1.setVisible(false);
            jToggleButtonRemindLater.setSelected(false);
            showOnTop(true);
            pack();
        }

        private void showOnTop(boolean show) {
            this.setAlwaysOnTop(show);
            this.setVisible(show);
        }

        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="initComponents">
        private void initComponents() {

            jLabel1 = new javax.swing.JLabel();
            jButtonYes = new javax.swing.JButton();
            jButtonNo = new javax.swing.JButton();
            jToggleButtonRemindLater = new javax.swing.JToggleButton();
            jPanel1 = new javax.swing.JPanel();
            jLabel2 = new javax.swing.JLabel();
            jTextFieldMinutes = new javax.swing.JTextField();
            jLabel3 = new javax.swing.JLabel();
            jButtonOk = new javax.swing.JButton();
            jPanel2 = new javax.swing.JPanel();

            setAlwaysOnTop(true);
            setResizable(false);
            setTitle("AnyOffice");
            addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    formKeyReleased(evt);
                }
            });

            jButtonYes.setText("Yes");
            jButtonYes.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonYesActionPerformed(evt);
                }
            });

            jButtonNo.setText("No");
            jButtonNo.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonNoActionPerformed(evt);
                }
            });

            jToggleButtonRemindLater.setText("Remind later");
            jToggleButtonRemindLater.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jPanel1.setVisible(jToggleButtonRemindLater.isSelected());
                    pack();
                }
            });

            jLabel2.setText("Remind me in");

            jTextFieldMinutes.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
            jTextFieldMinutes.setText("15");
            jTextFieldMinutes.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    jTextFieldMinutesKeyReleased(evt);
                }
            });

            jLabel3.setText("minutes");

            jButtonOk.setText("OK");
            jButtonOk.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonOkActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                    jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                            .addContainerGap(13, Short.MAX_VALUE)
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jTextFieldMinutes, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButtonOk, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
            );
            jPanel1Layout.setVerticalGroup(
                    jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextFieldMinutes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(jButtonOk))
            );

            jPanel2.setOpaque(false);

            javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
            jPanel2.setLayout(jPanel2Layout);
            jPanel2Layout.setHorizontalGroup(
                    jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGap(0, 219, Short.MAX_VALUE)
            );
            jPanel2Layout.setVerticalGroup(
                    jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGap(0, 6, Short.MAX_VALUE)
            );

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addGap(0, 0, Short.MAX_VALUE)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGroup(layout.createSequentialGroup()
                                                            .addComponent(jToggleButtonRemindLater)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(jButtonNo, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(jButtonYes, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel1)
                                            .addGap(0, 0, Short.MAX_VALUE)))
                            .addContainerGap())
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel1)
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButtonYes)
                                    .addComponent(jButtonNo)
                                    .addComponent(jToggleButtonRemindLater))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
            );
        }// </editor-fold>

        private void jButtonYesActionPerformed(java.awt.event.ActionEvent evt) {
            changeState(state);
            showOnTop(false);
        }

        private void jButtonNoActionPerformed(java.awt.event.ActionEvent evt) {
            showOnTop(false);
        }

        private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {
            remindIn(jTextFieldMinutes);
        }

        private void jTextFieldMinutesKeyReleased(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode() == (KeyEvent.VK_ENTER)) {
                jButtonOkActionPerformed(null);
            }
        }

        private void formKeyReleased(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode() == (KeyEvent.VK_ENTER)) {
                jButtonYesActionPerformed(null);
            }
        }

        private void remindIn(JTextField jTextFieldMinutes) {
            try {
                Thread t = new Thread(new RemindInThread(Integer.parseInt(jTextFieldMinutes.getText()), this.state));
                t.start();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(rootPane, "Please use only numeric characters.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private class RemindInThread implements Runnable {

            private final int minutes;
            private final PersonState state;

            public RemindInThread(int minutes, PersonState state) {
                this.state = state;
                this.minutes = minutes;
            }

            @Override
            public void run() {
                if (minutes < 1) {
                    JOptionPane.showMessageDialog(rootPane, "Please use positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                showOnTop(false);
                try {
                    Thread.sleep(minutes * 60000);
                } catch (InterruptedException ex) {
                    log.error("DND reminder sleep interrupted.", ex);
                }
                if (!currentState.equals(state)) {
                    display(state);
                }
            }

        }

        private javax.swing.JButton jButtonNo;
        private javax.swing.JButton jButtonOk;
        private javax.swing.JButton jButtonYes;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JTextField jTextFieldMinutes;
        private javax.swing.JToggleButton jToggleButtonRemindLater;
        private javax.swing.JPanel jPanel2;
    }
}
