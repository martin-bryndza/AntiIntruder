/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp;

import eu.bato.anyoffice.trayapp.entities.Consultation;
import java.awt.Image;
import javax.swing.JOptionPane;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Bato
 */
/**
 * Frame for alerting user, that a consultations target has accepted
 * the consultation.
 */
public class CallForRequestedConsultationAlert extends javax.swing.JFrame {

    private final Consultation consultation;
    private final RestClient client;
    
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(CallForRequestedConsultationAlert.class);
    
    public CallForRequestedConsultationAlert(Consultation consultation, RestClient client, Image icon) {
        this.consultation = consultation;
        this.client = client;
        initComponents();
        this.setLocationRelativeTo(null);
        setIconImage(icon);
    }

    public Consultation getConsultation() {
        return consultation;
    }

    @SuppressWarnings("unchecked")                        
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        setTitle("Consultation");
        setResizable(false);

        jButton1.setText("I'm coming");        
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                client.acceptCallFromTarget(consultation.getId());
                showInProgressDialog();
            }
        });

        jButton2.setText("Not now");        
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                client.cancelCallToRequester(consultation.getId());
                close();
            }
        });

        jButton3.setText("Cancel the consultation");        
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                client.cancelConsultationByRequester(consultation.getId());
                close();
            }
        });

        jScrollPane2.setBorder(null);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTextPane1.setEditable(false);
        jTextPane1.setBackground(new java.awt.Color(240, 240, 240));
        jTextPane1.setBorder(null);
        jTextPane1.setText(consultation.getTargetName() + " has accepted your request for consultation '" + consultation.getMessage() + "' and is waiting for you. ");
        jScrollPane2.setViewportView(jTextPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton3)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButton1)
                                .addComponent(jButton2)
                                .addComponent(jButton3))
                        .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }                       

    public void showMessage() {
        this.setAlwaysOnTop(true);
        this.setVisible(true);
    }
    
    public void showInProgressDialog() {
        String[] options = {"Settle the consultation", "Request again"};
        int selectedOption = JOptionPane.showOptionDialog(this,
                consultation.getTargetName() + " is waiting for you. His probable location is " + consultation.getTargetLocation() + ".",
                "Hurry up!",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (selectedOption == JOptionPane.YES_OPTION) {
            client.settleConsultation(consultation.getId());
        } else {
            client.cancelCallToRequester(consultation.getId());
        }
        close();
    }
    
    public void close() {
        setVisible(false);
        dispose();
    }
                    
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;       
}
