/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.config.Configuration;
import eu.bato.anyoffice.trayapp.config.Property;
import eu.bato.anyoffice.trayapp.entities.InteractionPerson;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
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
    private Map<PersonState, MenuItem> stateItems;
    private final UpdateIconMouseListener updateIconMouseListener;
    private final SwitchToDndFrame switchToDndFrame;

    private final RestClient client;

    private PersonState currentState;
    private String currentLocation;

    private static final Font BOLD_FONT = Font.decode(null).deriveFont(java.awt.Font.BOLD);

    private TrayIconManager() {
        updateIconMouseListener = new UpdateIconMouseListener();
        switchToDndFrame = new SwitchToDndFrame();
        String authString = Configuration.getInstance().getProperty(Property.GUID);
        if (authString.isEmpty() || !RestClient.isCorrectCredentials(new Credentials(authString))) {
            client = new RestClient(requestCredentials());
        } else {
            client = new RestClient(new Credentials(authString));
        }
        currentState = client.returnFromAway();
        currentLocation = client.getLocation();
        if (currentLocation == null || currentLocation.isEmpty()) {
            currentLocation = "-";
        }
        initialize(currentState, currentLocation);
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

    private synchronized void initialize(PersonState currentState, String currentLocation) {
        log.debug("Initializing visual components with state {} and location {}", currentState, currentLocation);
        stateItems = new HashMap<>();
        if (!SystemTray.isSupported()) {
            log.error("SystemTray is not supported on this system.");
            return;
        }
        SystemTray tray = SystemTray.getSystemTray();
        if (trayIcon == null) {
            trayIcon = createIcon(currentState.getIconPath(), currentState.getDescription());
            try {
                tray.add(trayIcon);
            } catch (AWTException ex) {
                log.error("Desktop system tray is missing", ex);
            }
            showInfoBubble("Welcome!\nRight-click this icon to change your current state.");
        } else {
            trayIcon.setImage(getTrayIconImage(currentState.getIconPath()));
            trayIcon.setToolTip(currentState.getDescription());
        }
        trayIcon.setPopupMenu(createMenu(currentState, currentLocation));
        trayIcon.addMouseListener(updateIconMouseListener);
        trayIcon.addActionListener((ActionEvent) -> {
            if (!currentState.equals(PersonState.DO_NOT_DISTURB) && stateItems.get(PersonState.DO_NOT_DISTURB).isEnabled()) {
                switchToDndFrame.display();
            }
        });
    }

    synchronized void update() {
        boolean wasDndAvailable = stateItems.get(PersonState.DO_NOT_DISTURB).isEnabled();
        if (!wasDndAvailable && client.isStateChangePossible(PersonState.DO_NOT_DISTURB)) {
            stateItems.get(PersonState.DO_NOT_DISTURB).setEnabled(true);
            log.debug("DND is now enabled");
            showInfoBubble("Do not disturb state is possible. Click this bubble for further actions.");
        }
        PersonState newState = client.getState();
        String newLocation = client.getLocation();
        List<InteractionPerson> availableConsulters = client.getNewAvailableConsulters();
        if (!availableConsulters.isEmpty()) {
            showAvailableConsultersMessage(availableConsulters);
        }
        if (newState.equals(currentState) && newLocation.equals(currentLocation)) {
            return;
        }
        log.debug("Updating icon to state {}, location {}", newState, newLocation);
        boolean showAvailableBubble = newState.equals(PersonState.AVAILABLE);
        currentState = newState;
        currentLocation = newLocation;
        initialize(currentState, currentLocation);
        if (showAvailableBubble) {
            int requests = client.getNumberOfRequests();
            showInfoBubble("You have gone Available. You have " + (requests == 0 ? "no" : requests) + " pending request" + (requests > 1 ? "s" : "") + " for consultation.");
        }
    }

    /**
     *
     * @param lock true to lock, false to unlock
     */
    synchronized void lock(boolean lock) {
        if (lock) {
            client.goAway();
        } else {
            client.returnFromAway();
        }
        log.debug("Session {}locked", lock ? "" : "un");
    }

    void close() {
        SystemTray.getSystemTray().remove(trayIcon);
        client.setState(PersonState.UNKNOWN);
    }

    private PopupMenu createMenu(PersonState currentState, String currentLocation) {
        PopupMenu popup = new PopupMenu("Any Office");
        for (PersonState state : PersonState.values()) {
            if (state.isAwayState()) {
                continue;
            }
            MenuItem item = new MenuItem(state.getDisplayName());
            if (state.equals(currentState)) {
                item.setFont(BOLD_FONT);
                item.setLabel("-" + item.getLabel() + "-");
            } else if (!client.isStateChangePossible(state)) {
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
        MenuItem locationMenuItem = new MenuItem("Set location... (" + currentLocation + ")");
        locationMenuItem.addActionListener((ActionEvent) -> {
            requestNewLocation(currentLocation);
        });
        popup.add(locationMenuItem);

        popup.addSeparator();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener((ActionEvent) -> {
            Main.programFinish();
        });
        popup.add(exitItem);

        return popup;
    }

    private void changeState(PersonState state) {
        if (state.equals(currentState)) {
            return;
        }
        log.debug("User switched state to " + state);
        if (!client.isStateChangePossible(state)) {
            showErrorBubble("Unable to switch to " + state.getDisplayName());
        }
        PersonState newStateByServer = client.setState(state);
        log.info("Server returned state " + newStateByServer + " after user switched state to " + state);
        currentState = state;
        initialize(state, currentLocation);
    }

    private TrayIcon createIcon(String path, String description) {
        TrayIcon icon = new TrayIcon(getTrayIconImage(path), description);
        return icon;
    }

    private Image getTrayIconImage(String path) {
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

    private void showInfoBubble(String text) {
        trayIcon.displayMessage("Any Office", text, TrayIcon.MessageType.INFO);
    }

    private void showErrorBubble(String text) {
        trayIcon.displayMessage("Any Office", text, TrayIcon.MessageType.ERROR);
    }

    private void showInfoMessage(String title, String text) {
        JFrame f = new JFrame();
        f.setAlwaysOnTop(true);
        Thread t = new Thread(() -> {
            JOptionPane.showMessageDialog(f, text, title, JOptionPane.PLAIN_MESSAGE);
        });
        t.start();
    }

    /**
     * Requests new newLocation.
     */
    private void requestNewLocation(String currentLocation) {
        JTextField field1 = new JTextField(currentLocation);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("What is your current location?"));
        panel.add(field1);
        field1.selectAll();
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        int result = JOptionPane.showConfirmDialog(frame, panel, "Location",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String location = field1.getText();
            if (client.setLocation(location)) {
                this.currentLocation = location;
                trayIcon.setPopupMenu(createMenu(currentState, location));
            }
        }
    }

    /**
     * Shows a popup window with request for credentials.
     *
     * @return New credentials
     */
    private Credentials requestCredentials() {
        Configuration config = Configuration.getInstance();
        JTextField field1 = new JTextField(config.getProperty(Property.CURRENT_USER));
        JPasswordField field2 = new JPasswordField();
        JCheckBox rememberCheckBox = new JCheckBox("Remember me");
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Username:"));
        panel.add(field1);
        panel.add(new JLabel("Password:"));
        panel.add(field2);
        panel.add(rememberCheckBox);
        field1.setSelectionStart(0);
        field1.setSelectionEnd(field1.getText().length() - 1);
//        field1.addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentShown(ComponentEvent ce) {
//                field1.setrequestFocusInWindow();
//            }
//        });
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        int result = JOptionPane.showConfirmDialog(frame, panel, "Please log in",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Credentials c;
            try {
                c = new Credentials(field1.getText(), field2.getPassword());
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(frame, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                return requestCredentials();
            }
            if (RestClient.isCorrectCredentials(c)) {
                Configuration.getInstance().setProperty(Property.CURRENT_USER, field1.getText());
                if (rememberCheckBox.isSelected()) {
                    log.debug("Saving authentication string.");
                    config.setProperty(Property.GUID, c.getEncodedAuthenticationString());
                }
                return c;
            } else {
                JOptionPane.showMessageDialog(frame, "Incorrect password or unknown user.", "Authentication failed", JOptionPane.ERROR_MESSAGE);
                return requestCredentials();
            }
        } else {
            frame.dispose();
            JOptionPane.showMessageDialog(null, "No credentials were provided. Application will exit now.", "Cancelled", JOptionPane.WARNING_MESSAGE);
            Main.programFinish();
            return null;
        }
    }

    private void showAvailableConsultersMessage(List<InteractionPerson> availableConsulters) {
        if (availableConsulters.isEmpty()) {
            return;
        }
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setTitle("Any Office - Available consultations");

        JLabel mainLabel = new JLabel("Following people are now available (at least how long for, where):\n");
        Map<JLabel, JButton> consulters = new LinkedHashMap<>();
        availableConsulters.forEach((p) -> {
            Long millis = p.getDndStart() - new Date().getTime();
            Integer minutes = 0;
            Integer seconds = 0;
            if (millis > 0) {
                minutes = millis.intValue() / 60000;
                seconds = millis.intValue() % 60;
            }
            StringBuilder sb = new StringBuilder();
            sb
                    .append(p.getDisplayName())
                    .append(" (")
                    .append(minutes).append("m ").append(seconds).append("s, ")
                    .append(p.getLocation())
                    .append(")");
            JLabel label = new JLabel(sb.toString());
            JButton b = new JButton("R");
            b.setToolTipText("Remind again in 10 minutes or when available again.");
            b.addActionListener((ActionEvent) -> {
                JOptionPane.showMessageDialog(frame, "Sorry, not supported yet.");
            });
            consulters.put(label, b);
        });
        JButton dismissButton = new JButton("Dismiss all");
        dismissButton.addActionListener((ActionEvent) -> {
            frame.dispose();
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(frame.getContentPane());
        frame.getContentPane().setLayout(layout);
        GroupLayout.ParallelGroup horizontalGroup1 = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup horizontalGroup2 = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
        List<GroupLayout.ParallelGroup> verticalGroups = new LinkedList<>();
        consulters.forEach((l, b) -> {
            horizontalGroup1.addComponent(l);
            horizontalGroup2.addComponent(b, javax.swing.GroupLayout.Alignment.TRAILING);
            GroupLayout.ParallelGroup verticalGroup = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE);
            verticalGroup.addComponent(l).addComponent(b);
            verticalGroups.add(verticalGroup);
        });
        horizontalGroup2.addComponent(dismissButton);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(mainLabel)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(horizontalGroup1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(horizontalGroup2))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(dismissButton)))
                        .addContainerGap())
        );
        GroupLayout.SequentialGroup sequentialGroup = layout.createSequentialGroup();
        sequentialGroup
                .addContainerGap()
                .addComponent(mainLabel)
                .addGap(18, 18, 18);
        verticalGroups.forEach((g) -> {
            sequentialGroup.addGroup(g);
            sequentialGroup.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        });
        sequentialGroup
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(dismissButton)
                .addContainerGap();
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(sequentialGroup)
        );

        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
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

    private class SwitchToDndFrame extends javax.swing.JFrame {

        /**
         * Creates new form SwitchToDndFrame
         */
        public SwitchToDndFrame() {
            initComponents();
            showOnTop(false);
            this.setLocationRelativeTo(null);
        }

        void display() {
            showOnTop(true);
            jButtonYes.setSelected(true);
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
            jLabel2 = new javax.swing.JLabel();
            jTextFieldMinutes = new javax.swing.JTextField();
            jLabel3 = new javax.swing.JLabel();
            jButtonOk = new javax.swing.JButton();

            setTitle("Switch to Do Not Disturb?");
            setResizable(false);
            setType(java.awt.Window.Type.POPUP);
            addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    formKeyReleased(evt);
                }
            });

            jLabel1.setText("Would you like to switch to Do Not Disturb state now?");

            jButtonYes.setText("Yes");
            jButtonYes.addActionListener((java.awt.event.ActionEvent evt) -> {
                jButtonYesActionPerformed(evt);
            });

            jButtonNo.setText("No");
            jButtonNo.setMaximumSize(new java.awt.Dimension(49, 23));
            jButtonNo.setMinimumSize(new java.awt.Dimension(49, 23));
            jButtonNo.setPreferredSize(new java.awt.Dimension(49, 23));
            jButtonNo.addActionListener((java.awt.event.ActionEvent evt) -> {
                jButtonNoActionPerformed(evt);
            });

            jLabel2.setText("Remind me in");

            jTextFieldMinutes.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
            jTextFieldMinutes.setText("15");
            jTextFieldMinutes.setMaximumSize(new java.awt.Dimension(18, 20));
            jTextFieldMinutes.setMinimumSize(new java.awt.Dimension(18, 20));
            jTextFieldMinutes.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    jTextFieldMinutesKeyReleased(evt);
                }
            });

            jLabel3.setText("minutes:");

            jButtonOk.setText("OK");
            jButtonOk.setMaximumSize(new java.awt.Dimension(49, 23));
            jButtonOk.setMinimumSize(new java.awt.Dimension(49, 23));
            jButtonOk.setPreferredSize(new java.awt.Dimension(49, 23));
            jButtonOk.addActionListener((java.awt.event.ActionEvent evt) -> {
                jButtonOkActionPerformed(evt);
            });

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addGroup(layout.createSequentialGroup()
                                            .addComponent(jButtonYes)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jButtonNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jLabel2)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jTextFieldMinutes, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jLabel3)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jButtonOk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButtonYes)
                                    .addComponent(jButtonNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)
                                    .addComponent(jTextFieldMinutes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3)
                                    .addComponent(jButtonOk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            pack();
        }// </editor-fold>                        

        private void jButtonYesActionPerformed(java.awt.event.ActionEvent evt) {
            changeState(PersonState.DO_NOT_DISTURB);
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
            int minutes;
            try {
                minutes = Integer.parseInt(jTextFieldMinutes.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(rootPane, "Please use only numeric characters.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (minutes < 1) {
                JOptionPane.showMessageDialog(rootPane, "Please use positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.setVisible(false);
            try {
                Thread.sleep(minutes * 60000);
            } catch (InterruptedException ex) {
                log.error("DND reminder sleep interrupted.", ex);
            }
            this.setVisible(true);
        }

        // Variables declaration - do not modify                     
        private javax.swing.JButton jButtonNo;
        private javax.swing.JButton jButtonOk;
        private javax.swing.JButton jButtonYes;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JTextField jTextFieldMinutes;
        // End of variables declaration                   
    }
}
