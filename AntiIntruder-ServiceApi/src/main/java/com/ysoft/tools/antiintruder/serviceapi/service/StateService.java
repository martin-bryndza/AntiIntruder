/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ysoft.tools.antiintruder.serviceapi.service;

import com.ysoft.tools.antiintruder.serviceapi.dto.StateDto;
import java.util.List;

/**
 *
 * @author Bato
 */
public interface StateService extends Service<StateDto>{
    
    public List<StateDto> findAll();
    
}
