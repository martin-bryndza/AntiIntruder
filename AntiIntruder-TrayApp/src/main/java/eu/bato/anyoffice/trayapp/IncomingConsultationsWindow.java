/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import static eu.bato.anyoffice.trayapp.TrayIconManager.log;
import eu.bato.anyoffice.trayapp.entities.Consultation;
import eu.bato.anyoffice.trayapp.entities.PendingConsultationState;
import eu.bato.anyoffice.trayapp.entities.PendingConsultations;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Bato
 */
/**
 * Frame for displaying people available to be contacted.
 */
public class IncomingConsultationsWindow extends javax.swing.JFrame {

    private JLabel mainLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private JButton dismissButton;
    private JTable consultationsTable;
    private final RestClient client;

    public IncomingConsultationsWindow(RestClient client, Image icon) {
        initComponents();
        showOnTop(false);
        this.setLocationRelativeTo(null);
        setIconImage(icon);
        this.client = client;
    }

    private void initComponents() {
        mainLabel = new javax.swing.JLabel("Following people have requested a consultation with you:\n");
        jScrollPane1 = new javax.swing.JScrollPane();
        dismissButton = new JButton("Dismiss all");

        dismissButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(dismissButton, "Sorry, not supported yet.");
            }
        });

        setTitle("Any Office - Pending consultations");
        setResizable(false);
        setType(java.awt.Window.Type.POPUP);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        String[] columnNames = {"Name", "Message", ""};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        consultationsTable = new javax.swing.JTable(model);
        consultationsTable.setEnabled(true);
        consultationsTable.setRowSelectionAllowed(false);
        consultationsTable.setColumnSelectionAllowed(false);
        consultationsTable.setCellSelectionEnabled(false);
        consultationsTable.setFocusable(false);
        consultationsTable.setShowHorizontalLines(false);
        consultationsTable.setShowVerticalLines(false);
        consultationsTable.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
            DefaultTableModel dm = (DefaultTableModel) consultationsTable.getModel();
            dm.setRowCount(0);
        }
    }

    void showIncomingConsultationsMessage(List<Consultation> consultations) {
        consultations = PendingConsultations.getInstance().updateConsultations(consultations);
        DefaultTableModel model = (DefaultTableModel) consultationsTable.getModel();
        model.setRowCount(0);
        if (consultations.isEmpty()) {
            hideIncomingConsultationsMessage();
            dispose();
            removeAll();
            return;
        }

        jScrollPane1.setViewportView(consultationsTable);

        for (Consultation c : consultations) {
            String label;
            if (c.getPendingState().equals(PendingConsultationState.PENDING)) {
                label = "Call";
            } else if (c.getPendingState().equals(PendingConsultationState.WAITING_FOR_REQUESTER)) {
                label = "Waiting...";
            } else {
                label = "Settle";
            }
            log.debug(label);
            model.addRow(new Object[]{c.getRequesterName(), c.getMessage(), label});
        }

        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf(e.getActionCommand());
                Consultation consultation = PendingConsultations.getInstance().getConsultation(modelRow);
                if (consultation.getPendingState().equals(PendingConsultationState.PENDING)) {
                    consultation.setPendingState(PendingConsultationState.WAITING_FOR_REQUESTER);
                    client.callRequester(consultation.getId());
                } else if (consultation.getPendingState().equals(PendingConsultationState.WAITING_FOR_REQUESTER)) {
                    consultation.setPendingState(PendingConsultationState.PENDING);
                    client.cancelCallToRequester(consultation.getId());
                } else {
                    PendingConsultations.getInstance().removeConsultation(consultation);
                    ((DefaultTableModel) table.getModel()).removeRow(modelRow);
                    client.settleConsultation(consultation.getId());
                }
                TrayIconManager.getInstance().updateFromServer();
            }
        };

        ButtonColumn buttonColumn = new ButtonColumn(consultationsTable, action, 2);
        buttonColumn.setMnemonic(KeyEvent.VK_D);
        pack();
        showOnTop(true);
    }

    void hideIncomingConsultationsMessage() {
        showOnTop(false);
    }

    private void formKeyReleased(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == (KeyEvent.VK_ENTER)) {
            showOnTop(false);
        }
    }
}
