/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

/**
 *
 * @author Bato
 */
class TrayIconManager {

    final static Logger log = LoggerFactory.getLogger(TrayIconManager.class);

    private static TrayIconManager instance;

    private TrayIcon trayIcon;
    private final Map<PersonState, MenuItem> stateItems;
    CheckboxMenuItem dndChkboxMenuItem;
    private final UpdateIconMouseListener updateIconMouseListener;

    private final SwitchToStateFrame switchToDndFrame;
    private final AvailableConsultersMessageFrame availableConsultersMessageFrame;

    private final RestClient client;

    private PersonState currentState;
    private String currentLocation;

    private static final Font BOLD_FONT = Font.decode(null).deriveFont(java.awt.Font.BOLD);
    private Image icon = null;

    private TrayIconManager() {
        icon = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("images/logo.png"));
        updateIconMouseListener = new UpdateIconMouseListener();
        switchToDndFrame = new SwitchToStateFrame();
        availableConsultersMessageFrame = new AvailableConsultersMessageFrame();
        stateItems = new HashMap<>();
        String authString = Configuration.getInstance().getProperty(Property.GUID);
        if (authString.isEmpty() || !RestClient.isCorrectCredentials(new Credentials(authString))) {
            client = new RestClient(requestCredentials(false));
        } else {
            client = new RestClient(new Credentials(authString));
        }
        client.ping();
        currentState = client.returnFromAway();
        currentLocation = client.getLocation();
        if (currentLocation == null || currentLocation.isEmpty()) {
            currentLocation = "-";
        }
        initialize(currentState, currentLocation);
        if (Configuration.getInstance().getBooleanProperty(Property.STATE_AUTO_SWITCH)
                && !currentState.equals(PersonState.DO_NOT_DISTURB)
                && client.isStateChangePossible(PersonState.DO_NOT_DISTURB)) {
            dndStateAutoSwitchProcess();
        }
    }

    static TrayIconManager initialize() {
        if (instance == null) {
            instance = new TrayIconManager();
        }
        return instance;
    }

    static TrayIconManager getInstance() {
        return instance;
    }

    private PersonState getCurrentState() {
        return currentState;
    }

    private synchronized void initialize(PersonState currentState, String currentLocation) {
        log.debug("Initializing visual components with state {} and location {}", currentState, currentLocation);
        if (!SystemTray.isSupported()) {
            log.error("SystemTray is not supported on this system.");
            return;
        }
        SystemTray tray = SystemTray.getSystemTray();
        if (trayIcon == null) {
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
            trayIcon.setImage(getTrayIconImage(currentState.getIcon()));
            trayIcon.setToolTip(currentState.getDescription());
            ActionListener[] actionListeners = trayIcon.getActionListeners();
            for (ActionListener a : actionListeners) {
                trayIcon.removeActionListener(a);
            }
        }
        trayIcon.setPopupMenu(createMenu(currentState, currentLocation));
        trayIcon.addMouseListener(updateIconMouseListener);
        trayIcon.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (getCurrentState().equals(PersonState.AVAILABLE)) {
                    if (stateItems.get(PersonState.DO_NOT_DISTURB).isEnabled()) {
                        switchToDndFrame.display(PersonState.DO_NOT_DISTURB);
                    } else {
                        Long current = new Date().getTime();
                        Long start = client.getDndStart();
                        showInfoMessage("AnyOffice", "Do Not Disturb will be available in " + ((start - current) / 60000) + " minutes.");
                    }
                } else if (getCurrentState().equals(PersonState.DO_NOT_DISTURB)) {
                    switchToDndFrame.display(PersonState.AVAILABLE);
                }
            }
        });
    }

    void pingServer() {
        client.ping();
    }

    synchronized void update() {
        if (!RestClient.isServerOnline()) {
            currentState = PersonState.UNKNOWN;
            initialize(currentState, currentLocation);
            return;
        }
        MenuItem dnd = stateItems.get(PersonState.DO_NOT_DISTURB);
        boolean wasDndAvailable = dnd != null && dnd.isEnabled();
        if (!wasDndAvailable && client.isStateChangePossible(PersonState.DO_NOT_DISTURB) && currentState.equals(PersonState.AVAILABLE)) {
            if (dnd != null) {
                dnd.setEnabled(true);
                dnd.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        changeState(PersonState.DO_NOT_DISTURB);
                    }
                });
            }
            if (dndChkboxMenuItem != null) {
                trayIcon.getPopupMenu().remove(dndChkboxMenuItem);
            }
            log.debug("DND is now enabled");
            if (Configuration.getInstance().getBooleanProperty(Property.STATE_AUTO_SWITCH)) {
                dndStateAutoSwitchProcess();
            } else if (dndChkboxMenuItem != null && dndChkboxMenuItem.getState()) {
                changeState(PersonState.DO_NOT_DISTURB);
                showInfoBubble("You are in Do Not Disturb state now.");
                dndChkboxMenuItem.setState(false);
                return;
            } else {
                showInfoBubble("Do Not Disturb state is possible. Click this bubble for further actions.");
            }
        }
        PersonState newState = client.getState();
        String newLocation = client.getLocation();
        if (!newState.isAwayState()) {
            List<InteractionPerson> availableConsulters = client.getNewAvailableConsulters();
            if (!availableConsulters.isEmpty()) {
                availableConsultersMessageFrame.showAvailableConsultersMessage(availableConsulters);
            }
            // if I can see a consultation request means, that I am or I've just recently been AVAILABLE
            int requests = client.getNumberOfRequests();
            if (requests > 0) {
                showInfoBubble(" You have " + requests + " pending request" + (requests > 1 ? "s" : "") + " for consultation.");
            }
        }
        Long current = new Date().getTime();
        if (newState.equals(PersonState.DO_NOT_DISTURB)) {
            Long end = client.getDndEnd() - current;
            trayIcon.setToolTip("Do not disturb will end in " + (end / 60000) + " minutes.");
        } else if (newState.equals(PersonState.AVAILABLE)) {
            Long start = client.getDndStart();
            if (start > current) {
                trayIcon.setToolTip("Do not disturb will be available in " + ((start - current) / 60000) + " minutes.");
            } else {
                trayIcon.setToolTip(PersonState.AVAILABLE.getDescription());
            }
        } else {
            trayIcon.setToolTip(newState.getDescription());
        }
        if (newState.equals(currentState) && newLocation.equals(currentLocation)) {
            return;
        }
        log.debug("Updating icon to state {}, location {}", newState, newLocation);
        boolean showAvailableBubble = newState.equals(PersonState.AVAILABLE) && currentState.equals(PersonState.DO_NOT_DISTURB);
        currentState = newState;
        currentLocation = newLocation;
        initialize(currentState, currentLocation);
        if (showAvailableBubble) {
            int requests = client.getNumberOfRequests();
            showInfoBubble("You have gone Available." + (requests == 0 ? "" : (" You have " + requests + " pending request" + (requests > 1 ? "s" : "") + " for consultation.")));
        }
    }

    private void dndStateAutoSwitchProcess() {
        //show bubble, add action to dismiss, start timer to autoswitch
        showInfoBubble("Do Not Disturb will be set in 20 seconds. Click this bubble to dismiss.");
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
     *
     * @param lock true to lock, false to unlock
     */
    synchronized void lock(boolean lock) {
        if (lock) {
            client.goAway();
            currentState = PersonState.AWAY;
        } else {
            client.returnFromAway();
            update();
        }
        log.debug("Session {}locked", lock ? "" : "un");
    }

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
     * - "Disturbed by" menu <br />
     * - Set location... button <br />
     * - Go to web page ... button <br />
     * - Buttons to switch to all states from PersonState enum <br />
     * The button for current state is in bold. The buttons for currently
     * unavailable states are disabled. For each disabled button there is a
     * check box item to switch to the state as soon as possible. These switches
     * have to be done elsewhere in code. States buttons are saved in map
     * stateItems. Check box items are saved in map chkBoxStateItems.
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

            String locationStr = (currentLocation == null || currentLocation.isEmpty()
                    ? ""
                    : (" (" + currentLocation.substring(0, currentLocation.length() > 15 ? 15 : currentLocation.length()) + ")"));
            MenuItem locationMenuItem = new MenuItem("Set location " + locationStr + "...");
            locationMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    requestNewLocation(currentLocation);
                }
            });
            popup.add(locationMenuItem);

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

            for (final PersonState state : PersonState.values()) {
                if (state.isAwayState()) {
                    continue;
                }
                MenuItem item = new MenuItem(state.getDisplayName());
                if (state.equals(currentState)) {
                    item.setFont(BOLD_FONT);
                    item.setLabel("-" + item.getLabel() + "-");
                } else if (!client.isStateChangePossible(state)) {
                    item.setEnabled(false);
                    if (state.equals(PersonState.DO_NOT_DISTURB) && !Configuration.getInstance().getBooleanProperty(Property.STATE_AUTO_SWITCH)) {
                        boolean chkBoxDndChecked = dndChkboxMenuItem != null && dndChkboxMenuItem.getState();
                        dndChkboxMenuItem = new CheckboxMenuItem("Switch to " + state.getDisplayName() + " (once)", chkBoxDndChecked);
                        popup.add(dndChkboxMenuItem);
                    }
                } else {
                    item.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            changeState(state);
                        }
                    });
                }
                stateItems.put(state, item);
                popup.add(item);
            }
        } else {
            MenuItem item = new MenuItem("Server is unreachable");
            item.setEnabled(false);
            popup.add(item);
        }
        return popup;
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

    private synchronized void changeState(PersonState state) {
        log.info("State change request by user -> " + state);
        if (state.equals(currentState)) {
            return;
        }
        log.debug("User switched state to " + state);
        if (!client.isStateChangePossible(state)) {
            showErrorBubble("Unable to switch to " + state.getDisplayName());
            log.warn("Attempt to switch to state {} unsuccessfull.", state.getDisplayName());
        }
        PersonState newStateByServer = client.setState(state);
        log.info("Server returned state " + newStateByServer + " after user switched state to " + state);
        currentState = newStateByServer;
        initialize(currentState, currentLocation);
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
     * Displays a window with a field to input a new newLocation. If user
     * confirms the location, sends the update to server.
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
     * confirms the location, sends the update to server.
     */
    private void showSettings() {
        Configuration conf = Configuration.getInstance();
        JTextField webAddressField = new JTextField(conf.getProperty(Property.WEB_ADDRESS));
        JCheckBox rememberMeCheckBox = new JCheckBox("Remember me", !conf.getProperty(Property.GUID).isEmpty());
        JCheckBox runAtStratupCheckBox = new JCheckBox("Run at Windows startup", conf.getBooleanProperty(Property.RUN_AT_STARTUP));
        JCheckBox popupMessagesCheckBox = new JCheckBox("Show popup messages", conf.getBooleanProperty(Property.POPUPS_ENABLED));
        JCheckBox autoSwitchCheckBox = new JCheckBox("Switch to DND automatically (always)", conf.getBooleanProperty(Property.STATE_AUTO_SWITCH));
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
        panel.add(autoSwitchCheckBox);
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
                shortcutInStartupFolder(runAtStratupCheckBox.isSelected());
            }
            conf.setProperty(Property.POPUPS_ENABLED, String.valueOf(popupMessagesCheckBox.isSelected()));
            if (!(autoSwitchCheckBox.isSelected() == conf.getBooleanProperty(Property.STATE_AUTO_SWITCH))) {
                conf.setProperty(Property.STATE_AUTO_SWITCH, String.valueOf(autoSwitchCheckBox.isSelected()));
                //switch right away if possible
                if (autoSwitchCheckBox.isSelected() && currentState.equals(PersonState.AVAILABLE) && client.isStateChangePossible(PersonState.DO_NOT_DISTURB)) {
                    changeState(PersonState.DO_NOT_DISTURB);
                } else if (autoSwitchCheckBox.isSelected()) {
                    trayIcon.getPopupMenu().remove(dndChkboxMenuItem);
                } else {
                    initialize(currentState, currentLocation);
                }
            }
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
                shortcutInStartupFolder(runAtStartupCheckBox.isSelected());
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
            Main.programFinish();
            return null;
        }
    }

    private void shortcutInStartupFolder(boolean create) {
        if (create) {
            runScript("startAtLogon.cmd");
        } else {
            runScript("notStartAtLogon.cmd");
        }
    }

    private void runScript(String script) {
        String commandString = "cmd /c " + this.getClass().getClassLoader().getResource(script).getPath().substring(1);
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

    private class UpdateIconMouseListener extends MouseAdapter {

        private boolean released = true;

        @Override
        public void mousePressed(MouseEvent e) {
            if (released) {
                update();
                released = false;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            released = true;
        }

    }

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

        /**
         * @version 1.0 11/09/98
         */
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

        /**
         * This method is called from within the constructor to initialize the
         * form. WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
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

        // Variables declaration - do not modify
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
        // End of variables declaration
    }
}
