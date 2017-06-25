/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bato.anyoffice.trayapp.entities;

import java.util.LinkedList;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Bato
 */
public class PendingConsultations {
    
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(PendingConsultations.class);
    
    private static PendingConsultations instance;
    
    private List<Consultation> consultations;
    
    private PendingConsultations() {
        consultations = new LinkedList<>();
    }
    
    public static PendingConsultations getInstance() {
        if (instance == null) {
            instance = new PendingConsultations();
        }
        return instance;
    }
    
    /**
     * 
     * @return Copy of the list of consultations.
     */
    public List<Consultation> getConsultations() {
        return new LinkedList(consultations);
    }
    
    /**
     * Updates the list of pending consultations. Any previously unknown
     * consultations are added to the end of the list. The existing consultations
     * are updated.
     * @param consultations
     * @return Copy of the updated list of consultations.
     */
    public List<Consultation> updateConsultations(List<Consultation> consultations) {
        List<Consultation> newCons = new LinkedList();
        for (Consultation c: consultations){
            int i = consultations.indexOf(c);
            if (i == -1) {
                newCons.add(c);
            } else {
                newCons.add(i, c);
            }
        }
        this.consultations = newCons;
        return getConsultations();
    }
    
    /**
     * Adds consultation into the list of pending consultations if the list does not contain such consultation.
     * @param consultation
     * @return Instance of the consultation in the list of pending consultations.
     */
    public Consultation addConsultation(Consultation consultation){
        return addConsultation(consultation, Integer.MAX_VALUE);
    }
    
    /**
     * Adds consultation into the list of pending consultations if the list does
     * not contain such consultation. If the consultation already exists in the list,
     * the priority stays unchanged.
     *
     * @param consultation
     * @param priority The position to which the new consultation should be added.
     * @return Instance of the consultation in the list of pending
     * consultations.
     */
    public Consultation addConsultation(Consultation consultation, int priority) {
        if (priority > consultations.size()) {
            priority = consultations.size();
        }
        int i = consultations.indexOf(consultation);
        if (i == -1){
            this.consultations.add(priority, consultation);
            return consultation;
        } else {
            return consultations.get(i);
        }
    }
    
    public void removeConsultation(Consultation consultation) {
        this.consultations.remove(consultation);
    }
    
    public void removeConsultation(int priority) {
        this.consultations.remove(priority);
    }
    
    public Consultation getConsultation(int priority) {
        return this.consultations.get(priority);
    }
    
}
