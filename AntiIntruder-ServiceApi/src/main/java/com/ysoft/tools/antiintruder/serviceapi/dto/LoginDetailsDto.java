/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.serviceapi.dto;

/**
 *
 * @author Bato
 */
public class LoginDetailsDto {
    
    private final String password;
    private final PersonRole role;

    public LoginDetailsDto(String password, PersonRole role) {
        this.password = password;
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public PersonRole getRole() {
        return role;
    }
    
}
