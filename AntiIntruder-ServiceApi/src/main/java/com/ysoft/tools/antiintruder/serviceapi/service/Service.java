/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.serviceapi.service;

/**
 *
 * @author Bato
 */
public interface Service<T> {

    public Long save(T dto);

    public T findOne(Long id);

    public void delete(Long id);
    
}
