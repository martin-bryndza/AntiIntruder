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
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
    private Map<PersonState, MenuItem> stateItems;
    private final UpdateIconMouseListener updateIconMouseListener;

    private final SwitchToDndFrame switchToDndFrame;
    private final AvailableConsultersMessageFrame availableConsultersMessageFrame;

    private final RestClient client;

    private PersonState currentState;
    private String currentLocation;

    private static final Font BOLD_FONT = Font.decode(null).deriveFont(java.awt.Font.BOLD);
    private Image icon = null;

    private TrayIconManager() {
        icon = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("images/logo.png"));
        updateIconMouseListener = new UpdateIconMouseListener();
        switchToDndFrame = new SwitchToDndFrame();
        availableConsultersMessageFrame = new AvailableConsultersMessageFrame();
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
            trayIcon = createIcon(currentState.getIcon());
            try {
                tray.add(trayIcon);
            } catch (AWTException ex) {
                log.error("Desktop system tray is missing", ex);
            }
            showInfoBubble("Welcome!\nRight-click this icon to change your current state.");
        } else {
            trayIcon.setImage(getTrayIconImage(currentState.getIcon()));
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
        MenuItem dnd = stateItems.get(PersonState.DO_NOT_DISTURB);
        boolean wasDndAvailable = dnd != null && dnd.isEnabled();
        if (!wasDndAvailable && client.isStateChangePossible(PersonState.DO_NOT_DISTURB)) {
            if (dnd != null) {
                dnd.setEnabled(true);
            }
            log.debug("DND is now enabled");
            showInfoBubble("Do not disturb state is possible. Click this bubble for further actions.");
        }
        PersonState newState = client.getState();
        String newLocation = client.getLocation();
        if (!newState.isAwayState()) {
            List<InteractionPerson> availableConsulters = client.getNewAvailableConsulters();
            if (!availableConsulters.isEmpty()) {
                availableConsultersMessageFrame.showAvailableConsultersMessage(availableConsulters);
            }
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
        boolean showAvailableBubble = newState.equals(PersonState.AVAILABLE);
        currentState = newState;
        currentLocation = newLocation;
        initialize(currentState, currentLocation);
        if (showAvailableBubble) {
            int requests = client.getNumberOfRequests();
            showInfoBubble("You have gone Available." + (requests == 0 ? "" : (" You have " + requests + " pending request" + (requests > 1 ? "s" : "") + " for consultation.")));
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
        if (client.isServerOnline()) {
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
            MenuItem locationMenuItem = new MenuItem("Set location..." + (currentLocation == null || currentLocation.isEmpty() ? "" : (" (" + currentLocation + ")")));
            locationMenuItem.addActionListener((ActionEvent) -> {
                requestNewLocation(currentLocation);
            });
            popup.add(locationMenuItem);
        } else {
            MenuItem item = new MenuItem("Server is unreachable");
            item.setEnabled(false);
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

    private TrayIcon createIcon(Image image) {
        TrayIcon trayIconn = new TrayIcon(getTrayIconImage(image));
        return trayIconn;
    }

    private Image getTrayIconImage(Image image) {
//        if (trayIconImage == null) {
//            log.error("Unable to create tray icon");
//            return new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
//        }
        int trayIconWidth = new TrayIcon(image).getSize().width;
        return image.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH);
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
            JOptionPane.showMessageDialog(f, text, title, JOptionPane.PLAIN_MESSAGE, new ImageIcon("images/logo.ico"));
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
        frame.setIconImage(icon);
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
        frame.setIconImage(icon);
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

            dismissButton.addActionListener((ActionEvent) -> {
                showOnTop(false);
            });

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
            availableConsulters.forEach((p) -> {
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
            });

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            consulters.forEach((s) -> model.addRow(new Object[]{s[0], s[1], s[2]/*, "Postpone"*/}));

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
                button.addActionListener((ActionEvent e) -> {
                    fireEditingStopped();
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

    private class SwitchToDndFrame extends javax.swing.JFrame {

        /**
         * Creates new form SwitchToDndFrame
         */
        public SwitchToDndFrame() {
            initComponents();
            showOnTop(false);
            this.setLocationRelativeTo(null);
            setIconImage(icon);
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
            try {
                Thread t = new Thread(new RemindInThread(Integer.parseInt(jTextFieldMinutes.getText()), this));
                t.start();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(rootPane, "Please use only numeric characters.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private class RemindInThread implements Runnable {

            private final int minutes;
            private final JFrame frame;

            public RemindInThread(int minutes, JFrame frame) {
                this.minutes = minutes;
                this.frame = frame;
            }

            @Override
            public void run() {
                if (minutes < 1) {
                    JOptionPane.showMessageDialog(rootPane, "Please use positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                frame.setVisible(false);
                try {
                    Thread.sleep(minutes * 60000);
                } catch (InterruptedException ex) {
                    log.error("DND reminder sleep interrupted.", ex);
                }
                if (!currentState.equals(PersonState.DO_NOT_DISTURB)) {
                    frame.setVisible(true);
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
        private javax.swing.JTextField jTextFieldMinutes;
        // End of variables declaration                   
    }
}
