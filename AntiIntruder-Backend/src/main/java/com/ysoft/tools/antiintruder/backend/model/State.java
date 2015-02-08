/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.backend.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author Bato
 */
@Entity
public enum State {

    AVAILABLE("a", 1, 1);
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private long maxDuration;
    private long minDuration;

    private State(){
        
    }
    
    private State(String name, long minDuration, long maxDuration){

    }
    
}
